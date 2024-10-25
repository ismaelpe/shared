package com.caixabank.absis3


interface PipelineExecutionMode {
    /**
     * Profiles constants
     */
    static final String DEFAULT_MODE = 'DEFAULT'
    static final String COMPLETE_TEST_AUTO = 'COMPLETE_TEST_AUTO'
    static final String COMPLETE_TEST_AUTO_HOTFIX = 'COMPLETE_TEST_AUTO_HOTFIX'
    static final String COMPLETE_TEST_AUTO_CONFIGURATIONFIX = 'COMPLETE_TEST_AUTO_CONFIGURATIONFIX'
    static final String COMPONENT_NEW_VERSION = 'COMPONENT_NEW_VERSION'
    static final String FEATURE_AUTO_MERGE_IF_OK = 'FEATURE_AUTO_MERGE_IF_OK'
    static final String SCHEDULED_CORE_UPDATE = 'SCHEDULED_CORE_UPDATE'
	static final String UPGRADE_CORE_AND_CREATE_RC = 'UPGRADE_CORE_AND_CREATE_RC'


    static final String ALM_FOLDER = "alm"

    String actionFlag()

    boolean invokeNextActionAuto()

    boolean skipTest()

    boolean skipIntegrationTest()

    boolean skipJavadoc()

    boolean skipAgileworksValidation()

    boolean isNotifyforFailureNeeded()

    String toString()

    String parseJobName(String jobName)
}
