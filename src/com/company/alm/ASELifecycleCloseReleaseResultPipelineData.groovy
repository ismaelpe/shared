package com.project.alm


class ASELifecycleCloseReleaseResultPipelineData extends ASELifecycleProvisioningResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }


    ASELifecycleCloseReleaseResultPipelineData(String gitUrl, String pipelineOrigId) {
        super(gitUrl, pipelineOrigId)
        environment = ASEVars.CLOSE_PIPELINE
    }

    @Override
    def getAcciones(boolean result) {
        if (result) {
            return [
            ]
        } else {
            return [
            ]
        }
    }

    @Override
    def getDeployed() {
        return TrazabilidadAppPortalType.NADA.toString()
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_VERSION
    }
}
