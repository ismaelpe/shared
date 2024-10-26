import com.project.alm.EchoLevel
import com.project.alm.CloudUtils
import com.project.alm.GlobalVars
import com.project.alm.CloudApiResponse

import java.text.SimpleDateFormat
import java.util.Date

def call(boolean cleanStress, int beforeDays) {

    printOpen("Vamos a recuperar los componentes para AB3APP", EchoLevel.INFO)

    CloudApiResponse responseApp = sendRequestToCloudApi("v1/api/application/PCLD/${GlobalVars.Cloud_APP_APPS}/component",null,"GET","${GlobalVars.Cloud_APP_APPS}","",false,false)
    def stressAppsMap = checkStressApps(responseApp)
	deleteAndUndeployWiremockServers(responseApp,GlobalVars.Cloud_APP_ID_APPS,GlobalVars.Cloud_APP_APPS,beforeDays,cleanStress, stressAppsMap)

    printOpen("Vamos a recuperar los componentes para AB3COR", EchoLevel.INFO)

    CloudApiResponse responseArch = sendRequestToCloudApi("v1/api/application/PCLD/${GlobalVars.Cloud_APP_ARCH}/component",null,"GET","${GlobalVars.Cloud_APP_ARCH}","",false,false)
	def stressArchMap = checkStressApps(responseArch)
	deleteAndUndeployWiremockServers(responseArch,GlobalVars.Cloud_APP_ID_ARCH,GlobalVars.Cloud_APP_ARCH,beforeDays,cleanStress,stressArchMap)

}


def deleteEvent(def id) {
    printOpen("Intentamos eliminar el evento ${id}", EchoLevel.DEBUG)
	def response = sendRequestToAlm3MS(
		'DELETE',
		"${GlobalVars.URL_CATALOGO_ALM_PRO}/audit/${id}",
		null,
		"${GlobalVars.CATALOGO_ALM_ENV}")
	if (response.status == 200) {
		printOpen("Elemento eliminado", EchoLevel.ALL)
	}else {
		printOpen("Error al eliminar el evento", EchoLevel.ALL)
	}
}


def getEvents(def componentName) {
    printOpen("Intentamos obtener el evento de la app ${componentName}", EchoLevel.DEBUG)
	def response = sendRequestToAlm3MS(
		'GET',
		"${GlobalVars.URL_CATALOGO_ALM_PRO}/audit/WIR/${componentName}",
		null,
		"${GlobalVars.CATALOGO_ALM_ENV}")

	if (response.status == 200) {
		//def json=readJSON text: response.content
		def json=response.content
		return json

	}else {
        printOpen("No se ha podido obtener el evento del componente ${componentName}", EchoLevel.ALL)
		return []
	}
}


def deleteAndUndeployWiremockServers(CloudApiResponse response, String cloudAppId, String cloudAppName, int beforeDays, boolean cleanStress, Map parameters = [:]) {
	
	printOpen("Revisamos los servidores wiremocks", EchoLevel.INFO)
	if (response.body!=null && response.body.size()>=1) {
		
		response.body.each{

            printOpen("Evaluating the app ${it}", EchoLevel.ALL)

			//Skipping Service Manager stress pod
            if (CloudUtils.isWiremockServer(it.name) && !it.name.startsWith("SERVICEMANAGER")) { 
                printOpen("${it} is a wiremock server", EchoLevel.INFO)

                def wiremockEvents = getEvents(it.name) 

                if (wiremockEvents.size()>0 && cleanStress) {
				    printOpen("El numero de elementos es de ${wiremockEvents.size()}", EchoLevel.ALL)
				    wiremockEvents.each{
					    val->
                            printOpen("El valor del evento es ${val}", EchoLevel.ALL)
                            if (val!=null) {
								SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd")
								Date datOfApp=SDF.parse(val.timestamp.substring(0,10))
								printOpen("La fecha de la app es ${datOfApp} y el entorno ${val.description.toUpperCase()}", EchoLevel.INFO)
								
								Date today = new Date().clearTime()
								Date priorDate = today - beforeDays
								printOpen("Si el micro es anterior a la fecha ${priorDate} debe eliminarse", EchoLevel.ALL)
								if (datOfApp.before(priorDate)) { 
									
									if (it.name.length()>8 && !parameters.isEmpty()) {
										def stressName = it.name.substring(0, it.name.length() - 8) + "STRESS"
										def stressId = parameters[stressName]
										printOpen("Stress name: ${stressName}, id: ${stressId}", EchoLevel.DEBUG)

										printOpen("Eliminamos la app de stress ${stressName}", EchoLevel.INFO)
										deleteAppCloud(stressName, stressId, cloudAppId, cloudAppName, val.description.toUpperCase(), 'ALL', true)
									}

									printOpen("Eliminamos el servidor wiremock ${it.name} y el evento asociado", EchoLevel.INFO)
									deleteAppCloud(it.name, it.id, cloudAppId, cloudAppName, val.description.toUpperCase(), 'ALL', true)
									deleteEvent(val.id)

								} else {
									printOpen("Los pods de stress y del servidor wiremock del ${it.name} no han caducado", EchoLevel.INFO)
								}
							}
                    }
                }
            }
        }
    }
}

def checkStressApps(CloudApiResponse response) {

	printOpen("Revisamos los pods de stress", EchoLevel.INFO)
	def map = [:]

	if (response.body!=null && response.body.size()>=1) {
		
		response.body.each{

            printOpen("Evaluating the app ${it}", EchoLevel.ALL)

			//Skipping Service Manager stress pod
            if (CloudUtils.isStressApp(it.name) && !it.name.startsWith("SERVICEMANAGER")) {
                printOpen("${it} is a stress app", EchoLevel.INFO)

				printOpen("name : ${it.name}, id: ${it.id}", EchoLevel.DEBUG)
				map[it.name] = it.id
			}
        }
    }

	printOpen("Map stress pods : ${map}", EchoLevel.DEBUG)
	return map

}
