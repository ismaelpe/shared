import groovy.transform.Field
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.SampleAppCleanMode
import com.caixabank.absis3.GlobalVars
import java.util.Map

@Field Map pipelineParams

@Field boolean successPipeline = true

@Field String icpEnv
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

	icpEnv = params.environmentParam
	namespace = params.namespaceParam
	app = params.appnameParam
	center = params.centerParam
    
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
			stage("get-app-icp") {
				steps {
					getAppIcpStep()
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
 * Stage 'getAppIcpStep'
 */
def getAppIcpStep() {
	printOpen("Get App ", EchoLevel.ALL)
	Map valuesDeployed = null
	if (icpEnv=="ALL"){
		valuesDeployed=getLastAppInfoICP("DEV",app, namespace,center)
		printAppICP(valuesDeployed)
		valuesDeployed=getLastAppInfoICP("TST",app, namespace,center)
		printAppICP(valuesDeployed)
		valuesDeployed=getLastAppInfoICP("PRE",app, namespace,center)
		printAppICP(valuesDeployed)
		valuesDeployed=getLastAppInfoICP("PRO",app, namespace,center)
		printAppICP(valuesDeployed)
	}else {
		valuesDeployed=getLastAppInfoICP(icpEnv,app, namespace,center)
		printAppICP(valuesDeployed)
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

        