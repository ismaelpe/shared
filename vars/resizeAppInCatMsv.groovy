import com.project.alm.*
import com.project.alm.GlobalVars
import groovy.json.JsonSlurperClassic
import java.util.List
import java.util.ArrayList


def call(def namespace, def type, def app, def major, def environment, def cpu, def memory, def replica) {

	
	def response = null
	if (env.SEND_TO_ALM_CATALOG!="" && env.SEND_TO_ALM_CATALOG=="true") {
		try{
		
			def bodyResize = [
				cpuSize: cpu,
				memSize: memory,
				replicaSize: replica
			]


			response = sendRequestToAlm3MS(
                'PUT',
                "${GlobalVars.URL_CATALOGO_ALM_PRO}/app/${type}/${app}/${major}/config?env=${environment}",
                bodyResize,
                "${GlobalVars.CATALOGO_ALM_ENV}",
                [
                    kpiAlmEvent: new KpiAlmEvent(
                        null, null,
                        KpiAlmEventStage.UNDEFINED,
                        KpiAlmEventOperation.CATMSV_HTTP_CALL)
                ])

            if (response.status == 200) {
				printOpen("Deploy realizado", EchoLevel.ALL)
			}else {
				printOpen("Error al proceder al despliegue del micro", EchoLevel.ALL)
				throw new Exception("el micro no existe en este namespace para este tipo")
			}
			
		}catch(Exception ex) {
			printOpen("Error en el envio al catalogo de alm ", EchoLevel.ALL)
			throw new Exception("Unexpected response from CATMSV, services catalog ")
			
		}
	}else {
		printOpen("El catalogo de alm esta desconnectado", EchoLevel.ALL)

	}	
	
	
}
