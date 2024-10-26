import com.project.alm.*
import hudson.Functions

def testMaximoButDoNotThrowException(PipelineData pipelineData, PomXmlStructure pomXml, String buildLog = null) {

    MaximoAbstractFallo fallo = new MaximoFalloPrueba()
    fallo.attachments = ['buildCloud.log': buildLog]

    boolean weShouldCreateMaximo = ( ! env.pruebaMaximoHasBeenCreated ) && weShouldCreateMaximoAccordingToEnvironment(pipelineData)
    // Solo haremos esta prueba en DEV
    weShouldCreateMaximo = weShouldCreateMaximo && pipelineData.deployStructure.env.equalsIgnoreCase("dev")

    if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(pipelineData, pomXml))
                .tipoFallo(fallo)

        env.cloudDeployMaximoHasBeenCreated = crearMaximo(request)

    } else {
        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)
    }

}

def cloudDeployException(PipelineData pipelineData, PomXmlStructure pomXml, def responseOrException, String buildLog = null) {

    String log = getLogFromObject(responseOrException)
    String environment = pipelineData.bmxStructure.environment.toUpperCase()
    MaximoAbstractFallo fallo = new MaximoFalloCloudDeploy(environment.toUpperCase(), log, buildLog)

    boolean weShouldCreateMaximo = ( ! env.cloudDeployMaximoHasBeenCreated ) && weShouldCreateMaximoAccordingToEnvironment(pipelineData)
    //FIXME: Desactivamos maximos en EDEN hasta solucionar el problema de los IDs de despliegue repetidos
    weShouldCreateMaximo = weShouldCreateMaximo && ! pipelineData.deployStructure.env.equalsIgnoreCase("eden")

    if (GlobalVars.KPILOGGER_IS_NOW_ACTIVE) {

        printOpen("Ibamos a crear un máximo por fallo Cloud pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último. No crearemos un máximo", EchoLevel.INFO)
        GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

        throw new Exception("Ibamos a crear un máximo por fallo Cloud pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último")

    } else if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(pipelineData, pomXml))
                .tipoFallo(fallo)

        env.cloudDeployMaximoHasBeenCreated = crearMaximo(request)

        throw new Exception(validateAndReplace(fallo.pipelineExceptionErrorMessage))

    } else if (!"${env.ALM_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()) {

        throw new Exception("Se ha producido un error. Si el error persiste abrid máximo al Servicio TI: ${fallo.equipoResponsable.servicioTI}")

    } else {

        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)

        throw new Exception("Ibamos a crear un máximo por fallo Cloud pero ya se ha creado")
    }

}

def kubernetesDisabledException(PipelineData pipelineData, PomXmlStructure pomXml, String environment, String feature) {

    MaximoAbstractFallo fallo = new MaximoFalloCloudDeployIsDisabled(environment, feature)

    boolean weShouldCreateMaximo = ( ! env.kubernetesDisabledMaximoHasBeenCreated ) && weShouldCreateMaximoAccordingToEnvironment(pipelineData)

    if (GlobalVars.KPILOGGER_IS_NOW_ACTIVE) {

        printOpen("Ibamos a crear un máximo por API Cloud desactivada pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último. No crearemos un máximo", EchoLevel.INFO)
        GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

        throw new Exception("Ibamos a crear un máximo por API Cloud desactivada pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último")

    } else if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(pipelineData, pomXml))
                .tipoFallo(fallo)

        env.kubernetesDisabledMaximoHasBeenCreated = crearMaximo(request)

        throw new Exception(validateAndReplace(fallo.pipelineExceptionErrorMessage))

    } else if (!"${env.ALM_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()) {

        throw new Exception("Se ha producido un error. Si el error persiste abrid máximo al Servicio TI: ${fallo.equipoResponsable.servicioTI}")

    } else {
        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)

        throw new Exception("Ibamos a crear un máximo por API Cloud desactivada pero ya se ha creado")
    }

    

}

