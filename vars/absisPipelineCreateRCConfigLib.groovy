import groovy.transform.Field
import com.caixabank.absis3.*

@Field Map pipelineParams

@Field String gitURL = "https://git.svb.lacaixa.es/"
@Field String gitCredentials = "GITLAB_CREDENTIALS"
@Field String jenkinsPath = "absis3/services"

@Field String originBranch = "${originBranchParam}"
@Field String pathToRepo = "${pathToRepoParam}"
@Field String repoName = "${repoParam}"
@Field String pipelineOrigId = "${pipelineOrigId}"
@Field String user = "${userId}"
@Field String executionProfileParam = "${executionProfileParam}"
@Field String targetAlmFolderParam = "${targetAlmFolderParam}"
@Field String artifactType = "${artifactTypeParam}"
@Field String artifactSubType = "${artifactSubTypeParam}"
@Field String commitId = "${commitIdParam}"
@Field String loggerLevel = "${loggerLevel}"
@Field String agentParam = "${agent}"

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean initGpl = false
@Field boolean successPipeline = false

//Pipeline que copia las configs al repo de TST basado en absisPipelineCreateRC
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
    pipelineOrigId = params.pipelineOrigId
    user = params.userId
    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam
    artifactType = params.artifactTypeParam
    artifactSubType = params.artifactSubTypeParam
    commitId = params.commitIdParam
    loggerLevel = params.loggerLevel
    agentParam = params.agent

    initGpl = false
    successPipeline = false
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
		agent {	node (absisJenkinsAgent(agentParam)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job

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
            stage('prepare-RC') {
                steps {
                    prepareRCStep()
                }
            }
            stage('next-Minor-Master') {
                steps {
                    nextMinorMasterStep()
                }
            }
            stage('push-repo-url') {
                steps {
                    pushRepoUrlStep()
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
    printOpen("Extract GIT Repo ${pathToRepo} ${originBranch}", EchoLevel.ALL)
    pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, true, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false)
    printOpen("Jenkinsfile is:", EchoLevel.INFO)
    sh "cat Jenkinsfile"
    calculateArchVersionWithModules(pomXmlStructure)
}

/** 
 * Step prepareRCStep
 */
def prepareRCStep() {
    updateVersionRC0(pomXmlStructure, true)

    currentBuild.displayName = "Creation_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    pipelineData = new PipelineData(PipelineStructureType.RELEASE_CANDIDATE_CONFIGLIB, "${env.BUILD_TAG}", params)
    pipelineData.initFromReleaseCandidate(pathToRepo, "${GlobalVars.RELEASE_BRANCH}/v${pomXmlStructure.getArtifactVersionWithoutQualifier()}", ArtifactSubType.valueOfSubType(artifactSubType), repoName)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam);
    pipelineData.commitId = commitId
    pipelineData.pushUser = user

    pipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()

    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100");
    initGpl = true

    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")
}

/** 
 * Step nextMinorMasterStep
 */
def nextMinorMasterStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "200");
    pushRepoUrl(pomXmlStructure, "${GlobalVars.RELEASE_BRANCH}/v${pomXmlStructure.getArtifactVersionWithoutQualifier()}", false, false, GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_RC)
    changeBranch("${GlobalVars.MASTER_BRANCH}")
    updateNextMinor(pomXmlStructure)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "200")
}

/** 
 * Step pushRepoUrlStep
 */
def pushRepoUrlStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "300");
    pushRepoUrl(pomXmlStructure, "${GlobalVars.MASTER_BRANCH}", false, true, GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_RC)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "300")
}

/** 
 * Step endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}

/** 
 * Step endPipelineSuccessStep
 */
def endPipelineSuccessStep() {
    printOpen("Se success el pipeline ${successPipeline}", EchoLevel.INFO)
    successPipeline = true
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
}

/** 
 * Step endPipelineFailureStep
 */
def endPipelineFailureStep() {
    printOpen("Se failure el pipeline ${successPipeline}", EchoLevel.ERROR)
    successPipeline = false
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
}