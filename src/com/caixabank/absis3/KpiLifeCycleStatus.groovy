package com.caixabank.absis3


enum KpiLifeCycleStatus {

    OK("OK"),
    KO("KO")


    private String name;

    private KpiLifeCycleStatus(String s) {
        name = s;
    }

    boolean equalsName(String other) {
        return name.equals(other);
    }

    static KpiLifeCycleStatus valueOfKpiLifeCycleStatus(String other) {
        values().find { it.name == other }
    }

    String asString() {
        return name
    }
}

