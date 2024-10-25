import com.project.alm.*
import com.project.alm.GlobalVars
import groovy.json.JsonSlurperClassic
import java.util.List
import java.util.ArrayList


def call(def namespace, def type, def app, def major, def environment) {

	if (environment=='EDEN') environment='DEV'
	def response = null
	if (env.SEND_TO_ALM_CATALOG!="" && env.SEND_TO_ALM_CATALOG=="true") {
		try{

			response = sendRequestToAbsis3MS(
                'GET',
                "${GlobalVars.URL_CATALOGO_ALM_PRO}/app/${type}/${app}/${major}/config?env=${environment}",
                null,
                "${GlobalVars.CATALOGO_ALM_ENV}",
                [
                    kpiAlmEvent: new KpiAlmEvent(
                        null, null,
                        KpiAlmEventStage.UNDEFINED,
                        KpiAlmEventOperation.CATMSV_HTTP_CALL)
                ])

            if (response.status == 200) {
				printOpen("Get : ${response.content}", EchoLevel.INFO)
			}else {
				printOpen("Error al proceder al despliegue del micro", EchoLevel.ERROR)
			}
			
		}catch(Exception ex) {
			printOpen("Error en el envio al catalogo de alm ", EchoLevel.ERROR)
			if (env.SEND_TO_ALM_CATALOG_REQUIRED!=null && env.SEND_TO_ALM_CATALOG_REQUIRED!="true") {
				throw new Exception("Unexpected response from CATMSV, services catalog ")
			}
		}
	}else {
		printOpen("El catalogo de alm esta desconnectado", EchoLevel.INFO)

	}	
	
	
}
