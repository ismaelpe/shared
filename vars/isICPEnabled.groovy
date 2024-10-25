import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.BranchType
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.NexusUtils
import com.caixabank.absis3.GarAppType

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
