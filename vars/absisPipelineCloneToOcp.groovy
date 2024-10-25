import groovy.transform.Field
import com.project.alm.*
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams
@Field String environment
@Field String micro
@Field String type
@Field String version
@Field String namespace
@Field String groupId
@Field String componentPom
@Field String multiplesMicros
@Field String dontGenerateImages
@Field String scriptsPath
@Field String buildCode
@Field String ignoreStart
@Field String dontDeployAsync
@Field String command
@Field String k8sOrigin 
@Field String k8sDestination
@Field String k8sOrigink8sDestination

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters
	
	environment = params.environmentParam
	micro = params.microParam
	type = params.typeParam
	version = params.versionParam
	namespace = params.namespaceParam
	groupId = 'NA'
	componentPom = params.componentPomParam
	multiplesMicros = params.multiplesMicrosParam
	dontGenerateImages = params.dontGenerateImagesParam
	ignoreStart = params.ignoreStart
	ignoreBuild = params.ignoreBuild
//Ignoramos el value correcto
//ignoreBuild = 'false'
	numMaxMicros = params.numMaxMicros as Integer
	dontDeployAsync = params.dontDeployAsync
	command = params.command
	k8sOrigink8sDestination = params.k8sOrigink8sDestination
	int contador=0
	scriptsPath = ""
	buildCode = env.BUILD_TAG

	
	pipelineOS.withoutSCM(pipelineParams){
		try {
			k8sOrigin='icp'
			k8sDestination='ocp'
			
			if (k8sOrigink8sDestination!=null && "icp->ocp".equals(k8sOrigink8sDestination)) {
				k8sOrigin='icp'
				k8sDestination='ocp'
			}
			if (k8sOrigink8sDestination!=null && "ocp->icp".equals(k8sOrigink8sDestination)) {
				k8sOrigin='ocp'
				k8sDestination='icp'
			}
			if (command==null) {
				command='CLONE'
			}
			printOpen("El multiples Micros ${multiplesMicros}",EchoLevel.INFO)
			printOpen("El componentPom ${componentPom}",EchoLevel.INFO)
			printOpen("El micro ${micro}",EchoLevel.INFO)
			
			stageOS('init-pipeline'){
				initGlobalVars()
				scriptsPath=CopyAllLibraryScripts()
			}
			stageOS('clone-environment'){
				withCredentials([usernamePassword(credentialsId: 'ALM_LOGCOLLECTOR_CREDENTIALS', passwordVariable: 'ALM_LOGCOLLECTOR_PASSWORD', usernameVariable: 'ALM_LOGCOLLECTOR_USERNAME')]) {
					passwordAlmLogUser="${ALM_LOGCOLLECTOR_USERNAME}"
					passwordAlmLogPassword="${ALM_LOGCOLLECTOR_PASSWORD}"
				}
				String microsCloneOK=""
				String microsCloneIgnorados=""
				String microsCloneKO=""
				String microsCloneKOButNotError=""
				
				if ('ALL'.equals(multiplesMicros) || !'NA'.equals(multiplesMicros)) {
				    currentBuild.displayName="${multiplesMicros}-${environment}-${command}-${k8sOrigink8sDestination}"
					//Tenemos que clonar todo el entorno o ciertos micros 
					if ('ALL'.equals(multiplesMicros)) {
						
						printOpen("Se solicita el duplicado del entorno ${multiplesMicros} ${environment}",EchoLevel.INFO)
						
						def microsDeployedOnEnvironment=redirectCatalogUtils.getMicros(environment.toUpperCase())
						if (microsDeployedOnEnvironment!=null && microsDeployedOnEnvironment.size()>0) {
							def contadorClone=0
							for (def tuplaMicro: microsDeployedOnEnvironment) {								
								printOpen("App ${tuplaMicro.appType.toString()}.${tuplaMicro.garApp.toString()} Version ${tuplaMicro.major.toString()}.${tuplaMicro.minor.toString()}.${tuplaMicro.fix.toString()}}",EchoLevel.INFO)
								if (type.equals('NA') || (!type.equals('NA') && type.equals(tuplaMicro.appType.toString()))) {
									if (contador<numMaxMicros) {
										try {
											contadorClone=cloneToIcp(scriptsPath,environment, tuplaMicro.garApp.toString(), tuplaMicro.major.toString()+'.'+tuplaMicro.minor.toString()+'.'+tuplaMicro.fix.toString(), namespace, tuplaMicro.appType.toString(), buildCode, true, groupId, tuplaMicro.appName.toString(), 'false', ignoreStart, ignoreBuild,dontDeployAsync,'CLONE',k8sOrigin,k8sDestination)
											if (contadorClone==0) {
												contador--
												microsCloneKO+=tuplaMicro.garApp.toString()+"-"+tuplaMicro.major.toString()+'.'+tuplaMicro.minor.toString()+'.'+tuplaMicro.fix.toString()+"#"
											}else {
												if (contadorClone==5) {
													contador--
													microsCloneIgnorados+=tuplaMicro.garApp.toString()+"-"+tuplaMicro.major.toString()+'.'+tuplaMicro.minor.toString()+'.'+tuplaMicro.fix.toString()+"#"
												}else {
													microsCloneOK+=tuplaMicro.garApp.toString()+"-"+tuplaMicro.major.toString()+'.'+tuplaMicro.minor.toString()+'.'+tuplaMicro.fix.toString()+"#"
												}
												
											}
										}catch(Exception e) {
											printOpen("El clone del micro ha fallado.... ${tuplaMicro.garApp.toString()} ${tuplaMicro.major.toString()} ${e}",EchoLevel.INFO)
											microsCloneKOButNotError+=tuplaMicro.garApp.toString()+"-"+tuplaMicro.major.toString()+'.'+tuplaMicro.minor.toString()+'.'+tuplaMicro.fix.toString()+"#"
										}
									}
									contador++
								}								
							}
						}
						printOpen("microsCloneOK  ${microsCloneOK}",EchoLevel.INFO)
						printOpen("microsCloneKO (No sabemos porque no van) ${microsCloneKO}",EchoLevel.INFO)
						printOpen("microsCloneKOButNotError (Petan a lo loco) ${microsCloneKOButNotError}",EchoLevel.INFO)
						printOpen("microsCloneIgnorados (Son ignorados por algun motivo) ${microsCloneIgnorados}",EchoLevel.INFO)
					}else {
						//Tenemos que recoger los micro 
						//typeParam1;microParam1;version#typeParam2;microParam2;version2#
						String[] listMicros=multiplesMicros.split('#')
						printOpen("Se solicita el duplicado de los siguientes micros ${multiplesMicros} ${environment} listMicros.length ${listMicros.length}",EchoLevel.INFO)
						for (String tuplaMicro: listMicros) {
							String[] listMicro=tuplaMicro.split(';')
							cloneToIcp(scriptsPath,environment, listMicro[1], listMicro[2], namespace, listMicro[0], buildCode, true, groupId, listMicro[1], 'false', ignoreStart, ignoreBuild,dontDeployAsync,'CLONE',k8sOrigin,k8sDestination)
						}
					}
				}else {
					 currentBuild.displayName="${type}.${micro}.${version}-${environment}-${command}-${k8sOrigink8sDestination}"
				}
			}
			stageOS('clone-micro-to-ocp'){
				if ('NA'.equals(multiplesMicros)) {
					if (command=='STOP') {
						//Se tiene que parar el micro
						stopMicro(scriptsPath,environment, micro, version, namespace, type, componentPom , k8sDestination)
					}else {
						cloneToIcp(scriptsPath,environment, micro, version, namespace, type, buildCode, true, groupId, componentPom, dontGenerateImages, ignoreStart, ignoreBuild, dontDeployAsync,command,k8sOrigin,k8sDestination)
					}
				}else {
					printOpen("Los multiplesMicros ${multiplesMicros}",EchoLevel.INFO)
				}	
			}
		}catch (err) {
			throw err
		}finally {
			cleanWorkspace()
		}
	}
}


