package com.caixabank.absis3;

class MaximoFalloDependencyPluginPreviouslyPresentNotFoundInNexus extends MaximoAbstractFallo {

    MaximoFalloDependencyPluginPreviouslyPresentNotFoundInNexus(def log) {

        this.equipoResponsable = MaximoEquipoResponsable.NEXUS
        this.resumen = "Una dependencia de tipo plugin que estaba previamente en Nexus parece no estar disponible ahora para su descarga"
        this.descripcion =
                "<p>Sobre las ${new Date().toString()} se ha intentado descargar una dependencia que al parecer no está disponible en Nexus.</p>" +
                "<p>La dependencia ha sido usada con anterioridad por lo que no comprendemos porque no está disponible ahora</p>" +
                "<p>La descarga tiene una política de reintentos, por lo que no parece un fallo puntual</p>" +
                "<p>Log del último intento realizado:</p>" +
                "<pre><code>${log}</code></pre>"
        this.pipelineExceptionErrorMessage =
            "Una dependencia de tipo plugin que estaba previamente en Nexus parece no estar disponible ahora para su descarga.\n" +
                "Se ha abierto un Máximo automáticamente al Servicio TI: ${this.equipoResponsable.servicioTI} notificando la incidencia.\n" +
                "Log del último intento realizado:\n\n${log}\n\n" +
                "Si no se ha abierto la incidencia automáticamente en una pipeline ejecutada al hacer un git push, " +
                "la causa podría ser que la dirección de e-mail configurada en su configuración local de git no se corresponde con la dada de alta en Máximo."
    }

}
