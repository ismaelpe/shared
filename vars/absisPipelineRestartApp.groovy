import groovy.transform.Field
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.IClientInfo
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PipelineStructureType
import com.caixabank.absis3.SampleAppCleanMode
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.Strings

import java.util.Map

@Field Map pipelineParams

@Field PipelineData pipelineData

@Field boolean successPipeline
@Field boolean initGpl

@Field String icpEnv
@Field String namespace
@Field String app
@Field String garApp
@Field String garType
@Field String center
@Field String userId

@Field Map valuesDeployed

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
    app = params.appnameParam
    garApp = params.garappnameParam
    garType = params.garTypeParam
    center = params.centerParam
    userId = params.userIdParam

    valuesDeployed = null
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
            timeout(time: 30, unit: 'MINUTES')
        }
        //Environment sobre el qual se ejecuta este tipo de job
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            JNKMSV = credentials('JNKMSV-USER-TOKEN')
            ICP_CERT = credentials('icp-absis3-pro-cert')
            ICP_PASS = credentials('icp-absis3-pro-cert-passwd')
            http_proxy = "$GlobalVars.proxyCaixa"
            https_proxy = "$GlobalVars.proxyCaixa"
            proxyHost = "$GlobalVars.proxyCaixaHost"
            proxyPort = "$GlobalVars.proxyCaixaPort"
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
    pipelineData = new PipelineData(PipelineStructureType.RESTART_APP, "${env.BUILD_TAG}", env.JOB_NAME, null)
    sendPipelineStartToGPL(pipelineData, garType, garApp, app-garApp, icpEnv.toUpperCase(),userId)
    initGpl = true
}

/**
 * Stage 'getAppIcpStep'
 */
def getAppIcpStep() {
    sendStageStartToGPL(pipelineData, garType, garApp, "100")
    currentBuild.displayName = "RestartingApp_${app} of ${icpEnv} and the namespace ${namespace} and the center ${center}"
    try {
        printOpen("Get App ", EchoLevel.ALL)
        valuesDeployed = null
        valuesDeployed=getLastAppInfoICP(icpEnv, app, namespace,center)
        printAppICP(valuesDeployed)
        sendStageEndToGPL(pipelineData, garType, garApp, "100")
    } catch (Exception e) {
        sendStageEndToGPL(pipelineData, garType, garApp, "100", Strings.toHtml(e.getMessage()), null, "error")
        throw e
    }
}

/**
 * Stage 'restartAppIcpStep'
 */
def restartAppIcpStep() {
    sendStageStartToGPL(pipelineData, garType, garApp, "200")
    try {
        printOpen("Restart App ", EchoLevel.ALL)
        restartApp(valuesDeployed,app,center,namespace,icpEnv)
        sendStageEndToGPL(pipelineData, garType, garApp, "200", null, icpEnv )

    } catch (Exception e) {
        sendStageEndToGPL(pipelineData, garType, garApp, "200", Strings.toHtml(e.getMessage()), icpEnv, "error")
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
    sendPipelineEndedToGPL(initGpl, pipelineData, garType, garApp, successPipeline)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen("Is pipeline unsuccessful? ${successPipeline}", EchoLevel.ERROR)
    successPipeline = false
    sendPipelineEndedToGPL(initGpl, pipelineData, garType, garApp, successPipeline)
}
