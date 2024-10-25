package com.caixabank.absis3

class ConfigReleaseResultPipelineJobData extends ResultPipelineData {


    ConfigReleaseResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map jobParameters) {
        super(environment, gitUrl, gitProject, deploy, jobName, jobParameters)
    }

	@Override
	def getAuthServiceToInform() {
		if (isBpiRepo()) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
		else if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
		return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
	}
	
	def deployConfigFixToProOnCenter1() {
		return [
			nombre                           : "Distribute ConfigFix PRO - Center 1",
			authorizationService             : getAuthService(),
			envAuthorization                 : GlobalVars.PRO_ENVIRONMENT,
			tipoAccion                       : "LANZAR_JOB",
			destino                          : GlobalVars.ALM_JOB_DEPLOY_PRO_CONFIGFIX,
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
						valor : DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1
					],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
				]
		]
	}
	
	def deployConfigFixToPro() {
		return [
			nombre                           : "Distribute ConfigFix PRO",
			authorizationService             : getAuthService(),
			envAuthorization                 : GlobalVars.PRO_ENVIRONMENT,
			tipoAccion                       : "LANZAR_JOB",
			destino                          : GlobalVars.ALM_JOB_DEPLOY_PRO_CONFIGFIX,
            actionWhenConfigurationFixInTestingPipeline: true,
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
						valor : DistributionModePRO.CANARY_ON_ALL_CENTERS
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
                deployConfigFixToProOnCenter1(),
                deployConfigFixToPro(),
                createFix(),
				createConfigFix()
            ]
        } else {
            ConfigFixResultPipelineData configFixResult = new ConfigFixResultPipelineData()
            return [
                super.getLogError(),
                super.retry([
                    authorizationService: configFixResult.getAuthService(),
                    envAuthorization: retryEnvAuthorization ? retryEnvAuthorization : environment
                ])
            ]
        }
    }

    @Override
    def getDeployed() {
		return TrazabilidadGPLType.ALTA.toString()
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "CREATE_CONFIG_RELEASE_OK"
        else return "CREATE_CONFIG_RELEASE_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }

}
