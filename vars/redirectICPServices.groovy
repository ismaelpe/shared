import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.ICPApiResponse
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.ICPDeployStructure
import com.caixabank.absis3.DeployStructure
import com.caixabank.absis3.DistributionModePRO
import groovy.json.JsonSlurperClassic
import com.caixabank.absis3.GarAppType
import com.caixabank.absis3.ICPStateUtility
import com.caixabank.absis3.ArtifactType
import com.caixabank.absis3.ICPWorkflowStates
import com.caixabank.absis3.ICPAppResources

import com.caixabank.absis3.ICPk8sComponentInfo
import com.caixabank.absis3.ICPk8sComponentInfoMult

import com.caixabank.absis3.ICPk8sActualStatusInfo
import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.BranchType
import com.caixabank.absis3.ArtifactSubType
import hudson.Functions
import java.io.NotSerializableException

def call( PomXmlStructure pomXml, PipelineData pipeline, String buildIdMain, String newImageMain, ICPStateUtility icpStateUtility) {
	
	if (pomXml.artifactMicro!="" || pomXml.artifactSampleApp!="") {
		printOpen("Init Redirect Services stage ${pipeline.distributionModePRO} ", EchoLevel.ALL)
		try {
			ICPDeployStructure deployStructure = pipeline.deployStructure;
		
			//Vemos si hay que hacer distribucion por centro
			String icpDistCenter="ALL"
			boolean isDeployByCenter = false
			if (pipeline.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1) {
				icpDistCenter="AZ1"
				isDeployByCenter = true
			} else if (pipeline.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2) {
				icpDistCenter="AZ2"
				isDeployByCenter = true
			}
			printOpen("isDeployByCentere ${isDeployByCenter} ${icpDistCenter}", EchoLevel.ALL)
			if (isDeployByCenter) {
				//Tenemos los estados de 
				//1. STATE OF THE ICP NEW_DEPLOY
				//2. ELIMINATE_STABLE_ROUTE_TO_CURRENT_APP
				//3. ELIMINATE_CURRENT_APP
				printOpen("redirectICPServices. El estado del ICPStateUtility es de ", EchoLevel.ALL)
				printOpen("${icpStateUtility.icpAppState}", EchoLevel.ALL)
				deployICPState(icpStateUtility, pomXml, pipeline, icpDistCenter)
				icpStateUtility.icpAppState=icpStateUtility.getNextStateWorkflow()
				printOpen("redirectICPServices. El estado del ICPStateUtility es de ", EchoLevel.ALL)
				printOpen("${icpStateUtility.icpAppState}", EchoLevel.ALL)
				deployICPState(icpStateUtility, pomXml, pipeline, icpDistCenter)
				icpStateUtility.icpAppState=icpStateUtility.getNextStateWorkflow()		
				
				if (pipeline.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2) {
					//Marcamos el ultimo deploy como ALL para que al llamar a la API de ICP con az: ALL, nos devuelva este estado final.
					//Los anteriores deploys a centro 2 deben hacerse como AZ2 para no afectar a centro 1.
					icpDistCenter="ALL"
				}
				printOpen("redirectICPServices. El estado del ICPStateUtility es de ${icpDistCenter}", EchoLevel.ALL)
				printOpen("${icpStateUtility.icpAppState}", EchoLevel.ALL)
				
				deployICPState(icpStateUtility, pomXml, pipeline, icpDistCenter)
			}
		}catch(Exception e) {			
			printOpen("ERROR " + Functions.printThrowable(e), EchoLevel.ERROR)
			throw e
		}
	}else {
		//No existe micro dentro del artifact
		//no deplegamos contra bluemix
        printOpen("No micro to deploy!!!", EchoLevel.INFO)
	}
	printOpen(" End Redirect Services stage", EchoLevel.ALL)
	return icpStateUtility
}