import com.caixabank.absis3.*


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

        }else throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")

    }else throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")


    printOpen("Is the micro up? ${microIsUp}", EchoLevel.INFO)

    return microIsUp
}

def ICPAppResourcesCatMsv generateICPResources(String requestedCpu, String requestedMemory, String environment, boolean isArchProject, String type) {
	String namespace = isArchProject ? "AB3COR" : "AB3APP"

	ICPAppResourcesCatMsv icpResources=getSizesFromCatalog(namespace, type, "PRO", isArchProject, requestedMemory, requestedCpu, "S");
	icpResources.replicasSize="S"
	icpResources.memSize=requestedMemory
	icpResources.cpuSize=requestedCpu
	
	return icpResources
}

def buildArtifactOnIcp(PipelineData pipeline, PomXmlStructure pomXml, String requestURL, def body, String method, String aplicacionGAR, String pollingRequestUrl )  {
	ICPApiResponse responseIcp=null
	try {

		try {

			responseIcp = sendRequestToICPApi(requestURL,body,method,aplicacionGAR,pollingRequestUrl,true,false, pipeline, pomXml)

		} catch(java.io.NotSerializableException e) {

			responseIcp = sendRequestToICPApi(requestURL,body,method,aplicacionGAR,pollingRequestUrl,true,false, pipeline, pomXml)

		}
		
		if (responseIcp.statusCode==500) {

			printOpen("Error puntual... vamos a probar suerte otra vez ${responseIcp.statusCode}", EchoLevel.ERROR)
			responseIcp = sendRequestToICPApi(requestURL,body,method,aplicacionGAR,pollingRequestUrl,true,false, pipeline, pomXml)

		}

	} catch(Exception e) {

		createMaximoAndThrow.deployBuildDockerImageFailure(pipeline, pomXml, e)

	}
	
	return responseIcp
}

def generateResourcesInICP(String environment, String componentName, String icpAppName, ICPAppResourcesCatMsv icpResources) {
	environment = environment.toUpperCase()
	componentName = componentName.toUpperCase()

	def resource = [
		cpuLimit: "${icpResources.getLimitsCPUPersonalized()}",
		cpuRequest: "${icpResources.getRequestCPUPersonalized()}",
		memoryLimit: "${icpResources.getLimitsMemoryPersonalized()}",
		memoryRequest: "${icpResources.getRequestMemoryPersonalized()}",
		replicas: 1,
		storage: "1Gi"
	]

	response=sendRequestToICPApi("v1/adm/application/PCLD/${icpAppName}/component/${componentName}/environment/${environment}/resource",resource,"POST",icpAppName,"",false,false)
	if (response.statusCode>=200 && response.statusCode<300) {
		printOpen("Resource generated on ${environment} with size [cpu:${icpResources.cpuSize}, memory:${icpResources.memSize}]", EchoLevel.INFO)
	} else if(response.statusCode>=409 && response.statusCode<423) {
		printOpen("Resource already exists on ${environment}. We are going to update it...", EchoLevel.INFO)
		response=sendRequestToICPApi("v1/adm/application/PCLD/${icpAppName}/component/${componentName}/environment/${environment}/resource",resource,"PUT",icpAppName,"",false,false)
		if (response.statusCode>=200 && response.statusCode<300) {
			printOpen("Resource updated on ${environment} with size [cpu:${icpResources.cpuSize}, memory:${icpResources.memSize}]", EchoLevel.INFO)
		} else {
			 throw new Exception("Error updating resource! ${response.body}")
		}
	} else {
		 throw new Exception("Error generating resource! ${response.body}")
	}
}

def getComponentInformationFromOriginal(PomXmlStructure pomXml, PipelineData pipeline) {
	String originalComponentNameInICP = ICPUtils.calculateICPComponentName(pipeline, pomXml).icpComponentName
	String stressComponentNameInICP = ICPUtils.calculateICPComponentName(pipeline, pomXml, [isStressMicro: true]).icpComponentName
	
	ICPApiResponse response=sendRequestToICPApi("v2/api/application/PCLD/${pomXml.getICPAppName()}/component/${originalComponentNameInICP.toUpperCase()}",null,"GET","${pomXml.getICPAppName()}","",false,false, pipeline, pomXml)
	if (response.statusCode>=200 && response.statusCode<300 && response.body != null) {
		return [
			chart: response.body.chart,
			componentType: response.body.componentType,
			deploymentArea: response.body.deploymentArea,
			loggingStack: response.body.loggingStack,
			loginType: response.body.loginType,
			mutualTLS: response.body.mutualTLS,
			name: stressComponentNameInICP.toUpperCase(),
			scmType: response.body.scmType,
			serviceType: response.body.serviceType,
			typeDR: response.body.typeDR,
			version: response.body.version
		]
	} else {
		throw new Exception("Error getting original micro information! ${response.body}")
	}

}

