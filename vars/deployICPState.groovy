import com.caixabank.absis3.*

def call(ICPStateUtility icpStateUtility, PomXmlStructure pomXml, PipelineData pipeline, String icpDistCenter) {

    long wholeCallDuration
    long wholeCallStartMillis = new Date().getTime()

    KpiAlmEvent kpiAlmEvent =
        new KpiAlmEvent(
            pomXml, pipeline,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.ICP_DEPLOY_STATE)

	if (icpStateUtility.icpAppState != ICPWorkflowStates.END)
	{
		//Tenemos que eliminar el siguiente estado
		printOpen("[ICP-STATE] STATE OF THE ICP ${icpStateUtility.icpAppState.toString()}", EchoLevel.ALL)
		printOpen("[Kibana] filtro para buscar: kubernetes.labels.app_kubernetes_io/instance  ${icpStateUtility.nameComponentInICP}", EchoLevel.ALL)
		
		ICPDeployStructure deployStructure = pipeline.deployStructure
		
		body = [
			az: "${icpDistCenter}",
			environment: "${deployStructure.envICP.toUpperCase()}",
			values: "${deployStructure.getEnvVariables(pipeline.garArtifactType.name,pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) ), pomXml.getMajorVersion() ,pipeline.domain,pipeline.subDomain,pipeline.company)}${icpStateUtility.getChartValues()}"
		]
		ICPApiResponse response
		response=sendRequestToICPApi("v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getICPAppName()}","v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
		
		if (response.statusCode>300) {

            long wholeCallEndMillis = new Date().getTime()
            wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

            kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

            createMaximoAndThrow.icpDeployException(pipeline, pomXml, response)

		} else {
			boolean isReady = false
			try {

				 isReady=waitICPDeploymentReady(pomXml,pipeline,deployStructure,icpStateUtility.getNewColour(),icpDistCenter)

			} catch(Exception e) {

                if (e.getMessage()!=null && e.getMessage().contains("DEPLOY FALLIDO")) {
				response=sendRequestToICPApi("v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getICPAppName()}","v1/application/PCLD/${pomXml.getICPAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
								 
				 if (response.statusCode>300) {

                     long wholeCallEndMillis = new Date().getTime()
                     wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                     kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

                     createMaximoAndThrow.icpDeployException(pipeline, pomXml, response)

				 }
				 try {

					 isReady=waitICPDeploymentReady(pomXml,pipeline,deployStructure,icpStateUtility.getNewColour(),icpDistCenter)

				 } catch(Exception e1) {

                     long wholeCallEndMillis = new Date().getTime()
                     wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                     kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                     throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")

				 }
					 
				  } else {

                    long wholeCallEndMillis = new Date().getTime()
                    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                    kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                    throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_INSTANCE_REBOOTING}")

				  }
			}
						 
			if (response!=null && response.body!=null)
				icpStateUtility.deployId=response.body.id
			else icpStateUtility.deployId=0

			if (isReady==false) {

                long wholeCallEndMillis = new Date().getTime()
                wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

                throw new Exception("Error deploying the new artifact")

            }
	
		}
	} else {
		printOpen("El deploy ha finalizado el estado es ${icpStateUtility.icpAppState}", EchoLevel.ALL)
	}

    long wholeCallEndMillis = new Date().getTime()
    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

    kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))

}
