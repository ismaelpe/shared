import com.caixabank.absis3.*

def call(MaximoCreationRequest request, boolean forceExecution = false) {

    boolean openMaximo = forceExecution || "${env.ABSIS3_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()

    MaximoAbstractFallo fallo = request.tipoFallo
    String subject = "${fallo.tipoMaximo} ${fallo.equipoResponsable.servicioTI}${fallo.propietario ? ' '+fallo.propietario : ''} # ${fallo.resumen}"
    String filenameNames = createFilenamesArray(fallo.attachments)
        
    if (openMaximo) {

        printOpen("Creando Máximo al Servicio TI ${fallo.equipoResponsable.servicioTI} y remitente ${request.emailUsuarioCreador}" +
        "\nResumen: ${fallo.resumen}" +
        "\nDescripcion:" +
        "\n${fallo.descripcion}" +
        "\nEl subject será ${subject}" +
        "\nCon los siguientes ficheros adjuntos: ${filenameNames}",
        EchoLevel.INFO)

        if (filenameNames) {

            createAttachmentFiles(fallo.attachments)

            emailext(
                mimeType: 'text/html',
                body: "${fallo.descripcion}<p></p><p></p>",
                subject: "${subject}",
                from: "${request.emailUsuarioCreador}",
                to: "${GlobalVars.EMAIL_CREACION_MAXIMOS}",
                attachLog: true,
                attachmentsPattern: "${filenameNames}"
            )

            purgeAttachmentFiles(fallo.attachments)

        } else {

            emailext(
                mimeType: 'text/html',
                body: "${fallo.descripcion}<p></p><p></p>",
                subject: "${subject}",
                from: "${request.emailUsuarioCreador}",
                to: "${GlobalVars.EMAIL_CREACION_MAXIMOS}",
                attachLog: true
            )

        }

        return true

    } else {

        printOpen("La creación de máximos está desactivada. El Máximo no será creado al Servicio TI ${fallo.equipoResponsable.servicioTI}\n" +
            "mimeType: text/html\n" +
            "body: ${fallo.descripcion}\n" +
            "subject: ${subject}\n" +
            "from: ${request.emailUsuarioCreador}\n" +
            "to: ${GlobalVars.EMAIL_CREACION_MAXIMOS}", EchoLevel.INFO)

        return false
    }

}

private createFilenamesArray(def attachments) {

    def filenames = ""

    for (attachment in attachments) {

        filenames += ",${attachment.key}"
        
    }

    return filenames ? filenames.substring(1) : ""
}

private createAttachmentFiles(def attachments) {

    // We do this shenanigan to avoid serialization issues with the Map when using writeFile
    def attachmentsAsList = []
    for (Map.Entry entry : attachments) {
        attachmentsAsList.add(entry.getKey())
        attachmentsAsList.add(entry.getValue())
    }

    for (int ctr = 0; ctr < attachmentsAsList.size(); ctr += 2) {

        if (attachmentsAsList.get(ctr + 1)) {

            writeFile(file: attachmentsAsList.get(ctr), text: attachmentsAsList.get(ctr + 1))

        }

    }

}

private purgeAttachmentFiles(def attachments) {

    // We do this shenanigan to avoid serialization issues with the Map when using fileExists
    def filenames = []
    for (Map.Entry entry : attachments) {
        filenames.add(entry.getKey())
    }

    for (filename in filenames) {

        def fileExists = fileExists filename

        if (fileExists) {
            sh "rm ${filename}"
        }

    }

}
