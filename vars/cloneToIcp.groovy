import com.project.alm.*

def migrateProfileAndResources(Map currentImage, String environment, String k8sOrigin='cloud', String k8sDestination='ocp', Map resources ){
	if (k8sOrigin.equals('cloud') && k8sDestination.equals('ocp')) {
		return migrateProfileCloudToOcpAndResources(currentImage, environment,resources)
	} else {
		if (k8sOrigin.equals('ocp') && k8sDestination.equals('cloud')){
			return migrateProfileOcpToCloudAndResources(currentImage, environment,resources)
		}
	}	
}

def migrateProfileOcpToCloudAndResources(Map currentImage, String environment, Map resources){
	printOpen("Los datos de la imagen es de ${currentImage}",EchoLevel.INFO)
	if (currentImage["envVars"]!=null) {
		def envVars=currentImage["envVars"]
		if (envVars[GlobalVars.PROFILES_SPRING]!=null && !'ocp'.contains(envVars[GlobalVars.PROFILES_SPRING])) {
			envVars[GlobalVars.PROFILES_SPRING]=migrateProfileOcpToCloud(envVars[GlobalVars.PROFILES_SPRING],environment)		
		}
		Integer requestCpu=resources["requests"]["cpu"]-'m' as Integer
		Integer limitsCpu=resources["limits"]["cpu"]-'m' as Integer
		currentImage["requests_cpu"]=requestCpu+"m"
		currentImage["limits_cpu"]=limitsCpu+"m"
	}
	return currentImage
}


def migrateProfileCloudToOcpAndResources(Map currentImage, String environment, Map resources){
	
	if (currentImage["envVars"]!=null) {
		def envVars=currentImage["envVars"]
		if (envVars[GlobalVars.PROFILES_SPRING]!=null && !'ocp'.contains(envVars[GlobalVars.PROFILES_SPRING])) {
			envVars[GlobalVars.PROFILES_SPRING]=migrateProfileCloudToOcp(envVars[GlobalVars.PROFILES_SPRING],environment)
			
			//Integer requestCpu=currentImage["requests_cpu"]-'m' as Integer
			//Integer limitsCpu=currentImage["limits_cpu"]-'m' as Integer
			
			Integer requestCpu=resources["requests"]["cpu"]-'m' as Integer
			Integer limitsCpu=resources["limits"]["cpu"]-'m' as Integer
			/*
			if ("pro".equals(environment.toLowerCase())) {
				//Tenemos que recalcular los limits
				//Tenemos que recalcular los requests
				requestCpu=requestCpu*GlobalVars.OCP_RATIO_CPU_PRO
				limitsCpu=limitsCpu*GlobalVars.OCP_RATIO_CPU_PRO
			}else {
				//Tenemos que recalcular los limits
				//Tenemos que recalcular los limits
				requestCpu=requestCpu*GlobalVars.OCP_RATIO_CPU_PRE
				limitsCpu=limitsCpu*GlobalVars.OCP_RATIO_CPU_PRE
			}*/
			currentImage["requests_cpu"]=requestCpu+"m"
			currentImage["limits_cpu"]=limitsCpu+"m"
			
		}
	}
	return currentImage
}
def generateShError(def path, def verbose) {
	generateShError(path,verbose,'Error no controlado')
}


def generateShError(def path, def verbose, def mensajeErrorGenerico) {
	printOpen("path ${path}",EchoLevel.INFO)
	
	def errorFile="${path}/logs/error.log"
	def existsFile=fileExists "${errorFile}"
	printOpen("path ${path} ${errorFile} ${existsFile}",EchoLevel.INFO)
	
	if (verbose) {
		def outputFile="${path}/logs/output.log"
		def existsFileOutput=fileExists "${outputFile}"
		
		if (existsFileOutput) {
			def mensajeOutput=sh (script: "cat '${outputFile}'", returnStdout: true)
			printOpen("Los logs son de ${mensajeOutput}",EchoLevel.INFO)
		}
		
		def contenido=sh (script: "ls -lart ${path}/logs/*", returnStdout: true)
		printOpen("Contenido logs ${contenido} ",EchoLevel.INFO)
		
	}
	
	if (existsFile){
		def mensajeError=sh (script: "cat '${errorFile}'", returnStdout: true)
		throw new Exception("${mensajeError}")
	}else{
		throw new Exception("${mensajeErrorGenerico}")
	}	
}

