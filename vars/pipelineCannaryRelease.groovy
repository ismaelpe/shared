import groovy.transform.Field
import com.project.alm.*
import groovy.json.JsonSlurperClassic
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams

@Field String gitURL
@Field String gitCredentials
@Field String jenkinsPath

@Field String version
@Field String oldVersion
@Field String pathToRepo
@Field String repoName
@Field String artifactSubType
@Field String artifactType
@Field String pipelineOrigId
@Field String originBranch
@Field String commitId
@Field String artifact
@Field int actualPercentatge
@Field String component
@Field String user
@Field String executionProfileParam
@Field String targetAlmFolderParam
@Field String loggerLevel

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean initGpl
@Field boolean successPipeline

//FIXME: You can remove this when parallel tests are completed
//Parallel refresh control variables
@Field boolean refreshFailed
@Field int start
@Field int end
@Field boolean refreshInProcess
@Field String microNameAndMajor

@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters
    
    gitURL = "https://git.svb.digitalscale.es/"
    gitCredentials = "GITLAB_CREDENTIALS"
    jenkinsPath = "alm/services"

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
    version = params.versionParam
    oldVersion = params.oldVersionParam
    pathToRepo = params.pathToRepoParam
    repoName = params.repoNameParam
    artifactSubType = params.artifactSubTypeParam
    artifactType = params.artifactTypeParam
    pipelineOrigId = params.pipelineOrigId
    originBranch = params.originBranchParam
    commitId = params.commitIdParam
    artifact = params.artifactParam
    actualPercentatge = params.actualPercentatgeParam as Integer
    component = params.componentParam
    user = params.userId
    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam
    loggerLevel = params.loggerLevel

    /**
     * 1. Recoger el artifact
     * 2. Copy config
     * 3. Desplegar a PRO
     * 3.5. Preparar Canario
     */
    initGpl = false
    successPipeline = false

    //FIXME: You can remove this when parallel tests are completed
    //Parallel refresh control variables
    refreshFailed = false
    start = 0
    end = 0
    refreshInProcess = true
    microNameAndMajor = ""
    
    almEvent = null
    initCallStartMillis = new Date().getTime()
    
    pipeline {        
        agent {    node (almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            JNKMSV = credentials('JNKMSV-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
            https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
            executionProfile = "${executionProfileParam ? executionProfileParam : 'DEFAULT'}"
        }        
        stages {
            stage('init-data') {
                steps {
                    initDataStep()
                }
            }
            stage('validate-dependencies'){
                when {
                    allOf {
                        expression { actualPercentatge <= CanaryUtilities.finalPercentage(pomXmlStructure.artifactName) }
                        expression { actualPercentatge != -1 }
                    }
                }
                steps {
                    validateDependenciesStep()
                }
            }            
            stage('modify-percentatge') {
                when {
                    expression { actualPercentatge <= CanaryUtilities.finalPercentage(pomXmlStructure.artifactName) }
                }
                steps {
                    modifyPercentatgeStep()
                }
            }
            stage('refresh-app') {
                when {
                    //FIXME: You can remove this microNameAndMajor condition when parallel tests are completed
                    expression { actualPercentatge <= CanaryUtilities.finalPercentage(pomXmlStructure.artifactName) && microNameAndMajor != 'demoarqalm-micro-1' }
                }
                steps {
                    refreshAppStep()
                }
            }
            stage('Parallel Stage') {
                when {
                    expression { actualPercentatge <= CanaryUtilities.finalPercentage(pomXmlStructure.artifactName) && microNameAndMajor == 'demoarqalm-micro-1' }
                }
                parallel {
                    stage('refresh-app-parallel') {
                        steps {
                            refreshAppParallelStep()
                        }
                    }
                    stage('threaddump-center-1') {
                        when {
                            expression { refreshInProcess }
                        }
                        steps {
                            threaddumpCenter1Step()
                        }
                    }
                    stage('threaddump-center-2') {
                        when {
                            expression { refreshInProcess }
                        }
                        steps {
                            threaddumpCenter2Step()
                        }
                    }
                }
            }
            stage('end-pipeline') {
                when {
                    expression { actualPercentatge <= CanaryUtilities.finalPercentage(pomXmlStructure.artifactName) }
                }
                steps {
                    endPipelineStep()
                }
            }
        }
        post {
            success {
                endPipelineSuccessStep()
            }
            failure {
                endPipelineFailureStep()
            }
            always {
                endPipelineAlwaysStep()
            }
        }
    }
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/**
 * Stage initDataStep
 */
def initDataStep() {
    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null
    //Revisar pomXmlStructure=getGitRepo(pathToRepo,'',repoName,false,ArtifactType.valueOfType(artifactType),ArtifactSubType.valueOfSubType(artifactSubType),version,true)
    pomXmlStructure = new PomXmlStructure(ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), artifact, version, component)
    currentBuild.displayName = "Cannary_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"

    pipelineData = new PipelineData(PipelineStructureType.INC_CANNARY, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.commitId = commitId
    pipelineData.pushUser = user
    pipelineData.initFromNonGit(GlobalVars.PRO_ENVIRONMENT.toString(), pathToRepo, originBranch, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), repoName)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam);

    almEvent = new KpiAlmEvent(
        pomXmlStructure, pipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_INC_CANNARY)

    if (actualPercentatge >= CanaryUtilities.finalPercentage(pomXmlStructure.artifactName)) {
        pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage = CanaryUtilities.finalPercentage(pomXmlStructure.artifactName) + 1
        printOpen("Cannary Percentatge more than ${CanaryUtilities.finalPercentage(pomXmlStructure.artifactName)}", EchoLevel.DEBUG)
    } else if (actualPercentatge == CanaryUtilities.rollbackPercentage()) {
        pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage = CanaryUtilities.initialPercentage()
        printOpen("Canary percentage is gonna be changed to ${pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage}. This is a rollback", EchoLevel.DEBUG)
    } else {
        pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage = CanaryUtilities.incrementPercentage(actualPercentatge)
        if (pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage == null) pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage = CanaryUtilities.finalPercentage(pomXmlStructure.artifactName)
        printOpen("Current canary percentage is ${actualPercentatge} and is gonna be changed to ${pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage}", EchoLevel.DEBUG)
    }

    pipelineData.pipelineStructure.pipelineId = pipelineData.pipelineStructure.pipelineId + "-" + pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage
    pipelineData.prepareResultData(version, artifact, component)
    pipelineData.buildCode = pomXmlStructure.artifactVersion
    pipelineData.pipelineStructure.resultPipelineData.oldVersionInCurrentEnvironment = oldVersion
    
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_STARTED, KpiLifeCycleStatus.OK)
    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100");
    initGpl = true
    debugInfo(null, pomXmlStructure, pipelineData)

    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.INCREASE_CANARY_$_STARTED, KpiLifeCycleStatus.OK, "BETA")
    
    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")
    printOpen("Init deploy with canary on ${pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage}%", EchoLevel.INFO)
}

