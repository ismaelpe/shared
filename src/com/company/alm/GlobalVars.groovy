package com.project.alm

class GlobalVars {
	
	static String URL_API_PORTAL = "https://l.tf7.lacaixa.es/alm/developerportal/ADPFrontend/services"
	static String URL_REDIRECTORA_JENKINS = "https://jnkmsv.pro.int.srv.project.com/jenkins/"
	static String PROFILES_SPRING="SPRING_PROFILES_ACTIVE"
	
	static Float Cloud_RATIO_CPU_PRO = 1.05
	static Float Cloud_RATIO_CPU_PRE = 1.33
	
	static Float OCP_RATIO_CPU_PRO = 0.95
	static Float OCP_RATIO_CPU_PRE = 0.75
	
	static Float OCP_RATIO_SIDECAR_CPU_PRO=0.1
	static Float OCP_RATIO_SIDECAR_CPU_PRE=0.1
	
	static Float OCP_RATIO_SIDECAR_MEM_PRE=0.3
	static Float OCP_RATIO_SIDECAR_MEM_PRO=0.2
	static Float OCP_SIDECAR_MEM_PRE_REQ=250
	static Float OCP_SIDECAR_MEM_PRO_REQ=250
	static Float OCP_SIDECAR_MEM_PRE_LIM=400
	static Float OCP_SIDECAR_MEM_PRO_LIM=400
	
	static String nonProxyHosts_EnvVar = "nonProxyHosts"
	static String additionalNonProxyHosts_EnvVar = "http.additionalNonProxyHosts"

	static String CAMPAIGN_GAR_APP = "CampaignIOP" 
	static String DEFAULT_DEPLOYMENT_TYPE = "D"
	
	static String EMAIL_FROM_ALM = "openservices.alm@project.com"    
    static String JENKINS_CURRENT_LOGIN_USER = "admin"

	//static String Cloud_LIQUIBASE_IMAGE = "docker-registry.cloud.project.com/containers/ab3app/arqrunbbdd" 
	//static String Cloud_LIQUIBASE_IMAGE_VERSION = "1.4.0"

	//static String Cloud_PROTOTYPE_IMAGE = "docker-registry.cloud.project.com/containers/ab3app/demoprototype1"
	//static String Cloud_PROTOTYPE_IMAGE_VERSION = "0.0.2"
	
	//static String Cloud_LIQUIBASE_IMAGE = "docker-registry.cloud.project.com/containers/ab3app/arqrunbbdd" 
	//static String Cloud_LIQUIBASE_IMAGE_VERSION = "1.4.0"
	static String Cloud_LIQUIBASE_IMAGE = "docker-registry.cloud.project.com/containers/ab3cor/liquibasedemo1"
	static String Cloud_LIQUIBASE_IMAGE_VERSION = "4.5.0"
	
	static String Cloud_PROTOTYPE_IMAGE = "docker-registry.cloud.project.com/containers/ab3app/demoprototype1"
	static String Cloud_PROTOTYPE_IMAGE_VERSION = "2.5.0"
	
	static int MAX_DEV_Cloud_RESTARTS = 2
	
	static int TIMEOUT_Cloud = 16
	static int TIMEOUT_MAX_Cloud = 30
	
	static String JENKINS_GIT_USER = "SVC_ALM_CONTDELIVE"
	static String SQL_SCRIPT_FILENAME = "script.sql"
	
	static String LIQUIBASE_CHANGE_SET_POM_FILENAME = "sqlchangeSets.xml"
	static String SQL_BBDD_INFO = "info.yaml" 
	static String SQL_CONFIG_DIRECTORY = "sql/config/"
	static String SQL_INPUT_DIRECTORY = "sql/input/"
	static String SQL_RELEASE_DIRECTORY = "sql/release/"
	static String SQL_OUTPUT_DIRECTORY = "sql/output/"
	static String SQL_OUTPUT_DIRECTORY_PROMOTED = "sql/deployed/"
	static String SQL_GENERATOR_TEMP_DIRECTORY = "bbddscript"
	
    static String reportToEmailList = "malvarez@silk.es"

    static String ARCH_ARTIFACT = "almcore"

    static String ALM_CORE_GROUPID = "com.project.alm"
    static String ALM_CORE_ARTIFACTID = "almcore-lib"

    static String EMAIL_REPORT = "alm.microservices.support@project.com"
    static String PROJECTS_LIST_SEPARATOR = ";"
    static String LIST_SEPARATOR = ";"
    static String GIT_CREDENTIAL_PROFILE_ID = "GITLAB_CREDENTIALS"
    static String GIT_CREDENTIAL_PASSWORD_VAR = "GIT_PASSWORD"
    static String GIT_CREDENTIAL_USER_VAR = "GIT_USERNAME"
    	
	static final String CANARY_TYPE_PROPERTY = "info.rollout-type";
	static final String CANARY_TYPE_DEVOPS = "devops";
	static final String CANARY_TYPE_CAMPAIGN = "campaign";


    /**
     * Artifacts with an arch version lower than that value will fail
     */
	// Este es el valor de la version minima de arquitectura
	static String MAX_VERSION_ARCH_NEXUS = "1.19.0"
	static String MINIMUM_VERSION_ARCH_Cloud = "1.11.0"
	
    static String MINIMUM_VERSION_ARCH = "1.1.0"
   
    //static String MINIMUM_VERSION_ARCH_EDEN = "1.6.0"
    //Esta version afecta a:
    // - artefactos en EDEN (feature)
    // - artefactos en DEV (master)
    static String MINIMUM_VERSION_ARCH_DEV = "1.6.0"
    //Esta afectacion es de los artefactos en RC
    static String MINIMUM_VERSION_ARCH_TST = "1.5.0"
    //Esto afecta a artefactos en Releases y hotfix
    static String MINIMUM_VERSION_ARCH_PRE = "1.4.0"
    //No tiene afectacion ninguna
    static String MINIMUM_VERSION_ARCH_PRO = "1.1.0"

