package com.project.alm

enum Gateway {

    INTERNAL, EXTERNAL

    static Gateway valueOfType(String other) {
        values().find { it.toString() == other.toUpperCase()  }
    }

}
