package com.project.alm

enum LiquibaseStatusType {
	OK("OK"),
    ERROR_INTERNAL("ERROR_INTERNAL"),
    ERROR_LIQUIBASE("ERROR_LIQUIBASE"),
	ERROR_NOT_FINISHED("ERROR_NOT_FINISHED")

    private String name;

    private LiquibaseStatusType(String s) {
        name = s;
    }

    public boolean equalsName(String other) {
        return name.equals(other);
    }

    static PipelineStructureType valueOfType(String other) {
        values().find { it.name == other }
    }


    String asString() {
        return name
    }

}
