import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.BranchType
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.NexusUtils
import com.project.alm.GarAppType

def call(PomXmlStructure pomXml, PipelineData pipeline) {
	
	String artifactApp=pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) )
	
	printOpen("Evaluating the app ${artifactApp} versus ${env.Cloud_PARALLEL_APP_DEPLOY} the  branch is ${pipeline.branchStructure.branchType} # ", EchoLevel.ALL)

	
	if (env.Cloud_PARALLEL_APP_DEPLOY!=null && pipeline.deployFlag == true) {
		if (env.Cloud_DEPLOY_TO_Cloud_ENV!=null ) {
			if (pipeline.bmxStructure!=null && pipeline.bmxStructure.environment!=null && env.Cloud_DEPLOY_TO_Cloud_ENV.contains(pipeline.bmxStructure.environment.toUpperCase())) {
				printOpen("Return true", EchoLevel.ALL)
				pipeline.deployOnCloud=true
				return true
			}
		}else {
			printOpen("Non informed ${Cloud_DEPLOY_TO_Cloud_ENV}", EchoLevel.ALL)
			pipeline.deployOnCloud=false
			return false
		}
	

				
		if (pipeline.branchStructure.branchType == BranchType.FEATURE && env.Cloud_PARALLEL_APP_DEPLOY.contains('ALL_FEATURE')) {
			pipeline.deployOnCloud=true
		}else if (pipeline.branchStructure.branchType == BranchType.MASTER && env.Cloud_PARALLEL_APP_DEPLOY.contains('ALL_MASTER')) {
			pipeline.deployOnCloud=true
		}else if ((pipeline.branchStructure.branchType == BranchType.HOTFIX || pipeline.branchStructure.branchType == BranchType.RELEASE) && env.Cloud_PARALLEL_APP_DEPLOY.contains('ALL_RELEASE')) {
			pipeline.deployOnCloud=true
		}else {

			if (artifactApp!=null)
				pipeline.deployOnCloud=env.Cloud_PARALLEL_APP_DEPLOY.contains(artifactApp)
			else pipeline.deployOnCloud=false
			
		}
		
		
	}else pipeline.deployOnCloud=false
	
	printOpen("The micro needs to deploy on cloud  ${pipeline.deployOnCloud}", EchoLevel.ALL)
	
	return pipeline.deployOnCloud

}
