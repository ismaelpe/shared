package com.caixabank.absis3


class NextJobOptions {

    private List<BuildParameter> parameters;
    private String nextJobName;
    private String type;


    JobType getJobType() {
        if ("URL_EXTERNA".equals(type)) {
            return JobType.MERGE_AUTO
        } else if ("LANZAR_JOB".equals(type)) {
            return JobType.INVOKE_JOB_AUTO
        }
    }

    enum JobType {
        INVOKE_JOB_AUTO,
        MERGE_AUTO
    }
}