import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.SampleAppCleanMode
import com.project.alm.GlobalVars

@Field Map pipelineParams

@Field boolean successPipeline

@Field String icpEnv
@Field String namespace
@Field String secret

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo

    successPipeline = true

	icpEnv = params.environmentParam
	namespace = params.namespaceParam
	secret  = params.secretParam
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
            timeout(time: 1, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
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
			stage("delete-secret-icp") {
				steps {
                    deleteSecretIcpStep()
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
 * Stage 'deleteSecretIcpStep'
 */
def deleteSecretIcpStep() {
    currentBuild.displayName = "Delete ${secret}_Namespace: ${namespace}_Environment: ${icpEnv}"
    printOpen("delete Secret ", EchoLevel.ALL)
    deleteSecretICP(icpEnv,secret, namespace)
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("Is pipeline successful? ${successPipeline}", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("Is pipeline unsuccessful? ${successPipeline}", EchoLevel.ERROR)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}

