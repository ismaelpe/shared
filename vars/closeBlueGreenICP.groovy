import com.project.alm.BmxUtilities
import com.project.alm.BranchStructure
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.PomXmlStructure
import com.project.alm.DeployStructure
import com.project.alm.AncientVersionInfo
import com.project.alm.Utilities
import com.project.alm.PipelineData
import com.project.alm.CloudDeployStructure
import com.project.alm.CloudAppResources
import com.project.alm.CloudUtils
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import java.util.Map
import java.util.HashMap

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK

def settingNewColourToTheService(def colour, def services, def idService) {
	Map almAppEnvQualifier=services["envQualifier"]
	
	Map service=almAppEnvQualifier[idService]
	service["targetColour"]=colour
}

def addStableToTheService(def colour, def services, def idService, def idStableService) {
	Map almAppEnvQualifier=services["envQualifier"]
	
	Map service=new HashMap()
	almAppEnvQualifier.put(idService,service)
	
	service.put("targetColour",colour)
	service.put("id",idStableService)

}


def call(PomXmlStructure artifactPom, PipelineData pipeline, boolean existAncient) {

    printOpen("close Blue Green Cloud", EchoLevel.ALL)
	
	String environment= pipeline.bmxStructure.environment.toUpperCase()
	CloudDeployStructure deployStructure=new CloudDeployStructure('cxb-ab3cor','cxb-ab3app',environment)

	printOpen("The component to undeploy is ${pipeline.componentId}", EchoLevel.ALL)
	
	pipeline.componentId=generateArtifactInCloud(artifactPom,pipeline,CloudUtils.generateCloudResources(deployStructure.memory,environment.toUpperCase(),artifactPom.isArchProject()))
	
	
	String cloudDistCenter="ALL"
	
	Map lastDeployedValuesYaml=generateValuesYamlLastCloudDeployment(artifactPom,pipeline,deployStructure.envCloud,cloudDistCenter)
	printOpen("lastDeployedValuesYaml ${lastDeployedValuesYaml}", EchoLevel.ALL)
	if (lastDeployedValuesYaml!=null) {
		Map alm=lastDeployedValuesYaml["alm"]
		printOpen("The apps ${alm} ", EchoLevel.ALL)
		Map almApp=alm["apps"]
		Map almAppEnvQualifier=almApp["envQualifier"]
		//Este map es el que contiene la info del micro desplegado
		Map stable=null
		Map old=null
		Map newApp=null
		
		boolean isReady=false
		def body=null
		def values=null
		def response=null
		
		stable=almAppEnvQualifier["stable"]
		newApp=almAppEnvQualifier["new"]
		
		if (almAppEnvQualifier["stable"]!=null && newApp!=null) {	

			def preServices=alm['services']
			printOpen("The preServices ara ${preServices}", EchoLevel.ALL)
			def services=preServices['envQualifier']
			printOpen("The services ara ${services}", EchoLevel.ALL)
			//1 Assignar la ruta estable a los dos			
			services['stable'].remove("targetColour")
			values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
			
			body = [
				az: "${cloudDistCenter}",
				environment: "${environment}",
				values: "${values}"
			]
			printOpen("The body with the new  ${body}", EchoLevel.ALL)
			response=sendRequestToCloudApi("v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${artifactPom.getCloudAppName()}","v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, artifactPom)
			if (response.statusCode>300) throw new Exception("Deploy failed ${artifactPom.applicationName}")
			
			//2 Quitar la ruta estable al old
			settingNewColourToTheService(newApp["colour"],alm['services'],'stable')
			values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
			body = [
				az: "${cloudDistCenter}",
				environment: "${environment}",
				values: "${values}"
			]
			printOpen("The body with the new  ${body}", EchoLevel.ALL)
			response=sendRequestToCloudApi("v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${artifactPom.getCloudAppName()}","v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, artifactPom)
			if (response.statusCode>300) throw new Exception("Deploy failed ${artifactPom.applicationName}")
			
			//3 Poner 0 instancias... llamar al new -> stable
			//						  llamar al stable-> old con 0 instancias
			almAppEnvQualifier.remove("new")
			
			almAppEnvQualifier.put("stable",newApp)
			stable.put("replicas",0)
			almAppEnvQualifier.put("old",stable)
			

			
			values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
			body = [
				az: "${cloudDistCenter}",
				environment: "${environment}",
				values: "${values}"
			]
			
			printOpen("The body with the new  ${body}", EchoLevel.ALL)
			response=sendRequestToCloudApi("v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${artifactPom.getCloudAppName()}","v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, artifactPom)
			if (response.statusCode>300) throw new Exception("Deploy failed ${artifactPom.applicationName}")
			
			//Tenemos estable 
		}else if (newApp!=null && stable==null){
			//Solo tenemos una version que pasara de new a stable
			String definitiveRoute=BmxUtilities.calculateArtifactId(artifactPom,pipeline.branchStructure).toLowerCase()
			
			addStableToTheService(newApp["colour"], alm['services'], 'stable',definitiveRoute) //deberia ser el routing definito)
			
				
				
			values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
			
			body = [
				az: "${cloudDistCenter}",
				environment: "${environment}",
				values: "${values}"
			]
			
			printOpen("The body with the new  ${body}", EchoLevel.ALL)
			response=sendRequestToCloudApi("v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${artifactPom.getCloudAppName()}","v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, artifactPom)
			if (response.statusCode>300) throw new Exception("Deploy failed ${artifactPom.applicationName}")
		
			
		}else if (stable!=null){
			//no tenemos que hacer nada solo esta la app estable			
			
			printOpen("Setteamos la ruta estable contra la el micro estable ${stable}", EchoLevel.ALL)
			
			
			def preServices=alm['services']
			printOpen("The preServices ara ${preServices}", EchoLevel.ALL)
			def services=preServices['envQualifier']
			
			if (services['stable'].get("targetColour")!=stable['colour']) {
				printOpen("Tenemos servicios diferentes ", EchoLevel.ALL)
				services['stable'].put("targetColour",stable['colour'])
				
				values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
				
				body = [
					az: "${cloudDistCenter}",
					environment: "${environment}",
					values: "${values}"
				]
				printOpen("El body es de ${body}", EchoLevel.ALL)
				response=sendRequestToCloudApi("v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${artifactPom.getCloudAppName()}","v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, artifactPom)
				if (response.statusCode>300) throw new Exception("Deploy failed ${artifactPom.applicationName}")
			
			}
			
		}
	}
}
