package com.caixabank.absis3;

class MaximoFalloSSLErrorWhileExecutingMavenGoal extends MaximoAbstractFallo {

    MaximoFalloSSLErrorWhileExecutingMavenGoal(String environment, def log) {

        this.equipoResponsable = MaximoEquipoResponsable.ICP
        this.resumen = "Fallo de conectividad SSL al ejecutar maven en un agente Jenkins corriendo en ICP"
        this.descripcion =
                "<p>Se han producido diversos errores de conectividad SSL durante una ejecución maven en el JaaS que corre en ICP a las ${new Date().toString()}.</p>" +
                "<p>La ejecución tiene una política de reintentos, por lo que no parece un fallo puntual</p>" +
                "<p>El entorno es ${environment}.</p>" +
                "<p>Extracto del log del último intento realizado:</p>" +
                "<pre><code>${log}</code></pre>"
        this.pipelineExceptionErrorMessage =
            "Fallo de conectividad SSL al ejecutar maven en un agente Jenkins corriendo en ICP.\n" +
                "Se ha abierto un Máximo automáticamente al Servicio TI: ${this.equipoResponsable.servicioTI} notificando la incidencia.\n" +
                "Extracto del log del último intento realizado:\n\n${log}\n\n" +
                "Si no se ha abierto la incidencia automáticamente en una pipeline ejecutada al hacer un git push, " +
                "la causa podría ser que la dirección de e-mail configurada en su configuración local de git no se corresponde con la dada de alta en Máximo."
    }

}