    static String ALM_SKIP_MINIMUM_VERSION_VALIDATION_LIST = "tauxconnector-micro"

    /**
     * Minimum arch version that needs JDK 1.11
     */
    static String MINIMUM_VERSION_ARCH_NEEDS_JAVA11 = "1.6.0"

    /**
     * Step skipping configuration
     */
    static boolean PUSH_SUREFIRE = false
    static boolean GSA_ENABLED = true

	/**
	 * JSON used in publishErrorManagemenTranslations
	 */
	static String JSON_ERROR_MANAGEMENT_PUT = "src/main/resources/errormanagement_put.json"
	static String JSON_ERROR_MANAGEMENT_DELETE = "src/main/resources/errormanagement_delete.json"

    /**
     * Dependency validations
     */
    static String ALM_SERVICES_DEPENDENCY_WHITELIST_BFF = "{}"
    static String ALM_SERVICES_DEPENDENCY_WHITELIST_DATASERVICE = "{}"

    /**
     * Micros that require special deployment scheme (p.e. almlogcollector-micro)
     */

    static String ALM_SERVICES_EXECUTE_IT_PRO = ""
    static String ALM_SERVICES_IT_TEST_JARS_GENERATION_WHITELIST = ""
    static String ALM_SERVICES_SIMPLIFIED_ALM_WHITELIST = ""

    /**
     * JSON used in pushCatalog
     */
    static String JSON_DEPENDENCIES = "target/classes/META-INF/maven/dependencies.json"
    static String JSON_ENDPOINTS = "target/classes/META-INF/maven/endpoints.json"

	static String CATALOG_CAMPAIGN_CANNARY_TYPE = "C"
	static String CATALOG_DEVOPS_CANNARY_TYPE = "D"
	
	static String ALM_JOB_CLONE_TO_OCP = "job/alm/job/services/job/arch/job/alm/job/job-clone-micro-to-ocp"
	
    static String CANNARY_PERCENTAGE_ALM = "info.beta-traffic.percentage"
	static String CAMPAIGN_CANNARY_PERCENTAGE_ALM = "info.campaign-beta-traffic.percentage"
    static String ALM_JOB_MANUAL_COPY = "alm/services/arch/alm/job-manual-Copy"
    static String ALM_JOB_CREATE_RC = "alm/services/arch/alm/job-create-RC"
	static String ALM_JOB_DEPLOY_PROTOTYPE = "alm/services/arch/alm/job-deploy-Prototype-to-Cloud"
	static String ALM_JOB_CREATE_RC_CONFIGLIB = "alm/services/arch/alm/job-create-RC-configlib"
    static String ALM_JOB_CREATE_RELEASE = "alm/services/arch/alm/job-create-Release"
	static String ALM_JOB_CREATE_RELEASE_CONFIGLIB = "alm/services/arch/alm/job-create-Release-configlib"
    static String ALM_JOB_DEPLOY_PRO = "alm/services/arch/alm/job-deploy-to-PRO"
	static String ALM_JOB_DEPLOY_PRO_CONFIGLIB = "alm/services/arch/alm/job-deploy-to-PRO-configlib"
	static String ALM_JOB_DEPLOY_PRO_CONFIGFIX = "alm/services/arch/alm/job-deploy-to-PRO-configfix"
    static String ALM_JOB_ROLLBACK = "alm/services/arch/alm/job-rollback-artifact"
    static String ALM_JOB_CIERRE_RELEASE = "alm/services/arch/alm/job-cierre-Release"
	static String ALM_JOB_CIERRE_RELEASE_CONFIGLIB = "alm/services/arch/alm/job-cierre-Release-configlib"
    static String ALM_JOB_DELETE_COMPONENT = "alm/services/arch/alm/job-delete-component-version"
    static String ALM_JOB_INCREMENT_CANNARY = "alm/services/arch/alm/job-inc-cannary"
    static String ALM_JOB_INCREMENT_CAMPAIGN_CANNARY = "alm/services/arch/alm/job-inc-campaign-cannary"
    static String ALM_JOB_CREATE_FIX = "alm/services/arch/alm/job-create-fix"
    static String ALM_JOB_CREATE_CONFIGFIX = "alm/services/arch/alm/job-create-configuration-fix"
	static String ALM_JOB_CREATE_CONFIGLIBFIX = "alm/services/arch/alm/job-create-configlib-fix"
    static String ALM_JOB_RETRY_CI = "alm/services/arch/alm/job-retry-ci"
	static String ALM_JOB_REFRESH_BUS = "alm/services/arch/alm/job-call-refresh-bus"
	static String ALM_JOB_CREATE_CONFIGURATION_RELEASE = "alm/services/arch/alm/job-create-configuration-Release"
    static String ALM_JOB_VALIDATE_BBDD_SCRIPTS = "alm/services/arch/alm/bbdd/job-validate-bbdd-script"
	static String ALM_JOB_CLOSE_CAMPAIGN = "alm/services/arch/alm/job-close-campaign"
	static String ALM_JOB_NOTIFY_CLOSE_CAMPAIGN = "alm/services/arch/alm/job-notify-close-campaign"
    static String ALM_JOB_STRESS_TEST = "alm/services/arch/alm/job-prepare-stress-tests"
    static String ALM_JOB_SONAR = "alm/services/arch/alm/job-sonar-scan"
    static String ALM_JOB_ACTUATOR_ENV = "alm/services/arch/alm/job-call-actuator-env"
	
	static String ALM_JOB_REPORT_LIQUIBASE = "alm/services/arch/alm/bbdd/job-liquibase-report"
    static String ALM_JOB_CREATE_BBDD_RC = "alm/services/arch/alm/bbdd/job-create-BBDD-RC"
    static String ALM_JOB_DEPLOY_BBDD_RELEASE = "alm/services/arch/alm/bbdd/job-deploy-BBDD-release"

