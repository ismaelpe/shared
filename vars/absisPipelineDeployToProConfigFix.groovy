import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

@Field String gitURL
@Field String gitCredentials
@Field String jenkinsPath

@Field String version
@Field String pathToRepo
@Field String repoName
@Field String artifactSubType
@Field String artifactType
@Field String pipelineOrigId
@Field String originBranch
@Field String commitId
@Field String user
@Field String executionProfileParam
@Field String targetAlmFolderParam
@Field String currentDistributionMode
@Field String loggerLevel
@Field String agentParam

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field AppDeploymentState center1State
@Field AppDeploymentState center2State
@Field boolean initGpl
@Field boolean successPipeline
@Field boolean hasAncientICP

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    gitURL = "https://git.svb.lacaixa.es/"
    gitCredentials = "GITLAB_CREDENTIALS"
    jenkinsPath = "alm/services"

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
    version = params.versionParam
    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam
    artifactSubType = params.artifactSubTypeParam
    artifactType = params.artifactTypeParam
    pipelineOrigId = params.pipelineOrigId
    originBranch = params.originBranchParam
    commitId = params.commitIdParam

    user = params.userId

    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam
    currentDistributionMode = nextDistributionMode

    loggerLevel = params.loggerLevel

    agentParam = params.agent

    /**
     * 1. Recoger el artifact
     * 2. Copy config
     * 3. Desplegar a PRO
     * 3.5. Preparar Canario
     */
    boolean initGpl = false
    boolean successPipeline = false
	boolean hasAncientICP = false
    /*
     * Pasos a seguir:
     * */
    
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
            ICP_CERT = credentials('icp-alm-pro-cert')
            ICP_PASS = credentials('icp-alm-pro-cert-passwd')
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
            stage('error-translations') {
                when {
                    expression { !pipelineData.isPushCI() }
                }
                steps {
                    errorTranslationsStep()
                }
            }
            stage('copy-config-files') {
                when {
                    expression { pipelineData.deployFlag && pomXmlStructure.isMicro() }
                }
                steps {
                    copyConfigFilesStep()
                }
            }
			stage('refresh-properties-configuration') {
				steps {
                    refreshPropertiesConfigurationStep()
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
 * Stage getGitRepoStep
 */
def getGitRepoStep() {
    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null
    pomXmlStructure = getGitRepo(pathToRepo, '', repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), version, true)
    calculateArchVersionWithModules(pomXmlStructure)

    pipelineData = new PipelineData(PipelineStructureType.IOP_PRO_CONFIGFIX, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.commitId = commitId
    pipelineData.initFromIOPPro(pathToRepo, originBranch, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), repoName)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam);
    pipelineData.distributionModePRO = currentDistributionMode
    printOpen("DistributionModePRO has been defined as: ${pipelineData.distributionModePRO}", EchoLevel.ALL)

    pipelineData.pushUser = user
    pipelineData.buildCode = pomXmlStructure.artifactVersion

    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100")
    currentBuild.displayName = "Deploying_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    initGpl = true
    debugInfo(null, pomXmlStructure, pipelineData)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")

    //INIT AND DEPLOY
    if (pipelineData.deployFlag) initICPDeploy(pomXmlStructure, pipelineData)
    calculatePreviousInstalledVersionInEnvironment(pipelineData, pomXmlStructure)
}

/**
 * Stage def errorTranslationsStep
 */
def errorTranslationsStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "200")

    printOpen("Upload error translations to errormanagement-micro", EchoLevel.ALL)
    publishErrorManagementTranslations(pipelineData, pomXmlStructure)

    sendStageEndToGPL(pomXmlStructure, pipelineData, "200")
}

/**
 * Stage copyConfigFilesStep
 */
def copyConfigFilesStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "210")
    // INICIO - IPE
    // Comentario: Si se definen las variables de entorno para Git esto no hace falta
    sh "git config http.sslVerify false"
    // FIN - IPE
    pushConfigFiles(pomXmlStructure, pipelineData, false, true)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "210")
}

/**
 * Stage refreshPropertiesConfigurationStep
 */
def refreshPropertiesConfigurationStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "300")
    printOpen("DistributionModePRO is: ${pipelineData.distributionModePRO}", EchoLevel.ALL)
    if (DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1 == pipelineData.distributionModePRO) {
        refreshConfigurationViaRefreshBus(pomXmlStructure,pipelineData, '1')
    } else if (DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2 == pipelineData.distributionModePRO) {
        refreshConfigurationViaRefreshBus(pomXmlStructure,pipelineData, '2')
    } else {
        printOpen("Refreshing center 1...", EchoLevel.ALL)
        refreshConfigurationViaRefreshBus(pomXmlStructure,pipelineData, '1')
        printOpen("Refreshing center 2...", EchoLevel.ALL)
        refreshConfigurationViaRefreshBus(pomXmlStructure,pipelineData, '2')
    }
    sendStageEndToGPL(pomXmlStructure, pipelineData, "300")
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
    printOpen("Pipeline successfully executed.", EchoLevel.ALL)
    pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName, pomXmlStructure.artifactType, pomXmlStructure.artifactSubType)
    printOpen("Next distribution mode is: ${pipelineData.pipelineStructure.resultPipelineData.nextDistributionMode}", EchoLevel.ALL)
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, true)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, true)

    if (pipelineData.getExecutionMode().invokeNextActionAuto()) {
        printOpen("Modo test activado en fase de build", EchoLevel.ALL)
        invokeNextJob(pipelineData, pomXmlStructure)
    } else if (DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1 != pipelineData.distributionModePRO) {
        printOpen("Administrative done to all centers. END RELEASE is being fired automatically...", EchoLevel.ALL)
        pipelineData.prepareExecutionMode('COMPONENT_NEW_VERSION', targetAlmFolderParam);
        invokeNextJob(pipelineData, pomXmlStructure)
    }
}

/**
 * Stage endPipelineFailureStep
 */
def endPipelineFailureStep() {
    printOpen("Pipeline executed with failures.", EchoLevel.ALL)
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, false)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, false)
}