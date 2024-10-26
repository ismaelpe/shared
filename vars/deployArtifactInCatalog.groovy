import com.project.alm.*
import com.project.alm.GlobalVars
import groovy.json.JsonSlurperClassic
import java.util.List
import java.util.ArrayList


def call(def body, PipelineData pipelineData, PomXmlStructure pomXml, CloudStateUtility cloudStateUtilitity, String environment) {

	
	def response = null
	if (env.SEND_TO_ALM_CATALOG!="" && env.SEND_TO_ALM_CATALOG=="true") {
		
		
		def bodyNew = null
		String environmetNew = environment
		if (pipelineData!=null && pipelineData.deployStructure!=null) {
			environmetNew = pipelineData.deployStructure.envCloud
		}

		if (body==null) {
			
			def typeVersion = ""
			
			if (pomXml.artifactVersionQualifier == null || pomXml.artifactVersionQualifier == "") {
				typeVersion = "RELEASE"
			} else if (pomXml.isSNAPSHOT()) {
				typeVersion = "SNAPSHOT"
			} else if (pomXml.isRCVersion()) {
				typeVersion = "RC"
			} else {
				typeVersion = "UNKNOWN"
			}
			
			
			bodyNew = [
					type: pipelineData.getGarArtifactType().getGarName(),
					aplicacion: pomXml.getApp(pipelineData.garArtifactType),
					major: pomXml.artifactMajorVersion,
					minor: pomXml.artifactMinorVersion,
					fix: pomXml.artifactFixVersion,
					typeVersion: typeVersion
				]
		}else {
			bodyNew = body
		}
		
				
		try {
			printOpen("Sending deployment info to Open's catalogue", EchoLevel.INFO)
			def deployParams=null
			if (pipelineData.deployStructure!=null && cloudStateUtilitity!=null) {
				
				
				deployParams = [
					replicas: cloudStateUtilitity.cloudResources.getNumInstances(environmetNew),
					memoryLimits: cloudStateUtilitity.cloudResources.getLimitsMemory(environmetNew)-'Mi',
					memoryRequests: cloudStateUtilitity.cloudResources.getRequestsMemory(environmetNew)-'Mi',
					cpuLimits: cloudStateUtilitity.cloudResources.getLimitsCPU(environmetNew)-'m',
					cpuRequests: cloudStateUtilitity.cloudResources.getRequestsCPU(environmetNew)-'m'
				]
			}
			
			def environmentOther =  environment
			
			if (environmentOther == null) {
				environmentOther = environmetNew
			}
			
			def bodyDeploy = [
				deploy: deployParams,
				srvEnvId: environmentOther.toUpperCase(),
				installationType: 'I'
			]
			
			response = sendRequestToAlm3MS(
                'PUT',
                "${GlobalVars.URL_CATALOGO_ALM_PRO}/app/${bodyNew.type}/${bodyNew.aplicacion}/version/${bodyNew.major}/${bodyNew.minor}/${bodyNew.fix}/${bodyNew.typeVersion}/deploy",
                bodyDeploy,
                "${GlobalVars.CATALOGO_ALM_ENV}",
                [
                    kpiAlmEvent: new KpiAlmEvent(
                        pomXml, pipelineData,
                        KpiAlmEventStage.UNDEFINED,
                        KpiAlmEventOperation.CATALOG_HTTP_CALL)
                ])

            if (response.status == 200) {

				printOpen("Deploy realizado", EchoLevel.INFO)

			} else {
                printOpen("Error sending deployment info to Open's catalogue", EchoLevel.ERROR)

			}
			
		}catch(Exception ex) {
			printOpen("Error sending sending data to Open's catalogue", EchoLevel.ERROR)
			if (env.SEND_TO_ALM_CATALOG_REQUIRED!=null && env.SEND_TO_ALM_CATALOG_REQUIRED!="true") {
				throw new Exception("Unexpected response from CATALOG, services catalog ")
			}
		}
	}else {
        printOpen("Open's catalogue is currently offline", EchoLevel.INFO)
	}	
	
	
}

def call(def body, PipelineData pipelineData, PomXmlStructure pomXml, CloudStateUtility cloudStateUtilitity) {
	deployArtifactInCatalog(body, pipelineData, pomXml, cloudStateUtilitity, null)
}

def sendRequestToCatalog(def body) {

	boolean weHaveToSendToCatalog = GlobalVars.SEND_TO_CATALOG

	if (weHaveToSendToCatalog) {

		def url = idecuaRoutingUtils.catalogPipelineUrl()

		printOpen("Body to be sent to BackEndAppPortal URL ${url}:\n\n${body}", EchoLevel.DEBUG)

		def response

		try {
			response = sendRequestToAppPortal('PUT', url, "", body)
		} catch (Exception e) {
			throw new Exception("Unexpected response when connecting to BackEndAppPortal (${response?.status})! + ${e.getMessage()}")
		}

		return response

	} else {
		def toJson = { input -> groovy.json.JsonOutput.toJson(input) }
		body = toJson(body)
		printOpen("Skipping artifact publication in catalog:\n\n${body}", EchoLevel.INFO)
	}
}
