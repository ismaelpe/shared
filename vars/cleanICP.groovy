import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.SampleAppCleanMode
import com.caixabank.absis3.ICPUtils
import com.caixabank.absis3.KpiAlmEvent

import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.ICPApiResponse
import java.text.SimpleDateFormat
import java.util.Date



def call(boolean cleanEden, int beforeDays, SampleAppCleanMode sampleAppCleanMode,def edenDays) {
    cleanICP(cleanEden,beforeDays,sampleAppCleanMode,edenDays,false)
}

def call(boolean cleanEden, int beforeDays, SampleAppCleanMode sampleAppCleanMode,def edenDays, def cleanPrototype) {
	
	boolean deleteAllEden=false
	
	if ("0".equals(edenDays)) {
		deleteAllEden=true
	}
		
	printOpen("cleanEden ${cleanEden} days ${beforeDays} typeOfSampleModeClean ${sampleAppCleanMode}", EchoLevel.ALL)
	if (cleanEden || sampleAppCleanMode != SampleAppCleanMode.NONE) {
		//Primero eliminamos el AB3APP
		ICPApiResponse response=sendRequestToICPApi("v1/application/${GlobalVars.ICP_APP_ID_ARCH}/component",null,"GET","${GlobalVars.ICP_APP_ARCH}","",false,false)
		deleteAndUndeployApps(response,GlobalVars.ICP_APP_ID_ARCH,GlobalVars.ICP_APP_ARCH,beforeDays,deleteAllEden)
		
		if (sampleAppCleanMode == SampleAppCleanMode.ALL) {
			deleteAllSampleAPPS(response,GlobalVars.ICP_APP_ID_ARCH,GlobalVars.ICP_APP_ARCH,'DEV')
			deleteAllSampleAPPS(response,GlobalVars.ICP_APP_ID_ARCH,GlobalVars.ICP_APP_ARCH,'TST')
			deleteAllSampleAPPS(response,GlobalVars.ICP_APP_ID_ARCH,GlobalVars.ICP_APP_ARCH,'PRE')
		}
		//Segundo eliminamos el AB3COR
		response=sendRequestToICPApi("v1/application/${GlobalVars.ICP_APP_ID_APPS}/component",null,"GET","${GlobalVars.ICP_APP_APPS}","",false,false)
		deleteAndUndeployApps(response,GlobalVars.ICP_APP_ID_APPS,GlobalVars.ICP_APP_APPS ,beforeDays,deleteAllEden)

	}else if (cleanPrototype) { 
		ICPApiResponse response=sendRequestToICPApi("v1/application/${GlobalVars.ICP_APP_ID_APPS}/component",null,"GET","${GlobalVars.ICP_APP_APPS}","",false,false)
		deleteAndUndeployAppsPrototype(response,GlobalVars.ICP_APP_ID_APPS,GlobalVars.ICP_APP_APPS,beforeDays)
	}else {
		printOpen("No selected environment/type to clean", EchoLevel.ALL)
	}


}

def deleteAllSampleAPPS(ICPApiResponse response, String icpAppId, String icpAppName, String environment) {
	if (response.body!=null && response.body.size()>=1) {
		
		response.body.each {
			def body = [
				az: "ALL",
				environment: "${environment}"
			]
			if (ICPUtils.isSampleApp(it.name) && it.name!="AB3COR" && it.name!="AB3APP") {
				def responseUndeploy=sendRequestToICPApi("v1/application/PCLD/${icpAppName}/component/${it.id}/deploy",body,"DELETE","${icpAppName}","",false,false)
				
				//printOpen(" The status code of the undeploy ${responseUndeploy.statusCode}", EchoLevel.DEBUG)
				printOpen(" Deleting the sample app ${it.name} ${environment} ${it.id} with the ${responseUndeploy.statusCode}", EchoLevel.ALL)
			}
		}
	}
}

def deleteEvent(def id) {
	def response = sendRequestToAbsis3MS(
		'DELETE',
		"${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/audit/${id}",
		null,
		"${GlobalVars.CATALOGO_ABSIS3_ENV}")
	if (response.status == 200) {
		printOpen("Elemento eliminado", EchoLevel.ALL)
	}else {
		printOpen("Error", EchoLevel.ALL)
	}
}

def getPrototypeEvents(def app) {
	
	def response = sendRequestToAbsis3MS(
		'GET',
		"${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/audit/PRT/${app}",
		null,
		"${GlobalVars.CATALOGO_ABSIS3_ENV}")

	if (response.status == 200) {
		//def json=readJSON text: response.content
		def json=response.content
		return json
	}else {
		return []
	}
}

