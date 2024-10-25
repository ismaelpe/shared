import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData


def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId) {

    sendStageStartToGPL(pomXmlStructure, pipelineData, stageId)

    try {

        validateForbiddenDependencyRestrictions(pomXmlStructure, pipelineData)

    } catch (Exception e) {

        sendStageEndToGPLAndThrowError(pomXmlStructure, pipelineData, stageId, e)

    }

    sendStageEndToGPL(pomXmlStructure, pipelineData, stageId)

}
