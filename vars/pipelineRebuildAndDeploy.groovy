import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.CloudAppResources
import com.project.alm.CloudAppResourcesCatMsv

@Field Map pipelineParams

@Field boolean successPipeline

@Field String cloudEnv
@Field String namespace
@Field String app
@Field String garApp
@Field String center
@Field String stableOrNew
@Field String scaleCPUCores
@Field String scaleMemory
@Field String scaleNumInstances
@Field String jvmConfig
@Field String garType
@Field String useCatalogSize
@Field String useProSize
	
@Field String action
@Field boolean startAndStop

@Field Map valuesDeployed

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters

	// las variables que se obtienen como parametro del job no es necesario
	// redefinirlas, se hace por legibilidad del codigo

	successPipeline = true

	cloudEnv = params.environmentParam
	namespace = params.namespaceParam
	app  = params.appnameParam
	garApp  = params.garAppnameParam
	center = params.centerParam
	stableOrNew = params.stableOrNewParam
    scaleCPUCores = "NO"
    scaleMemory = "NO"
    scaleNumInstances = "DEFAULT"
	jvmConfig = ""
	garType = params.garTypeParam
	useCatalogSize = "false"
	useProSize = "false"
		
	action= "START"
	startAndStop = false
	
	valuesDeployed = null
    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
		options {
			buildDiscarder(logRotator(numToKeepStr: '10'))			
            timestamps()
            timeout(time: 1, unit: 'HOURS')
		}		
		environment {
			GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
			https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
		}
		stages {
			stage("get-app-cloud") {
				steps {
					getAppCloudStep()
				}
			}
			stage("restart-app-cloud") {
				steps {
					restartAppCloudStep()
				}
			}
		}
        post {
            success {
                endPipelineSuccessStep()
            }
            failure {
                endPipelineFailureStep()
            }
            always {
                endPipelineAlwaysStep()
            }        
        }
	}
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/**
 * Stage 'getAppCloudStep'
 */
def getAppCloudStep() {
	currentBuild.displayName = "Rebuild_${app} of ${cloudEnv} and the namespace ${namespace} and the center ${center}"
	printOpen("Get App ", EchoLevel.ALL)
	valuesDeployed = null
	valuesDeployed=getLastAppInfoCloud(cloudEnv,app, namespace,center)
	printAppCloud(valuesDeployed)
}

/**
 * Stage 'restartAppCloudStep'
 */
def restartAppCloudStep() {
	printOpen("Restart App ${garApp} ", EchoLevel.ALL)

	def scalingMap = [
		scaleCPUCores:"${scaleCPUCores}",
		scaleMemory:"${scaleMemory}",
		scaleNumInstances:"${scaleNumInstances}"
	]

	if (action=="START") {
			startAndStop=true
	}
	//Compilar i gneerar la nueva imagen docker
	/**
		* Aplicamos el start & stop
		*/
	printOpen("No quieren escalar nada, no hace falta consultar el catalogo", EchoLevel.ALL)
	rebuildMicro(valuesDeployed,garType,garApp,app-garApp,cloudEnv ,namespace)
	startAndStopApp(valuesDeployed,app,center,namespace,cloudEnv,startAndStop,stableOrNew,garApp,jvmConfig,scalingMap)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
   cleanWorkspace()

}

/**
 * Stage 'reloadendPipelineSuccessStepStep'
 */
def endPipelineSuccessStep() {
	successPipeline = true
	printOpen("Is pipeline successful? ${successPipeline}", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
	successPipeline = false
	printOpen("Is pipeline unsuccessful? ${successPipeline}", EchoLevel.ERROR)
}
