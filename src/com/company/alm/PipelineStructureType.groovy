package com.caixabank.absis3

import com.cloudbees.groovy.cps.NonCPS

enum PipelineStructureType {
	DEPLOY_PROTOTYPE("DEPLOY_PROTOTYPE"),
    PROVISIONING("PROVISIONING"),
    CI("CI"),
    NO_CI("NO_CI"),
	CI_CONFIGS("CI_CONFIGS"),
    CICS_WSDL("CICS_WSDL"),
    CICS_HIS_BUILD("CICS_HIS_BUILD"),
    CICS_HIS_PROVISIONING("CICS_HIS_PROVISIONING"),
    CONFIG_RELEASE("CONFIG_RELEASE"),
    RELEASE("RELEASE"),
	RELEASE_CONFIGLIB("RELEASE_CONFIGLIB"),
    RELEASE_CANDIDATE("RELEASE_CANDIDATE"),
	RELEASE_CANDIDATE_CONFIGLIB("RELEASE_CANDIDATE_CONFIGLIB"),
    IOP_PRO("IOP_PRO"),
    IOP_PRO_CONFIGFIX("IOP_PRO_CONFIGFIX"),
	IOP_PRO_CONFIGLIB("IOP_PRO_CONFIGLIB"),
    ROLLBACK("ROLLBACK"),
    ROLLBACK_FINISH("ROLLBACK_FINISH"),
    HOTFIX("HOTFIX"),
    CONFIGFIX("CONFIGFIX"),
	CONFIGLIBFIX("CONFIGLIBFIX"),
    DELETE("DELETE"),
    CLOSE("CLOSE"),
	CLOSE_CAMPAIGN("CLOSE_CAMPAIGN"),
	INC_CAMPAIGN_CANNARY("INC_CAMPAIGN_CANNARY"),
    INC_CANNARY("INC_CANNARY"),
    IOP_DATASOURCES("IOP_DATASOURCES"),
    ADS_BUILD("ADS_BUILD"),
    ADS_PROVISIONING("ADS_PROVISIONING"),
	TANDEM_BUILD("TANDEM_BUILD"),
	TANDEM_PROVISIONING("TANDEM_PROVISIONING"),
    CGS_BUILD("CGS_BUILD"),
    CGS_PROVISIONING("CGS_PROVISIONING"),
    CLIENT_SNAPSHOT_BUILD("CLIENT_SNAPSHOT_BUILD"),
    CLIENT_LIFECYCLE_PROVISIONING("CLIENT_LIFECYCLE_PROVISIONING"),
	BBDD_LIQUIBASE_STATUS("BBDD_LIQUIBASE_STATUS"),
	BBDD_VALIDATION("BBDD_VALIDATION"),
	REPO_CONFIGURATION("REPO_CONFIGURATION"),
	NOTIFY_END_CAMPAIGN("NOTIFY_END_CAMPAIGN"),
	RELEASE_CANDIDATE_BBDD("RELEASE_CANDIDATE_BBDD"),
	DEPLOY_BBDD_SCRIPT("DEPLOY_BBDD_SCRIPT"),
    REFRESH_LITERALS("REFRESH_LITERALS"),
    START_STOP("START_STOP"),
	STRESS_TESTS_PREPARATION("STRESS_TESTS_PREPARATION"),
    RESTART_APP("RESTART_APP"),
    JOB_CLEAN_DEV_DUPLICATED_PODS("JOB_CLEAN_DEV_DUPLICATED_PODS"),
    SONAR_SCAN_AND_SEND_REPORT("SONAR_SCAN_AND_SEND_REPORT"),
    CALL_ACTUATOR_ENV("CALL_ACTUATOR_ENV")
    private String name;

    private PipelineStructureType(String s) {
        name = s;
    }

    public boolean equalsName(String other) {
        return name.equals(other);
    }

    static PipelineStructureType valueOfType(String other) {
        values().find { it.name == other }
    }

    @NonCPS
    //FIXME: Remove this when all pipeline-cps-method-mismatches have been solved for this class
    String toString() {
        return name
    }

    String asString() {
        return name
    }

}