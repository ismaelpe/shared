package com.caixabank.absis3

import com.caixabank.absis3.*

abstract class CicsHISProvisioningResultPipelineData extends ResultPipelineData {

    String dllFile
    String svcPath
    String starterArtifactId
    String starterArtifactGroupId
    String starterArtifactVersion

    String cicsHisGroupId
    String cicsHisArtifactId
    String cicsHisVersion

    String transactionId
    String nextEnvironment

    String originBranch
    String userEmail
    String user
    String agileModuleId
    String agileModuleName

    CicsHISProvisioningResultPipelineData(String gitUrl) {
        super()
        this.gitUrl = gitUrl
    }

}