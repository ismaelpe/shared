package com.project.alm


enum KpiAlmEventStatusType {

    SUCCESS("SUCCESS"),
    FAIL("FAIL"),
    APP_FAIL("APP_FAIL"),
    ALM_FAIL("ALM_FAIL")


    private String name;

    private KpiAlmEventStatusType(String s) {
        name = s;
    }

    boolean equalsName(String other) {
        return name.equals(other);
    }

    static KpiAlmEventStatusType valueOfKpiAlmEventStatusType(String other) {
        values().find { it.name == other }
    }

    String asString() {
        return name
    }
}

