import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.ICPApiResponse
import com.project.alm.ICPStressDeployment

@Field Map pipelineParams

@Field String groupId 
@Field String artifactId
@Field String version
@Field String classifier
@Field String icpEnv
@Field String icpCenter

@Field ICPStressDeployment icpStressDeployment

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
	icpEnv = params.environmentParam
	icpCenter = params.centerParam
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
		//Environment sobre el qual se ejecuta este tipo de job
		options {
			gitLabConnection('gitlab')
			buildDiscarder(logRotator(numToKeepStr: '3'))
			timestamps()
			timeout(time: 3, unit: 'HOURS')
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
	printOpen("Deploying Stress Test with GAVs: groupId: ${groupId} artifactId: ${artifactId} version: ${version} classifier: ${classifier} in environment ${icpEnv}", EchoLevel.ALL)
	icpStressDeployment = new ICPStressDeployment(groupId, artifactId, version, classifier, icpCenter, icpEnv.toLowerCase(), "${currentBuild.startTimeInMillis}");
	
	def bodyDeploy=[
		az: "AZ"+"${icpCenter}",
		environment: "${icpEnv}",
		values: icpStressDeployment.getChartValuesApps()
	]
	
	printOpen("Deploy value ${bodyDeploy}", EchoLevel.ALL)
	ICPApiResponse response=sendRequestToICPApi("v1/application/PCLD/AB3COR/component/18401/deploy",bodyDeploy,"POST","AB3COR","v1/application/PCLD/AB3COR/component/18401/deploy",true,true, null, null)
	if (response.statusCode>300) {
		throw new Exception("Error deploying stress component")
	}
}

/**
 * Stage 'collectDataStep'
 */
def collectDataStep() {
	printOpen("Collecting data when finish", EchoLevel.ALL)
	String url = "https://jmeter-micro-1." + "${icpEnv.toLowerCase()}" + ".icp-" + "${icpCenter}"+".absis.cloud.lacaixa.es"
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
		az: "AZ"+"${icpCenter}",
		environment: "${icpEnv}",
		values: icpStressDeployment.getChartValuesApps()
	]
	
	printOpen("Deploy value ${bodyDeploy}", EchoLevel.ALL)
	ICPApiResponse response=sendRequestToICPApi("v1/application/PCLD/AB3COR/component/18401/deploy",bodyDeploy,"DELETE","AB3COR","",false,true, null, null)
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