    static String ALM_JOB_START_STOP = "alm/services/arch/alm/go-utilities/job-start-stop-app/"
    static String ALM_JOB_RESTART = "alm/services/arch/alm/go-utilities/job-restart-pods"
    static String ALM_JOB_REFRESH_LITMID = "alm/services/arch/alm/go-utilities/job-litmid-literals-refresh"
    static String ALM_JOB_RESIZE = "alm/services/arch/alm/go-utilities/job-redimensionate-app"
    

    /**
     * CICS
     */
    static String ALM_JOB_CREATE_RC_CICS = "alm/services/arch/alm/cics/job-provisioning-CICS"
    static String ALM_JOB_CIERRE_RELEASE_CICS = "alm/services/arch/alm/cics/job-cierre-Release-CICS"


    /**
     * ADS
     */
    static String ALM_JOB_CREATE_RC_ADS = "alm/services/arch/alm/ads/job-provisioning-ADS"
    static String ALM_JOB_CIERRE_RELEASE_ADS = "alm/services/arch/alm/ads/job-cierre-Release-ADS"
	
	/**
	 * TANDEM
	 */
	static String ALM_JOB_PROVISIONING_TANDEM = "alm/services/arch/alm/tandem/job-provisioning-TANDEM"
	static String ALM_JOB_CIERRE_RELEASE_TANDEM = "alm/services/arch/alm/tandem/job-cierre-Release-TANDEM"
	
	/**
	 * CGS
	 */
	static String ALM_JOB_CREATE_RC_CGS = "alm/services/arch/alm/cgs/job-provisioning-CGS"
	static String ALM_JOB_CIERRE_RELEASE_CGS = "alm/services/arch/alm/cgs/job-cierre-Release-CGS"


    /**
     *
     */
    static String ENDPOINT_REFRESH = "actuator/refresh"
    static String ENDPOINT_INFO = "actuator/info"
    static String ENDPOINT_ENV = "actuator/env"
    static String ENDPOINT_THREADDUMP = "actuator/threaddump"
    static String PLATAFORMA_GPL = "alm"
    static String GPL_STATE_RUNNING = "running"

    static String INTERNAL_BUILDPACK = "git.svb.lacaixa.es/cbk/cf/alm/java-buildpack.git/#"
    static String INTERNAL_BUILDPACK_JAVA8 = GlobalVars.INTERNAL_BUILDPACK + "v4.8_Alm_1.0"
    static String INTERNAL_BUILDPACK_JAVA11 = GlobalVars.INTERNAL_BUILDPACK + "v4.20_Alm_1.2"

    /**
     * END GPL
     */

    static String domainTst = "tst.int.srv.project.com"
    static String domainPre = "pre.int.srv.project.com"
    static String domainPro = "pro.int.srv.project.com"

    static String nexusDownloadRepository = "http://eibcmasp03.lacaixa.es:8081/nexus/repository/npm-group/"

    static String bpGeneric = "staticfile_buildpack"
    static String bpApps = "staticfile_buildpack"
    static String bpStatic = "https://apiclasp-b3.svb.lacaixa.es:2443/buildpacks-cbk/staticfile-buildpack.git"
    static String containerAppName = "alm"

    static String GIT_REPO_APP_DATA_SERVICE = "/data-service/"
	static String GIT_REPO_APP_BFF_SERVICE = "/bff/"
    static String GIT_REPO_APP_SERVICE = "/service/"
    static String GIT_REPO_APP_LIBRARY = "/common/"
    static String GIT_REPO_ARCH = "alm/services/arch"
    static String GIT_REPO_DEFINITIONS = "alm/services/definitions"
    static String GIT_REPO_APP_CONF_LIBRARY = "/conf/"

    static String blueMixUrl = "https://api.intra1.lacaixa.bluemix.net"

    static String NEXUS_URL_BASE = "artifacts.cloud.project.com"
    static String NEXUS_URL = "https://$NEXUS_URL_BASE/"
    static String NEXUS_PUBLIC_REPO_NAME = "arq-openservices-maven-public"
    static String NEXUS_RELEASES_REPO_NAME = "arq-openservices-maven-releases"
    static String NEXUS_SNAPSHOTS_REPO_NAME = "arq-openservices-maven-snapshots"

    // Default values for deployments repos
	static String MVN_RELEASE_DEPLOYMENT_REPO = "maven-releases::default::https://artifacts.cloud.project.com/artifactory/arq-openservices-maven-releases"
	static String MVN_RELEASE_DEPLOYMENT_REPO_NAME = "maven-releases"
	static String MVN_RELEASE_DEPLOYMENT_REPO_URL = "https://artifacts.cloud.project.com/artifactory/arq-openservices-maven-releases"
    static String MVN_SNAPSHOT_DEPLOYMENT_REPO = "maven-snapshots::default::https://artifacts.cloud.project.com/artifactory/arq-openservices-maven-snapshots"
	static String MVN_SNAPSHOT_DEPLOYMENT_REPO_NAME = "maven-snapshots"
	static String MVN_SNAPSHOT_DEPLOYMENT_REPO_URL = "https://artifacts.cloud.project.com/artifactory/arq-openservices-maven-snapshots"

    // Default Maven Settings if from configfiles
    //static String MVN_DEFAULT_SETTINGS = "alm-maven-settings-with-singulares"
	static String MVN_DEFAULT_SETTINGS = "alm-maven-settings-sin-proxy-with-singulares"

    static String CONTRACT_MICRO_URL = "https://contractserver-micro-server-1.pro.int.srv.project.com"

    static String blueMixUrl_CD1_TST = "https://api.intra1.lacaixa.bluemix.net"
    static String blueMixUrl_CD2_TST = "https://api.intra2.lacaixa.bluemix.net"

