import com.project.alm.*
import com.project.alm.GlobalVars
import groovy.json.JsonSlurperClassic
import java.util.List
import java.util.ArrayList


def call(PipelineData pipelineData, PomXmlStructure pomXml, String environment, boolean isRollback) {

	
	def response = null
	if (env.SEND_TO_ALM_CATALOG!="" && env.SEND_TO_ALM_CATALOG=="true") {
		
		/**
	@DeleteMapping(value = "/app/{type}/{application}/version/{major}/environment/{environment}", produces = "application/json")	
	public ResponseEntity<VersionResource>deleteByMajor(@PathVariable("type") String type, @PathVariable("application") String application,
			                                               @PathVariable("major") BigDecimal major, @PathVariable("environment") String environment,
			                                               @RequestParam(required = false) String isRollback);
		 */
		
		def bodyNew = null
		String environmetNew = environment
		if (pipelineData!=null && pipelineData.deployStructure!=null) {
			environmetNew = pipelineData.deployStructure.envCloud
		}
				
		try {
			printOpen("Procedemos al envio del deploy contra el catalogo de alm", EchoLevel.ALL)
		
			def environmentOther =  environment
			
			if (environmentOther == null) {
				environmentOther = environmetNew
			}
			
			
			response = sendRequestToAlm3MS(
                'DELETE',
                "${GlobalVars.URL_CATALOGO_ALM_PRO}/app/${pipelineData.getGarArtifactType().getGarName()}/${pomXml.getApp(pipelineData.garArtifactType)}/version/${pomXml.artifactMajorVersion}/environment/${environmetNew.toUpperCase()}?isRollback=${isRollback}",
                null,
                "${GlobalVars.CATALOGO_ALM_ENV}",
                [
                    kpiAlmEvent: new KpiAlmEvent(
                        pomXml, pipelineData,
                        KpiAlmEventStage.UNDEFINED,
                        KpiAlmEventOperation.CATALOG_HTTP_CALL)
                ])

            if (response.status == 200) {

				printOpen("Deploy realizado", EchoLevel.ALL)

			}else {

				printOpen("Error al proceder al despliegue del micro", EchoLevel.ALL)

			}
			
		}catch(Exception ex) {
			printOpen("Error en el envio al catalogo de alm ", EchoLevel.ALL)
			if (env.SEND_TO_ALM_CATALOG_REQUIRED!=null && env.SEND_TO_ALM_CATALOG_REQUIRED!="true") {
				throw new Exception("Unexpected response from CATALOG, services catalog ")
			}
		}
	}else {
		printOpen("El catalogo de alm esta desconnectado", EchoLevel.ALL)

	}	
	
	
}

def call(def body, PipelineData pipelineData, PomXmlStructure pomXml, CloudStateUtility cloudStateUtilitity) {
	deployArtifactInCatalog(body, pipelineData, pomXml, cloudStateUtilitity, null)
}
