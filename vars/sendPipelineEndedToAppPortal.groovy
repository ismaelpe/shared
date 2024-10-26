import com.project.alm.GlobalVars
import com.project.alm.EchoLevel
import com.project.alm.PipelineData

def call(boolean initAppPortal, def pomXmlOrClientInfo, PipelineData pipelineData, boolean success) {

    def estadoFinal = success ? "ended" : "error"
    sendPipelineEnding(initAppPortal, pomXmlOrClientInfo, pipelineData, estadoFinal)

}

def call(boolean initAppPortal, def pomXmlOrClientInfo, PipelineData pipelineData, String estadoFinal) {

    sendPipelineEnding(initAppPortal, pomXmlOrClientInfo, pipelineData, estadoFinal)

}

def call(boolean initAppPortal, PipelineData pipelineData, String garAppName, String garAppType, boolean success) {

    def estadoFinal = success ? "ended" : "error"
    sendPipelineEnding(initAppPortal, pipelineData, garAppName, garAppType, estadoFinal)

}

private sendPipelineEnding(boolean initAppPortal, def pomXmlOrClientInfo, PipelineData pipelineData, String estadoFinal) {

    if (initAppPortal && notificationToAppPortalApplies()) {

        printOpen("Sending to AppPortal a Pipeline End Operation", EchoLevel.DEBUG)
        def url = idecuaRoutingUtils.pipelineUrlById(pipelineData.pipelineStructure.pipelineId)
        url = url + pipelineData.buildCode
        def body = [
            estado    : "${estadoFinal}",
            fechaFinal: new Date(),
            userId    : "${pipelineData.pushUser}",
            testData  : "${pipelineData.testData}"
        ]

        def response = sendRequestToAppPortal('PUT', url, "", body, pipelineData, pomXmlOrClientInfo)
        
        if(env.logsReport) echo "##################### LOGS #####################\n${GlobalVars.PIPELINE_LOGS}################################################"

        return response

    } else {
        printOpen("Not send the end of pipeline to AppPortal", EchoLevel.DEBUG)
    }

}

private sendPipelineEnding(boolean initAppPortal, PipelineData pipelineData, String garAppType, String garAppName, String estadoFinal) {

    if (initAppPortal && notificationToAppPortalApplies()) {
        printOpen("Sending to AppPortal a Pipeline End Operation", EchoLevel.DEBUG)
        def url = idecuaRoutingUtils.pipelineUrlById(pipelineData.pipelineStructure.pipelineId)
        url = url + pipelineData.buildCode
        def body = [
            estado    : "${estadoFinal}",
            fechaFinal: new Date()
        ]

        def response = sendRequestToAppPortal('PUT', url, "", body, garAppName, garAppType)
        
        if(env.logsReport) echo "##################### LOGS #####################\n${GlobalVars.PIPELINE_LOGS}################################################"

        return response

    } else {
        printOpen("Not send the end of pipeline to AppPortal", EchoLevel.DEBUG)
    }

}