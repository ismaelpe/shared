import com.project.alm.*
import com.project.alm.GlobalVars
import groovy.json.JsonSlurperClassic
import java.util.List
import java.util.ArrayList


def validateEnvironment(String appType, String application, String environment, String major) {
	def response = null
	String environmetNew = environment
	def status="true"

    response = sendRequestToAlm3MS(
        'GET',
        "${GlobalVars.URL_CATALOGO_ALM_PRO}/app/${appType}/${application}/version/environment/${environment}",
        null,
        "${GlobalVars.CATALOGO_ALM_ENV}",
        [
            kpiAlmEvent: new KpiAlmEvent(
                null, null,
                KpiAlmEventStage.UNDEFINED,
                KpiAlmEventOperation.CATMSV_HTTP_CALL)
        ])

    if (response.status == 200) {
		printOpen("Tenemos que revisar el numero de elementos retornados", EchoLevel.ALL)
		//Tenemos que revisar de cuantos elementos se compone la lista
		printOpen("${response.content}", EchoLevel.ALL)
		if (response.content!=null) {
			
			//def json = new groovy.json.JsonSlurperClassic().parseText(response.content)
			def json = response.content
			
			//Detectamos todas estas majors instaladas
			printOpen("Tenemos estas majors instaladas en el entorno de ${environmetNew} de un numero de ${json.size()} ", EchoLevel.ALL)
			//Este micro esta en la white list
			if (env.ALM_SERVICES_SKIP_MAJOR_CONTROL_LIST!=null && env.ALM_SERVICES_SKIP_MAJOR_CONTROL.indexOf("${pomXml.getApp(pipelineData.garArtifactType)}")!=-1) {
				printOpen("El micro se ha salvado... puede tener las majors que le de la gana", EchoLevel.ALL)
			}else {
				//El nombre de micros supera el numero maximo de micros por entorno
				def numMajors = "5"
				if (environmetNew.equals("DEV") && env.MAX_MAJORS_DEV!=null && env.MAX_MAJORS_DEV!="") {
					numMajors="${env.MAX_MAJORS_DEV}"
				}else if (environmetNew.equals("TST") && env.MAX_MAJORS_TST!=null && env.MAX_MAJORS_TST!="") {
					numMajors="${env.MAX_MAJORS_TST}"
				}else if (environmetNew.equals("PRE") && env.MAX_MAJORS_PRE!=null && env.MAX_MAJORS_PRE!="") {
					numMajors="${env.MAX_MAJORS_PRE}"
				}else if (environmetNew.equals("PRO") && env.MAX_MAJORS_PRO!=null && env.MAX_MAJORS_PRO!="") {
					numMajors="${env.MAX_MAJORS_PRO}"
				}
				int numMajorsInt = numMajors as int
				printOpen("el maximo es de ${numMajorsInt} el tamaño es de ${json.size()}", EchoLevel.ALL)
				if (numMajorsInt<json.size()) {
					status="Demasiadas majors ${json.size()} para el entorno ${environmetNew} el maximo es de ${numMajorsInt}"
					printOpen("Demasiadas majors ${json.size()} para el entorno ${environmetNew} el maximo es de ${numMajorsInt} ", EchoLevel.ALL)
				}else if (numMajorsInt==json.size()) {
					//Igual llegamos al limite de majors permitidas
					//Si la nueva major la tenemos no hay problema si la nueva major NO la tenemos estamos fuera de rango
					printOpen("La nueva major a validar es de ${major}", EchoLevel.ALL)
					boolean laTenemos=false
                    int majorInt = major as int
					json.each{
                        int it_majorInt = it.major as int
						if (it_majorInt==majorInt) {
							laTenemos=true
						}
					}
					if (laTenemos==false) {
						status="Demasiadas majors ${json.size()} para el entorno ${environmetNew} el maximo es de ${numMajorsInt}"
						printOpen("Demasiadas majors ${json.size()} para el entorno ${environmetNew} el maximo es de ${numMajorsInt} y vamos a instalar otra nueva", EchoLevel.ALL)
					}
				}else {
					printOpen("Tenemos menos majors que las permitidas. Maximas majors: ${numMajors} Majors instaladas  ${json.size()} para el entorno de ${environmetNew}", EchoLevel.ALL)
				}
			}
								
		}
	}else {
		printOpen("El elemento quizas no esta en catalogo", EchoLevel.ALL)
	}
	
	return status
}

def call(PipelineData pipelineData, PomXmlStructure pomXml) {
    def status="true"
	
	def response = null
	def toManyMajors = false
	if (env.SEND_TO_ALM_CATALOG!="" && env.SEND_TO_ALM_CATALOG=="true") {
		
		/**
	@GetMapping(value = "/app/{type}/{application}/version/environment/{environment}", produces = "application/json")	
	public ResponseEntity<List<VersionResource>>  getDeployedMajors(@PathVariable("type") String type, @PathVariable("application") String application,
			                                              @PathVariable("environment") String environment);
		 */
		
		def bodyNew = null
		String environmetNew = "TST"
		if (pipelineData!=null && pipelineData.bmxStructure.environment!=null) {
			environmetNew = pipelineData.bmxStructure.environment
			if (environmetNew.equals('eden')) {
				environmetNew="DEV"
			}
			environmetNew = environmetNew.toUpperCase()
		}else {
			printOpen("Las estructuras del deploy Structure son nulas", EchoLevel.ALL)
		}
				
		try {
			printOpen("Sending deploy against alm catalog", EchoLevel.INFO)
		
			status=validateEnvironment(pipelineData.getGarArtifactType().getGarName(),pomXml.getApp(pipelineData.garArtifactType),environmetNew,pomXml.getMajorVersion())			
			
			if ("true".equals(status) && "PRE".equals(environmetNew)) {
				//PRE esta bien... falta consultar PRO
				status=validateEnvironment(pipelineData.getGarArtifactType().getGarName(),pomXml.getApp(pipelineData.garArtifactType),"PRO",pomXml.getMajorVersion())
			}
			
			printOpen("Sending to the catalog is completed", EchoLevel.INFO)

		}catch(Exception ex) {
			
			if (toManyMajors) {
				throw ex
			}
			printOpen("Error sending to alm catalog", EchoLevel.ERROR)
			if (env.SEND_TO_ALM_CATALOG_REQUIRED!=null && env.SEND_TO_ALM_CATALOG_REQUIRED!="true") {
				throw new Exception("Unexpected response from CATMSV (services catalog)")
			}
		}
	}else {
		printOpen("Alm3 catalog is disconnected", EchoLevel.INFO)

	}	
	
	return status
	
}

def call(def body, PipelineData pipelineData, PomXmlStructure pomXml, CloudStateUtility cloudStateUtilitity) {
	deployArtifactInCatMsv(body, pipelineData, pomXml, cloudStateUtilitity, null)
}

def sendRequestToCatalog(def body) {

	boolean weHaveToSendToCatalog = GlobalVars.SEND_TO_CATALOG

	if (weHaveToSendToCatalog) {

		def url = idecuaRoutingUtils.catalogPipelineUrl()

		printOpen("Body to be sent to GSA URL ${url}:\n\n${body}", EchoLevel.ALL)

		def response

		try {
			response = sendRequestToGpl('PUT', url, "", body)
		} catch (Exception e) {
			throw new Exception("Unexpected response when connecting to GSA (${response?.status})! + ${e.getMessage()}")
		}

		return response

	} else {
		def toJson = { input -> groovy.json.JsonOutput.toJson(input) }
		body = toJson(body)
		printOpen("Skipping artifact publication in catalog:\n\n${body}", EchoLevel.ALL)
	}
}
