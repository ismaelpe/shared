import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.TrazabilidadGPLType
import com.caixabank.absis3.AuthorizationServiceToInformType

def call(boolean initGpl, def pomXmlOrIClientInfo, PipelineData pipelineData, boolean success) {

	// TrazabilidadGPLType.NADA.toString()
	
	printOpen("La trazabilidad es de ${pipelineData.pipelineStructure.resultPipelineData.getDeployed()}", EchoLevel.ALL)
	
    if (initGpl && notificationToGplApplies() && !TrazabilidadGPLType.NADA.toString().equals(pipelineData.pipelineStructure.resultPipelineData.getDeployed())) {

        printOpen("Sending to GPL a Pipeline Result operation", EchoLevel.DEBUG)

        def url = idecuaRoutingUtils.pipelineDeployUrl(pipelineData.pipelineStructure.pipelineId)
        def body = pipelineData.pipelineStructure.resultPipelineData.getTrazabilidad(success, this)
        body.acciones = getDeployActions(pipelineData, pomXmlOrIClientInfo)

        sendRequestToGpl('POST', url, "", body, pipelineData, pomXmlOrIClientInfo)		
	

    } else {
        
		printOpen("Not send notify to GPL", EchoLevel.DEBUG)

	}
}

private String getDeployActions(PipelineData pipelineData, def pomXmlOrIClientInfo) {
    printOpen("Preparing deploy actions", EchoLevel.DEBUG)
    if (!pomXmlOrIClientInfo.isMicro()){
         return []
    } else {
         return [
		getStartAction(pipelineData, pomXmlOrIClientInfo),
		getStopAction(pipelineData, pomXmlOrIClientInfo),
		getRestartAction(pipelineData, pomXmlOrIClientInfo),
		getLitmidAction(pipelineData, pomXmlOrIClientInfo),
        getResizeAction(pipelineData, pomXmlOrIClientInfo)
	]	
    }
}

private def getStartAction(PipelineData pipelineData, def pomXmlOrIClientInfo) {
    printOpen("Creating Start Action", EchoLevel.DEBUG)
    def startAction = getStartStopAction(pipelineData, pomXmlOrIClientInfo, "Iniciar aplicación", "START");
    return startAction
}

private def getStopAction(PipelineData pipelineData, def pomXmlOrIClientInfo) {
    printOpen("Creating Stop Action", EchoLevel.DEBUG)
    def stopAction = getStartStopAction(pipelineData, pomXmlOrIClientInfo, "Parar aplicación", "STOP");
    return stopAction
}

private def getStartStopAction(PipelineData pipelineData, def pomXmlOrIClientInfo, String name, String action) {
    printOpen("Generating startstop common data", EchoLevel.DEBUG)
    return [
        nombre                           : name,
        authorizationService             : getAuthorization(pipelineData),
        envAuthorization                 : pipelineData.bmxStructure.environment,
        tipoAccion                       : "LANZAR_JOB",
        destino                          : GlobalVars.ALM_JOB_START_STOP,
        actionInTestingPipeline          : true,
        actionWhenHotfixInTestingPipeline: true,
        canBeDisabled                    : true,
        serverUrl                        : "${env.JNKMSV_DEVPORTAL_URL}",
        jenkinsUserId                    : "${JNKMSV_USR}",
        jenkinsUserToken                 : "${JNKMSV_PSW}",
        parametros                       :
            [
                [
                    nombre: "appnameParam",
                    valor : pomXmlOrIClientInfo.getApp(pipelineData.garArtifactType).toUpperCase() + pomXmlOrIClientInfo.majorVersion
                ],
                [
                    nombre: "environmentParam",
                    valor : pipelineData.bmxStructure.environment.toUpperCase()
                ],
                [
                    nombre: "namespaceParam",
                    valor : pipelineData.pipelineStructure.resultPipelineData.artifactSubType.toString() == "MICRO_ARCH" ? "ARCH" : "APP"
                ],
                [
                    nombre: "centerParam",
                    valor : "ALL"
                ],
                [
                    nombre: "stableOrNewParam",
                    valor : "BOTH"
                ],
                [
                    nombre: "actionParam",
                    valor : action
                ],
                [
                    nombre: "garAppnameParam",
                    valor : pomXmlOrIClientInfo.getApp(pipelineData.garArtifactType).toUpperCase()
                ],
                [
                    nombre: "scaleCPUCoresParam",
                    valor : "NO"
                ],
                [
                    nombre: "scaleMemoryParam",
                    valor : "NO"
                ],
                [
                    nombre: "scaleNumInstancesParam",
                    valor : "DEFAULT"
                ],
                [
                    nombre: "jvmConfig",
                    valor : ""
                ],
                [
                    nombre: "garTypeParam",
                    valor : pipelineData.garArtifactType.toString()
                ],
                [
                    nombre: "useProSizeParam",
                    valor : "no"
                ],
                [
                    nombre: "useCatalogSizeParam",
                    valor : "yes"
                ]
            ]
    ]
}


