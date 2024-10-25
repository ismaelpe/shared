package com.project.alm

enum Environment {

    EDEN, DEV, TST, PRE, PRO

    static Environment valueOfType(String other) {
        values().find { it.toString() == other.toUpperCase()  }
    }

}
