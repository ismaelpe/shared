import com.project.alm.*
import groovy.json.JsonSlurperClassic

/**
 * Sube traducciones del micro aplicativo para el micro errormanagment-micro
 *
 *
 */
def call(PipelineData pipelineData, PomXmlStructure pomXml) {

    printOpen("Preparing data to publish in errormanagment-micro", EchoLevel.ALL)
	def translationPhase="FIRST"

	try {		
	
		kpiLogger(pomXml, pipelineData, KpiLifeCycleStage.ERROR_MANAGEMENT_PUBLISH_STARTED, KpiLifeCycleStatus.OK)
		
		String sourceFile = getSourcePath(pomXml,GlobalVars.JSON_ERROR_MANAGEMENT_DELETE) 
		printOpen("Reading delete json file: ${sourceFile}", EchoLevel.ALL)
		def existsFile = fileExists sourceFile
		def parsedTranslations = []
	
		if (existsFile) {
			translationPhase="DELETING_TRANSLATIONS"
			printOpen("	calling api to delete translations", EchoLevel.ALL)            
			sendRequestToErrorManagment([file: sourceFile], 'DELETE',
										pipelineData.domain,
										pipelineData.bmxStructure.environment,
										GlobalVars.URL_API_TRANSLATIONS,
										pomXml.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name)))
			
		}else {
			printOpen("	file does not exist -> do nothing", EchoLevel.ALL)
		}
		
		sourceFile = getSourcePath(pomXml,GlobalVars.JSON_ERROR_MANAGEMENT_PUT)
		printOpen("Reading put json file: ${sourceFile}", EchoLevel.ALL)
		existsFile = fileExists sourceFile
		parsedTranslations = []
	
		if (existsFile) {
			translationPhase="PUTTING_TRANSLATIONS"
			printOpen("	calling api to put translations", EchoLevel.ALL)            
			sendRequestToErrorManagment([file: sourceFile], 'PUT',
										pipelineData.domain,
										pipelineData.bmxStructure.environment,
										GlobalVars.URL_API_TRANSLATIONS,
										pomXml.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name)))
			
		}else {
			printOpen("	file does not exist -> do nothing", EchoLevel.ALL)
		}
		
		String disableNotification = "${env.ALM_SERVICES_SKIP_NOTIFICATION_UNMAPPED_ERRORS_ALL}"
		def whiteListApps = "${env.ALM_SERVICES_SKIP_NOTIFICATION_UNMAPPED_ERRORS_LIST}".split(";")
		
		printOpen("disableNotification: ${disableNotification}", EchoLevel.ALL)
		printOpen("whiteListApps: ${whiteListApps}", EchoLevel.ALL)
		printOpen("pomXml.artifactName: ${pomXml.artifactName}", EchoLevel.ALL)

		if ("true".equals(disableNotification) || Arrays.asList(whiteListApps).contains(pomXml.artifactName)) {
			printOpen("SKIP Manage unmapped translations", EchoLevel.ALL)
		} else {
			printOpen("DO Manage unmapped translations", EchoLevel.ALL)
			translationPhase="GETTING_UNMAPPED_TRANSLATIONS"
			printOpen("	calling api to get unmapped translations", EchoLevel.ALL)
			def jsonResponseUnmappedTranslations =
				sendRequestToErrorManagment([json: parsedTranslations], 'GET',
										pipelineData.domain,
										pipelineData.bmxStructure.environment,
										GlobalVars.URL_API_UNMMAPPED_TRANSLATIONS,
										pomXml.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name)))

				
			
			if (jsonResponseUnmappedTranslations.translations.size() > 0 || 
				jsonResponseUnmappedTranslations.translationscics.size() > 0) {
				printOpen("	there are pending unmapped translations", EchoLevel.ALL)
			
				String artifactGarAppName = pomXml.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name))
				printOpen("	artifactGarAppName ${artifactGarAppName}", EchoLevel.ALL)
				String artifactGarType = pipelineData.garArtifactType.getGarName()
				printOpen("	artifactGarType ${artifactGarType}", EchoLevel.ALL)
				String pipelineName = pipelineData.getPipelineBuildName();
				printOpen("	pipelineName ${pipelineName}", EchoLevel.ALL)
				
				sendEmailToMicroResposibles(pipelineName,artifactGarAppName,artifactGarType,jsonResponseUnmappedTranslations)
			
			}else {
				printOpen("	there are not ANY pending unmapped translations", EchoLevel.ALL)
			}
		}
		kpiLogger(pomXml, pipelineData, KpiLifeCycleStage.ERROR_MANAGEMENT_PUBLISH_ENDED, KpiLifeCycleStatus.OK)
	}catch( Exception e) {
		//Ha petado por algun motivo... quizas el error management en TST no esta fino o el gateway no funciona
		//Vamos a continuar con la peticion pero vamos a dar de alta los kpis
		kpiLogger(pomXml, pipelineData, KpiLifeCycleStage.ERROR_MANAGEMENT_PUBLISH_ERROR, KpiLifeCycleStatus.KO)
		printOpen("Existe un error en la captura de errores no traducidos o en la subida de los errores actuales ${e}", EchoLevel.ALL)
		//Revisamos si tenemos que abortar o continuar adelante 
		if (shouldWeAbortThePipe(pomXml,translationPhase)) {	
			printOpen("Abortaremos la pipeline por error en traduccion de errores", EchoLevel.ALL)
			throw e
		}
	}

}

