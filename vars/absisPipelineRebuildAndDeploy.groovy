import groovy.transform.Field
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.ICPAppResources
import com.caixabank.absis3.ICPAppResourcesCatMsv

@Field Map pipelineParams

@Field boolean successPipeline

@Field String icpEnv
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

	icpEnv = params.environmentParam
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
		agent {	node (absisJenkinsAgent(pipelineParams)) }
		options {
			buildDiscarder(logRotator(numToKeepStr: '10'))			
            timestamps()
            timeout(time: 1, unit: 'HOURS')
		}		
		environment {
			GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            ICP_CERT = credentials('icp-absis3-pro-cert')
            ICP_PASS = credentials('icp-absis3-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
			https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
		}
		stages {
			stage("get-app-icp") {
				steps {
					getAppIcpStep()
				}
			}
			stage("restart-app-icp") {
				steps {
					restartAppIcpStep()
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
 * Stage 'getAppIcpStep'
 */
def getAppIcpStep() {
	currentBuild.displayName = "Rebuild_${app} of ${icpEnv} and the namespace ${namespace} and the center ${center}"
	printOpen("Get App ", EchoLevel.ALL)
	valuesDeployed = null
	valuesDeployed=getLastAppInfoICP(icpEnv,app, namespace,center)
	printAppICP(valuesDeployed)
}

/**
 * Stage 'restartAppIcpStep'
 */
def restartAppIcpStep() {
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
	rebuildMicro(valuesDeployed,garType,garApp,app-garApp,icpEnv ,namespace)
	startAndStopApp(valuesDeployed,app,center,namespace,icpEnv,startAndStop,stableOrNew,garApp,jvmConfig,scalingMap)
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
