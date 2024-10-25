package com.caixabank.absis3

public enum TrazabilidadGPLType {
    ALTA("I"),
    BAJA("R"),
    NADA("NO")

    private String name;

    private TrazabilidadGPLType(String s) {
        name = s;
    }

    public boolean equalsName(String other) {
        return name.equals(other);
    }

    static TrazabilidadGPLType valueOfType(String other) {
        values().find { it.name == other }
    }

    public String toString() {
        return name
    }

}