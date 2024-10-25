import com.project.alm.BranchType
import com.project.alm.EchoLevel
import com.project.alm.GarAppType
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure

def call(PomXmlStructure pomXml, PipelineData pipeline, String resultDeployICP, String deployICPPhases , Exception e) {
	
	String artifactApp = pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name))
	if (resultDeployICP=="OK") {
		sendEmail(" Resultado ejecucion app ${artifactApp} - ${pipeline.getPipelineBuildName()}  OK ", env.ALM_SERVICES_EMAIL_ICP_DEPLOY_RESULT, "${artifactApp} rama ${pipeline.getPipelineBuildName()}", "OK en el paso ${deployICPPhases}")
	}else {
		sendEmail(" Resultado ejecucion app ${artifactApp} - ${pipeline.getPipelineBuildName()}  KO - ${deployICPPhases}", env.ALM_SERVICES_EMAIL_ICP_DEPLOY_RESULT, "${artifactApp} rama ${pipeline.getPipelineBuildName()}", "KO en el paso ${deployICPPhases}")
		abortPipelineICP(pomXml, pipeline," Resultado ejecucion app ${artifactApp} - ${pipeline.getPipelineBuildName()}  KO - ${deployICPPhases}")
	}
	if (e!=null) printOpen("Error en el deploy a ICP, de momento nos comemos el error hasta que esto sea estable ${e}", EchoLevel.ERROR)
}


def call(PomXmlStructure pomXml, PipelineData pipeline, String message) {
	
	String artifactApp=pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) )
	
	printOpen("Evaluating aborting the execution the app ${artifactApp} versus ${env.ICP_ABORT_DEPLOY_PIPELINE} the  branch is ${pipeline.branchStructure.branchType} # ", EchoLevel.DEBUG)
	
	boolean abortExecution=false
	
	
	if (env.ICP_ABORT_DEPLOY_PIPELINE!=null) {
				
		if (pipeline.branchStructure.branchType == BranchType.FEATURE && env.ICP_ABORT_DEPLOY_PIPELINE.contains('ALL_FEATURE')) {
			abortExecution=true
		}else if (pipeline.branchStructure.branchType == BranchType.MASTER && env.ICP_ABORT_DEPLOY_PIPELINE.contains('ALL_MASTER')) {
			abortExecution=true
		}else if ((pipeline.branchStructure.branchType == BranchType.HOTFIX || pipeline.branchStructure.branchType == BranchType.RELEASE) && env.ICP_ABORT_DEPLOY_PIPELINE.contains('ALL_RELEASE')) {
			abortExecution=true
		}		
		
	}
	
	if (abortExecution) {
		
		printOpen("We will abort the execution. ${message}", EchoLevel.INFO)
		throw new Exception("We will abort the execution. ${message}")
	}else {
		printOpen("We will resume the execution. ${message}", EchoLevel.DEBUG)
	}

}
