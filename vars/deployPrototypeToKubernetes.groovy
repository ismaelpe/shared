import com.project.alm.*


def validateMicroIsUpAnReturnError(String url,PomXmlStructure pomXml) {

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


    printOpen("El micro esta UP/DOWN ${microIsUp}", EchoLevel.ALL)

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




/**
 * Siempre despliega un prototipo en DEV
 * @param artifactPom objeto pom
 * @param pipeline pipeline data
 * @return
 */
def call(PomXmlStructure artifactPom, PipelineData pipeline) {
	String group = artifactPom.groupId
	String artifact = artifactPom.artifactName
	String version = artifactPom.artifactVersion
	String environmentDest = GlobalVars.DEV_ENVIRONMENT
	deployPrototypeToKubernetes(group,artifact,version,artifactPom,pipeline,environmentDest)
}

def generatePrototypeEvent(def environment, def componentName) {
	componentName=componentName.toUpperCase()
	def response = sendRequestToAlm3MS(
		'GET',
		"${GlobalVars.URL_CATALOGO_ALM_PRO}/audit/PRT/${componentName}",
		null,
		"${GlobalVars.CATALOGO_ALM_ENV}")
	if (response.status == 200) {
		//def json=readJSON text: response.content
		def json=response.content
		if (json.size()>0) {
			json.each{
				if (environment.equalsIgnoreCase(it.description)) {
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
	Date expiringDate = new Date().clearTime()+GlobalVars.PROTOTYPE_DAYS_LIVE
	def nuevoEvento = [
		   type: "PRT",
		   subtype: "PRT",
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
 * Despliega un prototipo en el entorno que se le pasa como parametro
 * @param group grupo del artefacto prototipo
 * @param artifact nombre del artefacto prototipo
 * @param version version del artefacto prototipo
 * @param artifactPom objeto pom
 * @param pipeline pipeline data
 * @param environmentDest viene desde GLOBALVARS. ejemplo GlobalVars.PRE_ENVIRONMENT
 * @return
 */
def call(String group,String artifact, String version, PomXmlStructure artifactPom, PipelineData pipeline, String environmentDest) {

    long wholeCallDuration
    long wholeCallStartMillis = new Date().getTime()

    KpiAlmEvent kpiAlmEvent =
        new KpiAlmEvent(
            artifactPom, pipeline,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.Cloud_DEPLOY_PROTOTYPE)

	printOpen("DEPLOY PROTO PARA : group is ${group}, artifact is ${artifact},version is ${version} ", EchoLevel.ALL)

	pipeline.pipelineStructure.resultPipelineData.groupParam = group
	pipeline.pipelineStructure.resultPipelineData.artifactParam = artifact
	pipeline.pipelineStructure.resultPipelineData.versionParam = version
	
	def result = [messageDeploy: "", urlPrototype: ""]

	CloudDeployStructure deployStructure=new CloudDeployStructure('cxb-ab3cor','cxb-ab3app',environmentDest)

	def cloudAppMetadata = CloudUtils.calculateCloudComponentName(pipeline, artifactPom, [isBBDD: false])
	String componentName=cloudAppMetadata.cloudComponentName 
	
	printOpen("componentName a buscar es ${componentName}", EchoLevel.ALL)
	String componentId=generateArtifactInCloud(artifactPom, pipeline, generateCloudResources(environmentDest,false,componentName),false )
	generatePrototypeEvent(environmentDest,componentName)
	componentName=componentName.toLowerCase()	
	
	checkCloudAvailability(artifactPom,pipeline,"CALCULATE","DEPLOY")
	
	CloudApiResponse response=null
	
	String imageCloud=GlobalVars.Cloud_PROTOTYPE_IMAGE
	String version_Image=GlobalVars.Cloud_PROTOTYPE_IMAGE_VERSION

	//cacular path para el service expuesto 			
	String pathProto = artifactPom.getBmxAppId()+'-proto';
	String appName = artifactPom.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) )
	String garAppType  = pipeline.garArtifactType.name
	String domain = pipeline.domain
	String subDomain = pipeline.subDomain
	String company = pipeline.company
	pathProto=pathProto.toLowerCase()
	printOpen("pathProto is ${pathProto},appName is ${appName},garAppType is ${garAppType},domain is ${domain},subDomain is ${subDomain},company is ${company}", EchoLevel.ALL)
	printOpen("env is ${deployStructure.envCloud.toUpperCase()}", EchoLevel.ALL)
	
	
	String cloudDistCenter="ALL"
	
	Date fechaActual=new Date()
	def forceDeploy=fechaActual.getTime()
	
	printOpen("Delete deployment because it could exist ....", EchoLevel.ALL)

	def bodyDeploy=[
		az: "${cloudDistCenter}",
		environment: "${deployStructure.envCloud.toUpperCase()}",
		values:"deployment:\n  readinessProbe:\n    initialDelaySeconds: 120\n    periodSeconds: 60\n    timeoutSeconds: 50\n    failureThreshold: 5\n  livenessProbe:\n    initialDelaySeconds: 120\n    periodSeconds: 60\n    timeoutSeconds: 50\n    failureThreshold: 5\nlocal:\n  app:\n    enableNonMtls: true\n    ingress:\n      enabled: false\n      deploymentArea: alm\n    envVars:\n      - name: jvmConfig\n        value: \"-XX:MaxRAMPercentage=50.0\"\n      - name: ARTIFACT_ID\n        value: ${artifact}\n      - name: VERSION_ARTIFACT\n        value: ${version}\n      - name: GROUP_ID\n        value: ${group}\n      - name: ALM_APP_ID\n        value: ${appName}\n      - name: ALM_CENTER_ID\n        value: 1\n      - name: ALM_APP_TYPE\n        value: ${garAppType}\n      - name: ALM_ENVIRONMENT\n        value: ${environmentDest.toUpperCase()}\n      - name: ALM_APP_DOMAIN\n        value: ${domain}\n      - name: ALM_APP_SUBDOMAIN\n        value: ${subDomain}\n      - name: ALM_APP_COMPANY\n        value: ${company}\n      - name: JAVA_OPTS\n        value: '-Dspring.cloud.config.failFast=true'\n      - name: nonProxyHosts\n        value: '*.cxb-pasdev-tst|*.cxb-ab3app-${environmentDest.toLowerCase()}|*.cxb-ab3cor-${environmentDest.toLowerCase()}'\n      - name: http.additionalNonProxyHosts\n        value: 'cxb-pasdev-${environmentDest.toLowerCase()},cxb-ab3app-dev,cxb-ab3cor-${environmentDest.toLowerCase()}'\n      - name: NO_PROXY\n        value: cxb-ab3cor-dev\n      - name: CF_INSTANCE_INDEX\n        value: 1\n      - name: spring.cloud.config.failFast\n        value: true\n      - name: SPRING_PROFILES_ACTIVE\n        value: standalone\n      - name: ALM_Cloud_ENVIRONMENT\n        value: ${environmentDest.toLowerCase()}\n    secrets:\nalm:\n  app:\n    loggingElkStack: alm0\n    replicas: 1\n    instance: ${componentName}\n    name: demoarqalm\n  resources:\n    requests:\n      memory: 450Mi\n      cpu: 5m\n    limits:\n       memory: 450Mi\n       cpu: 400m\n  apps:\n    envQualifier:\n      stable:\n        id: ${componentName}\n        colour: B\n        image: ${imageCloud}:${version_Image}\n        version: 1.0.1\n        stable: false\n        new: false\n        replicas: 1\n        readinessProbePath: /actuator/health\n        livenessProbePath: /actuator/health\n        envVars:\n          SPRING_PROFILES_ACTIVE: standalone\n          forceDeploy: ${forceDeploy}\n        requests_memory: 350Mi\n        requests_cpu: 5m\n        limits_memory: 350Mi\n        limits_cpu: 400m\n  services:\n    envQualifier:\n      stable:\n        id: ${pathProto}\n        targetColour: B\n"
	]
	
	CloudApiResponse responseDelete=sendRequestToCloudApi("v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",bodyDeploy,"DELETE","${artifactPom.getCloudAppName()}","v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",false,true, pipeline, artifactPom)

	printOpen("response delete proto is ${responseDelete.statusCode}", EchoLevel.ALL)
	
	printOpen("Deploy Image ...", EchoLevel.ALL)
	
	response = sendRequestToCloudApi("v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",bodyDeploy,"POST","${artifactPom.getCloudAppName()}","v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",true,true, pipeline, artifactPom)
	
	if (response.statusCode<200 || response.statusCode>300) {
        long wholeCallEndMillis = new Date().getTime()
        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

        kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

        createMaximoAndThrow.cloudDeployException(pipeline, artifactPom, response)

	}

	pipeline.componentId=componentId
					
	boolean isReady=waitCloudDeploymentReady(artifactPom,pipeline,deployStructure,'B',"${cloudDistCenter}")
	
	if (!isReady) {
		response = sendRequestToCloudApi("v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",bodyDeploy,"DELETE","${artifactPom.getCloudAppName()}","v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",false,true, pipeline, artifactPom)

        long wholeCallEndMillis = new Date().getTime()
        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

        kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

		throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")

	} else {
        String microUrl = deployStructure.getUrlActuatorPrefixTesting() + deployStructure.getUrlSuffixTesting()+"/"
		
		microUrl = microUrl + pathProto
		
        if (validateMicroIsUpAnReturnError(microUrl,artifactPom)) {
			result.messageDeploy = "Deploy OK"
			result.urlPrototype =  deployStructure.getUrlPrefixApiGateway() + "/" + pathProto + "/actuator/info" 
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
				
	return result
	
}
