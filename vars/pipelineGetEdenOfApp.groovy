import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.SampleAppCleanMode
import com.project.alm.GlobalVars
import java.util.Map
import java.util.List

@Field Map pipelineParams

@Field boolean successPipeline

@Field String cloudEnv
@Field String namespace
@Field String app
@Field Map valuesDeployed

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo

    successPipeline = true

    cloudEnv = params.environmentParam
    namespace = params.namespaceParam
    app = params.appnameParam
    valuesDeployed = null

    pipeline {
        agent {    node(almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
            timeout(time: 1, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
        environment {
            AppPortal = credentials('IDECUA-JENKINS-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
            https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
        }
        stages {
            stage('get-eden-apps') {
                steps {
                    getEdenAppsStep()
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
 * Stage 'getEdenAppsStep'
 */
def getEdenAppsStep() {
    currentBuild.displayName = "Getting EDEN artifacts of ${app} of ${cloudEnv} and the namespace ${namespace} "
    printOpen('Get App ', EchoLevel.INFO)
    List edenApps = getEdenVersionsOfApp(cloudEnv, app, namespace)
    edenApps.each {
        printOpen("El artefacto es de: ${it}", EchoLevel.INFO)
    }
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("Is pipeline successful? ${successPipeline}", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("Is pipeline unsuccessful? ${successPipeline}", EchoLevel.ERROR)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}
