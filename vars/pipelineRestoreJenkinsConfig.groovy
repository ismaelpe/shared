import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.ExportConfig
import com.project.alm.GitUtils
import com.project.alm.GlobalVars
import io.jenkins.plugins.casc.ConfigurationAsCode

@Field Map pipelineParams

@Field GitRepositoryHandler git
@Field ExportConfig exportConfig
@Field String jcascFileYaml
@Field String jcascPathYaml

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters
    
    jcascFileYaml = "jenkins.yaml"
    jcascPathYaml = "casc_configs"

    pipeline {
        agent { label 'master' }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            CIPHER_PASSWORD = credentials('cloud-alm-cipher-password')
            CIPHER_IV = credentials('cloud-alm-cipher-iv')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
        }
        stages {
            stage('git-pull-repo') {
                steps {
                    gitPullRepoStep()
                }
            }
            stage('jcasc-restore-config') {
                steps {
                   jcascRestoreConfigStep()
                }
            }            
            stage('reload-jenkins-config') {
                steps {
                    reloadJenkinsConfigStep()
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
 * Stage 'gitPullRepoStep'
 */
def gitPullRepoStep() {
    initGlobalVars()

    exportConfig = new ExportConfig(this)

    String enviroment = System.getProperty('jenkins.environment')
    String application = System.getProperty('jenkins.application')

    printOpen("Pull $enviroment config from $env.JENKINS_GIT_CONFIG_BRANCH", EchoLevel.INFO)
    
    // Load Instance Config
    git = new GitRepositoryHandler(this, "https://git.svb.lacaixa.es/cbk/alm/config/jenkins-${enviroment}.git", [gitProjectRelativePath: '.'])
        .initialize()
        .cloneFromGit()
        .checkout(env.JENKINS_GIT_CONFIG_BRANCH)
}

/**
 * Stage 'jcascRestoreConfigStep'
 */
def jcascRestoreConfigStep() {
    printOpen("Retore Config Jenkins config to $exportConfig.jenkinsHome/$jcascPathYaml/$jcascFileYaml")
    exportConfig.restoreValues(jcascPathYaml, jcascFileYaml)
}

/**
 * Stage 'reloadJenkinsConfig'
 */
def reloadJenkinsConfigStep() {
    printOpen("Reload Jenkins config to $exportConfig.jenkinsHome/$jcascPathYaml/$jcascFileYaml")
    exportConfig.reloadConfig(jcascPathYaml, jcascFileYaml)
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen("SUCCESS", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen("FAILURE", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}
