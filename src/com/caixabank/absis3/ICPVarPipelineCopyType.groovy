package com.caixabank.absis3


public enum ICPVarPipelineCopyType {

    EX_MODE_ALL("ALL"),
    EX_MODE_DEPLOY_IT("DEPLOY_IT"),
    EX_MODE_PLUGIN_STARTER("PLUGIN_STARTER"),
    EX_MODE_ONLY_IT("ONLY_IT"),
	EX_MODE_DEPLOY("DEPLOY"),
	ORIGIN_BRANCH("BRANCH"),
	ORIGIN_TAG("TAG"),
    DOES_NOT_APPLY("DOES_NOT_APPLY")


    private String name;

    private ICPVarPipelineCopyType(String s) {
        name = s;
    }

    public boolean equalsName(String other) {
        return name.equals(other);
    }

    static ICPVarPipelineCopyType valueOfVarPipelineCopyType(String other) {
        values().find { it.name == other }
    }

    public String toString() {
        return name
    }
}

