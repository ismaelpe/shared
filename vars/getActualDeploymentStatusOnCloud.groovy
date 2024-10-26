import com.project.alm.*
import com.project.alm.Cloudk8sComponentDeploymentInfo
import com.project.alm.BmxUtilities
import com.project.alm.Cloudk8sComponentInfo
import com.project.alm.Cloudk8sComponentServiceInfo
import com.project.alm.CloudDeployStructure
import com.project.alm.Cloudk8sActualStatusInfo
import com.project.alm.BranchType
import com.project.alm.CloudApiResponse
import com.project.alm.GlobalVars



def getCloudStatus(PomXmlStructure pomXml, PipelineData pipeline,CloudDeployStructure deployStructure, String distributionCenter) {
	CloudApiResponse response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/environment/${deployStructure.envCloud.toUpperCase()}/availabilityzone/${distributionCenter}/status",null,"GET","${pomXml.getCloudAppName()}","",false,false, pipeline, pomXml)
    printOpen("The status API of the app is ${response.body}", EchoLevel.DEBUG)
		
	if (response.statusCode>=200 && response.statusCode<300) {

        printOpen("Vamos a analizar el body", EchoLevel.DEBUG)
		
        Cloudk8sComponentInfoMult k8sComponentInfo = generateActualDeploymentOnCloud(response.body)

        printOpen("result ${k8sComponentInfo.toString()}", EchoLevel.DEBUG)

		return k8sComponentInfo

	} else return null
}



/**
 * Permite devolver el estado del status de una aplicacion en kubernetes
 * @param pomXml Contiene el identificador del pomXml
 * @param pipeline Contiene la informacion calculada de en el pipeline
 * @param deployStructure Informacion del deploy
 * @return
 */
def call(PomXmlStructure pomXml, PipelineData pipeline,CloudDeployStructure deployStructure) {
	
	return getCloudStatus(pomXml, pipeline, deployStructure, "ALL")

}

def call(PomXmlStructure pomXml, PipelineData pipeline,CloudDeployStructure deployStructure, String distributionCenter) {
	
	return getCloudStatus(pomXml, pipeline, deployStructure, distributionCenter)

}

def call(PomXmlStructure pomXml, PipelineData pipeline,CloudDeployStructure deployStructure, boolean stableMap) {
	return getActualDeploymentStatusOnCloud(pomXml,pipeline,deployStructure,stableMap,"ALL")
}

def call(PomXmlStructure pomXml, PipelineData pipeline,CloudDeployStructure deployStructure, boolean stableMap, String distributionCenter) {
	return getActualDeploymentStatusOnCloud(pomXml,pipeline,deployStructure,stableMap,distributionCenter,false)
}

/**
 * Permite devolver el estado del status de una aplicacion en kubernetes
 * @param pomXml Contiene el identificador del pomXml
 * @param pipeline Contiene la informacion calculada de en el pipeline
 * @param deployStructure Informacion del deploy
 * @param stableMap indica que tenemos el valor a devolver
 * @return
 */
def call(PomXmlStructure pomXml, PipelineData pipeline,CloudDeployStructure deployStructure, boolean stableMap, String distributionCenter,boolean searchForStable) {
	
	 //Si es feature no nos interesa el estado actual, lo vamos a fundir otra vez
	
	
	
	 Cloudk8sActualStatusInfo cloudActualStatusInfo=new Cloudk8sActualStatusInfo()
	 
	 //Validar primero si existe el ultimo deploy
	 CloudApiResponse response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/environment/${deployStructure.envCloud.toUpperCase()}/availabilityzone/${distributionCenter}/status",null,"GET","${pomXml.getCloudAppName()}","",false,false, pipeline, pomXml)
	
	  if (response.statusCode>=200 && response.statusCode<300) {
		  
		 if (response.body==null) {
             printOpen("Application doesn't exist. This is a first deploy.", EchoLevel.INFO)
			 return cloudActualStatusInfo
		 }
		 
		 if (distributionCenter!="ALL") {
			 if (response.body.size()==1 && response.body.getAt(0).items!=null && response.body.getAt(0).items.size()!=0) {
                 printOpen("This application already exists. This is not a first deploy.", EchoLevel.INFO)
                 printOpen("Current config: ${response.body.getAt(0).items}", EchoLevel.DEBUG)
			 }else {
                 printOpen("Application doesn't exist. This is a first deploy.", EchoLevel.INFO)
				 return cloudActualStatusInfo
			 }
		 }
		 if (distributionCenter=="ALL") {
			 if (response.body.size()==2 && response.body.getAt(0).items!=null && response.body.getAt(0).items.size()!=0) {
                 printOpen("This application already exists. This is not a first deploy.", EchoLevel.INFO)
                 printOpen("Current config: ${response.body.getAt(0).items}", EchoLevel.DEBUG)
			 }else {
                 printOpen("Application doesn't exist. This is a first deploy.", EchoLevel.INFO)
				 return cloudActualStatusInfo
			 }
		 }
	 }
	 
		 
	 Map lastDeployedValuesYaml=generateValuesYamlLastCloudDeployment(pomXml,pipeline,deployStructure.envCloud,distributionCenter)
	 
	 if (lastDeployedValuesYaml==null) return cloudActualStatusInfo
	 
	 Map alm=lastDeployedValuesYaml["alm"]
	 Map almApp=alm["apps"]
	 Map almAppEnvQualifier=almApp["envQualifier"]
	 //Este map es el que contiene la info del micro desplegado
	 Map stable=null
	 
	 if (almAppEnvQualifier["new"]!=null && searchForStable==false) {
		 stable=almAppEnvQualifier["new"]
	 }else stable=almAppEnvQualifier["stable"]
	 
	 if (stable!=null) {
		 //Vamos a coger los datos que nos interesan de este map ya que van a ser la version stable de la nueva promocion
		 //color
		 //instancia
		 //version
		 //readinessProbePath
		 //livenessProbePath
         //replicas
		 cloudActualStatusInfo.currentColour=stable["colour"]
		 cloudActualStatusInfo.currentVersion=stable["version"]
		 cloudActualStatusInfo.currentImage=stable["image"]
		 cloudActualStatusInfo.readinessProbePath=stable["readinessProbePath"]
		 cloudActualStatusInfo.livenessProbePath=stable["livenessProbePath"]
		 cloudActualStatusInfo.envVars=getEnvVars(stable["envVars"], lastDeployedValuesYaml["local"]["app"]["envVars"])
         cloudActualStatusInfo.replicas=stable["replicas"]
	 }

    printOpen("Stable ${stable}", EchoLevel.DEBUG)
		
	 return cloudActualStatusInfo
}

//Funcion para mover las variables genericas a variables de un deployment en particular en caso de usar la version 0.0.15 del chart de helm
def getEnvVars(Map deploymentEnvVars, List genericVars) {
	if (deploymentEnvVars == null || !deploymentEnvVars.containsKey("SPRING_PROFILES_ACTIVE")) {
		Map envVars = deploymentEnvVars
		if(envVars == null) {
			envVars = new HashMap<String, String>()
		}
		for (entry in genericVars) {
			if (entry.name == "SPRING_PROFILES_ACTIVE") {
				envVars.put(entry.name, entry.value)
			}
		}
		return envVars
	} else {
		return deploymentEnvVars
	}
}
