import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.BranchType
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.NexusUtils
import com.caixabank.absis3.GarAppType

def call(PomXmlStructure pomXml, PipelineData pipeline) {
	
	String artifactApp=pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) )
	
	printOpen("Evaluating the app ${artifactApp} versus ${env.ICP_PARALLEL_APP_DEPLOY} the  branch is ${pipeline.branchStructure.branchType} # ", EchoLevel.ALL)

	
	if (env.ICP_PARALLEL_APP_DEPLOY!=null && pipeline.deployFlag == true) {
		if (env.ICP_DEPLOY_TO_ICP_ENV!=null ) {
			if (pipeline.bmxStructure!=null && pipeline.bmxStructure.environment!=null && env.ICP_DEPLOY_TO_ICP_ENV.contains(pipeline.bmxStructure.environment.toUpperCase())) {
				printOpen("Return true", EchoLevel.ALL)
				pipeline.deployOnIcp=true
				return true
			}
		}else {
			printOpen("Non informed ${ICP_DEPLOY_TO_ICP_ENV}", EchoLevel.ALL)
			pipeline.deployOnIcp=false
			return false
		}
	

				
		if (pipeline.branchStructure.branchType == BranchType.FEATURE && env.ICP_PARALLEL_APP_DEPLOY.contains('ALL_FEATURE')) {
			pipeline.deployOnIcp=true
		}else if (pipeline.branchStructure.branchType == BranchType.MASTER && env.ICP_PARALLEL_APP_DEPLOY.contains('ALL_MASTER')) {
			pipeline.deployOnIcp=true
		}else if ((pipeline.branchStructure.branchType == BranchType.HOTFIX || pipeline.branchStructure.branchType == BranchType.RELEASE) && env.ICP_PARALLEL_APP_DEPLOY.contains('ALL_RELEASE')) {
			pipeline.deployOnIcp=true
		}else {

			if (artifactApp!=null)
				pipeline.deployOnIcp=env.ICP_PARALLEL_APP_DEPLOY.contains(artifactApp)
			else pipeline.deployOnIcp=false
			
		}
		
		
	}else pipeline.deployOnIcp=false
	
	printOpen("The micro needs to deploy on icp  ${pipeline.deployOnIcp}", EchoLevel.ALL)
	
	return pipeline.deployOnIcp

}
