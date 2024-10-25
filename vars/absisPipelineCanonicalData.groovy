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
@Field String user
@Field String commitId
@Field boolean successPipeline

//Pipeline que valida la estructura del repositorio de datos canonicos
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    gitURL = 'https://git.svb.lacaixa.es/'
    gitCredentials = 'GITLAB_CREDENTIALS'
    jenkinsPath = 'alm/services'
    originBranch = params.gitlabBranch
    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam
    user = params.userId
    commitId = params.gitlabMergeRequestLastCommit
    successPipeline = false

    pipeline {
        agent {    node(absisJenkinsAgent(pipelineParams)) }
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
        }
        stages {
            stage('get-git-repo') {
                when {
                    expression { env.gitlabMergeRequestLastCommit != null }
                }
                steps {
                    getGitRepoStep()
                }
            }

            stage('validate-files') {
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
 * Stage 'getGitRepoStep'
 */
def getGitRepoStep() {
    updateCommitStatus('running')
    printOpen("Extract GIT Repo $env.gitlabSourceRepoHttpUrl $env.gitlabSourceBranch $env.gitlabMergeRequestLastCommit", EchoLevel.INFO)
    getGitRepo(env.gitlabSourceRepoHttpUrl, env.gitlabSourceBranch, env.gitlabMergeRequestLastCommit, false)
}

/**
 * Stage 'validateFilesStep'
 */
def validateFilesStep() {
    printOpen("Extract GIT Repo $env.gitlabSourceRepoHttpUrl $env.gitlabSourceBranch $env.gitlabMergeRequestLastCommit", EchoLevel.INFO)
    validateFiles()
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("Se success el pipeline $successPipeline", EchoLevel.INFO)
    updateCommitStatus('success')
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("Se failure el pipeline ${successPipeline}", EchoLevel.ERROR)
    updateCommitStatus('failed')
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}
