import com.caixabank.absis3.*
import com.caixabank.absis3.ICPk8sComponentDeploymentInfo
import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.ICPk8sComponentInfo
import com.caixabank.absis3.ICPk8sComponentServiceInfo
import com.caixabank.absis3.ICPDeployStructure
import com.caixabank.absis3.ICPk8sActualStatusInfo
import com.caixabank.absis3.BranchType
import com.caixabank.absis3.ICPApiResponse
import com.caixabank.absis3.GlobalVars



def getICPStatus(PomXmlStructure pomXml, PipelineData pipeline,ICPDeployStructure deployStructure, String distributionCenter) {
	ICPApiResponse response=sendRequestToICPApi("v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/environment/${deployStructure.envICP.toUpperCase()}/availabilityzone/${distributionCenter}/status",null,"GET","${pomXml.getICPAppName()}","",false,false, pipeline, pomXml)
    printOpen("The status API of the app is ${response.body}", EchoLevel.DEBUG)
		
	if (response.statusCode>=200 && response.statusCode<300) {

        printOpen("Vamos a analizar el body", EchoLevel.DEBUG)
		
        ICPk8sComponentInfoMult k8sComponentInfo = generateActualDeploymentOnICP(response.body)

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
def call(PomXmlStructure pomXml, PipelineData pipeline,ICPDeployStructure deployStructure) {
	
	return getICPStatus(pomXml, pipeline, deployStructure, "ALL")

}

def call(PomXmlStructure pomXml, PipelineData pipeline,ICPDeployStructure deployStructure, String distributionCenter) {
	
	return getICPStatus(pomXml, pipeline, deployStructure, distributionCenter)

}

def call(PomXmlStructure pomXml, PipelineData pipeline,ICPDeployStructure deployStructure, boolean stableMap) {
	return getActualDeploymentStatusOnICP(pomXml,pipeline,deployStructure,stableMap,"ALL")
}

def call(PomXmlStructure pomXml, PipelineData pipeline,ICPDeployStructure deployStructure, boolean stableMap, String distributionCenter) {
	return getActualDeploymentStatusOnICP(pomXml,pipeline,deployStructure,stableMap,distributionCenter,false)
}

/**
 * Permite devolver el estado del status de una aplicacion en kubernetes
 * @param pomXml Contiene el identificador del pomXml
 * @param pipeline Contiene la informacion calculada de en el pipeline
 * @param deployStructure Informacion del deploy
 * @param stableMap indica que tenemos el valor a devolver
 * @return
 */
def call(PomXmlStructure pomXml, PipelineData pipeline,ICPDeployStructure deployStructure, boolean stableMap, String distributionCenter,boolean searchForStable) {
	
	 //Si es feature no nos interesa el estado actual, lo vamos a fundir otra vez
	
	
	
	 ICPk8sActualStatusInfo icpActualStatusInfo=new ICPk8sActualStatusInfo()
	 
	 //Validar primero si existe el ultimo deploy
	 ICPApiResponse response=sendRequestToICPApi("v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/environment/${deployStructure.envICP.toUpperCase()}/availabilityzone/${distributionCenter}/status",null,"GET","${pomXml.getICPAppName()}","",false,false, pipeline, pomXml)
	
	  if (response.statusCode>=200 && response.statusCode<300) {
		  
		 if (response.body==null) {
             printOpen("Application doesn't exist. This is a first deploy.", EchoLevel.INFO)
			 return icpActualStatusInfo
		 }
		 
		 if (distributionCenter!="ALL") {
			 if (response.body.size()==1 && response.body.getAt(0).items!=null && response.body.getAt(0).items.size()!=0) {
                 printOpen("This application already exists. This is not a first deploy.", EchoLevel.INFO)
                 printOpen("Current config: ${response.body.getAt(0).items}", EchoLevel.DEBUG)
			 }else {
                 printOpen("Application doesn't exist. This is a first deploy.", EchoLevel.INFO)
				 return icpActualStatusInfo
			 }
		 }
		 if (distributionCenter=="ALL") {
			 if (response.body.size()==2 && response.body.getAt(0).items!=null && response.body.getAt(0).items.size()!=0) {
                 printOpen("This application already exists. This is not a first deploy.", EchoLevel.INFO)
                 printOpen("Current config: ${response.body.getAt(0).items}", EchoLevel.DEBUG)
			 }else {
                 printOpen("Application doesn't exist. This is a first deploy.", EchoLevel.INFO)
				 return icpActualStatusInfo
			 }
		 }
	 }
	 
		 
	 Map lastDeployedValuesYaml=generateValuesYamlLastICPDeployment(pomXml,pipeline,deployStructure.envICP,distributionCenter)
	 
	 if (lastDeployedValuesYaml==null) return icpActualStatusInfo
	 
	 Map absis=lastDeployedValuesYaml["absis"]
	 Map absisApp=absis["apps"]
	 Map absisAppEnvQualifier=absisApp["envQualifier"]
	 //Este map es el que contiene la info del micro desplegado
	 Map stable=null
	 
	 if (absisAppEnvQualifier["new"]!=null && searchForStable==false) {
		 stable=absisAppEnvQualifier["new"]
	 }else stable=absisAppEnvQualifier["stable"]
	 
	 if (stable!=null) {
		 //Vamos a coger los datos que nos interesan de este map ya que van a ser la version stable de la nueva promocion
		 //color
		 //instancia
		 //version
		 //readinessProbePath
		 //livenessProbePath
         //replicas
		 icpActualStatusInfo.currentColour=stable["colour"]
		 icpActualStatusInfo.currentVersion=stable["version"]
		 icpActualStatusInfo.currentImage=stable["image"]
		 icpActualStatusInfo.readinessProbePath=stable["readinessProbePath"]
		 icpActualStatusInfo.livenessProbePath=stable["livenessProbePath"]
		 icpActualStatusInfo.envVars=getEnvVars(stable["envVars"], lastDeployedValuesYaml["local"]["app"]["envVars"])
         icpActualStatusInfo.replicas=stable["replicas"]
	 }

    printOpen("Stable ${stable}", EchoLevel.DEBUG)
		
	 return icpActualStatusInfo
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
