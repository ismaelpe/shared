import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GitUtils
import com.project.alm.GlobalVars
import com.project.alm.PomXmlStructure
import com.project.alm.Utilities

@Field Map pipelineParams

@Field String gitProjectUrl
@Field String gitProjectName
@Field String gitBranch
@Field String commitMessage

@Field PomXmlStructure pomXmlStructure

/**
 * Pipeline SCM Script, que sirve para reintentar jobs de CI.
 * Basicamente lo que hace es descargarse el repositorio, hacer commit empty y pushearlo
 * @param pipelineParams
 * @return void
 */
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    gitProjectUrl = params.pathToRepoParam ? params.pathToRepoParam : ""
    gitProjectName = Utilities.getNameOfProjectFromUrl(gitProjectUrl)
    gitBranch = params.originBranchParam

    commitMessage = "retry CI pipeline"
    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
            https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
        }
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }

            stage('push-repo') {
                steps {
                    pushRepoStep()
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
    def gitRepoUrl = null
    printOpen("Descargamos projecto git ${gitProjectName}", EchoLevel.ALL)
    pomXmlStructure = getPomFromGitRepo(gitProjectUrl, gitBranch, gitProjectName)
    printOpen("Descargado repositorio git correctamente", EchoLevel.ALL)
}

/** 
 * Step pushRepoStep
 */
def pushRepoStep() {
    withCredentials([
        usernamePassword(credentialsId: GlobalVars.GIT_CREDENTIAL_PROFILE_ID, passwordVariable: GlobalVars.GIT_CREDENTIAL_PASSWORD_VAR, usernameVariable: GlobalVars.GIT_CREDENTIAL_USER_VAR)
    ]) {
        GitUtils git = new GitUtils(this, false)
        git.removeFromWorkspaceFilesNotAllowedInGitRepos("${gitProjectName}")
        sh "cd ${gitProjectName} && git commit -m '${commitMessage}' --allow-empty"
        sh "cd ${gitProjectName} && git -c http.sslVerify=false push origin ${gitBranch}"
    }
}

/** 
 * Step endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}

/** 
 * Step endPipelineSuccessStep
 */
def endPipelineSuccessStep() {
    printOpen("Ejecución satisfactoria del pipeline de retry", EchoLevel.INFO)
}

/** 
 * Step endPipelineFailureStep
 */
def endPipelineFailureStep() {
    printOpen("Pipeline has failed", EchoLevel.ERROR)
}
