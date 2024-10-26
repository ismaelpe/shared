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

def generateCloudResources(String environment, boolean isArchProject, String artifactId ) {
	CloudAppResources cloudResources=new CloudAppResources()
	cloudResources.environment=environment
	cloudResources.isArchProject=isArchProject	
		
	cloudResources.replicasSize="S"	
	cloudResources.memSize="S"		
	cloudResources.cpuSize="S"
	
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




/**
 * Siempre despliega un wiremock en PRE
 * @param artifactPom objeto pom
 * @param pipeline pipeline data
 * @return
 */
def call(PomXmlStructure artifactPom, PipelineData pipeline, String environmentDest) {
	String group = artifactPom.groupId
	String artifact = artifactPom.artifactName
	String version = artifactPom.artifactVersion
	_deployWiremockServerToKubernetes(group,artifact,version,artifactPom,pipeline,environmentDest)
}

def generateWiremockEvent(def environment, def componentName) {
	def response = sendRequestToAlm3MS(
		'GET',
		"${GlobalVars.URL_CATALOGO_ALM_PRO}/audit/WIR/${componentName}",
		null,
		"${GlobalVars.CATALOGO_ALM_ENV}")
	if (response.status == 200) {
		//def json=readJSON text: response.content
		def json=response.content
		if (json.size()>0) {
			json.each{
				if (environment.equals(it.description)) {
					printOpen("Tenemos que eliminar este evento ${it.id} para el entorno ${environment}", EchoLevel.ALL)
					def responseUndeploy=sendRequestToAlm3MS( 'DELETE',
		                                                        "${GlobalVars.URL_CATALOGO_ALM_PRO}/audit/${it.id}",
		                                                        null,
		                                                        "${GlobalVars.CATALOGO_ALM_ENV}")
					if (responseUndeploy.status == 200) {
						printOpen("Elemento eliminado ${it.id} en el entorno ${environment}", EchoLevel.ALL)
					}
					
				}
			}
		}
	}else {
		printOpen("No podemos generar los eventos", EchoLevel.ALL)
	}
	//Damos de alta el nuevo evento
	Date today = new Date().clearTime()
	Date expiringDate = new Date().clearTime()+GlobalVars.WIREMOCK_DAYS_LIVE
	def nuevoEvento = [
		   type: "WIR",
		   subtype: "WIR",
		   application: componentName,
		   description: environment,
		   timestamp: today,
		   expiringDate: expiringDate,
		   user: "USER"
		]
	def responseHire = sendRequestToAlm3MS( 'PUT',
											   "${GlobalVars.URL_CATALOGO_ALM_PRO}/audit",
											   nuevoEvento,
												   "${GlobalVars.CATALOGO_ALM_ENV}")
	if (responseHire.status == 200) {
		printOpen("Evento Generado", EchoLevel.ALL)
	}else {
		printOpen("No se puede generar el evento", EchoLevel.ALL)
	}
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
def _deployWiremockServerToKubernetes(String group, String artifact, String version, PomXmlStructure artifactPom, PipelineData pipeline, String environmentDest) {

    long wholeCallDuration
    long wholeCallStartMillis = new Date().getTime()

    KpiAlmEvent kpiAlmEvent =
        new KpiAlmEvent(
            artifactPom, pipeline,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.Cloud_DEPLOY_WIREMOCK)

	printOpen("Deploy wiremock server : group is ${group}, artifact is ${artifact},version is ${version} ", EchoLevel.INFO)

	CloudDeployStructure deployStructure=new CloudDeployStructure('cxb-ab3cor','cxb-ab3app',environmentDest)
	pipeline.deployStructure = deployStructure

	def cloudAppMetadata = CloudUtils.calculateCloudComponentName(pipeline, artifactPom, [isWiremock: true])
	String componentName=cloudAppMetadata.cloudComponentName 
	String applicationName=cloudAppMetadata.aplicacion.toLowerCase()
	generateWiremockEvent(environmentDest,componentName.toUpperCase())
	
	printOpen("componentName a buscar es ${componentName}", EchoLevel.DEBUG)
	String componentId=generateArtifactInCloud(artifactPom, pipeline, generateCloudResources(environmentDest,false,componentName),false ,true)
	componentName=componentName.toLowerCase()
	
	checkCloudAvailability(artifactPom,pipeline,"CALCULATE","DEPLOY")
	
	CloudApiResponse response=null
	
	//cacular path para el service expuesto 			
	String pathWiremock = artifactPom.getBmxAppId()+'-wiremock';
	String appName = artifactPom.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) )
	String garAppType  = pipeline.garArtifactType.name
	String domain = pipeline.domain
	String subDomain = pipeline.subDomain
	String company = pipeline.company
	pathWiremock=pathWiremock.toLowerCase()
	printOpen("pathWiremock is ${pathWiremock},appName is ${appName},garAppType is ${garAppType},domain is ${domain},subDomain is ${subDomain},company is ${company}, componentId is ${componentId}", EchoLevel.DEBUG)
	
	//BUILD DOCKER IMAGE
	def body = [
		extraArgs: "GROUP_ID=${artifactPom.groupId},VERSION_ARTIFACT=${artifactPom.artifactVersion},ARTIFACT_ID=${artifactPom.artifactMicro}",
		version: "${artifactPom.artifactVersion}"
	]
	
	long wholeBuildImageStartMillis = new Date().getTime()
	
	try {

		response = buildArtifactOnCloud(pipeline, artifactPom, "v1/api/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentName.toUpperCase()}/build", body, "POST", "${artifactPom.getCloudAppName()}", "v1/api/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentName.toUpperCase()}/build")

		long wholeBuildImageEndMillis = new Date().getTime()
		long wholeBuildImageDuration = wholeBuildImageEndMillis - wholeBuildImageStartMillis

		kpiLogger(
			new KpiAlmEvent(artifactPom, pipeline, KpiAlmEventStage.UNDEFINED, KpiAlmEventOperation.Cloud_BUILD_DOCKER_IMAGE)
				.callSuccess(wholeBuildImageDuration)
		)

	} catch (Exception e) {

		long wholeBuildImageEndMillis = new Date().getTime()
		long wholeBuildImageDuration = wholeBuildImageEndMillis - wholeBuildImageStartMillis

		kpiLogger(
			new KpiAlmEvent(artifactPom, pipeline, KpiAlmEventStage.UNDEFINED, KpiAlmEventOperation.Cloud_BUILD_DOCKER_IMAGE)
				.callAlmFail(wholeBuildImageDuration)
		)

		throw e

	}
	
	boolean buildOk = response.statusCode>=200 && response.statusCode<300
	printOpen("Build response with status ${response.statusCode}: ${response.body}", buildOk ? EchoLevel.DEBUG : EchoLevel.ERROR)

	if (!buildOk) {
        long wholeCallEndMillis = new Date().getTime()
        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

        kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

		createMaximoAndThrow.deployBuildDockerImageFailure(pipeline, artifactPom, response)

	}

	//DEPLOY Cloud
	String cloudDistCenter="ALL"
	
	Date fechaActual=new Date()
	def forceDeploy=fechaActual.getTime()
	
	def bodyDeploy=[
		az: "${cloudDistCenter}",
		environment: "${deployStructure.envCloud.toUpperCase()}",
		values:"deployment:\n  readinessProbe:\n    initialDelaySeconds: 120\n    periodSeconds: 60\n    timeoutSeconds: 50\n    failureThreshold: 5\n  livenessProbe:\n    initialDelaySeconds: 120\n    periodSeconds: 60\n    timeoutSeconds: 50\n    failureThreshold: 5\nlocal:\n  app:\n    enableNonMtls: true\n    ingress:\n      enabled: false\n      deploymentArea: alm\n    envVars:\n      - name: jvmConfig\n        value: \"-XX:MaxRAMPercentage=50.0\"\n      - name: ARTIFACT_ID\n        value: ${artifact}\n      - name: VERSION_ARTIFACT\n        value: ${version}\n      - name: GROUP_ID\n        value: ${group}\n      - name: ALM_APP_ID\n        value: ${appName}\n      - name: ALM_CENTER_ID\n        value: 1\n      - name: ALM_APP_TYPE\n        value: ${garAppType}\n      - name: ALM_ENVIRONMENT\n        value: ${environmentDest.toUpperCase()}\n      - name: ALM_APP_DOMAIN\n        value: ${domain}\n      - name: ALM_APP_SUBDOMAIN\n        value: ${subDomain}\n      - name: ALM_APP_COMPANY\n        value: ${company}\n      - name: JAVA_OPTS\n        value: '-Dspring.cloud.config.failFast=true'\n      - name: nonProxyHosts\n        value: '*.cxb-pasdev-tst|*.cxb-ab3app-${environmentDest.toLowerCase()}|*.cxb-ab3cor-${environmentDest.toLowerCase()}'\n      - name: http.additionalNonProxyHosts\n        value: 'cxb-pasdev-${environmentDest.toLowerCase()},cxb-ab3app-dev,cxb-ab3cor-${environmentDest.toLowerCase()}'\n      - name: NO_PROXY\n        value: cxb-ab3cor-dev\n      - name: CF_INSTANCE_INDEX\n        value: 1\n      - name: spring.cloud.config.failFast\n        value: true\n      - name: SPRING_PROFILES_ACTIVE\n        value: standalone\n      - name: ALM_Cloud_ENVIRONMENT\n        value: ${environmentDest.toLowerCase()}\n    secrets:\nalm:\n  app:\n    loggingElkStack: alm0\n    replicas: 1\n    instance: ${componentName}\n    name: ${applicationName}\n  resources:\n    requests:\n      memory: 450Mi\n      cpu: 5m\n    limits:\n       memory: 450Mi\n       cpu: 400m\n  apps:\n    envQualifier:\n      stable:\n        id: ${componentName}\n        colour: B\n        image: ${response.body.imageRepo1}:${artifactPom.artifactVersion}\n        version: ${artifactPom.artifactVersion}\n        stable: false\n        new: false\n        replicas: 1\n        readinessProbePath: /actuator/health\n        livenessProbePath: /actuator/health\n        envVars:\n          SPRING_PROFILES_ACTIVE: standalone\n          forceDeploy: ${forceDeploy}\n        requests_memory: 350Mi\n        requests_cpu: 5m\n        limits_memory: 350Mi\n        limits_cpu: 400m\n  services:\n    envQualifier:\n      stable:\n        id: ${pathWiremock}\n        targetColour: B\n"
	]
	
	printOpen("Deploy Image ...", EchoLevel.ALL)
	
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
			pathWiremock = "/arch-service/" + pathWiremock
		} else {
			pathWiremock = "/" + pathWiremock
		}
		microUrl += pathWiremock
		
        if (validateMicroIsUpAnReturnError(microUrl)) {
			String urlWiremock =  deployStructure.getUrlPrefixApiGateway("AZ1") + pathWiremock + "/actuator/info"
			String urlLink = "<a href='${urlWiremock}'>${urlWiremock}</a>"
			printOpen("Wiremock Server path: ${urlLink}")
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
