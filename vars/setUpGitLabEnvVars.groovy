import com.project.alm.*

def call(String gitProjectApiUrl, Map params, Map gitLabResponse, boolean isMergeRequest = false) {

    def gitApiCommitInfo = "$gitProjectApiUrl/repository/commits"

    def pathMicro = params.pathToRepoParam.substring(0, params.pathToRepoParam.length() - 4)
    def appName = pathMicro.substring(pathMicro.lastIndexOf("/") + 1, pathMicro.length())
    def domainAux = pathMicro.substring(0, pathMicro.lastIndexOf(appName) - 1)
    def domain = domainAux.substring(domainAux.lastIndexOf("/") + 1, domainAux.length())

    env.GIT_URL = params.pathToRepoParam
    env.gitlabSourceRepoHomepage = pathMicro
    env.gitlabSourceRepoName = appName
    env.gitlabSourceNamespace = domain

    env.gitlabSourceRepoHttpUrl = params.pathToRepoParam
    env.gitlabTargetRepoName = appName
    env.gitlabTargetNamespace = domain
        
    env.gitlabSourceRepoURL = null
    env.gitlabSourceRepoSshUrl = null
    env.gitlabTargetRepoHttpUrl = null
    env.gitlabTargetRepoSshUrl = null
    env.gitlabUserUsername = null
    env.gitlabMergeCommitSha = null
    env.gitlabMergeRequestDescription = null
    env.gitlabMergeRequestAssignee = null
    env.gitlabBefore = null
    env.gitlabAfter = null
    env.gitlabTriggerPhrase = null

    if (isMergeRequest) {
        printOpen("Retrieving merge-request info from gitlab...", EchoLevel.INFO)

        env.gitlabActionType = "MERGE"

        env.gitlabBranch = gitLabResponse.source_branch
        env.gitlabSourceBranch = gitLabResponse.source_branch
        env.gitlabMergeRequestId = gitLabResponse.id
        env.gitlabMergeRequestIid = gitLabResponse.iid
        env.gitlabMergeRequestState = gitLabResponse.state
        env.gitlabMergeRequestLastCommit = gitLabResponse.sha
        env.gitlabMergeRequestTargetProjectId = gitLabResponse.project_id
        env.gitlabTargetBranch = gitLabResponse.target_branch

        printOpen("Get commit info by sha: $env.gitlabMergeRequestLastCommit")
        GitlabAPIResponse commitInfo = sendRequestToGitLabAPI([httpMode: "GET", url: "$gitApiCommitInfo/$env.gitlabMergeRequestLastCommit"])

        env.gitlabUserName = commitInfo.asJson.author_name
        env.gitlabUserUsername = 
        env.gitlabUserEmail = commitInfo.asJson.author_email
        env.gitlabMergeRequestTitle = commitInfo.asJson.title
        env.gitlabMergedByUser = gitLabResponse.author.username

        currentBuild.description = "Build Merge Request #$env.gitlabMergeRequestIid: $env.gitlabSourceBranch => $env.gitlabTargetBranch"
       

    } else {
        env.gitlabActionType = "PUSH"

        printOpen("Get commit info by branch: $params.originBranchParam")
    
        GitlabAPIResponse commitInfo = sendRequestToGitLabAPI([httpMode: "GET", url: "$gitApiCommitInfo/${URLEncoder.encode(params.originBranchParam)}"])
       
        env.gitlabSourceBranch = params.originBranchParam
        env.gitlabUserEmail = commitInfo.asJson.author_email
        env.gitlabUserName = commitInfo.asJson.author_name
        //env.gitlabMergeRequestTitle = commitInfo.asJson.title
        //env.gitlabMergedByUser = gitLabResponse.author.username

        currentBuild.description = "Build ${env.gitlabSourceBranch}"
       
    }

    def garType = PipelineData.initFromGitUrlGarApp(params.pathToRepoParam, ArtifactSubType.valueOfSubType(params.subType));

    currentBuild.displayName = "#${env.BUILD_ID} - ${garType}.$env.gitlabSourceRepoName"
}
