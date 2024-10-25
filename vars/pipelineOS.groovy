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
		 def absisJenkinsAgentInfo = absisJenkinsAgent(pipelineParams)
		 
		 //podTemplate(absisJenkinsAgentInfo){
		 
	 		 //node( absisJenkinsAgentInfo.label ){
	 		 node( absisJenkinsAgentInfo ){
				 
				 def executionProfile="executionProfile=${pipelineParams ? pipelineParams.get('executionProfile', 'DEFAULT') : 'DEFAULT'}"
				 def proxyHost="proxyHost=${GlobalVars.proxyCaixaHost}"
				 def proxyPort="proxyPort=${GlobalVars.proxyCaixaPort}"
				 def https_proxy="https_proxy=${GlobalVars.proxyCaixa}"
				 def http_proxy="http_proxy =${GlobalVars.proxyCaixa}"
				 
				 withCredentials([usernamePassword(credentialsId: 'ALM_LOGCOLLECTOR_CREDENTIALS', passwordVariable: 'ALM_LOGCOLLECTOR_PASSWORD', usernameVariable: 'ALM_LOGCOLLECTOR_USERNAME')]) {
					 
				 }
				 withCredentials([string(credentialsId: 'icp-absis3-pro-cert-passwd', variable: 'ICP_PASS_CRED'),
								  file(credentialsId: 'icp-absis3-pro-cert', variable: 'ICP_CERT_CRED'),
								  usernamePassword(credentialsId: 'ALM_LOGCOLLECTOR_CREDENTIALS', passwordVariable: 'ALM_LOGCOLLECTOR_PASSWORD_CRED', usernameVariable: 'ALM_LOGCOLLECTOR_USERNAME'),
								  usernamePassword(credentialsId: 'IDECUA-JENKINS-USER-TOKEN', passwordVariable: 'GPL_PSW', usernameVariable: 'GPL_USR')]){
					 
					 def GPL_USR_VAR="GPL_TEST=${GPL_USR}"
					 def ICP_PASS_VAR="ICP_PASS=${ICP_PASS_CRED}"
					 def ICP_CERT_VAR="ICP_CERT=${ICP_CERT_CRED}"				 
					 def ALM_LOGCOLLECTOR_PASSWORD_VAR="ALM_LOGCOLLECTOR_PASSWORD=${ALM_LOGCOLLECTOR_PASSWORD_CRED}"
					 
					 withEnv([proxyHost,
							  proxyPort,
							  ICP_PASS_VAR,
							  ICP_CERT_VAR,
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
									printOpen ("${env.ICP_CERT}",EchoLevel.INFO)
									printOpen ("${env.ICP_PASS}",EchoLevel.INFO)
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
		 
		 def absisJenkinsAgentInfo = absisJenkinsAgent(pipelineParams)		 

		 //podTemplate(absisJenkinsAgentInfo){
		 
			 //node( absisJenkinsAgentInfo.label ){
			 node( absisJenkinsAgentInfo ){
				 
				 def executionProfile="executionProfile=${pipelineParams ? pipelineParams.get('executionProfile', 'DEFAULT') : 'DEFAULT'}"
				 def proxyHost="proxyHost=${GlobalVars.proxyCaixaHost}"
				 def proxyPort="proxyPort=${GlobalVars.proxyCaixaPort}"
				 def https_proxy="https_proxy=${GlobalVars.proxyCaixa}"
				 def http_proxy="http_proxy =${GlobalVars.proxyCaixa}"
				 
				 withCredentials([string(credentialsId: 'icp-absis3-pro-cert-passwd', variable: 'ICP_PASS_CRED'),
								  file(credentialsId: 'icp-absis3-pro-cert', variable: 'ICP_CERT_CRED'),
								  usernamePassword(credentialsId: 'ALM_LOGCOLLECTOR_CREDENTIALS', passwordVariable: 'ALM_LOGCOLLECTOR_PASSWORD_CRED', usernameVariable: 'ALM_LOGCOLLECTOR_USERNAME'),
								  usernamePassword(credentialsId: 'IDECUA-JENKINS-USER-TOKEN', passwordVariable: 'GPL_PSW', usernameVariable: 'GPL_USR')]){
					 
					 def GPL_USR_VAR="GPL_TEST=${GPL_USR}"
					 def ICP_PASS_VAR="ICP_PASS=${ICP_PASS_CRED}"
					 def ICP_CERT_VAR="ICP_CERT=${ICP_CERT_CRED}"
					 def ALM_LOGCOLLECTOR_PASSWORD_VAR="ALM_LOGCOLLECTOR_PASSWORD=${ALM_LOGCOLLECTOR_PASSWORD_CRED}"
						 
					 withEnv([proxyHost,
							  proxyPort,
							  ICP_PASS_VAR,
							  ICP_CERT_VAR,
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
									printOpen ("${env.ICP_CERT}",EchoLevel.INFO)
									printOpen ("${env.ICP_PASS}",EchoLevel.INFO)
	
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
