import groovy.transform.Field
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.ICPApiResponse
import com.caixabank.absis3.SampleAppCleanMode
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.Utilities

import java.util.Map

@Field Map pipelineParams

@Field boolean successPipeline = true

@Field String app  = "${appnameParam}"
@Field String buildId  = "${buildIdParam}"
@Field String namespace  = "${namespaceParam}"

@Field Map valuesDeployed = null

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

	// las variables que se obtienen como parametro del job no es necesario
	// redefinirlas, se hace por legibilidad del codigo

	successPipeline = true
	
	app = params.appnameParam
	buildId = params.buildIdParam
	namespace = params.namespaceParam
	
	valuesDeployed = null
    
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
			stage("get-app-log") {
				steps {
                    getAppLogStep()
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
 * Stage 'getAppLogStep'
 */
def getAppLogStep() {
    try {
        currentBuild.displayName = "Get ${app} build log. Build Number ${buildId}"
        ICPApiResponse response = getBuildLog(app, namespace, buildId)

        if (response.statusCode>=200 && response.statusCode<300) {

            if (response.body!=null && response.body.size()>=1) {
                printOpen("*******************************************************************************************************************************************\n" +
                    "El log es de ${response.body}\n" +
                    "*******************************************************************************************************************************************",
                    EchoLevel.INFO)
            }


        } else {
            printOpen("APP NOT FOUND", EchoLevel.INFO)
        }

    } catch(Exception e) {

        printOpen(Utilities.prettyException(e), EchoLevel.ERROR)

    }
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    successPipeline = false
    printOpen("Is pipeline unsuccessful? ${successPipeline}", EchoLevel.ERROR)  
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = true
    printOpen("Is pipeline successful? ${successPipeline}", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}
