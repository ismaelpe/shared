import com.project.alm.*
import org.yaml.snakeyaml.Yaml


def settingNewColourToTheService(def colour, def services, def idService) {
	Map almAppEnvQualifier=services["envQualifier"]
	
	Map service=almAppEnvQualifier[idService]
	service["targetColour"]=colour	
}

def settingNewColourToAllTheService(def colour, def services) {
	Map almAppEnvQualifier=services["envQualifier"]
	almAppEnvQualifier.each{
		def valueService=it.value
		valueService["targetColour"]=colour
	}
}

def getNewService(def services) {
	Map almAppEnvQualifier=services["envQualifier"]
	return almAppEnvQualifier["new"]
}

def call(PomXmlStructure pomXml, PipelineData pipeline, String environment, boolean ignoreExistingAncient, boolean forceAllCenters) {
	environment=environment.toUpperCase()

    CloudDeployStructure deployStructure=new CloudDeployStructure('cxb-ab3cor','cxb-ab3app',environment)

	boolean resultStatus=false
	//No es necesario añadir la consulta a catalogo ya que solo es en consulta no en alta
	pipeline.componentId=generateArtifactInCloud(pomXml,pipeline,CloudUtils.generateCloudResources(deployStructure.memory,environment.toUpperCase(),pomXml.isArchProject()))
	printOpen("The component to undeploy is ${pipeline.componentId}", EchoLevel.ALL)
	
	String cloudDistCenter="ALL" //Centros a los que hacer el rollback
	String templateCenter="ALL" //Centro del que obtener la plantilla del rollback
	
	if (pipeline.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2) {
		cloudDistCenter="AZ1"
		templateCenter="AZ1"
	} else if (pipeline.distributionModePRO == DistributionModePRO.CONCLUDED) {
		cloudDistCenter="ALL"
		templateCenter="ALL"
	}
	
	/*
     [local:[app:[ingress:[enabled:false, mtls:[enabled:false, needsSystemRoute:true, route:null, needsSpecialVerifyDepth:false, verifyDepth:2]], 
     envVars:[[name:ALM_APP_ID, value:demoPipelineMicr], [name:ALM_CENTER_ID, value:1], [name:ALM_APP_TYPE, value:SRV.MS], [name:ALM_ENVIRONMENT, value:dev], [name:ALM_APP_DOMAIN, value:null], [name:ALM_APP_SUBDOMAIN, value:NO_SUBDOMAIN], [name:ALM_APP_COMPANY, value:null], [name:JAVA_OPTS, value:-Dspring.cloud.config.failFast=true], [name:nonProxyHosts, value:*.cxb-ab3cor-dev|*.cxb-ab3app-dev|*.api.tst.internal.cer.project.com|*.api.pre.internal.cer.project.com|*.api.pro.internal.cer.project.com], [name:http.additionalNonProxyHosts, value:cxb-ab3cor-dev,cxb-ab3app-dev,api.tst.internal.cer.project.com,api.pre.internal.cer.project.com,api.pro.internal.cer.project.com], [name:NO_PROXY, value:cxb-ab3cor-dev], [name:ALM_BLUE_GREEN, value:B], [name:CF_INSTANCE_INDEX, value:1], [name:SPRING_PROFILES_ACTIVE, value:cloud,dev,cloud,clouddev], [name:ALM_Cloud_ENVIRONMENT, value:dev]]]], 
     alm:[app:[loggingElkStack:alm0, replicas:1, instance:demoPipelineMicr1, name:demoPipelineMicr], resources:[requests:[memory:128Mi, cpu:50m], limits:[memory:512Mi, cpu:450m]], 
            apps:[envQualifier:[stable:[id:demopipelinemicr1-b, colour:B, image:pro-registry.pro.caas.project.com/containers/ab3app/demopipelinemicr1:1.73.0-SNAPSHOT-A, version:1.73.0-SNAPSHOT, stable:false, new:false, replicas:1, requests_memory:128Mi, requests_cpu:50m, limits_memory:512Mi, limits_cpu:450m]]], 
            services:[envQualifier:[stable:[id:demopipelinemicr-micro-1-dev, targetColour:B], new:[id:new-demopipelinemicr-micro-1-dev, targetColour:B]]]]]
	 */
	//We need the actual status of the deployment
	Map lastDeployedValuesYaml=generateValuesYamlLastCloudDeployment(pomXml,pipeline,deployStructure.envCloud,templateCenter)
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

		if (almAppEnvQualifier["old"]!=null && stable!=null) {
			old=almAppEnvQualifier["old"]	
			//Arrancaremos con las instancias del stable
			printOpen("Setting the number of replicas for the old app ${stable["replicas"]} ", EchoLevel.ALL)
			old.put("replicas",stable["replicas"])		
		
			values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
					
			//ahora tenemos que arrancar esta app 
			body = [
				az: "${cloudDistCenter}",
				environment: "${environment}",
				values: "${values}"
			]
			
			printOpen("The body is ${body}", EchoLevel.ALL)
			//Intentamos arrancar el micro a 0 
			
			response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
			
			printOpen("el resultado es ${response}", EchoLevel.ALL)
			if (response.statusCode>300) throw new Exception("Deploy failed ${pomXml.applicationName}")
			else {
				try {
					isReady=waitCloudDeploymentReady(pomXml,pipeline,deployStructure,old["colour"],cloudDistCenter)
				}catch(Exception e) {
					if (e.getMessage()!=null && e.getMessage().contains("DEPLOY FALLIDO")) {
						response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
						
						if (response.statusCode>300) throw new Exception("Deploy failed ${pomXml.applicationName}")
						//Esperamos que el deployment finalice
						isReady=waitCloudDeploymentReady(pomXml,pipeline,deployStructure,old["colour"],cloudDistCenter)				
						
					}
				}
				
				//Ya hemos arrancado el micro antiguo.... ahora toca  arrancar la ruta new contra el old
				settingNewColourToTheService(old["colour"],alm['services'],'new')
				
				
				//writeYaml file: 'values.yaml', data: lastDeployedValuesYaml, overwrite: true
				
				values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
				
				body = [
					az: "${cloudDistCenter}",
					environment: "${environment}",
					values: "${values}"
				]
				
				printOpen("The body with the new  ${body}", EchoLevel.ALL)
				response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
				if (response.statusCode>300) throw new Exception("Deploy failed ${pomXml.applicationName}")
				//Revisar si ha arrancado
			
				Map mapPathToMicro=getNewService(alm['services'])
				String pathToMicro = mapPathToMicro['id']
		
				if (pomXml.isArchProject()) {
						pathToMicro="arch-service/"+pathToMicro
				}
					
				String microUrl = deployStructure.getUrlActuatorPrefixTesting() + deployStructure.getUrlSuffixTesting()+"/"+pathToMicro					
				boolean microIsUp = validateMicroIsUp(microUrl)
				
				
				//Apuntar todas las rutas al nuevo micro
				settingNewColourToAllTheService(old["colour"],alm['services'])
				
				values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
				
				body = [
					az: "${cloudDistCenter}",
					environment: "${environment}",
					values: "${values}"
				]
				printOpen("The body with the new  ${body}", EchoLevel.ALL)
				response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
				if (response.statusCode>300) throw new Exception("Deploy failed ${pomXml.applicationName}")
				
				
				//Eliminar el micro stable
			    printOpen("Removing the old one", EchoLevel.ALL)
				almAppEnvQualifier.put("stable",almAppEnvQualifier['old'])
				almAppEnvQualifier.remove('old')
				
				values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
				
				body = [
					az: "${cloudDistCenter}",
					environment: "${environment}",
					values: "${values}"
				]
				printOpen("The body with the new  ${body}", EchoLevel.ALL)
				response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
				if (response.statusCode>300) throw new Exception("Deploy failed ${pomXml.applicationName}")
				
		   }
		   almPipelineStageCloneToOcp(pomXml, pipeline)
		   resultStatus=true 
		}else{
			printOpen("There is no old app", EchoLevel.ALL)
			//No tenemos el  old... no esta consolidado
			if (stable!=null && newApp!=null) {
				//Tenemos que liquidar el newApp
				def preServices=alm['services']
				def services=preServices['envQualifier']
				
				if (services!=null && services['stable'].get("targetColour")==newApp["colour"]) {
					//El micro apunta al valor que toca
					services['stable'].remove("targetColour")
					values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
					
					body = [
						az: "${cloudDistCenter}",
						environment: "${environment}",
						values: "${values}"
					]
					printOpen("The body with the new  ${body}", EchoLevel.ALL)
					response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
					if (response.statusCode>300) throw new Exception("Deploy failed ${pomXml.applicationName}")
					
				}
				
				//Apuntar todas las rutas al nuevo micro
				settingNewColourToAllTheService(stable["colour"],alm['services'])
				values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
				
				body = [
					az: "${cloudDistCenter}",
					environment: "${environment}",
					values: "${values}"
				]
				printOpen("The body with the new  ${body}", EchoLevel.ALL)
				response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
				if (response.statusCode>300) throw new Exception("Deploy failed ${pomXml.applicationName}")
					
				almAppEnvQualifier.remove("new")
				values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
				
				body = [
					az: "${cloudDistCenter}",
					environment: "${environment}",
					values: "${values}"
				]
				printOpen("The body with the new  ${body}", EchoLevel.ALL)
				
				response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
				if (response.statusCode>300) {
					 throw new Exception("Deploy failed ${pomXml.applicationName}")
				}
				resultStatus=true
				
				almPipelineStageCloneToOcp(pomXml, pipeline)
				
				
			}else {
				printOpen("Only one artifact no old, no new... only stable... only undeploy if is not consolidated", EchoLevel.ALL)
				//Si el micro no esta consolidado tienes un problema con lo que se tiene que eliminar el micro
				def preServices=alm['services']
				def services=preServices['envQualifier']
				//Para revisar si esta consolidado se tiene que mirar hacia donde apuntan los services
				if (services!=null && services['stable'].get("targetColour")=='K' || !"PRO".equals(environment)) {
					printOpen("Tenemos que hacer undeploy no esta consolidado, no da servicio", EchoLevel.ALL)
					
					printOpen("CloudAppName ${pomXml.getCloudAppName()} componentCloud-${pipeline.componentId} ", EchoLevel.ALL)
					
					values=new Yaml().dumpAsMap(lastDeployedValuesYaml)
					
					body = [
						az: "${cloudDistCenter}",
						environment: "${environment}",
						values: ""
					]
										
					response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"DELETE","${pomXml.getCloudAppName()}","",true,true)
					printOpen("El statusCode es de ${response.statusCode} ${response.body} el body es de ${body}", EchoLevel.ALL)
					
					if (response.statusCode>300 && response.statusCode!=404) throw new Exception("UnDeploy failed ${pomXml.applicationName}")
					resultStatus=true
					
					componentName
					
					if (env.ENV_K8S_OCP!=null && env.ENV_K8S_OCP.contains(environment.toUpperCase())) {
						String componentName = MavenUtils.sanitizeArtifactName(pomXml.artifactName, pipeline.garArtifactType)
						componentName = componentName+pomXml.getArtifactMajorVersion()
						
						response=sendRequestToCloudApi("v1/api/application/PCLD_MIGRATED/${pomXml.getCloudAppName()}/component/${componentName}",null,"DELETE","${pomXml.getCloudAppName()}","",false,false)
						printOpen(" The status code of the delete ${response.statusCode}", EchoLevel.INFO)
						
						if (response.statusCode>=200 && response.statusCode<300) {
							printOpen(" Delete of the component ${componentName} from the app ${pomXml.getCloudAppName()} - OCP", EchoLevel.INFO)
						}
					}
				}

			}
		}
		
			
			
		//Si tenemos old lo primero es arrancar el micro y proceder con lo anterior

	}
    printOpen(" End UnDeploy stage ${resultStatus}", EchoLevel.ALL)
	return resultStatus
}


