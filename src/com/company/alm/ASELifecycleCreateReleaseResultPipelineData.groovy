package com.project.alm

class ASELifecycleCreateReleaseResultPipelineData extends ASELifecycleProvisioningResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }

    ASELifecycleCreateReleaseResultPipelineData(String gitUrl, String pipelineOrigId) {
        super(gitUrl, pipelineOrigId)
        nextEnvironment = ASEVars.CLOSE_PIPELINE
        environment = GlobalVars.PRE_ENVIRONMENT
    }

    @Override
    def getAcciones(boolean result) {
        if (result) {
            return [
                [
                    nombre              : "End Release",
                    authorizationService: AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
                    envAuthorization    : GlobalVars.PRO_ENVIRONMENT,
                    tipoAccion          : "LANZAR_JOB",
                    destino             : ASEVars.ALM_JOB_CIERRE_RELEASE,
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
        return TrazabilidadGPLType.ALTA.toString()
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }
}
