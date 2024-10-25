import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.BranchType
import com.caixabank.absis3.PipelineData

//Si es feature... puede suceder que quieran esquivar el sonar
def call(PomXmlStructure pomXml, PipelineData pipelineData) {
	
	printOpen("Checking branch ${pipelineData.branchStructure.branchType}", EchoLevel.DEBUG)
	if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {
		String file = 'sonar/' + pipelineData.branchStructure.getFeatureRelease()+'.ignore'		
				
		def fileExistsSonar = fileExists file
		
		printOpen("Does the file ${file} exist? ${fileExistsSonar}", EchoLevel.DEBUG)
		
		if (fileExistsSonar) {
			return true
		} else {
			return false
		}
		
	}else {
		return false
	}	
	
}
