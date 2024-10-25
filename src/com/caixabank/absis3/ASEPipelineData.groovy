package com.caixabank.absis3

class ASEPipelineData extends PipelineData {

    String environment

    ASEPipelineData(String nextEnviroment, String pipelineInput) {

        if (ASEVars.DEV_ENVIROMENT.equals(nextEnviroment)) {
            pipelineStructureType = PipelineStructureType.CLIENT_SNAPSHOT_BUILD
        } else if (ASEVars.CLOSE_PIPELINE.equals(nextEnviroment)) {
            pipelineStructureType = PipelineStructureType.CLOSE
        } else {
            pipelineStructureType = PipelineStructureType.CLIENT_LIFECYCLE_PROVISIONING
        }

        if (pipelineInput != null) {
            pipelineId = pipelineInput.replace("/", "")
            pipelineId = pipelineId.replace("#", "")
            pipelineId = pipelineId.replace("%", "")
            pipelineId = pipelineId.replace(".", "-")
        }

        if (PipelineStructureType.CLIENT_SNAPSHOT_BUILD == pipelineStructureType) {
            pipelineStructure = new ASEPipelineStructure(pipelineId)
        } else if (PipelineStructureType.CLIENT_LIFECYCLE_PROVISIONING == pipelineStructureType) {
            pipelineStructure = new ASELifecycleProvisioningPipelineStructure(pipelineId)
        } else if (PipelineStructureType.CLOSE == pipelineStructureType) {
            pipelineStructure = new ASEClosePipelineStructure(pipelineId)
        }
        super.garArtifactType = GarAppType.ARCH_LIBRARY
        environment = nextEnviroment
    }

    def init(BranchStructure branch, String artifactSubType, String artifactType) {

        branchStructure = branch
        branch.initFeatureFromBranchName()

        if (PipelineStructureType.CLIENT_SNAPSHOT_BUILD.equals(pipelineStructureType)) {
            bmxStructure = new DevBmxStructure()
            pipelineStructure.resultPipelineData = new ASELifecycleNewYAMLResultPipelineData(gitUrl, pipelineId)
        }

        if (pipelineStructure.resultPipelineData != null) {
            pipelineStructure.resultPipelineData.repoName = getGitRepoName()
            pipelineStructure.resultPipelineData.branchName = branchStructure.branchName
            pipelineStructure.resultPipelineData.artifactType = artifactType
            pipelineStructure.resultPipelineData.artifactSubType = artifactSubType
            pipelineStructure.resultPipelineData.commitId = commitId
        }
    }

    def initFromASEProvisioning(String branchName, String gitUrl) {
        if (ASEVars.TST_ENVIRONMENT.equals(environment)) {
            bmxStructure = new TstBmxStructure()
            pipelineStructure.resultPipelineData = new ASELifecycleCreateRCResultPipelineData(gitUrl, pipelineId)
        } else if (ASEVars.PRE_ENVIRONMENT.equals(environment)) {
            bmxStructure = new PreBmxStructure()
            pipelineStructure.resultPipelineData = new ASELifecycleCreateReleaseResultPipelineData(gitUrl, pipelineId)
        } else if (PipelineStructureType.CLOSE.equals(pipelineStructureType)) {
            bmxStructure = new ProBmxStructure()
            pipelineStructure.resultPipelineData = new ASELifecycleCloseReleaseResultPipelineData(gitUrl, pipelineId)
        } else {
            throw new Exception("Invalid nextEnviroment variable: ${environment}")
        }

        branchStructure = new BranchStructure();
        branchStructure.branchName = branchName
        branchStructure.init()
    }

    String toString() {
        return "ASE PipelineData:\n" +
            "\tenvironment: $environment\n" + super.toString()
    }

    def getPipelineBuildName() {

        if (branchStructure.branchType == BranchType.FEATURE) return "" + branchStructure.branchType.toString() + "_" + branchStructure.featureNumber
        if (branchStructure.branchType == BranchType.MASTER) return "" + branchStructure.branchType.toString() + " "
        if (branchStructure.branchType == BranchType.RELEASE) return "" + branchStructure.branchType.toString() + "_" + branchStructure.releaseNumber
        if (branchStructure.branchType == BranchType.HOTFIX) return "" + branchStructure.branchType.toString() + "_" + branchStructure.releaseNumber
    }

    def getFeature(String version) {

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

