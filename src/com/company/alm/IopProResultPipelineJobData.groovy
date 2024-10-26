package com.project.alm

class IopProResultPipelineJobData extends ResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        if (isBpiRepo()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else {
            //Restablecer cuando AppPortal soporte el "succes=true/false"
            return AuthorizationServiceToInformType.MAXIMO.toString()
            //return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        }
    }

    int cannaryPercentage
    String cannaryType
    /**
     * false it is update of an existing artifact
     * true new artifact in the environment
     */
    boolean newVersion
	boolean microParam = true
	boolean isFastPath = false
	String userId = ""


    IopProResultPipelineJobData() {
        super()
    }

    IopProResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map jobParameters) {
        super(environment, gitUrl, gitProject, deploy, jobName, jobParameters)
    }

    @Override
    def getAuthService() {
        //Cambiar BPI cuando se confirme si hay que enviar a AGILE_WORK para el Cannary/EndRelease/Rollback
        if (isBpiRepo()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else return AuthorizationServiceToInformType.INFORMATIVE_MAXIMO.toString()
    }

    def getAuthManualService() {
        //Cambiar BPI cuando se confirme si hay que enviar a AGILE_WORK para el Cannary/EndRelease/Rollback
        if (isBpiRepo()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else return AuthorizationServiceToInformType.MAXIMO.toString()
    }
    /*
     * En este caso actionInTestingPipeline: true aparece tanto en cierre release como en inccannary porque son situaciones disjuntas
     */
	


    def getCierreRelease() {
        return [
            nombre                        : "End Release",
            authorizationService          : getAuthService(),
            authorizationServiceToInform  : getAuthServiceToInform(),
            envAuthorization              : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion                    : "LANZAR_JOB",
            destino                       : GlobalVars.ALM_JOB_CIERRE_RELEASE,
            canBeDisabled                 : true,
            actionInTestingPipeline       : true,
            triggerIfComponentIsNewVersion: true,
            parametros                    :
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
                        nombre: "originBranchParam",
                        valor : branchName
                    ],
                    [
                        nombre: "versionParam",
                        valor : version
                    ],
					[
						nombre: "oldVersionParam",
						valor : oldVersionInCurrentEnvironment ? oldVersionInCurrentEnvironment : ""
					],
                    [
                        nombre: "artifactParam",
                        valor : artifact
                    ],
                    [
                        nombre: "commitIdParam",
                        valor : commitId
                    ],
                    [
                        nombre: "existAncientParam",
                        valor : !newVersion
                    ],
                    [
                        nombre: "isMicroParam",
                        valor : microParam
                    ],
                    [
                        nombre: "componentParam",
                        valor : component
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
                        nombre: "retryAuthorizationService",
                        valor : getAuthService()
                    ],
                    [
                        nombre: "retryEnvAuthorization",
                        valor : GlobalVars.PRO_ENVIRONMENT
                    ],
                    [
                        nombre: "retryAuthorizationServiceToInform",
                        valor : getAuthServiceToInform()
                    ],
					[
						nombre: "userId",
						valor: userId
					],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
                ]
        ]
    }
	def getCierreReleaseCampaign() {
		return [
			nombre                        : "End Release (debe esperar notificación del fin de campaña)",
			authorizationService          : getAuthService(),
			authorizationServiceToInform  : getAuthServiceToInform(),
			envAuthorization              : GlobalVars.PRO_ENVIRONMENT,
			tipoAccion                    : "LANZAR_JOB",
			destino                       : GlobalVars.ALM_JOB_CIERRE_RELEASE,
			canBeDisabled                 : true,
			actionInTestingPipeline       : true,
			triggerIfComponentIsNewVersion: true,
			parametros                    :
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
						nombre: "originBranchParam",
						valor : branchName
					],
					[
						nombre: "versionParam",
						valor : version
					],
					[
						nombre: "oldVersionParam",
						valor : oldVersionInCurrentEnvironment ? oldVersionInCurrentEnvironment : ""
					],
					[
						nombre: "artifactParam",
						valor : artifact
					],
					[
						nombre: "commitIdParam",
						valor : commitId
					],
					[
						nombre: "existAncientParam",
						valor : !newVersion
					],
					[
						nombre: "isMicroParam",
						valor : microParam
					],
					[
						nombre: "componentParam",
						valor : component
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
						nombre: "retryAuthorizationService",
						valor : getAuthService()
					],
					[
						nombre: "retryEnvAuthorization",
						valor : GlobalVars.PRO_ENVIRONMENT
					],
					[
						nombre: "retryAuthorizationServiceToInform",
						valor : getAuthServiceToInform()
					],
					[
						nombre: "userId",
						valor: userId
					],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
				]
		]
	}
	
	def getCierreReleaseConfigLib() {
		return [
			nombre                        : "End Release",
			authorizationService          : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
			authorizationServiceToInform  : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
			envAuthorization              : GlobalVars.PRO_ENVIRONMENT,
			tipoAccion                    : "LANZAR_JOB",
			destino                       : GlobalVars.ALM_JOB_CIERRE_RELEASE_CONFIGLIB,
			canBeDisabled                 : true,
			actionInTestingPipeline       : true,
			actionWhenHotfixInTestingPipeline: true,
			triggerIfComponentIsNewVersion: true,
			parametros                    :
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
						nombre: "commitIdParam",
						valor : commitId
					],
					[
						nombre: "existAncientParam",
						valor : !newVersion
					],
					[
						nombre: "isMicroParam",
						valor : false
					],
					[
						nombre: "componentParam",
						valor : component
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
                        nombre: "retryAuthorizationService",
                        valor : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
                    ],
                    [
                        nombre: "retryEnvAuthorization",
                        valor : GlobalVars.PRO_ENVIRONMENT
                    ],
                    [
                        nombre: "retryAuthorizationServiceToInform",
                        valor : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
                    ],
					[
						nombre: "userId",
						valor: userId
					],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
				]
		]
	}
	

    def incCannary() {
        return [
            nombre                 : "Increment Percentage",
            authorizationService   : getAuthService(),
            authorizationServiceToInform  : getAuthServiceToInform(),
            envAuthorization       : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_INCREMENT_CANNARY,
            actionInTestingPipeline: true,
            canBeDisabled          : true,
            parametros             :
                [
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],
                    [
                        nombre: "pathToRepoParam",
                        valor : gitUrl
                    ],
                    [
                        nombre: "repoNameParam",
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
						nombre: "oldVersionParam",
						valor : oldVersionInCurrentEnvironment ? oldVersionInCurrentEnvironment : ""
					],
                    [
                        nombre: "artifactParam",
                        valor : artifact
                    ],
                    [
                        nombre: "commitIdParam",
                        valor : commitId
                    ],
                    [
                        nombre: "actualPercentatgeParam",
                        valor : 0
                    ],
                    [
                        nombre: "componentParam",
                        valor : component
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

    def createFix() {
        return [
            nombre       : "Create Fix " + incFix(version),
            tipoAccion   : "LANZAR_JOB",
            destino      : GlobalVars.ALM_JOB_CREATE_FIX,
            canBeDisabled: true,
            parametros   :
                [
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],
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
                        nombre: "versionParam",
                        valor : version
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

    def createConfigFix() {
        return [
            nombre       : "Create Configuration Fix " + incFix(version),
            tipoAccion   : "LANZAR_JOB",
            destino      : GlobalVars.ALM_JOB_CREATE_CONFIGFIX,
            canBeDisabled: true,
            parametros   :
                [
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],
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
                        nombre: "versionParam",
                        valor : version
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

    def getAccionesRollback(def onlyConfig ="false", def onlyDeploy = "true") {
		String nombreAccion="Rollback Artifact"
		if ("true".equals(onlyConfig) && "false".equals(onlyDeploy)) {
			nombreAccion=nombreAccion+ " (Only config)"
		}
        return [
            nombre                           : "${nombreAccion}",
            authorizationService             : getAuthManualService(),
            authorizationServiceToInform     : getAuthServiceToInform(),
            envAuthorization                 : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion                       : "LANZAR_JOB",
            destino                          : GlobalVars.ALM_JOB_ROLLBACK,
            canBeDisabled                    : true,
            actionWhenHotfixInTestingPipeline: true,
            parametros                       :
                [
					[
						nombre: "deployFinishedParam",
						valor : onlyDeploy
					],
					[
						nombre: "onlyConfigParam",
						valor : onlyConfig
					],
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],
                    [
                        nombre: "environmentParam",
                        valor : GlobalVars.PRO_ENVIRONMENT
                    ],
                    [
                        nombre: "ignoreExistingAncientParam",
                        valor : false
                    ],
                    [
                        nombre: "forceAllCentersParam",
                        valor : true
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
						nombre: "oldVersionParam",
						valor : oldVersionInCurrentEnvironment ? oldVersionInCurrentEnvironment : ""
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
                        nombre: "nextDistributionMode",
                        valor : nextDistributionMode
                    ],
                    [
                        nombre: "retryAuthorizationService",
                        valor : getAuthManualService()
                    ],
                    [
                        nombre: "retryEnvAuthorization",
                        valor : GlobalVars.PRO_ENVIRONMENT
                    ],
                    [
                        nombre: "retryAuthorizationServiceToInform",
                        valor : getAuthServiceToInform()
                    ],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
                ]
        ]
    }
	
	

    def getDeployOnCenter2() {
        return [
            nombre                           : "IOP Centro 2",
            authorizationService             : getAuthService(),
            authorizationServiceToInform     : getAuthServiceToInform(),
            envAuthorization                 : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion                       : "LANZAR_JOB",
            destino                          : GlobalVars.ALM_JOB_DEPLOY_PRO,
            actionInTestingPipeline          : true,
            actionWhenHotfixInTestingPipeline: true,
            canBeDisabled                    : true,
            parametros                       :
                [
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
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
						nombre: "oldVersionParam",
						valor : oldVersionInCurrentEnvironment ? oldVersionInCurrentEnvironment : ""
					],
                    [
                        nombre: "originBranchParam",
                        valor : branchName
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
                        nombre: "nextDistributionMode",
                        valor : nextDistributionMode
                    ],
                    [
                        nombre: "retryAuthorizationService",
                        valor : getAuthService()
                    ],
                    [
                        nombre: "retryEnvAuthorization",
                        valor : GlobalVars.PRO_ENVIRONMENT
                    ],
                    [
                        nombre: "retryAuthorizationServiceToInform",
                        valor : getAuthServiceToInform()
                    ],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
                ]
        ]
    }

    def getConfig(boolean isOnlyNewVersion = true, boolean isBeta = false, boolean isCenterOne = false) {
        String nombreAccion = "Get configuration"
        String nombreAccionBeta = " Beta"
        String nombreAccionEstable = " Stable"
        String nombreAccionCenter1 = " Center 1"
        
        if (isCenterOne) {
            nombreAccion+= nombreAccionCenter1
        } else {
            if (!isOnlyNewVersion) {
                if (isBeta) {
                    nombreAccion+= nombreAccionBeta
                } else {
                    nombreAccion+= nombreAccionEstable
                }
            }
            
        }
        

        return [
            nombre                 : "${nombreAccion}",
            authorizationService   : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
            authorizationServiceToInform  : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
            envAuthorization       : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_ACTUATOR_ENV,
            actionInTestingPipeline: true,
            canBeDisabled          : true,
            parametros             :
                [
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],
                    [
                        nombre: "pathToRepoParam",
                        valor : gitUrl
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
                        nombre: "betaParam",
                        valor : isBeta
                    ],
                    [
                        nombre: "centerOneParam",
                        valor : isCenterOne
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



    @Override
    def getAcciones(boolean result) {
        if (result) {
            if (isConfigLib()) {
                return [
                    getCierreReleaseConfigLib()
                ]
			//Nunca puede ser cierre si aun tenemos que desplegar en centro2
            } else if (newVersion && nextDistributionMode != DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2) {
                return [
                    getCierreRelease(),
                    getConfig(),
                    getAccionesRollback()
                ]
            } else if (isFastPath && nextDistributionMode != DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2) {
				return [
					getCierreRelease(),
                    getConfig(),
					getAccionesRollback()
				]
            } else {

                if (nextDistributionMode != DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2 && cannaryType == GlobalVars.CANARY_TYPE_CAMPAIGN) {
                    return [
                        getAccionesRollback(),
						getCierreReleaseCampaign()
                    ]
                } else if (nextDistributionMode == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2) {

                    return [
                        getDeployOnCenter2(),
                        getConfig(true, false, true),
                        getAccionesRollback()
                    ]

                } else if (nextDistributionMode == DistributionModePRO.CONCLUDED) {

                    return [
                        getCierreRelease()
                    ]

                } else {

                    return [
                        incCannary(),
                        getConfig(false, true, false),
                        getConfig(false, false, false),
                        getAccionesRollback()
                    ]

                }
            }
        } else {
            def actions = [
                super.getLogError(),
                retry([
                    authorizationService: retryAuthorizationService ? retryAuthorizationService : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
                    envAuthorization: retryEnvAuthorization ? retryEnvAuthorization : environment,
                    authorizationServiceToInform: retryAuthorizationServiceToInform ? retryAuthorizationServiceToInform : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
                ]),
                createFix()
            ]
            if (this.hasDeployedToCloud) {
                actions += getAccionesRollback()
            }else {
				if (isNowDeployed) {
					if (onlyConfig) {
						actions += getAccionesRollback('false','false')
					}
				}else {
				//Ha petado antes de llegar finalizar el deploy
					if (onlyConfig) {
						actions += getAccionesRollback('true','false')
					}
				}
			}
            return actions
        }
    }


    @Override
    def getDeployed() {
        if (isConfigLib()) {
            return TrazabilidadAppPortalType.ALTA.toString()
        }
        if (hasDeployedToCloud) {
            return TrazabilidadAppPortalType.ALTA.toString()
        } else {
            return TrazabilidadAppPortalType.NADA.toString()
        }
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "IOP_PRO_OK"
        else return "IOP_PRO_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }

    @Override
    def retry(def additionalParameters = [:]) {
        Map dictionary = [
            nombre              : "Reintentar",
            authorizationService: getAuthManualService(),
            authorizationServiceToInform : getAuthServiceToInform(),
            envAuthorization    : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion          : "LANZAR_JOB",
            destino             : jobName,
            canBeDisabled       : true,
            parametros          : []
        ]
        dictionary += additionalParameters

        jobParameters.each { key, value -> // Añadiendo los parametros del job recibidos
            dictionary.parametros.add(["nombre": key, "valor": value])
        }

        return dictionary
    }

}
