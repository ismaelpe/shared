import com.project.alm.*


def validateMicroIsUpAnReturnError(String url) {

    boolean microIsUp = validateMicroIsUp(url)
	String fileOutput= CopyGlobalLibraryScript('',null,'outputCommand.json')
    if (microIsUp) {
        def commandResult="curl -L -k --write-out '%{http_code}' -o ${fileOutput} -k -s -X GET ${url}/actuator/info  "
        responseStatusCode= sh(script: commandResult,returnStdout: true)
        sh "cat ${fileOutput}"
        statusCode = responseStatusCode as Integer
        if (statusCode>=200 && statusCode<300) {
			return true

        }else throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")

    }else throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")


    printOpen("Is the micro up? ${microIsUp}", EchoLevel.INFO)

    return microIsUp
}

def CloudAppResourcesCatalog generateCloudResources(String requestedCpu, String requestedMemory, String environment, boolean isArchProject, String type) {
	String namespace = isArchProject ? "AB3COR" : "AB3APP"

	CloudAppResourcesCatalog cloudResources=getSizesFromCatalog(namespace, type, "PRO", isArchProject, requestedMemory, requestedCpu, "S");
	cloudResources.replicasSize="S"
	cloudResources.memSize=requestedMemory
	cloudResources.cpuSize=requestedCpu
	
	return cloudResources
}

def buildArtifactOnCloud(PipelineData pipeline, PomXmlStructure pomXml, String requestURL, def body, String method, String aplicacionGAR, String pollingRequestUrl )  {
	CloudApiResponse responseCloud=null
	try {

		try {

			responseCloud = sendRequestToCloudApi(requestURL,body,method,aplicacionGAR,pollingRequestUrl,true,false, pipeline, pomXml)

		} catch(java.io.NotSerializableException e) {

			responseCloud = sendRequestToCloudApi(requestURL,body,method,aplicacionGAR,pollingRequestUrl,true,false, pipeline, pomXml)

		}
		
		if (responseCloud.statusCode==500) {

			printOpen("Error puntual... vamos a probar suerte otra vez ${responseCloud.statusCode}", EchoLevel.ERROR)
			responseCloud = sendRequestToCloudApi(requestURL,body,method,aplicacionGAR,pollingRequestUrl,true,false, pipeline, pomXml)

		}

	} catch(Exception e) {

		createMaximoAndThrow.deployBuildDockerImageFailure(pipeline, pomXml, e)

	}
	
	return responseCloud
}

def generateResourcesInCloud(String environment, String componentName, String cloudAppName, CloudAppResourcesCatalog cloudResources) {
	environment = environment.toUpperCase()
	componentName = componentName.toUpperCase()

	def resource = [
		cpuLimit: "${cloudResources.getLimitsCPUPersonalized()}",
		cpuRequest: "${cloudResources.getRequestCPUPersonalized()}",
		memoryLimit: "${cloudResources.getLimitsMemoryPersonalized()}",
		memoryRequest: "${cloudResources.getRequestMemoryPersonalized()}",
		replicas: 1,
		storage: "1Gi"
	]

	response=sendRequestToCloudApi("v1/adm/application/PCLD/${cloudAppName}/component/${componentName}/environment/${environment}/resource",resource,"POST",cloudAppName,"",false,false)
	if (response.statusCode>=200 && response.statusCode<300) {
		printOpen("Resource generated on ${environment} with size [cpu:${cloudResources.cpuSize}, memory:${cloudResources.memSize}]", EchoLevel.INFO)
	} else if(response.statusCode>=409 && response.statusCode<423) {
		printOpen("Resource already exists on ${environment}. We are going to update it...", EchoLevel.INFO)
		response=sendRequestToCloudApi("v1/adm/application/PCLD/${cloudAppName}/component/${componentName}/environment/${environment}/resource",resource,"PUT",cloudAppName,"",false,false)
		if (response.statusCode>=200 && response.statusCode<300) {
			printOpen("Resource updated on ${environment} with size [cpu:${cloudResources.cpuSize}, memory:${cloudResources.memSize}]", EchoLevel.INFO)
		} else {
			 throw new Exception("Error updating resource! ${response.body}")
		}
	} else {
		 throw new Exception("Error generating resource! ${response.body}")
	}
}