def genateSidecarResources(Map resources,String environment) {
	Map istioSize=[:]
	Map sidecar=[:]
	Map sidecarSize=[:]
	Map limits=[:]
	Map requests=[:]
	istioSize['sidecar']=sidecar
	sidecar['size']=sidecarSize
	sidecarSize['enabled']=true
	sidecarSize['requests']=requests
	sidecarSize['limits']=limits
	
	Integer requestCpu=resources["requests"]["cpu"]-'m' as Integer
	Integer limitsCpu=resources["limits"]["cpu"]-'m' as Integer
	
	Integer requestMem=resources["requests"]["memory"]-'Mi' as Integer
	Integer limitsMem=resources["limits"]["memory"]-'Mi' as Integer

	if ("pro".equals(environment.toLowerCase())) {
		limits['memory']=Math.round(GlobalVars.OCP_SIDECAR_MEM_PRO_LIM)+'Mi'
		if (Math.round(limitsCpu*GlobalVars.OCP_RATIO_SIDECAR_CPU_PRO)<120) {
			limits['cpu']='300m'
		}else {
			limits['cpu']=Math.round(limitsCpu*GlobalVars.OCP_RATIO_SIDECAR_CPU_PRO)+'m'
		}		
		requests['memory']=Math.round(GlobalVars.OCP_SIDECAR_MEM_PRO_REQ)+'Mi'
		requests['cpu']=Math.round(requestCpu*GlobalVars.OCP_RATIO_SIDECAR_CPU_PRO)+'m'
	}else {
		limits['memory']=Math.round(GlobalVars.OCP_SIDECAR_MEM_PRE_LIM)+'Mi'
		if (Math.round(limitsCpu*GlobalVars.OCP_RATIO_SIDECAR_CPU_PRE)<120) {
			limits['cpu']='300m'
		}else {
			limits['cpu']=Math.round(limitsCpu*GlobalVars.OCP_RATIO_SIDECAR_CPU_PRE)+'m'
		}
		
		requests['memory']=Math.round(GlobalVars.OCP_SIDECAR_MEM_PRE_REQ)+'Mi'
		requests['cpu']='5m'
	}

	
	return istioSize
}


def migrateResourcesTo(Map resources, String environment,String micro, String major,String microType, String k8sDestination){
	Integer requestCpu=resources["requests"]["cpu"]-'m' as Integer
	Integer limitsCpu=resources["limits"]["cpu"]-'m' as Integer
	
	//Sacar los datos del catalogo de componentes
	
	def response = sendRequestToAlm3MS(
		'GET',
		"${GlobalVars.URL_CATALOGO_ALM_PRO}/app/${microType}/${micro}/${major}/config?env=${environment}",
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
	
		//def json = new groovy.json.JsonSlurper().parseText(response.content)
		def json = response.content
		requestCpu="${json.cpuRequests}" as Integer
		limitsCpu="${json.cpuLimits}" as Integer
		
		printOpen("Resources del catalogo ${requestCpu} ${limitsCpu} para el micro ${microType}/${micro}/${major} en el entorno de ${environment}", EchoLevel.INFO)
	}
	
	if (k8sDestination.equals('ocp')) {
		if ("pro".equals(environment.toLowerCase())) {
			//Tenemos que recalcular los limits
			//Tenemos que recalcular los requests
			requestCpu=requestCpu*GlobalVars.OCP_RATIO_CPU_PRO
			limitsCpu=limitsCpu*GlobalVars.OCP_RATIO_CPU_PRO
		}else {
			//Tenemos que recalcular los limits
			//Tenemos que recalcular los limits
			requestCpu=requestCpu*GlobalVars.OCP_RATIO_CPU_PRE
			limitsCpu=limitsCpu*GlobalVars.OCP_RATIO_CPU_PRE
		}
	}
	
	printOpen("Resources del catalogo para Ocp ${requestCpu} ${limitsCpu} para el micro ${microType}/${micro}/${major} en el entorno de ${environment}", EchoLevel.INFO)
	
	resources["requests"]["cpu"]=""+requestCpu+"m"
	resources["limits"]["cpu"]=""+limitsCpu+"m"
	return resources
}


def migrateProfileCloudToOcp(String profile, String environment){
	if (profile!=null && !profile.contains('ocp')) {
		profile=profile+",ocp,ocp${environment.toLowerCase()}"
	}
	return profile
}

def migrateProfileOcpToCloud(String profile, String environment){
	if (profile!=null && profile.contains('ocp')) {
		profile=profile-",ocp${environment.toLowerCase()}"
		profile=profile-",ocp"
	}
	return profile
}

def isValidForClone(def image) {
	def definition=image["image"]
	if (definition.contains('MIGRATED')) {
		return true
	} else {
		return false
	}

}

def validateDefinitionImageMigrated(def image) {
	def definition=image["image"]
	if (definition.contains('MIGRATED')) {
		printOpen("La imagen ya contiene el MIGRATED ${definition}",EchoLevel.INFO)
	}else {
		printOpen("La imagen NO es apta para OCP no tiene el MIGRATED ${definition}",EchoLevel.INFO)
		//docker-registry.cloud.project.com/containers/ab3app/arqrun2:MIGRATED-2.17.0-SNAPSHOT-C
		String[] imageString=definition.split(':')
		if (imageString.size()>1) {
			definition=imageString[0]+":"+"MIGRATED-"+imageString[1]	
			image["image"]=definition
		}
		printOpen("La imagen AHORA es apta para OCP no tiene el MIGRATED ${definition}",EchoLevel.INFO)		
	}
}

def getInfoVersionFromImage(def image) {
	def definition=image["image"]
	//docker-registry.cloud.project.com/containers/ab3app/arqrun2:MIGRATED-2.17.0-SNAPSHOT-B
	versionImage=definition.split(':')[1]-'MIGRATED-'
	versionMicro=versionImage//Eliminamos por si acaso el -MIGRATED
	if (versionImage.contains('-SNAPSHOT-')) {
		versionMicro=versionImage.split('-SNAPSHOT-')[0]+'-SNAPSHOT'
	}
	return ["MIGRATED-${versionImage}","${versionMicro}"] as String[]
}


