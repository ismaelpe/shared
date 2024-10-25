import groovy.transform.Field
import com.caixabank.absis3.*

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
@Field String user
@Field String loggerLevel
@Field String agentParam

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean initGpl

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    nexus = new NexusUtils(this)

    gitURL = "https://git.svb.lacaixa.es/"
    gitCredentials = "GITLAB_CREDENTIALS"
    jenkinsPath = "absis3/services"

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

    user = params.userId
    loggerLevel = params.loggerLevel
    agentParam = params.agent

    initGpl = false

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
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
            stage('prepare-Release') {
                steps {
                    prepareReleaseStep()
                }
            }
			stage('validate-version') {
                when {
                    expression { pomXmlStructure.artifactSubType == ArtifactSubType.MICRO_APP }
                }
                steps {
                    validateVersionStep()
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
                    expression { pipelineData.deployFlag && pomXmlStructure.isMicro() && pipelineData.bmxStructure.usesConfigServer() }
                }
                steps {
                    copyConfigFilesStep()
                }
            }
            stage('refresh-properties-configuration') {
                when {
                    expression { pipelineData.deployFlag }
                }
                steps {
                    refreshPropertiesConfigurationStep()
                }
            }
            stage('push-Release-to-git') {
            	steps {
                    pushReleaseToGitStep()
            	}
            }
			stage('publish-artifact-catalog') {
				when {
					expression { GlobalVars.GSA_ENABLED  }
				}
				steps {
                    publishArtifactCatalogStep()
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

    printOpen("Extract GIT Repo ${pathToRepo} ${originBranch}", EchoLevel.ALL)
    pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false)
    pipelineData = new PipelineData(PipelineStructureType.CONFIG_RELEASE, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.commitId = commitId
    pipelineData.initFromRelease(pathToRepo, originBranch, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), repoName, false, true)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam);
    pipelineData.pushUser = user

    pipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()

    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100")
    initGpl = true

    //Buscamos version de arquitectura
    //Validamos que sea superior a la minim
    calculateArchVersionWithModules(pomXmlStructure)

    debugInfo(pipelineParams, pomXmlStructure, pipelineData)

    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")
}

/**
 * Stage prepareReleaseStep
 */
def prepareReleaseStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "200")
    validateBranch(pomXmlStructure.getArtifactVersionWithoutQualifier(), pipelineData.branchStructure)
    updateVersionForRelease(pomXmlStructure)
    pipelineData.buildCode = pomXmlStructure.artifactVersion
    currentBuild.displayName = "Creation_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    sendStageEndToGPL(pomXmlStructure, pipelineData, "200")
}

/**
 * Stage validateVersionStep
 */
def validateVersionStep() {
    absisPipelineStageValidateVersion(pomXmlStructure, pipelineData, "300")
}

/**
 * Stage errorTranslationsStep
 */
def errorTranslationsStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "400")
    
    printOpen("Upload error translations to errormanagement-micro", EchoLevel.ALL)
    publishErrorManagementTranslations(pipelineData, pomXmlStructure)

    sendStageEndToGPL(pomXmlStructure, pipelineData, "400")
}

/**
 * Stage copyConfigFilesStep
 */
def copyConfigFilesStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "500")
    sh "git config http.sslVerify false"
    
    pushConfigFiles(pomXmlStructure, pipelineData, false, true)
    
    sendStageEndToGPL(pomXmlStructure, pipelineData, "500")
}

/**
 * Stage refreshPropertiesConfigurationStep
 */
def refreshPropertiesConfigurationStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "600")

    refreshConfigurationViaRefreshBus(pomXmlStructure,pipelineData, '1')
    refreshConfigurationViaRefreshBus(pomXmlStructure,pipelineData, '2')

    sendStageEndToGPL(pomXmlStructure, pipelineData, "600")
}

/**
 * Stage pushReleaseToGitStep
 */
def pushReleaseToGitStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "700")
    pushRepoUrl(pomXmlStructure, "${originBranch}", false, true, GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_RELEASE)
    tagVersion(pomXmlStructure, pipelineData, true, false)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "700")
}

/**
 * Stage publishArtifactCatalog
 */
def publishArtifactCatalogStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "800")
    printOpen("publishing artifact in catalog", EchoLevel.ALL)
    publishArtifactInCatalog(pipelineData, pomXmlStructure)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "800")
}

/**
 * Stage endPipelineSuccessStep
 */
def endPipelineSuccessStep() {
    printOpen("Pipeline succeed", EchoLevel.ALL)
    printOpen("Preparing result data...", EchoLevel.ALL)
    pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName)
    pipelineData.setBuildCode(pomXmlStructure.artifactVersion)
    printOpen("ResultData ready.", EchoLevel.ALL)
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, true)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, true)

    if (pipelineData.getExecutionMode().invokeNextActionAuto() && !pipelineData.isPushCI()) {
        printOpen("Modo test activado en fase de crear release de configuracion", EchoLevel.ALL)
        invokeNextJob(pipelineData, pomXmlStructure)
    }
}

/**
 * Stage endPipelineFailureStep
 */
def endPipelineFailureStep() {                
    printOpen("Pipeline has failed", EchoLevel.ERROR)
    
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, false)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, false)
}

/**
 * Stage endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}
