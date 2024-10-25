package com.caixabank.absis3

class HotFixResultPipelineJobData extends ResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }


    HotFixResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map jobParameters) {
        super(environment, gitUrl, gitProject, deploy, jobName, jobParameters)
    }

    @Override
    def getAcciones(boolean result) {
        if (result) {
            return []
        } else {
            return [
                super.getLogError(),
                super.retry()
            ]
        }
    }

    @Override
    def getDeployed() {
        return TrazabilidadGPLType.NADA.toString()
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "CREATE_RELEASE_CANDIDATE_OK"
        else return "CREATE_RELEASE_CANDIDATE_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_VERSION
    }
}
