import com.project.alm.EchoLevel
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.GlobalVars
import com.project.alm.DeployStructure
import com.project.alm.BmxUtilities
import com.project.alm.ICPDeployStructure

import com.project.alm.ICPApiResponse

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK


def call(String environment, String app, String namespace, String center) {
	Map valuesDeployed=null
	
	String appICPId=GlobalVars.ICP_APP_ID_APPS
	String appICP=GlobalVars.ICP_APP_APPS
	
	String componentId="0"
	//Vamos a recuperar la info de la app en ICP
	if (namespace=="ARCH") {
		 appICP=GlobalVars.ICP_APP_ARCH
		 appICPId=GlobalVars.ICP_APP_ID_ARCH
	}
	ICPApiResponse response=sendRequestToICPApi("v1/application/${appICPId}/component",null,"GET","${appICP}","",false,false)
	
	if (response.statusCode>=200 && response.statusCode<300) {
		
		if (response.body!=null && response.body.size()>=1) {
			response.body.each {
				if (it.name.equals(app.toUpperCase())) {
					componentId=it.id
				}
			}
			
			
			
			//Tenemos el ID de la aplicacion
			//response=sendRequestToICPApi("v1/application/PCLD/${appICPId}/component/${componentId}/environment/${environment.toUpperCase()}/availabilityzone/${center}/status",null,"GET","${appICP}","",false,false)
			response=sendRequestToICPApi("v1/application/PCLD/${appICP}/component/${componentId}/deploy/current/environment/${environment.toUpperCase()}/az/${center}",null,"GET","${appICP}","",false,false)
			if (response.statusCode>=200 && response.statusCode<300) {
				//Ya tenemos la respuesta
				def opts = new DumperOptions()
				opts.setDefaultFlowStyle(BLOCK)
				Yaml yaml= new Yaml(opts)
				
				valuesDeployed=(Map)yaml.load( response.body.values)
			}		
			
		}
		
		
	}else {
		printOpen("APP NOT FOUND", EchoLevel.ALL)
	}
	return valuesDeployed
	
}
