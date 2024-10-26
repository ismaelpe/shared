import groovy.transform.Field
import com.project.alm.*
import com.project.alm.GlobalVars
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine

@Field Map pipelineParams
@Field String gitURL
@Field String gitCredentials
@Field String jenkinsPath

// las variables que se obtienen como parametro del job no es necesario
// redefinirlas, se hace por legibilidad del codigo
@Field String originBranch
@Field String pathToRepo
@Field String repoName
@Field String pipelineOrigId
@Field String user
@Field String executionProfileParam
@Field String targetAlmFolderParam
@Field String commandLiquibase
@Field String envCloud

@Field String artifactType
@Field String artifactSubType

@Field String commitId

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean initGpl
@Field boolean successPipeline

@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

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
    pipelineOrigId = params.pipelineOrigId
    user = params.userId
    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam
    commandLiquibase = params.commandLiquibaseParam
    envCloud = params.envParam.toLowerCase()
    artifactType = params.artifactTypeParam
    artifactSubType = params.artifactSubTypeParam
    commitId = params.commitIdParam

    initGpl = false
    successPipeline = false

    almEvent = null
    initCallStartMillis = new Date().getTime()

    pipeline {
        agent {    node(almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
            timeout(time: 1, unit: 'HOURS')
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
        //Atencion que en el caso que estemos en un MergeRequest... quizas solo debamos validar la issue
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
            stage('init-command') {
                steps {
                    initCommandStep()
                }
            }
            stage('report-bbdd-status') {
                steps {
                    reportBbddStatusStep()
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
 * Stage 'getGitRepoStep'
 */
def getGitRepoStep() {
    initGlobalVars(pipelineParams)
    printOpen("Extract GIT Repo ${pathToRepo} ${originBranch}", EchoLevel.INFO)
    pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, true, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false)
    calculateArchVersionWithModules(pomXmlStructure)
}

/**
 * Stage 'initCommandStep'
 */
def initCommandStep() {
    currentBuild.displayName = "${commandLiquibase}_ReportBBDD_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    pipelineData = new PipelineData(PipelineStructureType.BBDD_LIQUIBASE_STATUS, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.initVoidActions(pathToRepo, "${GlobalVars.RELEASE_BRANCH}/v${pomXmlStructure.getArtifactVersionWithoutQualifier()}", ArtifactSubType.valueOfSubType(artifactSubType), repoName, "${envCloud}")
    pipelineData.commitId = commitId
    pipelineData.pushUser = user
    pipelineData.commitLog = commandLiquibase
    env.pipelineBehavior = 'PUSH_NO_MR'
    pipelineData.branchStructure = getBranchInfo(originBranch)
    pipelineData.pipelineStructure.resultPipelineData.artifactType = artifactType
    pipelineData.pipelineStructure.resultPipelineData.artifactSubType = artifactSubType
    pipelineData.pipelineStructure.resultPipelineData.commitId = commitId
    pipelineData.pipelineStructure.resultPipelineData.branchName = originBranch
    pipelineData.pipelineStructure.resultPipelineData.executionProfile = executionProfileParam
    pipelineData.pipelineStructure.resultPipelineData.almSubFolder = targetAlmFolderParam
    initGpl = true
    pipelineData.initDomainProperties(pathToRepo, PipelineData.initFromGitUrlGarApp(pathToRepo, ArtifactSubType.valueOfSubType(artifactSubType)))
    almEvent = new KpiAlmEvent(
        pomXmlStructure, pipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_BBDD_REPORT)
    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, '100')
    sendStageEndToGPL(pomXmlStructure, pipelineData, '100')
}

/**
 * Stage 'reportBbddStatusStep'
 */
def reportBbddStatusStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, '110')
    pipelineData.deployFlag = false
    debugInfo(pipelineParams, pomXmlStructure, pipelineData)
    try {
        String result = ''
        result = reportStatusLiquibaseBBDD(pomXmlStructure, pipelineData, commandLiquibase)
        sendStageEndToGPL(pomXmlStructure, pipelineData, '110', result, null, 'ended')
    }catch (Exception e) {
        sendStageEndToGPL(pomXmlStructure, pipelineData, '110', Strings.toHtml(e.getMessage()), null, 'error')
        throw e
    }
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
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
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis - initCallStartMillis))

        successPipeline = false
        sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
        sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    }
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}

