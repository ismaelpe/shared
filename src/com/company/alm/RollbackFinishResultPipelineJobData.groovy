package com.project.alm

class RollbackFinishResultPipelineJobData extends ResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }

    RollbackFinishResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map jobParameters) {
        super(environment, gitUrl, gitProject, deploy, jobName, jobParameters)
    }


    @Override
    def getAcciones(boolean result) {
        if (result) {
            return []
        } else {
            return [
                super.getLogError(),
            ]
        }
    }

    @Override
    def getDeployed() {
        return TrazabilidadAppPortalType.ALTA.toString()
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "ROLLBACK_FINISH_OK"
        else return "ROLLBACK_FINISH_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_NONE
    }
}
