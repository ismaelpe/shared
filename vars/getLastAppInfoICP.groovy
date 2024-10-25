import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.DeployStructure
import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.ICPDeployStructure
import com.caixabank.absis3.ICPApiResponse

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK


def call(String environment, String app, String namespace, String center) {
	Map valuesDeployedCenter=null
	Map valuesDeployedAll=null
	Map valuesDeployed=null
	def deployIdCenter = 0
	def deployIdAll = 0
	
	String appICPId=GlobalVars.ICP_APP_ID_APPS
	String appICP=GlobalVars.ICP_APP_APPS
	
	String componentId=""
	//Vamos a recuperar la info de la app en ICP
	if (namespace=="ARCH") {
		 appICP=GlobalVars.ICP_APP_ARCH
		 appICPId=GlobalVars.ICP_APP_ID_ARCH
	}


	printOpen("The id of the appOn ICP is ${app}", EchoLevel.ALL)

    ICPApiResponse response = sendRequestToICPApi("v1/application/${appICPId}/component",null,"GET","${appICP}","",false,false)

	if (response.statusCode>=200 && response.statusCode<300) {
		
		if (response.body!=null && response.body.size()>=1) {
			componentId = sh(script: "jq '.[] | select(.name | match(\"^(?i)${app}\$\")).id' ${env.WORKSPACE}@tmp/outputCommand.json ", returnStdout:true ).trim()
			
			if(!"".equals(componentId)) {
				printOpen("The id of the componentIs is ${componentId}", EchoLevel.INFO)
			
				//Tenemos el ID de la aplicacion
				//response=sendRequestToICPApi("v1/application/PCLD/${appICPId}/component/${componentId}/environment/${environment.toUpperCase()}/availabilityzone/${center}/status",null,"GET","${appICP}","",false,false)
				response=sendRequestToICPApi("v1/application/PCLD/${appICP}/component/${componentId}/deploy/current/environment/${environment.toUpperCase()}/az/${center}",null,"GET","${appICP}","",false,false)
				if (response.statusCode>=200 && response.statusCode<300) {
					//Ya tenemos la respuesta
					def opts = new DumperOptions()
					opts.setDefaultFlowStyle(BLOCK)
					Yaml yaml= new Yaml(opts)
					
					valuesDeployedCenter=(Map)yaml.load( response.body.values)
					
					deployIdCenter="${response.body.id}".toInteger()
				}	
				
				response=sendRequestToICPApi("v1/application/PCLD/${appICP}/component/${componentId}/deploy/current/environment/${environment.toUpperCase()}/az/ALL",null,"GET","${appICP}","",false,false)

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

	String appICPId=GlobalVars.ICP_APP_ID_APPS
	String appICP=GlobalVars.ICP_APP_APPS
	
	
	//Vamos a recuperar la info de la app en ICP
	if (namespace=="ARCH") {
		 appICP=GlobalVars.ICP_APP_ARCH
		 appICPId=GlobalVars.ICP_APP_ID_ARCH
	}
	
	
	Map valuesAllAppDeployed = [:]
	def outputCommandResponseDeployment = "outputCommandResponseDeployment.json"
	ICPApiResponse responseGlobal=sendRequestToICPApi("v1/application/${appICPId}/component",null,"GET","${appICP}","",false,false, null, null, [:], outputCommandResponseDeployment)
	
	if (responseGlobal.statusCode>=200 && responseGlobal.statusCode<300) {
		appList.each { app ->
			printOpen("The id of the app on ICP is ${app}", EchoLevel.ALL)
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
					//response=sendRequestToICPApi("v1/application/PCLD/${appICPId}/component/${componentId}/environment/${environment.toUpperCase()}/availabilityzone/${center}/status",null,"GET","${appICP}","",false,false)
					ICPApiResponse response=sendRequestToICPApi("v1/application/PCLD/${appICP}/component/${componentId}/deploy/current/environment/${environment.toUpperCase()}/az/${center}",null,"GET","${appICP}","",false,false)
					if (response.statusCode>=200 && response.statusCode<300) {
						//Ya tenemos la respuesta
						def opts = new DumperOptions()
						opts.setDefaultFlowStyle(BLOCK)
						Yaml yaml= new Yaml(opts)
						
						valuesDeployedCenter=(Map)yaml.load( response.body.values)
						
						deployIdCenter="${response.body.id}".toInteger()
					}
					
					response=sendRequestToICPApi("v1/application/PCLD/${appICP}/component/${componentId}/deploy/current/environment/${environment.toUpperCase()}/az/ALL",null,"GET","${appICP}","",false,false)
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
