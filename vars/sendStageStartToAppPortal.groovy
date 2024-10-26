import com.project.alm.GlobalVars
import com.project.alm.EchoLevel
import com.project.alm.PipelineData

def call(def pomXmlOrIClientInfo, PipelineData pipelineData, String stageId) {

    if (notificationToAppPortalApplies()) {
    
        if(env.sendLogsToAppPortal) GlobalVars.STAGE_LOGS = ""

		stageId = pipelineData?.pipelineStructure?.pipelineId+stageId
		
		if(env.logsReport) {
            String stageName = pipelineData?.pipelineStructure?.getStageName(stageId)
            GlobalVars.PIPELINE_LOGS += "\n######### STAGE ${stageName} #########\n"
        }
		
        printOpen("Sending to AppPortal a Pipeline Stage Start Operation: ${stageId}", EchoLevel.DEBUG)

        def url = idecuaRoutingUtils.stageUrl(stageId)
        def body = [
            id         : stageId,
            estado     : "running",
            fechaInicio: new Date(),
        ]

        def response = sendRequestToAppPortal('PUT', url, "", body, pipelineData, pomXmlOrIClientInfo)

        return response
    }
}

def call(PipelineData pipelineData, String garAppType, String garAppName, String stageId) {

    if (notificationToAppPortalApplies()) {
    
        if(env.sendLogsToAppPortal) GlobalVars.STAGE_LOGS = ""

        stageId = pipelineData?.pipelineStructure?.pipelineId+stageId

        if(env.logsReport) {
            String stageName = pipelineData?.pipelineStructure?.getStageName(stageId)
            GlobalVars.PIPELINE_LOGS += "\n######### STAGE ${stageName} #########\n"
        }

        printOpen("Sending to AppPortal a Pipeline Stage Start Operation: ${stageId}", EchoLevel.DEBUG)

        def url = idecuaRoutingUtils.stageUrl(stageId)
        def body = [
            id         : stageId,
            estado     : "running",
            fechaInicio: new Date(),
        ]

        def response = sendRequestToAppPortal('PUT', url, "", body, garAppName, garAppType)

        return response
    }
}
