import com.project.alm.EchoLevel
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.GlobalVars

def call(PomXmlStructure pomXml, PipelineData pipeline) {

	generateContractPom(pomXml, pipeline, false)

	if("true".equals(GlobalVars.ALM_SERVICES_SKIP_VALIDATION_CONTRACT_ALL) || isInExclusionList(pomXml.artifactName)) {
		printOpen("Skipping contract validation for ${pomXml.artifactName}", EchoLevel.INFO)
	} else {
	    printOpen("Validating Contract... artifactId: ${pomXml.artifactName} version: ${pomXml.artifactVersion}", EchoLevel.INFO)
	    installDeployClientArtifact(pomXml, pipeline, true, false)
	    printOpen("The contract has been validated.", EchoLevel.INFO)
	}

}

def isInExclusionList(String component) {
	printOpen("List of skipped components", EchoLevel.DEBUG)
	def exclusionList = GlobalVars.ALM_SERVICES_SKIP_VALIDATION_CONTRACT_LIST.split(";")
	return Arrays.asList(exclusionList).contains(component)
}
