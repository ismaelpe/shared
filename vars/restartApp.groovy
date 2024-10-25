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
import java.util.Map


def call(Map valuesDeployed, String app, String center, String namespace, String environment) {
	
	def deployIdCenter = 0
	def deployIdAll = 0
	
	String appICPId=GlobalVars.ICP_APP_ID_APPS
	String appICP=GlobalVars.ICP_APP_APPS
	
	String componentId="0"
	//Vamos a recuperar la info de la app en ICP
	if (namespace=="ARCH") {
		 appICP=GlobalVars.ICP_APP_ARCH
		 appICPId=GlobalVars.ICP_APP_ID_ARCH
	}
	
	ICPApiResponse response=sendRequestToICPApi("v1/application/${appICPId}/component",null,"GET","${appICP}","",false,false)
	
	if (response.statusCode>=200 && response.statusCode<300 && valuesDeployed!=null) {
		
		if (response.body!=null && response.body.size()>=1) {
			response.body.each {
				if (it.name.equals(app.toUpperCase())) {
					componentId=it.id
				}
			}
			
			
			
			printOpen("component ${componentId} ${valuesDeployed}", EchoLevel.ALL)
			
			response=sendRequestToICPApi("v1/application/PCLD/${appICP}/component/${componentId}/environment/${environment.toUpperCase()}/availabilityzone/${center}/status",null,"GET","${appICP}","",false,false)
			
			if (response.statusCode>=200 && response.statusCode<300) {
				
			   if (response.body==null) {
				   printOpen("El micro no existe posiblemente es un primer deploy", EchoLevel.ALL)
				   throw new Exception("El micro no esta desplegado")
			   }
			   
			   
			   
			   if (center!="ALL") {
				   if (response.body.size()==1 && response.body.getAt(0).items!=null && response.body.getAt(0).items.size()!=0) {
					   printOpen("Existe una version anterior del micro ${response.body.getAt(0).items} ", EchoLevel.ALL)
				   }else {
					   printOpen("El micro no existe posiblemente es un primer deploy", EchoLevel.ALL)
					   throw new Exception ("El micro no esta desplegado")
				   }
			   }
			   if (center=="ALL") {
				   if (response.body.size()==2 && response.body.getAt(0).items!=null && response.body.getAt(0).items.size()!=0) {
					   printOpen("Existe una version anterior del micro ${response.body.getAt(0).items} ", EchoLevel.ALL)
				   }else {
					   printOpen("El micro no existe posiblemente es un primer deploy", EchoLevel.ALL)
					   throw new Exception ("El micro no esta desplegado")
				   }
			   }
			   
			}
			
			 
			if (valuesDeployed["local"]!=null) {
			
				Map valuesDeployedLocal=valuesDeployed["local"]
				printOpen("1 ${valuesDeployedLocal}", EchoLevel.ALL)
				if (valuesDeployedLocal["app"]!=null) {
					
					Map valuesDeployedLocalApp=valuesDeployedLocal["app"]
					printOpen("2 ${valuesDeployedLocalApp}", EchoLevel.ALL)
					
					if (valuesDeployedLocalApp["envVars"]!=null) {
						def valuesDeployedLocalEnvVarsList=valuesDeployedLocalApp["envVars"]
						def restartMap = [ name : "ALM_RESTART",
										   value : "1"]
						
						boolean moreOneReboot=false
						if (valuesDeployedLocalEnvVarsList) {
							valuesDeployedLocalEnvVarsList.each{
								if (it.name=="ALM_RESTART") {
									moreOneReboot=true
									it.value=it.value+"1"
									
								}
							}
						}
						if (moreOneReboot==false) valuesDeployedLocalEnvVarsList.add(restartMap)
					}
				}
			}
			
			def toJson = {
				input ->
				groovy.json.JsonOutput.toJson(input)
			}
			
			def body = [
				az: "${center}",
				environment: "${environment.toUpperCase()}",
				values: "${toJson(valuesDeployed)}"
			]
			
			response=sendRequestToICPApi("v1/application/PCLD/${appICP}/component/${componentId}/deploy",body,"POST","${appICP}","v1/application/PCLD/${appICP}/component/${componentId}/deploy",true,true)
			
		}
		
		
		
	}else {
		printOpen("APP NOT FOUND", EchoLevel.ALL)
	}
	return valuesDeployed
	
}