def deployBuildDockerImageFailure(PipelineData pipelineData, PomXmlStructure pomXml, def responseOrException, String buildLog = null) {

    String log = getLogFromObject(responseOrException)

    try {

        def json = new groovy.json.JsonSlurper().parseText(log)
        String buildId = json?.id

        buildLog = retrieveBuildLogIfAvailable(pipelineData, pomXml, buildId)

    } catch (Exception e) {
        //Do nothing
    }

    String environment = pipelineData.bmxStructure.environment.toUpperCase()
    MaximoAbstractFallo fallo = new MaximoFalloCloudBuildDocker(environment, log, buildLog)

    boolean weShouldCreateMaximo = ( ! env.deployBuildDockerImageFailureMaximoHasBeenCreated ) && weShouldCreateMaximoAccordingToEnvironment(pipelineData)
    //FIXME: Desactivamos maximos en EDEN hasta solucionar el problema de los IDs de despliegue repetidos
    weShouldCreateMaximo = weShouldCreateMaximo && ! pipelineData.deployStructure.env.equalsIgnoreCase("eden")

    if (GlobalVars.KPILOGGER_IS_NOW_ACTIVE) {

        printOpen("Ibamos a crear un máximo por fallo al hacer build de imagen docker pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último. No crearemos un máximo", EchoLevel.INFO)
        GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

        throw new Exception("Ibamos a crear un máximo por fallo al hacer build de imagen docker pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último")

    } else if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(pipelineData, pomXml))
                .tipoFallo(fallo)

        env.deployBuildDockerImageFailureMaximoHasBeenCreated = crearMaximo(request)

        throw new Exception(validateAndReplace(fallo.pipelineExceptionErrorMessage))

    } else if (!"${env.ALM_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()) {

        throw new Exception("Se ha producido un error. Si el error persiste abrid máximo al Servicio TI: ${fallo.equipoResponsable.servicioTI}")

    } else {
        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)

        throw new Exception("Ibamos a crear un máximo por fallo al hacer build de imagen docker pero ya se ha creado")
    }

}

def gplRequestException(PipelineData pipelineData, def pomXmlOrIClientInfo, def responseOrException, String httpMethod, String url, String body) {

    String log = getLogFromObject(responseOrException)

    if (pipelineData == null) {
        throw new Exception(
            "Unexpected exception when sending request to GPL:\n" +
                "${log}\n\n" +
                "La llamada ha sido un ${httpMethod} a la URI ${url} con el siguiente body:\n\n" +
                "${body}\n\n" +
                "Crear Maximo a GPL en Incidencias, Maximo: Servicio TI: APLICACION \\ HERRAMIENTAS \\ IDEGPL.PCLD")
    }

    String applicationName = pomXmlOrIClientInfo instanceof PomXmlStructure ?
        ((PomXmlStructure) pomXmlOrIClientInfo).getArtifactName() :
        ((IClientInfo) pomXmlOrIClientInfo).getArtifactId()

    String stageName = url.substring(url.lastIndexOf("/") + 1)

    MaximoAbstractFallo fallo = new MaximoFalloGPLRequest(applicationName, stageName, httpMethod, url, body, log)

    boolean weShouldCreateMaximo = ( ! env.gplRequestMaximoHasBeenCreated ) && weShouldCreateMaximoAccordingToEnvironment(pipelineData)

    if (GlobalVars.KPILOGGER_IS_NOW_ACTIVE) {

        printOpen("Ibamos a crear un máximo por fallo en GPL pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último. No crearemos un máximo", EchoLevel.INFO)
        GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

        throw new Exception("Ibamos a crear un máximo por fallo en GPL pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último")

    } else if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(pipelineData, pomXmlOrIClientInfo))
                .tipoFallo(fallo)

        env.gplRequestMaximoHasBeenCreated = crearMaximo(request)

        throw new Exception(validateAndReplace(fallo.pipelineExceptionErrorMessage))

    } else if (!"${env.ALM_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()) {

        throw new Exception("Se ha producido un error. Si el error persiste abrid máximo al Servicio TI: ${fallo.equipoResponsable.servicioTI}")

    } else {
        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)

        throw new Exception("Ibamos a crear un máximo por fallo en GPL pero ya se ha creado")
    }

}

