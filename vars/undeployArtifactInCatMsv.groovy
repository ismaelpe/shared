import com.caixabank.absis3.*
import com.caixabank.absis3.GlobalVars
import groovy.json.JsonSlurperClassic
import java.util.List
import java.util.ArrayList


def call(PipelineData pipelineData, PomXmlStructure pomXml, String environment, boolean isRollback) {

	
	def response = null
	if (env.SEND_TO_ABSIS3_CATALOG!="" && env.SEND_TO_ABSIS3_CATALOG=="true") {
		
		/**
	@DeleteMapping(value = "/app/{type}/{application}/version/{major}/environment/{environment}", produces = "application/json")	
	public ResponseEntity<VersionResource>deleteByMajor(@PathVariable("type") String type, @PathVariable("application") String application,
			                                               @PathVariable("major") BigDecimal major, @PathVariable("environment") String environment,
			                                               @RequestParam(required = false) String isRollback);
		 */
		
		def bodyNew = null
		String environmetNew = environment
		if (pipelineData!=null && pipelineData.deployStructure!=null) {
			environmetNew = pipelineData.deployStructure.envICP
		}
				
		try {
			printOpen("Procedemos al envio del deploy contra el catalogo de absis3", EchoLevel.ALL)
		
			def environmentOther =  environment
			
			if (environmentOther == null) {
				environmentOther = environmetNew
			}
			
			
			response = sendRequestToAbsis3MS(
                'DELETE',
                "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${pipelineData.getGarArtifactType().getGarName()}/${pomXml.getApp(pipelineData.garArtifactType)}/version/${pomXml.artifactMajorVersion}/environment/${environmetNew.toUpperCase()}?isRollback=${isRollback}",
                null,
                "${GlobalVars.CATALOGO_ABSIS3_ENV}",
                [
                    kpiAlmEvent: new KpiAlmEvent(
                        pomXml, pipelineData,
                        KpiAlmEventStage.UNDEFINED,
                        KpiAlmEventOperation.CATMSV_HTTP_CALL)
                ])

            if (response.status == 200) {

				printOpen("Deploy realizado", EchoLevel.ALL)

			}else {

				printOpen("Error al proceder al despliegue del micro", EchoLevel.ALL)

			}
			
		}catch(Exception ex) {
			printOpen("Error en el envio al catalogo de absis3 ", EchoLevel.ALL)
			if (env.SEND_TO_ABSIS3_CATALOG_REQUIRED!=null && env.SEND_TO_ABSIS3_CATALOG_REQUIRED!="true") {
				throw new Exception("Unexpected response from CATMSV, services catalog ")
			}
		}
	}else {
		printOpen("El catalogo de absis3 esta desconnectado", EchoLevel.ALL)

	}	
	
	
}

def call(def body, PipelineData pipelineData, PomXmlStructure pomXml, ICPStateUtility icpStateUtilitity) {
	deployArtifactInCatMsv(body, pipelineData, pomXml, icpStateUtilitity, null)
}