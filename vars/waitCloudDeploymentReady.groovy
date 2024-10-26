import com.project.alm.*

import com.project.alm.Cloudk8sComponentInfoMult
import com.project.alm.Cloudk8sComponentServiceInfo
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.CloudDeployStructure
import com.project.alm.Cloudk8sInstancesApp
import com.project.alm.BranchType
import com.project.alm.GlobalVars
import com.project.alm.TooManyRestartsException

boolean call(PomXmlStructure pomXml, PipelineData pipeline, CloudDeployStructure deployStructure) {
	return waitCloudDeploymentReady(pomXml, pipeline, deployStructure, null, "ALL")
}

boolean call(PomXmlStructure pomXml, PipelineData pipeline, CloudDeployStructure deployStructure, String colour) {
	return waitCloudDeploymentReady(pomXml, pipeline, deployStructure, colour, "ALL")
}



def analyzeHowManyRestarts(Cloudk8sComponentInfoMult k8sComponentInfo ) {
	if (k8sComponentInfo!=null) {
		k8sComponentInfo.clusters.each{
			cluster->
			  if (cluster.pods!=null) {
				  cluster.pods.each{
					  pod->
						  if (pod.restarts>GlobalVars.MAX_DEV_Cloud_RESTARTS) {
							  printOpen("Vamos a analizar el body", EchoLevel.DEBUG)
							  throw new TooManyRestartsException("Restarted to many times... (times: ${pod.restarts})")
						}
				  }
			  }
		}
	}
}

/**
 *
 * @param body
 * @return
 */
boolean call(PomXmlStructure pomXml, PipelineData pipeline, CloudDeployStructure deployStructure, String colour, String distributionCenter) {
	boolean returnValue=false

    printOpen("The current deployment color is $colour", EchoLevel.DEBUG)
    printOpen("Waiting for the application to be ready...", EchoLevel.INFO)
	
	//Tendriamos que espera la nueva version que arranque... la vieja no nos interesa
	
	timeout(time: 30, unit: 'MINUTES') {
		
		def initialPodList = null
		waitUntil(initialRecurrencePeriod: 15000) {
			Cloudk8sComponentInfoMult cloudActualStatusInfo=getActualDeploymentStatusOnCloud(pomXml,pipeline,deployStructure,distributionCenter)
			if (initialPodList==null) {
				initialPodList = cloudActualStatusInfo.getPodList()
			}

            printOpen("Los valores son de ${cloudActualStatusInfo.toString()}", EchoLevel.DEBUG)
			
			if (cloudActualStatusInfo!=null) {
				if (cloudActualStatusInfo.clusters.size()==0) {
                    printOpen("El Deploy ha jodido el entorno!!!!!", EchoLevel.ERROR)
					throw new Exception("DEPLOY FALLIDO")
				}
				
				
				if (pipeline.branchStructure.branchType == BranchType.FEATURE) colour='b'

                printOpen("The new colour to validate is ${colour}", EchoLevel.DEBUG)
                printOpen("The isTheDeploymentReady of the element 0 is ${cloudActualStatusInfo.clusters.get(0).isTheDeploymentReady()}", EchoLevel.DEBUG)
				
				//en dev no permitimos mas de 2 reinicios. Mas es erorr
				if ("DEV".equals(deployStructure.envCloud.toUpperCase())) {
					analyzeHowManyRestarts(cloudActualStatusInfo)
				}
				
					
				def errors=cloudActualStatusInfo.deploymentError()


				 
															
	 
				
                printOpen("isTheDeploymenttReady = ${cloudActualStatusInfo.isTheDeploymentReadyForColour(colour)}", EchoLevel.DEBUG)
				
				if (errors!=null) {
				    printOpen("The errors are ${errors}", EchoLevel.ERROR)
				    throw new Exception("Error ${errors}")
				}
				
				if ( deployStructure!=null && deployStructure.env!=null && ("pre".equals(deployStructure.env.toLowerCase()) || "pro".equals(deployStructure.env.toLowerCase()))) {				
					
					if ( cloudActualStatusInfo.isTheDeploymentReady()) {
						
						Cloudk8sInstancesApp cloudInstances=null
						cloudInstances=cloudActualStatusInfo.areAllTheInstanciesAvailable()
						//TODO comment that line if we do not want pods restart detection
						//throwErrorIfRestartsDetected(cloudActualStatusInfo,initialPodList,colour)
                        printOpen("The info of the instancies ${cloudInstances.toString()}", EchoLevel.DEBUG)
						
						returnValue=cloudInstances.allTheInstancesAreOk()

                        printOpen("The deployment is ready ${returnValue}", EchoLevel.INFO)
						if (returnValue) return true
						else return false
						
				   }else {
                        printOpen("The deployment is not ready!", EchoLevel.DEBUG)
						return false
				   }
				}else {
					if ( cloudActualStatusInfo.isTheDeploymentReadyForColour(colour)) {
						
						Cloudk8sInstancesApp cloudInstances=null
						if (colour==null) {
							cloudInstances=cloudActualStatusInfo.areAllTheInstanciesAvailable()
						}else {
							cloudInstances=cloudActualStatusInfo.areAllTheInstanciesAvailable(colour)
						}
						//TODO comment that line if we do not want pods restart detection
						//throwErrorIfRestartsDetected(cloudActualStatusInfo,initialPodList,colour)
                        printOpen("The info of the instancies ${cloudInstances.toString()}", EchoLevel.DEBUG)

						returnValue=cloudInstances.allTheInstancesAreOk()

                        printOpen("The deployment is ready ${returnValue}", EchoLevel.INFO)
						if (returnValue) return true
						else return false
						
				   }else {
                        printOpen("The deployment is not ready!", EchoLevel.DEBUG)
						return false
				   }   
				}
			}else {
                printOpen("The deployment is not ready!!", EchoLevel.DEBUG)
				return false
			}
		}
	}
	
	return returnValue
}

def throwErrorIfRestartsDetected(Cloudk8sComponentInfoMult cloudActualStatusInfo,def initialPodList,String colour) {
	boolean restartDetected = false
	
	if(initialPodList==null) {
        printOpen("initialPodList is null", EchoLevel.DEBUG)
	}else {
        printOpen("initialPodList = ${initialPodList.toString()}", EchoLevel.DEBUG)
	}
	
	if (colour==null) {
		restartDetected = cloudActualStatusInfo.wereSomeInstancesRestarted(initialPodList)
        printOpen("restartDetected = ${restartDetected}", EchoLevel.DEBUG)
		
	}else {
		restartDetected = cloudActualStatusInfo.wereSomeInstancesRestarted(colour,initialPodList)
        printOpen("restartDetected = ${restartDetected}, colour = ${colour}", EchoLevel.DEBUG)
	}
	
	if(restartDetected) {
        printOpen("Restart detected in pods, please review micro configuration", EchoLevel.ERROR)
		throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")
	}else {
        printOpen("No restart detected in pods", EchoLevel.INFO)
	}

}