def gplRequestException(String artifactGarAppName, String artifactGarType, def responseOrException, String httpMethod, String url, String body) {

    String log = getLogFromObject(responseOrException)

    if (artifactGarAppName == null || artifactGarType == null) {
        throw new Exception(
            "Unexpected exception when sending request to GPL:\n" +
                "${log}\n\n" +
                "La llamada ha sido un ${httpMethod} a la URI ${url} con el siguiente body:\n\n" +
                "${body}\n\n" +
                "Crear Maximo a GPL en Incidencias, Maximo: Servicio TI: APLICACION \\ HERRAMIENTAS \\ IDEGPL.PCLD")
    }

    String applicationName = artifactGarAppName
    String stageName = url.substring(url.lastIndexOf("/") + 1)

    MaximoAbstractFallo fallo = new MaximoFalloGPLRequest(applicationName, stageName, httpMethod, url, body, log)

    boolean weShouldCreateMaximo = ( ! env.gplRequestMaximoHasBeenCreated )

    if (GlobalVars.KPILOGGER_IS_NOW_ACTIVE) {

        printOpen("Ibamos a crear un máximo por fallo en GPL pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último. No crearemos un máximo", EchoLevel.INFO)
        GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

        throw new Exception("Ibamos a crear un máximo por fallo en GPL pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último")

    } else if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(artifactGarAppName, artifactGarType))
                .tipoFallo(fallo)

        env.gplRequestMaximoHasBeenCreated = crearMaximo(request)

        throw new Exception(validateAndReplace(fallo.pipelineExceptionErrorMessage))

    } else if (!"${env.ALM_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()) {

        throw new Exception("Se ha producido un error. Si el error persiste abrid máximo al Servicio TI: ${fallo.equipoResponsable.servicioTI}")

    } else {
        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)

        throw new Exception("Ibamos a crear un máximo por fallo en GPL pero ya se ha creado")
    }

    
}

def kpiLoggerRequestFailureButDoNotThrowException(KpiData kpiData, def httpResponseOrException) {

    PipelineData pipelineData = kpiData?.pipelineData
    PomXmlStructure pomXml = kpiData?.pomXml

    String log = getLogFromObject(httpResponseOrException)
    MaximoAbstractFallo fallo = new MaximoFalloELKKpiLogger(kpiData, log)

    boolean weShouldCreateMaximo = ( ! env.kpiLoggerRequestFailureMaximoHasBeenCreated ) && weShouldCreateMaximoAccordingToEnvironment(pipelineData)

	/*
    if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(pipelineData, pomXml))
                .tipoFallo(fallo)

        // For now we have decided not to create KPI-related Maximos
        env.kpiLoggerRequestFailureMaximoHasBeenCreated = true
        //env.kpiLoggerRequestFailureMaximoHasBeenCreated = crearMaximo(request)

    } else {
        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)
    }*/

    printOpen(fallo.pipelineExceptionErrorMessage, EchoLevel.ERROR)
}

