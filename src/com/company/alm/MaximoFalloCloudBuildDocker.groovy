package com.project.alm;

class MaximoFalloCloudBuildDocker extends MaximoAbstractFallo {

    MaximoFalloCloudBuildDocker(String environment, String log, String buildLog = null) {
        this.equipoResponsable = MaximoEquipoResponsable.Cloud
        this.resumen = "Fallo en la generación de la imagen Docker (Cloud_DEPLOY_BUILD_DOCKER_ERROR)"
        this.descripcion =
                "<p>La generación de la imagen Docker ha fallado a las ${new Date().toString()}.</p>" +
                "<p>El entorno es ${environment}</p>" +
                "<p>Log:</p>" +
                "<pre><code>${log}</code></pre>" +
                "<p>Se adjunta log de la pipeline.</p>" +
                "<p>${buildLog ? 'Se adjunta un fichero con el log del build en Cloud.' : 'No se pudo obtener el log del build de Cloud.'}</p>"
        this.attachments = buildLog ? ['cloudBuildLog.log': buildLog] : [:]
        this.pipelineExceptionErrorMessage =
            "El build en la imagen docker no ha funcionado correctamente.\n" +
                "Se ha abierto un Máximo automáticamente al Servicio TI: ${this.equipoResponsable.servicioTI} notificando la incidencia.\n" +
                "Log:\n\n${log}\n\n" +
                "Cloud build log:\n\n${buildLog ? buildLog : '<No se pudo obtener el log del build de Cloud>'}\n\n" +
                "Si no se ha abierto la incidencia automáticamente en una pipeline ejecutada al hacer un git push, " +
                "la causa podría ser que la dirección de e-mail configurada en su configuración local de git no se corresponde con la dada de alta en Máximo."
    }

}