def getComponentInformationFromOriginal(PomXmlStructure pomXml, PipelineData pipeline) {
	String originalComponentNameInCloud = CloudUtils.calculateCloudComponentName(pipeline, pomXml).cloudComponentName
	String stressComponentNameInCloud = CloudUtils.calculateCloudComponentName(pipeline, pomXml, [isStressMicro: true]).cloudComponentName
	
	CloudApiResponse response=sendRequestToCloudApi("v2/api/application/PCLD/${pomXml.getCloudAppName()}/component/${originalComponentNameInCloud.toUpperCase()}",null,"GET","${pomXml.getCloudAppName()}","",false,false, pipeline, pomXml)
	if (response.statusCode>=200 && response.statusCode<300 && response.body != null) {
		return [
			chart: response.body.chart,
			componentType: response.body.componentType,
			deploymentArea: response.body.deploymentArea,
			loggingStack: response.body.loggingStack,
			loginType: response.body.loginType,
			mutualTLS: response.body.mutualTLS,
			name: stressComponentNameInCloud.toUpperCase(),
			scmType: response.body.scmType,
			serviceType: response.body.serviceType,
			typeDR: response.body.typeDR,
			version: response.body.version
		]
	} else {
		throw new Exception("Error getting original micro information! ${response.body}")
	}

}

def generateComponentInCloud(PomXmlStructure pomXml, PipelineData pipeline, CloudAppResourcesCatalog cloudResources, String environment) {
	def cloudAppMetadata = CloudUtils.calculateCloudComponentName(pipeline, pomXml, [isStressMicro: true])
	String aplicacion = cloudAppMetadata.aplicacion
	String nameComponentInCloud = cloudAppMetadata.cloudComponentName

	printOpen("The application is ${aplicacion} and the name of the cloud component is ${nameComponentInCloud}", EchoLevel.INFO)

	def idComponentInCloud = 0
	CloudApiResponse response=sendRequestToCloudApi("v2/api/application/PCLD/${pomXml.getCloudAppName()}/component/${nameComponentInCloud.toUpperCase()}",null,"GET","${pomXml.getCloudAppName()}","",false,false, pipeline, pomXml)
	if (response.statusCode>=200 && response.statusCode<300 && response.body!=null && nameComponentInCloud.toUpperCase().equals(response.body.name)) {
		existsInCloud = true
		printOpen("The application already exists in Cloud with id = ${response.body.id}. Generating resources...")
		idComponentInCloud = response.body.id
		generateResourcesInCloud(environment, nameComponentInCloud, pomXml.getCloudAppName(), cloudResources)
	} else {
		//Generate component in Cloud
		def body = getComponentInformationFromOriginal(pomXml, pipeline)
		response=sendRequestToCloudApi("v1/api/application/PCLD/${pomXml.getCloudAppName()}/component",body,"POST","${pomXml.getCloudAppName()}","",false,false, pipeline, pomXml)
		printOpen("The response is of ${response.statusCode} and the response body ${response.body} ", EchoLevel.DEBUG)
		
		if (response.statusCode>=200 && response.statusCode<300) {
			printOpen("Component created before in Cloud with id = ${response.body.id}", EchoLevel.INFO)
			idComponentInCloud=response.body.id
			//Add resources to the resource
			generateResourcesInCloud(environment, nameComponentInCloud, pomXml.getCloudAppName(), cloudResources)
		}else {
			throw new Exception("Error generating component !!!! ${response.body}")
		}
	}
	
	return idComponentInCloud
}




/**
 * Siempre despliega un wiremock en PRE
 * @param artifactPom objeto pom
 * @param pipeline pipeline data
 * @return
 */
