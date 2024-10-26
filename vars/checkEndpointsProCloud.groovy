import com.project.alm.*
import java.util.Map

def thereIsAncient(lastDeployedValuesYamlC1) {
	Map almApp=lastDeployedValuesYamlC1["apps"]
	Map almAppEnvQualifier=almApp["envQualifier"]
	
	//Esto no es del todo correcto.
	//si es primera version esto devuelve que tienen ancient i no es cierto
	//Casos:
	//1-Solo Stable sin New 
	//2-Stable y New	
	if (almAppEnvQualifier["stable"]!=null && almAppEnvQualifier["new"]!=null) return true
	else return false
}

def thereIsAtLeastOneAncient(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String envCloud) {
	Map lastDeployedValuesYamlC1=generateValuesYamlLastCloudDeployment(pomXmlStructure,pipelineData,envCloud,"AZ1")
	Map lastDeployedValuesYamlC2=generateValuesYamlLastCloudDeployment(pomXmlStructure,pipelineData,envCloud,"AZ2")

	return thereIsAncient(lastDeployedValuesYamlC1["alm"]) || thereIsAncient(lastDeployedValuesYamlC2["alm"])
}

def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, DistributionModePRO distributionMode = DistributionModePRO.CANARY_ON_ALL_CENTERS, boolean checkOnlyMainUrl = false) {

	boolean failed = false
    def uris = []
    String output = "ENDPOINT TEST RESULT\n--------------------\n\n"
	
	String environment= pipelineData.bmxStructure.environment.toUpperCase()
	CloudDeployStructure deployStructure=new CloudDeployStructure('cxb-ab3cor','cxb-ab3app',environment)
	BranchStructure branchStructure = pipelineData.branchStructure
	
	BmxStructure bmxStructure = pipelineData.bmxStructure
	String artifactRoute = BmxUtilities.calculateRoute(pomXmlStructure, branchStructure)
	DeployStructure deployStructureCd1 = bmxStructure.getDeployStructure(GlobalVars.BMX_CD1)
	DeployStructure deployStructureCd2 = bmxStructure.getDeployStructure(GlobalVars.BMX_CD2)

	String suffix=""
	if (pomXmlStructure.isArchProject()) {
		suffix="/arch-service"
	}
	
	if (checkOnlyMainUrl || thereIsAtLeastOneAncient(pomXmlStructure,pipelineData,deployStructure.envCloud)) {

        //uris.add("https://k8sgateway.${deployStructureCd1.url_ext}${suffix}/${artifactRoute}/actuator/info")
        uris.add("https://k8sgateway.${deployStructureCd1.url_int}${suffix}/${artifactRoute}/actuator/info")

        if (DistributionModePRO.CANARY_ON_ALL_CENTERS == distributionMode || DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1 == distributionMode) {
            uris.add("https://k8sgateway.pro.cloud-1.alm.cloud.digitalscale.es${suffix}/${artifactRoute}/actuator/info")
        }
        if (DistributionModePRO.CANARY_ON_ALL_CENTERS == distributionMode || DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2 == distributionMode) {
            uris.add("https://k8sgateway.pro.cloud-2.alm.cloud.digitalscale.es${suffix}/${artifactRoute}/actuator/info")
        }

    }
	
	if (!checkOnlyMainUrl) {

		//uris.add("https://k8sgateway.${deployStructureCd1.url_ext}${suffix}/${GlobalVars.BETA_COMPONENT_SUFFIX.replace("<componentName>", artifactRoute)}/actuator/info")
		uris.add("https://k8sgateway.${deployStructureCd1.url_int}${suffix}/${GlobalVars.BETA_COMPONENT_SUFFIX.replace("<componentName>", artifactRoute)}/actuator/info")
		
	
	
	
	    if (DistributionModePRO.CANARY_ON_ALL_CENTERS == distributionMode || DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1 == distributionMode) {
			uris.add("https://k8sgateway.pro.cloud-1.alm.cloud.digitalscale.es${suffix}/${GlobalVars.BETA_COMPONENT_SUFFIX.replace("<componentName>", artifactRoute)}/actuator/info")        
	    }
	    if (DistributionModePRO.CANARY_ON_ALL_CENTERS == distributionMode || DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2 == distributionMode) {
			uris.add("https://k8sgateway.pro.cloud-2.alm.cloud.digitalscale.es${suffix}/${GlobalVars.BETA_COMPONENT_SUFFIX.replace("<componentName>", artifactRoute)}/actuator/info")        
	    }
	}

    for (String uri in uris) {
        try {

            def checkEndpointResponse = testURIWithRetries(uri)
            def isOk = "200" == checkEndpointResponse?.status
            if (!isOk) {
                failed = true
            }
            output += "${isOk ? '[  OK  ]' : '[FAILED]'} ${checkEndpointResponse?.status} ${uri} (${checkEndpointResponse?.body})\n"

        } catch (Exception ex) {

            failed = true
            output += "[FAILED]' ERR ${uri} (${ex.getMessage()})"

        }
    }

    if (failed) {
        error "${output}"
    }

    return output
}

def testURIWithRetries(String uri) {

    def checkEndpointResponse
    int retryNumber = GlobalVars.PRO_ENDPOINTS_CHECK_MAX_RETRIES

    waitUntil(initialRecurrencePeriod: 15000) {

        checkEndpointResponse = checkEndpoint(uri, [insecure: true, environment: "pro"])
        def isOk = "200" == checkEndpointResponse?.status

        if (isOk) {

            return true

        } else if (!isOk && --retryNumber != 0) {
            
            sleep 5
            printOpen("La comprobación del endpoint ${uri} no ha dado el resultado esperado\n\nHTTP ${checkEndpointResponse?.status}\n\n${checkEndpointResponse?.body}", EchoLevel.ALL)

            return false

        } else {

            return true

        }

    }

    return checkEndpointResponse
}
