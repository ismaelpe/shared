import groovy.transform.Field
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.SampleAppCleanMode
import com.caixabank.absis3.GlobalVars
import java.util.Date

@Field boolean successPipeline

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters
    
    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo

    successPipeline = true
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
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
            stage("clean-icp") {
                steps {
                   cleanIcpStep()
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
 * Stage 'cleanIcpStep'
 */
def cleanIcpStep() {
    Date today = new Date().clearTime()
    Date priorDate = today - Integer.parseInt(params.cleanEdenDays)
    
    currentBuild.displayName = "Cleaning ICP EDEN ${params.cleanEden} apps before ${priorDate}" 

    printOpen("Cleaning ICP with parameters ", EchoLevel.INFO)
    printOpen("Clean Eden: ${params.cleanEden}", EchoLevel.INFO)
    printOpen("Clean Eden days before: ${params.cleanEdenDays}", EchoLevel.INFO)
    printOpen("Clean Sample app mode: ${params.sampleAppCleanMode}", EchoLevel.INFO)

    SampleAppCleanMode sampleAppCleanMode = params.sampleAppCleanMode
    cleanICP(params.cleanEden, Integer.valueOf(params.cleanEdenDays), sampleAppCleanMode, params.cleanEdenDays)
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

