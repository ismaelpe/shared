package com.caixabank.absis3

class ReleaseResultPipelineData extends ResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        if (isBpiRepo() && version.contains('-RC')) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }

    /*
    @Override
    def getAuthServiceToInform() {

        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }*/

    ReleaseResultPipelineData() {
        super()
    }

    ReleaseResultPipelineData(String environment, String gitUrl, String gitProject, boolean deploy) {
        super(environment, gitUrl, gitProject, deploy)
    }
  
    def getSonar() {
        return [
            nombre                 : "Validate QA (Required)",
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_SONAR,
            actionInTestingPipeline: true,
            canBeDisabled          : true,
            parametros             :
                [
                    [
                        nombre: "pathToRepoParam",
                        valor : gitUrl
                    ],
                    [
                        nombre: "originBranchParam",
                        valor : branchName
                    ],
                    [
                        nombre: "repoParam",
                        valor : gitProject
                    ],                    
                    [
                        nombre: "pipelineOrigId",
                        valor: pipelineOrigId
                    ],                    
                    [
                        nombre: "mvnAdditionalParametersParam",
                        valor: mvnAdditionalParameters
                    ],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
                ]
        ]
    }

    @Override
    def getAcciones(boolean result) {
        if (result) {
            return [
                getSonar()
            ]           
        } else {
            return [
                super.getLogError()
            ]
        }
    }

    @Override
    def getDeployed() {
        if (deployFlag) {
            if (hasDeployedToCloud) {
                if (remoteITOk) {
                    return TrazabilidadGPLType.ALTA.toString()
                } else {
                    return TrazabilidadGPLType.NADA.toString()
                }
            } else {
                return TrazabilidadGPLType.NADA.toString()
            }
        } else {
            return TrazabilidadGPLType.ALTA.toString()
        }
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "CREATE_RELEASE_OK"
        else return "CREATE_RELEASE_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }
}
