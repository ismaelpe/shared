package com.project.alm

class ApiManagerTechnicalServicesRequest {

    ApiManagerTechnicalServicesInput technicalServicesInput
    boolean contractShouldBeSent
    String contractPath = "./contract/swagger-micro-contract.yaml"
    String apiManagerUri
    String almTokenName
    int timeout = GlobalVars.ALM_MS_TIMEOUT

    static ApiManagerTechnicalServicesRequest fromPipelineAndPomData(PipelineData pipelineData, PomXmlStructure pomXml) {

        ApiManagerTechnicalServicesRequest request = new ApiManagerTechnicalServicesRequest()
        request.contractShouldBeSent = shouldContractBeSent(pipelineData, pomXml)

        ApiManagerTechnicalServiceInput file = new ApiManagerTechnicalServiceInput();
        file.technicalServiceName = pomXml.artifactName
        file.version = pomXml.artifactVersionWithoutQualifier
        file.action = ApiManagerTechnicalServiceInput.Action.of(pipelineData, pomXml)
        file.description = pomXml.description
        file.appGar = "${pipelineData.garArtifactType.getGarName()}.${MavenUtils.sanitizeArtifactName(file.technicalServiceName, pipelineData.garArtifactType)}"
        file.company = getCompanyFromPipelineData(pipelineData)
        if (request.contractShouldBeSent) {
            file.specificationFileName = "swagger-micro-contract.yaml"
        }

        ApiManagerTechnicalServicesInput input = new ApiManagerTechnicalServicesInput()
        input.distributionId = ApiManagerTechnicalServicesInput.calculateDistributionId(pipelineData, pomXml)
        input.environmentId = new BigDecimal(ApiManagerTechnicalServicesInput.calculateEnvironmentIdFromString(pipelineData.bmxStructure.environment.trim().toUpperCase()))
        input.technicalServices.add(file)

        request.technicalServicesInput = input

        return request

    }

    private static boolean shouldContractBeSent(PipelineData pipelineData, PomXmlStructure pomXml) {

        boolean isManualCopyWithBuild =
            pipelineData.manualCopyExecutionMode == ICPVarPipelineCopyType.EX_MODE_ALL &&
                pipelineData.manualCopyElectionOriginArtifact != ICPVarPipelineCopyType.ORIGIN_TAG

        boolean isSNAPSHOTDeploymentToTST =
            pipelineData.isCIReleaseBranch() && pomXml.isSNAPSHOT() &&
                "TST" == pipelineData.bmxStructure.environment.toUpperCase()

        if (pipelineData.isRollbackFinish()) {
            return false
        } else if (pipelineData.isDelete()) {
            return false
        } else if (pipelineData.isCIReleaseBranch() && pomXml.isRCVersion()) {
            return true
        } else if ( (pipelineData.isRegularRelease() || pipelineData.isHotfixRelease()) && pomXml.isRelease() ) {
            return true
        } else if (pipelineData.isCIHotfixBranch() && !(pomXml.isRelease()) ) {
            return true
        } else if (pipelineData.isCIFeatureOrMaster() && "EDEN" == pipelineData.bmxStructure.environment.toUpperCase()) {
            return false
        } else if (pipelineData.isCIFeatureOrMaster() && "DEV" == pipelineData.bmxStructure.environment.toUpperCase()) {
            return true
        } else if (isSNAPSHOTDeploymentToTST && isManualCopyWithBuild) {
            return true
        } else if (isSNAPSHOTDeploymentToTST && !(isManualCopyWithBuild)) {
            return false
        } else if (pipelineData.isCloseRelease()) {
            return false
        }

        throw new Exception("There is no shouldContractBeSent() behaviour defined for this PipelineData and PomXmlStructure configuration!")
    }

    private static BigDecimal getCompanyFromPipelineData(PipelineData pipelineData) {
        
        if ( "CORP" == pipelineData.company) {
            return 0
        }
        if ( "CBK" == pipelineData.company) {
            return 1
        }
        if ( "BPI" == pipelineData.company) {
            return 225
        }
        if ( "IMG" == pipelineData.company) {
            return 262
        }
        return 0

    }
}
