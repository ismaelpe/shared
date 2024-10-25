package com.caixabank.absis3

class ASEVars {

    static String ARCHETYPE_GROUP_ID = "com.caixabank.absis.arch.backend.archetype.aseclient"
    static String ARCHETYPE_ARTIFACT_ID = "seconnector-spring-boot-starter-archetype"

    static String CLIENT_GROUP_ID = "com.caixabank.absis.arch.backend.ase"
    static String CLIENT_ARTIFACT_ID = "se-<servicename>-starter"

    static String GAR_APPLICATION_NAME = "seconnector"

    static String DEV_ENVIROMENT = "DEV"
    static String TST_ENVIRONMENT = "TST"
    static String PRE_ENVIRONMENT = "PRE"
    static String PRO_ENVIRONMENT = "PRO"
    static String CLOSE_PIPELINE = "CLOSE"

    static String APP_TYPE = 'SIMPLE'
    static String APP_SUBTYPE = 'APP_LIB'

    static String ALM_JOB_PROVISIONING = "absis3/services/arch/alm/ase/job-provisioning-ASE"
    static String ALM_JOB_CIERRE_RELEASE = "absis3/services/arch/alm/ase/job-cierre-Release-ASE"

}