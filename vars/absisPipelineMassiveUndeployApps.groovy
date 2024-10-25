import groovy.transform.Field
import com.caixabank.absis3.*
import com.caixabank.absis3.KpiAlmEvent
import com.caixabank.absis3.KpiAlmEventStage
import com.caixabank.absis3.KpiAlmEventOperation

@Field Map pipelineParams

@Field String namespace
@Field String environment
@Field String period
@Field String action

def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters
	
    namespace = params.namespaceParam
	environment = params.environmentParam
    period = params.periodParam
    action = params.actionParam

    buildCode = env.BUILD_TAG
    currentBuild.displayName="$namespace-$environment $action"
	
	pipelineOS.withoutSCM(pipelineParams){
		try {			
			stageOS('init-pipeline'){
				initGlobalVars()
				scriptsPath=CopyAllLibraryScripts()
			}
			stageOS('undeploy-apps'){
                massiveUndeployApps(scriptsPath, namespace, environment, period, action)
            }
		}catch (err) {
			throw err
		}finally {
			cleanWorkspace()
		}
	}
}