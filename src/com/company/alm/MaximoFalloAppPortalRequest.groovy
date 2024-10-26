package com.project.alm;

class MaximoFalloAppPortalRequest extends MaximoAbstractFallo {

    MaximoFalloAppPortalRequest(String applicationName, String stageName, String httpMethod, String url, String body, String log) {
        this.equipoResponsable = MaximoEquipoResponsable.AppPortal
        this.resumen = "Una o más peticiones contra AppPortal han fallado"
        this.descripcion =
                "<p>Se han detectado fallos al hacer peticiones a AppPortal, la hora aproximada es: ${new Date().toString()}.</p>" +
                "<p>El framework de la aplicación es ALM-SRV, la aplicación es ${applicationName} y el stage que ha fallado es ${stageName}</p>" +
                "<p>La petición que ha generado el error ha sido un ${httpMethod} a la URI <code>${url}</code> con el siguiente body:</p>" +
                "<pre>${body ? '<code>'+body+'</code>' : '(La petición no tenía body o no era serializable)'}</pre>" +
                "<p>El log del error ha sido:</p>" +
                "<pre><code>${log}</code></pre>" +
                "<p>El log completo del build está como adjunto en este Máximo.</p>"
        this.pipelineExceptionErrorMessage =
            "Error al hacer peticiones a AppPortal.\n" +
                "Se ha abierto un Máximo automáticamente al Servicio TI: ${this.equipoResponsable.servicioTI} notificando la incidencia.\n" +
                "La petición que ha generado el error ha sido un ${httpMethod} a la URI ${url} con el siguiente body:" +
                "\n\n${body ? body : '(La petición no tenía body o no era serializable)'}\n\n" +
                "El log del error ha sido:\n\n${log}\n\n"
                "Si no se ha abierto la incidencia automáticamente en una pipeline ejecutada al hacer un git push, " +
                "la causa podría ser que la dirección de e-mail configurada en su configuración local de git no se corresponde con la dada de alta en Máximo."
    }

}
