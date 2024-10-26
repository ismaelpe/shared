import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData


def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId) {

    sendStageStartToAppPortal(pomXmlStructure, pipelineData, stageId)

    try {

        validateForbiddenDependencyRestrictions(pomXmlStructure, pipelineData)

    } catch (Exception e) {

        sendStageEndToAppPortalAndThrowError(pomXmlStructure, pipelineData, stageId, e)

    }

    sendStageEndToAppPortal(pomXmlStructure, pipelineData, stageId)

}
