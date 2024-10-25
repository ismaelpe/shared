import com.caixabank.absis3.*
import groovy.json.JsonSlurperClassic

def call(PipelineData pipelineData, PomXmlStructure pomXml, String targetBranch, boolean approve = false, boolean mergeWhenFinish = false, boolean restricted = false, String customMessage = "", String customTitle = null, boolean mergeOnClose = false) {

    printOpen("Creating a merge request automatically approve ${approve} mergeWhenFinish ${mergeWhenFinish} restricted ${restricted} customMessage ${customMessage}", EchoLevel.ALL)

	String mrTitle = "Merge request. End of version ${pomXml.artifactVersion}. Artifact ${pomXml.artifactName}"
	if (customTitle!=null) {
		mrTitle=customTitle
	}

    def projectId = gitlabApiGetProjectId(pipelineData, pomXml)

    GitlabAPIResponse createdMergeRequestInfo = null
    GitlabAPIResponse aproversResult = null

    if ( restricted ) {
        //Faltaria settear a los encargados de la validacion de scripts
        createdMergeRequestInfo = sendRequestToGitLabAPI(pipelineData, pomXml, [
            url: "${GlobalVars.gitlabApiDomain}${projectId}/merge_requests",
            contentType: "application/x-www-form-urlencoded",
            isJsonInputData: false,
            requestBody: "approversremove_source_branch=true&source_branch=${pipelineData.branchStructure.branchName}&title=${mrTitle}&target_branch=${targetBranch}&description=${customMessage}",
            validResponseCodes: "200:300,409"
        ])

        def mergeRequestId = getMergeRequestIdFromResponse(createdMergeRequestInfo)
        GitlabAPIResponse getGroups = sendRequestToGitLabAPI(pipelineData, pomXml, [
            url: "${GlobalVars.gitlabApiDomainRaw}/groups?search=aquitectura-tecnica",
            contentType: "application/x-www-form-urlencoded",
        ])
        
        def groupArquitecturaTecnica = getGroups.asJson[0].id
        def bodyJson = groovy.json.JsonOutput.toJson([
                name                                : "Default BBDD",
                approvals_required                  : 1,
                user_ids                            : [31],
                group_ids                           : [groupArquitecturaTecnica],
                applies_to_all_protected_branches   : true
        ])
        
        aproversResult = sendRequestToGitLabAPI(pipelineData, pomXml, [                    
            url: "$GlobalVars.gitlabApiDomain$projectId/merge_requests/$mergeRequestId/approval_rules",
            contentType: "application/x-www-form-urlencoded",
            isJsonInputData: false,
            httpMode: "POST",
            requestBody: bodyJson
        ])
    } else {
        printOpen("source ${pipelineData.branchStructure.branchName} target ${targetBranch} Title ${mrTitle} ", EchoLevel.ALL)

        createdMergeRequestInfo = sendRequestToGitLabAPI(pipelineData, pomXml,[
            url: "${GlobalVars.gitlabApiDomain}${projectId}/merge_requests",
            contentType: "application/x-www-form-urlencoded",
            isJsonInputData: false,
            requestBody: "remove_source_branch=true&source_branch=${pipelineData.branchStructure.branchName}&title=${mrTitle}&target_branch=${targetBranch}",
            validResponseCodes: "200:300,409"
        ])
    }

    printOpen("Merge request created or using existing one", EchoLevel.ALL)


    if (approve) {

        printOpen("Approving merge request automatically, configure merge request author can approve", EchoLevel.ALL)

        def mergeRequestId = getMergeRequestIdFromResponse(createdMergeRequestInfo)

        approveMergeRequestAndMerge(pipelineData, pomXml, projectId, mergeRequestId, aproversResult)
    }

    if(mergeOnClose){
        statusApproval(pipelineData, pomXml, projectId)
    }

}

def mergeOnClose(PipelineData pipelineData, PomXmlStructure pomXml, String targetBranch) {
    mergeRequestToMaster(pipelineData, pomXml, targetBranch, false, false, false, "", null, mergeOnClose = true)
}

