import com.project.alm.EchoLevel
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.GlobalVars
import com.project.alm.ICPDeployStructure
import groovy.json.JsonSlurperClassic

def call(String componentType, String component) {
	try {
		def response = sendRequestToAbsis3MS(
			'GET',
			"${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${componentType}/${component}",
			null,
			"${GlobalVars.CATALOGO_ABSIS3_ENV}",
			200,
			[
				kpiAlmEvent: new KpiAlmEvent(
					null, null,
					KpiAlmEventStage.UNDEFINED,
					KpiAlmEventOperation.CATMSV_HTTP_CALL)
			])
		//def json = new groovy.json.JsonSlurperClassic().parseText(response.content)
		def json = response.content
		return json
	} catch (Exception ex) {
        printOpen("Error al consultar el catalogo de absis3: "+ex.getMessage(), EchoLevel.ERROR)
		throw ex
	}
}

def call(PomXmlStructure pomXml, PipelineData pipeline, ICPDeployStructure deployStructure) {
	try {
		String componentType = pipeline.garArtifactType.name
		String component = pomXml.getApp(pipeline.garArtifactType)

		def response = sendRequestToAbsis3MS(
            'GET',
            "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${componentType}/${component}",
            null,
            "${GlobalVars.CATALOGO_ABSIS3_ENV}",
            200,
            [
                kpiAlmEvent: new KpiAlmEvent(
                    pomXml, pipeline,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.CATMSV_HTTP_CALL)
            ])

        //SRV.MS/demoarqcbk
        printOpen("Get : ${response.content}", EchoLevel.DEBUG)
		//def json = new groovy.json.JsonSlurperClassic().parseText(response.content)
		def json = response.content
		if (pipeline.bmxStructure.environment == GlobalVars.PRO_ENVIRONMENT) {
    		String srvDeployTypeId = json.srvDeployTypeId
    		if(srvDeployTypeId == GlobalVars.CATALOG_CAMPAIGN_CANNARY_TYPE) {
                printOpen("We are going to use campaign canary type", EchoLevel.INFO)
    			deployStructure.cannaryType=GlobalVars.CANARY_TYPE_CAMPAIGN
    		} else if (srvDeployTypeId == GlobalVars.CATALOG_DEVOPS_CANNARY_TYPE){
                printOpen("We are going to use devops canary type", EchoLevel.INFO)
    			deployStructure.cannaryType=GlobalVars.CANARY_TYPE_DEVOPS
    		} else {
                printOpen("Unknown canary type: ${srvDeployTypeId}", EchoLevel.ERROR)
    			throw new Exception("Unknown canary type")
    		}
    	}
		deployStructure.isDb2=json.db2Dds
		deployStructure.isMq=json.mqConnect
		deployStructure.isKafka=json.kafkaConnect
		deployStructure.microType=json.srvTypeAppId
		
	} catch (Exception ex) {
        printOpen("Error getting data from Open's catalogue: "+ex.getMessage(), EchoLevel.ERROR)
		if (pipeline.bmxStructure.environment == GlobalVars.PRO_ENVIRONMENT) {
			throw ex
		}
	}

	return deployStructure
}

