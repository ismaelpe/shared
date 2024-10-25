import com.project.alm.ArtifactType
import com.project.alm.EchoLevel
import com.project.alm.FileUtils
import com.project.alm.GitUtils
import com.project.alm.GlobalVars
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineStructureType
import com.project.alm.GarAppType
import com.project.alm.Utilities
import groovy.json.JsonSlurperClassic

def stopCampaing() {
	startOrStop(false)
}

def startCampaing() {
	startOrStop(true)
}

def startOrStop(boolean isStart) {
	def bodyNew = null
	if (env.SEND_TO_ABSIS3_CATALOG!="" && env.SEND_TO_ABSIS3_CATALOG=="true") {
		def date=new Date().format("yyyy-mm-dd")
		def response=null
		
		if (isStart) {
			bodyNew = [
				initDate: date,
				startDate: date
			]
			response=sendRequestToAbsis3MS('PUT', "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/iop",bodyNew, "${GlobalVars.CATALOGO_ABSIS3_ENV}")
		}else {
			bodyNew = [
				endDate: date
			]
			response=sendRequestToAbsis3MS('POST', "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/iop/close",null, "${GlobalVars.CATALOGO_ABSIS3_ENV}")
		}

		if (response.status == 200) {
			printOpen("Deploy realizado", EchoLevel.ALL)
		} else if (response.status == 400) {
			//def json = new groovy.json.JsonSlurper().parseText(response.content)
			def json = response.content
			
			throw new Exception("Micros aun abiertos: \n"+json+"");
		}else {
			printOpen("Error al atacar al micro", EchoLevel.ALL)
			throw new Exception("Error al start/stop la iop")
		}
	}else {
		printOpen("Envio a catalogo no habilitado", EchoLevel.ALL)
	}
	
}

def closeVersion(def pomXml, def pipelineData) {
	if (env.CAMPAIGN_CANARY_FEATURE_ENABLED!=null && "true".equals(env.CAMPAIGN_CANARY_FEATURE_ENABLED)) {
		def typeVersion
		if (pomXml.artifactVersionQualifier == null || pomXml.artifactVersionQualifier == "") {
			typeVersion = "RELEASE"
		} else if (pomXml.isSNAPSHOT()) {
			typeVersion = "SNAPSHOT"
		} else if (pomXml.isRCVersion()) {
			typeVersion = "RC"
		} else {
			typeVersion = "UNKNOWN"
		}
		
		def response=sendRequestToAbsis3MS('POST', "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/iop/${pipelineData.getGarArtifactType().getGarName()}/${pomXml.getApp(pipelineData.garArtifactType)}/${pomXml.artifactMajorVersion}/${pomXml.artifactMinorVersion}/${pomXml.artifactFixVersion}/${typeVersion}?state=E",null, "${GlobalVars.CATALOGO_ABSIS3_ENV}")
		if (response.status == 200) {
		   printOpen("Version cerrada dentro de la IOP", EchoLevel.ALL)
		} else if (response.status == 404) {
			throw new Exception("No existe version para la IOP abierta")
		}else {
			throw new Exception("Error al cerrar el micro en la campaña")
		}		
	}
}

def getCannaryCampaignValue(def pomXml, def pipelineData) {
	if (env.CAMPAIGN_CANARY_FEATURE_ENABLED!=null && "true".equals(env.CAMPAIGN_CANARY_FEATURE_ENABLED)) {
		def response=sendRequestToAbsis3MS('GET', "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/iop?open=true",null, "${GlobalVars.CATALOGO_ABSIS3_ENV}")
		if (response.status == 200) {
			//def listIop = new JsonSlurperClassic().parseText(response.content)
			def listIop = response.content
			if (listIop.size()>0) {
				printOpen("Devolvemos el global cannary ${listIop.get(0)}", EchoLevel.ALL)
				return listIop.get(0).globalCannary
			}
		}else {
			printOpen("Ha fallado la consulta vamos a dar por correctamente abierta la IOP", EchoLevel.ALL)
			return 100
		}
	}else {
		return 100
	}
}

def getIop() {
	
	def response=sendRequestToAbsis3MS('GET', "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/iop?open=true",null, "${GlobalVars.CATALOGO_ABSIS3_ENV}")
	if (response.status == 200) {
	   printOpen("Devolveremos la info de la IOP", EchoLevel.ALL)
	   
	   //def listIop = new JsonSlurperClassic().parseText(response.content)
	   def listIop = response.content
	   if (listIop.size()>0) {
		   printOpen("Devolvemos el global cannary ${listIop.get(0)}", EchoLevel.ALL)
		   return listIop.get(0)
	   }else {
		   throw new Exception("No existe IOP abierta")
	   }
	} else if (response.status == 404) {
		throw new Exception("No existe IOP abierta")
	}else {
		throw new Exception("Error al añadir el micro a la campaña")
	}
	
	
}

