import groovy.transform.Field
import com.project.alm.*
import com.project.alm.GlobalVars
import groovy.json.JsonSlurperClassic
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
            stage('validate-dependencies') {
                when {
                    expression { currentPercentage <= CanaryUtilities.finalPercentage(clientInfo.applicationName) }
                }
                steps {
                    validateDependenciesStep()
                }
            }
            stage('modify-percentage') {
                when {
                    expression { currentPercentage <= CanaryUtilities.finalPercentage(clientInfo.applicationName) }
                }
                steps {
                    modifyPercentageStep()
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

    pipelineData = new PipelineData(PipelineStructureType.INC_CAMPAIGN_CANNARY, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.pushUser = user
    //Cambiar por entorno de DEV para hacer pruebas
    //pipelineData.initGlobalPipeline(GlobalVars.DEV_ENVIRONMENT.toString())
    pipelineData.initGlobalPipeline(GlobalVars.PRO_ENVIRONMENT.toString())
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolder)
    String rollbackOrIncreaseS = "${rollbackOrIncrease}"

    if (rollbackOrIncreaseS.equals('rollback')) {
        pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage = 0
        pipelineData.pipelineStructure.nombre = 'RollbackCampaignCannary_Pipeline'
        printOpen('Rollback Cannary', EchoLevel.INFO)
                        }else if (currentPercentage >= CanaryUtilities.finalPercentage(clientInfo.applicationName)) {
        pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage = CanaryUtilities.finalPercentage(clientInfo.applicationName) + 1
        printOpen("Cannary Percentatge more than ${CanaryUtilities.finalPercentage(clientInfo.applicationName)}", EchoLevel.INFO)
                        } else {
        pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage = CanaryUtilities.incrementPercentage(currentPercentage)

        if (pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage == null) pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage = CanaryUtilities.finalPercentage(clientInfo.applicationName)
        printOpen("Current canary percentage is ${currentPercentage} and is gonna be changed to ${pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage}", EchoLevel.INFO)
    }

    pipelineData.pipelineStructure.pipelineId = pipelineData.pipelineStructure.pipelineId + '-' + pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage
    almEvent = new KpiAlmEvent(
                            clientInfo, pipelineData,
                            KpiAlmEventStage.GENERAL,
                            KpiAlmEventOperation.PIPELINE_CAMPAING_INC_CANNARY)
    sendPipelineStartToGPL(clientInfo, pipelineData, pipelineOriginId)
    sendStageStartToGPL(clientInfo, pipelineData, '100')
    initGpl = true
    sendStageEndToGPL(clientInfo, pipelineData, '100')
}

/**
 * Stage 'validateDependenciesStep'
 */
def validateDependenciesStep() {
    sendStageStartToGPL(clientInfo, pipelineData, '110')
    try {
        validateDependenciesCatMsv(true, 'BETA')
        sendStageEndToGPL(clientInfo, pipelineData, '110')
                       }catch (Exception e) {
        printOpen("Error en la validacion de dependencias ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(clientInfo, pipelineData, '110', e.getMessage(), null, 'error')
        throw e
    }
}

/**
 * Stage 'modifyPercentageStep'
 */
def modifyPercentageStep() {
    sendStageStartToGPL(clientInfo, pipelineData, '200')

    printOpen('Modifying percentage', EchoLevel.INFO)

    increaseCampaignCannary(pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage, pipelineData.bmxStructure.environment)
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
 * Stage 'endPipelineStep'
 */
def endPipelineStep() {
    sendStageStartToGPL(clientInfo, pipelineData, '400')

    pipelineData.prepareResultData(clientInfo.artifactVersion, clientInfo.artifactId, clientInfo.applicationName)

    sendStageEndToGPL(clientInfo, pipelineData, '400')
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