    static String blueMixUrl_CD1_PRE = "https://api.intra1.lacaixa.bluemix.net"
    static String blueMixUrl_CD2_PRE = "https://api.intra2.lacaixa.bluemix.net"

    static String blueMixUrl_CD1_PRO = "https://api.intra1.lacaixa.bluemix.net"
    static String blueMixUrl_CD2_PRO = "https://api.intra2.lacaixa.bluemix.net"

    static String FEATURE_BRANCH = "feature"
    static String RELEASE_BRANCH = "release"
    static String MASTER_BRANCH = "master"
    static String HOTFIX_BRANCH = "hotfix"
    static String CONFIGFIX_BRANCH = "configfix"
    static String PROTOTYPE_BRANCH = "feature/prototype"
    static int PROTOTYPE_DAYS_LIVE = 30
	static int WIREMOCK_DAYS_LIVE = 1
	static int OCP_IMAGE_DAYS_LIVE = 180

    static String MERGE = "merge"

    static String EDEN_ENVIRONMENT = "eden"
    static String DEV_ENVIRONMENT = "dev"
    static String TST_ENVIRONMENT = "tst"
    static String PRE_ENVIRONMENT = "pre"
    static String PRO_ENVIRONMENT = "pro"

    static String BMX_CD1 = "1"
    static String BMX_CD2 = "2"

    static String BMX_TST_ORG_CD1 = "TST_ALM_CD1"
    static String BMX_PRE_ORG_CD1 = "PRE_ALM_CD1"
    static String BMX_PRO_ORG_CD1 = "PRO_ALM_CD1"

    static String BMX_TST_ORG_CD2 = "TST_ALM_CD2"
    static String BMX_PRE_ORG_CD2 = "PRE_ALM_CD2"
    static String BMX_PRO_ORG_CD2 = "PRO_ALM_CD2"

    static String BMX_DEV_SPACE = "SRV_TST"
    static String BMX_TST_SPACE = "SRV_TST"
    static String BMX_EDEN_SPACE = "SRV_TST"
    static String BMX_PRE_SPACE = "SRV_PRE"
    static String BMX_PRO_SPACE = "SRV_PRO"

    static String DEPLOY_TAG = "deploy"
    static String IGNORE_TAG = "ignore"
    static String EXECUTION_PROFILE_COMMIT_LOG_TAG = "executionProfile"
	static String STRESS_TEST_CLASSIFIER_TAG = "stressTest"
    static String LOG_LEVEL_ALL_TAG = "loglevelall"
    static String LOG_LEVEL_DEBUG_TAG = "logleveldebug"
    static String LOG_LEVEL_INFO_TAG = "loglevelinfo"
    static String DO_NOT_CI_TAG = "donotci"
	
    static String DOMAIN_INT_URL_TST = "tst.int.srv.project.com"
    static String DOMAIN_EXT_URL_TST = "tst.ext.srv.project.com"
    static String DOMAIN_INT_CENTER_URL_TST = "tst<center>.int.srv.project.com"

    static String DOMAIN_INT_URL_PRE = "pre.int.srv.project.com"
    static String DOMAIN_EXT_URL_PRE = "pre.ext.srv.project.com"
    static String DOMAIN_INT_CENTER_URL_PRE = "pre<center>.int.srv.project.com"

    static String DOMAIN_INT_URL_PRO = "pro.int.srv.project.com"
    static String DOMAIN_EXT_URL_PRO = "pro.ext.srv.project.com"
    static String DOMAIN_INT_CENTER_URL_PRO = "pro<center>.int.srv.project.com"

    static String FEATURE_NUM_SEPARATOR = "#"
    static String FEATURE_NUM_SEPARATOR1 = "/"

    static String FEATURE_DESC_SEPARATOR = "_"

    static String GIT_USER_NAME = "jenkins.pipeline.CI"
    static String GIT_USER_EMAIL = "jenkins.pipeline.CI@lacaixa.es"

    static String GIT_TAG_CR_PUSH = "<CreateRelease_Pipeline>:"
    static String GIT_TAG_CI_PUSH = "<CI_Pipeline>:"
	static String GIT_TAG_CONGIG_PUSH = "Config files"
    static String GIT_TAG_CI_PUSH_MESSAGE = "New Release"
    static String GIT_TAG_CI_PUSH_MESSAGE_RC = " New RC generated "
    static String GIT_TAG_CI_PUSH_MESSAGE_RELEASE = " New Release generated "
    static String GIT_TAG_CI_PUSH_MESSAGE_HOTFIX = " New HotFix generated "
    static String GIT_TAG_CI_PUSH_MESSAGE_CONFIGFIX = " New Configuration Fix generated "
    static String GIT_TAG_CI_PUSH_MESSAGE_ADS_PARAMETERS_NORMALIZED = "ADS XML parameters have been normalized"

    static String[] GIT_FILES_TO_BE_REMOVED = ["${GlobalVars.TMP_FILE_GENERATED_DATASOURCE}"]
    static String[] GIT_FILE_REMOVAL_REPO_EXCLUSION_LIST = ["config-repo", "config-sys"]

    //CF push's timeout (seconds).
    //The maximum push timeout 'maximum_health_check_timeout' is defined by the Cloud Foundry operator and can be defined to any value. By default is 180s (3min).
    static String CF_PUSH_TIMEOUT = "300"

    static int CF_PUSH_MAX_RETRIES = 2

    //CF login's timeout (seconds)
    static int CF_LOGIN_TIMEOUT = 45

    static int CF_LOGIN_MAX_RETRIES = 3

