import com.caixabank.absis3.*
import com.caixabank.absis3.GlobalVars
import groovy.json.JsonSlurperClassic
import java.util.List
import java.util.ArrayList
import groovy.json.JsonOutput


def call( boolean isCampaign, String environment) {
	printOpen("Procedemos a enviar las peticiones contra Catalogo en campaña", EchoLevel.ALL)
	validateDependenciesCatMsv(null,null,isCampaign,environment)
}


def generateOutputForLogsDevPortal(def content, boolean isCampaign) {
	if (isCampaign) {
		return JsonOutput.prettyPrint(JsonOutput.toJson(content))
	}else {
		return JsonOutput.prettyPrint(JsonOutput.toJson(content))
	}
}


def call(PipelineData pipelineData, PomXmlStructure pomXml, boolean isCampaign, String environment) {
	def response = null
	printOpen("Procedemos a enviar las peticiones contra Catalogo", EchoLevel.ALL)
	if (isCampaign) {
		response = sendRequestToAbsis3MS(
			'GET',
			"${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/iop/dependency/validate/${environment}",
			null,
			"${GlobalVars.CATALOGO_ABSIS3_ENV}",501,
			[
				kpiAlmEvent: new KpiAlmEvent(
					pomXml, pipelineData,
					KpiAlmEventStage.UNDEFINED,
					KpiAlmEventOperation.CATMSV_HTTP_CALL)
			])
		printOpen("Ejecucion catmsv Realizada", EchoLevel.ALL)
	} else {
		def typeVersion=""
		if (pomXml.artifactVersionQualifier == null || pomXml.artifactVersionQualifier == "") {
			typeVersion = "RELEASE"
		} else if (pomXml.isSNAPSHOT()) {
			typeVersion = "SNAPSHOT"
		} else if (pomXml.isRCVersion()) {
			typeVersion = "RC"
		} else {
			typeVersion = "UNKNOWN"
		}	
			
	     response = sendRequestToAbsis3MS(
			'GET',
			"${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${pipelineData.getGarArtifactType().getGarName()}/${pomXml.getApp(pipelineData.garArtifactType)}/version/${pomXml.artifactMajorVersion}/${pomXml.artifactMinorVersion}/${pomXml.artifactFixVersion}/${typeVersion}/dependency/validate/${environment}",
			null,
			"${GlobalVars.CATALOGO_ABSIS3_ENV}",
			[
				kpiAlmEvent: new KpiAlmEvent(
					pomXml, pipelineData,
					KpiAlmEventStage.UNDEFINED,
					KpiAlmEventOperation.CATMSV_HTTP_CALL)
			])
		 printOpen("Ejecucion catmsv Realizada", EchoLevel.ALL)
	}
	if (response.status == 200) {
		
		printOpen("Dependencias correctas, la iop esta bien", EchoLevel.ALL)
		
	} else if (response.status  == 400) {
		printOpen("Posible error en las dependencias, existen dependencias incompletas", EchoLevel.ALL)
		throw new Exception("Posible error en las dependencias ${response.content}")
	} else if (response.status  == 404 && isCampaign ) {
		printOpen("No hay ninguna Campaña abierta", EchoLevel.ALL)
		throw new Exception("No hay ninguna Campaña abierta")		
	} else if (response.status  == 404 && !isCampaign ) {
		printOpen("No hay ninguna Campaña abierta", EchoLevel.ALL)
		throw new Exception("La version no esta inventariada en el sistema")		
	} else {
		printOpen("Error en la validacion de dependencias al despliegue del micro", EchoLevel.ALL)
		throw new Exception("Error en la validacion de dependencias al despliegue del micro")
	}
					
	
}