/**
 * Stage validateDependenciesStep
 */
def validateDependenciesStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "110");                       
    try {
        validateDependenciesCatMsv(pipelineData, pomXmlStructure, false,"BETA")
        sendStageEndToGPL(pomXmlStructure, pipelineData, "110")
    }catch(Exception e) {
        sendStageEndToGPL(pomXmlStructure, pipelineData, "110", e.getMessage(), null, "warning")
    }
}

/**
 * Stage modifyPercentatgeStep
 */
def modifyPercentatgeStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "200");
    //build the workspace
    printOpen("Building the branch", EchoLevel.DEBUG)

    pushConfigFiles(pomXmlStructure, pipelineData, true, true, false)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "200")

    //FIXME: You can remove this when parallel tests are completed
    microNameAndMajor = BmxUtilities.calculateArtifactId(pomXmlStructure, pipelineData.branchStructure,true).toLowerCase()
    printOpen("microNameAndMajor is ${microNameAndMajor}", EchoLevel.DEBUG)
}

/**
 * Stage refreshAppStep
 */
def refreshAppStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "300");

    pipelineData.deployFlag == true // FIXME: ¿?
    initCloudDeploy(pomXmlStructure, pipelineData)

    if (pipelineData.deployOnCloud) refreshConfigurationViaRefreshBus(pomXmlStructure, pipelineData)

    sendStageEndToGPL(pomXmlStructure, pipelineData, "300")
    sendPipelineUpdateToGPL(initGpl, pomXmlStructure, pipelineData, '')
}

/**
 * Stage refreshAppParallelStep
 */        
def refreshAppParallelStep(){
    sendStageStartToGPL(pomXmlStructure, pipelineData, "300");
    pipelineData.deployFlag == true
    initCloudDeploy(pomXmlStructure, pipelineData)

    if (pipelineData.deployOnCloud) {
        printOpen("Refresh starting...", EchoLevel.INFO)

        try {
            start = new Date().getTime()
            refreshCloud(pomXmlStructure, pipelineData, GlobalVars.ENDPOINT_REFRESH, true)
            end = new Date().getTime()

            refreshFailed = (end - start) >= 60000
        } catch(Exception e) {
            refreshFailed = true
        } finally {
            refreshInProcess = false
            printOpen("Refresh ended", EchoLevel.INFO)
        }
    } else {
        refreshInProcess = false
        printOpen("No refresh will be done", EchoLevel.INFO)
    }
    
    sendStageEndToGPL(pomXmlStructure, pipelineData, "300")
    sendPipelineUpdateToGPL(initGpl, pomXmlStructure, pipelineData, '')
}