def sslEventualErrorWhileDoingITTest(PipelineData pipelineData, PomXmlStructure pomXml, MavenGoalExecutionFailureError error, String url) {

    def log = error.errors
    String environment = pipelineData.bmxStructure.environment.toUpperCase()
    MaximoAbstractFallo fallo = new MaximoFalloSSLErrorWhileDoingITTest(environment.toUpperCase(), url, log)

    boolean weShouldCreateMaximo = ( ! env.sslErrorOnITTestMaximoHasBeenCreated ) && weShouldCreateMaximoAccordingToEnvironment(pipelineData)

    if (GlobalVars.KPILOGGER_IS_NOW_ACTIVE) {

        printOpen("Ibamos a crear un máximo por fallo SSL durante test de integración pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último. No crearemos un máximo", EchoLevel.INFO)
        GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

        throw new Exception("Ibamos a crear un máximo por fallo SSL durante test de integración pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último")

    } else if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(pipelineData, pomXml))
                .tipoFallo(fallo)

        env.sslErrorOnITTestMaximoHasBeenCreated = crearMaximo(request)

        throw new Exception(validateAndReplace(fallo.pipelineExceptionErrorMessage))

    } else if (!"${env.ALM_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()) {

        throw new Exception("Se ha producido un error. Si el error persiste abrid máximo al Servicio TI: ${fallo.equipoResponsable.servicioTI}")

    } else {
        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)

        throw new Exception("Ibamos a crear un máximo por fallo SSL durante test de integración pero ya se ha creado")
    }

}

def sslEventualErrorWhileExecutingMavenGoal(PipelineData pipelineData, PomXmlStructure pomXml, MavenGoalExecutionFailureError error) {

    def log = error.errors
    String environment = pipelineData.bmxStructure.environment.toUpperCase()
    MaximoAbstractFallo fallo = new MaximoFalloSSLErrorWhileExecutingMavenGoal(environment.toUpperCase(), log)

    boolean weShouldCreateMaximo = ( ! env.sslErrorOnMavenGoalMaximoHasBeenCreated ) && weShouldCreateMaximoAccordingToEnvironment(pipelineData)

    if (GlobalVars.KPILOGGER_IS_NOW_ACTIVE) {

        printOpen("Ibamos a crear un máximo por fallo SSL al ejecutar un goal de maven pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último. No crearemos un máximo", EchoLevel.INFO)
        GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

        throw new Exception("Ibamos a crear un máximo por fallo SSL al ejecutar un goal de maven pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último")

    } else if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(pipelineData, pomXml))
                .tipoFallo(fallo)

        env.sslErrorOnMavenGoalMaximoHasBeenCreated = crearMaximo(request)

        throw new Exception(validateAndReplace(fallo.pipelineExceptionErrorMessage))

    } else if (!"${env.ALM_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()) {

        throw new Exception("Se ha producido un error. Si el error persiste abrid máximo al Servicio TI: ${fallo.equipoResponsable.servicioTI}")

    } else {
        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)

        throw new Exception("Ibamos a crear un máximo por fallo SSL al ejecutar un goal de maven pero ya se ha creado")
    }

    

}

def cloudSecretsVerificationFailed(PipelineData pipelineData, PomXmlStructure pomXml, String secret, String cloudEnvironment, String url, CloudApiResponse cloudApiResponse) {

    MaximoAbstractFallo fallo = new MaximoFalloCloudSecretsVerificationFailed(secret, cloudEnvironment, url, cloudApiResponse)

    boolean weShouldCreateMaximo = ( ! env.cloudSecretsVerificationFailedMaximoHasBeenCreated ) && weShouldCreateMaximoAccordingToEnvironment(pipelineData)

    if (GlobalVars.KPILOGGER_IS_NOW_ACTIVE) {

        printOpen("Ibamos a crear un máximo por fallo al verificar secretos Cloud pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último. No crearemos un máximo", EchoLevel.INFO)
        GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

        throw new Exception("Ibamos a crear un máximo por fallo al verificar secretos Cloud pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último")

    } else if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(pipelineData, pomXml))
                .tipoFallo(fallo)

        env.cloudSecretsVerificationFailedMaximoHasBeenCreated = crearMaximo(request)

        throw new Exception(validateAndReplace(fallo.pipelineExceptionErrorMessage))

    } else if (!"${env.ALM_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()) {

        throw new Exception("Se ha producido un error. Si el error persiste abrid máximo al Servicio TI: ${fallo.equipoResponsable.servicioTI}")

    } else {
        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)

        throw new Exception("Ibamos a crear un máximo por fallo al verificar secretos Cloud pero ya se ha creado")
    }

    

}

