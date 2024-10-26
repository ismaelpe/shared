import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    pipeline {
        agent {	node (almJenkinsAgent("light")) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '20'))
            disableConcurrentBuilds()
            timeout(time: 15, unit: 'MINUTES')
            timestamps()
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
        }
        stages {
            stage('service-catalog-publication') {
                steps {
                    serviceCatalogPublicationStep()
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
        }
    }
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/**
 * Stage 'serviceCatalogPublicationStep'
 */
 def serviceCatalogPublicationStep() {
    sleep(15)
    ServicesCatalogQueueHandler servicesCatalogQueueHandler = ServicesCatalogQueueHandler.init(this)
    servicesCatalogQueueHandler.publishSyncAndAsyncQueues()
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen("La pipeline se ejecuto sin problemas", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen("La pipeline falla", EchoLevel.ERROR)
}

