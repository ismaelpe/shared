import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GitlabAPIResponse
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure

def call(String status) {

    updateCommitStatus(null, null, status)

}

def call(PipelineData pipelineData, String status) {

    updateCommitStatus(pipelineData, null, status)

}

def call(PipelineData pipelineData, PomXmlStructure pomXml, String status) {

    updateCommitStatus(pipelineData, pomXml, status)

}

private void updateCommitStatus(PipelineData pipelineData, PomXmlStructure pomXml, String status) {

    printOpen("Update commit status", EchoLevel.INFO)

    if (env.gitlabActionType == 'MERGE' && env.gitlabMergeRequestLastCommit != null) {
        // obtener la ruta del repositorio como urlencoded para llamar a la API de
        // gitlab
        def projectId = env.gitlabMergeRequestTargetProjectId
        def gitCommit = env.gitlabMergeRequestLastCommit

        GitlabAPIResponse updateStatus =
            sendRequestToGitLabAPI(pipelineData, pomXml,
                [
                    url: "${GlobalVars.gitlabApiDomain}${projectId}/statuses/${gitCommit}?state=${status}&target_url=${env.RUN_DISPLAY_URL}&name=IntegrationJenkinsPipeline",
                    contentType: "application/json",
                    httpMode: "POST",
					
                ]
            )

		validateTheResponseCode(updateStatus,status)

    } else {
        printOpen("No Merge ${env.gitlabActionType} or No Commit ${env.gitlabMergeRequestLastCommit} ", EchoLevel.ERROR)
    }

}

private void validateTheResponseCode(GitlabAPIResponse updateStatus,status) {
	
	if (updateStatus.status==400) {
		def messageError400=""
		if (updateStatus.asJson.message!=null) {
			messageError400=updateStatus.asJson.message		
		}
		//Es un 400
		if (status.equals('running') && messageError400.indexOf(':run from :running')!=-1) {
			printOpen("Nos comemos el posible error, esta todo OK", EchoLevel.ALL)
		}else {
			throw new Exception("Error ocurrido en el update ${updateStatus.asJson}")
		}
	}
}
