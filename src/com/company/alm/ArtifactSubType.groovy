package com.project.alm


public enum ArtifactSubType {

    MICRO_APP("MICRO_APP"),
    MICRO_ARCH("MICRO_ARCH"),
    PLUGIN_STARTER("PLUGIN_STARTER"),
    PLUGIN_STARTER_SAMPLE_APP("PLUGIN_STARTER_SAMPLE_APP"),
    STARTER("STARTER"),
    ARCH_LIB("ARCH_LIB"),
	ARCH_LIB_WITH_SAMPLEAPP("ARCH_LIB_WITH_SAMPLEAPP"),
	ARCH_CFG("ARCH_CFG"),
	SRV_CFG("SRV_CFG"),
    APP_LIB("APP_LIB"),
    SAMPLE_APP("SAMPLE_APP"),
    PLUGIN("PLUGIN"),
	APP_LIB_WITH_SAMPLEAPP("APP_LIB_WITH_SAMPLEAPP"),
	GLOBAL_PIPELINE("GLOBAL_PIPELINE")


    private String name;

    private ArtifactSubType(String s) {
        name = s;
    }

    public boolean equalsName(String other) {
        return name.equals(other);
    }

    static ArtifactSubType valueOfSubType(String other) {
        values().find { it.name == other }
    }
	@NonCPS
    public String toString() {
        return name
    }
}

