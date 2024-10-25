import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.ICPApiResponse
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.ICPDeployStructure
import com.caixabank.absis3.DeployStructure
import groovy.json.JsonSlurperClassic
import com.caixabank.absis3.GarAppType
import com.caixabank.absis3.ICPStateUtility
import com.caixabank.absis3.ArtifactType
import com.caixabank.absis3.ICPWorkflowStates
import com.caixabank.absis3.ICPAppResources

import com.caixabank.absis3.ICPk8sComponentInfo
import com.caixabank.absis3.ICPk8sComponentInfoMult

import com.caixabank.absis3.ICPk8sActualStatusInfo
import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.BranchType
import com.caixabank.absis3.ArtifactSubType
import com.caixabank.absis3.EchoLevel
import hudson.Functions
import java.io.NotSerializableException

def call(String url) {
	
	printOpen("Checking the url ${url} ...", EchoLevel.DEBUG)
	
	String fileOutput= CopyGlobalLibraryScript('',null,'outputCommand.json')
	def command="curl -L -k --write-out '%{http_code}' -o ${fileOutput} -k -s -X GET ${url}/actuator/health --connect-timeout ${GlobalVars.ACTUATOR_HEALTH_TIMEOUT} "
	def microIsUp=false
	
	timeout(GlobalVars.ACTUATOR_HEALTH_RETRY_CYCLE_TIMEOUT) {
		waitUntil(initialRecurrencePeriod: 15000) {
			def responseStatusCode=null
			retry(GlobalVars.ACTUATOR_HEALTH_RETRY_CYCLE_MAX_RETRIES) {
				responseStatusCode= sh(script: command,returnStdout: true)
                sh "cat ${fileOutput}"
			}

			int statusCode
			statusCode = responseStatusCode as Integer

			if (statusCode>=200 && statusCode<300) {
				String contentResponse= sh(script: "cat ${fileOutput}", returnStdout:true )

				def json=new JsonSlurperClassic().parseText(contentResponse)
				
				if (json.status=='UP') {
					microIsUp=true
					return true
				}else return false
			}else return false
		}
	}
	
	if (microIsUp) {
	   printOpen("The application with url ${url} is UP", EchoLevel.DEBUG)
	} else {
	   printOpen("The application with url ${url} is DOWN", EchoLevel.DEBUG)
	}
	
	return microIsUp
	
}
