package com.caixabank.absis3

class ApiManagerTechnicalServiceInput {

    String technicalServiceName
    String version
    BigDecimal build = 0                                        // Not needed
    Action action
    BigDecimal apiType = 0                                      // 0-REST, 1-SOAP, 2-Other
    String description
    String appGar
    BigDecimal company = 0                                      // 0-CORP, 1-CBK, 225-BPI, 262-IMG
    String sceneDomainId                                        // Not needed
    String specificationURL                                     // Not used
    String specificationFileName                                // Use the same as in the swagger contract multipart filename field

    enum Action {
        CREATE, DELETE, ROLLBACK, RC, HOTFIX

        static Action of(PipelineData pipelineData, PomXmlStructure pomXml) {

            boolean isSNAPSHOTDeploymentToTST =
                pipelineData.isCIReleaseBranch() && pomXml.isSNAPSHOT() &&
                    "TST" == pipelineData.bmxStructure.environment.toUpperCase()

            if (pipelineData.isRollbackFinish()) {
                return ROLLBACK
            } else if (pipelineData.isDelete()) {
                return DELETE
            } else if (pipelineData.isCIReleaseBranch() && pomXml.isRCVersion()) {
                return RC
            } else if ( (pipelineData.isRegularRelease() || pipelineData.isHotfixRelease()) && pomXml.isRelease() ) {
                return CREATE   // RELEASE??
            } else if (pipelineData.isCIHotfixBranch() && !(pomXml.isRelease()) ) {
                return HOTFIX
            } else if (pipelineData.isCIFeatureOrMaster() || isSNAPSHOTDeploymentToTST || pipelineData.isCloseRelease()) {
                return CREATE   // SNAPSHOT for CIFeatureOrMaster??
            }
            throw new Exception("There is no Action defined for this PipelineData and PomXmlStructure configuration!")
        }
    }

}
