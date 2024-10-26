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


def call(String environment, String namespace, String center) {
	
	Map valuesDeployed=new HashMap<String, Map>()
	def deployIdCenter = 0
	def deployIdAll = 0
	
	String appCloudId=GlobalVars.Cloud_APP_ID_APPS
	String appCloud=GlobalVars.Cloud_APP_APPS
	
	if (namespace=="ARCH") {
		 appCloud=GlobalVars.Cloud_APP_ARCH
		 appCloudId=GlobalVars.Cloud_APP_ID_ARCH
	}
	
	CloudApiResponse response=sendRequestToCloudApi("v1/application/${appCloudId}/component",null,"GET","${appCloud}","",false,false)
	
	if (response.statusCode>=200 && response.statusCode<300) {
		
		if (response.body!=null && response.body.size()>=1) {
			response.body.each {
				String componentId=it.id
			
				if (center != null) {
					response=sendRequestToCloudApi("v1/application/PCLD/${appCloud}/component/${componentId}/deploy/current/environment/${environment.toUpperCase()}/az/${center}",null,"GET","${appCloud}","",false,false)
					if (response.statusCode>=200 && response.statusCode<300 && response.body.values != null) {
						//Ya tenemos la respuesta
						def opts = new DumperOptions()
						opts.setDefaultFlowStyle(BLOCK)
						Yaml yaml= new Yaml(opts)
						
						valuesDeployed.put(it.name, (Map)yaml.load( response.body.values))
					}
				} else {
					response=sendRequestToCloudApi("v1/application/PCLD/${appCloud}/component/${componentId}/deploy/current/environment/${environment.toUpperCase()}/az/ALL",null,"GET","${appCloud}","",false,false)
					if (response.statusCode>=200 && response.statusCode<300) {
						//Ya tenemos la respuesta
						def opts = new DumperOptions()
						opts.setDefaultFlowStyle(BLOCK)
						Yaml yaml= new Yaml(opts)
						
						valuesDeployed.put(it.name, (Map)yaml.load( response.body.values))
					}
				}
				
			}
			
		}
		
		
		
	}else {
		printOpen("APP NOT FOUND", EchoLevel.ALL)
	}
	return valuesDeployed
	
}