def putImageDockerCompiled(def componentName, def image, boolean isRc) {
	Date today = new Date().clearTime()
	Date expiringDate = new Date().clearTime()+GlobalVars.OCP_IMAGE_DAYS_LIVE
	
	String subtype="RELEASE"
	if (isRc) {
		subtype="RC"
	}
	def nuevoEvento = [
		type: "OCP",
		subtype: "${subtype}",
		application: componentName.toUpperCase(),
		description: image,
		timestamp: today,
		expiringDate: expiringDate,
		user: "USER"
	 ]
 def responseHire = sendRequestToAlm3MS( 'PUT',
											"${GlobalVars.URL_CATALOGO_ALM_PRO}/audit",
											nuevoEvento,
											"${GlobalVars.CATALOGO_ALM_ENV}")
 
	
}
def imageDockesIsCompiled(def app, def version, boolean isRc) {
	def appM=app.toUpperCase()
	def response = sendRequestToAlm3MS(
		'GET',
		"${GlobalVars.URL_CATALOGO_ALM_PRO}/audit/OCP/${appM}",
		null,
		"${GlobalVars.CATALOGO_ALM_ENV}")

	if (response.status == 200) {
		//def json=readJSON text: response.content
		def eventosOcp=response.content
		
		//Tenemos que iterar hasta encontrar la RELEASE
		if (eventosOcp.size()>0) {
			def imagendockerPrevia=null
			eventosOcp.each{
				val->
				if (val!=null) {
					printOpen("El evento es de ${val}",EchoLevel.INFO)
					printOpen("isRc ${isRc} version ${version}",EchoLevel.INFO)
					printOpen("val.subtyp ${val.subtype} description ${val.description}",EchoLevel.INFO)
					//Tenemos que revisar si es RELEASE o no
					if (!isRc && val.subtype=='RELEASE' && val.description.contains(version)) {
						//Tendriamos que ver que contiene la version de imagen docker que queremos
						imagendockerPrevia=val
					}else if (isRc && val.subtype!='RELEASE'  && val.description.contains(version)){
						imagendockerPrevia=val
					}
				}
			}			
			if (imagendockerPrevia!=null) {
				printOpen("La imagen previa es de ${imagendockerPrevia.description}",EchoLevel.INFO)
				return imagendockerPrevia.description
			}else {
				return null
			}
		}else {
			return null
		}		
	}else {
		return null
	}
}


def buildImageDocker(def app, def path, def micro, def namespace, def versionScript, def microType, def componentPom, def environment, def buildCode, def groupId, def  additionalBuildParam='NA', def k8sdestination='ocp') {
	printOpen("El micro esde ${micro} la map es de ${app}",EchoLevel.INFO)
	try {
		def versionsInfo=getInfoVersionFromImage(app)
		def imageDockesIsCompiledPrevious=null
		int contador=0
		def imageOcp=app["image"]
		boolean isRc=false
		Date start=new Date()
		Date end=null
		
		def nameComponentPom=componentPom
		if ('ARQ.MIA'.equals(microType)) {
			nameComponentPom=componentPom
			printOpen("El micro a desplegar es ${nameComponentPom} El micro es  de ${micro}",EchoLevel.INFO)
		}		
		
		String versionScriptBuild=""
		if (versionsInfo.size()>1) {
			versionScriptBuild=versionsInfo[1]			
		}
			
		
		if (versionsInfo.size()>1 && !versionsInfo[1].contains('SNAPSHOT')) {
			//Tenemos que validar si la version existe			
			if (versionsInfo[1].contains('RC')) {
				isRc=true
			}
			imageDockesIsCompiledPrevious=imageDockesIsCompiled(micro,versionsInfo[1],isRc)
		}
		
		String versionDelMicroBuild="${versionsInfo[0]}"
		
		while(contador<2 && imageDockesIsCompiledPrevious==null) {
			if (versionsInfo.size()>1 && versionsInfo[1].contains('SNAPSHOT')) {
				NexusUtils nexus = new NexusUtils(this)
				def versionSnapshot=nexus.getBuildCodeFromSnapshot(groupId,nameComponentPom,versionsInfo[1],GlobalVars.NEXUS_PUBLIC_REPO_NAME)
				if (versionSnapshot!=null && !("").equals(versionSnapshot)) {
					versionScriptBuild=versionSnapshot
				}				
				printOpen("El resultado buildCode ${versionScriptBuild}",EchoLevel.INFO)
			}else {
				printOpen("El resultado buildCode ${versionScriptBuild}",EchoLevel.INFO)
			}
			
			printOpen("${path}/buildArtifactCloud.sh -h '${GlobalVars.Cloud_PRO}' -M ${micro}"+
				" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${nameComponentPom} -E ${environment} -B '${buildCode}' -k ${k8sdestination} "+
				" -i '${groupId}:${versionScriptBuild}:${nameComponentPom}:${additionalBuildParam}:${versionDelMicroBuild}' ",EchoLevel.INFO)
			
			try {
				resultScript = sh( returnStdout: true, script: "${path}/buildArtifactCloud.sh -h '${GlobalVars.Cloud_PRO}' -M ${micro}"+
					" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${nameComponentPom} -E ${environment} -B '${buildCode}' -k ${k8sdestination} "+
					" -i '${groupId}:${versionScriptBuild}:${nameComponentPom}:${additionalBuildParam}:${versionDelMicroBuild}' ")
					
				/*{"id":448619,"extraArgs":"[GROUP_ID=com.project.alm.apps.dataservice.demo, VERSION_ARTIFACT=2.17.0-SNAPSHOT, ARTIFACT_ID=arqrun-micro]","version":"MIGRATED-2.17.0-SNAPSHOT-A","status":"OK","imageRepo1":"docker-registry.cloud.project.com/containers/ab3app/arqrun2","imageRepo2":"docker-registry.cloud.project.com/containers/ab3app/arqrun2"}*/
				end=new Date()
				printOpen("El resultado build ${resultScript}   Duration: ${(end.getTime()-start.getTime())/1000}sec ",EchoLevel.INFO)
				
				def resultFile="${path}/data/result.rslt"
				def existsFile=fileExists "${resultFile}"			
				
				if (existsFile){
					printOpen("El fichero ${resultFile} existe",EchoLevel.INFO)
		
					imageOcp=sh( returnStdout: true, script: "cat ${resultFile} | jq -rj '.imageRepo1+\":\"+ .version'")
					printOpen("Resultado ${imageOcp} existe",EchoLevel.INFO)
				}else {
					printOpen("El fichero ${resultFile} NO existe",EchoLevel.INFO)
				}
				contador=2
								
			}catch(e) {
				if 	(!'ARQ.MIA'.equals(microType)) {
					throw e
				}else {
					if (contador!=0) {
						throw e
					}else{
						nameComponentPom=nameComponentPom+'-server'
					}
				}
			}
			contador=contador+1
			
		}	
		if (imageDockesIsCompiledPrevious==null && imageOcp!=null && versionsInfo.size()>1 && !versionsInfo[1].contains('SNAPSHOT')) {
			//Se tiene que hace update del 
			putImageDockerCompiled(micro,imageOcp,isRc)
		}else if (imageDockesIsCompiledPrevious!=null) {
			imageOcp=imageDockesIsCompiledPrevious
		}
		app["image"]=imageOcp
	}catch(e) {
		printOpen("Error ocurrido ${e}",EchoLevel.ERROR)
		generateShError("${path}",true)
	}	
}

