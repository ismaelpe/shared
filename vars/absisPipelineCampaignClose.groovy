import groovy.transform.Field
import com.caixabank.absis3.*
import groovy.json.JsonSlurperClassic
import com.caixabank.absis3.KpiAlmEvent
import com.caixabank.absis3.KpiAlmEventStage
import com.caixabank.absis3.KpiAlmEventOperation

@Field Map pipelineParams
@Field String user
@Field String targetAlmFolder
@Field String pipelineOriginId
@Field ClientInfo clientInfo
@Field PipelineData pipelineData
@Field boolean initGpl
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
    initGpl = false
    successPipeline = false
    currentPercentage = 100
    almEvent = null
    initCallStartMillis = new Date().getTime()

    pipeline {
        agent {    node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            JNKMSV = credentials('JNKMSV-USER-TOKEN')
            ICP_CERT = credentials('icp-absis3-pro-cert')
            ICP_PASS = credentials('icp-absis3-pro-cert-passwd')
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
    clientInfo.setGroupId('com.caixabank.absis')
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
    sendPipelineStartToGPL(clientInfo, pipelineData, pipelineOriginId)
    sendStageStartToGPL(clientInfo, pipelineData, '100')
    initGpl = true
    sendStageEndToGPL(clientInfo, pipelineData, '100')
}

/**
 * Stage 'resetPercentageStep'
 */
def resetPercentageStep() {
    sendStageStartToGPL(clientInfo, pipelineData, '200')

    printOpen("Modifying percentage ${pipelineData.bmxStructure.environment}", EchoLevel.INFO)
    increaseCampaignCannary(0, pipelineData.bmxStructure.environment)
    sendStageEndToGPL(clientInfo, pipelineData, '200')
}

/**
 * Stage 'refreshApigatewayStep'
 */
def refreshApigatewayStep() {
    sendStageStartToGPL(clientInfo, pipelineData, '300')

    refreshConfigurationViaRefreshBus('1', 'ARQ.MIA', 'apigateway', '1', '*', pipelineData.bmxStructure.environment)
    refreshConfigurationViaRefreshBus('2', 'ARQ.MIA', 'apigateway', '1', '*', pipelineData.bmxStructure.environment)

    sendStageEndToGPL(clientInfo, pipelineData, '300')
}

/**
 * Stage 'closeCampaignStep'
 */
def closeCampaignStep() {
    sendStageStartToGPL(clientInfo, pipelineData, '400')

    printOpen('Closing Campaign', EchoLevel.INFO)
    try {
        iopCampaignCatalogUtils.startOrStop(false)
        sendStageEndToGPL(clientInfo, pipelineData, '400')
    }catch (Exception e) {
        sendStageEndToGPL(clientInfo, pipelineData, '400', e.getMessage(), null, 'error')
        throw e
    }
}

/**
 * Stage 'endPipelineStep'
 */
def endPipelineStep() {
    sendStageStartToGPL(clientInfo, pipelineData, '500')

    pipelineData.prepareResultData(clientInfo.artifactVersion, clientInfo.artifactId, clientInfo.applicationName)

    sendStageEndToGPL(clientInfo, pipelineData, '500')
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
    sendPipelineResultadoToGPL(initGpl, clientInfo, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, clientInfo, pipelineData, successPipeline)
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

    sendPipelineResultadoToGPL(initGpl, clientInfo, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, clientInfo, pipelineData, successPipeline)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(clientInfo, pipelineData)
    cleanWorkspace()
}
