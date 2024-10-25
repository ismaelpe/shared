import groovy.transform.Field
import com.project.alm.*
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams

@Field def gitCredentials = 'GITLAB_CREDENTIALS'
@Field def jenkinsPath = 'alm/services'

@Field def originBranch = "${originBranchParam}"
@Field def pathToRepo = "${pathToRepoParam}"
@Field def repoName = "${repoParam}"
@Field def pipelineOrigId = "${pipelineOrigId}"
@Field def user = "${userId}"
@Field def executionProfileParam = "${executionProfileParam}"
@Field def targetAlmFolderParam = "${targetAlmFolderParam}"

@Field String artifactType = "${artifactTypeParam}"
@Field String artifactSubType = "${artifactSubTypeParam}"

@Field def commitId = "${commitIdParam}"

@Field def loggerLevel = "${loggerLevel}"

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean initGpl = false
@Field boolean successPipeline = false

@Field KpiAlmEvent almEvent = null
@Field long initCallStartMillis = new Date().getTime()

//Pipeline que copia las configs de BBDD para promocionar a PRO
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    def gitCredentials = 'GITLAB_CREDENTIALS'
    def jenkinsPath = 'alm/services'

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
    def originBranch = "${originBranchParam}"
    def pathToRepo = "${pathToRepoParam}"
    def repoName = "${repoParam}"
    def pipelineOrigId = "${pipelineOrigId}"
    def user = "${userId}"
    def executionProfileParam = "${executionProfileParam}"
    def targetAlmFolderParam = "${targetAlmFolderParam}"

    String artifactType = "${artifactTypeParam}"
    String artifactSubType = "${artifactSubTypeParam}"

    def commitId = "${commitIdParam}"

    def loggerLevel = "${loggerLevel}"

    PomXmlStructure pomXmlStructure
    PipelineData pipelineData
    boolean initGpl = false
    boolean successPipeline = false

    KpiAlmEvent almEvent = null
    long initCallStartMillis = new Date().getTime()

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
        agent {    node(absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job

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
    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null
    printOpen("Extract GIT Repo ${pathToRepo} ${originBranch}", EchoLevel.ALL)
    pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, true, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false, true, true)
    calculateArchVersionWithModules(pomXmlStructure)
}

/**
 * Stage 'prepareRCStep'
 */
def prepareRCStep() {
    updateVersionRC0(pomXmlStructure, true)

    currentBuild.displayName = "Creation_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    pipelineData = new PipelineData(PipelineStructureType.RELEASE_CANDIDATE_BBDD, "${env.BUILD_TAG}", params)
    pipelineData.initFromReleaseCandidate(pathToRepo, "${GlobalVars.RELEASE_BRANCH}/BBDDv${pomXmlStructure.getArtifactVersionWithoutQualifier()}", ArtifactSubType.valueOfSubType(artifactSubType), repoName)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam)
    pipelineData.commitId = commitId
    pipelineData.pushUser = user

    pipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()

    almEvent = new KpiAlmEvent(
        pomXmlStructure, pipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_RELEASE_BBDD)

    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, '100')
    initGpl = true

    sendStageEndToGPL(pomXmlStructure, pipelineData, '100')
}

/**
 * Stage 'nextMinorMasterStep'
 */
def nextMinorMasterStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, '200')
    pushRepoUrl(pomXmlStructure, "${GlobalVars.RELEASE_BRANCH}/BBDDv${pomXmlStructure.getArtifactVersionWithoutQualifier()}", true, false, GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_RC)
    changeBranch("${GlobalVars.MASTER_BRANCH}")
    updateNextMinor(pomXmlStructure)
    sendStageEndToGPL(pomXmlStructure, pipelineData, '200')
}

/**
 * Stage 'pushRepoUrlStep'
 */
def pushRepoUrlStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, '300')
    pushRepoUrl(pomXmlStructure, "${GlobalVars.MASTER_BRANCH}", false, true, GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_RC)
    sendStageEndToGPL(pomXmlStructure, pipelineData, '300')
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen("Se success el pipeline ${successPipeline}", EchoLevel.INFO)
    successPipeline = true
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis - initCallStartMillis))
    }

    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen("Se failure el pipeline ${successPipeline}", EchoLevel.ERROR)
    successPipeline = false
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis - initCallStartMillis))
    }
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

