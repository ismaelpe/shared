import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PipelineBehavior
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PipelineUtils

PipelineData call(PipelineData pipelineData) {
    String commitLog
    if (env.gitlabMergeRequestLastCommit != null) {
        commitLog = sh(returnStdout: true, script: "git log --pretty=format:\"%H %B\" -n 1 ${env.gitlabMergeRequestLastCommit}")
        pipelineData.commitId = env.gitlabMergeRequestLastCommit
    } else {
        commitLog = sh(returnStdout: true, script: "git log --pretty=format:\"%H %B\" -n 1 ")
    }
    printOpen("Commit log: ${commitLog}", EchoLevel.INFO)
    commitLog = commitLog.replace("\n", " ")

    return infoGitToPipelineData(pipelineData, commitLog)
    
}

PipelineData call(PipelineData pipelineData, String commitLog) {
    pipelineData.commitLog = commitLog

    pipelineData.gitAction = "${env.gitlabActionType}"
    pipelineData.gitUrl = "${env.GIT_URL}"
    pipelineData.gitProject = "${env.GIT_URL}"

    //En gitlabUserName no viene el U01XXXXX por lo que hay que enviar el email para que IDECUA funcione correctamente
    pipelineData.pushUser = "${env.gitlabUserEmail}"
    pipelineData.pushUserEmail = "${env.gitlabUserEmail}"
    pipelineData.targetBranch = "${env.gitlabTargetBranch}"

    //Faltaria analizar el tipo o el modo de job de jenkins
    if (pipelineData.gitAction != "" && pipelineData.gitAction != null) {
        pipelineData.isMultiBranchPipeline = false
    } else {
        pipelineData.isMultiBranchPipeline = true
    }

    def weChangedLoggerLevel = false

    if (PipelineUtils.commitLogHasLogLevelInfoFlag(commitLog)) {
        GlobalVars.CONSOLE_LOGGER_LEVEL = "INFO"
        weChangedLoggerLevel = true
    } else if (PipelineUtils.commitLogHasLogLevelDebugFlag(commitLog)) {
        GlobalVars.CONSOLE_LOGGER_LEVEL = "DEBUG"
        weChangedLoggerLevel = true
    } else if (PipelineUtils.commitLogHasLogLevelAllFlag(commitLog)) {
        GlobalVars.CONSOLE_LOGGER_LEVEL = "ALL"
        weChangedLoggerLevel = true
    }

    if (weChangedLoggerLevel) {
        printOpen("The logger level for this pipeline has been set to ${GlobalVars.CONSOLE_LOGGER_LEVEL} as per command passed via commit log. This overrides Jenkinsfile and env configurations", EchoLevel.INFO)
    }

    if (PipelineUtils.commitLogHasDoNotCIFlag(pipelineData.commitLog)) {
        pipelineData.pipelineBehavior = PipelineBehavior.COMMITLOG_REQUESTED_NO_CI
        env.pipelineBehavior = PipelineBehavior.COMMITLOG_REQUESTED_NO_CI
    }

    return pipelineData

}