    static String GLOBAL_MVN_PARAMS = " --no-transfer-progress --update-snapshots "
	static String gitlabDomainNonHttps = "git.svb.lacaixa.es"
    static String gitlabDomain = "https://git.svb.lacaixa.es/"
    static String gitlabApiDomain = "https://git.svb.lacaixa.es/api/v4/projects/"
    static String gitlabApiDomainRaw = "https://git.svb.lacaixa.es/api/v4/"
    static String proxyCaixaHost = "proxyserv.svb.lacaixa.es"
    static String proxyCaixaPort = "8080"
    static String proxyCaixa = "http://${GlobalVars.proxyCaixaHost}:${GlobalVars.proxyCaixaPort}"

    //List separated by commas
    static String HTTP_ADDITIONAL_NON_PROXY_HOSTS = "pro.k8i-singulars.intranet.cloud.lacaixa.es,pre.k8i-singulars.intranet.cloud.lacaixa.es,tst.k8i-singulars.intranet.cloud.lacaixa.es,api.tst.internal.cer.project.com,api.pre.internal.cer.project.com,api.pro.internal.cer.project.com,apigwi.pre.serveis.almcloud.lacaixa.es,apigwi.pro.serveis.almcloud.lacaixa.es"

    static String URL_ZIP_INITIALIZR_DEV = "https://k8sgateway.dev.int.srv.project.com/arch-service/alminitializr-micro-1/alm/starter.zip"
	static String URL_ZIP_INITIALIZR_PRO = "https://alminitializr-micro-1.pro.int.srv.project.com/alm/starter.zip"
	
	static String INITITALIZR_DEFAULT_BOOT_VERSION = "2.3.4.RELEASE"

    static String K8S_URL = "https://k8sgateway.{environment}.int.srv.project.com"
    static String K8S_CENTER_URL = "https://k8sgateway.{environment}.cloud-{datacenter}.alm.cloud.lacaixa.es"


    static String urlZipInitializrTST = "https://k8sgateway.tst.int.srv.project.com/arch-service/alminitializr-micro-1/alm/starter.zip"
    static String initializrDefaultBootVersion = "2.3.4.RELEASE"

    static String EDEN_APPS_REGEX = /.*\-[[:digit:]]{1,}\-[[:graph:]]{1,16}\-[[:digit:]]{8}/
    static String DEV_APPS_REGEX = /.*\-[[:digit:]]{1,}\-dev\-[[:digit:]]{8}/

    static String ALL_SAMPLE_APPS_REGEX = /^.*\-sample\-app\-\d+(\-dev)*\-\d{8}[^\.]/
    static String EDEN_SAMPLE_APPS_REGEX = /^.*\-sample\-app\-\d+\-\s+\-\d{8}[^\.]/
    static String DEV_SAMPLE_APPS_REGEX = /^.*\-sample\-app\-\d+\-dev[^\.]/
    static String TST_SAMPLE_APPS_REGEX = /^.*\-sample\-app\-\d+\s[^\.]/
    static String PRE_SAMPLE_APPS_REGEX = /^.*\-sample\-app\-\d+\-\d{8}[^\.]/

	static String VOLATILE_APPS_REGEX = /^.*\-\d+\-\d{8}[^\.]/
	
    static String APP_STARTED_REGEX = /\d{8}$/

    static String REVAPI_POM_FILENAME = "revapiPom.xml"

    static String SYNTHETIC_TEST_POM_FILENAME = "syntheticTestPom.xml"

    static String SWAGGER_CONTRACT_FILENAME = "swagger-micro-contract.yaml"

    static String CONTRACT_PACKAGE_PROP = "contract.package"

    static String CONTRACT_VERSION_PROP = "contract.version"
	
	static String GIT_DEMO_PROTOTYPE_REPO_URL = "https://git.svb.lacaixa.es/cbk/alm/services/arch/cloud/demo-prototype.git"
	
    static String GIT_CONFIG_REPO_URL_TST = "https://git.svb.lacaixa.es/cbk/alm/config/repository-tst.git"
    static String GIT_CONFIG_REPO_URL_PRE = "https://git.svb.lacaixa.es/cbk/alm/config/repository-pre.git"
    static String GIT_CONFIG_REPO_URL_PRO = "https://git.svb.lacaixa.es/cbk/alm/config/repository-pro.git"

    static String GIT_CERTS_REPO_URL_TST = "https://git.svb.lacaixa.es/cbk/alm/config/certs-tst.git"
    static String GIT_CERTS_REPO_URL_PRE = "https://git.svb.lacaixa.es/cbk/alm/config/certs-pre.git"
    static String GIT_CERTS_REPO_URL_PRO = "https://git.svb.lacaixa.es/cbk/alm/config/certs-pro.git"

    static String GIT_CONFIG_REPO_TTSS_URL_TST = "https://git.svb.lacaixa.es/cbk/alm/services/config-sys/repository-tst.git"
    static String GIT_CONFIG_REPO_TTSS_URL_PRE = "https://git.svb.lacaixa.es/cbk/alm/services/config-sys/repository-pre.git"
    static String GIT_CONFIG_REPO_TTSS_URL_PRO = "https://git.svb.lacaixa.es/cbk/alm/services/config-sys/repository-pro.git"
	
	static String CONFIG_SERVER_URL = "https://k8sgateway.{environment}.cloud-{datacenter}.alm.cloud.lacaixa.es/arch-service/config-server-1"
	
    static int INCREMENT_CANNARY_PERCENTATGE = 25
    static int MAXIMUM_PERCENTATGE = 100


    static String SNAPSHOT_QUALIFIER = "SNAPSHOT"
    static String RC_QUALIFIER = "RC"

    static String DISABLED_POLICY_BY_BRANCH = "BY_BRANCH"
    static String DISABLED_POLICY_BY_COMMIT = "BY_COMMIT"
    static String DISABLED_POLICY_BY_VERSION = "BY_VERSION"
    static String DISABLED_POLICY_NONE = "NONE"
	
    static String NEW_COMPONENT_PREFIX = 'new-<componentName>'
    static String BETA_COMPONENT_SUFFIX = '<componentName>-beta'
    static String NEW_COMPONENT_PREFIX_Cloud = '<componentName>-new'

