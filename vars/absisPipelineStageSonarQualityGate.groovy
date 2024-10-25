import com.project.alm.BranchType
import com.project.alm.EchoLevel
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.Strings
import com.project.alm.Utilities

def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId) {
	
	if (skipSonarFeature(pomXmlStructure,pipelineData)) {
		printOpen("Sonar disabled in this feature ${pipelineData.branchStructure}",EchoLevel.INFO)
	}else {
		sendStageStartToGPL(pomXmlStructure, pipelineData, stageId)
		boolean ifSonarQualityGateOK = false
	
		boolean sonarScanDisabled =
			"${env.ALM_SERVICES_SKIP_SONAR_SCAN_LIST}".contains(pomXmlStructure.artifactName) ||
				'true'.equals("${env.ALM_SERVICES_SKIP_SONAR_SCAN_ALL}".toString())
	
		boolean sonarQualityGateDisabled = "${env.ALM_SERVICES_SKIP_SONAR_QUALITY_GATE}".contains(pomXmlStructure.artifactName) ||
			'true'.equals("${env.ALM_SERVICES_SKIP_SONAR_QUALITY_GATE_ALL}".toString())
			
		printOpen("${env.ALM_SERVICES_SKIP_SONAR_QUALITY_GATE}", EchoLevel.DEBUG)
		
		printOpen("${env.ALM_SERVICES_SKIP_SONAR_QUALITY_GATE_ALL}", EchoLevel.DEBUG)
	
		if (sonarScanDisabled && ( ! sonarQualityGateDisabled ) ) {
			def msg = "It seems that Sonar Scan has been disabled globally or just for this component but Sonar Quality Gate is enabled." +
				"Please contact administrators to have this configurations mismatch solved"
			sendStageEndToGPL(pomXmlStructure, pipelineData, stageId, msg, null, "error")
			error msg
		}
	
		try {
	
			printOpen("Sonar Quality Gate the Workspace!!!. ${pipelineData.branchStructure.branchType}", EchoLevel.DEBUG)
	
			if (sonarQualityGateDisabled) {
	
				printOpen("Sonar Quality Gate will not be executed as ${pomXmlStructure.artifactName} is white-listed", EchoLevel.INFO)
				ifSonarQualityGateOK = true
	
			} else {
	
				ifSonarQualityGateOK = sonarQualityGateWorkspace(pomXmlStructure, pipelineData)
	
			}
	
		} catch(err) {
	
			def exceptionPrintout = "Error stage sonar-quality-gate technical error:\n\n${Utilities.prettyException(err, true)}"
			printOpen(exceptionPrintout, EchoLevel.ERROR)
	
			if ( (pipelineData.branchStructure.branchType == BranchType.RELEASE) && pipelineData.isPushCreateRC()) {
	
				pipelineData.pipelineStructure.resultPipelineData.ifSonarQualityGateOK = false
				sendStageEndToGPL(
					pomXmlStructure, pipelineData, stageId,
					Strings.toHtml(exceptionPrintout),
					null, "error")
	
				createMaximoAndThrow.waitForQualityGateException(
					pipelineData, pomXmlStructure,
					Strings.toHtml(exceptionPrintout))

			}
	
		}
	
		if (ifSonarQualityGateOK) {
            pipelineData.pipelineStructure.resultPipelineData.ifSonarQualityGateOK = true
			if (sonarQualityGateDisabled) {
	
				sendStageEndToGPL(pomXmlStructure, pipelineData, stageId, "Sonar Quality Gate was skipped due to current configuration settings", null, "warning")
	
			} else {
	
				sendStageEndToGPL(pomXmlStructure, pipelineData, stageId)
	
			}
	
		} else {
			pipelineData.pipelineStructure.resultPipelineData.ifSonarQualityGateOK = false
			
			dealWithSonarQualityGateFailure(pomXmlStructure, pipelineData, stageId)
	
		}
	}
}

private void dealWithSonarQualityGateFailure(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId) {

    String errorQualityGate = "<Warning_or_Error> stage sonar-quality-gate" + "\n" + "<a href='${pipelineData.testData}'>${pipelineData.testData}</a>"

    def whiteListApps = "${env.ALM_SERVICES_SKIP_SONAR_QUALITY_GATE_PIPELINE_ABORT_WHEN_FAIL}".split(";")

    printOpen("Check quality gate for ${pomXmlStructure.artifactName}", EchoLevel.ERROR)

    if (!Arrays.asList(whiteListApps).contains(pomXmlStructure.artifactName)) {

        sendStageEndToGPL(pomXmlStructure, pipelineData, stageId, Strings.toHtml(errorQualityGate.replace("<Warning_or_Error>", "Error")), null, "warning")
        //error "sonar-quality-gate result is ERROR, pipeline aborted"

    } else {

        String msg = "sonar-quality-gate result is ERROR, but pipeline will not abort because ${pomXmlStructure.artifactName} is in white list"
        printOpen("${msg}", EchoLevel.INFO)

		if ( (pipelineData.branchStructure.branchType == BranchType.RELEASE) && pipelineData.isPushCreateRC()) {
			pipelineData.pipelineStructure.resultPipelineData.ifSonarQualityGateOK = true
		}

        sendStageEndToGPL(pomXmlStructure, pipelineData, stageId, "${msg}", null, "warning")

    }

}