def genericFailureWhileDoingActuatorRefresh(PipelineData pipelineData, PomXmlStructure pomXml, String url, Exception e) {

    def log = e.getMessage()
    String environment = pipelineData.bmxStructure.environment.toUpperCase()
    MaximoAbstractFallo fallo = new MaximoFalloGenericFailureWhenDoingRefresh(environment.toUpperCase(), url, log)

    boolean weShouldCreateMaximo = ( ! env.genericErrorOnActuatorRefreshMaximoHasBeenCreated ) && weShouldCreateMaximoAccordingToEnvironment(pipelineData)

    if (GlobalVars.KPILOGGER_IS_NOW_ACTIVE) {

        printOpen("Ibamos a crear un máximo por fallo al invocar actuator/refresh pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último. No crearemos un máximo", EchoLevel.INFO)
        GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

        throw new Exception("Ibamos a crear un máximo por fallo al invocar actuator/refresh pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último")

    } else if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(pipelineData, pomXml))
                .tipoFallo(fallo)

        env.genericErrorOnActuatorRefreshMaximoHasBeenCreated = crearMaximo(request)

        throw new Exception(validateAndReplace(fallo.pipelineExceptionErrorMessage))

    } else if (!"${env.ALM_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()) {

        throw new Exception("Se ha producido un error. Si el error persiste abrid máximo al Servicio TI: ${fallo.equipoResponsable.servicioTI}")

    } else {
        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)

        throw new Exception("Ibamos a crear un máximo por fallo al invocar actuator/refresh pero ya se ha creado")
    }

}

def nexusDownloadExceptionDueToAConnectivityIssue(PipelineData pipelineData, PomXmlStructure pomXml, String log) {

    MaximoAbstractFallo fallo = new MaximoFalloConnectivityErrorWhileDownloadingFromNexus(log)

    boolean weShouldCreateMaximo =
        ( ! env.nexusDownloadExceptionDueToAConnectivityIssueMaximoHasBeenCreated ) &&
            weShouldCreateMaximoAccordingToEnvironment(pipelineData)

    if (GlobalVars.KPILOGGER_IS_NOW_ACTIVE) {

        printOpen("Ibamos a crear un máximo por fallo al descargar artefactos de Artifactory pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último. No crearemos un máximo", EchoLevel.INFO)
        GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

        throw new Exception("Ibamos a crear un máximo por fallo al descargar artefactos de Artifactory pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último")

    } else if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(pipelineData, pomXml))
                .tipoFallo(fallo)

        env.nexusDownloadExceptionDueToAConnectivityIssueMaximoHasBeenCreated = crearMaximo(request)

        throw new Exception(validateAndReplace(fallo.pipelineExceptionErrorMessage))

    } else if (!"${env.ALM_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()) {

        throw new Exception("Se ha producido un error. Si el error persiste abrid máximo al Servicio TI: ${fallo.equipoResponsable.servicioTI}")

    } else {
        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)

        throw new Exception("Ibamos a crear un máximo por fallo al descargar artefactos de Artifactory pero ya se ha creado")
    }

    
}