    static String DEFAULT_MEMORY = '768M'
    static String DEFAULT_JAVA_OPTS = '-Dspring.cloud.config.failFast=true'

    static int HTTP_REQUEST_RETRY_LOOP_TIMEOUT = 5
    static int HTTP_REQUEST_SINGLE_CALL_TIMEOUT = 30
    static int HTTP_REQUEST_MAX_RETRIES = 5
    static String DEFAULT_RETRY_DELETE_APP_POLICY = 2
	
	static int DEFAULT_ALM_MS_REQUEST_RETRIES_TIMEOUT = 5
	static String ALM_MS_VALID_RESPONSE_STATUS = "100:599"
	static int ALM_MS_TIMEOUT = 100

    static int GITLAB_API_REQUEST_RETRIES_TIMEOUT = 5
    static int GITLAB_API_REQUEST_MAX_RETRIES_DUE_TO_EXCEPTION = 3

    static int MAVEN_INTEGRATION_TEST_RETRIES_TIMEOUT = 30
    static int MAVEN_INTEGRATION_TEST_MAX_RETRIES = 10
    static int MAVEN_GOAL_EXECUTION_MAX_RETRIES = 5

    static int LIQUIBASE_STATUS_RETRY_CYCLE_TIMEOUT = 8
    static int ACTUATOR_HEALTH_RETRY_CYCLE_TIMEOUT = 10
    static int ACTUATOR_HEALTH_RETRY_CYCLE_MAX_RETRIES = 5
    static int ACTUATOR_HEALTH_TIMEOUT = 60
    static int ACTUATOR_REFRESH_TIMEOUT = 300
	static int ACTUATOR_REFRESH_RETRY_LOOP_TIMEOUT = 900
    static int ACTUATOR_ENV_MAX_RETRIES = 5
    static int PRO_ENDPOINTS_CHECK_TIMEOUT = 45
    static int PRO_ENDPOINTS_CHECK_MAX_RETRIES = 10

    static int GIT_COMMAND_MAX_RETRIES_DUE_TO_EXCEPTION = 5

    public static String getArchVersionForTheEnvironment(String environment) {
        if (environment == GlobalVars.PRE_ENVIRONMENT) return MINIMUM_VERSION_ARCH_PRE
        else if (environment == GlobalVars.TST_ENVIRONMENT) return MINIMUM_VERSION_ARCH_TST
        else if (environment == GlobalVars.DEV_ENVIRONMENT) return MINIMUM_VERSION_ARCH_DEV
        else if (environment == GlobalVars.PRO_ENVIRONMENT) return MINIMUM_VERSION_ARCH_PRO
        else if (environment == GlobalVars.EDEN_ENVIRONMENT) return MINIMUM_VERSION_ARCH_DEV
        else return MINIMUM_VERSION_ARCH_DEV
    }

    /**
     * GPL
     */
    static String URL_GPL = "https://idegpl.pro.almcloud.lacaixa.es"
    static String URL_GPL_PRE = "https://idegpl.pre.almcloud.lacaixa.es"

    static String PATH_GPL_PIPELINE = "/api/v1/pipeline"
    static String PATH_GPL_STAGE = "/api/v1/stage"
    static String PATH_AUTH_FEATURE_AGILE_WORK = "/api/v1/authFeature"

    static boolean SEND_TO_GPL = true

	/**
	 * GAR
	 */
	static String URL_GAR = "https://eideswasp.svb.lacaixa.es/apw61/idegar"
	static String URL_GAR_PRE = "https://eideswasp.svb.lacaixa.es/apw61/idegar"
	static String PATH_GAR_RESPONSIBLES = "/api/aplicacion/v1/responsiblesFromApp/00001" //FIXME 0001 means only caixa?
	static String PATH_GAR_APP = "/api/aplicacion/v1/info/00001"
    static String PATH_GAR_USUARIOS = "/api/recurso/v1"

    /**
     * GSA
     */
    static String CATALOG_URL = "https://idegsa.pro.almcloud.lacaixa.es"
    static String CATALOG_URL_PRE = "https://idegsa.pre.almcloud.lacaixa.es"
    static String PATH_CATALOG_PIPELINE = "/api/v1/alm/insertComponenteALM"
	static String GSA_ACTUAL_INSTALLATION_PATH = "/api/v1/alm/actualInstallationALM/{garType}/{application}/{component}"
	

    static boolean PUSH_CATALOG_PRE = false
    static boolean PUSH_CATALOG_PRO = false

    static boolean SEND_TO_CATALOG = true

    static String JOB_DISPLAY_CONFLUENCE = "https://confluence.cloud.lacaixa.es/confluence/display/serArqMcrsvcs/ALM+-+Log+tracing"

    /**
     * Dataservices pipelines
     */
    static String TMP_FILE_GENERATED_DATASOURCE = "datasource_generated.tmp"

