package com.project.alm

/**
 * Arquitectura
 * Propuesta Tipo: ABSIS3 . Architecture Library 
 * Propuesta Tipo: ABSIS3 . Architecture Micro Architecture Plugin 
 * Propuesta Tipo: ABSIS3 . Architecture Micro Architecture 

 * Aplicaciones
 * Propuesta Tipo: ABSIS3 . Library
 * Propuesta Tipo: ABSIS3 . Micro Service
 * Propuesta Tipo: ABSIS3 . Data Service
 * @author u0180790*
 */
public enum GarAppType {

    MICRO_SERVICE("SRV.MS"),
    DATA_SERVICE("SRV.DS"),
	BFF_SERVICE("SRV.BFF"),
    LIBRARY("SRV.LIB"),
    ARCH_MICRO("ARQ.MIA"),
    ARCH_PLUGIN("ARQ.MAP"),
	ARCH_CONFIG("ARQ.CFG"),
	SRV_CONFIG("SRV.CFG"),
    ARCH_LIBRARY("ARQ.LIB"),
	GLOBAL_PIPELINE("SRV.CFG")

    private String name;

    private GarAppType(String s) {
        name = s;
    }

    public boolean equalsName(String other) {
        return name.equals(other);
    }

    static GarAppType valueOfType(String other) {
        values().find { it.name == other }
    }

    public String getGarName() {
        return name
    }

    //FIXME: Remove this when all pipeline-cps-method-mismatches have been solved for this class
	@NonCPS
	public String toString() {
		return name
	}

    public String prettyPrint() {
        return name.toString()
    }

}
