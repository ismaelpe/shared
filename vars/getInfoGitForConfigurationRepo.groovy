import com.project.alm.*

ConfigurationRepoPipelineData call(String pipelineId) {
    ConfigurationRepoPipelineData pipelineData = new ConfigurationRepoPipelineData(pipelineId)
    return infoGitToPipelineData(pipelineData)
}
