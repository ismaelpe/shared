package com.project.alm;

class MaximoFalloGenericFailureWhenDoingRefresh extends MaximoAbstractFallo {

    MaximoFalloGenericFailureWhenDoingRefresh(String environment, String url, def log) {

        this.equipoResponsable = MaximoEquipoResponsable.Cloud
        this.resumen = "Fallo genérico de conectividad al hacer refresh de micros en Cloud"
        this.descripcion =
                "<p>Se han producido diversos errores al intentar hacer un refresh de micros desplegados en Cloud a las ${new Date().toString()}.</p>" +
                "<p>La prueba tiene una política de reintentos, por lo que no parece un fallo puntual</p>" +
                "<p>El entorno es ${environment} y la URI a la que se estaba accediendo era ${url}</p>" +
                "<p>Log:</p>" +
                "<pre><code>${log}</code></pre>"
        this.pipelineExceptionErrorMessage =
            "Fallo de refresh de micros en Cloud.\n" +
                "Se ha abierto un Máximo automáticamente al Servicio TI: ${this.equipoResponsable.servicioTI} notificando la incidencia.\n" +
                "La URI a la que se estaba accediendo era ${url}\n" +
                "Log:\n\n${log}\n\n" +
                "Si no se ha abierto la incidencia automáticamente en una pipeline ejecutada al hacer un git push, " +
                "la causa podría ser que la dirección de e-mail configurada en su configuración local de git no se corresponde con la dada de alta en Máximo."
    }

}