def getArtifactId(def path, def micro, def namespace, def versionScript, def microType, def componentPom, def environment, def buildCode, def k8scluster) {
	try {
		
		printOpen("${path}/getArtifactCloud.sh -h '${GlobalVars.Cloud_PRO}' -M ${micro}"+
			" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${micro}-micro -E ${environment} -B '${buildCode}' -k ${k8scluster}  "+
			"  ",EchoLevel.INFO)
		resultScript = sh( returnStdout: true, script: "${path}/getArtifactCloud.sh -h '${GlobalVars.Cloud_PRO}' -M ${micro}"+
			" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${componentPom} -E ${environment} -B '${buildCode}'  -k ${k8scluster}  "+
			" "
			)
		//Tenemos un fichero dado de alta
		def resultFile="${path}/data/result.rslt"
		def existsFile=fileExists "${resultFile}"
		if (existsFile){
			printOpen("El fichero ${resultFile} existe",EchoLevel.INFO)
			
			def componentId=sh( returnStdout: true, script: "cat ${resultFile} | jq -rj '.id'")
			return componentId
		}else{
			printOpen("El fichero no existe ${resultFile}",EchoLevel.ERROR)
			return 0
		}
		
	}catch(e) {
		printOpen("Error ocurrido ${e}",EchoLevel.ERROR)
		generateShError("${path}",true)
	}
}


def generateArtifact(def path, def micro, def namespace, def versionScript, def microType, def componentPom, def environment, def buildCode,  def resourcesMicro, def k8sdestination) {
	try {
		def mem_req=resourcesMicro["requests"]["memory"]
		def mem_limits=resourcesMicro["limits"]["memory"]
		def cpu_req=resourcesMicro["requests"]["cpu"]
		def cpu_limits=resourcesMicro["limits"]["cpu"]
		
		printOpen("${path}/generateArtifactCloud.sh -h '${GlobalVars.Cloud_PRO}' -M ${micro}"+
			" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${micro}-micro -E ${environment} -B '${buildCode}' -k ${k8sdestination} "+
			" -i '${mem_req}:${mem_limits}:${cpu_req}:${cpu_limits}' ",EchoLevel.INFO)
		resultScript = sh( returnStdout: true, script: "${path}/generateArtifactCloud.sh -h '${GlobalVars.Cloud_PRO}' -M ${micro}"+
			" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${componentPom} -E ${environment} -B '${buildCode}' -k ${k8sdestination} "+
			" -i '${mem_req}:${mem_limits}:${cpu_req}:${cpu_limits}'"
			)
		//Tenemos un fichero dado de alta
		def resultFile="${path}/data/result.rslt"
		def existsFile=fileExists "${resultFile}"
		if (existsFile){
			printOpen("El fichero ${resultFile} existe",EchoLevel.INFO)
			
			def componentId=sh( returnStdout: true, script: "cat ${resultFile} | jq -rj '.id'")
			return componentId
		}else{
			printOpen("El fichero no existe ${resultFile}",EchoLevel.ERROR)
			return 0
		}
		
	}catch(e) {
		printOpen("Error ocurrido ${e}",EchoLevel.ERROR)
		generateShError("${path}",true)
	}
}


