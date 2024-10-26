import groovy.transform.Field
import com.project.alm.*
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams

@Field NexusUtils nexus

@Field String gitURL
@Field String gitCredentials
@Field String jenkinsPath

@Field String originBranch
@Field String pathToRepo
@Field String repoName
@Field String artifactSubType
@Field String artifactType
@Field String pipelineOrigId
@Field String commitId
@Field String executionProfileParam
@Field String targetAlmFolderParam
@Field String[] mvnAdditionalParameters
@Field String envParam
@Field String user

@Field String deployCloudPhases
@Field String resultDeployCloud
@Field CloudStateUtility cloudStateUtilitity

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean initGpl
@Field boolean successPipeline

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

    nexus = new NexusUtils(this)

    gitURL = "https://git.svb.lacaixa.es/"
    gitCredentials = "GITLAB_CREDENTIALS"
    jenkinsPath = "alm/services"

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
    originBranch = params.originBranchParam
    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam
    artifactSubType = params.artifactSubTypeParam
    artifactType = params.artifactTypeParam
    pipelineOrigId = params.pipelineOrigId
    commitId = params.commitIdParam
    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam
    mvnAdditionalParameters = params.mvnAdditionalParametersParam?.split(",")	
	envParam = params.envParam
    user = params.userId

    deployCloudPhases = "01-pre-deploy"
    resultDeployCloud = "OK"
    cloudStateUtilitity = null

    initGpl = false
    successPipeline = false

	almEvent = null
	initCallStartMillis = new Date().getTime()

    /**
     * 1. Modificar la release del artefacto de RC a Release definitiva sin tag
     * 2. Compilar
     * 2. Desplegar a PRE
     * 2.5. Etiquetar cliente ¿? Publish , etc
     * 3. Push GIT + etiqueta
     */    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
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
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
            executionProfile = "${executionProfileParam ? executionProfileParam : 'DEFAULT'}"
        }       
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
			stage('check-Cloud-availiability'){
				when {
					expression { pipelineData.deployFlag && pipelineData.deployOnCloud }
				}
				steps {
                    checkCloudAvailiabilityStep()
				}
			}
            stage('prepare-Release') {
                steps {
                    prepareReleaseStep()
                }
            }		
			stage('deploy-bbdd-scripts') {
				when {
					expression { !pipelineData.isPushCI() }
				}
				steps {
                    deployBbddScriptsStep()
				}
			}
			stage('create-MR') {
				when {
					expression { !pipelineData.isPushCI() && Environment.valueOfType(envParam)==Environment.PRO}
				}
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
                endiPipelineAlwaysStep()
            }
        }
    }
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/**
 * Stage 'getGitRepoStep'
 */
def getGitRepoStep() {
    initGlobalVars(pipelineParams)

    printOpen("Extract GIT Repo ${pathToRepo} ${originBranch}", EchoLevel.INFO)
    pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false, true, true)
    pipelineData = new PipelineData(PipelineStructureType.DEPLOY_BBDD_SCRIPT, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.commitId = commitId
    pipelineData.initFromRelease(pathToRepo, originBranch, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), repoName, false)
    
    printOpen("Sacamos los valores ${pipelineData.pipelineStructure.resultPipelineData}", EchoLevel.INFO)
    pipelineData.pipelineStructure.resultPipelineData.version = pomXmlStructure.artifactVersion
    pipelineData.mvnAdditionalParameters = mvnAdditionalParameters
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam);
    pipelineData.pushUser = user

    if (Environment.valueOfType(envParam)==Environment.PRO) {
        pipelineData.bmxStructure=new ProBmxStructure()
    }
    almEvent = new KpiAlmEvent(
        pomXmlStructure, pipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_DEPLOY_BBDD)

    pipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()

    //FIXME Improve this by reading the file and creating a Map that is to be stored in pipelineData.jenkinsFileParams
    String jenkinsFile = readFile 'Jenkinsfile'
    if (jenkinsFile?.replace(" ", "").contains("cleanInstallBeforeDeploy:true")) {

        pipelineData.jenkinsFileParams = [maven: [cleanInstallBeforeDeploy: true]]

    }
    
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_STARTED, KpiLifeCycleStatus.OK)
    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100")
    initGpl = true

    //Buscamos version de arquitectura
    //Validamos que sea superior a la minim
    calculateArchVersionWithModules(pomXmlStructure)
    try {
        printOpen("The environment is ${pipelineData.bmxStructure.environment}", EchoLevel.INFO)
        if (pipelineData.branchStructure.branchType == BranchType.HOTFIX) {
            printOpen("Hotfix branch. Architecture version not validated.", EchoLevel.INFO)
        } else {
            pomXmlStructure.validateArtifact(pipelineData.bmxStructure.environment)
        }
        //pomXmlStructure.validateArtifact()
    } catch (Exception e) {
        sendStageEndToGPL(pomXmlStructure, pipelineData, "100", e.getMessage(), null, "error")
        throw e
    }

    debugInfo(pipelineParams, pomXmlStructure, pipelineData)

    //INIT AND DEPLOY
    initCloudDeploy(pomXmlStructure, pipelineData)

    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")
}

