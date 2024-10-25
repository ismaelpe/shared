import com.project.alm.EchoLevel
import com.project.alm.GlobalVars


/**
 * Script que permite inicializar las versiones de arquitectura en variables locales. 
 * Estas son leidas de la configuracion de jenkins
 * @author u0180790*
 */

def call(def pipelineParams = [:]) {
	
	if (env.ABSIS3_SERVICES_SKIP_VALIDATION_CLOSE_RELEASE_LIST != null) GlobalVars.ABSIS3_SERVICES_SKIP_VALIDATION_CLOSE_RELEASE_LIST = env.ABSIS3_SERVICES_SKIP_VALIDATION_CLOSE_RELEASE_LIST
	if (env.ABSIS3_SERVICES_SKIP_VALIDATION_DEPENDENCIES_LIST != null) GlobalVars.ABSIS3_SERVICES_SKIP_VALIDATION_DEPENDENCIES_LIST = env.ABSIS3_SERVICES_SKIP_VALIDATION_DEPENDENCIES_LIST
	if (env.ABSIS3_SERVICES_SKIP_VALIDATION_AUTH_LIST != null) GlobalVars.ABSIS3_SERVICES_SKIP_VALIDATION_AUTH_LIST = env.ABSIS3_SERVICES_SKIP_VALIDATION_AUTH_LIST
	if (env.ABSIS3_SERVICES_SKIP_VALIDATION_CONTRACT_LIST != null) GlobalVars.ABSIS3_SERVICES_SKIP_VALIDATION_CONTRACT_LIST = env.ABSIS3_SERVICES_SKIP_VALIDATION_CONTRACT_LIST
	if (env.ABSIS3_SERVICES_SKIP_VALIDATION_SECRETS_LIST != null) GlobalVars.ABSIS3_SERVICES_SKIP_VALIDATION_SECRETS_LIST = env.ABSIS3_SERVICES_SKIP_VALIDATION_SECRETS_LIST
	if (env.ABSIS3_SERVICES_SKIP_VALIDATION_CLOSE_RELEASE_ALL != null) GlobalVars.ABSIS3_SERVICES_SKIP_VALIDATION_CLOSE_RELEASE_ALL = env.ABSIS3_SERVICES_SKIP_VALIDATION_CLOSE_RELEASE_ALL
	if (env.ABSIS3_SERVICES_SKIP_VALIDATION_DEPENDENCIES_ALL != null) GlobalVars.ABSIS3_SERVICES_SKIP_VALIDATION_DEPENDENCIES_ALL = env.ABSIS3_SERVICES_SKIP_VALIDATION_DEPENDENCIES_ALL
	if (env.ABSIS3_SERVICES_SKIP_VALIDATION_AUTH_ALL != null) GlobalVars.ABSIS3_SERVICES_SKIP_VALIDATION_AUTH_ALL = env.ABSIS3_SERVICES_SKIP_VALIDATION_AUTH_ALL
	if (env.ABSIS3_SERVICES_SKIP_VALIDATION_CONTRACT_ALL != null) GlobalVars.ABSIS3_SERVICES_SKIP_VALIDATION_CONTRACT_ALL = env.ABSIS3_SERVICES_SKIP_VALIDATION_CONTRACT_ALL
	if (env.ABSIS3_SERVICES_SKIP_VALIDATION_SECRETS_ALL != null) GlobalVars.ABSIS3_SERVICES_SKIP_VALIDATION_SECRETS_ALL = env.ABSIS3_SERVICES_SKIP_VALIDATION_SECRETS_ALL
	
	if(env.ICP_CUSTOM_LIVENESSPROBE_APPLICATIONS != null)GlobalVars.ICP_CUSTOM_LIVENESSPROBE_APPLICATIONS = env.ICP_CUSTOM_LIVENESSPROBE_APPLICATIONS
	
    if (env.ABSIS3_SRV_MINIMUM_VERSION_ARCH_DEV != null) GlobalVars.MINIMUM_VERSION_ARCH_DEV = env.ABSIS3_SRV_MINIMUM_VERSION_ARCH_DEV
    if (env.ABSIS3_SRV_MINIMUM_VERSION_ARCH_TST != null) GlobalVars.MINIMUM_VERSION_ARCH_TST = env.ABSIS3_SRV_MINIMUM_VERSION_ARCH_TST
    if (env.ABSIS3_SRV_MINIMUM_VERSION_ARCH_PRE != null) GlobalVars.MINIMUM_VERSION_ARCH_PRE = env.ABSIS3_SRV_MINIMUM_VERSION_ARCH_PRE
    if (env.ABSIS3_SRV_MINIMUM_VERSION_ARCH_PRO != null) GlobalVars.MINIMUM_VERSION_ARCH_PRO = env.ABSIS3_SRV_MINIMUM_VERSION_ARCH_PRO
    if (env.ABSIS3_SKIP_MINIMUM_VERSION_VALIDATION_LIST != null) GlobalVars.ABSIS3_SKIP_MINIMUM_VERSION_VALIDATION_LIST = env.ABSIS3_SKIP_MINIMUM_VERSION_VALIDATION_LIST

    if (env.ABSIS3_SERVICES_DEPENDENCY_WHITELIST_BFF) GlobalVars.ABSIS3_SERVICES_DEPENDENCY_WHITELIST_BFF = env.ABSIS3_SERVICES_DEPENDENCY_WHITELIST_BFF
    if (env.ABSIS3_SERVICES_DEPENDENCY_WHITELIST_DATASERVICE) GlobalVars.ABSIS3_SERVICES_DEPENDENCY_WHITELIST_DATASERVICE = env.ABSIS3_SERVICES_DEPENDENCY_WHITELIST_DATASERVICE

    if (env.ABSIS3_SERVICES_EXECUTE_IT_PRO) GlobalVars.ABSIS3_SERVICES_EXECUTE_IT_PRO = env.ABSIS3_SERVICES_EXECUTE_IT_PRO
    if (env.ABSIS3_SERVICES_IT_TEST_JARS_GENERATION_WHITELIST) GlobalVars.ABSIS3_SERVICES_IT_TEST_JARS_GENERATION_WHITELIST = env.ABSIS3_SERVICES_IT_TEST_JARS_GENERATION_WHITELIST
    if (env.ABSIS3_SERVICES_SIMPLIFIED_ALM_WHITELIST) GlobalVars.ABSIS3_SERVICES_SIMPLIFIED_ALM_WHITELIST = env.ABSIS3_SERVICES_SIMPLIFIED_ALM_WHITELIST

    if (env.IS_KPI_ENABLED) GlobalVars.IS_KPI_ENABLED = ((String)env.IS_KPI_ENABLED).toBoolean()
    if (env.ABSIS3_SERVICES_SEND_ALM_METRICS) GlobalVars.ABSIS3_SERVICES_SEND_ALM_METRICS = ((String)env.ABSIS3_SERVICES_SEND_ALM_METRICS).toBoolean()

    
 	if (env.MVN_RELEASE_DEPLOYMENT_REPO != null) GlobalVars.MVN_RELEASE_DEPLOYMENT_REPO = env.MVN_RELEASE_DEPLOYMENT_REPO
    if (env.MVN_SNAPSHOT_DEPLOYMENT_REPO != null) GlobalVars.MVN_SNAPSHOT_DEPLOYMENT_REPO = env.MVN_SNAPSHOT_DEPLOYMENT_REPO
	
	// Stress timeout
	if (env.STRESS_RETRY_CYCLE_TIMEOUT != null) GlobalVars.STRESS_RETRY_CYCLE_TIMEOUT = env.STRESS_RETRY_CYCLE_TIMEOUT
	
	// LITMIT URL 
	if (env.LITMID_URL != null) GlobalVars.LITMID_URL = env.LITMID_URL

    if (env.MVN_TEST_LOGGERS_LEVEL != null) GlobalVars.MVN_TEST_LOGGERS_LEVEL = env.MVN_TEST_LOGGERS_LEVEL

    // Logging
    if (pipelineParams?.loggerLevel != null) {
        GlobalVars.CONSOLE_LOGGER_LEVEL = pipelineParams?.loggerLevel
        printOpen("The logger level for this pipeline has been set to ${GlobalVars.CONSOLE_LOGGER_LEVEL} as per Jenkinsfile configuration or GPL-sourced job parameter injection", EchoLevel.INFO)
    } else if (env.CONSOLE_LOGGER_LEVEL != null) {
        GlobalVars.CONSOLE_LOGGER_LEVEL = env.CONSOLE_LOGGER_LEVEL
        printOpen("The logger level for this pipeline has been set to ${GlobalVars.CONSOLE_LOGGER_LEVEL} as per Jenkins environment variable", EchoLevel.INFO)
    }

    /*
     * Obtiene de la Configuración Global de Sonar la url.
     * Esta config siempre debe existir bajo la instalación 'sonarqube', en caso contrario se obtendrá un error
     * y cortara la ejecución
     */
    GlobalVars.SONAR_URL = jenkins.model.GlobalConfiguration.all()
                .getInstance(hudson.plugins.sonar.SonarGlobalConfiguration.class)
                .getInstallations()
                .find { it.name == 'sonarqube' }
                .getServerUrl()
    
    if (env.API_MANAGEMENT_ALM_URL != null) GlobalVars.URL_ARCH_API_MANAGEMENT_ALM = env.API_MANAGEMENT_ALM_URL

}
