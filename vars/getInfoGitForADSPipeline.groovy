import com.project.alm.*

ADSPipelineData call() {
    ADSPipelineData pipelineData = new ADSPipelineData(ADSVars.DEV_ENVIROMENT, "${env.BUILD_TAG}")
    return infoGitToPipelineData(pipelineData)
}
