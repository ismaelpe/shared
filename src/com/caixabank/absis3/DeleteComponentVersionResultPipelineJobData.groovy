package com.caixabank.absis3

class DeleteComponentVersionResultPipelineJobData extends ResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        if (environment == GlobalVars.DEV_ENVIRONMENT) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else if (environment == GlobalVars.TST_ENVIRONMENT) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else if (environment == GlobalVars.PRE_ENVIRONMENT) {
           if (isAuthExcluded()) {
                return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
            } else {
                return AuthorizationServiceToInformType.MAXIMO.toString()
            }
        } else if (environment == GlobalVars.PRO_ENVIRONMENT) {
           if (isAuthExcluded()) {
                return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
            } else {
                return AuthorizationServiceToInformType.MAXIMO.toString()
            }
        }
    }

    DeleteComponentVersionResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy) {
        super(environment, gitUrl, gitProject, deploy)
    }


	@Override
	def getAcciones(boolean result) {
		if (result) {
			if (environment == GlobalVars.DEV_ENVIRONMENT) {
				return [getDeleteInTST()]
			} else if (environment == GlobalVars.TST_ENVIRONMENT) {
				return [getDeleteInPRE()]
			} else if (environment == GlobalVars.PRE_ENVIRONMENT) {
				return [getDeleteInPRO()]
			} else if (environment == GlobalVars.PRO_ENVIRONMENT) {
				return [
					//getAccionesRollback()
				]
			}
		}else {
			return [
                super.getLogError(),
                retry([
                    authorizationService: getAuthServiceToInform()
                ])
            ]
		}
	}

    @Override
    def getDeployed() {
        return TrazabilidadGPLType.BAJA.toString()
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "DELETE_COMPONENT_VERSION_OK"
        else return "DELETE_COMPONENT_VERSION_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_NONE
    }

    def getDeleteInTST() {
        return [
            nombre              : "Delete Component Version in TST",
            authorizationService: getAuthServiceToInform(),
            envAuthorization    : GlobalVars.TST_ENVIRONMENT,
            tipoAccion          : "LANZAR_JOB",
            destino             : GlobalVars.ALM_JOB_DELETE_COMPONENT,
            canBeDisabled       : true,
            parametros          :
                [
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],
                    [
                        nombre: "environmentParam",
                        valor : GlobalVars.TST_ENVIRONMENT
                    ],
                    [
                        nombre: "pathToRepoParam",
                        valor : gitUrl
                    ],
                    [
                        nombre: "repoParam",
                        valor : gitProject
                    ],
                    [
                        nombre: "artifactSubTypeParam",
                        valor : artifactSubType
                    ],
                    [
                        nombre: "artifactTypeParam",
                        valor : artifactType
                    ],
                    [
                        nombre: "versionParam",
                        valor : version
                    ],
                    [
                        nombre: "artifactParam",
                        valor : artifact
                    ],
                    [
                        nombre: "executionProfileParam",
                        valor : executionProfile
                    ],
                    [
                        nombre: "targetAlmFolderParam",
                        valor : almSubFolder
                    ],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]

                ]
        ]
    }

    def getDeleteInPRE() {
        return [
            nombre              : "Delete Component Version in PRE",
            authorizationService: getAuthServiceToInform(),
            envAuthorization    : GlobalVars.PRE_ENVIRONMENT,
            tipoAccion          : "LANZAR_JOB",
            destino             : GlobalVars.ALM_JOB_DELETE_COMPONENT,
            canBeDisabled       : true,
            parametros          :
                [
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],
                    [
                        nombre: "environmentParam",
                        valor : GlobalVars.PRE_ENVIRONMENT
                    ],
                    [
                        nombre: "pathToRepoParam",
                        valor : gitUrl
                    ],
                    [
                        nombre: "repoParam",
                        valor : gitProject
                    ],
                    [
                        nombre: "artifactSubTypeParam",
                        valor : artifactSubType
                    ],
                    [
                        nombre: "artifactTypeParam",
                        valor : artifactType
                    ],
                    [
                        nombre: "versionParam",
                        valor : version
                    ],
                    [
                        nombre: "artifactParam",
                        valor : artifact
                    ],
                    [
                        nombre: "executionProfileParam",
                        valor : executionProfile
                    ],
                    [
                        nombre: "targetAlmFolderParam",
                        valor : almSubFolder
                    ],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]

                ]
        ]
    }

    def getDeleteInPRO() {
        return [
            nombre              : "Delete Component Version in PRO",
            authorizationService: getAuthServiceToInform(),
            envAuthorization    : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion          : "LANZAR_JOB",
            destino             : GlobalVars.ALM_JOB_DELETE_COMPONENT,
            canBeDisabled       : true,
            parametros          :
                [
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],
                    [
                        nombre: "environmentParam",
                        valor : GlobalVars.PRO_ENVIRONMENT
                    ],
                    [
                        nombre: "pathToRepoParam",
                        valor : gitUrl
                    ],
                    [
                        nombre: "repoParam",
                        valor : gitProject
                    ],
                    [
                        nombre: "artifactSubTypeParam",
                        valor : artifactSubType
                    ],
                    [
                        nombre: "artifactTypeParam",
                        valor : artifactType
                    ],
                    [
                        nombre: "versionParam",
                        valor : version
                    ],
                    [
                        nombre: "artifactParam",
                        valor : artifact
                    ],
                    [
                        nombre: "executionProfileParam",
                        valor : executionProfile
                    ],
                    [
                        nombre: "targetAlmFolderParam",
                        valor : almSubFolder
                    ],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]

                ]
        ]
    }

    def getAccionesRollback() {
        return [
            nombre       : "Rollback of Deletion",
            tipoAccion   : "LANZAR_JOB",
            destino      : GlobalVars.ALM_JOB_ROLLBACK,
            canBeDisabled: true,
            parametros   :
                [
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],
                    [
                        nombre: "environmentParam",
                        valor : environment
                    ],
                    [
                        nombre: "pathToRepoParam",
                        valor : gitUrl
                    ],
                    [
                        nombre: "repoParam",
                        valor : gitProject
                    ],
                    [
                        nombre: "artifactSubTypeParam",
                        valor : artifactSubType
                    ],
                    [
                        nombre: "artifactTypeParam",
                        valor : artifactType
                    ],
                    [
                        nombre: "originBranchParam",
                        valor : branchName
                    ],
                    [
                        nombre: "versionParam",
                        valor : version
                    ],
                    [
                        nombre: "artifactParam",
                        valor : artifact
                    ],
                    [
                        nombre: "executionProfileParam",
                        valor : executionProfile
                    ],
                    [
                        nombre: "targetAlmFolderParam",
                        valor : almSubFolder
                    ],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
                ]
        ]
    }

}
