package com.caixabank.absis3

import com.caixabank.absis3.*

abstract class TANDEMProvisioningResultPipelineData extends ResultPipelineData {

    String xmlFile
    String artifactId
    String artifactGroupId
    String artifactVersion

    String transactionId
    String nextEnvironment

    String originBranch
    String userEmail
    String user

    TANDEMProvisioningResultPipelineData(String gitUrl) {
        super()
        this.gitUrl = gitUrl
    }

}