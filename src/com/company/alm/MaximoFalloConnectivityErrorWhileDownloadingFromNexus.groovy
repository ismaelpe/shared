package com.caixabank.absis3;

class MaximoFalloConnectivityErrorWhileDownloadingFromNexus extends MaximoAbstractFallo {

    MaximoFalloConnectivityErrorWhileDownloadingFromNexus(def log) {

        this.equipoResponsable = MaximoEquipoResponsable.NEXUS
        this.resumen = "Fallo de conectividad al realizar descarga de dependencias desde Nexus"
        this.descripcion =
                "<p>Se han producido diversos errores de conectividad al intentar descargar dependencias de Nexus a las ${new Date().toString()}.</p>" +
                "<p>La prueba tiene una política de reintentos, por lo que no parece un fallo puntual</p>" +
                "<p>Log del último intento realizado:</p>" +
                "<pre><code>${log}</code></pre>"
        this.pipelineExceptionErrorMessage =
            "Fallo de conectividad al realizar descarga de dependencias desde Nexus.\n" +
                "Se ha abierto un Máximo automáticamente al Servicio TI: ${this.equipoResponsable.servicioTI} notificando la incidencia.\n" +
                "Log del último intento realizado:\n\n${log}\n\n" +
                "Si no se ha abierto la incidencia automáticamente en una pipeline ejecutada al hacer un git push, " +
                "la causa podría ser que la dirección de e-mail configurada en su configuración local de git no se corresponde con la dada de alta en Máximo."
    }

}
