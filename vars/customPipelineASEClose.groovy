import groovy.transform.Field
import com.caixabank.absis3.*
import com.caixabank.absis3.KpiAlmEvent
import com.caixabank.absis3.KpiAlmEventStage
import com.caixabank.absis3.KpiAlmEventOperation

@Field Map pipelineParams

@Field GitUtils gitUtils

@Field boolean initGpl
@Field boolean successPipeline

@Field ASEPipelineData asePipelineData
@Field ClientInfo clientInfo
@Field BranchStructure branchStructure

//Job parameters
@Field String yamlFilePath
@Field String contractGitCommit
@Field String artifactId
@Field String artifactGroupId
@Field String artifactVersion

@Field String nextEnvironment
@Field String gitUrl

@Field String originBranch
@Field String pipelineOrigId
@Field String userEmail
@Field String user
@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    gitUtils = new GitUtils(this, false)

    initGpl = false
    successPipeline = true

    //Job parameters
    yamlFilePath = params.yamlFilePath
    contractGitCommit = params.contractGitCommit
    artifactId = params.artifactId
    artifactGroupId = params.artifactGroupId
    artifactVersion = params.artifactVersion

    nextEnvironment = params.nextEnvironment
    gitUrl = params.gitUrl

    originBranch = params.originBranchParam
    pipelineOrigId = params.pipelineOrigId
    userEmail = params.userEmail
    user = params.user

    almEvent = null
    initCallStartMillis = new Date().getTime()

    pipeline {
        agent { node(absisJenkinsAgent(pipelineParams)) }
        //Environment sobre el cual se ejecuta este tipo de job
        options {
            gitLabConnection('gitlab')
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
def initPipelineStep() {
    initGlobalVars(pipelineParams)
    printOpen("BUILD_TAG: ${env.BUILD_TAG}", EchoLevel.ALL)
    asePipelineData = new ASEPipelineData(nextEnvironment, "${env.BUILD_TAG}")
    asePipelineData.initFromASEProvisioning(originBranch, gitUrl)
    printOpen('GIT URL: ', EchoLevel.ALL)
    asePipelineData.setGitUrl(gitUrl)
    printOpen('GIT URL: ', EchoLevel.ALL)
    branchStructure = getBranchInfo(originBranch)
    def contractId = yamlFilePath.replace('contracts/', '').replace('.yaml', '')
    currentBuild.displayName = "Build_${env.BUILD_ID}_" + asePipelineData.getPipelineBuildName() + '_' + contractId + '_' + nextEnvironment
    asePipelineData.pushUser = user

    def dummyArtifactList = ["${artifactId}-${artifactVersion}.pom"]
    asePipelineData.setBuildCode(NexusUtils.getBuildId(dummyArtifactList, artifactVersion))

    clientInfo = new ClientInfo()
    clientInfo.setApplicationName(ASEVars.GAR_APPLICATION_NAME)
    clientInfo.setArtifactId(artifactId)
    clientInfo.setArtifactVersion(artifactVersion)
    clientInfo.setArtifactType(ArtifactType.valueOfType(ASEVars.APP_TYPE))
    clientInfo.setArtifactSubType(ArtifactSubType.valueOfSubType(ASEVars.APP_SUBTYPE))
    clientInfo.setGroupId(artifactGroupId)
    almEvent = new KpiAlmEvent(
        clientInfo, asePipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_ASE_CLOSE)
    sendPipelineStartToGPL(clientInfo, asePipelineData, pipelineOrigId)
    initGpl = true
    sendStageStartToGPL(clientInfo, asePipelineData, '010')
    sendStageEndToGPL(clientInfo, asePipelineData, '010')
}

/**
 * Stage 'getGitRepoStep'
 */
def closeReleaseStep() {
    sendStageStartToGPL(clientInfo, asePipelineData, '020')
    printOpen('---------------------------', EchoLevel.ALL)
    printOpen("Clone git repo $gitUrl", EchoLevel.ALL)

    GitRepositoryHandler git = new GitRepositoryHandler(this, gitUrl)

    try {
        git.lockRepoAndDo({
            git.pullOrClone()
            boolean contractCommitIsOnBranchHEAD = gitUtils.getLastCommitId(originBranch, git.getGitProjectRelativePath()) == contractGitCommit
            def checkoutId = contractCommitIsOnBranchHEAD ? originBranch : contractGitCommit
            def msg = contractCommitIsOnBranchHEAD ? "Merge branch '${originBranch}'" : "Merge commit '${contractGitCommit}' (${originBranch}). contractGitCommit and branch HEAD did not match"

            git.checkout(originBranch, [force: true]).pull()

            if (contractCommitIsOnBranchHEAD) {
                printOpen("Checkout contractGitCommit (contractCommitIsOnBranchHEAD=${contractCommitIsOnBranchHEAD}): ${checkoutId}", EchoLevel.ALL)
                git.checkout(checkoutId, [force: true])
            }

            git.checkout('master', [force: true]).pull()
            git.cmdExecutor(["git merge ${checkoutId} --squash -Xtheirs"])
            git.commitAndPush(msg)

            if (contractCommitIsOnBranchHEAD) {
                printOpen('Delete ase service branch remote', EchoLevel.ALL)
                git.deleteBranchOnOrigin(originBranch)
            } else if (originBranch == 'master') {
                printOpen('WARNING: We have received master as originBranch. We are definitely not deleting this', EchoLevel.ALL)
            } else {
                printOpen('WARNING: contractGitCommit and branch HEAD did not match\n', EchoLevel.ALL)
                'Some changes in the branch were not merged as they were included after RC was created\n' +
                    'Those changes may be included in the next release\n' +
                    'Thus, the branch is not being deleted'
            }
        })
    } catch (err) {
        echo Utilities.prettyException(err)
        throw err
    } finally {
        git.purge()
    }

    sendStageEndToGPL(clientInfo, asePipelineData, '020')
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen('Pipeline has succeeded', EchoLevel.INFO)
    successPipeline = true
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis - initCallStartMillis))
    }
    sendPipelineEndedToGPL(initGpl, clientInfo, asePipelineData, successPipeline)
    sendPipelineResultadoToGPL(initGpl, clientInfo, asePipelineData, successPipeline)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen('Pipeline has failed', EchoLevel.ERROR)
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis - initCallStartMillis))
    }
    sendPipelineEndedToGPL(initGpl, clientInfo, asePipelineData, successPipeline)
    sendPipelineResultadoToGPL(initGpl, clientInfo, asePipelineData, successPipeline)
}

/**
 * Stage 'endiPipelineAlwaysStep'
 */
def endiPipelineAlwaysStep() {
    attachPipelineLogsToBuild(clientInfo, asePipelineData)
    cleanWorkspace()
}

