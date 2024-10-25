package com.project.alm

import com.project.alm.*

class ConfigurationRepoResultPipelineJobData extends ResultPipelineData {

	@Override
	public Object getAcciones(boolean result) {
		 return [];
	}

	@Override
    def getDeployed() {
        return TrazabilidadGPLType.ALTA.toString()
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_NONE
    }

	@Override
	public Object getAuthServiceToInform() {
		return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
	}
	
}