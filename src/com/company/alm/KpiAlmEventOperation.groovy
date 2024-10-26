package com.project.alm


enum KpiAlmEventOperation {

    /**
     * HTTP requests
     */
    GPL_HTTP_CALL("GPL_HTTP_CALL"),
    CATALOG_HTTP_CALL("CATALOG_HTTP_CALL"),
    CATMSV_HTTP_CALL("CATMSV_HTTP_CALL"),
    CloudAPI_HTTP_CALL("CloudAPI_HTTP_CALL"),
    CloudAPI_HTTP_POLL("CloudAPI_HTTP_POLL"),
    CloudAPI_HTTP_CURL("CloudAPI_HTTP_CURL"),
	LITMIDAPI_HTTP_CURL("LITMIDAPI_HTTP_CURL"),
    MULEAPI_HTTP_CURL("MULE_HTTP_CURL"),
    ERRORMANAGEMENT_HTTP_CALL("ERRORMANAGEMENT_HTTP_CALL"),
    SONAR_QUALITY_GATE_HTTP_REPORT_TASK("SONAR_QUALITY_GATE_HTTP_REPORT_TASK"),
    SONAR_QUALITY_GATE_HTTP_ANALYSIS("SONAR_QUALITY_GATE_HTTP_ANALYSIS"),
	CHECKMARX_SCAN("CHECKMARX_SCAN"),
    /**
     * Micro Operations
     */
    RUN_ACTUATOR_REFRESH("RUN_ACTUATOR_REFRESH"),
    CHECK_ENDPOINT("CHECK_ENDPOINT"),
    /**
     * Cloud Operations
     */
    Cloud_AVAILABILITY_CHECK("Cloud_AVAILABILITY_CHECK"),
    Cloud_DEPLOY("Cloud_DEPLOY"),
    Cloud_BUILD_DOCKER_IMAGE("Cloud_BUILD_DOCKER_IMAGE"),
    Cloud_CONSOLIDATE_NEW_DEPLOY("Cloud_CONSOLIDATE_NEW_DEPLOY"),
    Cloud_DEPLOY_STATE("Cloud_DEPLOY_STATE"),
    Cloud_DEPLOY_PROTOTYPE("Cloud_DEPLOY_PROTOTYPE"),
	Cloud_DEPLOY_WIREMOCK("Cloud_DEPLOY_WIREMOCK"),
	Cloud_DEPLOY_STRESS_MICRO("Cloud_DEPLOY_STRESS_MICRO"),
    Cloud_DEPLOY_SCRIPT("Cloud_DEPLOY_SCRIPT"),
    Cloud_SECRETS_VALIDATION("Cloud_SECRETS_VALIDATION"),
    /**
     * Maven Operations
     */
    MVN_SONAR_SCAN("MVN_SONAR_SCAN"),
    MVN_BUILD_WORKSPACE("MVN_BUILD_WORKSPACE"),
    MVN_BUILD_WORKSPACE_DEPLOY_NEXUS("MVN_BUILD_WORKSPACE_DEPLOY_NEXUS"),
    MVN_DEPLOY_NEXUS("MVN_DEPLOY_NEXUS"),
    MVN_DEPLOY_MICROS_NEXUS("MVN_DEPLOY_MICROS_NEXUS"),
    MVN_INSTALL_DEPLOY_CLIENT_ARTIFACT("MVN_INSTALL_DEPLOY_CLIENT_ARTIFACT"),
    MVN_VALIDATE_FORBIDDEN_DEPENDENCY_RESTRICTIONS("MVN_VALIDATE_FORBIDDEN_DEPENDENCY_RESTRICTIONS"),
    MVN_RUN_IT_TESTS("MVN_RUN_IT_TESTS"),
    MVN_DEPLOY_ADS_TRANSACTION_NEXUS("MVN_DEPLOY_ADS_TRANSACTION_NEXUS"),
	MVN_DEPLOY_TANDEM_TRANSACTION_NEXUS("MVN_DEPLOY_TANDEM_TRANSACTION_NEXUS"),
    MVN_DEPLOY_ASE_SERVICE_NEXUS("MVN_DEPLOY_ASE_SERVICE_NEXUS"),
    MVN_DEPLOY_GCS_OPERATION_NEXUS("MVN_DEPLOY_GCS_OPERATION_NEXUS"),
    MVN_DEPLOY_SQL_CHANGE_SET_PACKAGE_NEXUS("MVN_DEPLOY_SQL_CHANGE_SET_PACKAGE_NEXUS"),
    MVN_DEPLOY_SQL_SCRIPT_NEXUS("MVN_DEPLOY_SQL_SCRIPT_NEXUS"),
    MVN_DEPLOY_SQL_SCRIPT_RELEASE_NEXUS("MVN_DEPLOY_SQL_SCRIPT_RELEASE_NEXUS"),
    /**
     * Jenkins Plugin Operations
     */
    SONAR_QUALITY_GATE_PLUGIN("SONAR_QUALITY_GATE_PLUGIN"),

