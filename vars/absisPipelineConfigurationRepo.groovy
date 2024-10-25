import groovy.transform.Field
import com.caixabank.absis3.*

@Field Map pipelineParams

@Field String commitId

@Field ConfigurationRepoPipelineData pipelineData
@Field BranchStructure branchStructure
@Field ClientInfo clientInfo
@Field PomXmlStructure pomXmlStructure

@Field boolean successPipeline = false
@Field boolean initGpl = false

//Pipeline que valida la estructura del repositorio de datos canonicos
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    //el commit id
    commitId = env.gitlabMergeRequestLastCommit

    successPipeline = false
    initGpl = false

    pipeline {
        agent { node(absisJenkinsAgent(pipelineParams)) }
        options {
            gitLabConnection('gitlab')
            buildDiscarder(logRotator(numToKeepStr: '10'))
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
        }
        stages {
            stage('get-git-code') {
                when {
                    expression { env.gitlabMergeRequestLastCommit != null }
                }
                steps {
                    getGitCodeStep()
                }
            }
            stage('get-git-info') {
                steps {
                    getGitInfoStep()
                }
            }
            stage('validate-files') {
                when {
                    expression { !pipelineData.isPushConfigFile() }
                }
                steps {
                    validateFilesStep()
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
def getGitCodeStep() {
    printOpen("gitlabMergeRequestLastCommit: ${env.gitlabMergeRequestLastCommit}", EchoLevel.INFO)
    initGlobalVars(pipelineParams)
    new GitUtils(this, false).updateGitCode("${env.gitlabMergeRequestLastCommit}")
    printOpen("pipelineParams: ${pipelineParams.toString()}", EchoLevel.DEBUG)
    printOpen('Jenkinsfile is:', EchoLevel.DEBUG)
    printFile('Jenkinsfile', EchoLevel.DEBUG)
}

/**
 * Stage getGitInfoStep
 */
def getGitInfoStep() {
    clientInfo = new ClientInfo()
    clientInfo.setApplicationName('absis3core')
    clientInfo.setArtifactId('absis3core-conf')
    String date = new Date(System.currentTimeMillis()).format('yyyyMMddHHmmss')
    clientInfo.setArtifactVersion('0.0.' + date)
    clientInfo.setArtifactType(ArtifactType.valueOfType(pipelineParams.type))
    clientInfo.setArtifactSubType(ArtifactSubType.valueOfSubType(pipelineParams.subType))
    clientInfo.setGroupId('com.caixabank.absis')

    pipelineData = getInfoGitForConfigurationRepo(env.BUILD_TAG)
    branchStructure = getBranchInfo()
    pomXmlStructure = new PomXmlStructure()

    pipelineData.initFromConfigurationRepo(branchStructure, ArtifactType.valueOfType(pipelineParams.type), ArtifactSubType.valueOfSubType(pipelineParams.subType), env.gitlabSourceRepoHttpUrl)
    currentBuild.displayName = "Build_${env.BUILD_ID}_" + pipelineData.getPipelineBuildName() != null ? pipelineData.getPipelineBuildName() : 'dev'
    initGpl = true

    sendPipelineStartToGPL(clientInfo, pipelineData, '')
    sendStageStartToGPL(clientInfo, pipelineData, '100')
    sendStageEndToGPL(clientInfo, pipelineData, '100')
    updateCommitStatus(pipelineData, pomXmlStructure, 'running')
    debugInfo(pipelineParams, pomXmlStructure, pipelineData)
}

/**
 * Stage validateFilesStep
 */
def validateFilesStep() {
    try {
        sendStageStartToGPL(clientInfo, pipelineData, '200')
        validateFiles()
        sendStageEndToGPL(clientInfo, pipelineData, '200')
    } catch (Exception e) {
        sendStageEndToGPLAndThrowError(pomXmlStructure, pipelineData, '200', e)
    }
}

/**
 * Stage endPipelineSuccessStep
 */
def endPipelineSuccessStep() {
    successPipeline = true

    printOpen('SUCCESS', EchoLevel.INFO)
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)

    updateCommitStatus(pipelineData, pomXmlStructure, 'success')
}

/**
 * Stage endPipelineFailureStep
 */
def endPipelineFailureStep() {
    printOpen('FAILURE', EchoLevel.ERROR)
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)

    updateCommitStatus(pipelineData, pomXmlStructure, 'failed')
}

/**
 * Stage endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}
