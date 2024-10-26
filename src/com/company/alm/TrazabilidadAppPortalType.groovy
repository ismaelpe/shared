package com.project.alm

public enum TrazabilidadAppPortalType {
    ALTA("I"),
    BAJA("R"),
    NADA("NO")

    private String name;

    private TrazabilidadAppPortalType(String s) {
        name = s;
    }

    public boolean equalsName(String other) {
        return name.equals(other);
    }

    static TrazabilidadAppPortalType valueOfType(String other) {
        values().find { it.name == other }
    }

    public String toString() {
        return name
    }

}