def nexusDownloadExceptionDueToANonPresentPluginDependencyThatWasPreviouslyOnNexus(PipelineData pipelineData, PomXmlStructure pomXml, String log) {

    MaximoAbstractFallo fallo = new MaximoFalloDependencyPluginPreviouslyPresentNotFoundInNexus(log)

    boolean weShouldCreateMaximo =
        ( ! env.nexusDownloadExceptionDueToANonPresentPluginDependencyThatWasPreviouslyOnNexusMaximoHasBeenCreated ) &&
            weShouldCreateMaximoAccordingToEnvironment(pipelineData)

    if (GlobalVars.KPILOGGER_IS_NOW_ACTIVE) {

        printOpen("Ibamos a crear un máximo por fallo al descargar plugins de Artifactory pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último. No crearemos un máximo", EchoLevel.INFO)
        GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

        throw new Exception("Ibamos a crear un máximo por fallo al descargar plugins de Artifactory pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último")

    } else if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(pipelineData, pomXml))
                .tipoFallo(fallo)

        env.nexusDownloadExceptionDueToANonPresentPluginDependencyThatWasPreviouslyOnNexusMaximoHasBeenCreated = crearMaximo(request)

        throw new Exception(validateAndReplace(fallo.pipelineExceptionErrorMessage))

    } else if (!"${env.ALM_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()) {

        throw new Exception("Se ha producido un error. Si el error persiste abrid máximo al Servicio TI: ${fallo.equipoResponsable.servicioTI}")

    } else {
        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)

        throw new Exception("Ibamos a crear un máximo por fallo al descargar plugins de Artifactory pero ya se ha creado")
    }

    
}


def validateAndReplace(String message){
	boolean openMaximo = "${env.ALM_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()
	
	if (openMaximo) {
		return newMessage
	} else {
		String newMessage=message.replaceAll('Se ha abierto un Máximo automáticamente ','Se ha producido un error si el error persiste abrid máximo ')
		newMessage=newMessage-"Si no se ha abierto la incidencia automáticamente en una pipeline ejecutada al hacer un git push, "
		newMessage=newMessage-"la causa podría ser que la dirección de e-mail configurada en su configuración local de git no se corresponde con la dada de alta en Máximo."
		return newMessage
	}
} 

def sonarScanExceptionDueToAConnectivityIssue(PipelineData pipelineData, PomXmlStructure pomXml, String log) {

    MaximoAbstractFallo fallo = new MaximoFalloSonarConnectivityIssue(log)

    boolean weShouldCreateMaximo =
        ( ! env.sonarScanExceptionDueToAConnectivityIssueMaximoHasBeenCreated ) &&
            weShouldCreateMaximoAccordingToEnvironment(pipelineData)

    if (GlobalVars.KPILOGGER_IS_NOW_ACTIVE) {

        printOpen("Ibamos a crear un máximo por fallo SonarScan por problemas de conectividad pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último. No crearemos un máximo", EchoLevel.INFO)
        GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

        throw new Exception("Ibamos a crear un máximo por fallo SonarScan por problemas de conectividad pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último")

    } else if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(pipelineData, pomXml))
                .tipoFallo(fallo)

        env.sonarScanExceptionDueToAConnectivityIssueMaximoHasBeenCreated = crearMaximo(request)

        throw new Exception(validateAndReplace(fallo.pipelineExceptionErrorMessage))

    } else if (!"${env.ALM_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()) {

        throw new Exception("Se ha producido un error. Si el error persiste abrid máximo al Servicio TI: ${fallo.equipoResponsable.servicioTI}")

    } else {
        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)

        throw new Exception("Ibamos a crear un máximo por fallo SonarScan por problemas de conectividad pero ya se ha creado")
    }

}    

