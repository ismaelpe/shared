import com.project.alm.*
import com.project.alm.LiquibaseStatusType


def getMessageLiquibase(String url) {
	String fileOutput= CopyGlobalLibraryScript('',null,'outputCommand.json')
	def command="curl -L -k --write-out '%{http_code}' -o ${fileOutput} -k -s -X GET ${url} --connect-timeout ${GlobalVars.ACTUATOR_HEALTH_TIMEOUT} "
	String message="Error Internal"
	timeout(GlobalVars.LIQUIBASE_STATUS_RETRY_CYCLE_TIMEOUT) {
		waitUntil(initialRecurrencePeriod: 15000) {
			def responseStatusCode=null
			retry(GlobalVars.ACTUATOR_HEALTH_RETRY_CYCLE_MAX_RETRIES) {
				responseStatusCode= sh(script: command,returnStdout: true)
				sh "cat ${fileOutput}"
			}

			int statusCode
			statusCode = responseStatusCode as Integer

			if (statusCode==200) {
				def contentResponse= sh(script: "cat ${fileOutput}", returnStdout:true )
				message=contentResponse
				return true
			}else {
				return true
			}
		}
	}
	
	return message
}

def getErrorLiquibase(String url) {
	return getMessageLiquibase("${url}/actuator/liquibase/results/execution/error")
}

def getHistoryLiquibase(String url) {
	def resultLiquibase=getMessageLiquibase("${url}/actuator/liquibase/results/execution")
	return resultLiquibase.replaceAll('\\n','<br>').replaceAll('\'',' ')
}

def getExportBBDD(String url) {
	 def reportBBDD=getMessageLiquibase("${url}/actuator/liquibase/database/report")
	 printOpen("${reportBBDD}", EchoLevel.ALL)
	 return "${url}/actuator/liquibase/database/report"
}


def getLiquibaseStatus(String url, int numberMvnCommand) {
	
	String fileOutput= CopyGlobalLibraryScript('',null,'outputCommand.json')
	def command="curl -L -k --write-out '%{http_code}' -o ${fileOutput} -k -s -X GET ${url}/actuator/liquibase/status?numberOfSuccess=${numberMvnCommand} --connect-timeout ${GlobalVars.ACTUATOR_HEALTH_TIMEOUT} "
	def microIsUp=LiquibaseStatusType.ERROR_NOT_FINISHED
	
	timeout(GlobalVars.LIQUIBASE_STATUS_RETRY_CYCLE_TIMEOUT) {
		waitUntil(initialRecurrencePeriod: 15000) {
			def responseStatusCode=null
			retry(GlobalVars.ACTUATOR_HEALTH_RETRY_CYCLE_MAX_RETRIES) {
				responseStatusCode= sh(script: command,returnStdout: true)
				sh "cat ${fileOutput}"
			}

			int statusCode
			statusCode = responseStatusCode as Integer
            printOpen("The status code of the liquibase is ${statusCode}", EchoLevel.ALL)
			if (statusCode==200) {
				microIsUp=LiquibaseStatusType.OK
				return true
			}else if (statusCode==409) {
				microIsUp=LiquibaseStatusType.ERROR_LIQUIBASE
				return true
			}else if (statusCode==408) {
				microIsUp=LiquibaseStatusType.ERROR_NOT_FINISHED
				printOpen("No ha terminado", EchoLevel.ALL)
				return false
			}else if (statusCode==500) {
				microIsUp=LiquibaseStatusType.ERROR_INTERNAL
				return true
			}				
		}
	}	
	return microIsUp
}



def validateMicroIsUpAnReturnError(String url,PomXmlStructure pomXml,int numberOfMvnCommand) {

    boolean microIsUp = validateMicroIsUp(url)
	String fileOutput= CopyGlobalLibraryScript('',null,'outputCommand.json')
    if (microIsUp) {
		def liquibaseStatus=getLiquibaseStatus(url,numberOfMvnCommand)
		//Dependende del resultado tenemos que devolver un mensaje u otro
		if (liquibaseStatus == LiquibaseStatusType.ERROR_INTERNAL) {
			throw new Exception("${GlobalVars.LIQUIBASE_INTERNAL_ERROR}")
		}else if (liquibaseStatus == LiquibaseStatusType.ERROR_LIQUIBASE) {
			throw new Exception("${GlobalVars.LIQUIBASE_ORACLE_ERROR}"+""+getErrorLiquibase(url)+"")
		}else if (liquibaseStatus == LiquibaseStatusType.ERROR_LIQUIBASE) {
			throw new Exception("${GlobalVars.LIQUIBASE_NOT_FINISHED_ERROR}")
		}else {
			return true
		}

    }else throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")


    printOpen("El micro esta UP/DOWN ${microIsUp}", EchoLevel.ALL)

    return microIsUp
}

