package com.project.alm

class MasterResultPipelineData extends ResultPipelineData {

    MasterResultPipelineData(String environment, String gitUrl, String gitProject, boolean deploy) {
        super(environment, gitUrl, gitProject, deploy)
    }
   
    def getAuthService() {
        if (isBpiRepo()) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
        else return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }
	
	def getNoneAuthService() {
		return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
	}

    def getAccionesCR(boolean actionInTestingPipeline = true) {
        return [
            nombre                 : "Create Release Candidate",
            authorizationService   : getAuthService(),
            envAuthorization       : GlobalVars.TST_ENVIRONMENT,
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_CREATE_RC,
            canBeDisabled          : true,
            actionInTestingPipeline: actionInTestingPipeline,
			upgradeCoreAndCreateRC : true,
            parametros             :
                [
                    [
                        nombre: "pathToRepoParam",
                        valor : gitUrl
                    ],
                    [
                        nombre: "originBranchParam",
                        valor : GlobalVars.MASTER_BRANCH
                    ],
                    [
                        nombre: "repoParam",
                        valor : gitProject
                    ],
                    [
                        nombre: "artifactTypeParam",
                        valor : artifactType
                    ],
                    [
                        nombre: "artifactSubTypeParam",
                        valor : artifactSubType
                    ],
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],
                    [
                        nombre: "commitIdParam",
                        valor : commitId
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
                    ],
                    [
                        nombre: "agent", 
                        valor : agent
                    ]
                ]
        ]
    }
	
	
	def createRC() {
		return [
			nombre                           : "Create BBDD RC",
			authorizationService             : getAuthService(),
			envAuthorization                 : GlobalVars.PRO_ENVIRONMENT,
			tipoAccion                       : "LANZAR_JOB",
			destino                          : GlobalVars.ALM_JOB_CREATE_BBDD_RC,
			actionInTestingPipeline          : true,
			actionWhenHotfixInTestingPipeline: true,
			canBeDisabled                    : true,
			parametros                       :
				[
					[
						nombre: "pathToRepoParam",
						valor : gitUrl
					],
					[
						nombre: "originBranchParam",
						valor : "master"
					],
					[
						nombre: "repoParam",
						valor : gitProject
					],
					[
						nombre: "artifactTypeParam",
						valor : artifactType
					],
					[
						nombre: "artifactSubTypeParam",
						valor : artifactSubType
					],
					[
						nombre: "pipelineOrigId",
						valor : pipelineOrigId
					],
					[
						nombre: "commitIdParam",
						valor : commitId
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
	 
	
	def getAccionesInstalacionManual() {
		return [
			nombre                 : "Instalación manual del SNAPSHOT en el entorno de TST",
			authorizationService   : getNoneAuthService(),
			envAuthorization       : GlobalVars.TST_ENVIRONMENT,
			tipoAccion             : "LANZAR_JOB",
			destino                : GlobalVars.ALM_JOB_MANUAL_COPY,
			canBeDisabled          : true,
			actionInTestingPipeline: false,
			parametros             :
				[
					[
						nombre: "pathToRepoParam",
						valor : gitUrl
					],
					[
						nombre: "versionTagParam",
						valor : ""
					],
					[
						nombre: "pathFeatureParam",
						valor : ""
					],
					[
						nombre: "versionParam",
						valor : version
					],
					[
						nombre: "originBranchParam",
						valor : GlobalVars.MASTER_BRANCH
					],
					[
						nombre: "repoParam",
						valor : gitProject
					],
					[
						nombre: "artifactTypeParam",
						valor : artifactType
					],
					[
						nombre: "executionModeParam",
						valor : "ALL"
					],
					[
						nombre: "environmentDestParam",
						valor : "tst"
					],
					[
						nombre: "electionOriginArtifactParam",
						valor : "BRANCH"
					],
					[
						nombre: "artifactSubTypeParam",
						valor : artifactSubType
					],
					[
						nombre: "pipelineOrigId",
						valor : pipelineOrigId
					],
					[
						nombre: "commitIdParam",
						valor : commitId
					],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ],
                    [
                        nombre: "agent", 
                        valor : agent
                    ]
				]
		]
	}

    def getAccionesCRConfigLib() {
        return [
            nombre                 : "Create Release Candidate Config Lib",
            authorizationService   : getAuthService(),
            envAuthorization       : GlobalVars.TST_ENVIRONMENT,
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_CREATE_RC_CONFIGLIB,
            canBeDisabled          : true,
            actionInTestingPipeline: false,
            parametros             :
                [
                    [
                        nombre: "pathToRepoParam",
                        valor : gitUrl
                    ],
                    [
                        nombre: "originBranchParam",
                        valor : GlobalVars.MASTER_BRANCH
                    ],
                    [
                        nombre: "repoParam",
                        valor : gitProject
                    ],
                    [
                        nombre: "artifactTypeParam",
                        valor : artifactType
                    ],
                    [
                        nombre: "artifactSubTypeParam",
                        valor : artifactSubType
                    ],
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],
                    [
                        nombre: "commitIdParam",
                        valor : commitId
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

    def getAccionesSonar() {
        return [
            nombre                 : "Validate QA - Optional",
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_SONAR,
            canBeDisabled          : true,
            actionInTestingPipeline: true,
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
                    ],
                    [
                        nombre: "agent", 
                        valor : agent
                    ]
                ]
        ]
    }

    @Override
    def getAcciones(boolean result) {
        if (result) {
            if (isMicro()) {
				if (isBBDD) {
					return [
						getAccionesCR(),
						//getAccionesRollback(),
						createRC()
					]	
				}else {
					return [
						getAccionesCR(false),
						//getAccionesRollback(),
						getAccionesInstalacionManual(),
                        getAccionesSonar()
					]	
				}
			
            } else if (isConfigLib()) {
                return [
                    getAccionesCRConfigLib(),
                    getAccionesSonar()
                ]
            } else {
                return [
                    getAccionesCR(false),
                    getAccionesSonar()
                ]
            }
        } else {
            return [
                super.getLogError(),
                retry()
            ]
        }
    }

    @Override
    def retry(def additionalParameters = [:]) {
        Map dictionary = [
            nombre       : "Reintentar",
            tipoAccion   : "LANZAR_JOB",
            destino      : GlobalVars.ALM_JOB_RETRY_CI,
            canBeDisabled: true,
            parametros   : [
                [
                    nombre: "pathToRepoParam",
                    valor : gitUrl
                ],
                [
                    nombre: "originBranchParam",
                    valor : GlobalVars.MASTER_BRANCH
                ],

            ]
        ]
        dictionary += additionalParameters
        return dictionary
    }

    @Override
    def getDeployed() {
        if (deployFlag) {
            if (hasDeployedToCloud) {
                if (remoteITOk) {
                    return TrazabilidadAppPortalType.ALTA.toString()
                } else {
                    return TrazabilidadAppPortalType.NADA.toString()
                }
            } else {
                return TrazabilidadAppPortalType.NADA.toString()
            }
        } else {
            return TrazabilidadAppPortalType.ALTA.toString()
        }
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }

    @Override
    def getAuthServiceToInform() {
        //if (isBpiRepo()) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }

}
