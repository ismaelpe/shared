import com.project.alm.*
import com.project.alm.GlobalVars
import com.project.alm.DistributionModePRO


import groovy.json.JsonSlurperClassic

def call(PomXmlStructure pomXml, PipelineData pipelineData,String envCloud, String typeFeature = "BOTH") {
	def cloudDistCenter=""
	
	if (envCloud=="CALCULATE") {
		String environment=pipelineData.bmxStructure.environment
		CloudDeployStructure deployStructure=new CloudDeployStructure('cxb-ab3cor','cxb-ab3app',environment)
		//calculamos el entorno de Cloud
		envCloud="${deployStructure.envCloud.toUpperCase()}"
	}
	
	if (pipelineData.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1) {
		cloudDistCenter="AZ1"	
	} else if (pipelineData.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2) {
		cloudDistCenter="AZ2"
	} else {
		cloudDistCenter="ALL"
	}

	if (typeFeature=="BUILD" || typeFeature=="BOTH") {
		//El build no permite una AZ de AZ1
		checkCloudAvailability(pomXml,pipelineData,"ALL","PRO","build")
	}
	if (typeFeature=="DEPLOY" || typeFeature=="BOTH") {
		checkCloudAvailability(pomXml,pipelineData,cloudDistCenter,envCloud.toUpperCase(),"deploy")
	}
}

def call(PomXmlStructure pomXml, PipelineData pipelineData, String availabilityZone, String environment, String feature) {
	//Tenemos que validar Cloud

	String appCloudId=GlobalVars.Cloud_APP_ID_APPS
	String appCloud=GlobalVars.Cloud_APP_APPS
	
	if (env.CHECK_Cloud!=null && env.CHECK_Cloud == "true") {
		//Procederemos a validar si la app esta en la lista verde
		if (env.ALM_SERVICES_SKIP_CHECK_Cloud_LIST!=null && env.ALM_SERVICES_SKIP_CHECK_Cloud_LIST.indexOf(pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name)))!=-1) {
            printOpen("Excluded from validation", EchoLevel.INFO)
		} else {
			//Validate
            printOpen("Checking if Cloud is available... (cluster: ${availabilityZone}, environment: ${environment})", EchoLevel.INFO)

            KpiAlmEvent kpiAlmEvent =
                new KpiAlmEvent(
                    pomXml, pipelineData,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.Cloud_AVAILABILITY_CHECK)

            long wholeCallDuration
            long wholeCallStartMillis = new Date().getTime()

			CloudApiResponse response=sendRequestToCloudApi("features/environment/${environment.toUpperCase()}/az/${availabilityZone}/name/${feature}",null,"GET","${appCloud}","",false,false, pipelineData, pomXml)

            printOpen("El valor de la API del resultado es de ${response.prettyPrint()}", EchoLevel.DEBUG)
            long wholeCallEndMillis = new Date().getTime()
            wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

            if (response.statusCode>=200 && response.statusCode<300) {

				if (response.body.enabled==true) {

                    printOpen("Cloud's ${feature} seems available.", EchoLevel.INFO)
                    kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))

				} else {
                    printOpen("Cloud's ${feature} is NOT available.", EchoLevel.ERROR)
                    kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))
                    // Se ha acordado desactivar la creación de máximos al desactivarse el deploy de Cloud desde el Team Cloud
                    //createMaximoAndThrow.kubernetesDisabledException(pipelineData, pomXml, environment, feature)
                    throw new Exception("${feature} ${GlobalVars.Cloud_ERROR_DEPLOY_KUBERNETES_DISABLED}")

				}
					
			} else {
                printOpen("Cloud's availability API is not working properly.", EchoLevel.ERROR) 
                kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))
                //createMaximoAndThrow.kubernetesDisabledException(pipelineData, pomXml, environment, feature)
                throw new Exception("${feature} ${GlobalVars.Cloud_ERROR_DEPLOY_KUBERNETES_DISABLED}")

			}		
		}
	} else {
        printOpen("Cloud'S availability validation is not enabled.", EchoLevel.INFO)
	}
}