	/**
	 *ERROR_MANAGEMENT
	 *//*
	static String URL_ERRORMGNT_DEV = "https://k8sgateway.dev.cloud-1.alm.cloud.lacaixa.es/arch-service/errormanagement-micro-server-1"
	static String URL_ERRORMGNT_TST = "https://k8sgateway.dev.cloud-1.alm.cloud.lacaixa.es/arch-service/errormanagement-micro-server-1"
	static String URL_ERRORMGNT_PRE = "https://k8sgateway.dev.cloud-1.alm.cloud.lacaixa.es/arch-service/errormanagement-micro-server-1"
	static String URL_ERRORMGNT_PRO = "https://k8sgateway.dev.cloud-1.alm.cloud.lacaixa.es/arch-service/errormanagement-micro-server-1"
	*/
	//DEV debe apuntar a TST	
	static String URL_ERRORMGNT_DEV = "https://api.tst.internal.project.com/apps/almarq/error-management"
	static String URL_ERRORMGNT_TST = "https://api.tst.internal.project.com/apps/almarq/error-management"
	static String URL_ERRORMGNT_PRE = "https://api.pre.internal.project.com/apps/almarq/error-management"
	static String URL_ERRORMGNT_PRO = "https://api.pro.internal.project.com/apps/almarq/error-management"
	/*
	static String URL_CATALOGO_ALM_TST = "https://k8sgateway.dev.cloud-1.alm.cloud.lacaixa.es/arch-service/catmsv-micro-server-1"
	static String URL_CATALOGO_ALM_PRE = "https://k8sgateway.dev.cloud-1.alm.cloud.lacaixa.es/arch-service/catmsv-micro-server-1"
	static String URL_CATALOGO_ALM_PRO = "https://k8sgateway.dev.cloud-1.alm.cloud.lacaixa.es/arch-service/catmsv-micro-server-1"
    */
	static String CATALOGO_ALM_ENV = "PRO"
	//static String URL_CATALOGO_ALM_TST = "https://api.tst.internal.project.com/dev/tech/catmsv/1"
	static String URL_CATALOGO_ALM_TST = "https://catmsv-micro-server-1.tst.int.srv.project.com"
	static String URL_CATALOGO_ALM_PRE = "https://api.pre.internal.project.com/tech/catmsv/1"
	//static String URL_CATALOGO_ALM_PRO = "https://api.pro.internal.project.com/tech/catmsv/1"
	//static String URL_CATALOGO_ALM_PRO = "https://catmsv-micro-server-1.pro.int.srv.project.com"
	static String URL_CATALOGO_ALM_PRO = "https://catmsv-micro-server-1.pro.int.srv.project.com"  
	static String URL_API_TRANSLATIONS = "/translations"
	static String URL_API_UNMMAPPED_TRANSLATIONS = "/unmapped-translations"
	static String TEMP_FILE_UNMAPPED_TRANSLATIONS = "unmapped-translations.json"

    /**
     * Envelope MuleSoft Contracts
     */
	static String URL_ARCH_API_MANAGEMENT_ALM = "https://api.pro.internal.project.com/tech/ArchApiManagementLifecycle/1"

	/**
	 * LIQUIBASE
	 */
	static String LIQUIBASE_INTERNAL_ERROR = "Error interno, comunique el problema via foro al equipo de OpenServices"
	static String LIQUIBASE_ORACLE_ERROR = "El error ocurrido es el siguiente, puede ser la sentencia o problemas de configuracion \n"
	static String LIQUIBASE_NOT_FINISHED_ERROR = "El proceso no ha terminado en el tiempo establecido "
	static String LIQUIBASE_MESSAGE_UPDATE_OK = "El proceso ha terminado correctamente "
	static String LIQUIBASE_MESSAGE_HISTORY = "El conjunto de changeSets ejecutado es el siguiente "
	static String LIQUIBASE_MESSAGE_EXPORT_BBDD = "Puede consultar el modelo de datos en la siguiente url "

    /**
     * Cloud VARIABLES
     */
    static String Cloud_APP_ARCH = "AB3COR"
    static String Cloud_APP_APPS = "AB3APP"

    static String Cloud_APP_ID_ARCH = "1765"
    static String Cloud_APP_ID_APPS = "1766"


    static String Cloud_PRE = "https://publisher-ssp-cldalm.pre.ap.intranet.cloud.lacaixa.es"
    static String Cloud_PRO = "https://publisher-ssp-cldalm.pro.ap.intranet.cloud.lacaixa.es"

    static String Cloud_ERROR_DEPLOY_NO_INSTANCE_AVAILABLE = "El micro no esta correctamente configurado, pueden faltar secrets. Abrid foro indicando problema de configuracion del micro Cloud_DEPLOY_CONFIGURATION_ERROR Secret: "
	static String Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING = "The application is not starting. Check Kibana's logs to find the reason...\n(Doc: https://confluence.cloud.lacaixa.es/confluence/display/serArqMcrsvcs/Kibana+-+Log+tracing#Kibana-Logtracing-Consultalogsdearranque)"
	static String Cloud_ERROR_DEPLOY_ON_MITIGATED_CENTER = "No se puede acceder al micro mediante la VIPA de un centro mitigado. (https://confluence.cloud.lacaixa.es/confluence/display/serArqMcrsvcs/Kibana+-+Log+tracing#Kibana-Logtracing-Consultalogsdearranque)  para asegurar que el problema no sea aplicativo antes de abrir un foro"
    static String Cloud_ERROR_DEPLOY_KUBERNETES_DISABLED = " deshabilitado en Cloud debido a una intervención programada del equipo de Cloud. Intente la operación más tarde. En caso de considerarlo un incidente (p.e. perder una ventana de despliegue en PRO), puede abrir maximo contra Servicio TI: APLICACION \\ NO PROCEDE \\ CLDALM.PCLD Grupo resolutor: C-CXB-ES-ET-OTCAAS indicando el problema, recordando indicar la hora del error."

    static int Cloud_SECRET_VERIFICATION_MAX_RETRIES = 5
    static int Cloud_RUN_ACTUATOR_REFRESH_MAX_RETRIES = 5

    static int Cloud_API_REQUEST_MAX_RETRIES = 10
    static int Cloud_API_MAX_TIMEOUT_PER_REQUEST = 90

	/**
	 * CHECKMARX VARIABLES
	 */
	static String URL_CHECKMARX_SERVER = "https://checkmarx.lacaixa.es/CxWebClient"
	static String JENKINS_CREDENTIALS_ID_CHECKMARX = "almmscheckmarx"
	static String GROUP_TEAM_ID_CHECKMARX = "563e3977-6832-451d-bcae-e31f33171c3f"
	static String TEAM_PATH_CHECKMARX = "CxServer\\Ackcent\\Caixabank\\ALMSRV"
	static String ALM_PRESET_CHECKMARX = "100026" // ALM preset in Checkmarx
	static String FROM_SECURITY_EMAIL_CHECKMARX = "noreply@alm.project.com"
	static String ENV_CHECKMARX="PRO"

