import com.project.alm.*
import groovy.json.JsonSlurperClassic
import java.util.ArrayList
import com.project.alm.GitUser


def getApprovers(def listAprovers) {
    return "${listAprovers}"

}

def call(String gitUrl) {
    withCredentials([
            usernamePassword(credentialsId: 'GITLAB_CREDENTIALS', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD'),
            string(credentialsId: 'GITLAB_API_SECRET_TOKEN', variable: 'GITLAB_API_TOKEN')
    ]) {
        String target_branch = "master"
        String title = "New merge request"
        String approvalRuleName = "Default"
        def contentTypeAppJson = 'APPLICATION_JSON'
        def contentTypeAppForm = 'APPLICATION_FORM'
        def consoleLogResponseBody = true
        def ingnoreSslErrors = true
        def customHeaders = [[name: 'Private-Token', value: GITLAB_API_TOKEN], [name: 'Accept', value: "application/json"]]


        // obtener la ruta del repositorio como urlencoded para llamar a la API de
        // gitlab
        def projectPathUrlEncoded = URLEncoder.encode(gitUrl, "UTF-8")

        printOpen("==========================================================", EchoLevel.INFO)
        //step 0 - obtener informacion del repositorio
        printOpen("step 0 - obtener informacion del repositorio", EchoLevel.INFO)
        printOpen("==========================================================", EchoLevel.INFO)
        def projectInfo = httpRequest consoleLogResponseBody: consoleLogResponseBody,
                contentType: contentTypeAppJson,
                httpMode: 'GET',
                ignoreSslErrors: ingnoreSslErrors,
                customHeaders: customHeaders,
                url: "$GlobalVars.gitlabApiDomain$projectPathUrlEncoded",
                httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
                validResponseCodes: '200:300'

        def json = new JsonSlurperClassic().parseText(projectInfo.content)

        def projectId = json.id

        printOpen("==========================================================", EchoLevel.INFO)
		//step 1 - avoid author merge request approvals
        printOpen("Step 1 - avoid author merge request approvals $projectId", EchoLevel.INFO)
        printOpen("==========================================================", EchoLevel.INFO)
		def curlPostStatus1 = httpRequest consoleLogResponseBody: consoleLogResponseBody,				
				httpMode: 'POST',
				ignoreSslErrors: ingnoreSslErrors,
                contentType: contentTypeAppForm,
				customHeaders: customHeaders,
				requestBody: "merge_requests_author_approval=false",
				url: "$GlobalVars.gitlabApiDomain$projectId/approvals",
                httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
				validResponseCodes: '200:300'        
        printOpen("==========================================================", EchoLevel.INFO)
		//step 2 - delete previous push_rules
        printOpen("Step 2 - delete previous push_rules $projectId", EchoLevel.INFO)
        printOpen("==========================================================", EchoLevel.INFO)		
		def curlPostStatus2 = httpRequest consoleLogResponseBody: consoleLogResponseBody,				
				httpMode: 'DELETE',
				ignoreSslErrors: ingnoreSslErrors,
                contentType: contentTypeAppForm,
				customHeaders: customHeaders,
				url: "$GlobalVars.gitlabApiDomain$projectId/push_rule",
                httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
				validResponseCodes: '200:300'
		printOpen("==========================================================", EchoLevel.INFO)				
		//step 3 -  set regular expresion (feature|hotfix|release|configfix)\/[.\w -]*$
        printOpen("Step 3 -  set regular expresion $projectId",EchoLevel.INFO)
        printOpen("==========================================================", EchoLevel.INFO)	
		def curlPostStatus3 = httpRequest consoleLogResponseBody: consoleLogResponseBody,				
				httpMode: 'POST',
				ignoreSslErrors: ingnoreSslErrors,
                contentType: contentTypeAppForm,
				customHeaders: customHeaders,
				requestBody: "branch_name_regex=(feature|hotfix|release|configfix)\\/[.\\w -]*\$",
				url: "$GlobalVars.gitlabApiDomain$projectId/push_rule",
                httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
				validResponseCodes: '200:300'
		printOpen("==========================================================", EchoLevel.INFO)
		//step 4 -  more configuration
        printOpen("Step 4 -  more configuration $projectId", EchoLevel.INFO)		
        printOpen("==========================================================", EchoLevel.INFO)
        def curlRequestStatus = httpRequest consoleLogResponseBody: consoleLogResponseBody,                
                httpMode: 'PUT',
                ignoreSslErrors: ingnoreSslErrors,
                contentType: contentTypeAppForm,
                customHeaders: customHeaders,
                requestBody: "jobs_enabled=true&only_allow_merge_if_pipeline_succeeds=true&only_allow_merge_if_all_discussions_are_resolved=true&default_branch=master&builds_enabled=true&repository_access_level=enabled&builds_access_level=enabled&remove_sorce_branch_after_merge=true",
                url: "$GlobalVars.gitlabApiDomain$projectId",
                httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
                validResponseCodes: '200:300'
        printOpen("==========================================================", EchoLevel.INFO)        
		printOpen("Step 5 - check approval rules members $projectId - $approvalRuleName", EchoLevel.INFO)
        printOpen("==========================================================", EchoLevel.INFO)        
        def defaultApprobalRule = httpRequest consoleLogResponseBody: consoleLogResponseBody,                
                httpMode: 'GET',
                ignoreSslErrors: ingnoreSslErrors,
                contentType: contentTypeAppForm,
                customHeaders: customHeaders,
                url: "$GlobalVars.gitlabApiDomain$projectId/approval_rules",
                httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
                validResponseCodes: '200:300'
        
        json = new JsonSlurperClassic().parseText(defaultApprobalRule.content)
        if (json.size() > 0) {
            printOpen("Buscamos la regla '$approvalRuleName' ", EchoLevel.INFO)

            def result = json.find{it.name == approvalRuleName}

            if (result) {
                printOpen("Regla '$approvalRuleName' encontrada con id $result.id, será eliminada para volverla a crear", EchoLevel.INFO)

                def deleteRules = httpRequest consoleLogResponseBody: consoleLogResponseBody,                
                    httpMode: 'DELETE',
                    ignoreSslErrors: ingnoreSslErrors,
                    contentType: contentTypeAppForm,
                    customHeaders: customHeaders,
                    url: "$GlobalVars.gitlabApiDomain$projectId/approval_rules/$result.id",
                    httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
                    validResponseCodes: '200:300'
            }
        }        
        printOpen("==========================================================", EchoLevel.INFO)  
        //step 6 -  set members
        printOpen("Step 6 - set members $projectId for approval rule $approvalRuleName", EchoLevel.INFO)
        printOpen("==========================================================", EchoLevel.INFO)		
        def members = httpRequest consoleLogResponseBody: consoleLogResponseBody,                
                httpMode: 'GET',
                ignoreSslErrors: ingnoreSslErrors,
                contentType: contentTypeAppForm,
                customHeaders: customHeaders,
                url: "$GlobalVars.gitlabApiDomain$projectId/members/all?page=1&per_page=400",
                httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
                validResponseCodes: '200:300'

        json = new JsonSlurperClassic().parseText(members.content)

        if (json.size() > 0) {
            printOpen("Recogeremos los usuarios", EchoLevel.INFO)
            def aprovers = new ArrayList()
            GitUser gitUser
            //Nos interesan solo los de mayor nivel.... el resto no interesan
            json.each {
                //Valor 50
                if (it.state == 'active' && it.access_level > 20) {
                    printOpen("Agregando $it.name - $it.username valor $it.state level $it.access_level", EchoLevel.INFO)
                    aprovers.add(it.id)
                }
            }

            if (aprovers.size() > 0) {
                def toJson = groovy.json.JsonOutput.toJson([
                        name                                : approvalRuleName,
                        approvals_required                  : 1,
                        user_ids                            : aprovers,
                        applies_to_all_protected_branches   : true
                ])

                def aproversResult = httpRequest consoleLogResponseBody: consoleLogResponseBody,                        
                        httpMode: 'POST',
                        ignoreSslErrors: ingnoreSslErrors,
                        contentType: contentTypeAppJson,
                        customHeaders: customHeaders,
                        requestBody: toJson,
                        url: "$GlobalVars.gitlabApiDomain$projectId/approval_rules",
                        httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
                        validResponseCodes: '200:300'
            }            
        }
        printOpen("==========================================================", EchoLevel.INFO)
		//Recreacion webhook
		//Necesitamos el id de proyecto
		//projectId
		validateWebhookToJnkmsvAndSet(projectId,gitUrl)
    }

}

def validateWebhookToJnkmsvAndSet(def projectId, def projectName) {
	
	def getJenkinsService = httpRequest consoleLogResponseBody: true,
                        contentType: 'APPLICATION_JSON',
                        httpMode: 'GET',
                        ignoreSslErrors: true,
                        customHeaders: [[name: 'Private-Token', value: "${GITLAB_API_TOKEN}"], [name: 'Accept', value: "application/json"]],
                        //url: "${GlobalVars.gitlabApiDomain}${projectId}/integrations/jenkins",
						url: "${GlobalVars.gitlabApiDomain}${projectId}/services/jenkins",
                        httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
                        validResponseCodes: '200:499'
						
	def jsonJenkinsService = new JsonSlurperClassic().parseText(getJenkinsService.content)
	
	if (projectName!=null) {
		projectName=projectName.substring(4)
	}
			
	if ((jsonJenkinsService.status >= 400 || jsonJenkinsService.status <= 499) || jsonJenkinsService.id == null) {
		printOpen("---------projectName ${projectName}----------", EchoLevel.INFO)
		//No tiene vinculacion lo dejamos en paz
		printOpen("---------No tiene jenkins service ${jsonJenkinsService}----------", EchoLevel.INFO)
		
		def body = [
			jenkins_url: GlobalVars.URL_REDIRECTORA_JENKINS,
			project_name: projectName,
			username: JNKMSV_USR,
			password: JNKMSV_PSW,
			push_events: true,
			merge_requests_events: true
		  ]
		  def toJson = {
			  input ->
				  groovy.json.JsonOutput.toJson(input)
		  }
		  getJenkinsService = httpRequest consoleLogResponseBody: true,
						  contentType: 'APPLICATION_JSON',
						httpMode: 'PUT',
						ignoreSslErrors: true,
						 customHeaders: [[name: 'Private-Token', value: "${GITLAB_API_TOKEN}"], [name: 'Accept', value: "application/json"]],
						requestBody: toJson(body),
						url: "${GlobalVars.gitlabApiDomain}${projectId}/services/jenkins",
						httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
						validResponseCodes: '200:300'
		  return true
							
	}else {
		def pathOnJenkins=projectName-(GlobalVars.gitlabDomain)
		
		printOpen("---------Path ${pathOnJenkins}----------", EchoLevel.INFO)
				
		if (pathOnJenkins.startsWith('cbk')) {
			pathOnJenkins=pathOnJenkins.substring(4)
		}
		printOpen("---------Path ${pathOnJenkins}----------", EchoLevel.INFO)
		//Revisamos si se tiene que dar de alta
		if (!jsonJenkinsService.properties.jenkins_url.contains('jnkmsv')) {
			printOpen("Setteamos el valor ", EchoLevel.INFO)
			def body = [
				jenkins_url: GlobalVars.URL_REDIRECTORA_JENKINS,
				project_name: jsonJenkinsService.properties.project_name,
				username: JNKMSV_USR,
				password: JNKMSV_PSW,
				push_events: true,
				merge_requests_events: true
			  ]
			  def toJson = {
				  input ->
					  groovy.json.JsonOutput.toJson(input)
			  }
			  getJenkinsService = httpRequest consoleLogResponseBody: true,
			  				contentType: 'APPLICATION_JSON',
							httpMode: 'PUT',
							ignoreSslErrors: true,
				 		    customHeaders: [[name: 'Private-Token', value: "${GITLAB_API_TOKEN}"], [name: 'Accept', value: "application/json"]],
							requestBody: toJson(body),
							url: "${GlobalVars.gitlabApiDomain}${projectId}/integrations/jenkins",
							httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
							validResponseCodes: '200:300'
			  return true
			  
		}else {
			printOpen("El valor ya esta asignado contra la redirectora ", EchoLevel.INFO)
		}
	}
	return false
						
}
