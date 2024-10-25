package com.project.alm

public enum PipelineBehavior {

    PUSH_OPENED_MR("PUSH_OPENED_MR"),
    PUSH_OPENED_MR_WIP("PUSH_OPENED_MR_WIP"),
    FIRST_MR("FIRST_MR"),
    NOT_FIRST_MR("NOT_FIRST_MR"),
    PUSH_NO_MR("PUSH_NO_MR"),
    LIKE_ALWAYS("LIKE_ALWAYS"),
    COMMITLOG_REQUESTED_NO_CI("COMMITLOG_REQUESTED_NO_CI")


    private String name;

    private PipelineBehavior(String s) {
        name = s;
    }

    public boolean equalsName(String other) {
        return name.equals(other);
    }

    static PipelineBehavior valueOfSubType(String other) {
        values().find { it.name == other }
    }
}
