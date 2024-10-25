package com.caixabank.absis3

enum Environment {

    EDEN, DEV, TST, PRE, PRO

    static Environment valueOfType(String other) {
        values().find { it.toString() == other.toUpperCase()  }
    }

}
