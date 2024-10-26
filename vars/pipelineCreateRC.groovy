import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

@Field String gitURL
@Field String gitCredentials
@Field String jenkinsPath

@Field String originBranch
@Field String pathToRepo
@Field String repoName
@Field String pipelineOrigId
@Field String user
@Field String executionProfileParam
@Field String targetAlmFolderParam
@Field String agentParam
@Field String commitId
@Field String loggerLevel
@Field String artifactType
@Field String artifactSubType

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

    gitURL = "https://git.svb.digitalscale.es/"
    gitCredentials = "GITLAB_CREDENTIALS"
    jenkinsPath = "alm/services"

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
    originBranch = params.originBranchParam
    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam
    pipelineOrigId = params.pipelineOrigId
    user = params.userId
    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam
    agentParam = params.agent
    commitId = params.commitIdParam
    loggerLevel = params.loggerLevel
    artifactType = params.artifactTypeParam
    artifactSubType = params.artifactSubTypeParam
    
    initGpl = false
    successPipeline = false
	
	almEvent = null
	initCallStartMillis = new Date().getTime()
    /*
     * Pasos a seguir:
     * 0- git clone
     * 1- Validar el nombre de la rama origen
     * 2- Bajar el codigo del git y de la rama seleccionada
     * 3- Recoger del pom el artifact y la version
     * 4- Generar la rama.... la tenemos?
     * 5- Modificar en el pom con al nueva versión... deberia ser la VERSION-RC0
     * 6- Subir a la rama de release
     * 7- Modificar la master o rama origen con la versión MAJOR.MINOR+1
     * 8- Marcar con tag la rama master
     * 9- Feina feta
     * */
    pipeline {      
		agent {	node (almJenkinsAgent(agentParam)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
        /* options{
         gitLabConnection('gitlab')
         }*/
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
            sendLogsToGpl = true
        }
        //Atencion que en el caso que estemos en un MergeRequest... quizas solo debamos validar la issue
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
			stage('checkmarx-scan') {
				steps {
                    checkmarxScanStep()
				}
			}
            stage('prepare-RC') {
                steps {
                    prepareRCStep()
                }
            }/*
            stage('verify-PRO-state') {
                when {
                    expression { pomXmlStructure.isMicro() }
                }
                steps {
                   verifyProState()
                }
            }*/
            stage('next-Minor-Master') {
                steps {
                  nextMinorMaster()
                }
            }
            stage('push-repo-url') {
                steps {
                    pushRepoUrlStage()
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
 * Step getGitRepoStep
 */
def getGitRepoStep() {
    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null
    printOpen("The Git Repository is: ${pathToRepo} ${originBranch}", EchoLevel.DEBUG)
    pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, true, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false)
    printOpen("Jenkinsfile is:", EchoLevel.DEBUG)
    printFile("Jenkinsfile", EchoLevel.DEBUG)
    calculateArchVersionWithModules(pomXmlStructure)
}

/** 
 * Step checkmarxScanStep
 */
def checkmarxScanStep() {
    updateVersionRC0(pomXmlStructure, true)
    
    currentBuild.displayName = "Creation_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    pipelineData = new PipelineData(PipelineStructureType.RELEASE_CANDIDATE, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.initFromReleaseCandidate(pathToRepo, "${GlobalVars.RELEASE_BRANCH}/v${pomXmlStructure.getArtifactVersionWithoutQualifier()}", ArtifactSubType.valueOfSubType(artifactSubType), repoName)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam);
    pipelineData.commitId = commitId
    pipelineData.pushUser = user
    
    almEvent = new KpiAlmEvent(
        pomXmlStructure, pipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_RC)

    debugInfo(pipelineParams, pomXmlStructure, pipelineData, params)

    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100")
    try {

        checkmarxScanWorkspace(pomXmlStructure, pipelineData,true,originBranch,GlobalVars.TST_ENVIRONMENT.toUpperCase())
        sendStageEndToGPL(pomXmlStructure, pipelineData, "100")

    } catch(Exception e) {

        printOpen("Error during Checkmarx Scan: ${e.getMessage()}", EchoLevel.ERROR) 
        sendStageEndToGPL(pomXmlStructure, pipelineData, "100", null, null, "error")
        throw e

    }
    
}
	
/** 
 * Step prepareRCStep
 */   	
def prepareRCStep() {
    pipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()
    initGpl = true
    almPipelineStageValidateDependenciesVersion(pomXmlStructure, pipelineData, "110")
}

/** 
 * Step def verifyProState
 */
/*
def verifyProState() {
    try {
        sendStageStartToGPL(pomXmlStructure, pipelineData, "150")

        def exclusionList = GlobalVars.ALM_SERVICES_SKIP_VALIDATION_CLOSE_RELEASE_LIST.split(";")
        boolean excluded = Arrays.asList(exclusionList).contains(pomXmlStructure.artifactName)
        
        if("true".equals(GlobalVars.ALM_SERVICES_SKIP_VALIDATION_CLOSE_RELEASE_ALL) || excluded) {

            printOpen("Element ${pomXmlStructure.artifactName} excluded for verify-PRO-state stage. The skipped component list contain it.", EchoLevel.INFO)

        }else {

            printOpen("Checking deployments state of both centers", EchoLevel.INFO)
            AppDeploymentState center1State = getMultipleDeploymentsStateFromCloud(pomXmlStructure,pipelineData,"AZ1","PRO")
            AppDeploymentState center2State = getMultipleDeploymentsStateFromCloud(pomXmlStructure,pipelineData,"AZ2","PRO")

            printOpen("For the center 1 state ${center1State}, the current is ${center1State.current}", EchoLevel.INFO)
            printOpen("For the center 2 state ${center2State}, the current is ${center2State.current}", EchoLevel.INFO)

            boolean appDeployedButNotConsolidatedInCenter1 = AppDeploymentState.Current.CONSOLIDATED != center1State.current && center1State.isDeployed
            boolean appDeployedButNotConsolidatedInCenter2 = AppDeploymentState.Current.CONSOLIDATED != center2State.current && center2State.isDeployed
            boolean appNotConsolidated = appDeployedButNotConsolidatedInCenter1 || appDeployedButNotConsolidatedInCenter2
            boolean appStatusIsInconsistent = center1State.current != center2State.current

            if (appStatusIsInconsistent) {
                error "App deployment state is inconsistent between centers\n\n" +
                    center1State.toString() + "\n\n" + center2State.toString()
            } else if (appNotConsolidated) {
                error "A Release Candidate build cannot be started if previous version has not gone through the End Release stage\n\n" +
                    center1State.toString() + "\n\n" + center2State.toString()
            }

             printOpen("Checking OK", EchoLevel.INFO)

        }

        sendStageEndToGPL(pomXmlStructure, pipelineData, "150")

    } catch (Exception e) {

        printOpen("Error verifying PRO state: ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "150", null, null, "error")
        throw e
        
    }
}
*/

/** 
 * Step nextMinorMaster
 */
def nextMinorMaster() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "200");
    pushRepoUrl(pomXmlStructure, "${GlobalVars.RELEASE_BRANCH}/v${pomXmlStructure.getArtifactVersionWithoutQualifier()}", false, false, GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_RC)
    changeBranch("${GlobalVars.MASTER_BRANCH}")
    updateNextMinor(pomXmlStructure)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "200")
}

/** 
 * Step pushRepoUrlStage
 */
def pushRepoUrlStage() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "300");
    pushRepoUrl(pomXmlStructure, "${GlobalVars.MASTER_BRANCH}", false, true, GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_RC)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "300")
}

/** 
 * Step endPipelineSuccessStep
 */
def endPipelineSuccessStep(){
    printOpen("SUCCESS", EchoLevel.INFO)

    successPipeline = true 

    if ( almEvent!=null) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis-initCallStartMillis))
    }

    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
}

/** 
 * Step endPipelineFailureStep
 */
def endPipelineFailureStep(){
    printOpen("FAILURE", EchoLevel.ERROR)

    successPipeline = false 

    if ( almEvent!=null) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis-initCallStartMillis))
    }

    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)

}

/** 
 * Step endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}
