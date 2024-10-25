package com.project.alm;

class MaximoFalloSonarConnectivityIssue extends MaximoAbstractFallo {

    MaximoFalloSonarConnectivityIssue(def log) {

        this.equipoResponsable = MaximoEquipoResponsable.SONAR
        this.resumen = "Ha habido diversos fallos de conectividad al intentar contactar contra Sonarqube"
        this.descripcion =
                "<p>Sobre las ${new Date().toString()} se han producido diversos errores de conectividad contra SonarQube.</p>" +
                "<p>La descarga tiene una política de reintentos, por lo que no parece un fallo puntual</p>" +
                "<p>Log del último intento realizado:</p>" +
                "<pre><code>${log}</code></pre>"
        this.pipelineExceptionErrorMessage =
            "Se han producido diversos errores de conectividad al intentar contactar con SonarQube.\n" +
                "Se ha abierto un Máximo automáticamente al Servicio TI: ${this.equipoResponsable.servicioTI} notificando la incidencia.\n" +
                "Log del último intento realizado:\n\n${log}\n\n" +
                "Si no se ha abierto la incidencia automáticamente en una pipeline ejecutada al hacer un git push, " +
                "la causa podría ser que la dirección de e-mail configurada en su configuración local de git no se corresponde con la dada de alta en Máximo."
    }

}
