import com.caixabank.absis3.*
import groovy.json.JsonSlurperClassic

def call(PipelineData pipelineData, PomXmlStructure pomXml, Environment environment = Environment.PRO, Gateway gateway = Gateway.EXTERNAL) {

	try {

        ApiManagerTechnicalServicesRequest request = ApiManagerTechnicalServicesRequest.fromPipelineAndPomData(pipelineData, pomXml)
        request.apiManagerUri = resolveApiManagerUri(environment, gateway)
        request.absis3TokenName = resolveApiManagerTokenName(environment)

        ApiManagerTechnicalServicesResponse response = sendRequestToApiManager(request)


	} catch (Exception e) {

        printOpen("Ha ocurrido un error al hacer el registro TechnicalServices a API Manager\n\n${e}", EchoLevel.ERROR)

		if (shouldWeAbortThePipe()) {
            printOpen("Abortaremos la pipeline por error al hacer el registro TechnicalServices a API Manager", EchoLevel.ERROR)
			throw e
		}
        printOpen("Ignoramos el error y seguimos adelante", EchoLevel.INFO)
	}

}

def shouldWeAbortThePipe() {

	boolean abortThePipe = Utilities.getBooleanPropertyOrDefault(env.ABSIS3_SERVICES_APIMANAGER_TECHNICALSERVICES_REGISTRATION_IF_ERROR_THEN_PIPELINE_FAILS, true)
    printOpen("Looking if we have to abort the pipe: ${abortThePipe}", EchoLevel.INFO)

	return abortThePipe
}

def resolveApiManagerUri(Environment env, Gateway gateway) {

    env = env == null ? Environment.PRO : env
    gateway = gateway == null ? Gateway.EXTERNAL : gateway

    if (Environment.PRO == env && Gateway.EXTERNAL == gateway) {

        return "https://api.pro.internal.caixabank.com/tech/apimanager/technicalServices"

    } else if (Environment.PRE == env && Gateway.INTERNAL == gateway) {

        return "https://k8sgateway.pre.icp-1.absis.cloud.lacaixa.es/adpbdd-micro-4/portal/technicalServices"

    } else {

        throw new Exception("publishSwaggerContract2ApiManager.resolveApiManagerUri: " +
            "We don't have URI for Environment ${env} and Gateway ${gateway}")

    }

}

def resolveApiManagerTokenName(Environment environment) {

    environment = environment == null ? Environment.PRO : environment

    def tokenEnv = Environment.EDEN == environment ? Environment.DEV.toString() : environment.toString()
    def absis3TokenName = "ABSIS3_TOKEN_${tokenEnv}_V2"

    return absis3TokenName

}
