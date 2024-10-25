package com.project.alm

import com.project.alm.*


class ConfigurationRepoPipelineData extends PipelineData {

	ConfigurationRepoPipelineData(){
		
	}
	
    ConfigurationRepoPipelineData(String pipelineInput) {
		pipelineStructureType = PipelineStructureType.REPO_CONFIGURATION
		if (pipelineInput != null) {
			pipelineId = pipelineInput.replace("/", "")
			pipelineId = pipelineId.replace("#", "")
			pipelineId = pipelineId.replace("%", "")
			pipelineId = pipelineId.replace(".", "-")
		}
		this.garArtifactType = GarAppType.ARCH_CONFIG
    }

	void initFromConfigurationRepo(BranchStructure branch,ArtifactType artifactType ,ArtifactSubType artifactSubType , String gitUrl) {
		this.branchStructure = branch
		this.gitUrl = gitUrl
		
		String projectName = GitUtils.getProjectFromUrl(this.gitUrl)
		this.pipelineStructure = new ConfigurationRepoPipelineStructure(projectName, pipelineId)
		
		this.pipelineStructure.resultPipelineData = new ConfigurationRepoResultPipelineJobData()
		
		this.pipelineStructure.resultPipelineData.pipelineOrigId = pipelineId
		this.pipelineStructure.resultPipelineData.artifactSubType = artifactSubType
		this.pipelineStructure.resultPipelineData.artifactType = artifactType
		this.pipelineStructure.resultPipelineData.branchName = branch.branchName
		
		String environment = getEnvironmentFromGit(projectName)
		
		this.pipelineStructure.resultPipelineData.environment = environment
	}
	
	private String getEnvironmentFromGit(String projectName) {
		String environment = null
		if(projectName != null) {
			if(projectName.contains("repository-tst")) {
				environment = Environment.TST.name()
			}else if(projectName.contains("repository-pre")) {
				environment = Environment.PRE.name()
			}else if(projectName.contains("repository-pro")) {
				environment = Environment.PRO.name()
			}
		}
		return environment
	} 
}
