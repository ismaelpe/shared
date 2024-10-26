import groovy.transform.Field
import com.project.alm.*
import groovy.json.JsonSlurperClassic
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams
@Field String user
@Field String targetAlmFolder
@Field String pipelineOriginId
@Field ClientInfo clientInfo
@Field PipelineData pipelineData
@Field boolean initAppPortal
@Field boolean successPipeline
@Field int currentPercentage
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

    user = params.userId
    targetAlmFolder = params.targetAlmFolder
    pipelineOriginId = params.pipelineOriginId
    initAppPortal = false
    successPipeline = false
    currentPercentage = 100
    almEvent = null
    initCallStartMillis = new Date().getTime()

    pipeline {
        agent {    node (almJenkinsAgent(pipelineParams)) }
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
            stage('reset-percentage') {
                when {
                    expression { currentPercentage <= CanaryUtilities.finalPercentage(clientInfo.applicationName) }
                }
                steps {
                    resetPercentageStep()
                }
            }
            stage('refresh-apigateway') {
                when {
                    expression { currentPercentage <= CanaryUtilities.finalPercentage(clientInfo.applicationName) }
                }
                steps {
                    refreshApigatewayStep()
                }
            }
            stage('close-campaign') {
                steps {
                    closeCampaignStep()
                }
            }
            stage('end-pipeline') {
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
                endPipelineAlwaysStep()
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
    pipelineData = new PipelineData(PipelineStructureType.CLOSE_CAMPAIGN, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.pushUser = user
    //Cambiar por entorno de DEV para hacer pruebas
    //pipelineData.initGlobalPipeline(GlobalVars.DEV_ENVIRONMENT.toString())
    pipelineData.initGlobalPipeline(GlobalVars.PRO_ENVIRONMENT.toString())
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolder)

    pipelineData.pipelineStructure.pipelineId = pipelineData.pipelineStructure.pipelineId
    almEvent = new KpiAlmEvent(
                            clientInfo, pipelineData,
                            KpiAlmEventStage.GENERAL,
                            KpiAlmEventOperation.PIPELINE_CAMPAING_CLOSE_RELEASE)
    sendPipelineStartToAppPortal(clientInfo, pipelineData, pipelineOriginId)
    sendStageStartToAppPortal(clientInfo, pipelineData, '100')
    initAppPortal = true
    sendStageEndToAppPortal(clientInfo, pipelineData, '100')
}

/**
 * Stage 'resetPercentageStep'
 */
def resetPercentageStep() {
    sendStageStartToAppPortal(clientInfo, pipelineData, '200')

    printOpen("Modifying percentage ${pipelineData.bmxStructure.environment}", EchoLevel.INFO)
    increaseCampaignCannary(0, pipelineData.bmxStructure.environment)
    sendStageEndToAppPortal(clientInfo, pipelineData, '200')
}

/**
 * Stage 'refreshApigatewayStep'
 */
def refreshApigatewayStep() {
    sendStageStartToAppPortal(clientInfo, pipelineData, '300')

    refreshConfigurationViaRefreshBus('1', 'ARQ.MIA', 'apigateway', '1', '*', pipelineData.bmxStructure.environment)
    refreshConfigurationViaRefreshBus('2', 'ARQ.MIA', 'apigateway', '1', '*', pipelineData.bmxStructure.environment)

    sendStageEndToAppPortal(clientInfo, pipelineData, '300')
}

/**
 * Stage 'closeCampaignStep'
 */
def closeCampaignStep() {
    sendStageStartToAppPortal(clientInfo, pipelineData, '400')

    printOpen('Closing Campaign', EchoLevel.INFO)
    try {
        iopCampaignCatalogUtils.startOrStop(false)
        sendStageEndToAppPortal(clientInfo, pipelineData, '400')
    }catch (Exception e) {
        sendStageEndToAppPortal(clientInfo, pipelineData, '400', e.getMessage(), null, 'error')
        throw e
    }
}

/**
 * Stage 'endPipelineStep'
 */
def endPipelineStep() {
    sendStageStartToAppPortal(clientInfo, pipelineData, '500')

    pipelineData.prepareResultData(clientInfo.artifactVersion, clientInfo.artifactId, clientInfo.applicationName)

    sendStageEndToAppPortal(clientInfo, pipelineData, '500')
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen('SUCCESS', EchoLevel.INFO)
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
    printOpen('FAILURE', EchoLevel.ERROR)
    successPipeline = false
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis - initCallStartMillis))
    }

    sendPipelineResultadoToAppPortal(initAppPortal, clientInfo, pipelineData, successPipeline)
    sendPipelineEndedToAppPortal(initAppPortal, clientInfo, pipelineData, successPipeline)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(clientInfo, pipelineData)
    cleanWorkspace()
}
