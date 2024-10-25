package com.project.alm

import com.project.alm.*

abstract class ADSProvisioningResultPipelineData extends ResultPipelineData {

    String xmlFile
    String artifactId
    String artifactGroupId
    String artifactVersion

    String transactionId
    String nextEnvironment

    String originBranch
    String userEmail
    String user

    ADSProvisioningResultPipelineData(String gitUrl) {
        super()
        this.gitUrl = gitUrl
    }

}