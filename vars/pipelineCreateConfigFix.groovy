import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

@Field String gitURL
@Field String gitCredentials
@Field String jenkinsPath

// las variables que se obtienen como parametro del job no es necesario
// redefinirlas, se hace por legibilidad del codigo

@Field String originBranch
@Field String pathToRepo
@Field String repoName
@Field String version
@Field String pipelineOrigId
@Field String user

@Field String artifactType
@Field String artifactSubType

@Field String commitId

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean initAppPortal
@Field boolean successPipeline

@Field String executionProfileParam
@Field String targetAlmFolderParam

@Field String loggerLevel

@Field String agentParam

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    gitURL = 'https://git.svb.digitalscale.es/'
    gitCredentials = 'GITLAB_CREDENTIALS'
    jenkinsPath = 'alm/services'

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo

    originBranch = params.originBranchParam
    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam
    version = params.versionParam
    pipelineOrigId = params.pipelineOrigId
    user = params.userId

    artifactType = params.artifactTypeParam
    artifactSubType = params.artifactSubTypeParam

    commitId = params.commitIdParam

    initAppPortal = false
    successPipeline = false

    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam

    loggerLevel = params.loggerLevel

    agentParam = params.agent

    pipeline {
        agent {    node(almJenkinsAgent(agentParam)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
        }
        environment {
            AppPortal = credentials('IDECUA-JENKINS-USER-TOKEN')
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
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
            stage('prepare-ConfigFix') {
                steps {
                    prepareConfigFixStep()
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
    printOpen("Extract GIT Repo ${pathToRepo} ${originBranch}", EchoLevel.INFO)
    /**
    * Se recoge el proyecto de GIT para la version (tag) concreta
    */
    pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, true, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), version, true)
}

/**
 * Stage prepareConfigFixStep
 */
def prepareConfigFixStep() {
   /**
    * Se realiza un upgrade la version a una RC del FIX
    */
    calculateArchVersionWithModules(pomXmlStructure)
    updateVersionFix(pomXmlStructure)
    currentBuild.displayName = "Creation_ConfigFix_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    pipelineData = new PipelineData(PipelineStructureType.CONFIGFIX, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.initFromConfigFix(pathToRepo, "${GlobalVars.CONFIGFIX_BRANCH}/v${pomXmlStructure.getArtifactVersionWithoutQualifier()}", ArtifactSubType.valueOfSubType(artifactSubType), repoName)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam)
    pipelineData.commitId = commitId
    pipelineData.pushUser = user

    pipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()

    sendPipelineStartToAppPortal(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, '100')
    initAppPortal = true
    /**
     * Se consolida el codigo de la nueva rama generada del fix de configuracion en el repo de git
     */
    pushRepoUrl(pomXmlStructure, "${GlobalVars.CONFIGFIX_BRANCH}/v${pomXmlStructure.getArtifactVersionWithoutQualifier()}", true, false, GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_CONFIGFIX)
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, '100')
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
    printOpen('SUCCESS', EchoLevel.INFO)
    sendPipelineResultadoToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)
}

/**
 * Stage endPipelineFailureStep
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen('FAILURE', EchoLevel.ERROR)
    sendPipelineResultadoToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)
}
