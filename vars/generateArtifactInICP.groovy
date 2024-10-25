import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.ICPApiResponse
import com.caixabank.absis3.ArtifactSubType
import com.caixabank.absis3.ICPUtils
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.ICPAppResources
import com.caixabank.absis3.BranchType

def generateResourceToTheEnvironment(def environment, def componentId, def appGar, ICPAppResources icpResources, String icpAppId) {
	
	ICPApiResponse response=sendRequestToICPApi("v1/application/${icpAppId}/component/${componentId}/environment/${environment}/resource",null,"GET",appGar,"",false,false)
	
	if (response.statusCode>=200 && response.statusCode<300) {
        printOpen("The resource has been generated before on ${environment}", EchoLevel.INFO)
	}else {
		if (response.statusCode==500 || response.statusCode==404) {
			def resource = [
				cpuLimit: "${icpResources.getLimitsCPU(environment)}",
				cpuRequest: "${icpResources.getRequestsCPU(environment)}",
				memoryLimit: "${icpResources.getLimitsMemory(environment)}",
				memoryRequest: "${icpResources.getRequestsMemory(environment)}",
				replicas: 1,
				storage: "1Gi"
			]		
		
			response=sendRequestToICPApi("v1/application/${icpAppId}/component/${componentId}/environment/${environment}/resource",resource,"POST",appGar,"",false,false)
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

def generateResourcesToTheNewComponent(def componentId, def appGar, ICPAppResources icpResources, String icpAppId) {
	generateResourceToTheEnvironment('DEV',componentId,appGar,icpResources,icpAppId)
	generateResourceToTheEnvironment('TST',componentId,appGar,icpResources,icpAppId)
	generateResourceToTheEnvironment('PRE',componentId,appGar,icpResources,icpAppId)
	generateResourceToTheEnvironment('PRO',componentId,appGar,icpResources,icpAppId)
}

def generateResourcesToTheNewComponentOnlyDEV(def componentId, def appGar, ICPAppResources icpResources, String icpAppId) {
	generateResourceToTheEnvironment('DEV',componentId,appGar,icpResources,icpAppId)
}

def getChartId(String application) {
	if (env.ICP_CUSTOM_LIVENESSPROBE_APPLICATIONS.contains(application)) {
		return env.ICP_CUSTOM_LIVENESSPROBE_CHART_ID
	}
	return env.ICP_CHART_ID
}

def generatePostCreateComponent(String application, String componentName, boolean isWiremock = false) {
	def body= [
			chart: [ id:"${isWiremock?env.WIREMOCK_CHART_ID:getChartId(application)}"], //Identificador del component
			componentType: "PAAS",
			deploymentArea: "INTRANET",
			loggingStack: "absis30",
			loginType: "NONE",
			mutualTLS: false,
			name: componentName,
			scmType: "NONE",
			serviceType: "INTERNAL",
			typeDR: "AA",
			version: [ id: "${isWiremock?env.WIREMOCK_DOCKER_ID:env.ICP_VERSION}"]		//Este tambien puede llegar a variar
	]
	
	return body	
}

/**
 * Script que permite desplegar en bluemix el micro
 * @param pomXml 
 * @param pipeline
 * @return
 */
def call( PomXmlStructure pomXml, PipelineData pipeline, ICPAppResources icpResources, boolean isBBDD = false, boolean isWiremock = false) {

    def icpAppMetadata = ICPUtils.calculateICPComponentName(pipeline, pomXml, [isBBDD: isBBDD, isWiremock: isWiremock])

	String aplicacion = icpAppMetadata.aplicacion
	
	def nameComponentInICP = icpAppMetadata.icpComponentName
		
	def idComponentInICP = 0

    printOpen("The application is ${aplicacion} and the name of the icp component is ${nameComponentInICP}", EchoLevel.INFO)
	//Send to ICP and get id of the component
	ICPApiResponse response=sendRequestToICPApi("v1/application/${pomXml.getICPAppId()}/component",null,"GET","${pomXml.getICPAppName()}","",false,false, pipeline, pomXml)
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
							suffixComponent= ICPUtils.normalizeICPArtifactName(pipeline.branchStructure.featureNumber)
						}
					}
					
					if (((aplicacion+pomXml.getArtifactMajorVersion()+suffixSampleComponenteApp).length() + suffixComponent.length())>33) {
						suffixComponent=suffixComponent.reverse().take(33-(aplicacion+pomXml.getArtifactMajorVersion()+suffixSampleComponenteApp).length()).reverse()
					}
					
					if (it.name.startsWith((aplicacion+pomXml.getArtifactMajorVersion()+ suffixSampleComponenteApp + suffixComponent ).toUpperCase())) {
						 idComponentInICP=it.id //Solo nos interesa la primera parte 
						 return true
					}
				}else {
					if (it.name.equals(nameComponentInICP.toUpperCase())) {
						idComponentInICP=it.id
						return true
					}
				}
				return false
			}
		}
		
		if (idComponentInICP==0) {
			//Generate component in ICP
			def body=generatePostCreateComponent(aplicacion, nameComponentInICP.toUpperCase(), isWiremock)
			response=sendRequestToICPApi("v1/application/${pomXml.getICPAppId()}/component",body,"POST","${pomXml.getICPAppName()}","",false,false, pipeline, pomXml)
			printOpen("The response is of ${response.statusCode} and the response body ${response.body} ", EchoLevel.DEBUG)
			
			if (response.statusCode>=200 && response.statusCode<300) {
                printOpen("Component created before in ICP with id = ${response.body.id}", EchoLevel.INFO)
				idComponentInICP=response.body.id
				//Add resources to the resource
				if (pipeline.branchStructure.branchType == BranchType.FEATURE && !isBBDD){
					generateResourcesToTheNewComponentOnlyDEV(response.body.id,"${pomXml.getICPAppName()}",icpResources,pomXml.getICPAppId())
				}else {
					generateResourcesToTheNewComponent(response.body.id,"${pomXml.getICPAppName()}",icpResources,pomXml.getICPAppId())					
				}
			}else {
				throw new Exception("Error generating component !!!! ${response.body}")			
			}
		}else {
			//Componente generado
            printOpen("Component created before in ICP ${idComponentInICP}", EchoLevel.INFO)
			if (pipeline.branchStructure.branchType == BranchType.FEATURE && !isBBDD){
				generateResourcesToTheNewComponentOnlyDEV(idComponentInICP,"${pomXml.getICPAppName()}",icpResources,pomXml.getICPAppId())
			}else {
				generateResourcesToTheNewComponent(idComponentInICP,"${pomXml.getICPAppName()}",icpResources,pomXml.getICPAppId())
			}
		}
				
	}else { 
		throw new Exception("Error generating component !!!! ${response.body}")
	}
	
	return idComponentInICP
}