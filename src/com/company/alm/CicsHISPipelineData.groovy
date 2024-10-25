package com.project.alm

import com.project.alm.*


class CicsHISPipelineData extends PipelineData {


    String environment

    CicsHISPipelineData(String nextEnviroment, String pipelineInput) {

        if (CicsVars.DEV_ENVIROMENT.equals(nextEnviroment)) {
            pipelineStructureType = PipelineStructureType.CICS_HIS_BUILD
        } else if (CicsVars.CLOSE_PIPELINE.equals(nextEnviroment)) {
            pipelineStructureType = PipelineStructureType.CLOSE
        } else {
            pipelineStructureType = PipelineStructureType.CICS_HIS_PROVISIONING
        }

        if (pipelineInput != null) {
            pipelineId = pipelineInput.replace("/", "")
            pipelineId = pipelineId.replace("#", "")
            pipelineId = pipelineId.replace("%", "")
            pipelineId = pipelineId.replace(".", "-")
        }

        if (PipelineStructureType.CICS_HIS_BUILD == pipelineStructureType) {
            pipelineStructure = new CicsHISPipelineStructure(pipelineId)
        } else if (PipelineStructureType.CICS_HIS_PROVISIONING == pipelineStructureType) {
            pipelineStructure = new CicsHISCreateProvisioningPipelineStructure(pipelineId)
        } else if (PipelineStructureType.CLOSE == pipelineStructureType) {
            pipelineStructure = new CicsHISClosePipelineStructure(pipelineId)
        }
        super.garArtifactType = GarAppType.LIBRARY
        environment = nextEnviroment
    }

    def init(BranchStructure branch, String artifactSubType, String artifactType) {

        branchStructure = branch
        branch.initFeatureFromBranchName()

        if (PipelineStructureType.CICS_HIS_BUILD.equals(pipelineStructureType)) {
            bmxStructure = new DevBmxStructure()
            pipelineStructure.resultPipelineData = new CicsHISNewDLLResultPipelineData(gitUrl)
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

    def initFromCicsHISProvisioning(String branchName, String gitUrl, String moduleId, String moduleName) {
        if (CicsVars.TST_ENVIRONMENT.equals(environment)) {
            bmxStructure = new TstBmxStructure()
            pipelineStructure.resultPipelineData = new CicsHISCreateRCResultPipelineData(gitUrl)
            pipelineStructure.resultPipelineData.environment = GlobalVars.TST_ENVIRONMENT
        } else if (CicsVars.PRE_ENVIRONMENT.equals(environment)) {
            bmxStructure = new PreBmxStructure()
            pipelineStructure.resultPipelineData = new CicsHISCreateReleaseResultPipelineData(gitUrl)
            pipelineStructure.resultPipelineData.environment = GlobalVars.PRE_ENVIRONMENT
        } else if (CicsVars.PRO_ENVIRONMENT.equals(environment)) {
            bmxStructure = new ProBmxStructure()
            pipelineStructure.resultPipelineData = new CicsHISDeployProResultPipelineData(gitUrl)
            pipelineStructure.resultPipelineData.environment = GlobalVars.PRO_ENVIRONMENT
        } else if (PipelineStructureType.CLOSE.equals(pipelineStructureType)) {
            bmxStructure = new ProBmxStructure()
            pipelineStructure.resultPipelineData = new CicsHISCloseResultPipelineData(gitUrl)
            pipelineStructure.resultPipelineData.environment = GlobalVars.PRO_ENVIRONMENT
        } else {
            throw new Exception("Invalid nextEnviroment variable: ${nextEnvironment}")
        }

        pipelineStructure.resultPipelineData.agileModuleId = moduleId
        pipelineStructure.resultPipelineData.agileModuleName = moduleName

        branchStructure = new BranchStructure();
        branchStructure.branchName = branchName
        branchStructure.init()
    }

    String toString() {
        return "Cics PipelineData:\n" +
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
