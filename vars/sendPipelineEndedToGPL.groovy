import com.project.alm.GlobalVars
import com.project.alm.EchoLevel
import com.project.alm.PipelineData

def call(boolean initGpl, def pomXmlOrClientInfo, PipelineData pipelineData, boolean success) {

    def estadoFinal = success ? "ended" : "error"
    sendPipelineEnding(initGpl, pomXmlOrClientInfo, pipelineData, estadoFinal)

}

def call(boolean initGpl, def pomXmlOrClientInfo, PipelineData pipelineData, String estadoFinal) {

    sendPipelineEnding(initGpl, pomXmlOrClientInfo, pipelineData, estadoFinal)

}

def call(boolean initGpl, PipelineData pipelineData, String garAppName, String garAppType, boolean success) {

    def estadoFinal = success ? "ended" : "error"
    sendPipelineEnding(initGpl, pipelineData, garAppName, garAppType, estadoFinal)

}

private sendPipelineEnding(boolean initGpl, def pomXmlOrClientInfo, PipelineData pipelineData, String estadoFinal) {

    if (initGpl && notificationToGplApplies()) {

        printOpen("Sending to GPL a Pipeline End Operation", EchoLevel.DEBUG)
        def url = idecuaRoutingUtils.pipelineUrlById(pipelineData.pipelineStructure.pipelineId)
        url = url + pipelineData.buildCode
        def body = [
            estado    : "${estadoFinal}",
            fechaFinal: new Date(),
            userId    : "${pipelineData.pushUser}",
            testData  : "${pipelineData.testData}"
        ]

        def response = sendRequestToGpl('PUT', url, "", body, pipelineData, pomXmlOrClientInfo)
        
        if(env.logsReport) echo "##################### LOGS #####################\n${GlobalVars.PIPELINE_LOGS}################################################"

        return response

    } else {
        printOpen("Not send the end of pipeline to GPL", EchoLevel.DEBUG)
    }

}

private sendPipelineEnding(boolean initGpl, PipelineData pipelineData, String garAppType, String garAppName, String estadoFinal) {

    if (initGpl && notificationToGplApplies()) {
        printOpen("Sending to GPL a Pipeline End Operation", EchoLevel.DEBUG)
        def url = idecuaRoutingUtils.pipelineUrlById(pipelineData.pipelineStructure.pipelineId)
        url = url + pipelineData.buildCode
        def body = [
            estado    : "${estadoFinal}",
            fechaFinal: new Date()
        ]

        def response = sendRequestToGpl('PUT', url, "", body, garAppName, garAppType)
        
        if(env.logsReport) echo "##################### LOGS #####################\n${GlobalVars.PIPELINE_LOGS}################################################"

        return response

    } else {
        printOpen("Not send the end of pipeline to GPL", EchoLevel.DEBUG)
    }

}