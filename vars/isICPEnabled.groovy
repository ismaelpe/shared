import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.BranchType
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.NexusUtils
import com.project.alm.GarAppType

def call(String environment) {
	environment=environment.toLowerCase()
	printOpen("Evaluating the ICP environment enabled ${environment} versus ${env.ICP_PARALLEL_APP_DEPLOY} #", EchoLevel.ALL)
	
	if (env.ICP_PARALLEL_APP_DEPLOY!=null) {
				
		if (environment=="dev" && env.ICP_PARALLEL_APP_DEPLOY.contains('ALL_MASTER')) {
			return true
		}else if ((environment=="tst" || environment=="pre" || environment=="pro") && env.ICP_PARALLEL_APP_DEPLOY.contains('ALL_RELEASE')) {
			return true
		}
	}else return false

	return false
}
