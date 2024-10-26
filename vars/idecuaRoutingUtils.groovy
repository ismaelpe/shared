import com.project.alm.*
import groovy.json.JsonSlurperClassic

def agileWorkUrl() {
    return getGplBase() + GlobalVars.PATH_AUTH_FEATURE_AGILE_WORK
}

def pipelineUrlById(String pipelineId) {
    return getGplBase() + GlobalVars.PATH_GPL_PIPELINE + "/" + pipelineId + "/"
}

def pipelineResultUrl(String pipelineId) {
    //return getGplBase() + GlobalVars.PATH_GPL_PIPELINE + "/resultado/" + pipelineId
	return getGplBase() + GlobalVars.PATH_GPL_PIPELINE + "/updateResult/" + pipelineId
}

def pipelineDeployUrl(String pipelineId) {
    return getGplBase() + GlobalVars.PATH_GPL_PIPELINE + "/notifyDeployment/" + pipelineId
}

def pipelineUrl() {
    return getGplBase() + GlobalVars.PATH_GPL_PIPELINE
}

def stageUrl(String stageId) {
    return getGplBase() + GlobalVars.PATH_GPL_STAGE + "/" + stageId
}

def catalogPipelineUrl() {
    return getGsaBase() + GlobalVars.PATH_CATALOG_PIPELINE;
}

def instalationsUrl(String garType, String application, String component) {
	return getGsaBase() + GlobalVars.GSA_ACTUAL_INSTALLATION_PATH.replace("{garType}",garType).replace("{application}",application).replace("{component}",component);
}

private String getGplBase() {
    if (hasToInvokeGplPRE(env.executionProfile)) {
        return GlobalVars.URL_GPL_PRE
    } else {
        return GlobalVars.URL_GPL
    }
}

private String getGsaBase() {
    if (hasToInvokeGplPRE(env.executionProfile)) {
        return GlobalVars.CATALOG_URL_PRE
    } else {
        return GlobalVars.CATALOG_URL
    }
}

private String getGarBase() {
	if (hasToInvokeGplPRE(env.executionProfile)) {
		return GlobalVars.URL_GAR_PRE
	} else {
		return GlobalVars.URL_GAR
	}
}

def responsiblesPipelineUrl() {
	return getGarBase() + GlobalVars.PATH_GAR_RESPONSIBLES
}

def infoUsuariosGarUrl() {
    return getGarBase() + GlobalVars.PATH_GAR_USUARIOS
}

def infoAppGarUrl() {
	return getGarBase() + GlobalVars.PATH_GAR_APP
}

def getResponsiblesGarApp(String artifactGarAppName, String artifactGarType) {

    printOpen("	artifactGarAppName ${artifactGarAppName}", EchoLevel.ALL)
    printOpen("	artifactGarType ${artifactGarType}", EchoLevel.ALL)

    def emailsStr
    try {
        def url = responsiblesPipelineUrl();
        url = url + "/" +artifactGarType + "/" +artifactGarAppName
        printOpen("	url responsibles api: ${url}", EchoLevel.ALL)

        printOpen("	calling GAR responsibles api ", EchoLevel.ALL)

        def response = sendRequestToService('GET', url, "", null)
        def content = response.content

        printOpen("  GAR responsibles response:\n${content}", EchoLevel.ALL)
        return content

    } catch (Exception e) {
        printOpen("	WARNING: Error getResponsiblesGarApp: ${e.getMessage()}", EchoLevel.ALL)
    }
}

def getResponsiblesAppEmailList (String artifactGarAppName, String artifactGarType) {

    def emailsStr
	try {

        def responsiblesList = getResponsiblesGarApp(artifactGarAppName, artifactGarType)
        def emailList = []
        for(responsible in responsiblesList) {
            emailList.push(responsible.mail)
            //for test
            //emailList.push("leonardo.torres@mimacom.com")
        }

        emailsStr = emailList.join(', ')
        printOpen("emailsStr ${emailsStr}", EchoLevel.ALL)

	} catch (Exception e) {
			printOpen("	WARNING: Error getResponsiblesAppEmailList: ${e.getMessage()}", EchoLevel.ALL)
	}
	return emailsStr
}

def getInfoAppFromTypeAndName(String typeApp, String appId) {
	try {
		printOpen("Tomando la información de la app ${appId} con el GAR Id ${typeApp}", EchoLevel.ALL)
		
		def url = infoAppGarUrl() + "/" +typeApp + "/" + appId

		def response = sendRequestToService('GET', url, "", null)
		//def app = new JsonSlurperClassic().parseText(response.content)
		def app = response.content
		printOpen("Recogida info de la app ${typeApp} ${appId}:\n\n${app}", EchoLevel.ALL)

		return app

	} catch (Exception e) {
		printOpen("Error getInfoAppFromTypeAndName(${appId}): ${e.getMessage()}", EchoLevel.ALL)
		throw e
	}
}