def generateCloudResources(String environment, boolean isArchProject, String artifactId ) {
	CloudAppResources cloudResources=new CloudAppResources()
	cloudResources.environment=environment
	cloudResources.isArchProject=isArchProject	
		
	cloudResources.replicasSize="S"	
	cloudResources.memSize="S"		
	cloudResources.cpuSize="S"
	
	return cloudResources
}

private boolean isDatasourceMultitenant(Map datasourceMap) {
	if (datasourceMap.containsKey('alm')) {
		Map dataosourceMap1=datasourceMap.get('alm')
		return (dataosourceMap1.containsKey('datasource') && dataosourceMap1.get('datasource').containsKey('enable'));
	}
	
	return false;
}


private Map getDataSourceUrlMultiTenant(def map) {
/**
alm:
  datasource:
    enable: true
    connections:
      arqprudb:
        url: ${vcap.services.cbk-apps-demo-arqpru-arqprudb-database.credentials.uri}
 
 * 	
 */
	Map mapOutput=new HashMap()
	if (map.containsKey('datasource') && map.get('datasource').containsKey('connections')) {
		def connectionsKeys=map.get('datasource').get('connections').keySet()
		connectionsKeys.each {  
			def connectionValue=map.get('datasource').get('connections').get(it)
			if (connectionValue.containsKey('url')) {
				mapOutput.put(it, connectionValue.get('url'))
			}
		}
	}
	return mapOutput
	
}
private Map getDataSourceUrlNonMultiTenant(def map) {
/**
spring:
  datasource:
    url: ${vcap.services.cbk-apps-infraestructura-cgnofi-database.credentials.uri:uri}
*
*/
	if (map.containsKey('spring') && map.get('spring').containsKey('datasource')) {
	   return ['basic':map.get('spring').get('datasource').get('url')]
	}
}
def readYamlFromSystem() {
	
	def tmpDataSource = "./${GlobalVars.TMP_FILE_GENERATED_DATASOURCE}"
	
	
	def exists = fileExists tmpDataSource
	
    if (exists) {
		def fileApp = readYaml file: tmpDataSource
		printOpen("The fileDataIs ${fileApp}", EchoLevel.ALL)
		printOpen("Puede ser nuevo formato o el antiguo", EchoLevel.ALL)
		def multi=isDatasourceMultitenant(fileApp)
		printOpen("es multi data source ${multi}", EchoLevel.ALL)
		if (multi) {
			return getDataSourceUrlMultiTenant(fileApp.get('alm'))
		}else {
			return getDataSourceUrlNonMultiTenant(fileApp)
		}
	}else {
		printOpen("No existe... la info esta en el yaml aplicativo?", EchoLevel.ALL)
	}
	return []
	
}

def call(PomXmlStructure artifactPom, PipelineData pipeline, String group, String artifact, String version) {
	return 	deployScriptToKubernetes(artifactPom, pipeline, group, artifact, version, false, false)
}


def getUrlForTheConnection(String connection, def mapUrls) {
	String url=""
	if (mapUrls!=null && mapUrls.isEmpty()) {
		return null
	}else {
		printOpen("El map no esta vacio ${mapUrls.isEmpty()}", EchoLevel.ALL)
	}
	if (mapUrls.containsKey('basic')) {
		printOpen("Es datasource basico", EchoLevel.ALL)
		url=mapUrls.get('basic')
	}else {
		def connectionsKeys=mapUrls.keySet()
		connectionsKeys.each {
			if (connection.indexOf(it)>-1) {
				url=mapUrls.get(it)
			}
		}
	}
	if (!"".equals(url) && url.indexOf('vcap.services.')==-1) {
		return url
	}else {
		return null
	}
}



