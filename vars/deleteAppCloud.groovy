import com.project.alm.EchoLevel
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.CloudUtils

def call(def componentName, def componentId, def appId,def appName, def environment, def centers, boolean deleteFromCloud) {
	deleteAppCloud(componentName,componentId,appId,appName,environment,centers,deleteFromCloud,false)
}

def call(def componentName, def componentId, def appId,def appName, def environment, def centers, boolean deleteFromCloud, boolean onlyDeleteFromCloud) {
	def body = [
		az: centers,
		environment: environment
	]
	
	
	if (onlyDeleteFromCloud) {
		def response=sendRequestToCloudApi("v1/api/application/PCLD/${appName}/component/${componentName}",null,"DELETE","${appName}","",false,false)
		printOpen(" The status code of the delete ${response.statusCode}", EchoLevel.INFO)
		
		if (response.statusCode>=200 && response.statusCode<300) {
			printOpen(" Delete ${componentId} of the component ${componentName} from the app ${appName}", EchoLevel.INFO)
		}else {
			throw new Exception("Error deleting the app ${appName} and the component ${componentName}")
		}
		
		//Tenemos que eliminar en OCP?
		if (env.ENV_K8S_OCP!=null && env.ENV_K8S_OCP.contains(environment.toUpperCase())) {
			response=sendRequestToCloudApi("v1/api/application/PCLD_MIGRATED/${appName}/component/${componentName}",null,"DELETE","${appName}","",false,false)
			printOpen(" The status code of the delete ${response.statusCode}", EchoLevel.INFO)
			
			if (response.statusCode>=200 && response.statusCode<300) {
				printOpen(" Delete ${componentId} of the component ${componentName} from the app ${appName} - OCP", EchoLevel.INFO)
			}else{
				printOpen("Error deleting the app ${appName} and the component ${componentName}", EchoLevel.ERROR)
			}
		}
	}else {	
		def response=sendRequestToCloudApi("v1/api/application/PCLD/${appName}/component/${componentName}/deploy",body,"DELETE","${appName}","",false,false)
				
		printOpen(" The status code of the undeploy ${response.statusCode} ${deleteFromCloud}", EchoLevel.INFO)
		
		
		if (deleteFromCloud && (response.statusCode>=200 && response.statusCode<300 || response.statusCode==404)) {
			
			response=sendRequestToCloudApi("v1/api/application/PCLD/${appName}/component/${componentName}",null,"DELETE","${appName}","",false,false)
			printOpen(" The status code of the delete ${response.statusCode}", EchoLevel.ALL)
			
			if (response.statusCode>=200 && response.statusCode<300) {
				printOpen(" Delete ${componentId} of the component ${componentName} from the app ${appName}", EchoLevel.INFO)
			}else {
				throw new Exception("Error deleting the app ${appName} and the component ${componentName}")
			}
						
		}else {
			printOpen("Don't delete component from Cloud", EchoLevel.INFO)
		}
		if (env.ENV_K8S_OCP!=null && env.ENV_K8S_OCP.contains(environment.toUpperCase())) {
			///api/publisher/v1/api/application/PCLD_MIGRATED/AB3APP/component/BRANCHLOCATIONOPERATIONS6/deploy
			response=sendRequestToCloudApi("v1/api/application/PCLD_MIGRATED/${appName}/component/${componentName}/deploy",body,"DELETE","${appName}","",false,false)
			
			if (deleteFromCloud && (response.statusCode>=200 && response.statusCode<300 || response.statusCode==404)) {
				
				response=sendRequestToCloudApi("v1/api/application/PCLD_MIGRATED/${appName}/component/${componentName}",null,"DELETE","${appName}","",false,false)
				printOpen(" The status code of the delete ${response.statusCode}", EchoLevel.ALL)
				
				if (response.statusCode>=200 && response.statusCode<300) {
					printOpen(" Delete ${componentId} of the component ${componentName} from the app ${appName} OCP", EchoLevel.INFO)
				}else {
					throw new Exception("Error deleting the app ${appName} and the component ${componentName} OCP")
				}
							
			}else {
				printOpen("Don't delete component from OCP", EchoLevel.INFO)
			}
			
		}
		
	}
	
	
}

