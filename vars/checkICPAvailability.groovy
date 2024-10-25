import com.caixabank.absis3.*
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.DistributionModePRO


import groovy.json.JsonSlurperClassic

def call(PomXmlStructure pomXml, PipelineData pipelineData,String envICP, String typeFeature = "BOTH") {
	def icpDistCenter=""
	
	if (envICP=="CALCULATE") {
		String environment=pipelineData.bmxStructure.environment
		ICPDeployStructure deployStructure=new ICPDeployStructure('cxb-ab3cor','cxb-ab3app',environment)
		//calculamos el entorno de ICP
		envICP="${deployStructure.envICP.toUpperCase()}"
	}
	
	if (pipelineData.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1) {
		icpDistCenter="AZ1"	
	} else if (pipelineData.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2) {
		icpDistCenter="AZ2"
	} else {
		icpDistCenter="ALL"
	}

	if (typeFeature=="BUILD" || typeFeature=="BOTH") {
		//El build no permite una AZ de AZ1
		checkICPAvailability(pomXml,pipelineData,"ALL","PRO","build")
	}
	if (typeFeature=="DEPLOY" || typeFeature=="BOTH") {
		checkICPAvailability(pomXml,pipelineData,icpDistCenter,envICP.toUpperCase(),"deploy")
	}
}

def call(PomXmlStructure pomXml, PipelineData pipelineData, String availabilityZone, String environment, String feature) {
	//Tenemos que validar ICP

	String appICPId=GlobalVars.ICP_APP_ID_APPS
	String appICP=GlobalVars.ICP_APP_APPS
	
	if (env.CHECK_ICP!=null && env.CHECK_ICP == "true") {
		//Procederemos a validar si la app esta en la lista verde
		if (env.ABSIS3_SERVICES_SKIP_CHECK_ICP_LIST!=null && env.ABSIS3_SERVICES_SKIP_CHECK_ICP_LIST.indexOf(pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name)))!=-1) {
            printOpen("Excluded from validation", EchoLevel.INFO)
		} else {
			//Validate
            printOpen("Checking if ICP is available... (cluster: ${availabilityZone}, environment: ${environment})", EchoLevel.INFO)

            KpiAlmEvent kpiAlmEvent =
                new KpiAlmEvent(
                    pomXml, pipelineData,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.ICP_AVAILABILITY_CHECK)

            long wholeCallDuration
            long wholeCallStartMillis = new Date().getTime()

			ICPApiResponse response=sendRequestToICPApi("features/environment/${environment.toUpperCase()}/az/${availabilityZone}/name/${feature}",null,"GET","${appICP}","",false,false, pipelineData, pomXml)

            printOpen("El valor de la API del resultado es de ${response.prettyPrint()}", EchoLevel.DEBUG)
            long wholeCallEndMillis = new Date().getTime()
            wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

            if (response.statusCode>=200 && response.statusCode<300) {

				if (response.body.enabled==true) {

                    printOpen("ICP's ${feature} seems available.", EchoLevel.INFO)
                    kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))

				} else {
                    printOpen("ICP's ${feature} is NOT available.", EchoLevel.ERROR)
                    kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))
                    // Se ha acordado desactivar la creación de máximos al desactivarse el deploy de ICP desde el Team Cloud
                    //createMaximoAndThrow.kubernetesDisabledException(pipelineData, pomXml, environment, feature)
                    throw new Exception("${feature} ${GlobalVars.ICP_ERROR_DEPLOY_KUBERNETES_DISABLED}")

				}
					
			} else {
                printOpen("ICP's availability API is not working properly.", EchoLevel.ERROR) 
                kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))
                //createMaximoAndThrow.kubernetesDisabledException(pipelineData, pomXml, environment, feature)
                throw new Exception("${feature} ${GlobalVars.ICP_ERROR_DEPLOY_KUBERNETES_DISABLED}")

			}		
		}
	} else {
        printOpen("ICP'S availability validation is not enabled.", EchoLevel.INFO)
	}
}
