package com.project.alm

public enum ArtifactType {
    SIMPLE("SIMPLE"),
    AGREGADOR("AGREGADOR")

    private String name;

    private ArtifactType(String s) {
        name = s;
    }

    public boolean equalsName(String other) {
        return name.equals(other);
    }

    static ArtifactType valueOfType(String other) {
        values().find { it.name == other }
    }
	@NonCPS
    public String toString() {
        return name
    }

}
