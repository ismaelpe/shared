import com.project.alm.EchoLevel
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.GlobalVars
import com.project.alm.DeployStructure
import com.project.alm.BmxUtilities
import com.project.alm.CloudDeployStructure
import com.project.alm.CloudApiResponse

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK


def call(String environment, String app, String namespace, String center) {
	Map valuesDeployedCenter=null
	Map valuesDeployedAll=null
	Map valuesDeployed=null
	def deployIdCenter = 0
	def deployIdAll = 0
	
	String appCloudId=GlobalVars.Cloud_APP_ID_APPS
	String appCloud=GlobalVars.Cloud_APP_APPS
	
	String componentId=""
	//Vamos a recuperar la info de la app en Cloud
	if (namespace=="ARCH") {
		 appCloud=GlobalVars.Cloud_APP_ARCH
		 appCloudId=GlobalVars.Cloud_APP_ID_ARCH
	}


	printOpen("The id of the appOn Cloud is ${app}", EchoLevel.ALL)

    CloudApiResponse response = sendRequestToCloudApi("v1/application/${appCloudId}/component",null,"GET","${appCloud}","",false,false)

	if (response.statusCode>=200 && response.statusCode<300) {
		
		if (response.body!=null && response.body.size()>=1) {
			componentId = sh(script: "jq '.[] | select(.name | match(\"^(?i)${app}\$\")).id' ${env.WORKSPACE}@tmp/outputCommand.json ", returnStdout:true ).trim()
			
			if(!"".equals(componentId)) {
				printOpen("The id of the componentIs is ${componentId}", EchoLevel.INFO)
			
				//Tenemos el ID de la aplicacion
				//response=sendRequestToCloudApi("v1/application/PCLD/${appCloudId}/component/${componentId}/environment/${environment.toUpperCase()}/availabilityzone/${center}/status",null,"GET","${appCloud}","",false,false)
				response=sendRequestToCloudApi("v1/application/PCLD/${appCloud}/component/${componentId}/deploy/current/environment/${environment.toUpperCase()}/az/${center}",null,"GET","${appCloud}","",false,false)
				if (response.statusCode>=200 && response.statusCode<300) {
					//Ya tenemos la respuesta
					def opts = new DumperOptions()
					opts.setDefaultFlowStyle(BLOCK)
					Yaml yaml= new Yaml(opts)
					
					valuesDeployedCenter=(Map)yaml.load( response.body.values)
					
					deployIdCenter="${response.body.id}".toInteger()
				}	
				
				response=sendRequestToCloudApi("v1/application/PCLD/${appCloud}/component/${componentId}/deploy/current/environment/${environment.toUpperCase()}/az/ALL",null,"GET","${appCloud}","",false,false)

				if (response.statusCode>=200 && response.statusCode<300) {
					//Ya tenemos la respuesta
					def opts = new DumperOptions()
					opts.setDefaultFlowStyle(BLOCK)
					Yaml yaml= new Yaml(opts)
					valuesDeployedAll=(Map)yaml.load( response.body.values)
					deployIdAll="${response.body.id}".toInteger()
				}
				
				if (deployIdAll>deployIdCenter) valuesDeployed=valuesDeployedAll
				else valuesDeployed=valuesDeployedCenter
				
			}else {
				printOpen("APP NOT FOUND", EchoLevel.ALL)
			}
		}
		
		
		
	}else {
		printOpen("APP NOT FOUND", EchoLevel.ALL)
	}
	return valuesDeployed
	
}

def call(String environment, List appList, String namespace, String center) {

	String appCloudId=GlobalVars.Cloud_APP_ID_APPS
	String appCloud=GlobalVars.Cloud_APP_APPS
	
	
	//Vamos a recuperar la info de la app en Cloud
	if (namespace=="ARCH") {
		 appCloud=GlobalVars.Cloud_APP_ARCH
		 appCloudId=GlobalVars.Cloud_APP_ID_ARCH
	}
	
	
	Map valuesAllAppDeployed = [:]
	def outputCommandResponseDeployment = "outputCommandResponseDeployment.json"
	CloudApiResponse responseGlobal=sendRequestToCloudApi("v1/application/${appCloudId}/component",null,"GET","${appCloud}","",false,false, null, null, [:], outputCommandResponseDeployment)
	
	if (responseGlobal.statusCode>=200 && responseGlobal.statusCode<300) {
		appList.each { app ->
			printOpen("The id of the app on Cloud is ${app}", EchoLevel.ALL)
			String componentId=""
			Map valuesDeployedCenter=null
			Map valuesDeployedAll=null
			Map valuesDeployed=null
			def deployIdCenter = 0
			def deployIdAll = 0
		
			if (responseGlobal.body!=null && responseGlobal.body.size()>=1) {
				componentId = sh(script: "jq '.[] | select(.name | match(\"^(?i)${app}\$\")).id' ${env.WORKSPACE}@tmp/$outputCommandResponseDeployment ", returnStdout:true ).trim()
				
				if(!"".equals(componentId)) {
					printOpen("The id of the component is ${componentId}", EchoLevel.DEBUG)
				
					//Tenemos el ID de la aplicacion
					//response=sendRequestToCloudApi("v1/application/PCLD/${appCloudId}/component/${componentId}/environment/${environment.toUpperCase()}/availabilityzone/${center}/status",null,"GET","${appCloud}","",false,false)
					CloudApiResponse response=sendRequestToCloudApi("v1/application/PCLD/${appCloud}/component/${componentId}/deploy/current/environment/${environment.toUpperCase()}/az/${center}",null,"GET","${appCloud}","",false,false)
					if (response.statusCode>=200 && response.statusCode<300) {
						//Ya tenemos la respuesta
						def opts = new DumperOptions()
						opts.setDefaultFlowStyle(BLOCK)
						Yaml yaml= new Yaml(opts)
						
						valuesDeployedCenter=(Map)yaml.load( response.body.values)
						
						deployIdCenter="${response.body.id}".toInteger()
					}
					
					response=sendRequestToCloudApi("v1/application/PCLD/${appCloud}/component/${componentId}/deploy/current/environment/${environment.toUpperCase()}/az/ALL",null,"GET","${appCloud}","",false,false)
					if (response.statusCode>=200 && response.statusCode<300) {
						//Ya tenemos la respuesta
						def opts = new DumperOptions()
						opts.setDefaultFlowStyle(BLOCK)
						Yaml yaml= new Yaml(opts)
						
						valuesDeployedAll=(Map)yaml.load( response.body.values)
						
						deployIdAll="${response.body.id}".toInteger()
					}
					
					if (deployIdAll>deployIdCenter) {
						valuesDeployed=valuesDeployedAll
					}else {
						valuesDeployed=valuesDeployedCenter
					}
					valuesAllAppDeployed.put(app, valuesDeployed)
						
				}else {
					printOpen("APP NOT FOUND", EchoLevel.ERROR)
				}
			}
		}
	}else {
		printOpen("APP NOT FOUND", EchoLevel.ERROR)
	}
	return valuesAllAppDeployed
	
}
