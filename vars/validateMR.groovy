import com.project.alm.*
import groovy.json.JsonSlurperClassic
import com.project.alm.PipelineBehavior

def call(PipelineData pipelineData, PomXmlStructure pomXml) {

    printOpen("---------antes de hacer merge request-----------", EchoLevel.ALL)

    if (!pipelineData.branchStructure.branchType == BranchType.FEATURE) return false

    boolean weAreInMerge =
        env.gitlabActionType == 'PUSH' || env.gitlabActionType == null

    // projectId not available in merge. We'll have to ask API
    def projectId = weAreInMerge ?
        gitlabApiGetProjectId(pipelineData, pomXml) :
        env.gitlabMergeRequestTargetProjectId

    if (weAreInMerge) {

        def targetBranch = URLEncoder.encode(pipelineData.branchStructure.branchName, "UTF-8")

        GitlabAPIResponse mergeRequests =
            sendRequestToGitLabAPI(pipelineData, pomXml,
                [
                    url: "${GlobalVars.gitlabApiDomain}${projectId}/merge_requests?state=opened&source_branch=${targetBranch}",
                    contentType: "application/json"
                ]
        )

        if (mergeRequests.asJson.size() > 0) {

            printOpen("Existen MR abiertas", EchoLevel.DEBUG)
            if (mergeRequests.asJson[0].work_in_progress) {
                printOpen("La MR esta en WIP", EchoLevel.DEBUG)
                return PipelineBehavior.PUSH_OPENED_MR_WIP
            } else {
                return PipelineBehavior.PUSH_OPENED_MR
            }

        } else {

            printOpen("No existen MR abiertas", EchoLevel.DEBUG)
            return PipelineBehavior.PUSH_NO_MR

        }

    } else {
        //Vale ahora si estamos en MERGE deberiamos validar si somos la Primera MR por lo tanto no concurrente con el PUSH o por lo contrario tenemos un PUSH hermano
        //if  (env.gitlabActionType=='MERGE' && env.GIT_PREVIOUS_SUCCESSFUL_COMMIT==env.gitlabMergeRequestLastCommit) //Es la primer MERGE
        //	return PipelineBehavior.NOT_FIRST_MR
        //else if  (env.gitlabActionType=='MERGE')
        return PipelineBehavior.NOT_FIRST_MR
        //Aqui no deberia llegar
        //return PipelineBehavior.PUSH_NO_MR
    }

}
