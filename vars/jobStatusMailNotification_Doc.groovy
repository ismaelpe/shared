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
            from: 'absis3.microservices.support@caixabank.com',
            replyTo: "${env.ABSIS3_SERVICES_EMAIL_DISTRIBUTION_LIST}",
            subject: "${status} - ${env.JOB_NAME} ${env.BUILD_NUMBER}",
            to: "${env.ABSIS3_SERVICES_EMAIL_DISTRIBUTION_LIST}"
}
