import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.SampleAppCleanMode
import com.project.alm.GlobalVars

@Field Map pipelineParams

@Field boolean successPipeline
@Field String bmxEnv

@Field String cloudEnv
@Field String username
@Field String password
@Field String uri
@Field String namespace
@Field String secret
@Field String nameApp
@Field String tipoVersionApp

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo

    successPipeline = true
	bmxEnv = params.environmentParam

	cloudEnv = params.environmentParam
	username = params.usernameParam
	password = params.passwordParam
	uri = params.uriParam
	namespace = params.namespaceParam
	secret  = params.secretParam
	nameApp = params.nameAppParam
	tipoVersionApp = params.tipoVersionParam
    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
        options {
			buildDiscarder(logRotator(numToKeepStr: '0'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
            https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
        }
		stages {
			stage("create-secret-cloud") {
				steps {					
                     createSecretCloudStep()
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
 * Stage 'createSecretCloudStep'
 */
def createSecretCloudStep() {			
    printOpen("Create Secret1 ", EchoLevel.INFO)
    if (namespace!="CLEAN") createSecretCloud(cloudEnv,secret, username,password,uri,namespace,tipoVersionApp,nameApp)
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
