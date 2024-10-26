import com.project.alm.EchoLevel
import com.project.alm.CloudApiResponse
import com.project.alm.ArtifactSubType
import com.project.alm.CloudUtils
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.CloudAppResources
import com.project.alm.BranchType

def generateResourceToTheEnvironment(def environment, def componentId, def appGar, CloudAppResources cloudResources, String cloudAppId) {
	
	CloudApiResponse response=sendRequestToCloudApi("v1/application/${cloudAppId}/component/${componentId}/environment/${environment}/resource",null,"GET",appGar,"",false,false)
	
	if (response.statusCode>=200 && response.statusCode<300) {
        printOpen("The resource has been generated before on ${environment}", EchoLevel.INFO)
	}else {
		if (response.statusCode==500 || response.statusCode==404) {
			def resource = [
				cpuLimit: "${cloudResources.getLimitsCPU(environment)}",
				cpuRequest: "${cloudResources.getRequestsCPU(environment)}",
				memoryLimit: "${cloudResources.getLimitsMemory(environment)}",
				memoryRequest: "${cloudResources.getRequestsMemory(environment)}",
				replicas: 1,
				storage: "1Gi"
			]		
		
			response=sendRequestToCloudApi("v1/application/${cloudAppId}/component/${componentId}/environment/${environment}/resource",resource,"POST",appGar,"",false,false)
			if (response.statusCode>=200 && response.statusCode<300) {
                printOpen("Resource generated on ${environment}", EchoLevel.INFO)
			}else {
				 throw new Exception("Error generating component !!!! ${response.body}")
			}		
		}else {
			throw new Exception("Error generating component !!!! ${response.body}")
		}		 
	}	
}

def generateResourcesToTheNewComponent(def componentId, def appGar, CloudAppResources cloudResources, String cloudAppId) {
	generateResourceToTheEnvironment('DEV',componentId,appGar,cloudResources,cloudAppId)
	generateResourceToTheEnvironment('TST',componentId,appGar,cloudResources,cloudAppId)
	generateResourceToTheEnvironment('PRE',componentId,appGar,cloudResources,cloudAppId)
	generateResourceToTheEnvironment('PRO',componentId,appGar,cloudResources,cloudAppId)
}

def generateResourcesToTheNewComponentOnlyDEV(def componentId, def appGar, CloudAppResources cloudResources, String cloudAppId) {
	generateResourceToTheEnvironment('DEV',componentId,appGar,cloudResources,cloudAppId)
}

def getChartId(String application) {
	if (env.Cloud_CUSTOM_LIVENESSPROBE_APPLICATIONS.contains(application)) {
		return env.Cloud_CUSTOM_LIVENESSPROBE_CHART_ID
	}
	return env.Cloud_CHART_ID
}

def generatePostCreateComponent(String application, String componentName, boolean isWiremock = false) {
	def body= [
			chart: [ id:"${isWiremock?env.WIREMOCK_CHART_ID:getChartId(application)}"], //Identificador del component
			componentType: "PAAS",
			deploymentArea: "INTRANET",
			loggingStack: "alm0",
			loginType: "NONE",
			mutualTLS: false,
			name: componentName,
			scmType: "NONE",
			serviceType: "INTERNAL",
			typeDR: "AA",
			version: [ id: "${isWiremock?env.WIREMOCK_DOCKER_ID:env.Cloud_VERSION}"]		//Este tambien puede llegar a variar
	]
	
	return body	
}

/**
 * Script que permite desplegar en bluemix el micro
 * @param pomXml 
 * @param pipeline
 * @return
 */