    /**
     * MAXIMOS AUTOMATICOS
     */
    static String EMAIL_CREACION_MAXIMOS = "incidencias.maximo@project.com"
	
    /**
     * KPI VARIABLES
     */   
    static String URL_KPI = "https://internal-reca3i-reca3i.pro.intranet.cloud.lacaixa.es/api/elk/logs"
    //static String URL_ALMMETRICS = "https://k8sgateway.pre.cloud-1.alm.cloud.lacaixa.es/arch-service/almlogcollector-micro-1/api/elk/logs"
	static String URL_ALMMETRICS = "https://almlogcollector.pro.int.srv.project.com/api/elk/logs"
    static String ALMENV_ALMMETRICS = "pro"
	//static String ALMENV_ALMMETRICS = "dev"
    static String SONAR_URL = null
    static int DEFAULT_KPI_REQUEST_RETRIES_TIMEOUT = 10
    static int KPI_RETRY_POLICY = 0
    static int KPI_TIMEOUT = 120
    static String KPI_VALID_RESPONSE_STATUS = "100:405"
    
    static int DEFAULT_SONAR_REQUEST_RETRIES_TIMEOUT = 5
    static int SONAR_RETRY_POLICY = 4
    static int SONAR_TIMEOUT = 120
    static Boolean IS_KPI_ENABLED = false
    static Boolean ALM_SERVICES_SEND_ALM_METRICS = false

    // Semaphores
    static Boolean KPILOGGER_IS_NOW_ACTIVE = false

    /**
     * PROVISIONING EMAIL VARIABLES
     */
    static String EMAIL_REPOSITORY_ID = "25424"
    static String GIT_API_REPO_FILE_ENDPOINT = "/repository/files/"
    static String GIT_API_REPO_FILE_RAW = "/raw"
    static String EMAIL_RELATIVE_URL_WELCOME = "OpenService/welcome"
    static String EMAIL_LOGO_RELATIVE_URL = "OpenService/assets/logoOpenNow.png"
    static int HTTP_STD_TIMEOUT = 120  

	static  def ALLOWED_SONAR_TYPES = [ ArtifactSubType.SAMPLE_APP, ArtifactSubType.MICRO_ARCH, ArtifactSubType.MICRO_APP, ArtifactSubType.ARCH_LIB_WITH_SAMPLEAPP, ArtifactSubType.ARCH_LIB, ArtifactSubType.APP_LIB, ArtifactSubType.STARTER ,ArtifactSubType.PLUGIN, ArtifactSubType.PLUGIN_STARTER ,ArtifactSubType.PLUGIN_STARTER_SAMPLE_APP, ArtifactSubType.ARCH_CFG, ArtifactSubType.SRV_CFG ]
	
	static def LIMIT_LENGTH_FOR_PODNAME_WITHOUT_K8_SUFFIX = 34
	static def LIMIT_LENGTH_FOR_SERVICE_WITHOUT_K8_SUFFIX = 50
	
	static String ALM_SERVICES_SKIP_VALIDATION_CLOSE_RELEASE_LIST = ""
	static String ALM_SERVICES_SKIP_VALIDATION_DEPENDENCIES_LIST = ""
	static String ALM_SERVICES_SKIP_VALIDATION_AUTH_LIST = ""
	static String ALM_SERVICES_SKIP_VALIDATION_CONTRACT_LIST = ""
	static String ALM_SERVICES_SKIP_VALIDATION_SECRETS_LIST = ""
	
	static String ALM_SERVICES_SKIP_VALIDATION_CLOSE_RELEASE_ALL = false
	static String ALM_SERVICES_SKIP_VALIDATION_DEPENDENCIES_ALL = false
	static String ALM_SERVICES_SKIP_VALIDATION_AUTH_ALL = false
	static String ALM_SERVICES_SKIP_VALIDATION_CONTRACT_ALL = false
	static String ALM_SERVICES_SKIP_VALIDATION_SECRETS_ALL = false
	
	static String Cloud_CUSTOM_LIVENESSPROBE_APPLICATIONS = ""
	static String REVAPI_SKIP_VALIDATION = "Bajo responsabilidad del equipo de desarrollo no se ejecutará la validación de versión. Esta versión rompe contrato con su major vigente"
	
	static String RESOURCE_PATH = "src/main/resources"
	static String TENANT_WILDCARD = "ALL"

    /**
     * FEATURE BRANCH NAME VALIDATIONS
     */
     static String LAST_CHARACTER_IS_NOT_ALPHANUMERIC_REGEX = /^.+[^a-zA-Z0-9]$/
     static String ONE_CHARACTER_IS_NOT_ALPHANUMERIC_REGEX = /[^a-zA-Z0-9]$/
     static String DEFAULT_FEATURE_BRACH_NAME = "feature"
	 
	 static int STRESS_RETRY_CYCLE_TIMEOUT = 120
	 static int DEFAULT_HTTP_TIMEOUT = 60
	 
	 /**
	  * LIMIT URL DEFINITION
	  */
	 static String LITMID_URL
	 static int LITMID_API_MAX_TIMEOUT_PER_REQUEST = 90  
	 static int LITMID_API_REQUEST_MAX_RETRIES = 10

    /**
     * LOGGING
     */
    static String CONSOLE_LOGGER_LEVEL = "INFO"
	
	static String PIPELINE_LOGS = ""
	static String STAGE_LOGS = ""
	static int DEVELOPER_PORTAL_LOG_MAX_SIZE = 33500

    /**
     * LOGGING FOR MAVEN TEST
     */
    static String MVN_TEST_LOGGERS_LEVEL = "-Dspring.main.banner-mode=off"

}
