import com.caixabank.absis3.ArtifactType
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.FileUtils
import com.caixabank.absis3.GitUtils
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineStructureType
import com.caixabank.absis3.GarAppType
import com.caixabank.absis3.Utilities
import groovy.json.JsonSlurperClassic
import com.caixabank.absis3.ICPApiResponse

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK
import java.util.Date
import java.text.SimpleDateFormat


def retrieveNewAndStableAppICPDeploymentMetadata(def valuesDeployed, String tagServicesOrApps) {
	
	if (valuesDeployed["absis"]!=null) {

		Map valuesDeployedLocal=valuesDeployed["absis"]


		if (valuesDeployedLocal[tagServicesOrApps]!=null) {

			Map valuesDeployedLocalApp=valuesDeployedLocal[tagServicesOrApps]
			
			if ("app".equals(tagServicesOrApps)) {
				return valuesDeployedLocalApp
			}

			if (valuesDeployedLocalApp["envQualifier"]!=null) {

				def valuesDeployedLocalEnvVarsList = valuesDeployedLocalApp["envQualifier"]
				return valuesDeployedLocalEnvVarsList
			}

		}
	}

	return [:]
}


def getYamlDeploy( def body) {
	def dumperOptions = new DumperOptions()
	dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
	def yaml = new Yaml(dumperOptions)
	
	def microRedirgido=(Map)yaml.load( body)
	printOpen("El map ${microRedirgido}",EchoLevel.INFO)
	def appsDefinition=retrieveNewAndStableAppICPDeploymentMetadata(microRedirgido,'apps')
	def servicesDefinition=retrieveNewAndStableAppICPDeploymentMetadata(microRedirgido,'services')
	def appDefinition=retrieveNewAndStableAppICPDeploymentMetadata(microRedirgido,'app')
	//Setteamos el numero de pods a 0
	appDefinition['replicas']=0
	//Setteamos el numero de pods del new o del stable a 0
	Map app1 = appsDefinition["stable"]
	if (app1!=null) {
		app1['replicas']=0
		app1['image']='docker-registry.cloud.caixabank.com/'+(app1['image']-'pro-registry.pro.caas.caixabank.com/')//Correccion por si estaban desplegado en el nexus 
	}
	app1 = appsDefinition["new"]
	if (app1!=null) {
		app1['replicas']=0
		app1['image']='docker-registry.cloud.caixabank.com/'+(app1['image']-'pro-registry.pro.caas.caixabank.com/')//Correccion por si estaban desplegado en el nexus 
	}
	Map services = servicesDefinition['stable']
	if (services!=null) {
		services['targetColour']='G'
		services['targetName']='redirecttodev'
		services['targetInstance']='redirecttodev'
	}
	services = servicesDefinition['new']
	if (services!=null) {
		services['targetColour']='G'
		services['targetName']='redirecttodev'
		services['targetInstance']='redirecttodev'
	}
	services = servicesDefinition['newNonDev']
	if (services!=null) {
		services['targetColour']='G'
		services['targetName']='redirecttodev'
		services['targetInstance']='redirecttodev'
	}
	def absis3 = [
		service: [enabled:true],
		serviceexternal: [enabled:true]
	]
	
	microRedirgido['absis3']=absis3
	return yaml.dumpAsMap(microRedirgido)
	
}

def cleanMicrosOnlyOld(def microsDeployedOnDev, def maxDeaysToClean=30) {	
	def microsOldToBeExterminated=[]
	
	microsDeployedOnDev.each{
		micro-> 
		def installedOn=micro['installedOn']
		Date today = new Date().clearTime()
		Date priorDate = today - maxDeaysToClean
		def typeMicroOn=micro['appType']
		
		SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd")
		Date datOfApp=SDF.parse(installedOn.substring(0,10))
		
		if (datOfApp.before(priorDate) && !typeMicroOn.equals('ARQ.MIA') ) {
			microsOldToBeExterminated.add(micro)
		}
	}
	
	printOpen("########################### MICROS A ANALIZAR REPETIDOS DEV #################################", EchoLevel.ALL)
	microsOldToBeExterminated.each{
		micro->
		def installedOn=micro['installedOn']
		def typeMicroOn=micro['appType']
		def microOn=micro['appName']
		def majorOn=micro['major']
		def minorOn=micro['minor']
		def fixOn=micro['fix']
		def typeOn=micro['typeVersion']		
		printOpen("Micro DEV. ${typeMicroOn}.${microOn} ${majorOn}.${minorOn}.${fixOn}-${typeOn}: ${installedOn}", EchoLevel.ALL)		
	}
	printOpen("############################################################################################", EchoLevel.ALL)
	return microsOldToBeExterminated
}

