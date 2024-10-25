package com.project.alm;

class MaximoFalloICPDeploy extends MaximoAbstractFallo {

    MaximoFalloICPDeploy(String environment, String log, String buildLog = null) {
        this.equipoResponsable = MaximoEquipoResponsable.ICP
        this.resumen = "Fallo al desplegar en ICP"
        this.descripcion =
                "<p>Detectado un fallo de despliegue en ICP a las ${new Date().toString()}.</p>" +
                "<p>El entorno es ${environment}</p>" +
                "<p>Log:</p>" +
                "<pre><code>${log}</code></pre>" +
                "<p>Se adjunta log de la pipeline.</p>" +
                "<p>${buildLog ? 'Se adjunta un fichero con el log del build en ICP.' : 'No se pudo obtener el log del build de ICP.'}</p>"
        this.attachments = buildLog ? ['icpBuildLog.log': buildLog] : [:]
        this.pipelineExceptionErrorMessage =
            "Error al desplegar en ICP.\n" +
                "Se ha abierto un Máximo automáticamente al Servicio TI: ${this.equipoResponsable.servicioTI} notificando la incidencia.\n" +
                "Log:\n\n${log}\n\n" +
                "ICP build log:\n\n${buildLog ? buildLog : '<No se pudo obtener el log del build de ICP>'}\n\n" +
                "Si no se ha abierto la incidencia automáticamente en una pipeline ejecutada al hacer un git push, " +
                "la causa podría ser que la dirección de e-mail configurada en su configuración local de git no se corresponde con la dada de alta en Máximo."
    }

}
