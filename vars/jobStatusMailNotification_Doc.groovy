import com.project.alm.EchoLevel

/**
 *  Envia un notificación por email
 *
 *  Parametros:
 *    status | String | Obligatorio | Estado de la ejecución de la build que se incluirá en el asunto del mensaje
 */

def call(status) {

    printOpen("Send Email", EchoLevel.ALL)

    mail body: "${status} - ${env.BUILD_URL}",
            from: 'alm.microservices.support@project.com',
            replyTo: "${env.ALM_SERVICES_EMAIL_DISTRIBUTION_LIST}",
            subject: "${status} - ${env.JOB_NAME} ${env.BUILD_NUMBER}",
            to: "${env.ALM_SERVICES_EMAIL_DISTRIBUTION_LIST}"
}
