package com.caixabank.absis3

abstract class CGSProvisioningResultPipelineData extends ResultPipelineData {

    String xmlFileIn
	String xmlFileOut
    String artifactId
    String artifactGroupId
    String artifactVersion

    String operationId
    String nextEnvironment

    String originBranch
    String userEmail
    String user

    CGSProvisioningResultPipelineData(String gitUrl) {
        super()
        this.gitUrl = gitUrl
    }

}
