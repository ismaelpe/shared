import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.SampleAppCleanMode
import com.project.alm.GlobalVars

@Field Map pipelineParams

@Field boolean successPipeline
@Field String icpOriginEnv
@Field String icpDestinyEnv

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo

    successPipeline = true
	icpOriginEnv = params.environmentIcpOriginParam
	icpDestinyEnv = params.environmentIcpDestinyParam
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            ICP_CERT = credentials('icp-absis3-pro-cert')
            ICP_PASS = credentials('icp-absis3-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
        }
        stages {
            stage("get-secrets") {
                steps {
                    getSecretsStep()
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
 * Stage 'getSecretsStep'
 */
def getSecretsStep() {
    currentBuild.displayName = "Compare${env.BUILD_ID}_${icpOriginEnv}vs${icpDestinyEnv}"
    
    printOpen("Get all the secrets ", EchoLevel.INFO)
    printOpen("Compare: ${icpOriginEnv} vs ${icpDestinyEnv}", EchoLevel.INFO)
    
    compareSecretsICPToICP(icpOriginEnv,icpDestinyEnv)
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("SUCCESS", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("FAILURE", EchoLevel.ERROR)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}