def stopMicro(String path, String environment, String micro, String version, String namespace, String microType, String componentPom, String k8s) {
	def buildCode=1000
	def resultScript=null
	String versionScript=version.replaceAll('\\.',':')
	def componentId=cloneToIcp.getArtifactId(path,micro,namespace,versionScript,microType,componentPom,environment,buildCode,k8s)

	if('ocp'.equals(k8s)){
		pcldDestination="PCLD_MIGRATED"
	} else {
		pcldDestination="PCLD"
	}
	def versionArray=version.split('\\.')
	String nameMicro=micro.toUpperCase()+versionArray[0]
	
	if (componentId!=0) {
		printOpen("${path}/getLastDeployment.sh -u '${GlobalVars.ICP_PRO}/api/publisher/v1/api/application/${pcldDestination}/${namespace}/component/${nameMicro}/deploy/current/environment/${environment}/az/ALL' "+
			" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${componentPom} -E ${environment} -B '${buildCode}' -M ${micro} ",EchoLevel.INFO)
		
		try {
			resultScript = sh(returnStdout: true, script: "${path}/getLastDeployment.sh -u '${GlobalVars.ICP_PRO}/api/publisher/v1/api/application/${pcldDestination}/${namespace}/component/${nameMicro}/deploy/current/environment/${environment}/az/ALL' "+
			" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${componentPom} -E ${environment} -B '${buildCode}' -M ${micro} "
				)
		}catch(e) {
			printOpen("Error ocurrido ${e}",EchoLevel.ERROR)
			generateShError("${path}",false)
		}
		def zoneDistribution='ALL'
		resultScript=resultScript.substring(3)
		def resultScriptJson=jsonUtils.readJsonFromObject(resultScript)
		def resultScriptYaml=objectsParseUtils.parseYamlObject(resultScript)
		//Dar de alta el artefacto en OCP
	
		def lastDeployment=resultScriptYaml
		
		if (lastDeployment!=null) {
			Map absis=lastDeployment["absis"]
			Map absisApps=absis["apps"]
			Map absisAppEnvQualifier=absisApps["envQualifier"]
			
			Map absisApp=absis["app"]
			
			if (absisApp!=null && absisApp["replicas"]!=null) {
				absisApp["replicas"]=0
			}
			
			if (absisAppEnvQualifier!=null) {
				if (absisAppEnvQualifier["stable"]!=null) {
					def stable=absisAppEnvQualifier["stable"]
					stable["replicas"]=0
				}
				if (absisAppEnvQualifier["new"]!=null) {
					def stable=absisAppEnvQualifier["nre"]
					stable["replicas"]=0
				}
			}
			
			printOpen("${path}/deployArtifactICP.sh -h '${GlobalVars.ICP_PRO}' -M ${micro}"+
				" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${componentPom} -E ${environment} -B '${buildCode}' -k ${k8s} "+
				" -i '${objectsParseUtils.toYamlString(resultScriptYaml,true)}'  -c B -Z ${zoneDistribution} -w ${ignoreStart}",EchoLevel.INFO)
			resultScript = sh( returnStdout: true, script: "${path}/deployArtifactICP.sh -h '${GlobalVars.ICP_PRO}' -M ${micro}"+
				" -l '${GlobalVars.URL_ALMMETRICS}' -A ${namespace} -V ${versionScript} -T ${microType} -C ${componentPom} -E ${environment} -B '${buildCode}' -k ${k8s} "+
				" -i '${objectsParseUtils.toYamlString(resultScriptYaml,true)}'  -c B -Z ${zoneDistribution} -w ${ignoreStart}")
			
		}
	}
}