	/**
	 * Pipelines End State 
	 */
	PIPELINE_BUILD("PIPELINE_BUILD"),
	PIPELINE_RC("PIPELINE_RC"),
	PIPELINE_RELEASE("PIPELINE_RELEASE"),
	PIPELINE_DEPLOY_PRO("PIPELINE_DEPLOY_PRO"),
	PIPELINE_INC_CANNARY("PIPELINE_INC_CANNARY"),
	PIPELINE_CLOSE_RELEASE("PIPELINE_CLOSE_RELEASE"),
	PIPELINE_BBDD_VALIDATION("PIPELINE_BBDD_VALIDATION"),
	PIPELINE_BBDD_REPORT("PIPELINE_BBDD_REPORT"),
	PIPELINE_CAMPAING_INC_CANNARY("PIPELINE_CAMPAING_INC_CANNARY"),
	PIPELINE_CAMPAING_NOTIFY("PIPELINE_CAMPAING_NOTIFY"),
	PIPELINE_CAMPAING_CLOSE_RELEASE("PIPELINE_CAMPAING_CLOSE_RELEASE"),
	PIPELINE_RELEASE_BBDD("PIPELINE_RELEASE_BBDD"),
	PIPELINE_DEPLOY_PROTO("PIPELINE_DEPLOY_PROTO"),
	PIPELINE_DEPLOY_BBDD("PIPELINE_DEPLOY_BBDD"),
	PIPELINE_ADS_PROVISIONING("PIPELINE_ADS_PROVISIONING"),
	PIPELINE_ADS_BUILD("PIPELINE_ADS_BUILD"),
	PIPELINE_ADS_CLOSE("PIPELINE_ADS_CLOSE"),
	PIPELINE_TANDEM_PROVISIONING("PIPELINE_TANDEM_PROVISIONING"),
	PIPELINE_TANDEM_BUILD("PIPELINE_TANDEM_BUILD"),
	PIPELINE_TANDEM_CLOSE("PIPELINE_TANDEM_CLOSE"),
	PIPELINE_CGS_PROVISIONING("PIPELINE_CGS_PROVISIONING"),
	PIPELINE_CGS_BUILD("PIPELINE_CGS_BUILD"),
	PIPELINE_CGS_CLOSE("PIPELINE_CGS_CLOSE"),
	PIPELINE_ASE_PROVISIONING("PIPELINE_ASE_PROVISIONING"),
	PIPELINE_ASE_BUILD("PIPELINE_ASE_BUILD"),
	PIPELINE_ASE_CLOSE("PIPELINE_ASE_CLOSE"),
    PIPELINE_JOB_CLEAN_DEV_DUPLICATED_PODS("PIPELINE_JOB_CLEAN_DEV_DUPLICATED_PODS"),
	PIPELINE_STRESS_TEST_PREPARATION("PIPELINE_STRESS_TEST_PREPARATION"),
    PIPELINE_ACTUATOR_ENV("PIPELINE_ACTUATOR_ENV")
	
	
    private String name;

    private KpiAlmEventOperation(String s) {
        name = s;
    }

    boolean equalsName(String other) {
        return name.equals(other);
    }

    static KpiAlmEventOperation valueOfKpiAlmEventOperation(String other) {
        values().find { it.name == other }
    }

    String asString() {
        return name
    }
}
