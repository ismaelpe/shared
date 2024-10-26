import groovy.transform.Field
import com.project.alm.*
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams

@Field boolean initAppPortal
@Field boolean successPipeline

@Field CGSPipelineData cgsPipelineData
@Field ClientInfo clientCGSInfo
@Field BranchStructure branchStructure

@Field String operationId
@Field String artifactId
@Field String artifactGroupId
@Field String artifactVersion
@Field String nextEnvironment
@Field String gitUrl
@Field String originBranch
@Field String pipelineOrigId
@Field String user
@Field String loggerLevel

@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    initAppPortal = false
    successPipeline = true

    //Job parameters
    operationId = params.operationId
    artifactId = params.artifactId
    artifactGroupId = params.artifactGroupId
    artifactVersion = params.artifactVersion

    nextEnvironment = params.nextEnvironment
    gitUrl = params.gitUrl

    originBranch = params.originBranchParam
    pipelineOrigId = params.pipelineOrigId
    user = params.user

    loggerLevel = params.loggerLevel

    almEvent = null
    initCallStartMillis = new Date().getTime()

    pipeline {
        agent {    node(almJenkinsAgent(pipelineParams)) }
        //Environment sobre el cual se ejecuta este tipo de job
        options {
            gitLabConnection('gitlab')
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
            MAVEN_OPTS = "-Dhttp.proxyHost=${proxyHost} -Dhttp.proxyPort=${proxyPort} -Dhttps.proxyHost=${proxyHost} -Dhttps.proxyPort=${proxyPort}"
            GIT_SSL_NO_VERIFY = 'true'
        }
        stages {
            stage('init-pipeline') {
                steps {
                    initPipelineStep()
                }
            }
            stage('close-release') {
                steps {
                    closeReleaseStep()
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
 Stage 'initPipelineStep'
 */
def initPipelineStep() {
    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null
    printOpen("BUILD_TAG: ${env.BUILD_TAG}", EchoLevel.ALL)
    cgsPipelineData = new CGSPipelineData(nextEnvironment, "${env.BUILD_TAG}")
    cgsPipelineData.initFromCGSProvisioning(originBranch, gitUrl)
    printOpen('GIT URL: ', EchoLevel.ALL)
    cgsPipelineData.setGitUrl(gitUrl)
    printOpen('GIT URL: ', EchoLevel.ALL)
    branchStructure = getBranchInfo(originBranch)
    currentBuild.displayName = "Build_${env.BUILD_ID}_" + cgsPipelineData.getPipelineBuildName() + '_' + operationId + '_' + nextEnvironment
    cgsPipelineData.pushUser = user

    cgsPipelineData.setBuildCode(artifactVersion)

    clientCGSInfo = new ClientInfo()
    clientCGSInfo.setApplicationName(CGSVars.AppPortal_APPLICATION_NAME)
    clientCGSInfo.setArtifactId(artifactId)
    clientCGSInfo.setArtifactVersion(artifactVersion)
    clientCGSInfo.setArtifactType(ArtifactType.valueOfType(CGSVars.APP_TYPE))
    clientCGSInfo.setArtifactSubType(ArtifactSubType.valueOfSubType(CGSVars.APP_SUBTYPE))
    clientCGSInfo.setGroupId(artifactGroupId)
    almEvent = new KpiAlmEvent(
        clientCGSInfo, cgsPipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_CGS_CLOSE)
    sendPipelineStartToAppPortal(clientCGSInfo, cgsPipelineData, pipelineOrigId)
    initAppPortal = true
}

/**
 Stage 'closeReleaseStep'
 */
def closeReleaseStep() {
    sendStageStartToAppPortal(clientCGSInfo, cgsPipelineData, '200')
    printOpen('---------------------------', EchoLevel.ALL)
    printOpen("Clone git repo $gitUrl", EchoLevel.ALL)

    GitRepositoryHandler git = new GitRepositoryHandler(this, gitUrl)

    try {
        git.lockRepoAndDo({
            git.pullOrClone()

            def branchToMerge = cgsPipelineData.getBranchStructure().branchName

            git.checkout(branchToMerge).pull().checkout('master', [force: true]).pull()

            printOpen("Merge branch $branchToMerge into master", EchoLevel.ALL)
            git.cmdExecutor(["git merge -Xours $branchToMerge"])

            printOpen('Push changes to master', EchoLevel.ALL)
            git.commitAndPush().deleteBranchOnOrigin(branchToMerge)
        })
    } catch (err) {
        echo Utilities.prettyException(err)
        throw err
    } finally {
        git.purge()
        sendStageEndToAppPortal(clientCGSInfo, cgsPipelineData, '200')
    }

    sendStageEndToAppPortal(clientCGSInfo, cgsPipelineData, '200')
}

/**
 Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen('Pipeline has succeeded', EchoLevel.INFO)

    successPipeline = true
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis - initCallStartMillis))
    }

    sendPipelineEndedToAppPortal(initAppPortal, clientCGSInfo, cgsPipelineData, successPipeline)
    sendPipelineResultadoToAppPortal(initAppPortal, clientCGSInfo, cgsPipelineData, successPipeline)
}

/**
 Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false

    printOpen('Pipeline has failed', EchoLevel.ERROR)
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis - initCallStartMillis))
    }

    sendPipelineEndedToAppPortal(initAppPortal, clientCGSInfo, cgsPipelineData, successPipeline)
    sendPipelineResultadoToAppPortal(initAppPortal, clientCGSInfo, cgsPipelineData, successPipeline)
}

/**
 Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(clientCGSInfo, cgsPipelineData)
    cleanWorkspace()
}