//Devuelve si es null
private boolean isTrue(def property) {
	if (property==null || property=="false") {
		return false
	}else {
		if (property=="true") {
			return true
		}
	}
	return false
}

/**
 * Permite eludir el casque cuando peta el upload o el download del erorr management
 * @param pomXml PomXmlStructure
 * @param translationPhase Contoiene la fase del parametro
 * @return true si no vamos a abortar la pipeline false si tenemos que continuar con el pipe
 */
private def shouldWeAbortThePipe(PomXmlStructure pomXml, def translationPhase) {	
	boolean abortPipe = true

	printOpen("Looking if we have to abort the pipe ${translationPhase}", EchoLevel.ALL)

	//No tenemos la general ahora deberiamos quizas revisar a nivel de fase
	//Quizas deberiamos saber el tipo de rama en la que estamos para saber la gravedad del problema
	def type = ""
	def method = "PUT"
	
	if (translationPhase == "GETTING_UNMAPPED_TRANSLATIONS") {
		method = "GET"
	}	
	
	if (pomXml.isRelease()) {
		type = "RELEASE"		
	} else if (pomXml.isRCVersion()) {
		type = "RC"		
	} else {
		type = "SNAPSHOT"		
	}	

	def skipErrorTraslation_Key = "SKIP_ERROR_TRANSLATIONS"
	def skipErrorTraslation_Value = isTrue(env[skipErrorTraslation_Key])
		
	def skipErrMgrAll_Key = "SKIP_ERRMGR_ALL_${type}"
	def skipErrMgrAll_Value = isTrue(env[skipErrMgrAll_Key])

	def skipErrMgrMethod_Key = "SKIP_ERRMGR_${method}_${type}"
	def skipErrMgrMethod_Value = isTrue(env[skipErrMgrMethod_Key])
	
	abortPipe = !skipErrMgrAll_Value || !skipErrorTraslation_Value || !skipErrMgrMethod_Value

	printOpen("Checking variables ${skipErrMgrAll_Key}: ${skipErrMgrAll_Value}; ${skipErrorTraslation_Key}: ${skipErrorTraslation_Value}; ${skipErrMgrMethod_Key}: ${skipErrMgrMethod_Value} -> Abort Pipeline ${abortPipe}", EchoLevel.ALL)
	
	return abortPipe
}

private void sendEmailToMicroResposibles(String pipelineName,String artifactGarAppName, String artifactGarType,Object jsonResponseUnmappedTranslations) {

	printOpen("sendEmailToMicroResposibles BEGIN", EchoLevel.ALL)
	try {
		def json = groovy.json.JsonOutput.toJson(jsonResponseUnmappedTranslations)
		json = groovy.json.JsonOutput.prettyPrint(json)
		writeFile(file:GlobalVars.TEMP_FILE_UNMAPPED_TRANSLATIONS, text: json)

		def emailsStr = idecuaRoutingUtils.getResponsiblesAppEmailList(artifactGarAppName,artifactGarType)
		String bodyEmail = "<p>Buenos días, </p><p>Se han detectado errores sin traducir en la aplicación: ${artifactGarAppName}.</p><p>Se adjunta un fichero con la información técnica de estos.</p><p>Visualizar logs de la pipeline: ${GlobalVars.JOB_DISPLAY_CONFLUENCE}</p><p>Saludos.</p>"
		String subjectEmail = " Notificación errores sin traducir en la aplicación: ${artifactGarAppName} - ${pipelineName} "
		printOpen("	sending email", EchoLevel.ALL)
		sendEmail(subjectEmail, GlobalVars.EMAIL_FROM_ALM,emailsStr, "",GlobalVars.TEMP_FILE_UNMAPPED_TRANSLATIONS, bodyEmail) 
			
			
	} catch (Exception e) {
			printOpen("	WARNING: Error sendEmailToMicroResposibles: ${e.getMessage()}", EchoLevel.ALL)
	}finally {
		sh "rm -rf ${GlobalVars.TEMP_FILE_UNMAPPED_TRANSLATIONS}"
	}
	printOpen("sendEmailToMicroResposibles END", EchoLevel.ALL)
		
}


