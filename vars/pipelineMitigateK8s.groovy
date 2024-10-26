import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.CloudAppResources
import com.project.alm.CloudAppResourcesCatalog
import com.project.alm.PipelineData
import com.project.alm.PipelineStructureType
import com.project.alm.Strings
import com.project.alm.Cloudk8sComponentInfoMult

@Field Map pipelineParams

@Field String cloudEnv = "${environmentParam}"
@Field String center = "${centerParam}"
@Field String userId = "${userId}"?.trim() ? "${userId}" : "AB3ADM"
@Field String k8sOrigink8sDestination="${k8sOrigink8sDestinationParam}"
@Field String loggerLevel

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters
	loggerLevel = params.loggerLevel
	
	pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
        options {
            gitLabConnection('gitlab')
            buildDiscarder(logRotator(numToKeepStr: '10'))
			timestamps()
			timeout(time: 1, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
        environment {
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
            https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
        }
        stages {
			stage("init"){
				steps {
					script{
						currentBuild.displayName="${k8sOrigink8sDestination}-${cloudEnv}-${center}"
						initGlobalVars([loggerLevel: loggerLevel])
					}
				}
			}
            stage("enable-alive-cloud"){
				when {
					expression { "ocp->cloud".equals(k8sOrigink8sDestination) }
				}
                steps {
					script{
						enableDisableAliveCloud(cloudEnv,center,'enable')
					}
                }
            }
			stage("enable-alive-ocp"){
				when {
					expression { "cloud->ocp".equals(k8sOrigink8sDestination) }
				}
				steps {
					script{
						enableDisableAliveOcp(cloudEnv,center,'enable')
					}
				}
			}
            stage("disable-alive-cloud"){
				when {
					expression { "cloud->ocp".equals(k8sOrigink8sDestination)  }
				}
                steps {
					script{
						enableDisableAliveCloud(cloudEnv,center,'disable')
					}                    
                }
            }
            stage("disable-alive-ocp"){
				when {
					expression { "ocp->cloud".equals(k8sOrigink8sDestination)  }
				}
                steps {
					script{
						enableDisableAliveOcp(cloudEnv,center,'disable')
					}                    
                }
            }
        }
    }
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */


def enableDisableAliveOcp(def environment, def center, def action) {
	
	printOpen("enableDisableAliveOcp environment: ${environment}  center: ${center} action: ${action}",EchoLevel.INFO)

	def numReplicas=0
	if ('disable'.equals(action)) {
		numReplicas=0
	}else {
		numReplicas=1
	}
	
	def body = [
		az: "${center}",
		environment: "${environment.toUpperCase()}",
		values:"deployment:\n  readinessProbe:\n    initialDelaySeconds: 50\n    periodSeconds: 50\n    timeoutSeconds: 25\n    failureThreshold: 10\n  livenessProbe:\n    initialDelaySeconds: 50\n  "+
			"  periodSeconds: 50\n    timeoutSeconds: 25\n    failureThreshold: 10\nlocal:\n  app:\n    enableNonMtls: true\n    gateway:\n      enabled: false\n      gatewayWildCard: gw-wildcard-alm-nsn\n   "+ " ingress:\n      enabled: true\n      deploymentArea: alm\n      defineBodySize: true\n"+
			"      maxBodySize: 30m\n      defineTimeout: true\n      sendTimeout: 100\n      readTimeout: 100\n      connectTimeout: 15\n      alm:\n        enabled: true\n      mtls:\n        enabled: true\n      "+
			"  needsSystemRoute: true\n        route: null\n        needsSpecialVerifyDepth: false\n        verifyDepth: 2\n    envVars:\n    - name: NO_PROXY\n      value: cxb-ab3cor-dev\nalm:\n  app:\n   "+ " loggingElkStack: alm0\n    replicas: ${numReplicas}\n    instance: alive1\n    name: alive\n    type: ARQ.MIA\n   "+
			" stoppableByPlatform: true\n  resources:\n    requests:\n      memory: 200Mi\n      cpu: 16m\n    limits:\n      memory: 300Mi\n      cpu: 250m\n  apps:\n    envQualifier:\n      stable:\n        id:"+ " alive-b\n        colour: B\n        image: docker-registry.cloud.project.com/containers/ab3app/alive2:2.1.0\n        version: 1.31.0-SNAPSHOT\n        stable: false\n    "+
			"    new: false\n        replicas: ${numReplicas}\n        readinessProbePath: /actuator/health\n        livenessProbePath: /actuator/health\n        envVars:\n          SPRING_PROFILES_ACTIVE: cloud\n       "+ " requests_memory: 200Mi\n        requests_cpu: 16m\n        limits_memory: 300Mi\n        limits_cpu: 350m\n  services:\n    envQualifier:\n      stable:\n        id: "+
			"ab3app-ab3alive\n        targetColour: B\n  istio:\n    side"+
			"car:\n      size:\n        enabled: true\n        requests:\n          memory: 400Mi\n          cpu: 5m\n        limits:\n          memory: 400Mi\n          cpu: 300m"
		]	
			
	def	response = null
	printOpen("El body para el deploy es el siguiente ${body}",EchoLevel.INFO)
	//https://publisher-ssp-cldalm.pro.ap.intranet.cloud.digitalscale.es/api/publisher/v1/api/application/PCLD_MIGRATED/AB3APP/component/ALIVE/deploy
	response = sendRequestToCloudApi("v1/api/application/PCLD_MIGRATED/AB3APP/component/ALIVE/deploy",body,"POST","AB3APP","v1/api/application/PCLD_MIGRATED/AB3APP/component/ALIVE/deploy",true,true)
	if (response.statusCode < 300) {
		printOpen("Stop/Start correcto del micro",EchoLevel.INFO)
		
		timeout(GlobalVars.TIMEOUT_MAX_Cloud) {
			waitUntil(initialRecurrencePeriod: 15000) {
				response = sendRequestToCloudApi("v1/api/application/PCLD_MIGRATED/AB3APP/component/ALIVE/environment/${environment.toUpperCase()}/availabilityzone/${center}/status",null,"GET","AB3APP","",false,false)
		
				if (response.statusCode>=200 && response.statusCode<300) {
					printOpen("Revisamos el ${response.body}",EchoLevel.INFO)
		
							
					Cloudk8sComponentInfoMult k8sComponentInfo = generateActualDeploymentOnCloud(response.body)
					printOpen("result ${k8sComponentInfo.isTheDeploymentReady()}", EchoLevel.DEBUG)
							
					return k8sComponentInfo.isTheDeploymentReady()
				} else {
					return true
				}
			}
		}
		
	}else{
		printOpen("Status Code ${response.statusCode}",EchoLevel.INFO)
		throw new Exception("Error en el deploy")
	}
}

def enableDisableAliveCloud(def environment, def center, def action) {
	printOpen("enableDisableAliveCloud environment: ${environment}  center: ${center} action: ${action}",EchoLevel.INFO)	
	
	def body = [
		az: "${center}",
		environment: "${environment.toUpperCase()}",
		values:"init:\n  image: docker-registry.cloud.project.com/catalog/docker-init-setup:2.5.0\nglobal:\n  image:\n    repository: docker-registry.cloud.project.com/containers/ab3cor/ab3cor\n    tag: 0.0.7\ningressAlive:\n  enabled: true\n  ingressClass: alm\n  hosts:\n    - d"+
		"omain: 'alive.${environment.toLowerCase()}.ext.srv.project.com'\n      serviceName: ''\n      servicePort: 80\n      tlsCertificate:\n        enabled: true\n        secretName: crt-ext-srv-project-com\n    - domain: 'alive.${environment.toLowerCase()}.in"+
		"t.srv.project.com'\n      serviceName: ''\n      servicePort: 80\n      tlsCertificate:\n        enabled: true\n        secretName: crt-int-srv-project-com"
		]
	def	response = null
	printOpen("El body para el deploy es el siguiente ${body}",EchoLevel.INFO)
	def VERB='POST'
	if ('disable'.equals(action)) {
		VERB='DELETE'
	}else {
		VERB='POST'
	}

	response = sendRequestToCloudApi("v1/api/application/PCLD/AB3APP/component/AB3ALIVE/deploy",body,"${VERB}","AB3APP","",false,false)
	
	if (response.statusCode < 300 ) {
		printOpen("Stop/Start correcto del micro",EchoLevel.INFO)
		printOpen("Esperaremos el arranque",EchoLevel.INFO)
		timeout(GlobalVars.TIMEOUT_MAX_Cloud) {
			waitUntil(initialRecurrencePeriod: 15000) {
				response = sendRequestToCloudApi("v1/api/application/PCLD/AB3APP/component/AB3ALIVE/environment/${environment.toUpperCase()}/availabilityzone/${center}/status",null,"GET","AB3APP","",false,false)
		
				if (response.statusCode>=200 && response.statusCode<300) {
					printOpen("Revisamos el ${response.body}",EchoLevel.INFO)
		
							
					Cloudk8sComponentInfoMult k8sComponentInfo = generateActualDeploymentOnCloud(response.body)
					printOpen("result ${k8sComponentInfo.isTheDeploymentReady()}", EchoLevel.DEBUG)
							
					return k8sComponentInfo.isTheDeploymentReady()	
				} else {
					return true
				}
			}
		}
		
		
	}else{
		printOpen("Status Code ${response.statusCode}",EchoLevel.INFO)
		throw new Exception("Error en el deploy")
	}
	
}
