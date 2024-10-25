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
import java.util.Map
import java.util.ArrayList
import java.util.List

def call(String environment, String app, String namespace) {
	
	def deployIdCenter = 0
	def deployIdAll = 0
	
	List listadoAppEden=new ArrayList()
	
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
				if (it.name.startsWith(app.toUpperCase()) && it.name.endsWith("E")) {
					listadoAppEden.add(it.name)
				}
			}			
			
		}
		
		
		
	}else {
        printOpen("NO EDEN APP NOT FOUND", EchoLevel.INFO)
	}
	return listadoAppEden
	
}
