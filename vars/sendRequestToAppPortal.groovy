import com.project.alm.*

def call(String method, String url, String param, def body, PipelineData pipelineData, PomXmlStructure pomXml) {
    try {
        sendRequestToService(method, url, param, body,
            [
                validResponseCodes: '200',
                kpiAlmEvent: new KpiAlmEvent(
                    pomXml, pipelineData,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.AppPortal_HTTP_CALL),
                echoLevel: EchoLevel.ALL
            ])
    } catch (Exception e) {
        def request = sendRequestToService.sanitizeRequest(createMaximoAndThrow.getJsonFromObject(body))
        createMaximoAndThrow.appPortalRequestException(pipelineData, pomXml, e, method, url+param, request)
    }
}

def call(String method, String url, String param, def body, PipelineData pipelineData, IClientInfo clientInfo) {
    try {
        sendRequestToService(method, url, param, body,
            [
                validResponseCodes: '200',
                kpiAlmEvent: new KpiAlmEvent(
                    clientInfo, pipelineData,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.AppPortal_HTTP_CALL),
                echoLevel: EchoLevel.ALL
            ])
    } catch (Exception e) {
        def request = sendRequestToService.sanitizeRequest(createMaximoAndThrow.getJsonFromObject(body))
        createMaximoAndThrow.appPortalRequestException(pipelineData, clientInfo, e, method, url+param, request)
    }
}

def call(String method, String url, String param, def body, String artifactGarAppName, String artifactGarType) {
    try {
        sendRequestToService(method, url, param, body,
            [
                validResponseCodes: '200',
                kpiAlmEvent: new KpiAlmEvent(
                    null, null,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.AppPortal_HTTP_CALL),
                echoLevel: EchoLevel.ALL
            ])

    } catch (Exception e) {
        def request = sendRequestToService.sanitizeRequest(createMaximoAndThrow.getJsonFromObject(body))
        createMaximoAndThrow.appPortalRequestException(artifactGarAppName, artifactGarType, e, method, url+param, request)
    }
}