def getErrorManagementUrl(String environment) {
	String url = ""
	switch (environment) {
		case GlobalVars.EDEN_ENVIRONMENT:
			url = GlobalVars.URL_ERRORMGNT_DEV
			break
		case GlobalVars.DEV_ENVIRONMENT:
			url = GlobalVars.URL_ERRORMGNT_DEV
			break
		case GlobalVars.TST_ENVIRONMENT:
			url = GlobalVars.URL_ERRORMGNT_TST
			break
		case GlobalVars.PRE_ENVIRONMENT:
			url = GlobalVars.URL_ERRORMGNT_PRE
			break
		case GlobalVars.PRO_ENVIRONMENT:
			 url = GlobalVars.URL_ERRORMGNT_PRO
			 break
	}
	return url
}



def getSourcePath(PomXmlStructure pomXml, String dependenciesPath) {
	
	String sourceFolder = dependenciesPath

	if (pomXml.artifactType == ArtifactType.AGREGADOR) {
		printOpen("Artifact is agregador with subtype ${pomXml.artifactSubType}", EchoLevel.ALL)
		if (pomXml.artifactSubType == ArtifactSubType.MICRO_ARCH) {
			printOpen("Artifact is micro", EchoLevel.ALL)
			sourceFolder = pomXml.artifactMicro + "/" + dependenciesPath
		} else if (pomXml.artifactSubType == ArtifactSubType.PLUGIN) {
			printOpen("Artifact is sample app", EchoLevel.ALL)
			sourceFolder = pomXml.artifactSampleApp + "/" + dependenciesPath
		} else if (pomXml.artifactSubType == ArtifactSubType.ARCH_LIB || pomXml.artifactSubType == ArtifactSubType.ARCH_LIB_WITH_SAMPLEAPP) {
			printOpen("Artifact is sample app", EchoLevel.ALL)
			sourceFolder = pomXml.artifactSampleApp + "/" + dependenciesPath
		}
	}
	return sourceFolder
}

def sendRequestToErrorManagment(def param,String method,String domain,String enviroment,String api, String app) {		
		def url = getErrorManagementUrl(enviroment) + api +"?domain=${domain}&app=${app}"
		def responseJson

		try {
			def httpValidStatus = ~/[2-3]{1}[0-9]{2}/
			def response 
			
			if (param.file) {
				def file = "@$env.WORKSPACE/$param.file"
				
				printOpen("Send to errormanagement using 'file' option: $file", EchoLevel.INFO)
				
				response = sendRequestToAlm3MS(method, url, file, enviroment,[
					isJsonInputData: false,
					kpiAlmEvent: new KpiAlmEvent(
						null, null,
						KpiAlmEventStage.UNDEFINED,
						KpiAlmEventOperation.ERRORMANAGEMENT_HTTP_CALL)])
				
				responseJson = response.content
			} else if (param.json) {
				printOpen("Send to errormanagement using 'json' option:", EchoLevel.INFO)
				response = sendRequestToAlm3MS(method, url, param.json, enviroment,[
					isJsonInputData: false,
					kpiAlmEvent: new KpiAlmEvent(
						null, null,
						KpiAlmEventStage.UNDEFINED,
						KpiAlmEventOperation.ERRORMANAGEMENT_HTTP_CALL)])
				//responseJson = new JsonSlurperClassic().parseText(response.content)
				responseJson = response.content
			} else {
				throw new Exception("Unknown send option to errormanagement: $param")
			}
			
			if (!httpValidStatus.matcher("${response.status}").matches()) {
				throw new Exception("Response Failed with " + response.status + " HTTP Status. Error :" + responseJson)
			}
		} catch (Exception e) {
			printOpen("Somthing was wrong during errormanagment-micro sendRequest: ${e.getMessage()}", EchoLevel.ALL)
			throw e
		}

		return responseJson

}
