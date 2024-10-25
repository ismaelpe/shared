package com.project.alm;

class MaximoFalloELKKpiLogger extends MaximoAbstractFallo {

    MaximoFalloELKKpiLogger(KpiData kpiData, String log) {
        this.equipoResponsable = kpiData instanceof KpiAlmEvent ? MaximoEquipoResponsable.ARQUITECTURA_ABSIS3 : MaximoEquipoResponsable.ELK
        this.resumen = "Fallan las peticiones contra KPI loggers de ELK"
        this.descripcion =
                "<p>Se han detectado fallos al hacer peticiones a los KPI loggers de ELK, la hora aproximada es: ${new Date().toString()}.</p>" +
                "<p>Log:</p>" +
                "<pre><code>${log}</code></pre>"
        this.pipelineExceptionErrorMessage =
            "Error al hacer peticiones al logger de KPIs de ELK.\n" +
                "Se ha abierto un Máximo automáticamente al Servicio TI: ${this.equipoResponsable.servicioTI} notificando la incidencia.\n" +
                "Log:\n\n${log}\n\n" +
                "Si no se ha abierto la incidencia automáticamente en una pipeline ejecutada al hacer un git push, " +
                "la causa podría ser que la dirección de e-mail configurada en su configuración local de git no se corresponde con la dada de alta en Máximo."
    }

}
