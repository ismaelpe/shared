import com.caixabank.absis3.BmxStructure
import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.DeployStructure
import com.caixabank.absis3.AppDeploymentStateICP
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.ICPDeployStructure
import com.caixabank.absis3.BranchStructure
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.ICPAppResources
import com.caixabank.absis3.ICPApiResponse

import java.util.Map

def thereIsAncient(lastDeployedValuesYamlC1) {
	Map absisApp=lastDeployedValuesYamlC1["apps"]
	Map absisAppEnvQualifier=absisApp["envQualifier"]
	
	//Esto no es del todo correcto.
	//si es primera version esto devuelve que tienen ancient i no es cierto
	//Casos:
	//1-Solo Stable sin New
	//2-Stable y New
	if (absisAppEnvQualifier["stable"]!=null && absisAppEnvQualifier["new"]!=null) return true
	else return false
}


def call( PomXmlStructure pomXmlStructure, PipelineData pipelineData, String icpCenter, String env) {
	//Validaremos el ultimo deploy para saber si tenemos ancient
	//Posibilidades
	//Que tenga old...
	//O que tenga stable y new a la vez

	
	String environment= env
	ICPDeployStructure deployStructure=new ICPDeployStructure('cxb-ab3cor','cxb-ab3app',environment)

    BranchStructure branchStructure = pipelineData.branchStructure
	
	if (pipelineData.componentId==null) {
		pipelineData.componentId=generateArtifactInICP(pomXmlStructure,pipelineData,new ICPAppResources())
		
	}
	
	BmxStructure bmxStructure = pipelineData.bmxStructure
	DeployStructure deployStructureCd1 = bmxStructure.getDeployStructure(GlobalVars.BMX_CD1)
	
	
	//Si el micro no existe esto nos devuelve el ultimo deploy 
	//se tiene que validar previamente si el micro existe o no 
	ICPApiResponse response=sendRequestToICPApi("v1/application/PCLD/${pomXmlStructure.getICPAppName()}/component/${pipelineData.componentId}/environment/${deployStructure.envICP.toUpperCase()}/availabilityzone/${icpCenter}/status",null,"GET","${pomXmlStructure.getICPAppName()}","",false,false, pipelineData, pomXmlStructure)
	//Revisamos el status del micro
	Map lastDeployedValuesYamlC1=new HashMap()
	def appName = "${pomXmlStructure.artifactMicro}-${pomXmlStructure.artifactMajorVersion}"
	
	if (response.statusCode>=200 && response.statusCode<300) {
		
		if (response.body==null) {
			printOpen("El micro no existe posiblemente es un primer deploy", EchoLevel.ALL)
			return new AppDeploymentStateICP(lastDeployedValuesYamlC1,appName)
		}
		
		
		if (icpCenter!="ALL") {
			if (response.body.size()==1 && response.body.getAt(0).items!=null && response.body.getAt(0).items.size()!=0) {
				printOpen("Existe una version anterior del micro ${response.body.getAt(0).items} ", EchoLevel.ALL)
				lastDeployedValuesYamlC1=generateValuesYamlLastICPDeployment(pomXmlStructure,pipelineData,deployStructure.envICP,icpCenter)
			}else {
				printOpen("El micro no existe posiblemente es un primer deploy", EchoLevel.ALL)
				return new AppDeploymentStateICP(lastDeployedValuesYamlC1,appName)
			}
		}
		if (icpCenter=="ALL") {
			if (response.body.size()==2 && response.body.getAt(0).items!=null && response.body.getAt(0).items.size()!=0) {
				printOpen("Existe una version anterior del micro ${response.body.getAt(0).items} ", EchoLevel.ALL)
				lastDeployedValuesYamlC1=generateValuesYamlLastICPDeployment(pomXmlStructure,pipelineData,deployStructure.envICP,icpCenter)
			}else {
				printOpen("El micro no existe posiblemente es un primer deploy", EchoLevel.ALL)
				return new AppDeploymentStateICP(lastDeployedValuesYamlC1,appName)
			}
		}

	}else {
		printOpen("El micro no tiene ninguna major desplegada", EchoLevel.ALL)
	}
	

	
	AppDeploymentStateICP appState = new AppDeploymentStateICP(lastDeployedValuesYamlC1,appName)
	
	
	return appState
}

def call( PomXmlStructure pomXmlStructure, PipelineData pipelineData, String icpCenter) {

	//Validaremos el ultimo deploy para saber si tenemos ancient
	//Posibilidades
	//Que tenga old... 
	//O que tenga stable y new a la vez
	return getMultipleDeploymentsStateFromICP(pomXmlStructure,pipelineData,icpCenter,pipelineData.bmxStructure.environment.toUpperCase())
	

}
