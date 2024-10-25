import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.ICPApiResponse
import com.project.alm.ICPPodsStatus

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
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
            timeout(time: 1, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            ICP_CERT = credentials('icp-absis3-pro-cert')
            ICP_PASS = credentials('icp-absis3-pro-cert-passwd')
            http_proxy = "$GlobalVars.proxyCaixa"
            https_proxy = "$GlobalVars.proxyCaixa"
            proxyHost = "$GlobalVars.proxyCaixaHost"
            proxyPort = "$GlobalVars.proxyCaixaPort"
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

