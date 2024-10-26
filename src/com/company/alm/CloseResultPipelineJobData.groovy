package com.project.alm

import com.project.alm.*


class CloseResultPipelineJobData extends ResultPipelineData {


    CloseResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map jobParameters) {
        super(environment, gitUrl, gitProject, deploy, jobName, jobParameters)
    }

	def createConfigLibFix() {
		return [
			nombre                 : "Create Fix " + incFix(version),
			tipoAccion             : "LANZAR_JOB",
			destino                : GlobalVars.ALM_JOB_CREATE_CONFIGLIBFIX,
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
	
    def createFix() {
        return [
            nombre                 : "Create Fix " + incFix(version),
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_CREATE_FIX,
            actionInTestingPipeline: false,
            actionWhenConfigurationFixInTestingPipeline: true,
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
                    ]/*,
                    [
                        nombre: "agent",
                        valor : agent
                    ]*/
                ]
        ]

    }
	
	def createConfigFix() {
		return [
			nombre       : "Create Configuration Fix " + incFix(version),
			tipoAccion   : "LANZAR_JOB",
			destino      : GlobalVars.ALM_JOB_CREATE_CONFIGFIX,
            actionInTestingPipeline: true,
            actionWhenConfigurationFixInTestingPipeline: false,
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

    def getAccionesRollback() {
        return [
            nombre                           : "Rollback Artifact",
            tipoAccion                       : "LANZAR_JOB",
            destino                          : GlobalVars.ALM_JOB_ROLLBACK,
            canBeDisabled                    : true,
            actionWhenHotfixInTestingPipeline: true,
            parametros                       :
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
						valor : oldVersionInCurrentEnvironment
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
    
    def getConfig() {
        return [
            nombre                 : "Get configuration",
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
                        valor : false
                    ],
                    [
                        nombre: "centerOneParam",
                        valor : false
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
			if (isMicro()) {
				return [
					createFix(),
					createConfigFix(),
                    getConfig(),
					getAccionesRollback()
				]
			}else if (isConfigLib()) {
				return [
					createConfigLibFix()
				]	
			}else {
				return [
					createFix()
					]
			}
        }else {
            return [
                super.getLogError(),
                super.retry([
                    authorizationService: retryAuthorizationService ? retryAuthorizationService : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
                    envAuthorization: retryEnvAuthorization ? retryEnvAuthorization : environment,
                    authorizationServiceToInform: retryAuthorizationServiceToInform ? retryAuthorizationServiceToInform : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
                ])
            ]
        }
    }

    @Override
    def getDeployed() {
        return TrazabilidadAppPortalType.NADA.toString()
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "CLOSE_OK"
        else return "CLOSE_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_VERSION
    }

    @Override
    def getAuthServiceToInform() {
        if (isBpiRepo()) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
        else if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        return AuthorizationServiceToInformType.MAXIMO.toString()
    }
}
