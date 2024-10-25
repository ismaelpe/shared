import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.BranchStructure
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.DeployStructure
import com.caixabank.absis3.AncientVersionInfo
import com.caixabank.absis3.Utilities
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.ICPDeployStructure
import com.caixabank.absis3.ICPAppResources
import com.caixabank.absis3.ICPUtils
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import java.util.Map
import java.util.HashMap

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK

def settingNewColourToTheService(def colour, def services, def idService) {
	Map absisAppEnvQualifier=services["envQualifier"]
	
	Map service=absisAppEnvQualifier[idService]
	service["targetColour"]=colour
}

def addStableToTheService(def colour, def services, def idService, def idStableService) {
	Map absisAppEnvQualifier=services["envQualifier"]
	
	Map service=new HashMap()
	absisAppEnvQualifier.put(idService,service)
	
	service.put("targetColour",colour)
	service.put("id",idStableService)

}


def call(PomXmlStructure artifactPom, PipelineData pipeline, boolean existAncient) {

    printOpen("close Blue Green ICP", EchoLevel.ALL)
	
	String environment= pipeline.bmxStructure.environment.toUpperCase()
	ICPDeployStructure deployStructure=new ICPDeployStructure('cxb-ab3cor','cxb-ab3app',environment)

	printOpen("The component to undeploy is ${pipeline.componentId}", EchoLevel.ALL)
	
	pipeline.componentId=generateArtifactInICP(artifactPom,pipeline,ICPUtils.generateICPResources(deployStructure.memory,environment.toUpperCase(),artifactPom.isArchProject()))
	
	
	String icpDistCenter="ALL"
	
	Map lastDeployedValuesYaml=generateValuesYamlLastICPDeployment(artifactPom,pipeline,deployStructure.envICP,icpDistCenter)
	printOpen("lastDeployedValuesYaml ${lastDeployedValuesYaml}", EchoLevel.ALL)
	if (lastDeployedValuesYaml!=null) {
		Map absis=lastDeployedValuesYaml["absis"]
		printOpen("The apps ${absis} ", EchoLevel.ALL)
		Map absisApp=absis["apps"]
		Map absisAppEnvQualifier=absisApp["envQualifier"]
		//Este map es el que contiene la info del micro desplegado
		Map stable=null
		Map old=null
		Map newApp=null
		
		boolean isReady=false
		def body=null
		def values=null
		def response=null
		
		stable=absisAppEnvQualifier["stable"]
		newApp=absisAppEnvQualifier["new"]
		
		if (absisAppEnvQualifier["stable"]!=null && newApp!=null) {	

			def preServices=absis['services']
			printOpen("The preServices ara ${preServices}", EchoLevel.ALL)
			def services=preServices['envQualifier']
			printOpen("The services ara ${services}", EchoLevel.ALL)
			//1 Assignar la ruta estable a los dos			
			services['stable'].remove("targetColour")
			values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
			
			body = [
				az: "${icpDistCenter}",
				environment: "${environment}",
				values: "${values}"
			]
			printOpen("The body with the new  ${body}", EchoLevel.ALL)
			response=sendRequestToICPApi("v1/application/PCLD/${artifactPom.getICPAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${artifactPom.getICPAppName()}","v1/application/PCLD/${artifactPom.getICPAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, artifactPom)
			if (response.statusCode>300) throw new Exception("Deploy failed ${artifactPom.applicationName}")
			
			//2 Quitar la ruta estable al old
			settingNewColourToTheService(newApp["colour"],absis['services'],'stable')
			values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
			body = [
				az: "${icpDistCenter}",
				environment: "${environment}",
				values: "${values}"
			]
			printOpen("The body with the new  ${body}", EchoLevel.ALL)
			response=sendRequestToICPApi("v1/application/PCLD/${artifactPom.getICPAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${artifactPom.getICPAppName()}","v1/application/PCLD/${artifactPom.getICPAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, artifactPom)
			if (response.statusCode>300) throw new Exception("Deploy failed ${artifactPom.applicationName}")
			
			//3 Poner 0 instancias... llamar al new -> stable
			//						  llamar al stable-> old con 0 instancias
			absisAppEnvQualifier.remove("new")
			
			absisAppEnvQualifier.put("stable",newApp)
			stable.put("replicas",0)
			absisAppEnvQualifier.put("old",stable)
			

			
			values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
			body = [
				az: "${icpDistCenter}",
				environment: "${environment}",
				values: "${values}"
			]
			
			printOpen("The body with the new  ${body}", EchoLevel.ALL)
			response=sendRequestToICPApi("v1/application/PCLD/${artifactPom.getICPAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${artifactPom.getICPAppName()}","v1/application/PCLD/${artifactPom.getICPAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, artifactPom)
			if (response.statusCode>300) throw new Exception("Deploy failed ${artifactPom.applicationName}")
			
			//Tenemos estable 
		}else if (newApp!=null && stable==null){
			//Solo tenemos una version que pasara de new a stable
			String definitiveRoute=BmxUtilities.calculateArtifactId(artifactPom,pipeline.branchStructure).toLowerCase()
			
			addStableToTheService(newApp["colour"], absis['services'], 'stable',definitiveRoute) //deberia ser el routing definito)
			
				
				
			values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
			
			body = [
				az: "${icpDistCenter}",
				environment: "${environment}",
				values: "${values}"
			]
			
			printOpen("The body with the new  ${body}", EchoLevel.ALL)
			response=sendRequestToICPApi("v1/application/PCLD/${artifactPom.getICPAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${artifactPom.getICPAppName()}","v1/application/PCLD/${artifactPom.getICPAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, artifactPom)
			if (response.statusCode>300) throw new Exception("Deploy failed ${artifactPom.applicationName}")
		
			
		}else if (stable!=null){
			//no tenemos que hacer nada solo esta la app estable			
			
			printOpen("Setteamos la ruta estable contra la el micro estable ${stable}", EchoLevel.ALL)
			
			
			def preServices=absis['services']
			printOpen("The preServices ara ${preServices}", EchoLevel.ALL)
			def services=preServices['envQualifier']
			
			if (services['stable'].get("targetColour")!=stable['colour']) {
				printOpen("Tenemos servicios diferentes ", EchoLevel.ALL)
				services['stable'].put("targetColour",stable['colour'])
				
				values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
				
				body = [
					az: "${icpDistCenter}",
					environment: "${environment}",
					values: "${values}"
				]
				printOpen("El body es de ${body}", EchoLevel.ALL)
				response=sendRequestToICPApi("v1/application/PCLD/${artifactPom.getICPAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${artifactPom.getICPAppName()}","v1/application/PCLD/${artifactPom.getICPAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, artifactPom)
				if (response.statusCode>300) throw new Exception("Deploy failed ${artifactPom.applicationName}")
			
			}
			
		}
	}
}