def call(PomXmlStructure artifactPom, PipelineData pipeline, String requestedCPU, String requestedMemory, String environmentDest) {
	String group = artifactPom.groupId
	String artifact = artifactPom.artifactName
	String version = artifactPom.artifactVersion
	String garAppType  = pipeline.garArtifactType.name
	boolean archProject = artifactPom.isArchProject();
	CloudAppResourcesCatalog cloudResources = generateCloudResources(requestedCPU, requestedMemory, environmentDest, archProject, garAppType)
	_deployStressMicroToKubernetes(group,artifact,version,artifactPom,pipeline,environmentDest, cloudResources)
}

/**
 * Despliega un servidor wiremock en el entorno que se le pasa como parametro
 * @param group grupo del artefacto wiremock
 * @param artifact nombre del artefacto wiremock
 * @param version version del artefacto wiremock
 * @param artifactPom objeto pom
 * @param pipeline pipeline data
 * @param environmentDest viene desde GLOBALVARS. ejemplo GlobalVars.PRE_ENVIRONMENT
 * @return
 */
def _deployStressMicroToKubernetes(String group, String artifact, String version, PomXmlStructure artifactPom, PipelineData pipeline, String environmentDest, CloudAppResourcesCatalog cloudResources) {

    long wholeCallDuration
    long wholeCallStartMillis = new Date().getTime()

    KpiAlmEvent kpiAlmEvent =
        new KpiAlmEvent(
            artifactPom, pipeline,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.Cloud_DEPLOY_STRESS_MICRO)

	printOpen("Deploy stress micro : group is ${group}, artifact is ${artifact},version is ${version} ", EchoLevel.INFO)

	CloudDeployStructure deployStructure=new CloudDeployStructure('cxb-ab3cor','cxb-ab3app',environmentDest)
	pipeline.deployStructure = deployStructure

	def cloudAppMetadata = CloudUtils.calculateCloudComponentName(pipeline, artifactPom, [isStressMicro: true])
	String componentName=cloudAppMetadata.cloudComponentName 
	String applicationName=cloudAppMetadata.aplicacion.toLowerCase()
	
	printOpen("componentName a buscar es ${componentName}", EchoLevel.DEBUG)
	String componentId=generateComponentInCloud(artifactPom, pipeline, cloudResources, environmentDest)
	componentName=componentName.toLowerCase()
	
	checkCloudAvailability(artifactPom,pipeline,"CALCULATE","DEPLOY")
	
	CloudApiResponse response=null
	
	//cacular path para el service expuesto 			
	String pathMicro = artifactPom.getBmxAppId()+'-stress';
	String appName = artifactPom.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) )
	String appNameWithVersion = appName + artifactPom.getMajorVersion()
	String garAppType  = pipeline.garArtifactType.name
	String domain = pipeline.domain
	String subDomain = pipeline.subDomain
	String company = pipeline.company
	String namespace = artifactPom.isArchProject() ? "ARCH" : "APP"
	pathMicro=pathMicro.toLowerCase()
	printOpen("pathMicro is ${pathMicro},appName is ${appName},garAppType is ${garAppType},domain is ${domain},subDomain is ${subDomain},company is ${company}, componentId is ${componentId}", EchoLevel.DEBUG)
	
	//DEPLOY Cloud
	printOpen("Deploying stress micro...")
	String cloudDistCenter="ALL"
	
	Date fechaActual=new Date()
	def forceDeploy=fechaActual.getTime()
	
	Map valuesDeployed = getLastAppInfoCloud(environmentDest, appNameWithVersion.toUpperCase(), namespace, "ALL")
	if (valuesDeployed == null) throw new Exception("The app ${appNameWithVersion} doesn't exists on Cloud")
	printAppCloud(valuesDeployed)
	//adding stress to spring profiles
	String springProfilesStr = valuesDeployed.alm.apps.envQualifier.stable.envVars.SPRING_PROFILES_ACTIVE
	printOpen("Current spring profiles: "+springProfilesStr, EchoLevel.DEBUG)
	def springProfiles = springProfilesStr.split(",").toList()
	if (!springProfiles.contains("stress")) {
		springProfiles.add("stress")
		springProfilesStr = springProfiles.join(",")
		valuesDeployed.alm.apps.envQualifier.stable.envVars.SPRING_PROFILES_ACTIVE = springProfilesStr
		printOpen("New spring profiles: "+springProfilesStr, EchoLevel.DEBUG)
	} else {
		printOpen("Profile [stress] is already defined", EchoLevel.DEBUG)
	}

	if(cloudResources.jvmArgs!=null) valuesDeployed.alm.apps.envQualifier.stable.envVars.jvmConfig = cloudResources.jvmArgs
	//changing app name
	valuesDeployed.alm.app.instance = componentName
	valuesDeployed.alm.apps.envQualifier.stable.id = componentName
    //changing resources
	valuesDeployed.alm.apps.envQualifier.stable.requests_memory = cloudResources.getRequestMemoryPersonalized()
	valuesDeployed.alm.apps.envQualifier.stable.requests_cpu = cloudResources.getRequestCPUPersonalized()
	valuesDeployed.alm.apps.envQualifier.stable.limits_memory = cloudResources.getLimitsMemoryPersonalized()
	valuesDeployed.alm.apps.envQualifier.stable.limits_cpu = cloudResources.getLimitsCPUPersonalized()
	//changing service name
	valuesDeployed.alm.services.envQualifier.stable.id = pathMicro
	//removing additional deployments & colour
	valuesDeployed.alm.apps.envQualifier.stable.colour = "S"
	valuesDeployed.alm.apps.envQualifier = ["stable": valuesDeployed.alm.apps.envQualifier.stable]
	if(valuesDeployed.alm.services.envQualifier.stable.targetColour != null) valuesDeployed.alm.services.envQualifier.stable.remove("targetColour")
	valuesDeployed.alm.services.envQualifier = ["stable": valuesDeployed.alm.services.envQualifier.stable]

	def bodyDeploy=[
		az: "${cloudDistCenter}",
		environment: "${deployStructure.envCloud.toUpperCase()}",
		//values: "${groovy.json.JsonOutput.toJson(valuesDeployed)}"
		values: "${objectsParseUtils.toYamlString(valuesDeployed)}"
	]
	
	printOpen("Deploy Image ...", EchoLevel.DEBUG)
	
	response = sendRequestToCloudApi("v1/api/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentName.toUpperCase()}/deploy",bodyDeploy,"POST","${artifactPom.getCloudAppName()}","v1/api/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentName.toUpperCase()}/deploy",true,true, pipeline, artifactPom)
	
	if (response.statusCode<200 || response.statusCode>300) {
        long wholeCallEndMillis = new Date().getTime()
        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

        kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

        createMaximoAndThrow.cloudDeployException(pipeline, artifactPom, response)

	}

	pipeline.componentId=componentId
					
	boolean isReady=waitCloudDeploymentReady(artifactPom,pipeline,deployStructure,'B',"${cloudDistCenter}")
	
	if (!isReady) {
		response = sendRequestToCloudApi("v1/api/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentName.toUpperCase()}/deploy",bodyDeploy,"DELETE","${artifactPom.getCloudAppName()}","v1/api/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentName.toUpperCase()}/deploy",false,true, pipeline, artifactPom)

        long wholeCallEndMillis = new Date().getTime()
        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

        kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

		throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")

	} else {
        String microUrl = deployStructure.getUrlActuatorPrefixTesting() + deployStructure.getUrlSuffixTesting()
		if (artifactPom.isArchProject()) {
			pathMicro = "/arch-service/" + pathMicro
		} else {
			pathMicro = "/" + pathMicro
		}
		microUrl += pathMicro
		
        if (validateMicroIsUpAnReturnError(microUrl)) {
			String urlMicro =  deployStructure.getUrlPrefixApiGateway("AZ1") + pathMicro + "/actuator/info"
			String urlLink = "<a href='${urlMicro}'>${urlMicro}</a>"
			printOpen("Stress micro path: ${urlLink}")
		} else {
            long wholeCallEndMillis = new Date().getTime()

            wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

            kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

			throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")
		}
	}
	

    long wholeCallEndMillis = new Date().getTime()
    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

    kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))
				
}
