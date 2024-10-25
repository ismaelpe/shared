package com.caixabank.absis3

abstract class ASELifecycleProvisioningResultPipelineData extends ResultPipelineData {

    String yamlFilePath
    String artifactId
    String artifactGroupId
    String artifactVersion

    String serviceNameAndVersion
    String nextEnvironment

    String originBranch
    String userEmail
    String user

    String contractGitCommit

    ASELifecycleProvisioningResultPipelineData(String gitUrl, String pipelineOrigId) {
        super()
        this.gitUrl = gitUrl
        this.pipelineOrigId = pipelineOrigId
    }

}
