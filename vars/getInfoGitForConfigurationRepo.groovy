import com.caixabank.absis3.*

ConfigurationRepoPipelineData call(String pipelineId) {
    ConfigurationRepoPipelineData pipelineData = new ConfigurationRepoPipelineData(pipelineId)
    return infoGitToPipelineData(pipelineData)
}
