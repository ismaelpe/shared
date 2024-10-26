import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.SampleAppCleanMode
import com.project.alm.GlobalVars
import java.util.Map

@Field Map pipelineParams

@Field boolean successPipeline = true

@Field String cloudEnv
@Field String namespace
@Field String app
@Field String center

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters

	// las variables que se obtienen como parametro del job no es necesario
	// redefinirlas, se hace por legibilidad del codigo

	successPipeline = true

	cloudEnv = params.environmentParam
	namespace = params.namespaceParam
	app = params.appnameParam
	center = params.centerParam
    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
		options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
            timeout(time: 1, unit: 'HOURS')
		}
		//Environment sobre el qual se ejecuta este tipo de job
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
			stage("get-app-cloud") {
				steps {
					getAppCloudStep()
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
 * Stage 'getAppCloudStep'
 */
def getAppCloudStep() {
	printOpen("Get App ", EchoLevel.ALL)
	Map valuesDeployed = null
	if (cloudEnv=="ALL"){
		valuesDeployed=getLastAppInfoCloud("DEV",app, namespace,center)
		printAppCloud(valuesDeployed)
		valuesDeployed=getLastAppInfoCloud("TST",app, namespace,center)
		printAppCloud(valuesDeployed)
		valuesDeployed=getLastAppInfoCloud("PRE",app, namespace,center)
		printAppCloud(valuesDeployed)
		valuesDeployed=getLastAppInfoCloud("PRO",app, namespace,center)
		printAppCloud(valuesDeployed)
	}else {
		valuesDeployed=getLastAppInfoCloud(cloudEnv,app, namespace,center)
		printAppCloud(valuesDeployed)
	}						
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

        