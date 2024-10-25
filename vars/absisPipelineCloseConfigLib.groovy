import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

@Field String gitURL
@Field String gitCredentials
@Field String jenkinsPath
@Field boolean initGpl

@Field String originBranch
@Field String pathToRepo
@Field String repoName
@Field String artifactSubType
@Field String artifactType
@Field String user
@Field String pipelineOrigenId

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean successPipeline

@Field String enviroment
@Field String artifact
@Field String version

@Field boolean existAncient
@Field boolean isMicro
@Field String commitId

@Field String artifactId
@Field BmxStructure bmxStructure
@Field DeployStructure deployStructure
@Field def component

@Field String executionProfileParam
@Field String targetAlmFolderParam

@Field String loggerLevel
@Field String agentParam

//Pipeline para realizar el cierre de una release de un config-conf
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    //Mantener estos parametros/variables por si se deben generar estructuras de datos para enviar a GPL
    gitURL = "https://git.svb.lacaixa.es/"
    gitCredentials = "GITLAB_CREDENTIALS"
    jenkinsPath = "absis3/services"
    initGpl = false

    originBranch = params.originBranchParam
    pathToRepo = params.pathToRepoParam
    repoName =  params.repoParam
    artifactSubType = params.artifactSubTypeParam
    artifactType = params.artifactTypeParam
    user = params.userId
    pipelineOrigenId = params.pipelineOrigId

    successPipeline = false

    enviroment = params.environmentParam
    artifact = params.artifactParam
    version = params.versionParam

    existAncient = params.existAncientParam.toString().toBoolean()
    isMicro = params.isMicroParam.toString().toBoolean()
    commitId = params.commitIdParam

    artifactId = artifact + "-" + version
    component = params.componentParam

    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam

    loggerLevel = params.loggerLevel
    agentParam = params.agent
    
    pipeline {      
		agent {	node (absisJenkinsAgent(agentParam)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
			JNKMSV = credentials('JNKMSV-USER-TOKEN')
            ICP_CERT = credentials('icp-absis3-pro-cert')
            ICP_PASS = credentials('icp-absis3-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
            executionProfile = "${executionProfileParam ? executionProfileParam : 'DEFAULT'}"
        }
        stages {
            stage('init-pipeline') {
                steps {
                    initPipelineStep()
                }
            }

            stage('create-MR') {
                steps {
                    createMRStep()
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
 * Stage initPipelineStep
 */
def initPipelineStep() {
    printOpen("Booleans ${existAncientParam} ${isMicroParam} ${existAncient} ${isMicro}", EchoLevel.ALL)
    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null
    printOpen("Extract GIT Repo ${pathToRepo} ${originBranch}", EchoLevel.INFO)

    //DisplayName
    pipelineData = new PipelineData(PipelineStructureType.CLOSE, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.initFromClose(enviroment, pathToRepo, originBranch, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), repoName)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam);
    pomXmlStructure = new PomXmlStructure(ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), artifact, version, component)
    pipelineData.commitId = commitId

    currentBuild.displayName = "Closing_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    pipelineData.pushUser = user

    pipelineData.buildCode = pomXmlStructure.artifactVersion
    printOpen("Branch Type: ${pipelineData.branchStructure.branchType}", EchoLevel.INFO)

    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigenId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100");
    
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_STARTED, KpiLifeCycleStatus.OK)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.CLOSE_STARTED, KpiLifeCycleStatus.OK)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_STARTED, KpiLifeCycleStatus.OK, "PRO")
    
    pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName, pomXmlStructure.artifactType, pomXmlStructure.artifactSubType, originBranch)
    
    initGpl = true
    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")
}

/**
 * Stage createMRStep
 */
def createMRStep(){
    sendStageStartToGPL(pomXmlStructure, pipelineData, "600")
    if (!(pipelineData.getExecutionMode().invokeNextActionAuto() && ObtainNextJobOptionsUtils.hasNextJob(pipelineData.getExecutionMode().actionFlag(), pipelineData.pipelineStructure.resultPipelineData.getAcciones(true)))) {
        mergeRequestToMaster(pipelineData, pomXmlStructure, 'master')
    }
    printOpen("ArtifactType: ${pipelineData.pipelineStructure.resultPipelineData.artifactType} ArtifactSubType: ${pipelineData.pipelineStructure.resultPipelineData.artifactSubType}", EchoLevel.DEBUG)
    if (pipelineData.pipelineStructure.resultPipelineData.artifactSubType==ArtifactSubType.SRV_CFG || pipelineData.pipelineStructure.resultPipelineData.artifactSubType==ArtifactSubType.ARCH_CFG) {
        printOpen("CFG ", EchoLevel.DEBUG)
    }else {
        printOpen("Another thing", EchoLevel.DEBUG)
    }
    sendStageEndToGPL(pomXmlStructure, pipelineData, "600")
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
    printOpen("SUCCESS", EchoLevel.INFO)
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_FINISHED, KpiLifeCycleStatus.OK, "PRO")
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.CLOSE_FINISHED, KpiLifeCycleStatus.OK)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.OK)
        
    if (pipelineData.getExecutionMode().invokeNextActionAuto() && !pipelineData.isPushCI()) {
        printOpen("Modo test activado en fase de cierre", EchoLevel.INFO)
        invokeNextJob(pipelineData, pomXmlStructure)
    }
}

/**
 * Stage endPipelineFailureStep
 */
def endPipelineFailureStep() { 
    successPipeline = false
    printOpen("FAILURE", EchoLevel.ERROR)
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_FINISHED, KpiLifeCycleStatus.KO, "PRO")
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.CLOSE_FINISHED, KpiLifeCycleStatus.KO)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.KO)
}
