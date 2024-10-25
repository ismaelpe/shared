import com.caixabank.absis3.*

ADSPipelineData call() {
    ADSPipelineData pipelineData = new ADSPipelineData(ADSVars.DEV_ENVIROMENT, "${env.BUILD_TAG}")
    return infoGitToPipelineData(pipelineData)
}