def addMicroToCampaign(def pomXml, def pipelineData) {
	
	if (env.CAMPAIGN_CANARY_FEATURE_ENABLED!=null && "true".equals(env.CAMPAIGN_CANARY_FEATURE_ENABLED)) {
		def typeVersion
		if (pomXml.artifactVersionQualifier == null || pomXml.artifactVersionQualifier == "") {
			typeVersion = "RELEASE"
		} else if (pomXml.isSNAPSHOT()) {
			typeVersion = "SNAPSHOT"
		} else if (pomXml.isRCVersion()) {
			typeVersion = "RC"
		} else {
			typeVersion = "UNKNOWN"
		}
		
		def response=sendRequestToAbsis3MS('PUT', "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/iop/${pipelineData.getGarArtifactType().getGarName()}/${pomXml.getApp(pipelineData.garArtifactType)}/${pomXml.artifactMajorVersion}/${pomXml.artifactMinorVersion}/${pomXml.artifactFixVersion}/${typeVersion}",null, "${GlobalVars.CATALOGO_ABSIS3_ENV}")
		if (response.status == 200) {
		   printOpen("App añadida a la IOP", EchoLevel.ALL)
		} else if (response.status == 404) {
			throw new Exception("No existe IOP abierta")
		}else {
			throw new Exception("Error al añadir el micro a la campaña")
		}
		
	}
	
}

def deleteMicroFromCampaign(def pomXml, def pipelineData) {
	
	if (env.CAMPAIGN_CANARY_FEATURE_ENABLED!=null && "true".equals(env.CAMPAIGN_CANARY_FEATURE_ENABLED)) {
		def typeVersion
		if (pomXml.artifactVersionQualifier == null || pomXml.artifactVersionQualifier == "") {
			typeVersion = "RELEASE"
		} else if (pomXml.isSNAPSHOT()) {
			typeVersion = "SNAPSHOT"
		} else if (pomXml.isRCVersion()) {
			typeVersion = "RC"
		} else {
			typeVersion = "UNKNOWN"
		}
		
		def response=sendRequestToAbsis3MS('DELETE', "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/iop/${pipelineData.getGarArtifactType().getGarName()}/${pomXml.getApp(pipelineData.garArtifactType)}/${pomXml.artifactMajorVersion}/${pomXml.artifactMinorVersion}/${pomXml.artifactFixVersion}/${typeVersion}",null, "${GlobalVars.CATALOGO_ABSIS3_ENV}")
		if (response.status == 200) {
		   printOpen("App eliminada de la IOP", EchoLevel.ALL)
		} else if (response.status == 404) {
			throw new Exception("No existe IOP abierta")
		}else {
			throw new Exception("Error al añadir el micro a la campaña")
		}
		
	}
	
}

def validateCampaign() {	
	if (env.CAMPAIGN_CANARY_FEATURE_ENABLED!=null && "true".equals(env.CAMPAIGN_CANARY_FEATURE_ENABLED)) {
		def response=sendRequestToAbsis3MS('GET', "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/iop/dependency/validate/${GlobalVars.CATALOGO_ABSIS3_ENV}", null, "${GlobalVars.CATALOGO_ABSIS3_ENV}")
		if (response.status == 200) {
		   printOpen("Validaciones de depencias correctas", EchoLevel.ALL)
		} else if (response.status == 400) {  
			//def responseJson = new groovy.json.JsonSlurper().parseText(response.content)
			def responseJson = response.content
			def dependencies = responseJson.collect{ "\t\u2022 $it.appVersion.appName:$it.appVersion.major.$it.appVersion.minor.$it.appVersion.fix:$it.appVersion.typeVersion"}.join("\n")

			throw new Exception("Con la campaña en marcha no se puede hacer rollback del micro por fallos de dependencias:\n${dependencies}\nGestione la salida de la aplicación de la campaña para poder proceder.")
		} else if (response.status == 404) {
			throw new Exception("No existe IOP abierta")
		} else {
			// Con la campaña en marcha no se puede hacer rollback del micro por fallos de dependencias
			throw new Exception("No se puede validar")
		}
	}
}

def increaseCannaryCampaign(def campaignCannaryPercentage) {
	if (env.SEND_TO_ABSIS3_CATALOG!="" && env.SEND_TO_ABSIS3_CATALOG=="true") {
		def bodyNew = null
		bodyNew = [
			globalCannary: campaignCannaryPercentage
		]
		
		def response=sendRequestToAbsis3MS('PUT', "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/iop",bodyNew, "${GlobalVars.CATALOGO_ABSIS3_ENV}")
		if (response.status == 200) {
			printOpen("Deploy realizado", EchoLevel.ALL)
		}else if (response.status == 404) {
			printOpen("No existe IOP abierta, no se puede incrementar el cannary", EchoLevel.ALL)
			throw new Exception("No existe IOP abierta, no se puede incrementar el cannary")
		}else {
			printOpen("Error al consolidar el valor en el cannary", EchoLevel.ALL)
			throw new Exception("Error al consolidar el valor en el cannary")
		}
	}
}