def getMicros(def environment) {
	return getMicrosNotRedirected(environment, false, false)
}

def getMicrosNotRedirected(def environment, def redirected=false) {
	return getMicrosNotRedirected(environment, redirected, false)
}
	

def getMicrosNotRedirected(def environment, def redirected, def redirectedInterested) {
	def response=null
	if (redirectedInterested) {
		response=sendRequestToAbsis3MS('GET', "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${environment}?isRedirected=${redirected}",null, "${GlobalVars.CATALOGO_ABSIS3_ENV}")
	}else {
		response=sendRequestToAbsis3MS('GET', "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${environment}",null, "${GlobalVars.CATALOGO_ABSIS3_ENV}")
	}
	
	if (response.status == 200) {

	   def listMicros = response.content
	   if (listMicros.size()>0) {
		   return listMicros
	   }else {
		   return listMicros
	   }
	} else if (response.status == 404) {
		throw new Exception("Fallan los endpoints")
	}else {
		throw new Exception("Error al consultar los no redirigidos")
	}
}
	


def deployRedirectAndZeroPods(def micro,def newChar, def newDocker) {
	//Tenemos que recoger el micro del id 
	//Tenemos que recuperar el deploy
	//Tenemos que hacer el deploy con 0 pods y redirigir los service contra la redirectora 
	//appICPId
	def componentId=0
	def app=micro.garApp.toUpperCase()+micro.major
	//ICPApiResponse response = sendRequestToICPApi("v1/application/${GlobalVars.ICP_APP_ID_APPS}/component",null,"GET","${GlobalVars.ICP_APP_APPS}","", false, false)
	ICPApiResponse response = sendRequestToICPApi("v2/api/application/PCLD/${GlobalVars.ICP_APP_APPS}/component/${app}",null,"GET","${GlobalVars.ICP_APP_APPS}","", false, false)
	
	if (response.statusCode>=200 && response.statusCode<300) {
		//if (response.body!=null && response.body.size()>=1) {
		if (response.body!=null) {
			printOpen("Procedemos a buscar el micro ${app}",EchoLevel.INFO)
			//def component = response.body.find { it.name.equals(app) }
			def component = response.body
			printOpen("Hemos recuperado este elemento ${component} ${component.id}",EchoLevel.INFO)
			componentId=component.id	
			
			//Miramos si tenemos que actualizar la tecnologia
			if (component.chart.id<newChar) {
				printOpen("Tenemos que actualizar el chart es muy antiguo ${component.chart.id} para la app ${component.name}",EchoLevel.INFO)
				component.chart.id=newChar
				component.version.id=newDocker
				printOpen("El nuevo componente actualizado es de ${component} ${component.name}",EchoLevel.INFO)
				
				response = sendRequestToICPApi("v1/api/application/PCLD/${GlobalVars.ICP_APP_APPS}/component/${component.name}",component,"PUT","${GlobalVars.ICP_APP_APPS}","", false, false)
				if (response.statusCode>=200 && response.statusCode<300) {
					printOpen("Tecnologia correctamente actualizada",EchoLevel.INFO)
				}else {
					throw new Exception("Error al actualizar el componente ${componentId} y el status ${response.statusCode}")
				}
			}		 	
		}
		printOpen("El ID para la app ${app} son de ${componentId}",EchoLevel.INFO)
		//Procedemos a recoger el ultimo Deploy
		//response = sendRequestToICPApi("v1/application/PCLD/${GlobalVars.ICP_APP_APPS}/component/${componentId}/environment/${GlobalVars.DEV_ENVIRONMENT.toUpperCase()}/availabilityzone/ALL/status",null,"GET","${GlobalVars.ICP_APP_APPS}","",false,false)
		response=sendRequestToICPApi("v1/application/PCLD/${GlobalVars.ICP_APP_APPS}/component/${componentId}/deploy/current/environment/${GlobalVars.DEV_ENVIRONMENT.toUpperCase()}/az/ALL",null,"GET","${GlobalVars.ICP_APP_APPS}","",false,false)
		
		if (response.statusCode>=200 && response.statusCode<300) {
			if (response.body!=null) {
				//Tenemos que modificar para dejar 0 pods
				def yamlDeployed=getYamlDeploy(response.body.values)				
			
				def toJson = {
					input ->
					groovy.json.JsonOutput.toJson(input)
				}
				
				def body = [
					az: "ALL",
					environment: "${GlobalVars.DEV_ENVIRONMENT.toUpperCase()}",
					values: yamlDeployed
				]	
			
				def response1 = sendRequestToICPApi("v1/application/PCLD/${GlobalVars.ICP_APP_APPS}/component/${componentId}/deploy",body,"POST","${GlobalVars.ICP_APP_APPS}","v1/application/PCLD/${GlobalVars.ICP_APP_APPS}/component/${componentId}/deploy",true,true)
				
				if (response1.statusCode>=200 && response1.statusCode<300) {
					printOpen("Redireccion efectuada ",EchoLevel.INFO)
					//I OCP que pasa con el 
					if (env.ENV_K8S_OCP!=null && env.ENV_K8S_OCP.contains(GlobalVars.DEV_ENVIRONMENT.toUpperCase())) {
						///api/publisher/v1/api/application/PCLD/AB3APP/component/SRVPIL1/deploy
						try {
							response1 = sendRequestToICPApi("v1/api/application/PCLD_MIGRATED/${GlobalVars.ICP_APP_APPS}/component/${app}/deploy",body,"POST","${GlobalVars.ICP_APP_APPS}","v1/api/application/PCLD_MIGRATED/${GlobalVars.ICP_APP_APPS}/component/${app}/deploy",true,true)
							if (response1.statusCode>=200 && response1.statusCode<300) {
								printOpen("Redireccion efectuada OCP",EchoLevel.INFO)
							}
						}catch(Exception e) {
							printOpen("Error o",EchoLevel.ERROR)
						}
					}
					
				}else{
					printOpen("Algo ha fallado en la redireccion ",EchoLevel.ERROR)
					return false
				}
				return true
			}else {
				printOpen("No tenemos ningun deploy valido para  la app ${app} son de ${componentId}",EchoLevel.INFO)
				return false
			}
		}else {
			if (response.statusCode==404) {
				printOpen("El micro no tiene deploys ${app} son de ${componentId}",EchoLevel.ERROR)
				return false
			}else {
				printOpen("Error en la consulta para de ddeploys para  la app ${app} son de ${componentId}",EchoLevel.ERROR)
				return false
			}
		}		
	}else {
		printOpen("Error en la consulta contra el API de ICP ${response.statusCode}" ,EchoLevel.ERROR)
		return false
	}
}

