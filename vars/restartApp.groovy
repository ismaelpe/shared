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
import java.util.Map


def call(Map valuesDeployed, String app, String center, String namespace, String environment) {
	
	def deployIdCenter = 0
	def deployIdAll = 0
	
	String appCloudId=GlobalVars.Cloud_APP_ID_APPS
	String appCloud=GlobalVars.Cloud_APP_APPS
	
	String componentId="0"
	//Vamos a recuperar la info de la app en Cloud
	if (namespace=="ARCH") {
		 appCloud=GlobalVars.Cloud_APP_ARCH
		 appCloudId=GlobalVars.Cloud_APP_ID_ARCH
	}
	
	CloudApiResponse response=sendRequestToCloudApi("v1/application/${appCloudId}/component",null,"GET","${appCloud}","",false,false)
	
	if (response.statusCode>=200 && response.statusCode<300 && valuesDeployed!=null) {
		
		if (response.body!=null && response.body.size()>=1) {
			response.body.each {
				if (it.name.equals(app.toUpperCase())) {
					componentId=it.id
				}
			}
			
			
			
			printOpen("component ${componentId} ${valuesDeployed}", EchoLevel.ALL)
			
			response=sendRequestToCloudApi("v1/application/PCLD/${appCloud}/component/${componentId}/environment/${environment.toUpperCase()}/availabilityzone/${center}/status",null,"GET","${appCloud}","",false,false)
			
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
			
			response=sendRequestToCloudApi("v1/application/PCLD/${appCloud}/component/${componentId}/deploy",body,"POST","${appCloud}","v1/application/PCLD/${appCloud}/component/${componentId}/deploy",true,true)
			
		}
		
		
		
	}else {
		printOpen("APP NOT FOUND", EchoLevel.ALL)
	}
	return valuesDeployed
	
}
