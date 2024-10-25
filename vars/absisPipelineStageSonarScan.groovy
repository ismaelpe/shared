import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.EchoLevel

def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId) {
    if (skipSonarFeature(pomXmlStructure,pipelineData)) {
		printOpen("Sonar disabled in this feature ${pipelineData.branchStructure}",EchoLevel.INFO)
	}else {
		sendStageStartToGPL(pomXmlStructure, pipelineData, stageId)
		
			def whiteListDataServicesAllowedClients = "${env.ABSIS3_DATA_SERVICES_ALLOWED_ANY_CLIENT}".split(";")
			//step must be after build in order to check json dependencies
			validateDependenciesForDataServices(pomXmlStructure, pipelineData, whiteListDataServicesAllowedClients)
			try {
		
					   
				boolean sonarScanDisabled =
					"${env.ABSIS3_SERVICES_SKIP_SONAR_SCAN_LIST}".contains(pomXmlStructure.artifactName) ||
					'true'.equals("${env.ABSIS3_SERVICES_SKIP_SONAR_SCAN_ALL}".toString())
				
					printOpen("Sonar Scan starting... ${pipelineData.branchStructure.branchType} ${sonarScanDisabled} XXX${env.ABSIS3_SERVICES_SKIP_SONAR_SCAN_ALL}XXX", EchoLevel.DEBUG)
					
				if (sonarScanDisabled) {
		
					printOpen("Sonar Quality Gate will not be executed as ${pomXmlStructure.artifactName} is whitelisted or Sonar Scan is globally disabled",EchoLevel.INFO)
		
					sendStageEndToGPL(pomXmlStructure, pipelineData, stageId, "Sonar Scan was skipped due to current configuration settings", null, "warning")
		
				} else {
					sonarScanWorkspace(pomXmlStructure, pipelineData)
					sendStageEndToGPL(pomXmlStructure, pipelineData, stageId)
		
				}
		
			} catch(Exception err) {
		
				sendStageEndToGPL(pomXmlStructure, pipelineData, stageId, "Warning stage sonar-scan technical error", null, "error")
				
				throw err
		
			}		
				
	}


}
