import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.SampleAppCleanMode
import com.project.alm.GlobalVars

@Field Map pipelineParams

@Field boolean successPipeline

@Field String cloudEnv
@Field String namespace
@Field String center
@Field Map routes

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters

	successPipeline = true

	cloudEnv = params.environmentParam
	namespace = params.namespaceParam
	center = params.centerParam
    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
		options {
			buildDiscarder(logRotator(numToKeepStr: '10'))
		}
		environment {
			GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
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
	Map valuesDeployed=getAllDeployedAppsInfoCloud(cloudEnv, namespace, center)
	routes = getRoutesFromAppsInfo(valuesDeployed)					
}

/**
 * Stage 'getArchVersionStep'
 */
def getArchVersionStep() {
	printOpen("Get arch's versions...", EchoLevel.ALL)
	Map archVersions = getArchVersions(routes, cloudEnv, namespace, center)
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