/**
 * Stage threaddumpCenter1Step
 */
def threaddumpCenter1Step() {
    printOpen("Threaddumps starting...", EchoLevel.DEBUG)
    def threadDumps = []
    String urlThreadDump = refreshCloud.getActuatorRefreshUri(GlobalVars.BMX_CD1, pomXmlStructure.isArchProject(), microNameAndMajor+'-beta', GlobalVars.ENDPOINT_THREADDUMP)

    try {
        timeout(time: 30, unit: 'MINUTES') {
            while(refreshInProcess) {
                printOpen("Doing threaddump...", EchoLevel.DEBUG)

                def response = runActuatorRefresh.sendRequest(urlThreadDump, [consoleLogResponseBody: false])
                try {
                    //threadDumps += [time: new Date().toString(), status: response?.status, body: new JsonSlurperClassic().parseText(response?.content)]
                    threadDumps += [time: new Date().toString(), status: response?.status, body: response?.content]
                } catch(err) {
                    threadDumps += [time: new Date().toString(), status: response?.status, body: response?.content]
                }
                sleep 15
            }
        }
    } catch(err) {
        refreshFailed = true
    }

    printOpen("Thread dumps ended on Center 1", EchoLevel.DEBUG)

    if (refreshFailed) {
        try {
            print groovy.json.JsonOutput.toJson(threadDumps)
        } catch (Exception e) {
            printOpen("Parsing as JSON failed", EchoLevel.ERROR)
            for(threadDump in threadDumps) {
                print threadDump
            }

        }
    }
}

/**
 * Stage threaddumpCenter2Step
 */
def threaddumpCenter2Step() {
    printOpen("Threaddumps starting...", EchoLevel.DEBUG)
    def threadDumps = []
    String urlThreadDump = refreshCloud.getActuatorRefreshUri(GlobalVars.BMX_CD2, pomXmlStructure.isArchProject(), microNameAndMajor+'-beta', GlobalVars.ENDPOINT_THREADDUMP)

    try {
        timeout(time: 30, unit: 'MINUTES') {
            while(refreshInProcess) {
                printOpen("Doing threaddump...", EchoLevel.DEBUG)
                
                def response = runActuatorRefresh.sendRequest(urlThreadDump, [consoleLogResponseBody: false])
                try {
                    //threadDumps += [time: new Date().toString(), status: response?.status, body: new JsonSlurperClassic().parseText(response?.content)]
                    threadDumps += [time: new Date().toString(), status: response?.status, body: response?.content]
                } catch(err) {
                    threadDumps += [time: new Date().toString(), status: response?.status, body: response?.content]
                }
                sleep 15
            }
        }
    } catch(err) {
        refreshFailed = true
    }

    printOpen("Thread dumps ended on Center 2", EchoLevel.DEBUG)

    if (refreshFailed) {
        try {
            print groovy.json.JsonOutput.toJson(threadDumps)
        } catch (Exception e) {
            printOpen("Parsing as JSON failed", EchoLevel.ERROR)
            for(threadDump in threadDumps) {
                print threadDump
            }
        }
    }
}

/**
 * Stage endPipelineStep
 */
def endPipelineStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "400");
    pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, component)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "400")
}

/**
 * Stage endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}

/**
 * Stage endPipelineSuccessStep
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("Se success el pipeline ${successPipeline}", EchoLevel.DEBUG)
    if ( almEvent!=null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis-initCallStartMillis))                        
    }
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.INCREASE_CANARY_$_FINISHED, KpiLifeCycleStatus.OK, "BETA")
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.OK)
    if (pipelineData.getExecutionMode().invokeNextActionAuto()) {
        printOpen("Modo test activado en fase de canary", EchoLevel.INFO)
        invokeNextJob(pipelineData, pomXmlStructure)
    }
}

/**
 * Stage endPipelineFailureStep
 */
def endPipelineFailureStep() {
    printOpen("Se failure el pipeline ${successPipeline}", EchoLevel.DEBUG)

    successPipeline = false 
    if ( almEvent!=null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis-initCallStartMillis))
    }

    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.INCREASE_CANARY_$_FINISHED, KpiLifeCycleStatus.KO, "BETA")
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.KO)
}