def deleteAndUndeployAppsPrototype(ICPApiResponse response, String icpAppId, String icpAppName, int beforeDays) {
	
	if (response.body!=null && response.body.size()>=1) {
		
		response.body.each {
			printOpen("Evaluating the app ${it}", EchoLevel.INFO)
	
			//Tenemos que consultar el catalogo y devolver todos los eventos de esta aplicacion
			//Para cada uno de ellos deberemos revisar la fecha
			//de caducidad
			//en caso que haya caducado se debera eliminar aplicar el delete sobre el entorno
			//eliminar el evento
			//En caso que no tenga ningun evento tiene sentido la aplicacion en ICP?
			
			if (ICPUtils.isPrototypeApp(it.name)) {
				printOpen("${it} is a prototype APP ", EchoLevel.INFO)
				
						
				def prototypeEvents=getPrototypeEvents(it.name)
				printOpen("Los eventos asociados son de ${prototypeEvents}",EchoLevel.INFO)
				boolean seTieneQueEliminar=true
				if (prototypeEvents.size()>0) {
					printOpen("El numero de elementos es de ${prototypeEvents.size()}", EchoLevel.INFO)
					def numberOfEvents=prototypeEvents.size()
					prototypeEvents.each{
						val->
						    printOpen("El valor ${val}", EchoLevel.INFO)
							if (val!=null) {
								SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd")
								Date datOfApp=SDF.parse(val.timestamp.substring(0,10))
								printOpen("El valor ${val} la fecha es de ${val.timestamp} Entorno ${val.description}", EchoLevel.INFO)
								
								Date today = new Date().clearTime()
								Date priorDate = today - beforeDays
								printOpen("La fecha inicial a caducar ${priorDate} fecha aplicacion ${datOfApp} la fecha de hoy ${today} y los dias a restar ${beforeDays} ", EchoLevel.INFO)
								if (datOfApp.before(priorDate)) {
									//Se tiene que hacer el delete del micro en el entorno X
									printOpen("Se tiene que eliminar el micro en el entorno ${val.description} i eliminar el id evento", EchoLevel.INFO)
									deleteAppICP(it.name, it.id, icpAppId, icpAppName, val.description.toUpperCase(), 'ALL',false)
									deleteEvent(val.id)
								}else {
									seTieneQueEliminar=false
									printOpen("El prototipo no ha caducado", EchoLevel.INFO)
								}
							}
					}
					if (seTieneQueEliminar) {
						printOpen("Eliminamos la app en ICP", EchoLevel.INFO)
						//deleteAppICP(it.name, it.id, icpAppId, icpAppName, 'DEV', 'ALL',true, true)
					}
				}else {
					//deleteAppICP(it.name, it.id, icpAppId, icpAppName, 'DEV', 'ALL',true, true)
					printOpen("Eliminamos la app en ICP", EchoLevel.INFO)
				}
			}
		}
	}
}
	

def deleteAndUndeployApps(ICPApiResponse response, String icpAppId, String icpAppName, int beforeDays,boolean deleteAll) {
	
	if (response.body!=null && response.body.size()>=1) {
		
		response.body.each {
			printOpen("Evaluating the app ${it}", EchoLevel.ALL)
	
			
			if (ICPUtils.isEdenApp(it.name)) {
				printOpen("${it} is an Eden APP ", EchoLevel.ALL)
				
				String date=it.name.substring(it.name.length()-9,it.name.length()-1)
			
				SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd")
				Date today = new Date().clearTime()
				Date priorDate = today - beforeDays
							
				Date datOfApp=SDF.parse(date)
				
				printOpen("The priorDate is ${priorDate} and the dateOfTheAp ${datOfApp} Is before ${datOfApp.before(priorDate)}", EchoLevel.ALL)
				if (datOfApp.before(priorDate) || deleteAll) {
					//Delete App
					printOpen("The first installation date of the artifact is ${date}", EchoLevel.ALL)
					
					String mantainEdenMicro = "${env.ABSIS3_SERVICES_MANTAIN_EDEN_MICRO}"
			
					
					if (mantainEdenMicro!=null && mantainEdenMicro.contains(it.name)) {
					    printOpen("We have to mantain the app ${it.name}", EchoLevel.ALL)
					}else {
					   deleteAppICP(it.name, it.id, icpAppId, icpAppName, 'DEV', 'ALL',true)
					}
				}
			}
		}
	}
}






