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
@Field boolean initGpl
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

    initGpl = false
    successPipeline = false
    iop = null
    appsPendientes = null

    almEvent = null
    initCallStartMillis = new Date().getTime()

    pipeline {
        agent { node(absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            JNKMSV = credentials('JNKMSV-USER-TOKEN')
            ICP_CERT = credentials('icp-alm-pro-cert')
            ICP_PASS = credentials('icp-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
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
    clientInfo.setGroupId('com.project.absis')
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
    sendPipelineStartToGPL(clientInfo, pipelineData, pipelineOriginId)
    sendStageStartToGPL(clientInfo, pipelineData, '100')
    
    initGpl = true
    sendStageEndToGPL(clientInfo, pipelineData, '100')
}

/**
 * Stage 'getIopStep'
 */
def getIopStep() {
    sendStageStartToGPL(clientInfo, pipelineData, '110')
    try {
        iop = iopCampaignCatalogUtils.getIop()
        sendStageEndToGPL(clientInfo, pipelineData, '110')
    }catch (Exception e) {
        printOpen("Error en la validacion de dependencias ${e.getMessage()}", EchoLevel.ALL)
        sendStageEndToGPL(clientInfo, pipelineData, '110', e.getMessage(), null, 'error')
        throw e
    }
}

/**
 * Stage 'notifyIopAppsStep'
 */
def notifyIopAppsStep() {
    sendStageStartToGPL(clientInfo, pipelineData, '200')
    try {
        appsPendientes = iopCampaignCatalogUtils.notifyCloseIop(iop)
        if (appsPendientes == null) {
            sendStageEndToGPL(clientInfo, pipelineData, '200')
        }else {
            sendStageEndToGPL(clientInfo, pipelineData, '200', appsPendientes, null, 'warning')
        }
    }catch (Exception e) {
        printOpen("Error en la validacion de dependencias ${e.getMessage()}", EchoLevel.ALL)
        sendStageEndToGPL(clientInfo, pipelineData, '200', e.getMessage(), null, 'error')
        throw e
    }
}

/**
 * Stage 'endPipelineStep'
 */
def endPipelineStep() {
    sendStageStartToGPL(clientInfo, pipelineData, '300')

    pipelineData.prepareResultData(clientInfo.artifactVersion, clientInfo.artifactId, clientInfo.applicationName)
    if (appsPendientes != null) {
        //Tenemos apps pendientes no podemos hacer close
        pipelineData.pipelineStructure.resultPipelineData.areAppsPending = true
    }

    sendStageEndToGPL(clientInfo, pipelineData, '300')
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
    sendPipelineResultadoToGPL(initGpl, clientInfo, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, clientInfo, pipelineData, successPipeline)
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

    sendPipelineResultadoToGPL(initGpl, clientInfo, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, clientInfo, pipelineData, successPipeline)
}

/**
 * Stage 'endiPipelineAlwaysStep'
 */
def endiPipelineAlwaysStep() {
    attachPipelineLogsToBuild(clientInfo, pipelineData)
    cleanWorkspace()
}
