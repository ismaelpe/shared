import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.ICPApiResponse
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK

def call(PomXmlStructure pomXml, PipelineData pipeline,  String env) {
	generateValuesYamlLastICPDeployment(pomXml,pipeline,env,'ALL')
}
/**
 *
 * @param body
 * @return
 */
//def call(PomXmlStructure pomXml, PipelineData pipeline, String deployId, String env) {
def call(PomXmlStructure pomXml, PipelineData pipeline,  String env, String distributionCenter) {
	Map valuesDeployed=null
	
	
	ICPApiResponse response=sendRequestToICPApi("v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy/current/environment/${env.toUpperCase()}/az/${distributionCenter}",null,"GET","${pomXml.getICPAppName()}","",false,false, pipeline, pomXml)
	printOpen("The response of the last of the deployment ${response.prettyPrint()}", EchoLevel.ALL)

	if (response.statusCode>=200 && response.statusCode<300) {
		String values=""
		if (response.body.values==null && response.body.id!=null) {
			//Tenemos el deployId no sabemos cual es el motivo que esta API nos devuelve el API pero no el deploy
			String deployId=response.body.id
			//Vamos a buscar el values del deployId concreto
			ICPApiResponse response1=sendRequestToICPApi("v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy/${deployId}",null,"GET","${pomXml.getICPAppName()}","",false,false, pipeline, pomXml)
			
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
