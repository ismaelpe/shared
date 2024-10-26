import com.project.alm.EchoLevel
import com.project.alm.GlobalVars


import com.project.alm.BranchType
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.NexusUtils
import com.project.alm.GarAppType

def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId) {
    sendStageStartToGPL(pomXmlStructure, pipelineData, stageId)
    try {
        if (pomXmlStructure.contractVersion) {
            printOpen("Contract Version '$pomXmlStructure.contractVersion', pomArtifactVersion '$pomXmlStructure.artifactVersion'", EchoLevel.INFO)
            def contractVersionParts = pomXmlStructure.contractVersion.split("\\.")
            def pomVersionParts = pomXmlStructure.artifactVersion.split("\\.")
            
            if (contractVersionParts[0] == pomVersionParts[0]) {
                printOpen("It skips this stage because the 'contract.version' it's defined in property pom.xml, ALM will be configured to connect to remote contract", EchoLevel.INFO)
                sendStageEndToGPL(pomXmlStructure, pipelineData, stageId)
            } else {
                def message = "Contract Break deteted, check contract-version and artifact.version in pom.xml"
                printOpen(message, EchoLevel.ERROR)
                throw new Exception(message)
            }
        } else {
            if (skipValidateVersion(pomXmlStructure) || !pomXmlStructure.contractVersion) {
                printOpen("This artifact has a swagger contract => Revapi", EchoLevel.INFO)
                try {
                    validateVersion(pomXmlStructure, pipelineData)
                    sendStageEndToGPL(pomXmlStructure, pipelineData, stageId)
                } catch(Exception ex) {
                    printOpen("Version's validation failed, but the pipeline will continue", EchoLevel.ERROR)
                    sendStageEndToGPL(pomXmlStructure, pipelineData, stageId, "${GlobalVars.REVAPI_SKIP_VALIDATION}", null, 'warning')
                }
            } else {
                validateVersion(pomXmlStructure, pipelineData)
                sendStageEndToGPL(pomXmlStructure, pipelineData, stageId)
            }
        }
    } catch (Exception e) {
        sendStageEndToGPL(pomXmlStructure, pipelineData, stageId, null, null, "error")
        throw e
    }
}