def deployRedirectedCatMsv(def micro) {
	def deployParams = [
		replicas: 0,
		memoryLimits: ""+micro.deployedMemoryLimits-'Mi',
		memoryRequests: ""+micro.deployedMemoryRequests-'Mi',
		cpuLimits: ""+micro.deployedCpuLimits-'m',
		cpuRequests: ""+micro.deployedCpuRequests-'m'
	]
	
	def bodyDeploy = [
		deploy: deployParams,
		srvEnvId: GlobalVars.DEV_ENVIRONMENT.toUpperCase(),
		microRedirected: true,
		installationType: 'I'
	]
	
	printOpen("Se procede a updatear el catalogo los siguientes valores ${bodyDeploy} para el micro ${micro.appType}${micro.appName} ${micro.major}/${micro.minor}/${micro.fix}/${micro.typeVersion}",EchoLevel.INFO)
	def response = sendRequestToAbsis3MS(
		'PUT',
		"${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${micro.appType}/${micro.garApp}/version/${micro.major}/${micro.minor}/${micro.fix}/${micro.typeVersion}/deploy",
		bodyDeploy,
		"${GlobalVars.CATALOGO_ABSIS3_ENV}")
	if (response.status == 200) {
		printOpen("Micro Redirigido correctamenhte",EchoLevel.ERROR)
		return true
	}else {
		printOpen("Error en la update del redirigido",EchoLevel.ERROR)
		return false
	}
	
}

def stringToMap(def microsToClean) {
	if (microsToClean==null) {
		return [:]
	}else {
		String[] str
		str=microsToClean.split(',')
		def mapResult = [:]
		str.each {
			val->
				if (val!='' && val!=null) {
					mapResult[val.toLowerCase()] = true
				}
		}
		return mapResult
	}
}


