import com.project.alm.GlobalVars
import com.project.alm.ICPApiResponse
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.ICPDeployStructure
import com.project.alm.DeployStructure
import groovy.json.JsonSlurperClassic
import com.project.alm.GarAppType
import com.project.alm.ICPStateUtility
import com.project.alm.ArtifactType
import com.project.alm.ICPWorkflowStates
import com.project.alm.ICPAppResources

import com.project.alm.ICPk8sComponentInfo
import com.project.alm.ICPk8sComponentInfoMult

import com.project.alm.ICPk8sActualStatusInfo
import com.project.alm.BmxUtilities
import com.project.alm.BranchType
import com.project.alm.ArtifactSubType
import com.project.alm.EchoLevel
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
