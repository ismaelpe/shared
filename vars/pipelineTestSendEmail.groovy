import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars

@Field Map pipelineParams

@Field String subject
@Field String from
@Field String to
@Field String replyTo
@Field String attachmentsPattern
@Field String body
@Field String mimeType

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    subject = params.subject
    from = params.from
    to = params.to
    replyTo = params.replyTo
    attachmentsPattern = params.attachmentsPattern
    body = params.body
    mimeType = params.mimeType
    
    pipeline {        
        agent {    node (almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '50'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
        }
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
            stage('send-email') {
                steps {
                  sendEmailStep()
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
 * Stage 'getAppRoutesStep'
 */
def sendEmailStep() {
    printOpen("subject ${subject}", EchoLevel.ALL)
    printOpen("from ${from}", EchoLevel.ALL)
    printOpen("to ${to}", EchoLevel.ALL)
    printOpen("replyTo ${replyTo}", EchoLevel.ALL)
    printOpen("attachmentsPattern ${attachmentsPattern}", EchoLevel.ALL)
    printOpen("body ${body}", EchoLevel.ALL)
    printOpen("mimeType ${mimeType}", EchoLevel.ALL)
    sendEmail(subject, from, to, replyTo, attachmentsPattern, body, mimeType)
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
    printOpen("TESTEMAIL_STARTED realizado con exito", EchoLevel.ALL)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen("TESTEMAIL_STARTED fail", EchoLevel.ALL)
}