def getUsuarioInfoFromMatricula(String matricula) {

    try {
        printOpen("Tomando la información del usuario ${matricula} de GAR", EchoLevel.ALL)
        def url = infoUsuariosGarUrl() + "/" +matricula

        def response = sendRequestToService('GET', url, "", null, [echoLevel: EchoLevel.NONE])
        //def usuario = new JsonSlurperClassic().parseText(response.content)
		def usuario = response.content
        
        // FIXME: SE PRINTA EN EL LOG INFORMACION SENSIBLE
        // printOpen("Recogida info del usuario ${matricula}:\n\n${usuario}", EchoLevel.ALL)
        printOpen("Recogida info del usuario ${matricula}:\n\nContenido de la respuesta no mostrado por seguridad!!", EchoLevel.ALL)
        
        return usuario

    } catch (Exception e) {
        printOpen("Error getUsuarioInfoFromMatricula(${matricula}): ${e.getMessage()}", EchoLevel.ALL)
        throw e
    }

}

def getUsuarioEmailFromPipelineMetadata(PipelineData pipelineData, PomXmlStructure pomXml) {

    def emailFromPipelineData = getUsuarioEmailFromPipelineMetadata(pipelineData)

    return emailFromPipelineData ? emailFromPipelineData :
        getEmailFromFirstResponsibleOnGarIfAvailable(pipelineData, pomXml)
}

def getUsuarioEmailFromPipelineMetadata(PipelineData pipelineData, IClientInfo clientInfo) {

    def emailFromPipelineData = getUsuarioEmailFromPipelineMetadata(pipelineData)

    return emailFromPipelineData ? emailFromPipelineData :
        getEmailFromFirstResponsibleOnGarIfAvailable(pipelineData, clientInfo)
}

def getUsuarioEmailFromPipelineMetadata(PipelineData pipelineData) {

    if (thisUserEmailCanHandleMaximos(pipelineData.pushUserEmail)) {

        return pipelineData.pushUserEmail

    } else if (weHaveValidMatriculaAvailable(pipelineData.pushUser)) {

        def garUserInfo = getUsuarioInfoFromMatricula(pipelineData.pushUser)
        if (garUserInfo?.mail) {
            return garUserInfo.mail
        }

    }

    return null
}

def getUsuarioEmailFromPipelineMetadata(String artifactGarAppName, String artifactGarType) {

    return getEmailFromFirstResponsibleOnGarIfAvailable(artifactGarAppName, artifactGarType)
}


def getEmailFromFirstResponsibleOnGarIfAvailable(PipelineData pipelineData, PomXmlStructure pomXml) {

    String artifactGarAppName = pomXml.getApp(pipelineData.garArtifactType)
    String artifactGarType = pipelineData.garArtifactType.getGarName()

    return getEmailFromFirstResponsibleOnGarIfAvailable(artifactGarAppName, artifactGarType)
}

def getEmailFromFirstResponsibleOnGarIfAvailable(PipelineData pipelineData, IClientInfo clientInfo) {

    String artifactGarAppName = clientInfo.getApp(pipelineData.garArtifactType)
    String artifactGarType = pipelineData.garArtifactType.getGarName()

    return getEmailFromFirstResponsibleOnGarIfAvailable(artifactGarAppName, artifactGarType)
}

def getEmailFromFirstResponsibleOnGarIfAvailable(String artifactGarAppName, String artifactGarType) {

    def responsiblesList = getResponsiblesGarApp(artifactGarAppName, artifactGarType)

    if (responsiblesList) {

        return responsiblesList[0].mail

    }

    return null

}

private boolean thisUserEmailCanHandleMaximos(String email) {

    return email && email != "jenkins.pipeline.CI@digitalscale.es"

}

private boolean weHaveValidMatriculaAvailable(String matricula) {

    final String matriculaRegex = "(U01)(\\d+)(\\d+)(\\d+)(\\d+)(\\d+)"
    return matricula.matches(matriculaRegex)

}

private boolean hasToInvokeGplPRE(String executionProfile) {
    if (
        PipelineExecutionMode.COMPLETE_TEST_AUTO.equals(executionProfile) ||
        PipelineExecutionMode.COMPLETE_TEST_AUTO_HOTFIX.equals(executionProfile) ||
        PipelineExecutionMode.COMPLETE_TEST_AUTO_CONFIGURATIONFIX.equals(executionProfile)
    ) {

        // Decided that we'll send to PRO also when pipeline is under testing
        return false

    } else {

        return false

    }
}

