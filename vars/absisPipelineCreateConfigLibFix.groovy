import groovy.transform.Field
import com.caixabank.absis3.*

@Field Map pipelineParams

@Field def gitURL = "https://git.svb.lacaixa.es/"
@Field def gitCredentials = "GITLAB_CREDENTIALS"
@Field def jenkinsPath = "absis3/services"

@Field String originBranch = "${originBranchParam}"
@Field String pathToRepo = "${pathToRepoParam}"
@Field String repoName = "${repoParam}"
@Field String version = "${versionParam}"
@Field String pipelineOrigId = "${pipelineOrigId}"
@Field String user = "${userId}"
@Field String artifactType = "${artifactTypeParam}"
@Field String artifactSubType = "${artifactSubTypeParam}"
@Field String commitId = "${commitIdParam}"
@Field String executionProfileParam = "${executionProfileParam}"
@Field String targetAlmFolderParam = "${targetAlmFolderParam}"
@Field String loggerLevel = "${loggerLevel}"
@Field String agentParam = "${agent}"

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean initGpl = false
@Field boolean successPipeline = false

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters
    
    gitURL = "https://git.svb.lacaixa.es/"
    gitCredentials = "GITLAB_CREDENTIALS"
    jenkinsPath = "absis3/services"

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
    executionProfileParam = executionProfileParam
    targetAlmFolderParam = targetAlmFolderParam
    loggerLevel = params.loggerLevel
    agentParam = params.agent

    initGpl = false
    successPipeline = false
    
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
        //Atencion que en el caso que estemos en un MergeRequest... quizas solo debamos validar la issue
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
            stage('prepare-HotFix') {
                steps {
                    prepareHotFixStep()
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
def getGitRepoStep(){
    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null
    printOpen("Extract GIT Repo ${pathToRepo} ${originBranch}", EchoLevel.ALL)
    /**
        * Se recoge el proyecto de GIT para la version (tag) concreta
        */
    pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, true, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), version, true)
}

/**
 * Stage prepareHotFixStep
 */
def prepareHotFixStep() {
    /**
     * Se realiza un upgrade la version a una RC del FIX
     */
    calculateArchVersionWithModules(pomXmlStructure)
    updateVersionFix(pomXmlStructure)
    currentBuild.displayName = "Creation_ConfigLibFix_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    pipelineData = new PipelineData(PipelineStructureType.CONFIGLIBFIX, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.initFromFix(pathToRepo, "${GlobalVars.HOTFIX_BRANCH}/v${pomXmlStructure.getArtifactVersionWithoutQualifier()}", ArtifactSubType.valueOfSubType(artifactSubType), repoName)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam);
    pipelineData.commitId = commitId
    pipelineData.pushUser = user
    pipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()

    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_STARTED, KpiLifeCycleStatus.OK)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.HOTFIX_CREATION_STARTED, KpiLifeCycleStatus.OK)
    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100");
    initGpl = true
    /**
        * Se consolida el codigo de la nueva rama generada del hotfix en el repo de git
        */
    pushRepoUrl(pomXmlStructure, "${GlobalVars.HOTFIX_BRANCH}/v${pomXmlStructure.getArtifactVersionWithoutQualifier()}", true, false, GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_HOTFIX)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")
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
    printOpen("Se success el pipeline ${successPipeline}", EchoLevel.INFO)
    successPipeline = true
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.HOTFIX_CREATION_FINISHED, KpiLifeCycleStatus.OK)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.OK)
}

/**
 * Stage endPipelineFailureStep
 */
def endPipelineFailureStep() {
    printOpen("Se failure el pipeline ${successPipeline}", EchoLevel.ERROR)
    successPipeline = false
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.HOTFIX_CREATION_FINISHED, KpiLifeCycleStatus.KO)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.KO)
}

