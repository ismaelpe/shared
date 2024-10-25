import groovy.transform.Field
import com.caixabank.absis3.ArtifactSubType
import com.caixabank.absis3.ArtifactType
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.ICPAppResources
import com.caixabank.absis3.ICPAppResourcesCatMsv
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PipelineStructureType
import com.caixabank.absis3.Strings

@Field Map pipelineParams

@Field PipelineData pipelineData

@Field boolean successPipeline = false
@Field boolean initGpl = false

@Field String icpEnv = "${environmentParam}"
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
@Field String useCatalogSize = "${useCatalogSizeParam}"
@Field String useProSize = "${useProSizeParam}"
@Field String userId = "${userId}"?.trim() ? "${userId}" : "AB3ADM"

@Field String action= "${actionParam}"
@Field boolean startAndStop = false

@Field Map valuesDeployed = null

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
    successPipeline = false
    initGpl = false

    icpEnv = params.environmentParam
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
    useCatalogSize = params.useCatalogSizeParam
    useProSize = params.useProSizeParam
    userId = params.userId?.trim() ? params.userId : "AB3ADM"
    action= params.actionParam

    startAndStop = false

    valuesDeployed = null
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            gitLabConnection('gitlab')
            buildDiscarder(logRotator(numToKeepStr: '10'))
			timestamps()
			timeout(time: 3, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            JNKMSV = credentials('JNKMSV-USER-TOKEN')
            ICP_CERT = credentials('icp-absis3-pro-cert')
            ICP_PASS = credentials('icp-absis3-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
        }
        stages {
            stage("init"){
                steps {
                    initStep()
                }
            }
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
 * Stage 'initStep'
 */
def initStep() {
    initGlobalVars(pipelineParams)
    pipelineData = new PipelineData(PipelineStructureType.START_STOP, "${env.BUILD_TAG}", env.JOB_NAME, null)
    def startData=sendPipelineStartToGPL(pipelineData, garType, garApp, app-garApp, icpEnv.toUpperCase(),userId)
	if (startData==null) {
		printOpen("Elemento no generado en idegpl, marcamos el envio a GPL como false ", EchoLevel.INFO)
		initGpl = false
	}else {
		initGpl = true
	}
    
}

/**
 * Stage 'getAppIcpStep'
 */
def getAppIcpStep() {
    if (initGpl) {
		sendStageStartToGPL(pipelineData, garType, garApp, "100")
    }
    currentBuild.displayName = "${action}_${app} of ${icpEnv} and the namespace ${namespace} and the center ${center}"
    try {
        printOpen("Get App ", EchoLevel.ALL)
        valuesDeployed = null
        valuesDeployed = getLastAppInfoICP(icpEnv, app, namespace, center)
        printAppICP(valuesDeployed)
		if (initGpl) {
			sendStageEndToGPL(pipelineData, garType, garApp, "100")
		}
    } catch (Exception e) {
		if (initGpl) {
			sendStageEndToGPL(pipelineData, garType, garApp, "100", Strings.toHtml(e.getMessage()), null, "error")
		}
        throw e
    }
}

/**
 * Stage 'restartAppIcpStep'
 */
def restartAppIcpStep() {
	if (initGpl) {
		sendStageStartToGPL(pipelineData, garType, garApp, "200")
	}
	
	def icpNamespace=null
	boolean isArchProject=false
	
	try {
        printOpen("Restart App ${garApp} ", EchoLevel.ALL)

        def scalingMap = [
            scaleCPUCores:"${scaleCPUCores}",
            scaleMemory:"${scaleMemory}",
            scaleNumInstances:"${scaleNumInstances}"
        ]

        if (action=="START") {
            startAndStop=true
        }
		if (namespace=="APP") {
			icpNamespace=GlobalVars.ICP_APP_APPS
			isArchProject=false
		}else {
			icpNamespace=GlobalVars.ICP_APP_ARCH
			isArchProject=true
		}
        if ( "yes".equals(useCatalogSize) || (!"NO".equals(scaleCPUCores) ||  !"NO".equals(scaleMemory) || !"DEFAULT".equals(scaleNumInstances)) && (env.CATMSV_SIZE!=null && "true".equals(env.CATMSV_SIZE))) {
            ICPAppResources icpResources = null
            def sizeCPU="M"
            def sizeMEM="M"
            def sizeREP="M"

            /**
                * Si ha seleccionado hacer el scaling de algo no tiene que usar los tamaños del catalogo
                */
            if (!"NO".equals(scaleCPUCores) || !"NO".equals(scaleMemory) ||  !"DEFAULT".equals(scaleNumInstances)) {
                useCatalogSize="no"
            }

            if (!"NO".equals(scaleCPUCores)) {
                sizeCPU=scaleCPUCores
            }
            if (!"NO".equals(scaleMemory)) {
                sizeMEM=scaleMemory
            }
            if (!"DEFAULT".equals(scaleNumInstances)) {
                sizeREP=scaleNumInstances
            }

            def icpEnvResources=icpEnv
            /**
                * Vamos a usar los tamaños del catalogo de PRO para realizar el deploy sea cual sea el entorno
                */
            if ("yes".equals(useProSize)){
                icpEnvResources="PRO" //Usaremos recursos de PRO
                printOpen("El tamaño que queremos usar es el de PRO no el del entorno donde estamos", EchoLevel.ALL)
            }else {
                printOpen("El entorno del tamaño a usar es el del entorno ${icpEnv}", EchoLevel.ALL)
            }

            /**
                * Consultamos el tamaño del micro en catalogo o usamos los tamaños definidos para el entorno
                */
            if ("yes".equals(useCatalogSize)) {
                printOpen("Se deben usar los tamaños del catalogo", EchoLevel.ALL)
                icpResources = generateICPResources(null,icpEnvResources,true, garApp,null,garType,app-garApp,icpNamespace)
            }else{
                printOpen("No se debe usar los tamaños del catalogo", EchoLevel.ALL)
                icpResources = getSizesFromCatalog(icpNamespace,garType,icpEnvResources,isArchProject,sizeMEM,sizeCPU,sizeREP)
            }
            /**
                * Tenemos que aplicar las restricciones de tamaño para poder ya que sino es PRO ciertos valores no se pueden permitir
                */
            //icpResources=restrictSizesFromCatalog(icpResources,icpEnv)

            if (!"NO".equals(scaleCPUCores) || "yes".equals(useCatalogSize)) {
                scaleCPUCores=icpResources.cpuSize
            }
            if (!"NO".equals(scaleMemory) || "yes".equals(useCatalogSize)) {
                scaleMemory=icpResources.memSize
            }
            if (!"DEFAULT".equals(scaleNumInstances) || "yes".equals(useCatalogSize)) {
                scaleNumInstances=icpResources.replicasSize
            }

            scalingMap = [
                scaleCPUCores:"${scaleCPUCores}",
                scaleMemory:"${scaleMemory}",
                scaleNumInstances:"${scaleNumInstances}"
            ]
            /**
                * Aplicamos el start & stop
                */
            printOpen("Se desea escalar, no hace falta consultar el catalogo ${scalingMap} ${icpResources}", EchoLevel.INFO)			
            startAndStopApp(valuesDeployed,app,center,namespace,icpEnv,startAndStop,stableOrNew,garApp,jvmConfig,scalingMap,icpResources,garType,null)
			//Se tiene que clonar OCP			
			absisPipelineStageCloneToOcp(app-garApp+'.0.0',garApp+'-micro',garApp,icpNamespace,icpEnv,userId,garType,userId,'false','true',action)
        }else  {
            /**
                * Aplicamos el start & stop
                */
            printOpen("No se desea escalar nada, no hace falta consultar el catalogo", EchoLevel.ALL)
            startAndStopApp(valuesDeployed,app,center,namespace,icpEnv,startAndStop,stableOrNew,garApp,jvmConfig,scalingMap)
			absisPipelineStageCloneToOcp(app-garApp+'.0.0',garApp+'-micro',garApp,namespace,icpEnv,userId,garType,userId,'false','true',action)
        }
		if (initGpl) {
			sendStageEndToGPL(pipelineData, garType, garApp, "200", null, icpEnv )
		}
    } catch (Exception e) {
		if (initGpl) {
			sendStageEndToGPL(pipelineData, garType, garApp, "200", Strings.toHtml(e.getMessage()), icpEnv, "error")
		}
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
	if (initGpl) {
		sendPipelineEndedToGPL(initGpl, pipelineData, garType, garApp, successPipeline)
	}
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("Is pipeline unsuccessful? ${successPipeline}", EchoLevel.ERROR)
}
