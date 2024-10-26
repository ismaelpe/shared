import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.SampleAppCleanMode

@Field Map pipelineParams
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
		agent {	node (almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
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
            stage("clean-cloud") {
                steps {
                   cleanCloudStep()
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
 * Stage 'cleanCloudStep'
 */
def cleanCloudStep() {
    Date today = new Date().clearTime()
    Date priorDate = today - Integer.parseInt(params.cleanProtypesDays)
    
    currentBuild.displayName = "Cleaning Cloud Prototypes ${params.cleanProtypesDays} apps before ${priorDate}" 
    
    printOpen("Cleaning Cloud with parameters ", EchoLevel.INFO)
    printOpen("Clean Cloud: ${params.cleanProtypesDays}", EchoLevel.INFO)
    printOpen("Clean Prototypes days before: ${priorDate}", EchoLevel.INFO)
    
    cleanCloud(false, Integer.valueOf(params.cleanProtypesDays), SampleAppCleanMode.NONE, params.cleanProtypesDays, true)
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