def waitForQualityGateException(PipelineData pipelineData, PomXmlStructure pomXml, String log) {
    MaximoAbstractFallo fallo = new MaximoFalloWaitForQualityGate(log)

    boolean weShouldCreateMaximo = ( ! env.sonarQualityGateExceptionMaximoHasBeenCreated ) && weShouldCreateMaximoAccordingToEnvironment(pipelineData)

    if (GlobalVars.KPILOGGER_IS_NOW_ACTIVE) {

        printOpen("Ibamos a crear un máximo por fallo al esperar al Sonar Quality Gate pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último. No crearemos un máximo", EchoLevel.INFO)
        GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

        throw new Exception("Ibamos a crear un máximo por fallo al esperar al Sonar Quality Gate pero hemos detectado que el logger de KPIs estaba en ejecución por lo que entendemos que el fallo estaba en este último")

    } else if (weShouldCreateMaximo) {

        MaximoCreationRequest request =
            new MaximoCreationRequest()
                .emailUsuarioCreador(idecuaRoutingUtils.getUsuarioEmailFromPipelineMetadata(pipelineData, pomXml))
                .tipoFallo(fallo)

        env.sonarQualityGateExceptionMaximoHasBeenCreated = crearMaximo(request)

        throw new Exception(validateAndReplace(fallo.pipelineExceptionErrorMessage))

    } else if (!"${env.ALM_SERVICES_MAXIMO_INCIDENTS_REGISTRATION_ENABLED}".toBoolean()) {

        throw new Exception("Se ha producido un error. Si el error persiste abrid máximo al Servicio TI: ${fallo.equipoResponsable.servicioTI}")

    } else {
        printOpen("Ya se ha creado Máximo para este caso de uso en esta pipeline. No lo haremos de nuevo", EchoLevel.INFO)

        throw new Exception("Ibamos a crear un máximo por fallo al esperar al Sonar Quality Gate pero ya se ha creado")
    }
    
}

boolean weShouldCreateMaximoAccordingToEnvironment(PipelineData pipelineData) {
	//Desconectamsos los incidentes
	return ! (pipelineData.pipelineDataExecutionMode instanceof PipelineCompleteTestExecutionMode)
}

private retrieveBuildLogIfAvailable(PipelineData pipeline, PomXmlStructure pomXml, String buildId) {

    String buildLog = null

    try {

        if (buildId) {

            def cloudAppMetadata = CloudUtils.calculateCloudComponentName(pipeline, pomXml)
            def nameComponentInCloud = cloudAppMetadata.cloudComponentName

            CloudApiResponse buildLogResponse = getBuildLog(nameComponentInCloud, pomXml.getCloudAppName(), buildId)

            if (buildLogResponse.statusCode>=200 && buildLogResponse.statusCode<300) {

                if (buildLogResponse.body!=null && buildLogResponse.body.size()>=1) {

                    try {

                        buildLog = buildLogResponse.body.log.replace("\\r", "\r").replace("\\n", "\n")

                    } catch(Exception e) {

                        printOpen("The JSON containing the Cloud build log couldn't be parsed. Raw data will be used.", EchoLevel.ERROR)
                        printOpen(Utilities.prettyException(e, true), EchoLevel.ERROR)
                        buildLog = buildLogResponse.body

                    }

                } else {

                    buildLog = "<Build log not found>"

                }

            }

        }

    } catch (Exception e) {

        printOpen("There was a problem while retrieving the Cloud build log", EchoLevel.ERROR)
        printOpen(Utilities.prettyException(e, true), EchoLevel.ERROR)

    }

    String currentDate = Utilities.getActualDate("yyyyMMddHHmmss")
    String outputFilename = "${env.WORKSPACE}@tmp/pipelineLogs/${currentDate}dockerImageMavenBuild.log"
    writeFile file: outputFilename, text: "${buildLog}"

    return buildLog
}

String getJsonFromObject(def input) {

    if ( ! input ) {
        return "(La petición no tenía body)"
    }

    try {

        return groovy.json.JsonOutput.toJson(input)

    } catch (Exception e) {

        return "(El body no era serializable)"

    }

}

private String getLogFromObject(def responseOrException) {

    if ( ! responseOrException ) {

        return "(No se proporcionó log)"

    } else if (responseOrException instanceof CloudApiResponse) {

        return getDeployErrorMessageFromCloud(responseOrException)

    } else if (responseOrException instanceof Throwable) {

        return Functions.printThrowable(responseOrException)

    } else {

        return responseOrException.toString()

    }

}
