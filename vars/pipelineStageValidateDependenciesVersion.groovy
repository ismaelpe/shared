import com.project.alm.EchoLevel
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.ArtifactSubType


def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId) {

    sendStageStartToAppPortal(pomXmlStructure, pipelineData, stageId)
	
	def status=""
	def warning=""

    try {

        validateDependenciesVersion(pomXmlStructure, pipelineData)
		
		if (pipelineData.deployFlag==true && pomXmlStructure.artifactSubType == ArtifactSubType.MICRO_APP) {
			printOpen("Validating majors installed", EchoLevel.ALL)
			//Falta el env

			status=validateMicrosMajor(pipelineData,pomXmlStructure)
			
			if (!"true".equals(status)) {
				String disableValidateMajor = "${env.ALM_SERVICES_SKIP_MAJOR_CONTROL}"
				if (disableValidateMajor!=null && disableValidateMajor=="false") {
					throw new Exception(status)
				}else {
					//Es un warning no un error
					warning=status
				}			
			}
		}

    } catch (Exception e) {
		printOpen("Error validating dependencies version: ${e.getMessage()}", EchoLevel.ERROR)
		sendStageEndToAppPortal(pomXmlStructure, pipelineData, stageId, null, null, "error")
        throw e

    }
    if (warning=="") {
		sendStageEndToAppPortal(pomXmlStructure, pipelineData, stageId)
	}else {
		sendStageEndToAppPortal(pomXmlStructure, pipelineData, stageId, warning, null, 'warning')
    }
    

}
