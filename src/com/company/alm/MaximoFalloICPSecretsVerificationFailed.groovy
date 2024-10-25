package com.project.alm;

class MaximoFalloICPSecretsVerificationFailed extends MaximoAbstractFallo {

    MaximoFalloICPSecretsVerificationFailed(String secret, String icpEnvironment, String url, ICPApiResponse icpApiResponse) {

        this.equipoResponsable = MaximoEquipoResponsable.ICP
        this.resumen = "Fallo al verificar secrets de ICP"
        this.descripcion =
                "<p>Se han producido diversos errores al hacer la verificación de secrets de ICP a las ${new Date().toString()}.</p>" +
                "<p>La prueba tiene una política de reintentos, por lo que no parece un fallo puntual</p>" +
                "<p>El entorno ICP es ${icpEnvironment}.</p>" +
                "<p>La URI que estaba siendo invocada es: ${url}.</p>" +
                "<p>Log del último intento realizado:</p>" +
                "<pre><code>Status Code: ${icpApiResponse?.statusCode}</code></pre>" +
                "<pre><code>Body: ${icpApiResponse?.body}</code></pre>"
        this.pipelineExceptionErrorMessage =
            "Se han producido diversos errores al hacer la verificación de secrets de ICP.\n" +
                "Se ha abierto un Máximo automáticamente al Servicio TI: ${this.equipoResponsable.servicioTI} notificando la incidencia.\n" +
                "Log del último intento realizado:\n\nStatus Code: ${icpApiResponse?.statusCode}\nBody: ${icpApiResponse?.body}\n\n" +
                "Si no se ha abierto la incidencia automáticamente en una pipeline ejecutada al hacer un git push, " +
                "la causa podría ser que la dirección de e-mail configurada en su configuración local de git no se corresponde con la dada de alta en Máximo."
    }

}
