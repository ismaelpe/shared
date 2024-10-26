import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.CloudApiResponse
import com.project.alm.CloudPodsStatus

@Field Map pipelineParams

@Field String namespaceId  = "${namespaceParam}"
@Field String type = "${typeParam}"
@Field String app = "${appParam}"
@Field String major = "${majorParam}"
@Field String cpuSize = "${cpuSizeParam}"
@Field String memSize = "${memSizeParam}"
@Field String replicaSize = "${replicaSizeParam}"
@Field String envi = "${envParam}"
@Field String command = "${commandParam}"

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
    namespaceId  = params.namespaceParam
	type = params.typeParam
    app = params.appParam
    major = params.majorParam
	cpuSize = params.cpuSizeParam
	memSize = params.memSizeParam
	replicaSize = params.replicaSizeParam
	envi = params.envParam
	command = params.commandParam
    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
            timeout(time: 1, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "$GlobalVars.proxyDigitalscale"
            https_proxy = "$GlobalVars.proxyDigitalscale"
            proxyHost = "$GlobalVars.proxyDigitalscaleHost"
            proxyPort = "$GlobalVars.proxyDigitalscalePort"
            executionProfile = "${executionProfileParam ? executionProfileParam : 'DEFAULT'}"
        }
        stages {
            stage("resize-app") {
				when {
					expression { "PUT".equals(command) }
				}
                steps {
                    resizeAppStep()
                }
            } 
			stage("get-size-app") {
				steps {
                    getSizeAppStep()
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
 * Stage 'resizeAppStep'
 */
def resizeAppStep() {
    printOpen("Resizing the app ${app} and the type ${type} size: ${memSize}, ${cpuSize} ", EchoLevel.ALL)
    resizeAppInCatMsv(namespaceId,type,app,major,envi,cpuSize,memSize,replicaSize)
}

/**
 * Stage 'getSizeAppStep'
 */
def getSizeAppStep() {
    currentBuild.displayName = "${env.BUILD_ID}_${command} ${type}.${app}.${major}"
    getSizeAppInCatMsv(namespaceId,type,app,major,envi)
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
    printOpen("Pipeline has succeeded", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen("Pipeline has failed", EchoLevel.ERROR)
}

