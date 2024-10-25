package com.caixabank.absis3

class CGSPipelineData extends PipelineData {

    String environment

	CGSPipelineData(String nextEnvironment, String pipelineInput, Map pipelineParams) {
		this(nextEnvironment, pipelineInput)
		this.jobParameters = pipelineParams
	}
	
    CGSPipelineData(String nextEnviroment, String pipelineInput) {

        if (CGSVars.DEV_ENVIROMENT.equals(nextEnviroment)) {
            pipelineStructureType = PipelineStructureType.CGS_BUILD
        } else if (CGSVars.CLOSE_PIPELINE.equals(nextEnviroment)) {
            pipelineStructureType = PipelineStructureType.CLOSE
        } else {
            pipelineStructureType = PipelineStructureType.CGS_PROVISIONING
        }

        if (pipelineInput != null) {
            pipelineId = pipelineInput.replace("/", "")
            pipelineId = pipelineId.replace("#", "")
            pipelineId = pipelineId.replace("%", "")
            pipelineId = pipelineId.replace(".", "-")
        }

        if (PipelineStructureType.CGS_BUILD == pipelineStructureType) {
            pipelineStructure = new CGSPipelineStructure(pipelineId)
        } else if (PipelineStructureType.CGS_PROVISIONING == pipelineStructureType) {
            pipelineStructure = new CGSCreateProvisioningPipelineStructure(pipelineId)
        } else if (PipelineStructureType.CLOSE == pipelineStructureType) {
            pipelineStructure = new CGSClosePipelineStructure(pipelineId)
        }
        this.garArtifactType = GarAppType.ARCH_LIBRARY
        environment = nextEnviroment
    }

    def init(BranchStructure branch, String artifactSubType, String artifactType) {

        branchStructure = branch
        branch.initFeatureFromBranchName()

        if (PipelineStructureType.CGS_BUILD.equals(pipelineStructureType)) {
            bmxStructure = new DevBmxStructure()
            pipelineStructure.resultPipelineData = new CGSNewXMLResultPipelineData(gitUrl) //Cambiar
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

    def initFromCGSProvisioning(String branchName, String gitUrl) {
        if (CGSVars.TST_ENVIRONMENT.equals(environment)) {
            bmxStructure = new TstBmxStructure()
            pipelineStructure.resultPipelineData = new CGSCreateRCResultPipelineData(gitUrl)
            pipelineStructure.resultPipelineData.environment = GlobalVars.TST_ENVIRONMENT
        } else if (CGSVars.PRE_ENVIRONMENT.equals(environment)) {
            bmxStructure = new PreBmxStructure()
            pipelineStructure.resultPipelineData = new CGSCreateReleaseResultPipelineData(gitUrl)
            pipelineStructure.resultPipelineData.environment = GlobalVars.PRE_ENVIRONMENT
        } else if (CGSVars.PRO_ENVIRONMENT.equals(environment)) {
            bmxStructure = new ProBmxStructure()
            pipelineStructure.resultPipelineData = new CGSDeployProResultPipelineData(gitUrl)
            pipelineStructure.resultPipelineData.environment = GlobalVars.PRO_ENVIRONMENT
        } else if (PipelineStructureType.CLOSE.equals(pipelineStructureType)) {
            bmxStructure = new ProBmxStructure()
            pipelineStructure.resultPipelineData = new CGSCloseResultPipelineData(gitUrl)
        } else {
            throw new Exception("Invalid nextEnviroment variable: ${nextEnvironment}")
        }
		
		pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(jobParameters)

        branchStructure = new BranchStructure();
        branchStructure.branchName = branchName
        branchStructure.init()
    }

    String toString() {
        return "CGS PipelineData:\n" +
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
