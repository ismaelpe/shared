import com.project.alm.EchoLevel
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
import hudson.Functions
import java.io.NotSerializableException

def call(String url) {
	
	String fileOutput= CopyGlobalLibraryScript('',null,'outputCommand.json')
	def command="curl -L -k --write-out '%{http_code}' -o ${fileOutput} -k -s -X GET ${url}/jmeter/finish --connect-timeout ${GlobalVars.DEFAULT_HTTP_TIMEOUT} "
	def finishStressTest
	timeout(GlobalVars.STRESS_RETRY_CYCLE_TIMEOUT) {
		waitUntil(initialRecurrencePeriod: 15000) {
			def responseStatusCode= sh(script: command,returnStdout: true)
            sh "cat ${fileOutput}"
			printOpen("Response ${responseStatusCode}", EchoLevel.ALL)
			int statusCode = responseStatusCode as Integer

			if (statusCode>=200 && statusCode<300) {
				String contentResponse= sh(script: "cat ${fileOutput}", returnStdout:true )
				if (contentResponse.contains('true')) {
					finishStressTest = true
				}else {
					finishStressTest = false
				}
			}else {
				 finishStressTest = false
			}
			return finishStressTest
		}
	}
	
	return finishStressTest
	
}