def redirectAndNotify(def microsOnDev, def microsOnTst, def maxMicrosRedirected, def microsToClean, def newVersionChar, def newVersionDocker) {
	
	printOpen("************************** MICROS DE TST A COMPARAR *****************************************", EchoLevel.ALL)
	microsOnTst.each{
		micro->
		def installedOn=micro['installedOn']
		def typeMicroOn=micro['appType']
		def microOn=micro['appName']
		def majorOn=micro['major']
		def minorOn=micro['minor']
		def fixOn=micro['fix']
		def typeOn=micro['typeVersion']		
		printOpen("Micro TST. ${typeMicroOn}.${microOn} ${majorOn}.${minorOn}.${fixOn}-${typeOn}: ${installedOn}", EchoLevel.ALL)		
	}	
	printOpen("**********************************************************************************************", EchoLevel.ALL)
	
	
	
	mapResult=generateListToMap(microsOnTst)
	def mapMicros=stringToMap(microsToClean)
	printOpen("La lista de micros a limpiar es de ${mapMicros}",EchoLevel.INFO)
	
	if (mapMicros!=null && mapMicros['na']!=null) {
		mapMicros=null
	}
	if (microsOnDev!=null) {
		String idMicro=""
		boolean weHaveToContinue=true
		boolean weHaveToContinueMain=true
		int numMicrosRedirected=0
		microsOnDev.each {
			val-> idMicro=val.srvAppId.toString()+"-"+val.major.toString()+"-"+val.minor.toString()+"-"+val.fix.toString()
				  if (weHaveToContinueMain) {
					  printOpen("numMicrosRedirected ${numMicrosRedirected} maxMicrosRedirected ${maxMicrosRedirected} weHaveToContinue ${weHaveToContinue}",EchoLevel.INFO)
					  weHaveToContinue=true
					  if (mapResult[idMicro]==true) {
						  printOpen("Tenemos el micro ${val.garApp.toString().toLowerCase()} con version repetida vamos a ver si esta en la lista",EchoLevel.INFO)
					      if ((mapMicros==null || mapMicros.isEmpty() ||  (mapMicros!=null && mapMicros[val.garApp.toString().toLowerCase()]==true))) {
							  printOpen("Tenemos el micro ${val.srvAppId} en TST",EchoLevel.INFO)
							  
							  printOpen("El micro va a ser eliminado ${val.srvAppId} ${val.garApp.toString().toLowerCase()}-${val.major.toString()} ",EchoLevel.INFO)
							  
							  if (weHaveToContinue) {
								  weHaveToContinue=deployRedirectAndZeroPods(val,newVersionChar,newVersionDocker)
								  if (weHaveToContinue==true){
									  weHaveToContinue=deployRedirectedCatMsv(val)
									  if (weHaveToContinue) {
										  notifyToAppTeam(val)
										  numMicrosRedirected=numMicrosRedirected+1
									  }
								  }
							  }
							  //SOLO MOVEMOS LOS MICROS DE 10 en 10
							  if (numMicrosRedirected>maxMicrosRedirected) {
								  weHaveToContinueMain=false
							  }else{
								  printOpen("Ya hemos migrado todas las apps ${weHaveToContinueMain}",EchoLevel.INFO)
								  printOpen("No se migrara ya hemos finalizado por hoy ${val.srvAppId} en TST",EchoLevel.INFO)
							  }
							  //hacer deploy en k8s marcando como redirigido
							  //hacer deploy en catalago marcando como redirigido
							  //enviar correo
						  }
					  }else {
							  printOpen("NO Tenemos el micro ${val.srvAppId} ${val.garApp} en TST",EchoLevel.INFO)
					  }
			    }
		}
	}
	
}

def generateListToMap(def microsOnTst) {
	def mapResult = [:]
	
	if (microsOnTst!=null) {
		microsOnTst.each {
			val-> mapResult[val.srvAppId+"-"+val.major+"-"+val.minor+"-"+val.fix] = true
		}
	}
	
	return mapResult
}

private sendEmail(def subject, def body, def replyTo, def from, def to) {
	printOpen("${body}-${replyTo}", EchoLevel.ALL)
	
	emailext(
		mimeType: 'text/html',
		replyTo: "${replyTo}",
		body: "${body}",
		subject: "[OpenServices] ${subject} ",
		from: "${from}",
		to: "${to}")
}

def notifyToAppTeam(def micro) {

	printOpen("Se procede notificar a la aplicacion ${micro.appType}${micro.garApp} para el cierre la ${micro.major}.${micro.minor}.${micro.fix}.${micro.typeVersion}", EchoLevel.ALL)
	//enviarCorreo al reponsable
	def usuarioReponsable=idecuaRoutingUtils.getResponsiblesAppEmailList(micro.garApp, micro.appType)
	printOpen("Enviaremos el correo a este señor ${usuarioReponsable}", EchoLevel.ALL)
	def subject = "Version ${micro.major}.${micro.minor}.${micro.fix}.${micro.typeVersion} de la app ${micro.garApp} se procede a redirigir contra TST"
	def body = "<p>Buenos días,</p><p>Procedemos a redirigir la versión del micro <b>${micro.major}.${micro.minor}.${micro.fix}.${micro.typeVersion} de la app ${micro.garApp} de DEV a TST ya que es la misma version.</b></p>"+
	           "<p>Puede revertir la situación mediante los procesos del ALM para desplegar en DEV.</p>"
    		   "<p>Saludos </p>"			 
	sendEmail(subject,body,null, GlobalVars.EMAIL_FROM_ALM,usuarioReponsable)				
	
	return null
}
