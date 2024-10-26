import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.CloudApiResponse
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.CloudDeployStructure
import com.project.alm.DeployStructure
import groovy.json.JsonSlurperClassic
import com.project.alm.GarAppType
import com.project.alm.CloudStateUtility
import com.project.alm.ArtifactType
import com.project.alm.CloudWorkflowStates
import com.project.alm.CloudAppResources

import com.project.alm.Cloudk8sComponentInfo
import com.project.alm.Cloudk8sComponentInfoMult

import com.project.alm.Cloudk8sActualStatusInfo
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
