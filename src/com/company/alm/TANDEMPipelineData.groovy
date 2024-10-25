package com.project.alm


class TANDEMPipelineData extends PipelineData {

    Environment environment

    TANDEMPipelineData(Environment nextEnvironment, String pipelineInput, Map pipelineParams) {
        this(nextEnvironment, pipelineInput)
        this.jobParameters = pipelineParams
    }

    TANDEMPipelineData(Environment nextEnviroment, String pipelineInput) {

        if (Environment.DEV.equals(nextEnviroment)) {
            pipelineStructureType = PipelineStructureType.TANDEM_BUILD
        } else if (Environment.TST.equals(nextEnviroment) || Environment.PRE.equals(nextEnviroment) || Environment.PRO.equals(nextEnviroment)) {
        	pipelineStructureType = PipelineStructureType.TANDEM_PROVISIONING
		}

        if (pipelineInput != null) {
            pipelineId = pipelineInput.replace("/", "")
            pipelineId = pipelineId.replace("#", "")
            pipelineId = pipelineId.replace("%", "")
            pipelineId = pipelineId.replace(".", "-")
        }

        if (PipelineStructureType.TANDEM_BUILD == pipelineStructureType) {
            pipelineStructure = new TANDEMPipelineStructure(pipelineId)
        } else if (PipelineStructureType.TANDEM_PROVISIONING == pipelineStructureType) {
			pipelineStructure = new TANDEMCreateProvisioningPipelineStructure(pipelineId, "TANDEM_Provisioning_${nextEnviroment}")
        }
        garArtifactType = GarAppType.ARCH_LIBRARY
        environment = nextEnviroment
    }

    def init(BranchStructure branch, ArtifactType artifactType, ArtifactSubType artifactSubType) {

        branchStructure = branch
        branch.initFeatureFromBranchName()

        if (PipelineStructureType.TANDEM_BUILD.equals(pipelineStructureType)) {
            bmxStructure = new DevBmxStructure()
            pipelineStructure.resultPipelineData = new TANDEMNewXMLResultPipelineData(gitUrl)
            pipelineStructure.resultPipelineData.environment = GlobalVars.DEV_ENVIRONMENT
        }

        if (pipelineStructure.resultPipelineData != null) {
            pipelineStructure.resultPipelineData.repoName = getGitRepoName()
            pipelineStructure.resultPipelineData.pipelineOrigId = pipelineId
            pipelineStructure.resultPipelineData.branchName = branchStructure.branchName
            pipelineStructure.resultPipelineData.artifactType = artifactType.name()
            pipelineStructure.resultPipelineData.artifactSubType = artifactSubType.name()
            pipelineStructure.resultPipelineData.commitId = commitId
        }
    }

    def initFromTANDEMProvisioning(String branchName, String gitUrl) {
        if (Environment.TST.equals(environment)) {
            bmxStructure = new TstBmxStructure()
            pipelineStructure.resultPipelineData = new TANDEMCreateRCResultPipelineData(gitUrl)
            pipelineStructure.resultPipelineData.environment = GlobalVars.TST_ENVIRONMENT
        } else if (Environment.PRE.equals(environment)) {
            bmxStructure = new PreBmxStructure()
            pipelineStructure.resultPipelineData = new TANDEMCreateReleaseResultPipelineData(gitUrl)
            pipelineStructure.resultPipelineData.environment = GlobalVars.PRE_ENVIRONMENT
        } else if (Environment.PRO.equals(environment)) {
            bmxStructure = new ProBmxStructure()
            pipelineStructure.resultPipelineData = new TANDEMDeployProResultPipelineData(gitUrl)
            pipelineStructure.resultPipelineData.environment = GlobalVars.PRO_ENVIRONMENT
        } else {
            throw new Exception("Invalid nextEnviroment variable: ${nextEnvironment}")
        }

        pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(jobParameters)

        branchStructure = new BranchStructure();
        branchStructure.branchName = branchName
        branchStructure.init()
    }

    String toString() {
        return "TANDEM PipelineData:\n" +
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
