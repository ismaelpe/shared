package com.caixabank.absis3

enum Gateway {

    INTERNAL, EXTERNAL

    static Gateway valueOfType(String other) {
        values().find { it.toString() == other.toUpperCase()  }
    }

}
