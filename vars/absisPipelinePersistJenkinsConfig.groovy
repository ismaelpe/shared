import groovy.transform.Field
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.ExportConfig
import com.caixabank.absis3.GitUtils
import com.caixabank.absis3.GlobalVars
import io.jenkins.plugins.casc.ConfigurationAsCode

@Field Map pipelineParams

@Field ExportConfig exportConfig
@Field GitRepositoryHandler gitConfig
@Field GitRepositoryHandler gitIcp
@Field String jcascPathYaml
@Field String jcascPathValuesYaml
@Field String jobParam

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    jcascPathYaml = "casc_configs"
    jcascPathValuesYaml = "casc_configs_values"
    
    pipeline {
        agent { label 'master' }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            CIPHER_PASSWORD = credentials('icp-absis3-cipher-password')
            CIPHER_IV = credentials('icp-absis3-cipher-iv')
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
            stage('jcasc-flush-config') {
                steps {
                    jcascFlushConfigStep()
                }
            }
            stage('jcasc-update-helm-values') {
                steps {
                    jcascUpdateHelmValuesStep()
                }
            }
            stage('jcasc-update-git-config') {
                steps {
                    jcascUpdateGitConfigStep()
                }
            }
            stage('git-push-repo') {
                steps {
                    gitPushRepoStep()
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
    String component = System.getProperty('jenkins.component')

    printOpen("Pull $enviroment 'values.yaml' from $application", EchoLevel.INFO)

    gitIcp = new GitRepositoryHandler(this, "https://git.svb.lacaixa.es/cbk/k8s/$application/$component", [gitProjectRelativePath: jcascPathValuesYaml])
        .initialize()
        .cloneFromGit()
        .checkout("develop")

    printOpen("Pull $enviroment config from $env.JENKINS_GIT_CONFIG_BRANCH", EchoLevel.INFO)

    gitConfig = new GitRepositoryHandler(this, "https://git.svb.lacaixa.es/cbk/absis3/config/jenkins-${enviroment}.git", [gitProjectRelativePath: jcascPathYaml])
        .initialize()
        .cloneFromGit()
        .checkout(env.JENKINS_GIT_CONFIG_BRANCH)
}

/**
 * Stage 'jcascFlushConfigStep'
 */
def jcascFlushConfigStep() {
    printOpen("Generating and exporting Jenkins JCasc config")
    exportConfig.flushJCascConfigToDisk()
}

/**
 * Stage 'jcascUpdateHelmValuesStep'
 */
def jcascUpdateHelmValuesStep() {
    printOpen("Update Helm values.yaml")
    def environment = System.getProperty('jenkins.environment')
    exportConfig.updateValuesYamlFromJCasc("$jcascPathValuesYaml/k8s/$environment/values.yaml")
}

/**
 * Stage 'jcascUpdateGitConfigStep'
 */
def jcascUpdateGitConfigStep() {
    printOpen("Try to copy Exported JCASC and rencrypt in WorkSpace $jcascPathYaml", EchoLevel.INFO)
    exportConfig.copyAndRencryptCredentialsFromJCasc("$jcascPathYaml/jenkins.yaml")
    
    printOpen("Try to standarize $jcascPathYaml/jenkins.yaml", EchoLevel.INFO)
    exportConfig.templarizeJCascFile("$jcascPathYaml/jenkins.yaml")
}

/**
 * Stage 'gitPushRepoStep'
 */
def gitPushRepoStep() {
    def jenkinsInfo = System.getProperty('jenkins.application') + " of " + System.getProperty('jenkins.environment')
    gitIcp.add().commitAndPush("Update JCasc Config", [allowEmpty: true])
    gitConfig.add().commitAndPush("Update config from Jenkins $jenkinsInfo", [allowEmpty: true])
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
