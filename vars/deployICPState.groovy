import com.project.alm.*

def call(CloudStateUtility cloudStateUtility, PomXmlStructure pomXml, PipelineData pipeline, String cloudDistCenter) {

    long wholeCallDuration
    long wholeCallStartMillis = new Date().getTime()

    KpiAlmEvent kpiAlmEvent =
        new KpiAlmEvent(
            pomXml, pipeline,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.Cloud_DEPLOY_STATE)

	if (cloudStateUtility.cloudAppState != CloudWorkflowStates.END)
	{
		//Tenemos que eliminar el siguiente estado
		printOpen("[Cloud-STATE] STATE OF THE Cloud ${cloudStateUtility.cloudAppState.toString()}", EchoLevel.ALL)
		printOpen("[Kibana] filtro para buscar: kubernetes.labels.app_kubernetes_io/instance  ${cloudStateUtility.nameComponentInCloud}", EchoLevel.ALL)
		
		CloudDeployStructure deployStructure = pipeline.deployStructure
		
		body = [
			az: "${cloudDistCenter}",
			environment: "${deployStructure.envCloud.toUpperCase()}",
			values: "${deployStructure.getEnvVariables(pipeline.garArtifactType.name,pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) ), pomXml.getMajorVersion() ,pipeline.domain,pipeline.subDomain,pipeline.company)}${cloudStateUtility.getChartValues()}"
		]
		CloudApiResponse response
		response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
		
		if (response.statusCode>300) {

            long wholeCallEndMillis = new Date().getTime()
            wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

            kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

            createMaximoAndThrow.cloudDeployException(pipeline, pomXml, response)

		} else {
			boolean isReady = false
			try {

				 isReady=waitCloudDeploymentReady(pomXml,pipeline,deployStructure,cloudStateUtility.getNewColour(),cloudDistCenter)

			} catch(Exception e) {

                if (e.getMessage()!=null && e.getMessage().contains("DEPLOY FALLIDO")) {
				response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)
								 
				 if (response.statusCode>300) {

                     long wholeCallEndMillis = new Date().getTime()
                     wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                     kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

                     createMaximoAndThrow.cloudDeployException(pipeline, pomXml, response)

				 }
				 try {

					 isReady=waitCloudDeploymentReady(pomXml,pipeline,deployStructure,cloudStateUtility.getNewColour(),cloudDistCenter)

				 } catch(Exception e1) {

                     long wholeCallEndMillis = new Date().getTime()
                     wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                     kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                     throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")

				 }
					 
				  } else {

                    long wholeCallEndMillis = new Date().getTime()
                    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                    kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                    throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")

				  }
			}
						 
			if (response!=null && response.body!=null)
				cloudStateUtility.deployId=response.body.id
			else cloudStateUtility.deployId=0

			if (isReady==false) {

                long wholeCallEndMillis = new Date().getTime()
                wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

                throw new Exception("Error deploying the new artifact")

            }
	
		}
	} else {
		printOpen("El deploy ha finalizado el estado es ${cloudStateUtility.cloudAppState}", EchoLevel.ALL)
	}

    long wholeCallEndMillis = new Date().getTime()
    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

    kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))

}
