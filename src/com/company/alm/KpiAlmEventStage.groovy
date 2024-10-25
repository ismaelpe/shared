package com.caixabank.absis3


enum KpiAlmEventStage {

    ICP_SECRETS_VALIDATION("ICP_SECRETS_VALIDATION"),
    SONAR_QUALITY_GATE("SONAR_QUALITY_GATE"),
    UNDEFINED("UNDEFINED"),
	GENERAL("PIPELINE")


    private String name;

    private KpiAlmEventStage(String s) {
        name = s;
    }

    boolean equalsName(String other) {
        return name.equals(other);
    }

    static KpiAlmEventStage valueOfKpiLifeCycleStages(String other) {
        values().find { it.name == other }
    }

    String asString() {
        return name
    }
}