private def getRestartAction(PipelineData pipelineData, def pomXmlOrIClientInfo) {
    printOpen("Creating Restart Action", EchoLevel.DEBUG)
    return [
        nombre                           : "Reiniciar aplicación",
        authorizationService             : getAuthorization(pipelineData),
        envAuthorization                 : pipelineData.bmxStructure.environment,
        tipoAccion                       : "LANZAR_JOB",
        destino                          : GlobalVars.ALM_JOB_RESTART,
        actionInTestingPipeline          : true,
        actionWhenHotfixInTestingPipeline: true,
        canBeDisabled                    : true,
        serverUrl                        : "${env.JNKMSV_DEVPORTAL_URL}",
        jenkinsUserId                    : "${JNKMSV_USR}",
        jenkinsUserToken                 : "${JNKMSV_PSW}",
        parametros                       :
            [
                [
                    nombre: "componentNames",
                    valor : pomXmlOrIClientInfo.getApp(pipelineData.garArtifactType).toLowerCase() + pomXmlOrIClientInfo.majorVersion
                ],
                [
                    nombre: "color",
                    valor : "BOTH"
                ],
                [
                    nombre: "namespace",
                    valor : pipelineData.pipelineStructure.resultPipelineData.artifactSubType.toString() == "MICRO_ARCH" ? "AB3COR" : "AB3APP"
                ],
                [
                    nombre: "center",
                    valor : "ALL"
                ],
                [
                    nombre: "environment",
                    valor : pipelineData.bmxStructure.environment.toUpperCase()
                ]
            ]
    ]
}

private def getLitmidAction(PipelineData pipelineData, def pomXmlOrIClientInfo) {
    printOpen("Creating Litmid Action", EchoLevel.DEBUG)
    return [
        nombre                           : "Refrescar literales",
        authorizationService             : getAuthorization(pipelineData),
        envAuthorization                 : pipelineData.bmxStructure.environment,
        tipoAccion                       : "LANZAR_JOB",
        destino                          : GlobalVars.ALM_JOB_REFRESH_LITMID,
        actionInTestingPipeline          : true,
        actionWhenHotfixInTestingPipeline: true,
        canBeDisabled                    : true,
        serverUrl                        : "${env.JNKMSV_DEVPORTAL_URL}",
        jenkinsUserId                    : "${JNKMSV_USR}",
        jenkinsUserToken                 : "${JNKMSV_PSW}",
        parametros                       :
            [
                [
                    nombre: "typeAppParam",
                    valor : pipelineData.garArtifactType.toString()
                ],
                [
                    nombre: "artifactNameParam",
                    valor : pomXmlOrIClientInfo.artifactName
                ],
                [
                    nombre: "majorVersionParam",
                    valor : pomXmlOrIClientInfo.majorVersion
                ],
                [
                    nombre: "artifactTypeParam",
                    valor : pipelineData.pipelineStructure.resultPipelineData.artifactType
                ],
                [
                    nombre: "environmentParam",
                    valor : pipelineData.bmxStructure.environment.toLowerCase()
                ],
                [
                    nombre: "executionProfile",
                    valor : "DEFAULT"
                ],
                [
                    nombre: "targetAlmFolderParam",
                    valor : ""
                ]
            ]
    ]
}

private def getResizeAction(PipelineData pipelineData, def pomXmlOrIClientInfo) {
    printOpen("Generating resize action", EchoLevel.DEBUG)
    return [
        nombre                           : "Redimensionar aplicación",
        authorizationService             : AuthorizationServiceToInformType.MAXIMO.toString(),
        envAuthorization                 : "PRO",
        tipoAccion                       : "LANZAR_JOB",
        destino                          : GlobalVars.ALM_JOB_RESIZE,
        actionInTestingPipeline          : true,
        actionWhenHotfixInTestingPipeline: true,
        canBeDisabled                    : true,
        serverUrl                        : "${env.JNKMSV_DEVPORTAL_URL}",
        jenkinsUserId                    : "${JNKMSV_USR}",
        jenkinsUserToken                 : "${JNKMSV_PSW}",
        parametros                       :
            [
                [
                    nombre: "appnameParam",
                    valor : pomXmlOrIClientInfo.getApp(pipelineData.garArtifactType).toUpperCase() + pomXmlOrIClientInfo.majorVersion
                ],
                [
                    nombre: "environmentParam",
                    valor : pipelineData.bmxStructure.environment.toUpperCase()
                ],
                [
                    nombre: "namespaceParam",
                    valor : pipelineData.pipelineStructure.resultPipelineData.artifactSubType.toString() == "MICRO_ARCH" ? "ARCH" : "APP"
                ],
                [
                    nombre: "centerParam",
                    valor : "ALL"
                ],
                [
                    nombre: "stableOrNewParam",
                    valor : "BOTH"
                ],
                [
                    nombre: "garAppnameParam",
                    valor : pomXmlOrIClientInfo.getApp(pipelineData.garArtifactType).toUpperCase()
                ],
                [
                    nombre: "scaleCPUCoresParam",
                    valor : "M"
                ],
                [
                    nombre: "scaleMemoryParam",
                    valor : "M"
                ],
                [
                    nombre: "scaleNumInstancesParam",
                    valor : "M"
                ],
                [
                    nombre: "jvmConfig",
                    valor : ""
                ],
                [
                    nombre: "garTypeParam",
                    valor : pipelineData.garArtifactType.toString()
                ]
            ]
    ]
}

private def getAuthorization(PipelineData pipelineData) {
    printOpen("Getting authorization", EchoLevel.DEBUG)
    return (pipelineData.bmxStructure.environment != GlobalVars.PRO_ENVIRONMENT ? 
            AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString() : 
            AuthorizationServiceToInformType.MAXIMO.toString())
}
