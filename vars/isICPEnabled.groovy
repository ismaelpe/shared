import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.BranchType
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.NexusUtils
import com.project.alm.GarAppType

def call(String environment) {
	environment=environment.toLowerCase()
	printOpen("Evaluating the Cloud environment enabled ${environment} versus ${env.Cloud_PARALLEL_APP_DEPLOY} #", EchoLevel.ALL)
	
	if (env.Cloud_PARALLEL_APP_DEPLOY!=null) {
				
		if (environment=="dev" && env.Cloud_PARALLEL_APP_DEPLOY.contains('ALL_MASTER')) {
			return true
		}else if ((environment=="tst" || environment=="pre" || environment=="pro") && env.Cloud_PARALLEL_APP_DEPLOY.contains('ALL_RELEASE')) {
			return true
		}
	}else return false

	return false
}
