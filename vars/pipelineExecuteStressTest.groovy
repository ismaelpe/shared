import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.CloudApiResponse
import com.project.alm.CloudStressDeployment

@Field Map pipelineParams

@Field String groupId 
@Field String artifactId
@Field String version
@Field String classifier
@Field String cloudEnv
@Field String cloudCenter

@Field CloudStressDeployment cloudStressDeployment

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

	groupId = params.groupId
	artifactId = params.artifactId
	version = params.version
	classifier = params.classifier
	cloudEnv = params.environmentParam
	cloudCenter = params.centerParam
    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
		//Environment sobre el qual se ejecuta este tipo de job
		options {
			gitLabConnection('gitlab')
			buildDiscarder(logRotator(numToKeepStr: '3'))
			timestamps()
			timeout(time: 3, unit: 'HOURS')
		}
		environment {
			AppPortal = credentials('IDECUA-JENKINS-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
			http_proxy = "${GlobalVars.proxyDigitalscale}"
			https_proxy = "${GlobalVars.proxyDigitalscale}"
			proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
			proxyPort = "${GlobalVars.proxyDigitalscalePort}"
		}
		stages {
			stage('init') {
				steps {
					initStep()
				}
			}
			stage('deploy') {
				steps {
					deployStep()
				}
			}
			stage('collect-data'){
				steps {
					collectDataStep()
				}
			}
			stage('undeploy'){
				steps {
					undeployStep()
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
 * Stage 'initStep'
 */
def initStep() {
	initGlobalVars(pipelineParams)
	currentBuild.displayName = "Execute_Stress_Test_${env.BUILD_ID}"
}

/**
 * Stage 'deployStep'
 */
def deployStep() {
	printOpen("Deploying Stress Test with GAVs: groupId: ${groupId} artifactId: ${artifactId} version: ${version} classifier: ${classifier} in environment ${cloudEnv}", EchoLevel.ALL)
	cloudStressDeployment = new CloudStressDeployment(groupId, artifactId, version, classifier, cloudCenter, cloudEnv.toLowerCase(), "${currentBuild.startTimeInMillis}");
	
	def bodyDeploy=[
		az: "AZ"+"${cloudCenter}",
		environment: "${cloudEnv}",
		values: cloudStressDeployment.getChartValuesApps()
	]
	
	printOpen("Deploy value ${bodyDeploy}", EchoLevel.ALL)
	CloudApiResponse response=sendRequestToCloudApi("v1/application/PCLD/AB3COR/component/18401/deploy",bodyDeploy,"POST","AB3COR","v1/application/PCLD/AB3COR/component/18401/deploy",true,true, null, null)
	if (response.statusCode>300) {
		throw new Exception("Error deploying stress component")
	}
}

/**
 * Stage 'collectDataStep'
 */
def collectDataStep() {
	printOpen("Collecting data when finish", EchoLevel.ALL)
	String url = "https://jmeter-micro-1." + "${cloudEnv.toLowerCase()}" + ".cloud-" + "${cloudCenter}"+".alm.cloud.digitalscale.es"
	validateStressTestIsFinish(url)
	//Download csv and target.zip
	String fileOutput= CopyGlobalLibraryScript('','.','resultsMetrics.csv')
	def command="curl -L -k --write-out '%{http_code}' -o ${fileOutput} -k -s -X GET ${url}/jmeter/resultsMetrics --connect-timeout ${GlobalVars.DEFAULT_HTTP_TIMEOUT} "
	sh(script: command,returnStdout: true)
	printOpen("Tenemos que hacer sleep de 60 segundos", EchoLevel.ALL)
	sleep 60
	printOpen("Finalizamos sleep de 60 segundos", EchoLevel.ALL)
	fileOutput= CopyGlobalLibraryScript('','.','target.zip')
	command="curl -L -k --write-out '%{http_code}' -o ${fileOutput} -k -s -X GET ${url}/jmeter/target --connect-timeout ${GlobalVars.DEFAULT_HTTP_TIMEOUT} "
	sh(script: command,returnStdout: true)
	
	fileOutput= CopyGlobalLibraryScript('','.','jmeter.result')
	command="curl -L -k --write-out '%{http_code}' -o ${fileOutput} -k -s -X GET ${url}/jmeter/result --connect-timeout ${GlobalVars.DEFAULT_HTTP_TIMEOUT} "
	sh(script: command,returnStdout: true)
	
	zip zipFile: 'workspace.zip', archive: true	
}

/**
 * Stage 'undeployStep'
 */
def undeployStep() {
	printOpen("Undeploy component", EchoLevel.ALL)
	def bodyDeploy=[
		az: "AZ"+"${cloudCenter}",
		environment: "${cloudEnv}",
		values: cloudStressDeployment.getChartValuesApps()
	]
	
	printOpen("Deploy value ${bodyDeploy}", EchoLevel.ALL)
	CloudApiResponse response=sendRequestToCloudApi("v1/application/PCLD/AB3COR/component/18401/deploy",bodyDeploy,"DELETE","AB3COR","",false,true, null, null)
	if (response.statusCode>300) {
		throw new Exception("Error deploying stress component")
	}
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
	printOpen("sucess", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
	printOpen("Pipeline has failed", EchoLevel.ERROR)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
	cleanWs()
	cleanWorkspace()
}