def call( PomXmlStructure pomXml, PipelineData pipeline, CloudAppResources cloudResources, boolean isBBDD = false, boolean isWiremock = false) {

    def cloudAppMetadata = CloudUtils.calculateCloudComponentName(pipeline, pomXml, [isBBDD: isBBDD, isWiremock: isWiremock])

	String aplicacion = cloudAppMetadata.aplicacion
	
	def nameComponentInCloud = cloudAppMetadata.cloudComponentName
		
	def idComponentInCloud = 0

    printOpen("The application is ${aplicacion} and the name of the cloud component is ${nameComponentInCloud}", EchoLevel.INFO)
	//Send to Cloud and get id of the component
	CloudApiResponse response=sendRequestToCloudApi("v1/application/${pomXml.getCloudAppId()}/component",null,"GET","${pomXml.getCloudAppName()}","",false,false, pipeline, pomXml)
	if (response.statusCode>=200 && response.statusCode<300) {

		if (response.body!=null && response.body.size()>=1) {
			response.body.find { 
			
				if (pipeline.branchStructure.branchType == BranchType.FEATURE && !isBBDD){
					String suffixComponent=""
					String suffixSampleComponenteApp=""
					if (pomXml.artifactSubType!=ArtifactSubType.MICRO_APP && pomXml.artifactSubType!=ArtifactSubType.MICRO_ARCH) {
						//Es una sample app
						suffixSampleComponenteApp="S"
					}
					suffixComponent= suffixComponent+pipeline.branchStructure.featureNumber
					
					if (suffixComponent=="" || suffixComponent==null) suffixComponent="666"
					else {
						//Nos hemos encontrado que las features us eliminan nuestra app
						if (suffixComponent!=null) {
							suffixComponent= CloudUtils.normalizeCloudArtifactName(pipeline.branchStructure.featureNumber)
						}
					}
					
					if (((aplicacion+pomXml.getArtifactMajorVersion()+suffixSampleComponenteApp).length() + suffixComponent.length())>33) {
						suffixComponent=suffixComponent.reverse().take(33-(aplicacion+pomXml.getArtifactMajorVersion()+suffixSampleComponenteApp).length()).reverse()
					}
					
					if (it.name.startsWith((aplicacion+pomXml.getArtifactMajorVersion()+ suffixSampleComponenteApp + suffixComponent ).toUpperCase())) {
						 idComponentInCloud=it.id //Solo nos interesa la primera parte 
						 return true
					}
				}else {
					if (it.name.equals(nameComponentInCloud.toUpperCase())) {
						idComponentInCloud=it.id
						return true
					}
				}
				return false
			}
		}
		
		if (idComponentInCloud==0) {
			//Generate component in Cloud
			def body=generatePostCreateComponent(aplicacion, nameComponentInCloud.toUpperCase(), isWiremock)
			response=sendRequestToCloudApi("v1/application/${pomXml.getCloudAppId()}/component",body,"POST","${pomXml.getCloudAppName()}","",false,false, pipeline, pomXml)
			printOpen("The response is of ${response.statusCode} and the response body ${response.body} ", EchoLevel.DEBUG)
			
			if (response.statusCode>=200 && response.statusCode<300) {
                printOpen("Component created before in Cloud with id = ${response.body.id}", EchoLevel.INFO)
				idComponentInCloud=response.body.id
				//Add resources to the resource
				if (pipeline.branchStructure.branchType == BranchType.FEATURE && !isBBDD){
					generateResourcesToTheNewComponentOnlyDEV(response.body.id,"${pomXml.getCloudAppName()}",cloudResources,pomXml.getCloudAppId())
				}else {
					generateResourcesToTheNewComponent(response.body.id,"${pomXml.getCloudAppName()}",cloudResources,pomXml.getCloudAppId())					
				}
			}else {
				throw new Exception("Error generating component !!!! ${response.body}")			
			}
		}else {
			//Componente generado
            printOpen("Component created before in Cloud ${idComponentInCloud}", EchoLevel.INFO)
			if (pipeline.branchStructure.branchType == BranchType.FEATURE && !isBBDD){
				generateResourcesToTheNewComponentOnlyDEV(idComponentInCloud,"${pomXml.getCloudAppName()}",cloudResources,pomXml.getCloudAppId())
			}else {
				generateResourcesToTheNewComponent(idComponentInCloud,"${pomXml.getCloudAppName()}",cloudResources,pomXml.getCloudAppId())
			}
		}
				
	}else { 
		throw new Exception("Error generating component !!!! ${response.body}")
	}
	
	return idComponentInCloud
}