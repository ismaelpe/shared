import com.caixabank.absis3.*
import groovy.json.JsonSlurperClassic

def call(PomXmlStructure pomXml, PipelineData pipelineData, boolean mergedMR = false) {

    def projectId = gitlabApiGetProjectId(pipelineData, pomXml)

    GitlabAPIResponse mergeRequests =
        sendRequestToGitLabAPI(pipelineData, pomXml,
            [
                url: "${GlobalVars.gitlabApiDomain}${projectId}/merge_requests?state=merged&target_branch=${pipelineData.branchStructure.branchName}&source_branch=${pipelineData.originPushBranchToMaster}",
                contentType: "application/x-www-form-urlencoded"
            ]
        )

    if (mergeRequests.asJson != null) {

        return mergeRequests.asJson[0].title

    } else {

        return null

    }

}
