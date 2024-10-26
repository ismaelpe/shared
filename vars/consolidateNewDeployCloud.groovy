import com.project.alm.EchoLevel
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.GlobalVars
import com.project.alm.BmxUtilities
import com.project.alm.CloudStateUtility
import com.project.alm.GarAppType
import com.project.alm.CloudApiResponse
import com.project.alm.CloudWorkflowStates
import hudson.Functions

def call(PomXmlStructure pomXml, PipelineData pipeline, CloudStateUtility cloudStateUtility) {


    long wholeCallDuration
    long wholeCallStartMillis = new Date().getTime()

    KpiAlmEvent kpiAlmEvent =
        new KpiAlmEvent(
            pomXml, pipeline,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.Cloud_CONSOLIDATE_NEW_DEPLOY)

	String returnUrlValue=""

	if (pomXml.artifactMicro!="" && cloudStateUtility!=null && cloudStateUtility.cloudAppState!=CloudWorkflowStates.END) {
		printOpen("Consolidating new deployment...", EchoLevel.INFO)

		def body = [
			az: "ALL",
			buildBean: [
				id: "${cloudStateUtility.buildId}"
			],
			environment: "${cloudStateUtility.cloudDeployStructure.envCloud.toUpperCase()}",
			values: "${cloudStateUtility.cloudDeployStructure.getEnvVariables(pipeline.garArtifactType.name,pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) ),pomXml.getMajorVersion(),pipeline.domain,pipeline.subDomain,pipeline.company)}${cloudStateUtility.getChartValues()}"			]

		CloudApiResponse response = sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
        printOpen("0000 - [Cloud-STATE] STATE OF THE Cloud ${cloudStateUtility.cloudAppState.toString()} Status code ${response.statusCode}", EchoLevel.INFO)
		
		if (response.statusCode>300) {

            long wholeCallEndMillis = new Date().getTime()
            wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

            kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

            printOpen("(Consolidate new deploy - 1st iteration) POST /deploy failed:\n${response}", EchoLevel.ERROR)

            createMaximoAndThrow.cloudDeployException(pipeline, pomXml, response)

		} else {
			def isReady=false
			
			try {

				isReady=waitCloudDeploymentReady(pomXml,pipeline,cloudStateUtility.cloudDeployStructure,cloudStateUtility.getNewColour())

		   } catch(Exception e) {

			   printOpen( Functions.printThrowable(e), EchoLevel.ERROR)
			   if (e.getMessage()!=null && e.getMessage().contains("DEPLOY FALLIDO")) {

                   response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
			   
				   if (response.statusCode>300) {

                       long wholeCallEndMillis = new Date().getTime()
                       wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                       kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

                       printOpen("(Consolidate new deploy - 1st iteration) POST /deploy failed (after waitCloudDeploymentReady threw exception):\n${response}", EchoLevel.ERROR)

                       createMaximoAndThrow.cloudDeployException(pipeline, pomXml, response)

				   }
				   try {

					   isReady=waitCloudDeploymentReady(pomXml,pipeline,cloudStateUtility.cloudDeployStructure,cloudStateUtility.getNewColour())

				   } catch(Exception e1) {

                       long wholeCallEndMillis = new Date().getTime()
                       wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                       kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                       printOpen("(Consolidate new deploy - 1st iteration) sendRequestToCloudApi threw exception two times:\n${response}", EchoLevel.ERROR)

					   throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")

				   }

			   } else {

                   long wholeCallEndMillis = new Date().getTime()
                   wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                   kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                   printOpen("(Consolidate new deploy - 1st iteration) sendRequestToCloudApi threw exception:\n${response}", EchoLevel.ERROR)

				   throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")

			   }
		   }
			
			if (isReady==false) {

                long wholeCallEndMillis = new Date().getTime()
                wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                printOpen("(Consolidate new deploy - 1st iteration) sendRequestToCloudApi did not throw exception but false was returned:\n${response}", EchoLevel.ERROR)

                throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")

            }
			
			if (response!=null && response.body!=null)	cloudStateUtility.deployId=response.body.id
			else cloudStateUtility.deployId=0
					
			cloudStateUtility.cloudAppState=cloudStateUtility.getNextStateWorkflow()
			body = [
				az: "ALL",
				buildBean: [
					id: "${cloudStateUtility.buildId}"
				],
				environment: "${cloudStateUtility.cloudDeployStructure.envCloud.toUpperCase()}",
				values: "${cloudStateUtility.cloudDeployStructure.getEnvVariables(pipeline.garArtifactType.name,pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) ),pomXml.getMajorVersion(),pipeline.domain,pipeline.subDomain,pipeline.company)}${cloudStateUtility.getChartValues()}"
			]

            printOpen("000 - [Cloud-STATE] STATE OF THE Cloud ${cloudStateUtility.cloudAppState.toString()}", EchoLevel.INFO)
			
			response = sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)

            if (response.statusCode>300) {

                long wholeCallEndMillis = new Date().getTime()
                wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

                printOpen("(Consolidate new deploy - 2nd iteration) POST /deploy failed:\n${response}", EchoLevel.ERROR)

                createMaximoAndThrow.cloudDeployException(pipeline, pomXml, response)

			} else {
				try {

					isReady=waitCloudDeploymentReady(pomXml,pipeline,cloudStateUtility.cloudDeployStructure,cloudStateUtility.getNewColour())

				} catch(Exception e) {

				    printOpen( Functions.printThrowable(e), EchoLevel.ERROR)
					if (e.getMessage()!=null && e.getMessage().contains("DEPLOY FALLIDO")) {

					   response = sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
				
					   if (response.statusCode>300) {

                           long wholeCallEndMillis = new Date().getTime()
                           wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                           kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

                           printOpen("(Consolidate new deploy - 2nd iteration) POST /deploy failed (after waitCloudDeploymentReady threw exception):\n${response}", EchoLevel.ERROR)

                           createMaximoAndThrow.cloudDeployException(pipeline, pomXml, response)

					   }
					   try{

						   isReady=waitCloudDeploymentReady(pomXml,pipeline,cloudStateUtility.cloudDeployStructure,cloudStateUtility.getNewColour())

					   } catch(Exception e1) {

                           long wholeCallEndMillis = new Date().getTime()
                           wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                           kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                           printOpen("(Consolidate new deploy - 2nd iteration) sendRequestToCloudApi threw exception two times:\n${response}", EchoLevel.ERROR)

						   throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")

					   }
				    } else {

                        long wholeCallEndMillis = new Date().getTime()
                        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                        kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                        printOpen("(Consolidate new deploy - 2nd iteration) sendRequestToCloudApi threw exception:\n${response}", EchoLevel.ERROR)

						throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")

					}
				}
				
				if (isReady==false) {

                    long wholeCallEndMillis = new Date().getTime()
                    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                    kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                    printOpen("(Consolidate new deploy - 2nd iteration) sendRequestToCloudApi did not throw exception but false was returned:\n${response}", EchoLevel.ERROR)

                    throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")

                }

				if (response!=null && response.body!=null)	cloudStateUtility.deployId=response.body.id
				else cloudStateUtility.deployId=0

				if (cloudStateUtility.cloudAppState!=CloudWorkflowStates.ELIMINATE_CURRENT_APP) {
				//No hemos terminado
					cloudStateUtility.cloudAppState=cloudStateUtility.getNextStateWorkflow()
                    printOpen("00 - [Cloud-STATE] STATE OF THE Cloud ${cloudStateUtility.cloudAppState.toString()}", EchoLevel.INFO)
					
					body = [
						az: "ALL",
						buildBean: [
							id: "${cloudStateUtility.buildId}"
						],
						environment: "${cloudStateUtility.cloudDeployStructure.envCloud.toUpperCase()}",
						values: "${cloudStateUtility.cloudDeployStructure.getEnvVariables(pipeline.garArtifactType.name,pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) ),pomXml.getMajorVersion(),pipeline.domain,pipeline.subDomain,pipeline.company)}${cloudStateUtility.getChartValues()}"
					]
					
					response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)

                    if (response.statusCode>300) {

                        long wholeCallEndMillis = new Date().getTime()
                        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                        kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

                        printOpen("(Consolidate new deploy - 3rd iteration) POST /deploy failed:\n${response}", EchoLevel.ERROR)

                        createMaximoAndThrow.cloudDeployException(pipeline, pomXml, response)

					}
				}				
                 	
			}
		}
		String pathToMicro = BmxUtilities.calculatePathToMicro(pomXml,pipeline.branchStructure,cloudStateUtility)
		returnUrlValue = cloudStateUtility.cloudDeployStructure.getUrlPrefixApiGateway()+"/"+pathToMicro + "/actuator/info"
		printOpen("The new deployment has been consolidated. Url: <a href='${returnUrlValue}'>${returnUrlValue}</a>", EchoLevel.INFO)
			
	} else if (pomXml.artifactSampleApp!=""){
		if (pipeline.undeploySampleApp) {
			printOpen("Deleting the sample app ${pomXml.artifactSampleApp} ...", EchoLevel.INFO)
			
			//Es una sample APP.... vampos a hacer undeploy
			def body = [
				"az": "ALL",
				"environment": "${cloudStateUtility.cloudDeployStructure.envCloud.toUpperCase()}"
			]
			/**
			curl -vvv -k -X DELETE "https://publisher-ssp-cldalm.pro.ap.intranet.cloud.digitalscale.es/api/publisher/v1/application/PCLD/AB3COR/component/2742/deploy"\
			**/
			CloudApiResponse response = sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"DELETE","${pomXml.getCloudAppName()}","",false,true, pipeline, pomXml)

			if (response.statusCode>300) {

				long wholeCallEndMillis = new Date().getTime()
				wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

				kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

				printOpen("(Consolidate new deploy - 4th iteration) POST /deploy failed:\n${response}", EchoLevel.ERROR)

				createMaximoAndThrow.cloudDeployException(pipeline, pomXml, response)

			}
			printOpen("The sample app ${pomXml.artifactSampleApp} has been deleted", EchoLevel.INFO)
		} else {
			printOpen("The sample app ${pomXml.artifactSampleApp} it's marked to still alive", EchoLevel.INFO)
		}
		
    } else {
        //No existe micro dentro del artifact
        //no deplegamos contra bluemix
        printOpen("No micro to consolidate!", EchoLevel.INFO)
    }

    long wholeCallEndMillis = new Date().getTime()
    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

    kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))


	return returnUrlValue
}
