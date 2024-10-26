import com.project.alm.GlobalVars
import com.project.alm.*
import hudson.scm.SCM
 
def call(pipelineParams = [:], body) {
	 Date date = new Date()
	 
	 properties([
		 //buildDiscarder(logRotator(numToKeepStr: '10')),
		 gitLabConnection('gitlab')
	 ])
	 try {
		 def almJenkinsAgentInfo = almJenkinsAgent(pipelineParams)
		 
		 //podTemplate(almJenkinsAgentInfo){
		 
	 		 //node( almJenkinsAgentInfo.label ){
	 		 node( almJenkinsAgentInfo ){
				 
				 def executionProfile="executionProfile=${pipelineParams ? pipelineParams.get('executionProfile', 'DEFAULT') : 'DEFAULT'}"
				 def proxyHost="proxyHost=${GlobalVars.proxyDigitalscaleHost}"
				 def proxyPort="proxyPort=${GlobalVars.proxyDigitalscalePort}"
				 def https_proxy="https_proxy=${GlobalVars.proxyDigitalscale}"
				 def http_proxy="http_proxy =${GlobalVars.proxyDigitalscale}"
				 
				 withCredentials([usernamePassword(credentialsId: 'ALM_LOGCOLLECTOR_CREDENTIALS', passwordVariable: 'ALM_LOGCOLLECTOR_PASSWORD', usernameVariable: 'ALM_LOGCOLLECTOR_USERNAME')]) {
					 
				 }
				 withCredentials([string(credentialsId: 'cloud-alm-pro-cert-passwd', variable: 'Cloud_PASS_CRED'),
								  file(credentialsId: 'cloud-alm-pro-cert', variable: 'Cloud_CERT_CRED'),
								  usernamePassword(credentialsId: 'ALM_LOGCOLLECTOR_CREDENTIALS', passwordVariable: 'ALM_LOGCOLLECTOR_PASSWORD_CRED', usernameVariable: 'ALM_LOGCOLLECTOR_USERNAME'),
								  usernamePassword(credentialsId: 'IDECUA-JENKINS-USER-TOKEN', passwordVariable: 'AppPortal_PSW', usernameVariable: 'AppPortal_USR')]){
					 
					 def AppPortal_USR_VAR="AppPortal_TEST=${AppPortal_USR}"
					 def Cloud_PASS_VAR="Cloud_PASS=${Cloud_PASS_CRED}"
					 def Cloud_CERT_VAR="Cloud_CERT=${Cloud_CERT_CRED}"				 
					 def ALM_LOGCOLLECTOR_PASSWORD_VAR="ALM_LOGCOLLECTOR_PASSWORD=${ALM_LOGCOLLECTOR_PASSWORD_CRED}"
					 
					 withEnv([proxyHost,
							  proxyPort,
							  Cloud_PASS_VAR,
							  Cloud_CERT_VAR,
							  ALM_LOGCOLLECTOR_PASSWORD_VAR,
							  https_proxy,
							  http_proxy,
							   executionProfile]){
												
							//timestamps{
								//timeout(time: 3, unit: 'HOURS'){
						   stage('CI'){
									printOpen ("Execution ${executionProfile}",EchoLevel.INFO)
									printOpen ("Init pipeline ${env.proxyHost}",EchoLevel.INFO)
									printOpen ("Init executionProfile ${env.executionProfile}",EchoLevel.INFO)
									printOpen ("${env.Cloud_CERT}",EchoLevel.INFO)
									printOpen ("${env.Cloud_PASS}",EchoLevel.INFO)
									def varSCM=checkout scm
									
									env.GIT_URL=varSCM.GIT_URL
									env.GIT_BRANCH=varSCM.GIT_BRANCH
									env.GIT_COMMIT=varSCM.GIT_COMMIT
									env.GIT_PREVIOUS_SUCCESSFUL_COMMIT=varSCM.GIT_PREVIOUS_SUCCESSFUL_COMMIT
	
									body()
									   
							}
					   }				 
				 }
			  }
		 //}   
			 

	 }catch(err) {
		 printOpen("Error Pipeline ${err}",EchoLevel.ERROR)
		 throw err
	 }finally{
		 printOpen ("End pipeline ${date}",EchoLevel.INFO)
	 }
 }
 
 def withoutSCM(pipelineParams = [:], body) {
	 Date date = new Date()
	 
	 properties([
		 buildDiscarder(logRotator(numToKeepStr: '10')),
		 gitLabConnection('gitlab')
	 ])
	 try {
		 
		 def almJenkinsAgentInfo = almJenkinsAgent(pipelineParams)		 

		 //podTemplate(almJenkinsAgentInfo){
		 
			 //node( almJenkinsAgentInfo.label ){
			 node( almJenkinsAgentInfo ){
				 
				 def executionProfile="executionProfile=${pipelineParams ? pipelineParams.get('executionProfile', 'DEFAULT') : 'DEFAULT'}"
				 def proxyHost="proxyHost=${GlobalVars.proxyDigitalscaleHost}"
				 def proxyPort="proxyPort=${GlobalVars.proxyDigitalscalePort}"
				 def https_proxy="https_proxy=${GlobalVars.proxyDigitalscale}"
				 def http_proxy="http_proxy =${GlobalVars.proxyDigitalscale}"
				 
				 withCredentials([string(credentialsId: 'cloud-alm-pro-cert-passwd', variable: 'Cloud_PASS_CRED'),
								  file(credentialsId: 'cloud-alm-pro-cert', variable: 'Cloud_CERT_CRED'),
								  usernamePassword(credentialsId: 'ALM_LOGCOLLECTOR_CREDENTIALS', passwordVariable: 'ALM_LOGCOLLECTOR_PASSWORD_CRED', usernameVariable: 'ALM_LOGCOLLECTOR_USERNAME'),
								  usernamePassword(credentialsId: 'IDECUA-JENKINS-USER-TOKEN', passwordVariable: 'AppPortal_PSW', usernameVariable: 'AppPortal_USR')]){
					 
					 def AppPortal_USR_VAR="AppPortal_TEST=${AppPortal_USR}"
					 def Cloud_PASS_VAR="Cloud_PASS=${Cloud_PASS_CRED}"
					 def Cloud_CERT_VAR="Cloud_CERT=${Cloud_CERT_CRED}"
					 def ALM_LOGCOLLECTOR_PASSWORD_VAR="ALM_LOGCOLLECTOR_PASSWORD=${ALM_LOGCOLLECTOR_PASSWORD_CRED}"
						 
					 withEnv([proxyHost,
							  proxyPort,
							  Cloud_PASS_VAR,
							  Cloud_CERT_VAR,
							  ALM_LOGCOLLECTOR_PASSWORD_VAR,
							  https_proxy,
							  http_proxy,
							   executionProfile]){
												
							//timestamps{
								//timeout(time: 3, unit: 'HOURS'){
						   stage('CI'){
									printOpen ("Execution ${executionProfile}",EchoLevel.INFO)
									printOpen ("Init pipeline ${env.proxyHost}",EchoLevel.INFO)
									printOpen ("Init executionProfile ${env.executionProfile}",EchoLevel.INFO)
									printOpen ("${env.Cloud_CERT}",EchoLevel.INFO)
									printOpen ("${env.Cloud_PASS}",EchoLevel.INFO)
	
									body()
									   
							}
							//}
					   }
				 
				 }
		//	}
   
	 } 

	 }catch(err) {
		 printOpen("Error Pipeline ${err}",EchoLevel.ERROR)
		 throw err
	 }finally{
		 printOpen ("End pipeline ${date}",EchoLevel.INFO)
	 }
 }
