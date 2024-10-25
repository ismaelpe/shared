package com.caixabank.absis3


enum KpiAlmEventSubOperation {

    CALL("CALL"),
    REQUEST("REQUEST"),
	PIPELINE("PIPELINE"),
    RETRY("RETRY")


    private String name;

    private KpiAlmEventSubOperation(String s) {
        name = s;
    }

    boolean equalsName(String other) {
        return name.equals(other);
    }

    static KpiAlmEventSubOperation valueOfKpiAlmEventSubOperation(String other) {
        values().find { it.name == other }
    }

    String asString() {
        return name
    }
}
