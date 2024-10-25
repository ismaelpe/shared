package com.project.alm

enum AuthorizationServiceToInformType {
    AGILE_WORKS("AGILE_WORKS"),
    MAXIMO("MAXIMO"),
    WITHOUT_AUTHORIZATION("WITHOUT_AUTHORIZATION"),
    INFORMATIVE_MAXIMO("INFORMATIVE_MAXIMO")

    private String name;

    private AuthorizationServiceToInformType(String s) {
        name = s;
    }

    boolean equalsName(String other) {
        return name.equals(other);
    }

    static AuthorizationServiceToInformType valueOfType(String other) {
        values().find { it.name == other }
    }

    String toString() {
        return name
    }

}
