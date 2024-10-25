package com.caixabank.absis3;

class MaximoFalloSSLErrorWhileDoingITTest extends MaximoAbstractFallo {

    MaximoFalloSSLErrorWhileDoingITTest(String environment, String url, def log) {

        this.equipoResponsable = MaximoEquipoResponsable.ICP
        this.resumen = "Fallo de conectividad SSL al hacer llamadas hacia ICP"
        this.descripcion =
                "<p>Se han producido diversos errores de conectividad SSL durante una prueba de integración a las ${new Date().toString()}.</p>" +
                "<p>La prueba tiene una política de reintentos, por lo que no parece un fallo puntual</p>" +
                "<p>El entorno es ${environment} y la URI a la que se estaba accediendo era ${url}</p>" +
                "<p>Log:</p>" +
                "<pre><code>${log}</code></pre>"
        this.pipelineExceptionErrorMessage =
            "Fallo de conectividad SSL al hacer llamadas hacia ICP.\n" +
                "Se ha abierto un Máximo automáticamente al Servicio TI: ${this.equipoResponsable.servicioTI} notificando la incidencia.\n" +
                "La URI a la que se estaba accediendo era ${url}\n" +
                "Log:\n\n${log}\n\n" +
                "Si no se ha abierto la incidencia automáticamente en una pipeline ejecutada al hacer un git push, " +
                "la causa podría ser que la dirección de e-mail configurada en su configuración local de git no se corresponde con la dada de alta en Máximo."
    }

}
