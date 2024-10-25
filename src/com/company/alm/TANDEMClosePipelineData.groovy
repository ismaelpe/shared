package com.project.alm


class TANDEMClosePipelineData extends PipelineData {

    TANDEMClosePipelineData(String pipelineInput, String gitUrl, String user, String version, Map pipelineParams) {
		jobParameters = pipelineParams
        pipelineStructureType = PipelineStructureType.CLOSE

        if (pipelineInput != null) {
            pipelineId = pipelineInput.replace("/", "")
            pipelineId = pipelineId.replace("#", "")
            pipelineId = pipelineId.replace("%", "")
            pipelineId = pipelineId.replace(".", "-")
        }

        garArtifactType = GarAppType.ARCH_LIBRARY
        bmxStructure = new ProBmxStructure()

		this.gitUrl = gitUrl
		pushUser = user
		setBuildCode(version)
    }
	
	void init(String branchName) {
		pipelineStructure = new TANDEMClosePipelineStructure(pipelineId)
		pipelineStructure.resultPipelineData = new TANDEMCloseResultPipelineData(gitUrl)
				
		pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(jobParameters)
		
		branchStructure = new BranchStructure();
		branchStructure.branchName = branchName
		branchStructure.init()
	}

    String toString() {
        return "TANDEM Close PipelineData:\n" +
                super.toString()
    }

}