/**
 * Stage 'checkCloudAvailiabilityStep'
 */
def checkCloudAvailiabilityStep() {
    printOpen("The artifact ${pomXmlStructure.artifactName}  from group ${pomXmlStructure.groupId} the micro to deploy is ${pomXmlStructure.artifactMicro}", EchoLevel.INFO)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "110")
    try {
        checkCloudAvailability(pomXmlStructure,pipelineData,envParam,"BOTH")
        sendStageEndToGPL(pomXmlStructure, pipelineData, "110")
    } catch (Exception e) {
        sendStageEndToGPL(pomXmlStructure, pipelineData, "110", Strings.toHtml(e.getMessage()), null, "error")
        throw e
    }
}

/**
 * Stage 'prepareReleaseStep'
 */
def prepareReleaseStep() {
    kpiLogger(pomXmlStructure, pipelineData,KpiLifeCycleStage.CREATE_RELEASE_STARTED, KpiLifeCycleStatus.OK)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "200")
    validateBranch(pomXmlStructure.getArtifactVersionWithoutQualifier(), pipelineData.branchStructure)
    updateVersionForRelease(pomXmlStructure)
    calculatePreviousInstalledVersionInEnvironment(pipelineData, pomXmlStructure)
    pipelineData.buildCode = pomXmlStructure.artifactVersion
    currentBuild.displayName = "Creation_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    sendStageEndToGPL(pomXmlStructure, pipelineData, "200")
    
    printOpen("Prepare release ${pipelineData.isPushCI()}", EchoLevel.INFO)
}

/**
 * Stage 'deployBbddScriptsStep'
 */
def deployBbddScriptsStep() {
    if (hasBBDD(pomXmlStructure,pipelineData,true)) {
        sendStageStartToGPL(pomXmlStructure, pipelineData, "406")
        printOpen("Deploying sql to the BBDD", EchoLevel.INFO)
        pipelineData.pipelineStructure.resultPipelineData.environment=envParam
        try {
            def missatge=generateSqlScriptRelease(pomXmlStructure,pipelineData,false)
            sendStageEndToGPL(pomXmlStructure, pipelineData, "406",missatge,null,"ended")
        }catch(Exception e) {
            sendStageEndToGPL(pomXmlStructure, pipelineData, "406", Strings.toHtml(e.getMessage()), null, "error")
            throw e
        }	
    }else {
        String nombreFicheroRelease="${GlobalVars.SQL_RELEASE_DIRECTORY}changeSet-${pomXmlStructure.artifactVersion}.yml"
        sendStageStartToGPL(pomXmlStructure, pipelineData, "406")
        printOpen("No tiene el script release con la nomenclatura pactada. Revise el fichero dentro del directorio release. El nombre del fichero debe ser este (el fichero es este ${nombreFicheroRelease}.", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "406", Strings.toHtml("No tiene el script release con la nomenclatura pactada. Revise el fichero dentro del directorio release. El nombre del fichero debe ser este (el fichero es este ${nombreFicheroRelease}."), null, "error")
        throw new Exception("No tiene el script release con la nomenclatura pactada")
    }
}

/**
 * Stage 'createMRStep'
 */
def createMRStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "500")
    mergeRequestToMaster(pipelineData, pomXmlStructure, 'master')						
    sendStageEndToGPL(pomXmlStructure, pipelineData, "500")
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("Se success el pipeline ${successPipeline}", EchoLevel.INFO)
    if ( almEvent!=null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis-initCallStartMillis))
    }
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    kpiLogger(pomXmlStructure, pipelineData,KpiLifeCycleStage.CREATE_RELEASE_FINISHED, KpiLifeCycleStatus.OK)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.OK)

    if (pipelineData.getExecutionMode().invokeNextActionAuto() && !pipelineData.isPushCI()) {
        printOpen("Modo test activado en fase de crear release", EchoLevel.INFO)
        invokeNextJob(pipelineData, pomXmlStructure)
    }
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    if ( almEvent!=null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis-initCallStartMillis))
    }
    printOpen("Se failure el pipeline ${successPipeline}", EchoLevel.ERROR)
    kpiLogger(pomXmlStructure, pipelineData,KpiLifeCycleStage.CREATE_RELEASE_FINISHED, KpiLifeCycleStatus.KO)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.KO)
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
}

/**
 * Stage 'endiPipelineAlwaysStep'
 */
def endiPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}

