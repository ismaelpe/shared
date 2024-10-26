import com.project.alm.PipelineData
import com.project.alm.Strings

def call(def pomXmlOrIClientInfo, PipelineData pipelineData, String stageId, Exception e) {
	sendStageEndToAppPortal(pomXmlOrIClientInfo, pipelineData, stageId, Strings.toHtml(e.getMessage()), null, "error")
	throw e
}

def call(def pomXmlOrIClientInfo, PipelineData pipelineData, String stageId, String message) {
    sendStageEndToAppPortal(pomXmlOrIClientInfo, pipelineData, stageId, Strings.toHtml(message), null, "error")
    error "${message}"
}
