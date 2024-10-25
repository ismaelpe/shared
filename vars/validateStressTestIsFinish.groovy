import com.caixabank.absis3.EchoLevel
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
