package com.project.alm

class ReleaseCandidateResultPipelineJobData extends ResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }


    ReleaseCandidateResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map JobParameters) {
        super(environment, gitUrl, gitProject, deploy, jobName, JobParameters)
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
        return TrazabilidadAppPortalType.NADA.toString()
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
