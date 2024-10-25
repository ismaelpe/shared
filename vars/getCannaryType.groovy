import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.KpiAlmEvent
import com.caixabank.absis3.KpiAlmEventOperation
import com.caixabank.absis3.KpiAlmEventStage
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.GlobalVars

def call(PomXmlStructure pomXml, PipelineData pipeline) {
	if (env.CAMPAIGN_CANARY_FEATURE_ENABLED == "true") {
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
			printOpen("Get : ${response.content}", EchoLevel.ALL)
			//def json = new groovy.json.JsonSlurper().parseText(response.content)
			def json = response.content
			String srvDeployTypeId = json.srvDeployTypeId
			if(srvDeployTypeId == GlobalVars.CATALOG_CAMPAIGN_CANNARY_TYPE) {
				printOpen("El micro usara el canary en modo campanya", EchoLevel.ALL)
				return GlobalVars.CANARY_TYPE_CAMPAIGN
			} else if (srvDeployTypeId == GlobalVars.CATALOG_DEVOPS_CANNARY_TYPE){
				printOpen("El micro usara el canary en modo devops", EchoLevel.ALL)
				return GlobalVars.CANARY_TYPE_DEVOPS
			} else {
				printOpen("Unknown canary type: ${srvDeployTypeId}", EchoLevel.ALL)
				if (pipelineData.bmxStructure.environment == GlobalVars.PRO_ENVIRONMENT) {
					throw new Exception("Unknown canary type")
				}
			}
		} catch (Exception ex) {
			printOpen("Error al consultar el catalogo de absis3: ", EchoLevel.ALL)
			if (pipeline.bmxStructure.environment == GlobalVars.PRO_ENVIRONMENT) {
				throw ex
			}
		}
	}
	printOpen("Devolvemos el canary por defecto DEVOPS", EchoLevel.ALL)
	return GlobalVars.CANARY_TYPE_DEVOPS
}

