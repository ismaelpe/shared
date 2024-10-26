import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.IClientInfo
import com.project.alm.PipelineData
import com.project.alm.PipelineStructureType
import com.project.alm.SampleAppCleanMode
import com.project.alm.GlobalVars
import com.project.alm.Strings

import java.util.Map

@Field Map pipelineParams

@Field PipelineData pipelineData

@Field boolean successPipeline
@Field boolean initAppPortal

@Field String cloudEnv
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
    initAppPortal = false

    cloudEnv = params.environmentParam
    namespace = params.namespaceParam
    app = params.appnameParam
    garApp = params.garappnameParam
    garType = params.garTypeParam
    center = params.centerParam
    userId = params.userIdParam

    valuesDeployed = null
    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
            timeout(time: 30, unit: 'MINUTES')
        }
        //Environment sobre el qual se ejecuta este tipo de job
        environment {
            AppPortal = credentials('IDECUA-JENKINS-USER-TOKEN')
            JNKMSV = credentials('JNKMSV-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "$GlobalVars.proxyDigitalscale"
            https_proxy = "$GlobalVars.proxyDigitalscale"
            proxyHost = "$GlobalVars.proxyDigitalscaleHost"
            proxyPort = "$GlobalVars.proxyDigitalscalePort"
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
    pipelineData = new PipelineData(PipelineStructureType.RESTART_APP, "${env.BUILD_TAG}", env.JOB_NAME, null)
    sendPipelineStartToAppPortal(pipelineData, garType, garApp, app-garApp, cloudEnv.toUpperCase(),userId)
    initAppPortal = true
}

/**
 * Stage 'getAppCloudStep'
 */
def getAppCloudStep() {
    sendStageStartToAppPortal(pipelineData, garType, garApp, "100")
    currentBuild.displayName = "RestartingApp_${app} of ${cloudEnv} and the namespace ${namespace} and the center ${center}"
    try {
        printOpen("Get App ", EchoLevel.ALL)
        valuesDeployed = null
        valuesDeployed=getLastAppInfoCloud(cloudEnv, app, namespace,center)
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
    try {
        printOpen("Restart App ", EchoLevel.ALL)
        restartApp(valuesDeployed,app,center,namespace,cloudEnv)
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
    printOpen("Is pipeline unsuccessful? ${successPipeline}", EchoLevel.ERROR)
    successPipeline = false
    sendPipelineEndedToAppPortal(initAppPortal, pipelineData, garType, garApp, successPipeline)
}
