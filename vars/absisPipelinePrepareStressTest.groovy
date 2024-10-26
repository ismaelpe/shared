import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.CloudAppResources
import com.project.alm.StressComponent
import com.project.alm.StressEnviromentConfiguration

@Field Map pipelineParams

@Field boolean successPipeline
@Field String cloudEnv
@Field String center
@Field String environmentConfiguration
@Field StressEnviromentConfiguration configuration
@Field Map cloudResourcesArchDeployed
@Field Map cloudResourcesAppDeployed
    
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

	// las variables que se obtienen como parametro del job no es necesario
	// redefinirlas, se hace por legibilidad del codigo

	successPipeline = true

	//Entorno
	cloudEnv = params.environmentParam

	// Centro
	center = params.centerParam

	//JSON con la configuracion
	environmentConfiguration = params.environmentConfiguration
		
	cloudResourcesArchDeployed = [:]
	cloudResourcesAppDeployed = [:]
    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
		options {
			buildDiscarder(logRotator(numToKeepStr: '10'))			
            timestamps()
            timeout(time: 1, unit: 'HOURS')
		}
		//Environment sobre el qual se ejecuta este tipo de job
		environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
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
			stage("get-app-cloud-for-applications") {
				steps {
					getAppCloudForApplicationsStep()
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
	currentBuild.displayName = "Prepare ${env.BUILD_ID} environment ${cloudEnv} and the center ${center} for stress testing"
	configuration = calculateStressConfiguration(environmentConfiguration);
}

/**
 * Stage 'getAppCloudForApplicationsStep'
 */
def getAppCloudForApplicationsStep() {
	List archComponents = configuration.getArchComponentNameList()
	configuration.valuesArchDeployed = getLastAppInfoCloud(cloudEnv,archComponents, "ARCH",center);

	List appComponents = configuration.getAppComponentNameList()
	configuration.valuesAppDeployed = getLastAppInfoCloud(cloudEnv,appComponents, "APP",center);
}

/**
 * Stage 'getAppSizeStep'
 */
def getAppSizeStep() {
	CloudAppResources cloudResources = null
	String garName = null
	configuration.valuesArchDeployed?.each { archApp -> 
		StressComponent component = configuration.getArchStressComponentByCloudName(archApp.key)
		printOpen("Calculating resources for ${archApp.key}", EchoLevel.ALL)
		garName = component?.garName;
		if(component.specificSize) {
			//Para el tamaño de instancias pondremo el tamaño M
			cloudResources = getSizesFromCatalog(GlobalVars.Cloud_APP_ARCH,component.garType,GlobalVars.PRO_ENVIRONMENT.toUpperCase(),true,component.specificSize.memorySize,component.specificSize.cpuSize,'M')
			cloudResources=restrictSizesFromCatalog(cloudResources,cloudEnv)
			cloudResourcesArchDeployed.put(archApp.key, cloudResources)
		}else if(garName) {
			//cloudResources=generateCloudResources(null,envCloud.toUpperCase(),true, garName,null)//String type, String major, String namespace)
			cloudResources=generateCloudResources(null,GlobalVars.PRO_ENVIRONMENT.toUpperCase(),true, garName,null,component.garType,component.version,GlobalVars.Cloud_APP_ARCH)//String type, String major, String namespace)
			cloudResources=restrictSizesFromCatalog(cloudResources,cloudEnv)
			cloudResourcesArchDeployed.put(archApp.key, cloudResources)
		}
	}
	configuration.valuesAppDeployed?.each { app ->
		StressComponent component = configuration.getAppStressComponentByCloudName(app.key)
		printOpen("Calculating resources for ${app.key}", EchoLevel.ALL)
		garName = component?.garName;
		if(component.specificSize) {
			cloudResources = getSizesFromCatalog(GlobalVars.Cloud_APP_APPS,component.garType,GlobalVars.PRO_ENVIRONMENT.toUpperCase(),false,component.specificSize.memorySize,component.specificSize.cpuSize,component.specificSize.replicaSize)
			cloudResources=restrictSizesFromCatalog(cloudResources,cloudEnv)
			cloudResourcesAppDeployed.put(app.key, cloudResources)
		}else if(garName) {
			cloudResources=generateCloudResources(null,GlobalVars.PRO_ENVIRONMENT.toUpperCase(),false, garName,null,component.garType,component.version,GlobalVars.Cloud_APP_APPS)
			cloudResources=restrictSizesFromCatalog(cloudResources,cloudEnv)
			cloudResourcesAppDeployed.put(app.key, cloudResources)
		}
	}			
}

/**
 * Stage 'resizeAppsStep'
 */
def resizeAppsStep() {
	configuration.valuesArchDeployed?.each { archApp ->
		def scalingMap = [
			scaleCPUCores: cloudResourcesArchDeployed.get(archApp.key).cpuSize,
			scaleMemory:cloudResourcesArchDeployed.get(archApp.key).memSize,
			scaleNumInstances:cloudResourcesArchDeployed.get(archApp.key).replicasSize
		]
		StressComponent component = configuration.getArchStressComponentByCloudName(archApp.key)
		String garApp = component?.garName;
		//Add blank space to jvmconfig to avoid errors in docker run
		printOpen("Deploying and resizing ${archApp.key} ${scalingMap} ${cloudResourcesArchDeployed.get(archApp.key).toString()}", EchoLevel.ALL)
		startAndStopApp(archApp.value,archApp.key,center,"ARCH",cloudEnv,true,"STABLE",garApp," ",scalingMap, cloudResourcesArchDeployed.get(archApp.key),component.garType)
	}
	configuration.valuesAppDeployed?.each { app ->
		def scalingMap = [
			scaleCPUCores: cloudResourcesAppDeployed.get(app.key).cpuSize,
			scaleMemory:cloudResourcesAppDeployed.get(app.key).memSize,
			scaleNumInstances:cloudResourcesAppDeployed.get(app.key).replicasSize
		]
		StressComponent component = configuration.getAppStressComponentByCloudName(app.key)
		String garApp = component?.garName;
		//Add blank space to jvmconfig to avoid errors in docker run
		printOpen("Deploying and resizing ${app.key} ${scalingMap} ${cloudResourcesAppDeployed.get(app.key).toString()}", EchoLevel.ALL)
		startAndStopApp(app.value,app.key,center,"APP",cloudEnv,true,"STABLE",garApp," ",scalingMap, cloudResourcesAppDeployed.get(app.key),component.garType)
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
