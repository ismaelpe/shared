import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.CloudApiResponse
import com.project.alm.CloudPodsStatus

@Field Map pipelineParams

@Field String components
@Field String blueOrGreen
@Field String namespaceId
@Field String centerId
@Field String env_param

@Field CloudPodsStatus preRestartPodsStatus

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo

    components = params.componentNames.toLowerCase().split(",")
    blueOrGreen = params.color
    namespaceId  = params.namespace
    centerId = params.center
    env_param = params.environment

    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
            timeout(time: 30, unit: 'MINUTES')
        }
        //Environment sobre el qual se ejecuta este tipo de job
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
            stage("acquire-pod-info") {
                steps {
                  acquirePodInfoStep()
                }
            }
            stage("restart-blue-pods-center-one") {
                when {
                    expression { (centerId == 'AZ1' || centerId == 'ALL') && (blueOrGreen == 'BLUE' || blueOrGreen == 'BOTH') }
                }
                steps {
                    restartBluePodsCenterOneStep()
                }
            }
            stage("restart-green-pods-center-one") {
                when {
                    expression { (centerId == 'AZ1' || centerId == 'ALL') && (blueOrGreen == 'GREEN' || blueOrGreen == 'BOTH') }
                }
                steps {
                    restartGreenPodsCenterOneStep()
                }
            }
            stage("restart-blue-pods-center-two") {
                when {
                    expression { (centerId == 'AZ2' || centerId == 'ALL') && (blueOrGreen == 'BLUE' || blueOrGreen == 'BOTH') }
                }
                steps {
                    restartBluePodsCenterTwoStep()
                }
            }
            stage("restart-green-pods-center-two") {
                when {
                    expression { (centerId == 'AZ2' || centerId == 'ALL') && (blueOrGreen == 'GREEN' || blueOrGreen == 'BOTH') }
                }
                steps {
                    restartGreenPodsCenterTwoStep()
                }
            }
            stage("acquire-pod-info-after-restart") {
                steps {
                    acquirePodInfoAfterRestartStep()
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
 * Stage 'acquirePodInfoStep'
 */
def acquirePodInfoStep() {
    def response = sendRequestToCloud.getPodsInfo(namespaceId, env_param, centerId)
    preRestartPodsStatus = new CloudPodsStatus(response.body, components, env_param)
    printOpen("preRestartPodsStatus: ${preRestartPodsStatus.podsStatus}", EchoLevel.ALL)
}

/**
 * Stage 'restartBluePodsCenterOneStep'
 */
def restartBluePodsCenterOneStep() {
    printOpen("Restarting pods on AZ1 with color blue", EchoLevel.ALL)
    runPodRestart(preRestartPodsStatus, namespaceId, env_param, 'AZ1', 'blue')
}

/**
 * Stage 'restartGreenPodsCenterOneStep'
 */
def restartGreenPodsCenterOneStep() {
    printOpen("Restarting pods on AZ1 with color green", EchoLevel.ALL)
    runPodRestart(preRestartPodsStatus, namespaceId, env_param, 'AZ1', 'green')
}

/**
 * Stage 'restartBluePodsCenterTwoStep'
 */
def restartBluePodsCenterTwoStep() {    
    printOpen("Restarting pods on AZ2 with color blue", EchoLevel.ALL)
    runPodRestart(preRestartPodsStatus, namespaceId, env_param, 'AZ2', 'blue')
}

/**
 * Stage 'restartGreenPodsCenterTwoStep'
 */
def restartGreenPodsCenterTwoStep() {
    printOpen("Restarting pods on AZ2 with color green", EchoLevel.ALL)
    runPodRestart(preRestartPodsStatus, namespaceId, env_param, 'AZ2', 'green')
}

/**
 * Stage 'acquirePodInfoAfterRestartStep'
 */
def acquirePodInfoAfterRestartStep() {
    def response = sendRequestToCloud.getPodsInfo(namespaceId, env_param, centerId)
    postRestartPodsStatus = new CloudPodsStatus(response.body, components, env_param)
    printOpen("postRestartPodsStatus: ${postRestartPodsStatus.podsStatus}", EchoLevel.ALL)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen("Pipeline has succeeded", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen("Pipeline has failed", EchoLevel.ERROR)
}
