import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.Strings

def call(def pomXmlOrIClientInfo, PipelineData pipelineData, String stageId, Exception e) {
	sendStageEndToGPL(pomXmlOrIClientInfo, pipelineData, stageId, Strings.toHtml(e.getMessage()), null, "error")
	throw e
}

def call(def pomXmlOrIClientInfo, PipelineData pipelineData, String stageId, String message) {
    sendStageEndToGPL(pomXmlOrIClientInfo, pipelineData, stageId, Strings.toHtml(message), null, "error")
    error "${message}"
}
