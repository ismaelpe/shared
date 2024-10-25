import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.KpiAlmEvent
import com.caixabank.absis3.KpiAlmEventOperation
import com.caixabank.absis3.KpiAlmEventStage
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.ICPStateUtility
import com.caixabank.absis3.GarAppType
import com.caixabank.absis3.ICPApiResponse
import com.caixabank.absis3.ICPWorkflowStates
import hudson.Functions

def call(PomXmlStructure pomXml, PipelineData pipeline, ICPStateUtility icpStateUtility) {


    long wholeCallDuration
    long wholeCallStartMillis = new Date().getTime()

    KpiAlmEvent kpiAlmEvent =
        new KpiAlmEvent(
            pomXml, pipeline,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.ICP_CONSOLIDATE_NEW_DEPLOY)

	String returnUrlValue=""

	if (pomXml.artifactMicro!="" && icpStateUtility!=null && icpStateUtility.icpAppState!=ICPWorkflowStates.END) {
		printOpen("Consolidating new deployment...", EchoLevel.INFO)

		def body = [
			az: "ALL",
			buildBean: [
				id: "${icpStateUtility.buildId}"
			],
			environment: "${icpStateUtility.icpDeployStructure.envICP.toUpperCase()}",
			values: "${icpStateUtility.icpDeployStructure.getEnvVariables(pipeline.garArtifactType.name,pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) ),pomXml.getMajorVersion(),pipeline.domain,pipeline.subDomain,pipeline.company)}${icpStateUtility.getChartValues()}"			]

		ICPApiResponse response = sendRequestToICPApi("v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getICPAppName()}","v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
        printOpen("0000 - [ICP-STATE] STATE OF THE ICP ${icpStateUtility.icpAppState.toString()} Status code ${response.statusCode}", EchoLevel.INFO)
		
		if (response.statusCode>300) {

            long wholeCallEndMillis = new Date().getTime()
            wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

            kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

            printOpen("(Consolidate new deploy - 1st iteration) POST /deploy failed:\n${response}", EchoLevel.ERROR)

            createMaximoAndThrow.icpDeployException(pipeline, pomXml, response)

		} else {
			def isReady=false
			
			try {

				isReady=waitICPDeploymentReady(pomXml,pipeline,icpStateUtility.icpDeployStructure,icpStateUtility.getNewColour())

		   } catch(Exception e) {

			   printOpen( Functions.printThrowable(e), EchoLevel.ERROR)
			   if (e.getMessage()!=null && e.getMessage().contains("DEPLOY FALLIDO")) {

                   response=sendRequestToICPApi("v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getICPAppName()}","v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
			   
				   if (response.statusCode>300) {

                       long wholeCallEndMillis = new Date().getTime()
                       wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                       kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

                       printOpen("(Consolidate new deploy - 1st iteration) POST /deploy failed (after waitICPDeploymentReady threw exception):\n${response}", EchoLevel.ERROR)

                       createMaximoAndThrow.icpDeployException(pipeline, pomXml, response)

				   }
				   try {

					   isReady=waitICPDeploymentReady(pomXml,pipeline,icpStateUtility.icpDeployStructure,icpStateUtility.getNewColour())

				   } catch(Exception e1) {

                       long wholeCallEndMillis = new Date().getTime()
                       wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                       kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                       printOpen("(Consolidate new deploy - 1st iteration) sendRequestToICPApi threw exception two times:\n${response}", EchoLevel.ERROR)

					   throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")

				   }

			   } else {

                   long wholeCallEndMillis = new Date().getTime()
                   wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                   kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                   printOpen("(Consolidate new deploy - 1st iteration) sendRequestToICPApi threw exception:\n${response}", EchoLevel.ERROR)

				   throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")

			   }
		   }
			
			if (isReady==false) {

                long wholeCallEndMillis = new Date().getTime()
                wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                printOpen("(Consolidate new deploy - 1st iteration) sendRequestToICPApi did not throw exception but false was returned:\n${response}", EchoLevel.ERROR)

                throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")

            }
			
			if (response!=null && response.body!=null)	icpStateUtility.deployId=response.body.id
			else icpStateUtility.deployId=0
					
			icpStateUtility.icpAppState=icpStateUtility.getNextStateWorkflow()
			body = [
				az: "ALL",
				buildBean: [
					id: "${icpStateUtility.buildId}"
				],
				environment: "${icpStateUtility.icpDeployStructure.envICP.toUpperCase()}",
				values: "${icpStateUtility.icpDeployStructure.getEnvVariables(pipeline.garArtifactType.name,pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) ),pomXml.getMajorVersion(),pipeline.domain,pipeline.subDomain,pipeline.company)}${icpStateUtility.getChartValues()}"
			]

            printOpen("000 - [ICP-STATE] STATE OF THE ICP ${icpStateUtility.icpAppState.toString()}", EchoLevel.INFO)
			
			response = sendRequestToICPApi("v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getICPAppName()}","v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)

            if (response.statusCode>300) {

                long wholeCallEndMillis = new Date().getTime()
                wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

                printOpen("(Consolidate new deploy - 2nd iteration) POST /deploy failed:\n${response}", EchoLevel.ERROR)

                createMaximoAndThrow.icpDeployException(pipeline, pomXml, response)

			} else {
				try {

					isReady=waitICPDeploymentReady(pomXml,pipeline,icpStateUtility.icpDeployStructure,icpStateUtility.getNewColour())

				} catch(Exception e) {

				    printOpen( Functions.printThrowable(e), EchoLevel.ERROR)
					if (e.getMessage()!=null && e.getMessage().contains("DEPLOY FALLIDO")) {

					   response = sendRequestToICPApi("v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getICPAppName()}","v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
				
					   if (response.statusCode>300) {

                           long wholeCallEndMillis = new Date().getTime()
                           wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                           kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

                           printOpen("(Consolidate new deploy - 2nd iteration) POST /deploy failed (after waitICPDeploymentReady threw exception):\n${response}", EchoLevel.ERROR)

                           createMaximoAndThrow.icpDeployException(pipeline, pomXml, response)

					   }
					   try{

						   isReady=waitICPDeploymentReady(pomXml,pipeline,icpStateUtility.icpDeployStructure,icpStateUtility.getNewColour())

					   } catch(Exception e1) {

                           long wholeCallEndMillis = new Date().getTime()
                           wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                           kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                           printOpen("(Consolidate new deploy - 2nd iteration) sendRequestToICPApi threw exception two times:\n${response}", EchoLevel.ERROR)

						   throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")

					   }
				    } else {

                        long wholeCallEndMillis = new Date().getTime()
                        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                        kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                        printOpen("(Consolidate new deploy - 2nd iteration) sendRequestToICPApi threw exception:\n${response}", EchoLevel.ERROR)

						throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")

					}
				}
				
				if (isReady==false) {

                    long wholeCallEndMillis = new Date().getTime()
                    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                    kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                    printOpen("(Consolidate new deploy - 2nd iteration) sendRequestToICPApi did not throw exception but false was returned:\n${response}", EchoLevel.ERROR)

                    throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")

                }

				if (response!=null && response.body!=null)	icpStateUtility.deployId=response.body.id
				else icpStateUtility.deployId=0

				if (icpStateUtility.icpAppState!=ICPWorkflowStates.ELIMINATE_CURRENT_APP) {
				//No hemos terminado
					icpStateUtility.icpAppState=icpStateUtility.getNextStateWorkflow()
                    printOpen("00 - [ICP-STATE] STATE OF THE ICP ${icpStateUtility.icpAppState.toString()}", EchoLevel.INFO)
					
					body = [
						az: "ALL",
						buildBean: [
							id: "${icpStateUtility.buildId}"
						],
						environment: "${icpStateUtility.icpDeployStructure.envICP.toUpperCase()}",
						values: "${icpStateUtility.icpDeployStructure.getEnvVariables(pipeline.garArtifactType.name,pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) ),pomXml.getMajorVersion(),pipeline.domain,pipeline.subDomain,pipeline.company)}${icpStateUtility.getChartValues()}"
					]
					
					response=sendRequestToICPApi("v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getICPAppName()}","v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)

                    if (response.statusCode>300) {

                        long wholeCallEndMillis = new Date().getTime()
                        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                        kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

                        printOpen("(Consolidate new deploy - 3rd iteration) POST /deploy failed:\n${response}", EchoLevel.ERROR)

                        createMaximoAndThrow.icpDeployException(pipeline, pomXml, response)

					}
				}				
                 	
			}
		}
		String pathToMicro = BmxUtilities.calculatePathToMicro(pomXml,pipeline.branchStructure,icpStateUtility)
		returnUrlValue = icpStateUtility.icpDeployStructure.getUrlPrefixApiGateway()+"/"+pathToMicro + "/actuator/info"
		printOpen("The new deployment has been consolidated. Url: <a href='${returnUrlValue}'>${returnUrlValue}</a>", EchoLevel.INFO)
			
	} else if (pomXml.artifactSampleApp!=""){
		if (pipeline.undeploySampleApp) {
			printOpen("Deleting the sample app ${pomXml.artifactSampleApp} ...", EchoLevel.INFO)
			
			//Es una sample APP.... vampos a hacer undeploy
			def body = [
				"az": "ALL",
				"environment": "${icpStateUtility.icpDeployStructure.envICP.toUpperCase()}"
			]
			/**
			curl -vvv -k -X DELETE "https://publisher-ssp-cldalm.pro.ap.intranet.cloud.lacaixa.es/api/publisher/v1/application/PCLD/AB3COR/component/2742/deploy"\
			**/
			ICPApiResponse response = sendRequestToICPApi("v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",body,"DELETE","${pomXml.getICPAppName()}","",false,true, pipeline, pomXml)

			if (response.statusCode>300) {

				long wholeCallEndMillis = new Date().getTime()
				wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

				kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

				printOpen("(Consolidate new deploy - 4th iteration) POST /deploy failed:\n${response}", EchoLevel.ERROR)

				createMaximoAndThrow.icpDeployException(pipeline, pomXml, response)

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
