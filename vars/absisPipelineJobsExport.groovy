import groovy.transform.Field
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.ExportJobs
import com.caixabank.absis3.GitUtils
import com.caixabank.absis3.GlobalVars

@Field Map pipelineParams

@Field String gitRepoUrl

@Field ExportJobs exportJobs
@Field GitRepositoryHandler git

@Field Map jobsList

@Field boolean sanitizeExport
@Field String jobBranch

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    sanitizeExport = params.sanitizeExport
    jobBranch = params.jobBranch ? params.jobBranch : env.JENKINS_GIT_JOBS_BRANCH

    jobsList = [:]

    pipeline {
        agent { label 'master' }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            ICP_CERT = credentials('icp-absis3-pro-cert')
            ICP_PASS = credentials('icp-absis3-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
        }
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
            stage('get-jobs') {
                steps {
                    getJobsStep()
                }
            }
            stage('export-jobs') {
                steps {
                    exportJobsStep()
                }
            }
            stage('push-git') {
                steps {
                    pushGitStep()
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
 Stage 'getGitReporStep'
 */
def getGitRepoStep() {
    exportJobs = new ExportJobs(this, sanitizeExport)

    String enviroment = System.getProperty('jenkins.environment')
   
    gitRepoUrl = "https://git.svb.lacaixa.es/cbk/absis3/config/jenkins-${enviroment}.git"

    printOpen("Pull from '$enviroment' and '$jobBranch' branch, gitUrl is $gitRepoUrl", EchoLevel.INFO)

    git = new GitRepositoryHandler(this, gitRepoUrl, [checkoutBranch: jobBranch, gitProjectRelativePath: '.'])
    git.pullOrClone()
}

/**
 Stage 'getJobsSteps'
 */
def getJobsStep() {
    jobsList = exportJobs.listJobs()
}
/**
 * Stage 'exportJobsStep'
 */
def exportJobsStep() {
    exportJobs.export(jobsList)
}

/**
 * Stage 'pushGitStep'
 */
def pushGitStep() {
    withCredentials([usernamePassword(credentialsId: GlobalVars.GIT_CREDENTIAL_PROFILE_ID, passwordVariable: GlobalVars.GIT_CREDENTIAL_PASSWORD_VAR, usernameVariable: GlobalVars.GIT_CREDENTIAL_USER_VAR)]) {
        try {
            String application = System.getProperty('jenkins.application')
            String enviroment = System.getProperty('jenkins.environment')
            
            printOpen("Push to '$enviroment' git '$jobBranch' branch, gitUrl is $gitRepoUrl", EchoLevel.INFO)

            GitUtils gitUtils = new GitUtils(this, false)
            gitUtils.removeFromWorkspaceFilesNotAllowedInGitRepos()

            git.add()
            git.commit("Extracted Jobs from Jenkins $application - $enviroment", [allowEmpty: true])
            sh("git push --set-upstream origin $jobBranch")
        } catch (Exception e) {
            String message = "Fail on push $enviroment git $jobBranch branch, gitUrl is $gitRepoUrl and error was: ${e.getMessage()}"
            printOpen(message, EchoLevel.ERROR)
            throw new Exception(message)
        }
    }
}
/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen('SUCCESS', EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen('FAILURE', EchoLevel.INFO)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}
