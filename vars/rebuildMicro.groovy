import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.KpiAlmEvent

import com.caixabank.absis3.KpiAlmEventOperation
import com.caixabank.absis3.KpiAlmEventStage
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.ICPDeployStructure
import com.caixabank.absis3.ICPApiResponse
import groovy.json.JsonSlurperClassic


private Map retrieveAppInfo(Map valuesDeployed)
{
	if (valuesDeployed["absis"]!=null) {
		Map valuesDeployedLocal=valuesDeployed["absis"]
		if (valuesDeployedLocal["app"]!=null) {
			
			Map valuesDeployedLocalApp=valuesDeployedLocal["app"]

			return valuesDeployedLocalApp

		}
	}
}
private Map retrieveNewAndStableAppICPDeploymentMetadata(Map valuesDeployed) {
	
		if (valuesDeployed["absis"]!=null) {
	
			Map valuesDeployedLocal=valuesDeployed["absis"]
	
	
			if (valuesDeployedLocal["apps"]!=null) {
	
				Map valuesDeployedLocalApp=valuesDeployedLocal["apps"]
	
				if (valuesDeployedLocalApp["envQualifier"]!=null) {
	
					def valuesDeployedLocalEnvVarsList = valuesDeployedLocalApp["envQualifier"]
					return valuesDeployedLocalEnvVarsList
				}
	
			}
		}
	
		return [:]
	}

def buildArtifactOnIcp(String requestURL, def body, String method, String aplicacionGAR, String pollingRequestUrl )  {
	ICPApiResponse responseIcp=null
	try {

		try {

			responseIcp = sendRequestToICPApi(requestURL,body,method,aplicacionGAR,pollingRequestUrl,true,false)

		} catch(java.io.NotSerializableException e) {

			responseIcp = sendRequestToICPApi(requestURL,body,method,aplicacionGAR,pollingRequestUrl,true,false)

		}
		
		if (responseIcp.statusCode==500) {

            printOpen("Error puntual... vamos a probar suerte otra vez ${responseIcp.statusCode}", EchoLevel.ERROR)
			responseIcp = sendRequestToICPApi(requestURL,body,method,aplicacionGAR,pollingRequestUrl,true,false)

		}

	} catch(Exception e) {

		throw e

	}
	
	return responseIcp
}
def call(def valuesDeployed, String componentType, String component, String major, String environment, String namespace) {
	def app=component+major
	def valuesDeployedLocalEnvVarsList = retrieveNewAndStableAppICPDeploymentMetadata(valuesDeployed)
	def valuesApp=retrieveAppInfo(valuesDeployed)
	
	if (valuesApp!=null) {
		valuesApp.put("type", componentType)
	}
	if (valuesDeployedLocalEnvVarsList["new"]!=null) {
		throw new Exception ("El micro no esta consolidado!!!!! no se puede migrar")
	}
    printOpen("Los valores setteados son los siguientes ${valuesDeployedLocalEnvVarsList}", EchoLevel.DEBUG)
    printOpen("Antes de llamar a recuperar el id de componente", EchoLevel.DEBUG)
	def response = sendRequestToAbsis3MS(
					'GET',
					"${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${componentType}/${component}",
					null,
					"${GlobalVars.CATALOGO_ABSIS3_ENV}",
					200,
					[
							kpiAlmEvent: new KpiAlmEvent(
							null, null,
							KpiAlmEventStage.UNDEFINED,
							KpiAlmEventOperation.CATMSV_HTTP_CALL)
					])

    //SRV.MS/demoarqcbk
    printOpen("Get : ${response.content}", EchoLevel.DEBUG)
	//def json = new groovy.json.JsonSlurperClassic().parseText(response.content)		
	def json = response.content

	
	//Recuperar el micro desplegado en el entorno

	//Validar que estemos aun desplegando
	response = sendRequestToAbsis3MS(
		'GET',
		"${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${componentType}/${component}/version/${major}/environment/${environment}",
		null,
		"${GlobalVars.CATALOGO_ABSIS3_ENV}",
		
		[
			kpiAlmEvent: new KpiAlmEvent(
				null, null,
				KpiAlmEventStage.UNDEFINED,
				KpiAlmEventOperation.CATMSV_HTTP_CALL)
		])

	if (response.status == 200) {
		//def json1 = new JsonSlurperClassic().parseText(response.content)
		def json1 = response.content
        printOpen("Get : ${response.content}", EchoLevel.DEBUG)
		
		//DEV 1:1.1.0-SNAPSHOT-B
		//TST 1.0.0-RC1
		//PRE 1.0.0
		//PRO 1.0.0-R
		def typeVersion=json1.typeVersion
		if ("RELEASE".equals(typeVersion)) {			
			typeVersion=""
		}else if ("RC".equals(typeVersion)) {
			typeVersion="-"+json1.buildCode
		}else {			
			typeVersion="-"+typeVersion
		}
		
		String additionalBuildParam=""
		if (json.db2Dds && getDb2Driver(environment)!=null) {
			additionalBuildParam=additionalBuildParam+",DB2_DRIVER_INPUT="+getDb2Driver(environment)
		}else {
            printOpen("No es DB2, no tenemos que actualizar nada de nada", EchoLevel.INFO)
		}
		def versionArtifact=json1.major+"."+json1.minor+"."+json1.fix+typeVersion
		def versionImage="REBUILD-"+versionArtifact
		
		def body = [
			extraArgs: "GROUP_ID=${json.groupId},VERSION_ARTIFACT=${versionArtifact},ARTIFACT_ID=${json.name}${additionalBuildParam}",
			version: "${versionImage}"
		]
		
		String appICPId = namespace=="ARCH" ? GlobalVars.ICP_APP_ID_ARCH : GlobalVars.ICP_APP_ID_APPS
		String appICP = namespace=="ARCH" ? GlobalVars.ICP_APP_ARCH : GlobalVars.ICP_APP_APPS
		
		ICPApiResponse responseComp = sendRequestToICPApi("v1/application/${appICPId}/component",null,"GET","${appICP}","", false, false)
		String componentId="0"
		
		if (responseComp.statusCode>=200 && responseComp.statusCode<300 && valuesDeployed!=null) {
			responseComp.body.each {
				if (it.name.equals(app.toUpperCase())) {
					componentId=it.id
				}
			}
		}

		def responseBuild = buildArtifactOnIcp("v1/type/PCLD/application/${appICP}/component/${componentId}/build", body, "POST", "${appICP}", "v1/type/PCLD/application/${appICP}/component/${componentId}/build")
		
		//New image
		if (responseBuild.statusCode>=200 && responseBuild.statusCode<300) {
			//Nueva imagen creada
			Map appStable = valuesDeployedLocalEnvVarsList["stable"]
            printOpen("la nueva imagen es de ${responseBuild.body.imageRepo1} ${responseBuild.body.version}", EchoLevel.INFO)
			appStable.put("image", responseBuild.body.imageRepo1+":"+responseBuild.body.version)
		}else {
			throw new Exception("Esto es un error grave")
		}
	
		
	}else {
		throw new Exception("La app no existe ${component} en la major ${major} en el entorno ${environment}")
	}
    printOpen("Devolvemos el canary por defecto DEVOPS", EchoLevel.INFO)
	return valuesDeployed
}

