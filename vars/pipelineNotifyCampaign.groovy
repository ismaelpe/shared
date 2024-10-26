import groovy.transform.Field
import com.project.alm.*
import groovy.json.JsonSlurperClassic
import com.project.alm.GlobalVars
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams

@Field int currentPercentage
@Field String user
@Field String targetAlmFolder
@Field String pipelineOriginId
@Field String rollbackOrIncrease

@Field ClientInfo clientInfo
@Field PipelineData pipelineData
@Field boolean initAppPortal
@Field boolean successPipeline = false
@Field def iop
@Field def appsPendientes

@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    currentPercentage = params.currentPercentage as Ineteger

    user = params.userId
    targetAlmFolder = params.targetAlmFolder
    pipelineOriginId = params.pipelineOriginId
    rollbackOrIncrease = params.isRollbackParam

    initAppPortal = false
    successPipeline = false
    iop = null
    appsPendientes = null

    almEvent = null
    initCallStartMillis = new Date().getTime()

    pipeline {
        agent { node(almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
        }
        environment {
            AppPortal = credentials('IDECUA-JENKINS-USER-TOKEN')
            JNKMSV = credentials('JNKMSV-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
            https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
            executionProfile = "${executionProfileParam ? executionProfileParam : 'DEFAULT'}"
        }
        stages {
            stage('init-data') {
                steps {
                    initDataStep()
                }
            }
            stage('get-iop') {
                steps {
                    getIopStep()
                }
            }
            stage('notify-iop-apps') {
                steps {
                    notifyIopAppsStep()
                }
            }
            stage('end-pipeline') {
                when {
                    expression { currentPercentage <= CanaryUtilities.finalPercentage(clientInfo.applicationName) }
                }
                steps {
                    endPipelineStep()
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
                endiPipelineAlwaysStep()
            }
        }
    }
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/**
 * Stage 'initDataStep'
 */
def initDataStep() {
    initGlobalVars(pipelineParams)
    clientInfo = new ClientInfo()
    clientInfo.setApplicationName(GlobalVars.CAMPAIGN_GAR_APP)
    clientInfo.setArtifactId(GlobalVars.CAMPAIGN_GAR_APP + '-conf')
    
    clientInfo.setArtifactVersion('1.0.0')
    clientInfo.setArtifactType(ArtifactType.SIMPLE)
    clientInfo.setArtifactSubType(ArtifactSubType.GLOBAL_PIPELINE)
    clientInfo.setGroupId('com.project.alm')
    pipelineData = new PipelineData(PipelineStructureType.NOTIFY_END_CAMPAIGN, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.pushUser = user
    //Cambiar por entorno de DEV para hacer pruebas
    pipelineData.initGlobalPipeline(GlobalVars.PRO_ENVIRONMENT.toString())
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolder)
    
    pipelineData.pipelineStructure.pipelineId = pipelineData.pipelineStructure.pipelineId + '-' + pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage
    
    almEvent = new KpiAlmEvent(
        clientInfo, pipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_CAMPAING_NOTIFY)
    sendPipelineStartToAppPortal(clientInfo, pipelineData, pipelineOriginId)
    sendStageStartToAppPortal(clientInfo, pipelineData, '100')
    
    initAppPortal = true
    sendStageEndToAppPortal(clientInfo, pipelineData, '100')
}

/**
 * Stage 'getIopStep'
 */
def getIopStep() {
    sendStageStartToAppPortal(clientInfo, pipelineData, '110')
    try {
        iop = iopCampaignCatalogUtils.getIop()
        sendStageEndToAppPortal(clientInfo, pipelineData, '110')
    }catch (Exception e) {
        printOpen("Error en la validacion de dependencias ${e.getMessage()}", EchoLevel.ALL)
        sendStageEndToAppPortal(clientInfo, pipelineData, '110', e.getMessage(), null, 'error')
        throw e
    }
}

/**
 * Stage 'notifyIopAppsStep'
 */
def notifyIopAppsStep() {
    sendStageStartToAppPortal(clientInfo, pipelineData, '200')
    try {
        appsPendientes = iopCampaignCatalogUtils.notifyCloseIop(iop)
        if (appsPendientes == null) {
            sendStageEndToAppPortal(clientInfo, pipelineData, '200')
        }else {
            sendStageEndToAppPortal(clientInfo, pipelineData, '200', appsPendientes, null, 'warning')
        }
    }catch (Exception e) {
        printOpen("Error en la validacion de dependencias ${e.getMessage()}", EchoLevel.ALL)
        sendStageEndToAppPortal(clientInfo, pipelineData, '200', e.getMessage(), null, 'error')
        throw e
    }
}

/**
 * Stage 'endPipelineStep'
 */
def endPipelineStep() {
    sendStageStartToAppPortal(clientInfo, pipelineData, '300')

    pipelineData.prepareResultData(clientInfo.artifactVersion, clientInfo.artifactId, clientInfo.applicationName)
    if (appsPendientes != null) {
        //Tenemos apps pendientes no podemos hacer close
        pipelineData.pipelineStructure.resultPipelineData.areAppsPending = true
    }

    sendStageEndToAppPortal(clientInfo, pipelineData, '300')
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("Se success el pipeline ${successPipeline}", EchoLevel.INFO)
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis - initCallStartMillis))
    }
    sendPipelineResultadoToAppPortal(initAppPortal, clientInfo, pipelineData, successPipeline)
    sendPipelineEndedToAppPortal(initAppPortal, clientInfo, pipelineData, successPipeline)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen("Se failure el pipeline ${successPipeline}", EchoLevel.ERROR)

    successPipeline = false
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis - initCallStartMillis))
    }

    sendPipelineResultadoToAppPortal(initAppPortal, clientInfo, pipelineData, successPipeline)
    sendPipelineEndedToAppPortal(initAppPortal, clientInfo, pipelineData, successPipeline)
}

/**
 * Stage 'endiPipelineAlwaysStep'
 */
def endiPipelineAlwaysStep() {
    attachPipelineLogsToBuild(clientInfo, pipelineData)
    cleanWorkspace()
}
