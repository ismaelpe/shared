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
@Field boolean successPipeline
@Field def openedMicros
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

    currentPercentage = params.currentPercentage as Integer
    user = params.userId
    targetAlmFolder = params.targetAlmFolder
    pipelineOriginId = params.pipelineOriginId
    rollbackOrIncrease = params.isRollbackParam

    initGpl = false
    successPipeline = false

    openedMicros = null
    appsPendientes = null

    almEvent = null
    initCallStartMillis = new Date().getTime()

    pipeline {
        agent {    node(almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            JNKMSV = credentials('JNKMSV-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
            https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
            executionProfile = "${executionProfileParam ? executionProfileParam : 'DEFAULT'}"
        }
        //Atencion que en el caso que estemos en un MergeRequest... quizas solo debamos validar la issue
        stages {
            stage('init-data') {
                steps {
                    initDataStep()
                }
            }
            stage('get-opened-micros') {
                steps {
                    getOpenedMicros()
                }
            }
            stage('notify-opened-micros') {
                steps {
                    notifyOpenedMicrosStep()
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
    clientInfo.setArtifactId(GlobalVars.CAMPAIGN_GAR_APP)
    String date = new Date(System.currentTimeMillis()).format('yyyyMMddHHmmss')
    clientInfo.setArtifactVersion('0.0.' + date)
    clientInfo.setArtifactType(ArtifactType.SIMPLE)
    clientInfo.setArtifactSubType(ArtifactSubType.GLOBAL_PIPELINE)
    clientInfo.setGroupId('com.project.alm')

    pipelineData = new PipelineData(PipelineStructureType.NOTIFY_END_CAMPAIGN, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.pushUser = user
    //Cambiar por entorno de DEV para hacer pruebas
    pipelineData.initGlobalPipeline(GlobalVars.PRO_ENVIRONMENT.toString())
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolder)

    almEvent = new KpiAlmEvent(
        clientInfo, pipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_CAMPAING_NOTIFY)
    pipelineData.pipelineStructure.pipelineId = pipelineData.pipelineStructure.pipelineId + '-' + pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage

    initGpl = true
}

/**
 * Stage 'getOpenedMicrosStep'
 */
def getOpenedMicrosStep() {
    try {
        openedMicros = iopDevopsCatalogUtils.getOpenedMicros(1)
    }catch (Exception e) {
        printOpen("Error en la validacion de dependencias ${e.getMessage()}", EchoLevel.ALL)
        throw e
    }
}

/**
 * Stage 'notifyOpenedMicrosStep'
 */
def notifyOpenedMicrosStep() {
    try {
        appsPendientes = iopDevopsCatalogUtils.notifyToAppTeam(openedMicros)
    }catch (Exception e) {
        printOpen("Error en la validacion de dependencias ${e.getMessage()}", EchoLevel.ALL)
        throw e
    }
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
}

/**
 * Stage 'endiPipelineAlwaysStep'
 */
def endiPipelineAlwaysStep() {
    printOpen('Cleaning pipeline', EchoLevel.ALL)
    cleanWs()
    cleanWorkspace()
}