def getInfoApp(def path, def micro, def namespace, def versionScript, def microType, def componentPom, def environment, def buildCode) {
	try {
		
		printOpen("${path}/getArtifactCatMsv.sh -h '${GlobalVars.URL_CATALOGO_ALM_PRO}' -M ${micro}"+
			" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${micro}-micro -E ${environment} -B '${buildCode}'  ",EchoLevel.INFO)
		resultScript = sh( returnStdout: true, script: "${path}/getArtifactCatMsv.sh -h '${GlobalVars.URL_CATALOGO_ALM_PRO}' -M ${micro}"+
			" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${componentPom} -E ${environment} -B '${buildCode}' "
			)
		//Tenemos un fichero dado de alta
		def resultFile="${path}/data/result.rslt"
		def existsFile=fileExists "${resultFile}"
		if (existsFile){
			printOpen("El fichero ${resultFile} existe",EchoLevel.INFO)
			
			def isDb2=sh( returnStdout: true, script: "cat ${resultFile} | jq -rj '.db2Dds'")
			def groupId=sh( returnStdout: true, script: "cat ${resultFile} | jq -rj '.groupId'")
			def otherComponentPom=sh( returnStdout: true, script: "cat ${resultFile} | jq -rj '.name'")
			def isMq=sh( returnStdout: true, script: "cat ${resultFile} | jq -rj '.mqConnect'")
			def isKafka=sh( returnStdout: true, script: "cat ${resultFile} | jq -rj '.kafkaConnect'")
			
			if ('true'.equals(isDb2)) {
				return [groupId: "${groupId}", additionalBuildParam: ",DB2_DRIVER_INPUT="+getDb2Driver(environment), componentPom: "${otherComponentPom}",  isMq: "${isMq}" , isKafka: "${isKafka}"]
			}else {
				return [groupId: "${groupId}", additionalBuildParam: '', componentPom: "${otherComponentPom}", isMq: "${isMq}" , isKafka: "${isKafka}"]
			}
		}else{
			printOpen("El fichero no existe ${resultFile}",EchoLevel.ERROR)
			return null
		}
		
	}catch(e) {
		printOpen("Error ocurrido ${e}",EchoLevel.ERROR)
		generateShError(path,true)
	}
}