def notifyCloseIop(def iop) {
	if (env.SEND_TO_ABSIS3_CATALOG!="" && env.SEND_TO_ABSIS3_CATALOG=="true") {
		def response=sendRequestToAbsis3MS('GET', "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/iop/version?open=true",null, "${GlobalVars.CATALOGO_ABSIS3_ENV}")
		if (response.status == 200) {
			//def listIop = new JsonSlurperClassic().parseText(response.content)
			def listIop = response.content
			def apps = "Existen apps pendientes:  "
			boolean existeApps = false
			listIop.each{
				x-> 
				if (x.state=="S") {
					printOpen("Se procede notificar a la aplicacion ${x.app.srvTypeAppId}${x.app.application} para el cierre la ${x.version.major}.${x.version.minor}.${x.version.fix}.${x.version.typeVersion}", EchoLevel.ALL)
					//enviarCorreo al reponsable
					def usuarioReponsable=idecuaRoutingUtils.getResponsiblesAppEmailList(x.app.application, x.app.srvTypeAppId)
					printOpen("Enviaremos el correo a este señor ${usuarioReponsable}", EchoLevel.ALL)
					def subject = "Campaña ${iop.initDate} ${iop.id} finalizada. Cierre ${x.app.srvTypeAppId} ${x.app.application}"
					def body = "La campaña <b>${iop.initDate} ${iop.id}</b> ha finalizado. Puede proceder a hacer el cierre de la app ${x.app.srvTypeAppId}.${x.app.application}. <br>  "+
		                       "La versión concreta es la siguiente ${x.version.major}.${x.version.minor}.${x.version.fix}, acceda a IDECUA para poder hacer el cierre. <br> "+
							   "Saludos <br> "			 
					sendCloseCampaign(subject,body,null, GlobalVars.EMAIL_FROM_ALM,usuarioReponsable)
					apps = apps + subject
					existeApps=true
				}else{
					printOpen("La aplicacion ${x.app.srvTypeAppId}${x.app.application} ya esta cerrada", EchoLevel.ALL)
				}
			}
			if (existeApps == true) {
				//Tenemos que dar warning de que existen apps
				return apps
			}
		}else {
			throw new Exception("Error al acceder a los componentes de la aplicacion")
		}
	}
	return null
}

private sendCloseCampaign(def subject, def body, def replyTo, def from, def to) {
		emailext(
			mimeType: 'text/html',
			replyTo: "${replyTo}",
			body: "${body}",
			subject: "[OpenServices] ${subject} ",
			from: "${from}",
			to: "${to}")	
}

private increaseCampaignCannaryPercentage(String baseFolder, int newPercentage) {

    FileUtils fileUtils = new FileUtils(this)

    String destinationFolder = baseFolder+"/services/arch/api-gateway-1"
    fileUtils.createPathIfNotExists(destinationFolder)
	String fileName = destinationFolder+"/api-gateway-1.yml"
    int actualIntPercentage = newPercentage
    //Tenemos que buscar el fichero
    printOpen("Increase the campaign cannary percentage to ${newPercentage}", EchoLevel.ALL)

    String existeArchAppYaml = sh(returnStdout: true, script: "ls ${fileName} | wc -l")

    existeArchAppYaml = (existeArchAppYaml.trim()).replaceAll('/n', '')

    printOpen("Existe el value ${existeArchAppYaml}", EchoLevel.ALL)

    if (existeArchAppYaml == '0') {

        //No existe el fichero lo voy a generar
        printOpen("Setting ${actualIntPercentage} to file ${fileName}", EchoLevel.ALL)
        sh "echo '${GlobalVars.CAMPAIGN_CANNARY_PERCENTAGE_ABSIS}: ${actualIntPercentage}' >  ${fileName}"

    } else {

        String appYaml = sh(returnStdout: true, script: "cat ${fileName}")
        String newYaml = ''
        String actualPercentage = ''

        appYaml.tokenize('\n').each { x ->
            if (x.contains(GlobalVars.CAMPAIGN_CANNARY_PERCENTAGE_ABSIS)) {
                actualPercentage = x - "${GlobalVars.CAMPAIGN_CANNARY_PERCENTAGE_ABSIS}: "
                actualIntPercentage = actualPercentage.trim().toInteger()
                printOpen("The actual Percentatge is ${actualIntPercentage} the new is ${newPercentage}", EchoLevel.ALL)
            } else {
                if (x != '') newYaml = newYaml + x + '\n'
            }
        }

        sh "echo ${newYaml} > ${fileName}"
        sh "echo '${GlobalVars.CAMPAIGN_CANNARY_PERCENTAGE_ABSIS}: ${newPercentage}' >>  ${fileName}"

    }

}
