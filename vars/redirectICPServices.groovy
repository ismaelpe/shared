import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.CloudApiResponse
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.CloudDeployStructure
import com.project.alm.DeployStructure
import com.project.alm.DistributionModePRO
import groovy.json.JsonSlurperClassic
import com.project.alm.GarAppType
import com.project.alm.CloudStateUtility
import com.project.alm.ArtifactType
import com.project.alm.CloudWorkflowStates
import com.project.alm.CloudAppResources

import com.project.alm.Cloudk8sComponentInfo
import com.project.alm.Cloudk8sComponentInfoMult

import com.project.alm.Cloudk8sActualStatusInfo
import com.project.alm.BmxUtilities
import com.project.alm.BranchType
import com.project.alm.ArtifactSubType
import hudson.Functions
import java.io.NotSerializableException

def call( PomXmlStructure pomXml, PipelineData pipeline, String buildIdMain, String newImageMain, CloudStateUtility cloudStateUtility) {
	
	if (pomXml.artifactMicro!="" || pomXml.artifactSampleApp!="") {
		printOpen("Init Redirect Services stage ${pipeline.distributionModePRO} ", EchoLevel.ALL)
		try {
			CloudDeployStructure deployStructure = pipeline.deployStructure;
		
			//Vemos si hay que hacer distribucion por centro
			String cloudDistCenter="ALL"
			boolean isDeployByCenter = false
			if (pipeline.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1) {
				cloudDistCenter="AZ1"
				isDeployByCenter = true
			} else if (pipeline.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2) {
				cloudDistCenter="AZ2"
				isDeployByCenter = true
			}
			printOpen("isDeployByCentere ${isDeployByCenter} ${cloudDistCenter}", EchoLevel.ALL)
			if (isDeployByCenter) {
				//Tenemos los estados de 
				//1. STATE OF THE Cloud NEW_DEPLOY
				//2. ELIMINATE_STABLE_ROUTE_TO_CURRENT_APP
				//3. ELIMINATE_CURRENT_APP
				printOpen("redirectCloudServices. El estado del CloudStateUtility es de ", EchoLevel.ALL)
				printOpen("${cloudStateUtility.cloudAppState}", EchoLevel.ALL)
				deployCloudState(cloudStateUtility, pomXml, pipeline, cloudDistCenter)
				cloudStateUtility.cloudAppState=cloudStateUtility.getNextStateWorkflow()
				printOpen("redirectCloudServices. El estado del CloudStateUtility es de ", EchoLevel.ALL)
				printOpen("${cloudStateUtility.cloudAppState}", EchoLevel.ALL)
				deployCloudState(cloudStateUtility, pomXml, pipeline, cloudDistCenter)
				cloudStateUtility.cloudAppState=cloudStateUtility.getNextStateWorkflow()		
				
				if (pipeline.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2) {
					//Marcamos el ultimo deploy como ALL para que al llamar a la API de Cloud con az: ALL, nos devuelva este estado final.
					//Los anteriores deploys a centro 2 deben hacerse como AZ2 para no afectar a centro 1.
					cloudDistCenter="ALL"
				}
				printOpen("redirectCloudServices. El estado del CloudStateUtility es de ${cloudDistCenter}", EchoLevel.ALL)
				printOpen("${cloudStateUtility.cloudAppState}", EchoLevel.ALL)
				
				deployCloudState(cloudStateUtility, pomXml, pipeline, cloudDistCenter)
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
	return cloudStateUtility
}
