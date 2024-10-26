import com.project.alm.BmxStructure
import com.project.alm.BmxUtilities
import com.project.alm.DeployStructure
import com.project.alm.AppDeploymentStateCloud
import com.project.alm.EchoLevel
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.CloudDeployStructure
import com.project.alm.BranchStructure
import com.project.alm.GlobalVars
import com.project.alm.CloudAppResources
import com.project.alm.CloudApiResponse

import java.util.Map

def thereIsAncient(lastDeployedValuesYamlC1) {
	Map almApp=lastDeployedValuesYamlC1["apps"]
	Map almAppEnvQualifier=almApp["envQualifier"]
	
	//Esto no es del todo correcto.
	//si es primera version esto devuelve que tienen ancient i no es cierto
	//Casos:
	//1-Solo Stable sin New
	//2-Stable y New
	if (almAppEnvQualifier["stable"]!=null && almAppEnvQualifier["new"]!=null) return true
	else return false
}


def call( PomXmlStructure pomXmlStructure, PipelineData pipelineData, String cloudCenter, String env) {
	//Validaremos el ultimo deploy para saber si tenemos ancient
	//Posibilidades
	//Que tenga old...
	//O que tenga stable y new a la vez

	
	String environment= env
	CloudDeployStructure deployStructure=new CloudDeployStructure('cxb-ab3cor','cxb-ab3app',environment)

    BranchStructure branchStructure = pipelineData.branchStructure
	
	if (pipelineData.componentId==null) {
		pipelineData.componentId=generateArtifactInCloud(pomXmlStructure,pipelineData,new CloudAppResources())
		
	}
	
	BmxStructure bmxStructure = pipelineData.bmxStructure
	DeployStructure deployStructureCd1 = bmxStructure.getDeployStructure(GlobalVars.BMX_CD1)
	
	
	//Si el micro no existe esto nos devuelve el ultimo deploy 
	//se tiene que validar previamente si el micro existe o no 
	CloudApiResponse response=sendRequestToCloudApi("v1/application/PCLD/${pomXmlStructure.getCloudAppName()}/component/${pipelineData.componentId}/environment/${deployStructure.envCloud.toUpperCase()}/availabilityzone/${cloudCenter}/status",null,"GET","${pomXmlStructure.getCloudAppName()}","",false,false, pipelineData, pomXmlStructure)
	//Revisamos el status del micro
	Map lastDeployedValuesYamlC1=new HashMap()
	def appName = "${pomXmlStructure.artifactMicro}-${pomXmlStructure.artifactMajorVersion}"
	
	if (response.statusCode>=200 && response.statusCode<300) {
		
		if (response.body==null) {
			printOpen("El micro no existe posiblemente es un primer deploy", EchoLevel.ALL)
			return new AppDeploymentStateCloud(lastDeployedValuesYamlC1,appName)
		}
		
		
		if (cloudCenter!="ALL") {
			if (response.body.size()==1 && response.body.getAt(0).items!=null && response.body.getAt(0).items.size()!=0) {
				printOpen("Existe una version anterior del micro ${response.body.getAt(0).items} ", EchoLevel.ALL)
				lastDeployedValuesYamlC1=generateValuesYamlLastCloudDeployment(pomXmlStructure,pipelineData,deployStructure.envCloud,cloudCenter)
			}else {
				printOpen("El micro no existe posiblemente es un primer deploy", EchoLevel.ALL)
				return new AppDeploymentStateCloud(lastDeployedValuesYamlC1,appName)
			}
		}
		if (cloudCenter=="ALL") {
			if (response.body.size()==2 && response.body.getAt(0).items!=null && response.body.getAt(0).items.size()!=0) {
				printOpen("Existe una version anterior del micro ${response.body.getAt(0).items} ", EchoLevel.ALL)
				lastDeployedValuesYamlC1=generateValuesYamlLastCloudDeployment(pomXmlStructure,pipelineData,deployStructure.envCloud,cloudCenter)
			}else {
				printOpen("El micro no existe posiblemente es un primer deploy", EchoLevel.ALL)
				return new AppDeploymentStateCloud(lastDeployedValuesYamlC1,appName)
			}
		}

	}else {
		printOpen("El micro no tiene ninguna major desplegada", EchoLevel.ALL)
	}
	

	
	AppDeploymentStateCloud appState = new AppDeploymentStateCloud(lastDeployedValuesYamlC1,appName)
	
	
	return appState
}

def call( PomXmlStructure pomXmlStructure, PipelineData pipelineData, String cloudCenter) {

	//Validaremos el ultimo deploy para saber si tenemos ancient
	//Posibilidades
	//Que tenga old... 
	//O que tenga stable y new a la vez
	return getMultipleDeploymentsStateFromCloud(pomXmlStructure,pipelineData,cloudCenter,pipelineData.bmxStructure.environment.toUpperCase())
	

}
