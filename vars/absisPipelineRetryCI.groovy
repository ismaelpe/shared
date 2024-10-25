import groovy.transform.Field
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GitUtils
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.Utilities

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
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
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