import groovy.transform.Field
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.SampleAppCleanMode
import com.caixabank.absis3.GlobalVars

@Field Map pipelineParams

@Field boolean successPipeline

@Field String icpEnv
@Field String namespace
@Field String center
@Field Map routes

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters

	successPipeline = true

	icpEnv = params.environmentParam
	namespace = params.namespaceParam
	center = params.centerParam
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
		options {
			buildDiscarder(logRotator(numToKeepStr: '10'))
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
			stage("get-app-routes") {
				steps {					
					getAppRoutesStep()
				}
			}
			stage("get-arch-version") {
				steps {
					getArchVersionStep()
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
 * Stage 'getAppRoutesStep'
 */
def getAppRoutesStep() {				
	printOpen("Get app's routes...", EchoLevel.ALL)
	Map valuesDeployed=getAllDeployedAppsInfoICP(icpEnv, namespace, center)
	routes = getRoutesFromAppsInfo(valuesDeployed)					
}

/**
 * Stage 'getArchVersionStep'
 */
def getArchVersionStep() {
	printOpen("Get arch's versions...", EchoLevel.ALL)
	Map archVersions = getArchVersions(routes, icpEnv, namespace, center)
	printArchVersions(archVersions)
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
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
	successPipeline = false
	printOpen("Is pipeline unsuccessful? ${successPipeline}", EchoLevel.ERROR)   
}