def call(String path, String environment, String micro, String version, String namespace, String microType, String buildCode, boolean buildAllImages = true, String groupId, String componentPom, String dontGenerateImages='false', String ignoreStart, String ignoreBuild, String dontDeployAsync, String command='CLONE', String k8sOrigin='cloud', String k8sDestination='ocp') {
	
	//Necesitamos primero el ultimo deploy
	//y a partir de ahi hacemos el deploy en OCP
	String additionalBuildParam='NA'
	printOpen("El valor es de ${version}",EchoLevel.DEBUG)
	def versionArray=version.split('\\.')
	String nameMicro=micro.toUpperCase()+versionArray[0]
	printOpen("El valor es de ${nameMicro}",EchoLevel.DEBUG)
	Date start=new Date()
	Date startParcial=null
	String pcldOrigin="PCLD"
	String pcldDestination="PCLD_MIGRATED"
	
	//String k8sOrigin='cloud', 
	//String k8sDestination='ocp'	
	if ("ocp".equals(k8sOrigin)) {
		pcldOrigin="PCLD_MIGRATED"
		pcldDestination="PCLD"
	}else{
		pcldOrigin="PCLD"
		pcldDestination="PCLD_MIGRATED"
	}
	
	String versionScript=version.replaceAll('\\.',':')
	
	
	def componentId=getArtifactId(path,micro,namespace,versionScript,microType,componentPom,environment,buildCode,k8sOrigin)
	Date end=new Date()
	printOpen ("End getArtifactId  EndTime: ${end} Duration: ${(end.getTime()-start.getTime())/1000}sec",EchoLevel.INFO)
	
	printOpen("El componente a clonar es ${componentId}",EchoLevel.INFO)
	//additionalBuildParam=additionalBuildParam+",DB2_DRIVER_INPUT="+getDb2Driver(deployStructure.envCloud)
	
	if (componentId!=0) {
		def infoApp=null
		if ("NA".equals(groupId)) {
			infoApp=getInfoApp(path,micro,namespace,versionScript,microType,componentPom,environment,buildCode)
			if (infoApp!=null) {
				groupId=infoApp['groupId']
				additionalBuildParam= infoApp['additionalBuildParam']
				componentPom=infoApp['componentPom']
			}			
		}
		printOpen("La info del ${infoApp}",EchoLevel.INFO)
		end=new Date()
		printOpen ("End getInfoApp  EndTime: ${end} Duration: ${(end.getTime()-start.getTime())/1000}sec",EchoLevel.INFO)
		
		printOpen("${path}/getLastDeployment.sh -u '${GlobalVars.Cloud_PRO}/api/publisher/v1/api/application/${pcldOrigin}/${namespace}/component/${nameMicro}/deploy/current/environment/${environment}/az/ALL' "+
			" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${componentPom} -E ${environment} -B '${buildCode}' -M ${micro} ",EchoLevel.INFO)
		
		//Recogemos el deploy en Cloud
		def resultScript = null
		startParcial=new Date()
		try {	
			resultScript = sh(returnStdout: true, script: "${path}/getLastDeployment.sh -u '${GlobalVars.Cloud_PRO}/api/publisher/v1/api/application/${pcldOrigin}/${namespace}/component/${nameMicro}/deploy/current/environment/${environment}/az/ALL' "+
			" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${componentPom} -E ${environment} -B '${buildCode}' -M ${micro} "
				)
		}catch(e) {
			printOpen("Error ocurrido ${e}",EchoLevel.ERROR)
			generateShError("${path}",false)
		}
		end=new Date()
		printOpen ("End getLastDeployment   EndTime: ${end} Duration: ${(end.getTime()-start.getTime())/1000}sec Partial Duration: ${(end.getTime()-startParcial.getTime())/1000}sec",EchoLevel.INFO)
		//def resultScript = sh(returnStdout: true, script: "${path}/sendCurlToCloud.sh ")
		
		def zoneDistribution='ALL'
		resultScript=resultScript.substring(3)
		def resultScriptJson=jsonUtils.readJsonFromObject(resultScript)
		def resultScriptYaml=objectsParseUtils.parseYamlObject(resultScript)
		//Dar de alta el artefacto en OCP
		
		printOpen("El resultado del deploy Last es de ${resultScript}",EchoLevel.INFO)
	
		def lastDeployment=resultScriptYaml
		def searchForStable=true
		def resourcesMicro=null
		
		def compareCloudvsOCP="false"
		
		//Build de la imagen
		if (lastDeployment!=null) {
			//VALIDAR QUE EL MICRO ESTE RUNNING
			String microUrl=""
			String microUrlCloud=""
			
			if ("AZ2".equals(zoneDistribution)) {
				if ("ocp".equals(k8sOrigin)) {
					microUrlCloud="https://k8sgateway.${environment.toLowerCase()}.ocp-2.ocp-priv.alm.cloud.lacaixa.es"
					microUrl="https://k8sgateway.${environment.toLowerCase()}.cloud-2.alm.cloud.lacaixa.es"
				} else {
					microUrl="https://k8sgateway.${environment.toLowerCase()}.ocp-2.ocp-priv.alm.cloud.lacaixa.es"
					microUrlCloud="https://k8sgateway.${environment.toLowerCase()}.cloud-2.alm.cloud.lacaixa.es"
				}
			}else if ("AZ1".equals(zoneDistribution)) {
				if ("ocp".equals(k8sOrigin)) {
					microUrlCloud="https://k8sgateway.${environment.toLowerCase()}.ocp-1.ocp-priv.alm.cloud.lacaixa.es"
					microUrl="https://k8sgateway.${environment.toLowerCase()}.cloud-1.alm.cloud.lacaixa.es"
				} else {
					microUrl="https://k8sgateway.${environment.toLowerCase()}.ocp-1.ocp-priv.alm.cloud.lacaixa.es"
					microUrlCloud="https://k8sgateway.${environment.toLowerCase()}.cloud-1.alm.cloud.lacaixa.es"
				}
			}else {
				if ("ocp".equals(k8sOrigin)) {
					microUrlCloud="https://k8sgateway.${environment.toLowerCase()}.ocp-priv.alm.cloud.lacaixa.es"
					microUrl="https://k8sgateway.${environment.toLowerCase()}.alm.cloud.lacaixa.es"
				} else {
					microUrl="https://k8sgateway.${environment.toLowerCase()}.ocp-priv.alm.cloud.lacaixa.es"
					microUrlCloud="https://k8sgateway.${environment.toLowerCase()}.alm.cloud.lacaixa.es"
				}
			}
			
			//RECUPERACION DEPLOYMENT Cloud
			
			Map alm=lastDeployment["alm"]
			Map almApp=alm["apps"]
			Map almAppEnvQualifier=almApp["envQualifier"]
			
			Map stableApp=null
			Map newApp=null
			Map oldApp=null
			
			
			Map almServices=alm["services"]
			def microRoute=componentPom.toLowerCase()
			if (almServices!=null) {
				Map envQualifier=almServices["envQualifier"]
				
				if (envQualifier!=null) {
					microRoute=envQualifier["stable"]["id"]
				}
			}
			
			
			String finalSuffix="/${microRoute}/actuator/info"
			if (microType.equals("ARQ.MIA")) {
				finalSuffix="/arch-service/${microRoute}/actuator/info"
			}
			microUrl=microUrl+finalSuffix
			microUrlCloud=microUrlCloud+finalSuffix
			
			try {
				printOpen("${path}/compareMicroCloudVsOcp.sh -i '${microUrlCloud}' -o '${microUrl}'",EchoLevel.INFO)
				resultScript = sh( returnStdout: true, script: "${path}/compareMicroCloudVsOcp.sh -i '${microUrlCloud}' -o '${microUrl}'")
				
				compareCloudvsOCP="${resultScript}"
				printOpen("El resultado del compare Micros ${resultScript} #${compareCloudvsOCP}#",EchoLevel.INFO)
			}catch(e) {
				printOpen("Error ocurrido en el compareMicroCloudVsOcp  ${e}",EchoLevel.ERROR)
			}
			
			if (compareCloudvsOCP.contains("false")){					
			
				Map almGeneralApp=alm["app"]		
				
				if (almGeneralApp!=null && infoApp!=null) {
					printOpen("App catalogo ${infoApp}",EchoLevel.INFO)
					if ((infoApp['isMq']!=null && infoApp['isMq']=='true') || (infoApp['isKafka']!=null && infoApp['isKafka']=='true')){
						almGeneralApp['stoppableByPlatform']=true
						if (dontDeployAsync!=null && dontDeployAsync=='true') {
							return 5
						}
					}else {
						almGeneralApp['stoppableByPlatform']=false
					}
					printOpen("App catalogo ${almGeneralApp}",EchoLevel.INFO)
				}else{
					printOpen("No tiene info en catalogo",EchoLevel.INFO)
				}
				
				//Tenemos que migrar el profile que esta en variables generales
				Map local=lastDeployment["local"]
				Map app=local["app"]
				def envVars=app["envVars"]
				
				if (envVars!=null) {
					envVars.collect{
						if (GlobalVars.PROFILES_SPRING.equals(it.name)) {
							if (!it.value.contains('ocp') && k8sDestination.equals('ocp')) {
								it.value=migrateProfileCloudToOcp(it.value,environment)
								Map resourcesApp=alm["resources"]
								resourcesMicro=migrateResourcesTo(alm["resources"],environment,micro,versionArray[0],microType,k8sDestination)
								alm["resources"]=resourcesMicro
							}
							if (it.value.contains('ocp') && k8sDestination.equals('cloud')) {
								it.value=migrateProfileOcpToCloud(it.value,environment)
								Map resourcesApp=alm["resources"]
								resourcesMicro=migrateResourcesTo(alm["resources"],environment,micro,versionArray[0],microType,k8sDestination)
								alm["resources"]=resourcesMicro
							}
						}
						if (GlobalVars.nonProxyHosts_EnvVar.equals(it.name)) {
							if (!it.value.contains(GlobalVars.gitlabDomainNonHttps)) {
								it.value=it.value+'|'+GlobalVars.gitlabDomainNonHttps
							}
							//Añadimos las urls de kafka
							if ("PRO".equals(environment.toUpperCase())) {
								it.value=it.value+'|'+'schema-registry-fastdata.svb.lacaixa.es'
							}else{
								it.value=it.value+'|'+'schema-registry-pre-fastdata.svb.lacaixa.es'
							}
						}
						if (GlobalVars.additionalNonProxyHosts_EnvVar.equals(it.name)) {
							if (!it.value.contains(GlobalVars.gitlabDomainNonHttps)) {
								it.value=it.value+','+GlobalVars.gitlabDomainNonHttps
							}
							//Añadimos las urls de kafka
							if ("PRO".equals(environment.toUpperCase())) {
								it.value=it.value+','+'schema-registry-fastdata.svb.lacaixa.es'
							}else{
								it.value=it.value+','+'schema-registry-pre-fastdata.svb.lacaixa.es'
							}
						}
					}
				}
				
				printOpen("Los recursos a asignar a las nueva imagenes docker son de ${resourcesMicro}",EchoLevel.INFO)
				
				if (almAppEnvQualifier["new"]!=null) {
					newApp=almAppEnvQualifier["new"]
				}
				if (almAppEnvQualifier["stable"]!=null) {
					stableApp=almAppEnvQualifier["stable"]
				}
				if (almAppEnvQualifier["old"]!=null) {
					oldApp=almAppEnvQualifier["old"]
				}
				
				if (stableApp!=null) {
					def currentImage=stableApp["image"]
					stableApp=migrateProfileAndResources(stableApp,environment,k8sOrigin,k8sDestination,alm["resources"])
					printOpen("El currentImage es de JSON ${currentImage} stable",EchoLevel.INFO)
				}else {
					printOpen("No tiene currentImage stable",EchoLevel.INFO)
				}
				if (newApp!=null) {
					def currentImage=newApp["image"]
					//docker-registry.cloud.project.com/containers/ab3app/arqrun2:2.17.0-SNAPSHOT-A
					newApp=migrateProfileAndResources(stableApp,environment,k8sOrigin,k8sDestination,alm["resources"])
					printOpen("El currentImage es de JSON ${currentImage} new",EchoLevel.INFO)
				}else {
					printOpen("No tiene currentImage new",EchoLevel.INFO)
				}
				if (oldApp!=null) {
					def currentImage=oldApp["image"]
					//docker-registry.cloud.project.com/containers/ab3app/arqrun2:2.17.0-SNAPSHOT-A
					oldApp=migrateProfileAndResources(stableApp,environment,k8sOrigin,k8sDestination,alm["resources"])
					printOpen("El currentImage es de JSON ${currentImage} old",EchoLevel.INFO)
				}else {
					printOpen("No tiene currentImage old",EchoLevel.INFO)
				}
				
				if (resourcesMicro!=null && alm["istio"]==null) {
					alm["istio"]=genateSidecarResources(resourcesMicro,environment)
				}
				
				printOpen("Generate Artifact ",EchoLevel.INFO)
				//ALTA COMPONENTE_OCP.
				def idNewComponent=generateArtifact(path,micro,namespace,versionScript,microType,componentPom,environment,buildCode,resourcesMicro,k8sDestination)
				end=new Date()
				printOpen ("End generateArtifact  EndTime: ${end} Duration: ${(end.getTime()-start.getTime())/1000}sec",EchoLevel.INFO)			
				printOpen("El ID del componente es de ${idNewComponent}",EchoLevel.INFO)
	
				//localAlm
				Map localAlm=lastDeployment["local"]
				if (localAlm!=null) {
					Map localAppAlm=localAlm["app"]
					if (localAppAlm!=null) {
						Map ingressAppAlm=localAppAlm["ingress"]
						if (ingressAppAlm!=null) {
							Map nameSpaceSystem=["enabled":"false"]
							ingressAppAlm.put("namespacesSystem",nameSpaceSystem)
							printOpen("Añadimos el ${ingressAppAlm} ",EchoLevel.INFO)
						}
					}
				}
				//GENERACION IMAGEN DOCKER.
				def colour='G'
				printOpen("Añadimos el ${ignoreBuild} command ${command}  ${dontGenerateImages} ${buildAllImages} ${zoneDistribution}",EchoLevel.INFO)
				try {
					if (ignoreBuild=='false' || command=='START' ) {
						if (stableApp!=null) {
							if (dontGenerateImages.equals('false') && !"AZ2".equals(zoneDistribution) && !isValidForClone(stableApp)) {
								buildImageDocker(stableApp,path,micro,namespace,versionScript,microType,componentPom,environment,buildCode,groupId,additionalBuildParam,k8sDestination)
								end=new Date()
								printOpen ("End buildImageDocker stable  EndTime: ${end} Duration: ${(end.getTime()-start.getTime())/1000}sec",EchoLevel.INFO)
							} else {
								printOpen ("Imagen docker ya migrada y correcta",EchoLevel.INFO)
							}
							colour=stableApp['colour']
						}
						if ( (stableApp!=null && buildAllImages && newApp!=null) || (stableApp==null && newApp!=null)) {
							if (dontGenerateImages.equals('false') && !"AZ2".equals(zoneDistribution) && !isValidForClone(newApp)) {
								buildImageDocker(newApp,path,micro,namespace,versionScript,microType,componentPom,environment,buildCode,groupId,additionalBuildParam,k8sDestination)
								end=new Date()
								printOpen ("End buildImageDocker new  EndTime: ${end} Duration: ${(end.getTime()-start.getTime())/1000}sec",EchoLevel.INFO)
							} else {
								printOpen ("Imagen docker ya migrada y correcta",EchoLevel.INFO)
							}
							colour=newApp['colour']
						}
						if ( (stableApp!=null && buildAllImages && oldApp!=null) || (stableApp==null && oldApp!=null)) {
							if (dontGenerateImages.equals('false') && !"AZ2".equals(zoneDistribution) && !isValidForClone(oldApp)) {
								buildImageDocker(oldApp,path,micro,namespace,versionScript,microType,componentPom,environment,buildCode,groupId,additionalBuildParam,k8sDestination)
								end=new Date()
								printOpen ("End buildImageDocker old  EndTime: ${end} Duration: ${(end.getTime()-start.getTime())/1000}sec",EchoLevel.INFO)
							} else {
								printOpen ("Imagen docker ya migrada y correcta",EchoLevel.INFO)
							}
						}
					}	
				}catch(e) {
					printOpen("Error ocurrido ${e}",EchoLevel.ERROR)
					generateShError("${path}",false)
				}
				if (stableApp!=null && ignoreBuild=='false') {
					validateDefinitionImageMigrated(stableApp)
				}
				if (newApp!=null && ignoreBuild=='false') {
					validateDefinitionImageMigrated(newApp)
				}
				if (oldApp!=null && ignoreBuild=='false') {
					validateDefinitionImageMigrated(oldApp)
				}
				startParcial=new Date()
				//DEPLOY DEL MICRO EN OCP
				try {
					// Esto no es correcto.... es un yaml no un json
					
					printOpen("El resultado es de JSON para ${k8sDestination} ${objectsParseUtils.toYamlString(resultScriptYaml,true)}",EchoLevel.INFO)
					
					printOpen("${path}/deployArtifactCloud.sh -h '${GlobalVars.Cloud_PRO}' -M ${micro}"+
						" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${componentPom} -E ${environment} -B '${buildCode}' -k ${k8sDestination} "+
						" -i '${objectsParseUtils.toYamlString(resultScriptYaml,true)}' -I ${idNewComponent} -c ${colour} -Z ${zoneDistribution} -w ${ignoreStart}",EchoLevel.INFO)
					resultScript = sh( returnStdout: true, script: "${path}/deployArtifactCloud.sh -h '${GlobalVars.Cloud_PRO}' -M ${micro}"+
						" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${componentPom} -E ${environment} -B '${buildCode}' -k ${k8sDestination} "+
						" -i '${objectsParseUtils.toYamlString(resultScriptYaml,true)}' -I ${idNewComponent} -c ${colour} -Z ${zoneDistribution} -w ${ignoreStart}")
					
					end=new Date()
					printOpen ("End deployArtifactCloud  EndTime: ${end} Duration: ${(end.getTime()-start.getTime())/1000}sec  Partial Duration: ${(end.getTime()-startParcial.getTime())/1000}sec",EchoLevel.INFO)
					
				}catch(e) {
					printOpen("Error ocurrido en el deploy ${e}",EchoLevel.ERROR)
					generateShError("${path}",true)
				}		
				
				if ('false'.equals(ignoreStart)) {
					try {
						printOpen("${path}/validateMicroIsUp.sh -h '${microUrl}' "+
							" -E ${environment} "+
							" ",EchoLevel.INFO)
						resultScript = sh( returnStdout: true, script: "${path}/validateMicroIsUp.sh -h '${microUrl}' -E ${environment} ")
						
						end=new Date()
						printOpen ("End validateMicroIsUp  EndTime: ${end} Duration: ${(end.getTime()-start.getTime())/1000}sec",EchoLevel.INFO)
						
					}catch(e) {
						printOpen("Error ocurrido ${e}",EchoLevel.ERROR)
						
						generateShError("${path}",true, 'El micro no arranca')
					}										
				}				
			} else {
				printOpen("Esta version ya esta en OCP/Cloud ${nameMicro} ${version}", EchoLevel.INFO)
			}		
		} else {
			return 0
		}
	} else {
		return 0
	}
	return 1
	//Necesitamos el ID de la imaegen 
	//El group Id del micro
	//La versoin concreta
	
	//Deploy de la misma

	
}

