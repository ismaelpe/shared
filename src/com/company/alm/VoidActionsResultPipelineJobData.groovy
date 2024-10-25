package com.project.alm

import com.project.alm.BranchStructure
import com.project.alm.BranchType
import com.project.alm.PipelineStructureType
import com.project.alm.PipelineStructure
import com.project.alm.CIPipelineStructure
import com.project.alm.GlobalVars
import com.project.alm.GarAppType
import com.project.alm.ArtifactSubType
import com.project.alm.TrazabilidadGPLType

class VoidActionsResultPipelineJobData extends ResultPipelineData {
	
	
	
	
    @Override
    def getAuthServiceToInform() {
        return null
    }


    VoidActionsResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map JobParameters) {
        super(environment, gitUrl, gitProject, deploy, jobName, JobParameters)
    }

    @Override
    def getAcciones(boolean result) {
       return []
    }

    @Override
    def getDeployed() {
        return TrazabilidadGPLType.NADA.toString()
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "VALIDATE_DDL_OK"
        else return "VALIDATE_DDL_KO"
    }
	
	@Override
	def getDisabledPolicy() {
		return GlobalVars.DISABLED_POLICY_NONE
	}

}
