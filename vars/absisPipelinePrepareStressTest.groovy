import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.ICPAppResources
import com.project.alm.StressComponent
import com.project.alm.StressEnviromentConfiguration

@Field Map pipelineParams

@Field boolean successPipeline
@Field String icpEnv
@Field String center
@Field String environmentConfiguration
@Field StressEnviromentConfiguration configuration
@Field Map icpResourcesArchDeployed
@Field Map icpResourcesAppDeployed
    
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

	// las variables que se obtienen como parametro del job no es necesario
	// redefinirlas, se hace por legibilidad del codigo

	successPipeline = true

	//Entorno
	icpEnv = params.environmentParam

	// Centro
	center = params.centerParam

	//JSON con la configuracion
	environmentConfiguration = params.environmentConfiguration
		
	icpResourcesArchDeployed = [:]
	icpResourcesAppDeployed = [:]
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
		options {
			buildDiscarder(logRotator(numToKeepStr: '10'))			
            timestamps()
            timeout(time: 1, unit: 'HOURS')
		}
		//Environment sobre el qual se ejecuta este tipo de job
		environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            ICP_CERT = credentials('icp-alm-pro-cert')
            ICP_PASS = credentials('icp-alm-pro-cert-passwd')
			http_proxy = "${GlobalVars.proxyCaixa}"
			https_proxy = "${GlobalVars.proxyCaixa}"
			proxyHost = "${GlobalVars.proxyCaixaHost}"
			proxyPort = "${GlobalVars.proxyCaixaPort}"
		}
		stages {
			stage("get-json-information") {
				steps {
					getJsonInformationStep()
				}
			}
			stage("get-app-icp-for-applications") {
				steps {
					getAppIcpForApplicationsStep()
				}
			}
			stage("get-app-size") {
				steps {
					getAppSizeStep()
				}
			}
			stage("resize-apps"){
				steps {
					resizeAppsStep()
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
 * Stage 'getJsonInformationStep'
 */
def getJsonInformationStep() {
	currentBuild.displayName = "Prepare ${env.BUILD_ID} environment ${icpEnv} and the center ${center} for stress testing"
	configuration = calculateStressConfiguration(environmentConfiguration);
}

/**
 * Stage 'getAppIcpForApplicationsStep'
 */
def getAppIcpForApplicationsStep() {
	List archComponents = configuration.getArchComponentNameList()
	configuration.valuesArchDeployed = getLastAppInfoICP(icpEnv,archComponents, "ARCH",center);

	List appComponents = configuration.getAppComponentNameList()
	configuration.valuesAppDeployed = getLastAppInfoICP(icpEnv,appComponents, "APP",center);
}

/**
 * Stage 'getAppSizeStep'
 */
def getAppSizeStep() {
	ICPAppResources icpResources = null
	String garName = null
	configuration.valuesArchDeployed?.each { archApp -> 
		StressComponent component = configuration.getArchStressComponentByIcpName(archApp.key)
		printOpen("Calculating resources for ${archApp.key}", EchoLevel.ALL)
		garName = component?.garName;
		if(component.specificSize) {
			//Para el tamaño de instancias pondremo el tamaño M
			icpResources = getSizesFromCatalog(GlobalVars.ICP_APP_ARCH,component.garType,GlobalVars.PRO_ENVIRONMENT.toUpperCase(),true,component.specificSize.memorySize,component.specificSize.cpuSize,'M')
			icpResources=restrictSizesFromCatalog(icpResources,icpEnv)
			icpResourcesArchDeployed.put(archApp.key, icpResources)
		}else if(garName) {
			//icpResources=generateICPResources(null,envICP.toUpperCase(),true, garName,null)//String type, String major, String namespace)
			icpResources=generateICPResources(null,GlobalVars.PRO_ENVIRONMENT.toUpperCase(),true, garName,null,component.garType,component.version,GlobalVars.ICP_APP_ARCH)//String type, String major, String namespace)
			icpResources=restrictSizesFromCatalog(icpResources,icpEnv)
			icpResourcesArchDeployed.put(archApp.key, icpResources)
		}
	}
	configuration.valuesAppDeployed?.each { app ->
		StressComponent component = configuration.getAppStressComponentByIcpName(app.key)
		printOpen("Calculating resources for ${app.key}", EchoLevel.ALL)
		garName = component?.garName;
		if(component.specificSize) {
			icpResources = getSizesFromCatalog(GlobalVars.ICP_APP_APPS,component.garType,GlobalVars.PRO_ENVIRONMENT.toUpperCase(),false,component.specificSize.memorySize,component.specificSize.cpuSize,component.specificSize.replicaSize)
			icpResources=restrictSizesFromCatalog(icpResources,icpEnv)
			icpResourcesAppDeployed.put(app.key, icpResources)
		}else if(garName) {
			icpResources=generateICPResources(null,GlobalVars.PRO_ENVIRONMENT.toUpperCase(),false, garName,null,component.garType,component.version,GlobalVars.ICP_APP_APPS)
			icpResources=restrictSizesFromCatalog(icpResources,icpEnv)
			icpResourcesAppDeployed.put(app.key, icpResources)
		}
	}			
}

/**
 * Stage 'resizeAppsStep'
 */
def resizeAppsStep() {
	configuration.valuesArchDeployed?.each { archApp ->
		def scalingMap = [
			scaleCPUCores: icpResourcesArchDeployed.get(archApp.key).cpuSize,
			scaleMemory:icpResourcesArchDeployed.get(archApp.key).memSize,
			scaleNumInstances:icpResourcesArchDeployed.get(archApp.key).replicasSize
		]
		StressComponent component = configuration.getArchStressComponentByIcpName(archApp.key)
		String garApp = component?.garName;
		//Add blank space to jvmconfig to avoid errors in docker run
		printOpen("Deploying and resizing ${archApp.key} ${scalingMap} ${icpResourcesArchDeployed.get(archApp.key).toString()}", EchoLevel.ALL)
		startAndStopApp(archApp.value,archApp.key,center,"ARCH",icpEnv,true,"STABLE",garApp," ",scalingMap, icpResourcesArchDeployed.get(archApp.key),component.garType)
	}
	configuration.valuesAppDeployed?.each { app ->
		def scalingMap = [
			scaleCPUCores: icpResourcesAppDeployed.get(app.key).cpuSize,
			scaleMemory:icpResourcesAppDeployed.get(app.key).memSize,
			scaleNumInstances:icpResourcesAppDeployed.get(app.key).replicasSize
		]
		StressComponent component = configuration.getAppStressComponentByIcpName(app.key)
		String garApp = component?.garName;
		//Add blank space to jvmconfig to avoid errors in docker run
		printOpen("Deploying and resizing ${app.key} ${scalingMap} ${icpResourcesAppDeployed.get(app.key).toString()}", EchoLevel.ALL)
		startAndStopApp(app.value,app.key,center,"APP",icpEnv,true,"STABLE",garApp," ",scalingMap, icpResourcesAppDeployed.get(app.key),component.garType)
	}
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
	cleanWorkspace()
}

/**
 * Stage 'endPipelineSuccessStep'
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
