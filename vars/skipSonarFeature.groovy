import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.PomXmlStructure
import com.project.alm.BranchType
import com.project.alm.PipelineData

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