def generateComponentInICP(PomXmlStructure pomXml, PipelineData pipeline, ICPAppResourcesCatMsv icpResources, String environment) {
	def icpAppMetadata = ICPUtils.calculateICPComponentName(pipeline, pomXml, [isStressMicro: true])
	String aplicacion = icpAppMetadata.aplicacion
	String nameComponentInICP = icpAppMetadata.icpComponentName

	printOpen("The application is ${aplicacion} and the name of the icp component is ${nameComponentInICP}", EchoLevel.INFO)

	def idComponentInICP = 0
	ICPApiResponse response=sendRequestToICPApi("v2/api/application/PCLD/${pomXml.getICPAppName()}/component/${nameComponentInICP.toUpperCase()}",null,"GET","${pomXml.getICPAppName()}","",false,false, pipeline, pomXml)
	if (response.statusCode>=200 && response.statusCode<300 && response.body!=null && nameComponentInICP.toUpperCase().equals(response.body.name)) {
		existsInICP = true
		printOpen("The application already exists in ICP with id = ${response.body.id}. Generating resources...")
		idComponentInICP = response.body.id
		generateResourcesInICP(environment, nameComponentInICP, pomXml.getICPAppName(), icpResources)
	} else {
		//Generate component in ICP
		def body = getComponentInformationFromOriginal(pomXml, pipeline)
		response=sendRequestToICPApi("v1/api/application/PCLD/${pomXml.getICPAppName()}/component",body,"POST","${pomXml.getICPAppName()}","",false,false, pipeline, pomXml)
		printOpen("The response is of ${response.statusCode} and the response body ${response.body} ", EchoLevel.DEBUG)
		
		if (response.statusCode>=200 && response.statusCode<300) {
			printOpen("Component created before in ICP with id = ${response.body.id}", EchoLevel.INFO)
			idComponentInICP=response.body.id
			//Add resources to the resource
			generateResourcesInICP(environment, nameComponentInICP, pomXml.getICPAppName(), icpResources)
		}else {
			throw new Exception("Error generating component !!!! ${response.body}")
		}
	}
	
	return idComponentInICP
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
	ICPAppResourcesCatMsv icpResources = generateICPResources(requestedCPU, requestedMemory, environmentDest, archProject, garAppType)
	_deployStressMicroToKubernetes(group,artifact,version,artifactPom,pipeline,environmentDest, icpResources)
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
def _deployStressMicroToKubernetes(String group, String artifact, String version, PomXmlStructure artifactPom, PipelineData pipeline, String environmentDest, ICPAppResourcesCatMsv icpResources) {

    long wholeCallDuration
    long wholeCallStartMillis = new Date().getTime()

    KpiAlmEvent kpiAlmEvent =
        new KpiAlmEvent(
            artifactPom, pipeline,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.ICP_DEPLOY_STRESS_MICRO)

	printOpen("Deploy stress micro : group is ${group}, artifact is ${artifact},version is ${version} ", EchoLevel.INFO)

	ICPDeployStructure deployStructure=new ICPDeployStructure('cxb-ab3cor','cxb-ab3app',environmentDest)
	pipeline.deployStructure = deployStructure

	def icpAppMetadata = ICPUtils.calculateICPComponentName(pipeline, artifactPom, [isStressMicro: true])
	String componentName=icpAppMetadata.icpComponentName 
	String applicationName=icpAppMetadata.aplicacion.toLowerCase()
	
	printOpen("componentName a buscar es ${componentName}", EchoLevel.DEBUG)
	String componentId=generateComponentInICP(artifactPom, pipeline, icpResources, environmentDest)
	componentName=componentName.toLowerCase()
	
	checkICPAvailability(artifactPom,pipeline,"CALCULATE","DEPLOY")
	
	ICPApiResponse response=null
	
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
	
	//DEPLOY ICP
	printOpen("Deploying stress micro...")
	String icpDistCenter="ALL"
	
	Date fechaActual=new Date()
	def forceDeploy=fechaActual.getTime()
	
	Map valuesDeployed = getLastAppInfoICP(environmentDest, appNameWithVersion.toUpperCase(), namespace, "ALL")
	if (valuesDeployed == null) throw new Exception("The app ${appNameWithVersion} doesn't exists on ICP")
	printAppICP(valuesDeployed)
	//adding stress to spring profiles
	String springProfilesStr = valuesDeployed.absis.apps.envQualifier.stable.envVars.SPRING_PROFILES_ACTIVE
	printOpen("Current spring profiles: "+springProfilesStr, EchoLevel.DEBUG)
	def springProfiles = springProfilesStr.split(",").toList()
	if (!springProfiles.contains("stress")) {
		springProfiles.add("stress")
		springProfilesStr = springProfiles.join(",")
		valuesDeployed.absis.apps.envQualifier.stable.envVars.SPRING_PROFILES_ACTIVE = springProfilesStr
		printOpen("New spring profiles: "+springProfilesStr, EchoLevel.DEBUG)
	} else {
		printOpen("Profile [stress] is already defined", EchoLevel.DEBUG)
	}

	if(icpResources.jvmArgs!=null) valuesDeployed.absis.apps.envQualifier.stable.envVars.jvmConfig = icpResources.jvmArgs
	//changing app name
	valuesDeployed.absis.app.instance = componentName
	valuesDeployed.absis.apps.envQualifier.stable.id = componentName
    //changing resources
	valuesDeployed.absis.apps.envQualifier.stable.requests_memory = icpResources.getRequestMemoryPersonalized()
	valuesDeployed.absis.apps.envQualifier.stable.requests_cpu = icpResources.getRequestCPUPersonalized()
	valuesDeployed.absis.apps.envQualifier.stable.limits_memory = icpResources.getLimitsMemoryPersonalized()
	valuesDeployed.absis.apps.envQualifier.stable.limits_cpu = icpResources.getLimitsCPUPersonalized()
	//changing service name
	valuesDeployed.absis.services.envQualifier.stable.id = pathMicro
	//removing additional deployments & colour
	valuesDeployed.absis.apps.envQualifier.stable.colour = "S"
	valuesDeployed.absis.apps.envQualifier = ["stable": valuesDeployed.absis.apps.envQualifier.stable]
	if(valuesDeployed.absis.services.envQualifier.stable.targetColour != null) valuesDeployed.absis.services.envQualifier.stable.remove("targetColour")
	valuesDeployed.absis.services.envQualifier = ["stable": valuesDeployed.absis.services.envQualifier.stable]

	def bodyDeploy=[
		az: "${icpDistCenter}",
		environment: "${deployStructure.envICP.toUpperCase()}",
		//values: "${groovy.json.JsonOutput.toJson(valuesDeployed)}"
		values: "${objectsParseUtils.toYamlString(valuesDeployed)}"
	]
	
	printOpen("Deploy Image ...", EchoLevel.DEBUG)
	
	response = sendRequestToICPApi("v1/api/application/PCLD/${artifactPom.getICPAppName()}/component/${componentName.toUpperCase()}/deploy",bodyDeploy,"POST","${artifactPom.getICPAppName()}","v1/api/application/PCLD/${artifactPom.getICPAppName()}/component/${componentName.toUpperCase()}/deploy",true,true, pipeline, artifactPom)
	
	if (response.statusCode<200 || response.statusCode>300) {
        long wholeCallEndMillis = new Date().getTime()
        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

        kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

        createMaximoAndThrow.icpDeployException(pipeline, artifactPom, response)

	}

	pipeline.componentId=componentId
					
	boolean isReady=waitICPDeploymentReady(artifactPom,pipeline,deployStructure,'B',"${icpDistCenter}")
	
	if (!isReady) {
		response = sendRequestToICPApi("v1/api/application/PCLD/${artifactPom.getICPAppName()}/component/${componentName.toUpperCase()}/deploy",bodyDeploy,"DELETE","${artifactPom.getICPAppName()}","v1/api/application/PCLD/${artifactPom.getICPAppName()}/component/${componentName.toUpperCase()}/deploy",false,true, pipeline, artifactPom)

        long wholeCallEndMillis = new Date().getTime()
        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

        kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

		throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")

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

			throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")
		}
	}
	

    long wholeCallEndMillis = new Date().getTime()
    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

    kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))
				
}
