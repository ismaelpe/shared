package com.project.alm


abstract class MaximoAbstractFallo {

    protected String tipoMaximo = "Incidencia"
    protected MaximoEquipoResponsable equipoResponsable
    protected String propietario = "CXB-00"
    protected String resumen
    protected String descripcion
    protected def attachments = [:]

    protected String pipelineExceptionErrorMessage

}
