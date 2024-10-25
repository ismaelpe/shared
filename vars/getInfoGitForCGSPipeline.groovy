import com.project.alm.*

CGSPipelineData call() {
    CGSPipelineData pipelineData = new CGSPipelineData(CGSVars.DEV_ENVIROMENT, "${env.BUILD_TAG}")
    return infoGitToPipelineData(pipelineData)
}