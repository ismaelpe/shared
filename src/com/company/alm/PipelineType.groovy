package com.caixabank.absis3

public enum PipelineType {

    PROVISIONING("MICRO_APP"),
    CI("CI"),
    CREATE_RELEASE_CANDIDATE("CREATE_RC")


    private String name;

    private ArtifactSubType(String s) {
        name = s;
    }

    public boolean equalsName(String other) {
        return name.equals(other);
    }

    static PipelineType valueOfSubType(String other) {
        values().find { it.name == other }
    }
}
