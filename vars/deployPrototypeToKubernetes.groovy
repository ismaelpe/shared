import com.caixabank.absis3.*


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

        }else throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")

    }else throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")


    printOpen("El micro esta UP/DOWN ${microIsUp}", EchoLevel.ALL)

    return microIsUp
}

def generateICPResources(String environment, boolean isArchProject, String artifactId ) {
	ICPAppResources icpResources=new ICPAppResources()
	icpResources.environment=environment
	icpResources.isArchProject=isArchProject	
		
	icpResources.replicasSize="S"	
	icpResources.memSize="S"		
	icpResources.cpuSize="S"
	
	return icpResources
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
	def response = sendRequestToAbsis3MS(
		'GET',
		"${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/audit/PRT/${componentName}",
		null,
		"${GlobalVars.CATALOGO_ABSIS3_ENV}")
	if (response.status == 200) {
		//def json=readJSON text: response.content
		def json=response.content
		if (json.size()>0) {
			json.each{
				if (environment.equalsIgnoreCase(it.description)) {
					printOpen("Tenemos que eliminar este evento ${it.id} para el entorno ${environment}", EchoLevel.ALL)
					def responseUndeploy=sendRequestToAbsis3MS( 'DELETE',
		                                                        "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/audit/${it.id}",
		                                                        null,
		                                                        "${GlobalVars.CATALOGO_ABSIS3_ENV}")
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
	def responseHire = sendRequestToAbsis3MS( 'PUT',
											   "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/audit",
											   nuevoEvento,
												   "${GlobalVars.CATALOGO_ABSIS3_ENV}")
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
            KpiAlmEventOperation.ICP_DEPLOY_PROTOTYPE)

	printOpen("DEPLOY PROTO PARA : group is ${group}, artifact is ${artifact},version is ${version} ", EchoLevel.ALL)

	pipeline.pipelineStructure.resultPipelineData.groupParam = group
	pipeline.pipelineStructure.resultPipelineData.artifactParam = artifact
	pipeline.pipelineStructure.resultPipelineData.versionParam = version
	
	def result = [messageDeploy: "", urlPrototype: ""]

	ICPDeployStructure deployStructure=new ICPDeployStructure('cxb-ab3cor','cxb-ab3app',environmentDest)

	def icpAppMetadata = ICPUtils.calculateICPComponentName(pipeline, artifactPom, [isBBDD: false])
	String componentName=icpAppMetadata.icpComponentName 
	
	printOpen("componentName a buscar es ${componentName}", EchoLevel.ALL)
	String componentId=generateArtifactInICP(artifactPom, pipeline, generateICPResources(environmentDest,false,componentName),false )
	generatePrototypeEvent(environmentDest,componentName)
	componentName=componentName.toLowerCase()	
	
	checkICPAvailability(artifactPom,pipeline,"CALCULATE","DEPLOY")
	
	ICPApiResponse response=null
	
	String imageIcp=GlobalVars.ICP_PROTOTYPE_IMAGE
	String version_Image=GlobalVars.ICP_PROTOTYPE_IMAGE_VERSION

	//cacular path para el service expuesto 			
	String pathProto = artifactPom.getBmxAppId()+'-proto';
	String appName = artifactPom.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) )
	String garAppType  = pipeline.garArtifactType.name
	String domain = pipeline.domain
	String subDomain = pipeline.subDomain
	String company = pipeline.company
	pathProto=pathProto.toLowerCase()
	printOpen("pathProto is ${pathProto},appName is ${appName},garAppType is ${garAppType},domain is ${domain},subDomain is ${subDomain},company is ${company}", EchoLevel.ALL)
	printOpen("env is ${deployStructure.envICP.toUpperCase()}", EchoLevel.ALL)
	
	
	String icpDistCenter="ALL"
	
	Date fechaActual=new Date()
	def forceDeploy=fechaActual.getTime()
	
	printOpen("Delete deployment because it could exist ....", EchoLevel.ALL)

	def bodyDeploy=[
		az: "${icpDistCenter}",
		environment: "${deployStructure.envICP.toUpperCase()}",
		values:"deployment:\n  readinessProbe:\n    initialDelaySeconds: 120\n    periodSeconds: 60\n    timeoutSeconds: 50\n    failureThreshold: 5\n  livenessProbe:\n    initialDelaySeconds: 120\n    periodSeconds: 60\n    timeoutSeconds: 50\n    failureThreshold: 5\nlocal:\n  app:\n    enableNonMtls: true\n    ingress:\n      enabled: false\n      deploymentArea: absis\n    envVars:\n      - name: jvmConfig\n        value: \"-XX:MaxRAMPercentage=50.0\"\n      - name: ARTIFACT_ID\n        value: ${artifact}\n      - name: VERSION_ARTIFACT\n        value: ${version}\n      - name: GROUP_ID\n        value: ${group}\n      - name: ABSIS_APP_ID\n        value: ${appName}\n      - name: ABSIS_CENTER_ID\n        value: 1\n      - name: ABSIS_APP_TYPE\n        value: ${garAppType}\n      - name: ABSIS_ENVIRONMENT\n        value: ${environmentDest.toUpperCase()}\n      - name: ABSIS_APP_DOMAIN\n        value: ${domain}\n      - name: ABSIS_APP_SUBDOMAIN\n        value: ${subDomain}\n      - name: ABSIS_APP_COMPANY\n        value: ${company}\n      - name: JAVA_OPTS\n        value: '-Dspring.cloud.config.failFast=true'\n      - name: nonProxyHosts\n        value: '*.cxb-pasdev-tst|*.cxb-ab3app-${environmentDest.toLowerCase()}|*.cxb-ab3cor-${environmentDest.toLowerCase()}'\n      - name: http.additionalNonProxyHosts\n        value: 'cxb-pasdev-${environmentDest.toLowerCase()},cxb-ab3app-dev,cxb-ab3cor-${environmentDest.toLowerCase()}'\n      - name: NO_PROXY\n        value: cxb-ab3cor-dev\n      - name: CF_INSTANCE_INDEX\n        value: 1\n      - name: spring.cloud.config.failFast\n        value: true\n      - name: SPRING_PROFILES_ACTIVE\n        value: standalone\n      - name: ABSIS_ICP_ENVIRONMENT\n        value: ${environmentDest.toLowerCase()}\n    secrets:\nabsis:\n  app:\n    loggingElkStack: absis30\n    replicas: 1\n    instance: ${componentName}\n    name: demoarqalm\n  resources:\n    requests:\n      memory: 450Mi\n      cpu: 5m\n    limits:\n       memory: 450Mi\n       cpu: 400m\n  apps:\n    envQualifier:\n      stable:\n        id: ${componentName}\n        colour: B\n        image: ${imageIcp}:${version_Image}\n        version: 1.0.1\n        stable: false\n        new: false\n        replicas: 1\n        readinessProbePath: /actuator/health\n        livenessProbePath: /actuator/health\n        envVars:\n          SPRING_PROFILES_ACTIVE: standalone\n          forceDeploy: ${forceDeploy}\n        requests_memory: 350Mi\n        requests_cpu: 5m\n        limits_memory: 350Mi\n        limits_cpu: 400m\n  services:\n    envQualifier:\n      stable:\n        id: ${pathProto}\n        targetColour: B\n"
	]
	
	ICPApiResponse responseDelete=sendRequestToICPApi("v1/application/PCLD/${artifactPom.getICPAppName()}/component/${componentId}/deploy",bodyDeploy,"DELETE","${artifactPom.getICPAppName()}","v1/application/PCLD/${artifactPom.getICPAppName()}/component/${componentId}/deploy",false,true, pipeline, artifactPom)

	printOpen("response delete proto is ${responseDelete.statusCode}", EchoLevel.ALL)
	
	printOpen("Deploy Image ...", EchoLevel.ALL)
	
	response = sendRequestToICPApi("v1/application/PCLD/${artifactPom.getICPAppName()}/component/${componentId}/deploy",bodyDeploy,"POST","${artifactPom.getICPAppName()}","v1/application/PCLD/${artifactPom.getICPAppName()}/component/${componentId}/deploy",true,true, pipeline, artifactPom)
	
	if (response.statusCode<200 || response.statusCode>300) {
        long wholeCallEndMillis = new Date().getTime()
        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

        kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

        createMaximoAndThrow.icpDeployException(pipeline, artifactPom, response)

	}

	pipeline.componentId=componentId
					
	boolean isReady=waitICPDeploymentReady(artifactPom,pipeline,deployStructure,'B',"${icpDistCenter}")
	
	if (!isReady) {
		response = sendRequestToICPApi("v1/application/PCLD/${artifactPom.getICPAppName()}/component/${componentId}/deploy",bodyDeploy,"DELETE","${artifactPom.getICPAppName()}","v1/application/PCLD/${artifactPom.getICPAppName()}/component/${componentId}/deploy",false,true, pipeline, artifactPom)

        long wholeCallEndMillis = new Date().getTime()
        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

        kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

		throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")

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

			throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")
		}
	}

    long wholeCallEndMillis = new Date().getTime()
    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

    kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))
				
	return result
	
}