def call(PomXmlStructure artifactPom, PipelineData pipeline, String group, String artifact, String version, boolean isHistory, boolean isGenerateChangeLog) {

    long wholeCallDuration
    long wholeCallStartMillis = new Date().getTime()
	boolean isReleaseLiquibase = true
	int numberOfMvnCommand=1
	
	if (isHistory==false && isGenerateChangeLog==false && isReleaseLiquibase==true) {
		numberOfMvnCommand=2
	}

    KpiAlmEvent kpiAlmEvent =
        new KpiAlmEvent(
            artifactPom, pipeline,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.Cloud_DEPLOY_SCRIPT)
	
	def messageDeploy=""
	String environmentDest=pipeline.bmxStructure.environment
	CloudDeployStructure deployStructure=new CloudDeployStructure('cxb-ab3cor','cxb-ab3app',environmentDest)
	
	if (pipeline.deployStructure==null) {
		pipeline.deployStructure=deployStructure
	}
	
	String componentName=artifactPom.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name))+"bbdd"
	String componentId=generateArtifactInCloud(artifactPom, pipeline, generateCloudResources(environmentDest,false,componentName),true )
	//Pasamos a minusculas el nombre del componente
	componentName=componentName.toLowerCase()
	
	checkCloudAvailability(artifactPom,pipeline,deployStructure.envCloud,"DEPLOY")
	
	String newManifest = ''//Por ejemplo le pasamos centro 1
	//FIXME: Porque usamos un deployStructure de BMX en Cloud?
    if (pipeline.garArtifactType == GarAppType.DATA_SERVICE)  generateDataSourceFile(artifactPom,pipeline)
	newManifest = generateManifest(artifactPom, pipeline.bmxStructure.getDeployStructure(GlobalVars.BMX_CD1),pipeline,true)
	
	String manifestContent=sh(returnStdout: true, script: "cat ${newManifest}")
	printOpen(" ${deployStructure.initSecretsMemoryFromYaml(manifestContent)}", EchoLevel.ALL)
	
	printOpen("Los secrets son los siguientes ${deployStructure.secrets}", EchoLevel.ALL)
	def tmpDataSource = "${GlobalVars.TMP_FILE_GENERATED_DATASOURCE}"
	def urlDataSourceMap=readYamlFromSystem()
	
	printOpen("Las Urls son las siguientes ${urlDataSourceMap}", EchoLevel.ALL)
	
	def exists = fileExists tmpDataSource
	
	if (exists) {
		sh "cat './${GlobalVars.TMP_FILE_GENERATED_DATASOURCE}'"
		sh "rm ${tmpDataSource}"
	}else {
		printOpen("No existe el fichero temporal", EchoLevel.ALL)
	}
	
	printOpen("Siempre vamos a desbloquear... es obligatorio, no podemos tener dos pods de la misma app a la vez", EchoLevel.ALL)
	//Build Image
	CloudApiResponse response=null
	
	String imageCloud=GlobalVars.Cloud_LIQUIBASE_IMAGE
	String version_Image=GlobalVars.Cloud_LIQUIBASE_IMAGE_VERSION


	String actualDateString = Utilities.getActualDate("yyyyMMddHH24mmss")
	
	if (deployStructure.secrets!=null && deployStructure.secrets.size()>0) {		
		
		for (int i=0;i<deployStructure.secrets.size();i++) {
			
			def x = deployStructure.secrets.getAt(i)
		
			if (x.contains('database') && !x.contains('ro-database')) {
				
				printOpen("Deploying to the ${x} database. ", EchoLevel.ALL)
				
				def url=getUrlForTheConnection(x,urlDataSourceMap)
				def secret_pool_owner
				def env_secret_pool_owner=""
				if (url!=null) {
					//Tenemos la url en el map
					//vcap.services.pool-database.credentials.uri
					printOpen("La url es de ${url} tenemos que usar esta url no el secret", EchoLevel.ALL)
					env_secret_pool_owner="      - name: vcap.services.pool-database.credentials.uri\n        value: \"${url}\"\n"
					secret_pool_owner=""
				}else {
					env_secret_pool_owner=""
					secret_pool_owner="      - name: ${x}\n        alias: pool-database\n"					
				}
				printOpen("El secret es de ${secret_pool_owner}", EchoLevel.ALL)
				printOpen("La variable de entorno es ${env_secret_pool_owner}", EchoLevel.ALL)
				pipeline.componentId=componentId
				
				String centroLiquibase=getCloudActiveSite(deployStructure.envCloud)
				
				//Deploy Image
				def bodyDeploy=[
					az: "AZ${centroLiquibase}",
					environment: "${deployStructure.envCloud.toUpperCase()}",
					values: "deployment:\n  readinessProbe:\n    periodSeconds: 60\n    timeoutSeconds: 50\n    failureThreshold: 5\n  livenessProbe:\n    periodSeconds: 60\n    timeoutSeconds: 50\n    failureThreshold: 5\nlocal:\n  app:\n    ingress:\n      defineBodySize: true\n      maxBodySize: 30m\n      enabled: false\n      deploymentArea: alm\n      alm:\n        enabled: false\n      mtls:\n        enabled: true\n        needsSystemRoute: true\n        needsSpecialVerifyDepth: false\n    envVars:\n      - name: ALM_Cloud_ENVIRONMENT\n        value: ${environmentDest.toLowerCase()}\n      - name: ALM_APP_ID\n        value: arqrunbbdd\n      - name: ALM_CENTER_ID\n        value: 1\n      - name: RELEASE_LIQUIBASE\n        value: ${isReleaseLiquibase}\n      - name: HISTORY_LIQUIBASE\n        value: ${isHistory}\n      - name: GENERATE_CHANGELOG_LIQUIBASE\n        value: ${isGenerateChangeLog}\n      - name: ALM_ENVIRONMENT\n        value: ${environmentDest.toUpperCase()}\n      - name: JAVA_OPTS\n        value: '-Dspring.cloud.config.failFast=false'\n      - name: nonProxyHosts\n        value: '*.cxb-pasdev-tst|*.cxb-ab3app-${environmentDest.toLowerCase()}|*.cxb-ab3cor-${environmentDest.toLowerCase()}'\n      - name: http.additionalNonProxyHosts\n        value: 'cxb-pasdev-${environmentDest.toLowerCase()},cxb-ab3app-dev,cxb-ab3cor-${environmentDest.toLowerCase()}'\n      - name: NO_PROXY\n        value: cxb-ab3cor-${environmentDest.toLowerCase()}\n      - name: ACTUAL_DATE\n        value: ${actualDateString}\n      - name: ARTIFACT_ID\n        value: ${artifact}\n${env_secret_pool_owner}      - name: VERSION_ARTIFACT\n        value: ${version}\n      - name: GROUP_ID\n        value: ${group}\n      - name: SPRING_PROFILES_ACTIVE\n        value: cloud,${environmentDest.toLowerCase()},cloud\n    secrets:\n      - name: ${x}\n      - name: ${x}-owner\n    secrets_alias:\n${secret_pool_owner}      - name: ${x}-owner\n        alias: pool-owner-database\nalm:\n  app:\n    loggingElkStack: alm0\n    replicas: 1\n    instance: ${componentName}\n    name: ${componentName}\n  resources:\n    requests:\n      memory: 1024Mi\n      cpu: 50m\n    limits:\n       memory: 2048Mi\n       cpu: 500m\n  apps:\n    envQualifier:\n      new:\n        id: ${componentName}-b\n        colour: G\n        image: ${imageCloud}:${version_Image}\n        version: ${version}\n        stable: false\n        new: false\n        replicas: 1\n        requests_memory: 2500Mi\n        requests_cpu: 25m\n        limits_memory: 2500Mi\n        limits_cpu: 500m\n  services:\n    envQualifier:\n      stable:\n        id: ${componentName}\n        targetColour: G\n"
				]
				
				validateSecrets([x,"${x}-owner"], deployStructure.envCloud.toUpperCase(), artifactPom, pipeline)
				printOpen("Procedemos a validar estado para saber si tenemos que eliminar o no el micro actual", EchoLevel.ALL)
				//Deberiamos validar primero si existe, no?
				Cloudk8sComponentInfoMult cloudActualStatusInfo=getActualDeploymentStatusOnCloud(artifactPom,pipeline,deployStructure,"AZ${centroLiquibase}")
				printOpen("El estado es ${cloudActualStatusInfo.toString()}", EchoLevel.ALL)
				if (cloudActualStatusInfo.getPodList()!=null && cloudActualStatusInfo.getPodList().size()>0) {
					printOpen("Deleting the element", EchoLevel.ALL)
					response=sendRequestToCloudApi("v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",bodyDeploy,"DELETE","${artifactPom.getCloudAppName()}","v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",false,true, pipeline, artifactPom)
				}else{
					printOpen("No hay micro a borrar", EchoLevel.ALL)
				}
				
				response=sendRequestToCloudApi("v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",bodyDeploy,"POST","${artifactPom.getCloudAppName()}","v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",true,true, pipeline, artifactPom)
			
				if (response.statusCode<200 || response.statusCode>300) {

                    long wholeCallEndMillis = new Date().getTime()
                    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                    kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

                    createMaximoAndThrow.cloudDeployException(pipeline, artifactPom, response)

				}
								
				boolean isReady=waitCloudDeploymentReady(artifactPom,pipeline,deployStructure,'B',"AZ${centroLiquibase}")
				
				printOpen("Esta ready el micro? ${isReady}", EchoLevel.ALL)
				
				if (!isReady) {

				    response=sendRequestToCloudApi("v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",bodyDeploy,"DELETE","${artifactPom.getCloudAppName()}","v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",false,true, pipeline, artifactPom)

                    long wholeCallEndMillis = new Date().getTime()
                    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                    kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                    throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")

				} else {
					//MicroIsUp
					String microUrl=deployStructure.getUrlActuatorPrefixTesting() + deployStructure.getUrlSuffixTesting("AZ${centroLiquibase}")+"/"
					
					if (artifactPom.isArchProject()) {
						microUrl=microUrl+"/arch-service/"
					}
					
					microUrl=microUrl+componentName
					
					def microIsUpAndOk=false
					
					try {
						microIsUpAndOk=validateMicroIsUpAnReturnError(microUrl, artifactPom,numberOfMvnCommand)
					}catch(Exception e) {
						printOpen("El micro no esta correcto ${microIsUpAndOk}", EchoLevel.ALL)
						printOpen("Se debe borrar el pod.. no tiene sentido que exista", EchoLevel.ALL)
						response=sendRequestToCloudApi("v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",bodyDeploy,"DELETE","${artifactPom.getCloudAppName()}","v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",false,true, pipeline, artifactPom)
						if (response.statusCode<200 || response.statusCode>300) {
							printOpen("Pod NO eliminado hemos tenido algun tipo de problema", EchoLevel.ALL)
						}else {
							printOpen("Pod eliminado", EchoLevel.ALL)
						}
						throw e
					}
					
					if (microIsUpAndOk) {

						messageDeploy=GlobalVars.LIQUIBASE_MESSAGE_UPDATE_OK
						
						if (isHistory || isGenerateChangeLog) {
							printOpen("Es history o generacion de change Log", EchoLevel.ALL)
							if (isHistory) {
								messageDeploy="<p><pre>Puede consultar los changelogs aplicados:\n"+"<br>"+getHistoryLiquibase(microUrl)+" </pre></p>"
								
							}else {
								messageDeploy="<p><pre>Puede consultar el export <a href=\""+getExportBBDD(microUrl)+"\">en el link</a> recuerde que el link caduca.</pre></p>"
							}
							
						}else{
							messageDeploy="<p><pre>Logs de la ejecucion:\n"+"<br>"+getHistoryLiquibase(microUrl)+" </pre></p>"
							printOpen("Se procede a revisar el estado de liquibase ", EchoLevel.ALL)
							printOpen("El resultado es de la ejecucion es de  ${messageDeploy}", EchoLevel.ALL)
							printOpen("Se debe borrar el pod.. no tiene sentido que exista", EchoLevel.ALL)
							
							response=sendRequestToCloudApi("v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",bodyDeploy,"DELETE","${artifactPom.getCloudAppName()}","v1/application/PCLD/${artifactPom.getCloudAppName()}/component/${componentId}/deploy",false,true, pipeline, artifactPom)
							 
							if (response.statusCode<200 || response.statusCode>300) {
								printOpen("Pod NO eliminado hemos tenido algun tipo de problema", EchoLevel.ALL)
							}else {
								printOpen("Pod eliminado", EchoLevel.ALL)
							}
						}
						
						
					} else {

                        long wholeCallEndMillis = new Date().getTime()
                        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                        kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

						throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")

					}
					
				}
			}
		}
	}

    long wholeCallEndMillis = new Date().getTime()
    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

    kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))

	return messageDeploy
}