private void approveMergeRequestAndMerge(PipelineData pipelineData, PomXmlStructure pomXml, def projectId, def mergeRequestId, def aproversResult = null) {

    GitlabAPIResponse allowApprovedSameUser = sendRequestToGitLabAPI(pipelineData, pomXml, [
        url: "${GlobalVars.gitlabApiDomain}${projectId}/approvals",
        contentType: "application/x-www-form-urlencoded",
        isJsonInputData: false,
        requestBody: "merge_requests_author_approval=true"
    ])

    GitlabAPIResponse approveMergeRequestInfo = sendRequestToGitLabAPI(pipelineData, pomXml, [
        url: "${GlobalVars.gitlabApiDomain}${projectId}/merge_requests/${mergeRequestId}/approve",
        contentType: "application/x-www-form-urlencoded",
        httpMode: "POST"
    ])

    GitlabAPIResponse mergeRequestInfo = sendRequestToGitLabAPI(pipelineData, pomXml, [
        url: "${GlobalVars.gitlabApiDomain}${projectId}/merge_requests/${mergeRequestId}",
        contentType: "application/x-www-form-urlencoded"
    ])

    //El status lo ponemos a true
    statusApproval(pipelineData, pomXml, projectId)

    //Recogemos la info
    mergeRequestInfo = sendRequestToGitLabAPI(pipelineData, pomXml, [
        url: "${GlobalVars.gitlabApiDomain}${projectId}/merge_requests/${mergeRequestId}",
        contentType: "application/x-www-form-urlencoded"
    ])

    //Hacemos el merge de la rama
    GitlabAPIResponse acceptMergeRequestInfo = sendRequestToGitLabAPI(pipelineData, pomXml, [
        url: "${GlobalVars.gitlabApiDomain}${projectId}/merge_requests/${mergeRequestId}/merge",
        contentType: "application/x-www-form-urlencoded",
        httpMode: "PUT",
        isJsonInputData: false,
        requestBody: "should_remove_source_branch=true&merge_when_pipeline_succeeds=true"
    ])

    if (aproversResult) {
        //Volvemos a dejar los usuarios como estaban en approvals
        allowApprovedSameUser = sendRequestToGitLabAPI(pipelineData, pomXml, [
            url: "$GlobalVars.gitlabApiDomain$projectId/approval_rules/$aproversResult.asJson.id",
            contentType: "application/x-www-form-urlencoded",
            httpMode: "DELETE",
            isJsonInputData: false
        ])
    }
}

private void statusApproval(PipelineData pipelineData, PomXmlStructure pomXml, projectId) {
    if (env.gitlabMergeRequestLastCommit != null) {
        GitlabAPIResponse updateStatus = sendRequestToGitLabAPI(pipelineData, pomXml, [
            url        : "${GlobalVars.gitlabApiDomain}${projectId}/statuses/${env.gitlabMergeRequestLastCommit}?state=success&target_url=${env.RUN_DISPLAY_URL}&name=IntegrationJenkinsPipeline",
            contentType: "application/x-www-form-urlencoded",
            httpMode   : "POST"
        ])
    } else {
        printOpen("We have not any commit to set status success", EchoLevel.ALL)
        //Nos falta el commit id para poder continuar
        setStatusMerge(pipelineData, pomXml, projectId)
    }
}

private void setStatusMerge(PipelineData pipelineData, PomXmlStructure pomXml, def projectId) {

    def targetBranch = pipelineData.branchStructure.branchName
    def targetEncoded = URLEncoder.encode("${targetBranch}", "UTF-8")

    GitlabAPIResponse commitsList =
        sendRequestToGitLabAPI(pipelineData, pomXml,
            [
                url: "${GlobalVars.gitlabApiDomain}${projectId}/repository/commits/${targetEncoded}",
                contentType: "application/x-www-form-urlencoded"
            ]
        )

    if (commitsList.asJson != null) {
        def commitId = commitsList.asJson.id

        printOpen("El idenfiticador del commit es el siguiente ${commitId}", EchoLevel.ALL)

        GitlabAPIResponse updateStatus =
            sendRequestToGitLabAPI(pipelineData, pomXml,
                [
                    url: "${GlobalVars.gitlabApiDomain}${projectId}/statuses/${commitId}?state=success&target_url=${env.RUN_DISPLAY_URL}&name=IntegrationJenkinsPipeline",
                    contentType: "application/x-www-form-urlencoded",
                    httpMode: "POST"
                ]
            )
    }

    printOpen("Los commits asociados son de ${commitsList}", EchoLevel.ALL)
}

private String getMergeRequestIdFromResponse(GitlabAPIResponse response) {

    if (response.status == 409) {

        def msg = response?.content
		
		if (msg==null && response.asJson!=null) {
			msg=response.asJson
			if (msg?.message!=null) { 
				
				if ( msg?.message instanceof List) {
					msg=msg?.message?.get(0)
				}else {
					msg=msg?.message
				}
				
			}
		}
		
        def start = msg?.lastIndexOf("!")
        def end = msg?.lastIndexOf("\"")
		
		if ( start == -1 ){
			return msg	
		}
		
		if ( end == -1 ) {
			return msg?.substring(start+1, msg?.length())
		}

        return msg?.substring(start+1, end)

    } else {

        return response.asJson.iid

    }

}
