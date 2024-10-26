import com.project.alm.EchoLevel
import com.project.alm.CloudApiResponse
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK

def call(PomXmlStructure pomXml, PipelineData pipeline,  String env) {
	generateValuesYamlLastCloudDeployment(pomXml,pipeline,env,'ALL')
}
/**
 *
 * @param body
 * @return
 */
//def call(PomXmlStructure pomXml, PipelineData pipeline, String deployId, String env) {
def call(PomXmlStructure pomXml, PipelineData pipeline,  String env, String distributionCenter) {
	Map valuesDeployed=null
	
	
	CloudApiResponse response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy/current/environment/${env.toUpperCase()}/az/${distributionCenter}",null,"GET","${pomXml.getCloudAppName()}","",false,false, pipeline, pomXml)
	printOpen("The response of the last of the deployment ${response.prettyPrint()}", EchoLevel.ALL)

	if (response.statusCode>=200 && response.statusCode<300) {
		String values=""
		if (response.body.values==null && response.body.id!=null) {
			//Tenemos el deployId no sabemos cual es el motivo que esta API nos devuelve el API pero no el deploy
			String deployId=response.body.id
			//Vamos a buscar el values del deployId concreto
			CloudApiResponse response1=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy/${deployId}",null,"GET","${pomXml.getCloudAppName()}","",false,false, pipeline, pomXml)
			
			if (response1.statusCode>=200 && response1.statusCode<300) {
				values=response1.body.values
			}else return valuesDeployed
			
				
		}else values=response.body.values
		
		if (values==null) {
			return valuesDeployed
		}
						
		def opts = new DumperOptions()
		opts.setDefaultFlowStyle(BLOCK)
		Yaml yaml= new Yaml(opts)
		
		valuesDeployed=(Map)yaml.load( values)
		
		printOpen("Last deployed ${valuesDeployed}", EchoLevel.ALL)
			
	}
	
	return valuesDeployed
}	
