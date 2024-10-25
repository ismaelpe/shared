package com.caixabank.absis3


enum KpiAlmEventErrorCode {

    static String HTTP_404 = "HTTP_404"
    static String TIMEOUT_BLOCK_EXPIRED = "TIMEOUT_BLOCK_EXPIRED"
    static String UNDEFINED = "UNDEFINED"


    private String name;

    private KpiAlmEventErrorCode(String s) {
        name = s;
    }

    boolean equalsName(String other) {
        return name.equals(other);
    }

    static KpiAlmEventErrorCode valueOfKpiAlmEventErrorCode(String other) {
        values().find { it.name == other }
    }

    String asString() {
        return name
    }

}
