import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams
@Field String userIdParam
@Field String dataCenterParam
@Field String appTypeParam
@Field String appNameParam
@Field String appMajorVesionParam
@Field String blueOrGreenParam
@Field String configServerEnvironmentParam
@Field StringBuilder logmessageReport
@Field String configServerURL

//Pipeline para realizar el refresh de un micro o varios usando spring cloud bus kafka
//llama al endpoint bus-refresh del config server, el cual pone un mensaje en el topic kafka
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    userIdParam = params.userId
    dataCenterParam = params.dataCenter
    appTypeParam = params.appType
    appNameParam = params.appName
    appMajorVesionParam = params.appMajorVersion
    blueOrGreenParam = params.blueOrGreen
    configServerEnvironmentParam = params.configServerEnvironment
    logmessageReport = new StringBuilder()
    configServerURL = null

    pipeline {
        agent {    node(almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
        }
        environment {
            AppPortal = credentials('IDECUA-JENKINS-USER-TOKEN')
            JNKMSV = credentials('JNKMSV-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
            https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
        }
        stages {
            stage('stage-init') {
                steps {
                    stageInitStep()
                }
            }

            stage('stage-test-alive') {
                steps {
                    stageTestAliveStep()
                }
            }

            stage('stage-execute-bus-refresh') {
                steps {
                    stageExecuteBusRefreshStep()
                }
            }
            stage('stage-report') {
                steps {
                    stageReportStep()
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
 * Stage 'stageInitStep'
 */
def stageInitStep() {
    String message = null
    message = '====================================================\n'
    message += '>\n'
    message += '>      1 - STAGE-INIT-SHOW-PARAMS\n'
    message += ">      param userIdParam=${userIdParam}\n"
    message += ">      param dataCenterParam=${dataCenterParam}\n"
    message += ">      param appTypeParam=${appTypeParam}\n"
    message += ">      param appNameParam=${appNameParam}\n"
    message += ">      param appMajorVesionParam=${appMajorVesionParam}\n"
    message += ">      param blueOrGreenParam=${blueOrGreenParam}\n"
    message += ">      param configServerEnvironmentParam=${configServerEnvironmentParam}\n"
    message += '>\n'
    message += '>\n'
    message += '====================================================\n'
    printOpen("${message}", EchoLevel.INFO)

    if (dataCenterParam == '*') {
        printOpen('ERROR: datacenter, no puede ser nulo', EchoLevel.ERROR)
        error('datacenter, no puede ser nulo')
    }

    if (appTypeParam == '*') {
        printOpen('ERROR: appType, no puede ser nulo', EchoLevel.ERROR)
        error('appType, no puede ser nulo')
    }
}

/**
 * Stage 'stageTestAliveStep'
 */
def stageTestAliveStep() {
    String message = null
    message = '====================================================\n'
    message += '>\n'
    message += '>      2 - STAGE-TEST-ALIVE\n'
    message += '>\n'
    message += '>\n'
    message += '====================================================\n'
    printOpen("${message}", EchoLevel.INFO)

    try {
        sendRequestToConfigServer('GET', '/actuator/info', configServerEnvironmentParam, dataCenterParam)
    } catch (Exception e) {
        printOpen('Error test-alive ', EchoLevel.ERROR)
        printOpen(e.getMessage(), EchoLevel.ERROR)
        throw e
    }
}

/**
 * Stage 'stageExecuteBusRefreshStep'
 */
def stageExecuteBusRefreshStep() {
    String message = null
    message = '====================================================\n'
    message += '>\n'
    message += '>      3 - STAGE-EXECUTE-BUS-REFRESH\n'
    message += '>\n'
    message += '>\n'
    message += '====================================================\n'
    printOpen("${message}", EchoLevel.INFO)

    def destiny = dataCenterParam + ':' + appTypeParam + ':'

    if (appNameParam == '*') {
        destiny += '**'
                    }else {
        destiny += appNameParam + ':'

        if (appMajorVesionParam == '*') {
            destiny += '**'
                        }else {
            destiny += appMajorVesionParam + ':'

            if (blueOrGreenParam == '*') {
                destiny += '**'
                            }else {
                destiny += blueOrGreenParam
            }
        }
    }

    try {
        sendRequestToConfigServer('POST', '/actuator/bus-refresh/' + destiny, configServerEnvironmentParam, dataCenterParam)
    } catch (Exception e) {
        printOpen('Error bus-refresh ', EchoLevel.ERROR)
        printOpen(e.getMessage(), EchoLevel.ERROR)
        throw e
    }
}

/**
 * Stage 'stageReportStep'
 */
def stageReportStep() {
    String message = null
    message = '====================================================\n'
    message += '>\n'
    message += '>      4 - STAGE-REPORT (Check ACKs)\n'
    message += '>\n'
    message += '>\n'
    message += '====================================================\n'
    printOpen("${message}", EchoLevel.INFO)

    def response = null
    int responsesSize = 0

    try {
        //el /actuator/alm-log-bus-operations nos dice si las llamada asincrona de refresh fue recibida por las instancias, estas devuelven un ACK
        response =     sendRequestToConfigServer('GET', '/actuator/alm-log-bus-operations', configServerEnvironmentParam, dataCenterParam)
        def json = new groovy.json.JsonSlurper().parseText(response.content)
        responsesSize  = json[0]?.responses?.size()
        printOpen("latest response acks size=${responsesSize}", EchoLevel.DEBUG)
                        } catch (Exception e) {
        printOpen('Error alm-log-bus-operations ', EchoLevel.ERROR)
        printOpen(e.getMessage(), EchoLevel.ERROR)
        throw e
    }

    if (responsesSize == null || responsesSize == 0) {
        printOpen('WARNING cero respuestas en ultimo tracking de respuestas ACKs contestadas por la ultima llamada asincrona de refresh', EchoLevel.ERROR)
        printOpen('Please check the last call to <config_server>/actuator/alm-log-bus-operations in your browser in order to track the last refresh-bus call', EchoLevel.ERROR)
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
    printOpen('FAILURE', EchoLevel.ERROR)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}
