package com.project.alm;

class MaximoFalloCloudDeployIsDisabled extends MaximoAbstractFallo {

    MaximoFalloCloudDeployIsDisabled(String environment, String feature) {
        this.equipoResponsable = MaximoEquipoResponsable.Cloud
        this.resumen = "Fallo al desplegar en Cloud por desactivación de la API"
        this.descripcion =
                "<p>Se ha detectado a las ${new Date().toString()} que la API de despliegue de Cloud está desactivada.</p>" +
                "<p>El entorno es ${environment}</p>" +
                "<p>La feature que se ha probado es <code>${feature}</code></p>"
        this.pipelineExceptionErrorMessage =
            "Deploy deshabilitado en Cloud.\n" +
                "Se ha abierto un Máximo automáticamente al Servicio TI: ${this.equipoResponsable.servicioTI} notificando la incidencia.\n" +
                "Si no se ha abierto la incidencia automáticamente en una pipeline ejecutada al hacer un git push, " +
                "la causa podría ser que la dirección de e-mail configurada en su configuración local de git no se corresponde con la dada de alta en Máximo."
    }

}
