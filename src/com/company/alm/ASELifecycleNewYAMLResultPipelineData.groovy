package com.project.alm

class ASELifecycleNewYAMLResultPipelineData extends ASELifecycleProvisioningResultPipelineData {


    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }


    ASELifecycleNewYAMLResultPipelineData(String gitUrl, String pipelineOrigId) {
        super(gitUrl, pipelineOrigId)
        nextEnvironment = ASEVars.TST_ENVIRONMENT
        environment = GlobalVars.DEV_ENVIRONMENT
    }

    @Override
    def getAcciones(boolean result) {
        if (result) {
            return [
                [
                    nombre              : "Create Release Candidate",
                    authorizationService: AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
                    //FIXME: IDECUA is not progressing pipelines when this is set to MAXIMO. Double check if it is working already
                    envAuthorization    : GlobalVars.TST_ENVIRONMENT,
                    tipoAccion          : "LANZAR_JOB",
                    destino             : ASEVars.ALM_JOB_PROVISIONING,
                    parametros          :
                        [
                            [
                                nombre: "pipelineOrigId",
                                valor : pipelineOrigId
                            ],
                            [
                                nombre: "yamlFilePath",
                                valor : yamlFilePath
                            ],
                            [
                                nombre: "artifactId",
                                valor : artifactId
                            ],
                            [
                                nombre: "artifactGroupId",
                                valor : artifactGroupId
                            ],
                            [
                                nombre: "artifactVersion",
                                valor : artifactVersion
                            ],
                            [
                                nombre: "nextEnvironment",
                                valor : nextEnvironment
                            ],
                            [
                                nombre: "originBranchParam",
                                valor : originBranch
                            ],
                            [
                                nombre: "userEmail",
                                valor : userEmail
                            ],
                            [
                                nombre: "user",
                                valor : user
                            ],
                            [
                                nombre: "contractGitCommit",
                                valor : contractGitCommit
                            ],
                            [
                                nombre: "gitUrl",
                                valor : gitUrl
                            ],
                            [
                                nombre: "loggerLevel",
                                valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                            ]
                        ]
                ]
            ]
        } else {
            return [
            ]
        }
    }

    @Override
    def getDeployed() {
        return TrazabilidadAppPortalType.ALTA.toString()
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }
}
