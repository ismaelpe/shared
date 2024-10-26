import groovy.transform.Field
import com.project.alm.ArtifactSubType
import com.project.alm.ArtifactType
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.CloudAppResources
import com.project.alm.CloudAppResourcesCatalog
import com.project.alm.PipelineData
import com.project.alm.PipelineStructureType
import com.project.alm.Strings

@Field Map pipelineParams

@Field PipelineData pipelineData

@Field boolean successPipeline = false
@Field boolean initAppPortal = false

@Field String cloudEnv = "${environmentParam}"
@Field String namespace = "${namespaceParam}"
@Field String app  = "${appnameParam}"
@Field String garApp  = "${garAppnameParam}"
@Field String center = "${centerParam}"
@Field String stableOrNew = "${stableOrNewParam}"
@Field String scaleCPUCores = "${scaleCPUCoresParam}"
@Field String scaleMemory = "${scaleMemoryParam}"
@Field String scaleNumInstances = "${scaleNumInstancesParam}"
@Field String jvmConfig = "${jvmConfig}"
@Field String garType = "${garTypeParam}"
@Field String userId = "${userId}"?.trim() ? "${userId}" : "AB3ADM"


@Field Map valuesDeployed = null

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
    successPipeline = false
    initAppPortal = false

    cloudEnv = params.environmentParam
    namespace = params.namespaceParam
    app  = params.appnameParam
    garApp  = params.garAppnameParam
    center = params.centerParam
    stableOrNew = params.stableOrNewParam
    scaleCPUCores = params.scaleCPUCoresParam
    scaleMemory = params.scaleMemoryParam
    scaleNumInstances = params.scaleNumInstancesParam
    jvmConfig = params.jvmConfig
    garType = params.garTypeParam
    userId = params.userId?.trim() ? params.userId : "AB3ADM"

    valuesDeployed = null
    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
        options {
            gitLabConnection('gitlab')
            buildDiscarder(logRotator(numToKeepStr: '10'))
			timestamps()
			timeout(time: 3, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
        environment {
            AppPortal = credentials('IDECUA-JENKINS-USER-TOKEN')
            JNKMSV = credentials('JNKMSV-USER-TOKEN')
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
                    initStep()
                }
            }
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
 * Stage 'initStep'
 */
def initStep() {
    initGlobalVars(pipelineParams)
    pipelineData = new PipelineData(PipelineStructureType.START_STOP, "${env.BUILD_TAG}", env.JOB_NAME, null)
    sendPipelineStartToAppPortal(pipelineData, garType, garApp, app-garApp, cloudEnv.toUpperCase(),userId)
    initAppPortal = true
}

/**
 * Stage 'getAppCloudStep'
 */
def getAppCloudStep() {
    sendStageStartToAppPortal(pipelineData, garType, garApp, "100")
    currentBuild.displayName = "${app} of ${cloudEnv} and the namespace ${namespace} and the center ${center}"
    try {
        printOpen("Get App ", EchoLevel.ALL)
        valuesDeployed = null
        valuesDeployed = getLastAppInfoCloud(cloudEnv, app, namespace, center)
        printAppCloud(valuesDeployed)
        sendStageEndToAppPortal(pipelineData, garType, garApp, "100")
    } catch (Exception e) {
        sendStageEndToAppPortal(pipelineData, garType, garApp, "100", Strings.toHtml(e.getMessage()), null, "error")
        throw e
    }
}

/**
 * Stage 'restartAppCloudStep'
 */
def restartAppCloudStep() {
    sendStageStartToAppPortal(pipelineData, garType, garApp, "200")
	
	def cloudNamespace=null
	boolean isArchProject=false
	
	try {
        printOpen("Restart App ${garApp} ", EchoLevel.ALL)

        def scalingMap = [
            scaleCPUCores:"${scaleCPUCores}",
            scaleMemory:"${scaleMemory}",
            scaleNumInstances:"${scaleNumInstances}"
        ]

		if (namespace=="APP") {
			cloudNamespace=GlobalVars.Cloud_APP_APPS
			isArchProject=false
		}else {
			cloudNamespace=GlobalVars.Cloud_APP_ARCH
			isArchProject=true
		}
        if (  (!"NO".equals(scaleCPUCores) ||  !"NO".equals(scaleMemory) || !"DEFAULT".equals(scaleNumInstances)) && (env.CATALOG_SIZE!=null && "true".equals(env.CATALOG_SIZE))) {
            CloudAppResources cloudResources = null
            def sizeCPU="M"
            def sizeMEM="M"
            def sizeREP="M"

            /**
                * Si ha seleccionado hacer el scaling de algo no tiene que usar los tamaños del catalogo
                */


            if (!"NO".equals(scaleCPUCores)) {
                sizeCPU=scaleCPUCores
            }
            if (!"NO".equals(scaleMemory)) {
                sizeMEM=scaleMemory
            }
            if (!"DEFAULT".equals(scaleNumInstances)) {
                sizeREP=scaleNumInstances
            }

            def cloudEnvResources=cloudEnv
            /**
                * Vamos a usar los tamaños del catalogo de PRO para realizar el deploy sea cual sea el entorno
                */

                printOpen("El entorno del tamaño a usar es el del entorno ${cloudEnv}", EchoLevel.ALL)


            /**
                * Consultamos el tamaño del micro en catalogo o usamos los tamaños definidos para el entorno
                */

                printOpen("No se debe usar los tamaños del catalogo", EchoLevel.ALL)
                cloudResources = getSizesFromCatalog(cloudNamespace,garType,cloudEnvResources,isArchProject,sizeMEM,sizeCPU,sizeREP)

            /**
                * Tenemos que aplicar las restricciones de tamaño para poder ya que sino es PRO ciertos valores no se pueden permitir
                */
            //cloudResources=restrictSizesFromCatalog(cloudResources,cloudEnv)

            if (!"NO".equals(scaleCPUCores)) {
                scaleCPUCores=cloudResources.cpuSize
            }
            if (!"NO".equals(scaleMemory)) {
                scaleMemory=cloudResources.memSize
            }
            if (!"DEFAULT".equals(scaleNumInstances)) {
                scaleNumInstances=cloudResources.replicasSize
            }

            scalingMap = [
                scaleCPUCores:"${scaleCPUCores}",
                scaleMemory:"${scaleMemory}",
                scaleNumInstances:"${scaleNumInstances}"
            ]
            /**
                * Aplicamos el escalado
                */
            printOpen("Se desea escalar, no hace falta consultar el catalogo ${scalingMap} ${cloudResources}", EchoLevel.INFO)
            redimensionApp(valuesDeployed,app,center,namespace,cloudEnv,stableOrNew,garApp,jvmConfig,scalingMap,cloudResources,garType)
			//Se tiene que clonar OCP
			almPipelineStageCloneToOcp(app-garApp+'.0.0',garApp+'-micro',garApp,cloudNamespace,cloudEnv,userId,garType,userId,'false','true','start')
        }
        sendStageEndToAppPortal(pipelineData, garType, garApp, "200", null, cloudEnv )
    } catch (Exception e) {
        sendStageEndToAppPortal(pipelineData, garType, garApp, "200", Strings.toHtml(e.getMessage()), cloudEnv, "error")
        throw e
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
    sendPipelineEndedToAppPortal(initAppPortal, pipelineData, garType, garApp, successPipeline)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("Is pipeline unsuccessful? ${successPipeline}", EchoLevel.ERROR)
}

