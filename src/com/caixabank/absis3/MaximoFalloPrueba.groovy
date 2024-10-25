package com.caixabank.absis3;

class MaximoFalloPrueba extends MaximoAbstractFallo {

    MaximoFalloPrueba() {
        this.equipoResponsable = MaximoEquipoResponsable.ARQUITECTURA_ABSIS3
        this.resumen = "Esto es una incidencia de test"
        this.descripcion = "<p>Este es el texto HTML</p><p>de la incidencia de test a ${this.equipoResponsable.servicioTI}</p>"
        this.pipelineExceptionErrorMessage = "Esto es un ejemplo de mensaje de error en la pipeline."
    }

}
