package com.project.alm


class ADSPipelineData extends PipelineData {

    String environment

    ADSPipelineData(String nextEnvironment, String pipelineInput, Map pipelineParams) {
        this(nextEnvironment, pipelineInput)
        this.jobParameters = pipelineParams
    }

    ADSPipelineData(String nextEnviroment, String pipelineInput) {

        if (ADSVars.DEV_ENVIROMENT.equals(nextEnviroment)) {
            pipelineStructureType = PipelineStructureType.ADS_BUILD
        } else if (ADSVars.CLOSE_PIPELINE.equals(nextEnviroment)) {
            pipelineStructureType = PipelineStructureType.CLOSE
        } else {
            pipelineStructureType = PipelineStructureType.ADS_PROVISIONING
        }

        if (pipelineInput != null) {
            pipelineId = pipelineInput.replace("/", "")
            pipelineId = pipelineId.replace("#", "")
            pipelineId = pipelineId.replace("%", "")
            pipelineId = pipelineId.replace(".", "-")
        }

        if (PipelineStructureType.ADS_BUILD == pipelineStructureType) {
            pipelineStructure = new ADSPipelineStructure(pipelineId)
        } else if (PipelineStructureType.ADS_PROVISIONING == pipelineStructureType) {
            pipelineStructure = new ADSCreateProvisioningPipelineStructure(pipelineId)
        } else if (PipelineStructureType.CLOSE == pipelineStructureType) {
            pipelineStructure = new ADSClosePipelineStructure(pipelineId)
        }
        garArtifactType = GarAppType.ARCH_LIBRARY
        environment = nextEnviroment
    }

    def init(BranchStructure branch, String artifactSubType, String artifactType) {

        branchStructure = branch
        branch.initFeatureFromBranchName()

        if (PipelineStructureType.ADS_BUILD.equals(pipelineStructureType)) {
            bmxStructure = new DevBmxStructure()
            pipelineStructure.resultPipelineData = new ADSNewXMLResultPipelineData(gitUrl) //Cambiar
            pipelineStructure.resultPipelineData.environment = GlobalVars.DEV_ENVIRONMENT
        }

        if (pipelineStructure.resultPipelineData != null) {
            pipelineStructure.resultPipelineData.repoName = getGitRepoName()
            pipelineStructure.resultPipelineData.pipelineOrigId = pipelineId
            pipelineStructure.resultPipelineData.branchName = branchStructure.branchName
            pipelineStructure.resultPipelineData.artifactType = artifactType
            pipelineStructure.resultPipelineData.artifactSubType = artifactSubType
            pipelineStructure.resultPipelineData.commitId = commitId
        }
    }

    def initFromADSProvisioning(String branchName, String gitUrl) {
        if (ADSVars.TST_ENVIRONMENT.equals(environment)) {
            bmxStructure = new TstBmxStructure()
            pipelineStructure.resultPipelineData = new ADSCreateRCResultPipelineData(gitUrl)
            pipelineStructure.resultPipelineData.environment = GlobalVars.TST_ENVIRONMENT
        } else if (ADSVars.PRE_ENVIRONMENT.equals(environment)) {
            bmxStructure = new PreBmxStructure()
            pipelineStructure.resultPipelineData = new ADSCreateReleaseResultPipelineData(gitUrl)
            pipelineStructure.resultPipelineData.environment = GlobalVars.PRE_ENVIRONMENT
        } else if (ADSVars.PRO_ENVIRONMENT.equals(environment)) {
            bmxStructure = new ProBmxStructure()
            pipelineStructure.resultPipelineData = new ADSDeployProResultPipelineData(gitUrl)
            pipelineStructure.resultPipelineData.environment = GlobalVars.PRO_ENVIRONMENT
        } else if (PipelineStructureType.CLOSE.equals(pipelineStructureType)) {
            bmxStructure = new ProBmxStructure()
            pipelineStructure.resultPipelineData = new ADSCloseResultPipelineData(gitUrl)
        } else {
            throw new Exception("Invalid nextEnviroment variable: ${nextEnvironment}")
        }

        pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(jobParameters)

        branchStructure = new BranchStructure();
        branchStructure.branchName = branchName
        branchStructure.init()
    }

    String toString() {
        return "ADS PipelineData:\n" +
                "\tenvironment: $environment\n" + super.toString()
    }

    def getFeature(String version) {
        //if (branchStructure!=null && branchStructure.branchType==BranchType.FEATURE && eventToPush!=null && eventToPush!="" && originPushToMaster!=null && originPushToMaster!=BranchType.RELEASE) {
        if (eventToPush == null || eventToPush == "") {
            eventToPush = branchStructure.featureNumber
        }
        if (branchStructure != null && branchStructure.branchType == BranchType.FEATURE && eventToPush != null && eventToPush != "" && bmxStructure.environment == GlobalVars.DEV_ENVIRONMENT) {
            return [
                    name: eventToPush,
                    type: "FEATURE"
            ]
        } else
            return null

    }

}
