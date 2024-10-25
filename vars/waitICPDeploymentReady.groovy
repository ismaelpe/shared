import com.project.alm.*

import com.project.alm.ICPk8sComponentInfoMult
import com.project.alm.ICPk8sComponentServiceInfo
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.ICPDeployStructure
import com.project.alm.ICPk8sInstancesApp
import com.project.alm.BranchType
import com.project.alm.GlobalVars
import com.project.alm.TooManyRestartsException

boolean call(PomXmlStructure pomXml, PipelineData pipeline, ICPDeployStructure deployStructure) {
	return waitICPDeploymentReady(pomXml, pipeline, deployStructure, null, "ALL")
}

boolean call(PomXmlStructure pomXml, PipelineData pipeline, ICPDeployStructure deployStructure, String colour) {
	return waitICPDeploymentReady(pomXml, pipeline, deployStructure, colour, "ALL")
}



def analyzeHowManyRestarts(ICPk8sComponentInfoMult k8sComponentInfo ) {
	if (k8sComponentInfo!=null) {
		k8sComponentInfo.clusters.each{
			cluster->
			  if (cluster.pods!=null) {
				  cluster.pods.each{
					  pod->
						  if (pod.restarts>GlobalVars.MAX_DEV_ICP_RESTARTS) {
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
boolean call(PomXmlStructure pomXml, PipelineData pipeline, ICPDeployStructure deployStructure, String colour, String distributionCenter) {
	boolean returnValue=false

    printOpen("The current deployment color is $colour", EchoLevel.DEBUG)
    printOpen("Waiting for the application to be ready...", EchoLevel.INFO)
	
	//Tendriamos que espera la nueva version que arranque... la vieja no nos interesa
	
	timeout(time: 30, unit: 'MINUTES') {
		
		def initialPodList = null
		waitUntil(initialRecurrencePeriod: 15000) {
			ICPk8sComponentInfoMult icpActualStatusInfo=getActualDeploymentStatusOnICP(pomXml,pipeline,deployStructure,distributionCenter)
			if (initialPodList==null) {
				initialPodList = icpActualStatusInfo.getPodList()
			}

            printOpen("Los valores son de ${icpActualStatusInfo.toString()}", EchoLevel.DEBUG)
			
			if (icpActualStatusInfo!=null) {
				if (icpActualStatusInfo.clusters.size()==0) {
                    printOpen("El Deploy ha jodido el entorno!!!!!", EchoLevel.ERROR)
					throw new Exception("DEPLOY FALLIDO")
				}
				
				
				if (pipeline.branchStructure.branchType == BranchType.FEATURE) colour='b'

                printOpen("The new colour to validate is ${colour}", EchoLevel.DEBUG)
                printOpen("The isTheDeploymentReady of the element 0 is ${icpActualStatusInfo.clusters.get(0).isTheDeploymentReady()}", EchoLevel.DEBUG)
				
				//en dev no permitimos mas de 2 reinicios. Mas es erorr
				if ("DEV".equals(deployStructure.envICP.toUpperCase())) {
					analyzeHowManyRestarts(icpActualStatusInfo)
				}
				
					
				def errors=icpActualStatusInfo.deploymentError()


				 
															
	 
				
                printOpen("isTheDeploymenttReady = ${icpActualStatusInfo.isTheDeploymentReadyForColour(colour)}", EchoLevel.DEBUG)
				
				if (errors!=null) {
				    printOpen("The errors are ${errors}", EchoLevel.ERROR)
				    throw new Exception("Error ${errors}")
				}
				
				if ( deployStructure!=null && deployStructure.env!=null && ("pre".equals(deployStructure.env.toLowerCase()) || "pro".equals(deployStructure.env.toLowerCase()))) {				
					
					if ( icpActualStatusInfo.isTheDeploymentReady()) {
						
						ICPk8sInstancesApp icpInstances=null
						icpInstances=icpActualStatusInfo.areAllTheInstanciesAvailable()
						//TODO comment that line if we do not want pods restart detection
						//throwErrorIfRestartsDetected(icpActualStatusInfo,initialPodList,colour)
                        printOpen("The info of the instancies ${icpInstances.toString()}", EchoLevel.DEBUG)
						
						returnValue=icpInstances.allTheInstancesAreOk()

                        printOpen("The deployment is ready ${returnValue}", EchoLevel.INFO)
						if (returnValue) return true
						else return false
						
				   }else {
                        printOpen("The deployment is not ready!", EchoLevel.DEBUG)
						return false
				   }
				}else {
					if ( icpActualStatusInfo.isTheDeploymentReadyForColour(colour)) {
						
						ICPk8sInstancesApp icpInstances=null
						if (colour==null) {
							icpInstances=icpActualStatusInfo.areAllTheInstanciesAvailable()
						}else {
							icpInstances=icpActualStatusInfo.areAllTheInstanciesAvailable(colour)
						}
						//TODO comment that line if we do not want pods restart detection
						//throwErrorIfRestartsDetected(icpActualStatusInfo,initialPodList,colour)
                        printOpen("The info of the instancies ${icpInstances.toString()}", EchoLevel.DEBUG)

						returnValue=icpInstances.allTheInstancesAreOk()

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

def throwErrorIfRestartsDetected(ICPk8sComponentInfoMult icpActualStatusInfo,def initialPodList,String colour) {
	boolean restartDetected = false
	
	if(initialPodList==null) {
        printOpen("initialPodList is null", EchoLevel.DEBUG)
	}else {
        printOpen("initialPodList = ${initialPodList.toString()}", EchoLevel.DEBUG)
	}
	
	if (colour==null) {
		restartDetected = icpActualStatusInfo.wereSomeInstancesRestarted(initialPodList)
        printOpen("restartDetected = ${restartDetected}", EchoLevel.DEBUG)
		
	}else {
		restartDetected = icpActualStatusInfo.wereSomeInstancesRestarted(colour,initialPodList)
        printOpen("restartDetected = ${restartDetected}, colour = ${colour}", EchoLevel.DEBUG)
	}
	
	if(restartDetected) {
        printOpen("Restart detected in pods, please review micro configuration", EchoLevel.ERROR)
		throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")
	}else {
        printOpen("No restart detected in pods", EchoLevel.INFO)
	}

}

