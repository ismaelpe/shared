package com.project.alm

enum MaximoEquipoResponsable {

    ARQUITECTURA_ALM("ALMCORE.ARQ.LIB"),
    ICP("CLDALM.PCLD"),
    GPL("IDEGPL.PCLD"),
    ELK("RECA3I.PCLD"), //Esto parece pertenecer al grupo de la recolectora de Canal y no ELK. Confirmar que podemos abrir máximos ahí por el tema de KPIs
    GIT("IDESCM.IDE"),
    NEXUS("IDENEX.IDE"),
    SONAR("SONARS.PCLD"),
    PRUEBA("PROVESRUNTIME.SI.MCA")

    public final String servicioTI

    MaximoEquipoResponsable(String servicioTI) {
        this.servicioTI = servicioTI
    }

    static MaximoEquipoResponsable fromString(String value) {
        values().find { it.servicioTI == value }
    }

}
