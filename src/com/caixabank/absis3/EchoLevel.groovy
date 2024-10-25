package com.caixabank.absis3

import com.cloudbees.groovy.cps.NonCPS

enum EchoLevel {

    NONE(0b0000, "NONE"),
    ERROR(0b0001, "ERROR"),
    INFO(0b0011, "INFO"),
    DEBUG(0b0111, "DEBUG"),
    ALL(0b1111, "ALL")

    int level;
    String name;

    private EchoLevel(int level, String name) {
        this.level = level;
        this.name = name;
    }

    @NonCPS
    public int level() {
        return this.level;
    }

    @NonCPS
    public String toString() {
        return this.name;
    }

    @NonCPS
    public static EchoLevel defaultLevel() {
        return EchoLevel.INFO;
    }

    @NonCPS
    public static EchoLevel toEchoLevel(String echoLevel) {
        if (echoLevel == EchoLevel.NONE.name) {
            return EchoLevel.NONE;
        } else if (echoLevel == EchoLevel.ERROR.name) {
            return EchoLevel.ERROR;
        } else if (echoLevel == EchoLevel.INFO.name) {
            return EchoLevel.INFO;
        } else if (echoLevel == EchoLevel.DEBUG.name) {
            return EchoLevel.DEBUG;
        } else if (echoLevel == EchoLevel.ALL.name) {
            return EchoLevel.ALL;
        } else {
            return null;
        }
    }
}

