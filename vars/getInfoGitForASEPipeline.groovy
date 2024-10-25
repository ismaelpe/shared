import com.caixabank.absis3.ASEPipelineData
import com.caixabank.absis3.ASEVars
import com.caixabank.absis3.EchoLevel

ASEPipelineData call(def contractGitCommit, def gitUrl, def user, def userEmail) {
    ASEPipelineData pipelineData = new ASEPipelineData(ASEVars.DEV_ENVIROMENT, "${env.BUILD_TAG}")
    if (contractGitCommit != null) {
        pipelineData.commitLog = sh(returnStdout: true, script: "git log --pretty=oneline -n 1 ${contractGitCommit}")
        printOpen("ASE Pipeline. gitlabMergeRequestLastCommit:${contractGitCommit} message:${pipelineData.commitLog}", EchoLevel.DEBUG)
        pipelineData.commitId = contractGitCommit
    } else {
        pipelineData.commitLog = sh(returnStdout: true, script: "git log --pretty=oneline -n 1 ")
        printOpen("ASE Pipeline. gitlabMergeRequestLastCommit message:${pipelineData.commitLog}", EchoLevel.DEBUG)
    }
    pipelineData.gitAction = env.gitlabActionType
    pipelineData.gitUrl = gitUrl
    pipelineData.gitProject = gitUrl

    pipelineData.pushUser = user
    pipelineData.pushUserEmail = userEmail
    pipelineData.targetBranch = env.gitlabTargetBranch

    printOpen("The last commitLog is:\n\n${pipelineData.commitLog}", EchoLevel.INFO)

    printOpen(pipelineData.toString(), EchoLevel.DEBUG)

    return pipelineData
}
