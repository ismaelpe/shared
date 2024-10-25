package com.caixabank.absis3

class ApiManagerTechnicalServicesInput {

    String distributionId                                       // p.e. 20201218.121940-13
    String distributionTimestamp = System.currentTimeMillis()   // Epoch time
    BigDecimal distributionType = 1                             // 0-Absis2, 1-Absis3, 2-Other
    BigDecimal environmentId                                    // 1-DEV, 2-TST, 3-PRE, 4-PRO
    List<ApiManagerTechnicalServiceInput> technicalServices = new ArrayList<ApiManagerTechnicalServiceInput>()

    static int calculateEnvironmentIdFromString(String env) {

        switch (env) {
            case "EDEN":
                return 1
            case "DEV":
                return 1
            case "TST":
                return 2
            case "PRE":
                return 3
            case "PRO":
                return 4
            default:
                throw new Exception("Invalid environment specified during instantiation of ApiManagerTechnicalServicesInput.environmentId! (${env})")
        }

    }

    static String calculateDistributionId(PipelineData pipelineData, PomXmlStructure pomXml) {

        if ( (pipelineData.isCIReleaseBranch() ||
            pipelineData.isCIHotfixBranch() ||
            pipelineData.isCloseRelease()) &&
            pomXml.isRelease()) {
            return "F"
        }

        return pipelineData.buildCode
    }
}
