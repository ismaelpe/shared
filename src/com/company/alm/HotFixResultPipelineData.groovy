package com.project.alm

import com.project.alm.*

class HotFixResultPipelineData extends ResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        if (isBpiRepo() && version.contains('-RC')) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
        else if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }

    boolean isArchetype
    String archetypeModel


    HotFixResultPipelineData(String environment, String gitUrl, String gitProject, boolean deploy, boolean isArchetype, String archetypeModel) {
        super(environment, gitUrl, gitProject, deploy)
        this.isArchetype = isArchetype
        this.archetypeModel = archetypeModel
    }

    
    def getCreateReleaseNonUpgradeTST() {
        return [
            nombre                 : "Create Release - Non Upgrade TST",
            authorizationService   : getAuthService(),
            envAuthorization       : GlobalVars.PRE_ENVIRONMENT,
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_CREATE_RELEASE,
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
                        nombre: "deployToTstParam",
                        valor : false
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
                        nombre: "isArchetypeParam",
                        valor : isArchetype
                    ],
                    [
                        nombre: "archetypeModelParam",
                        valor : archetypeModel
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
                        nombre: "mvnAdditionalParametersParam",
                        valor : mvnAdditionalParameters
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

	def getCreateReleaseConfigLib() {
		return [
			nombre              : "Create Release Config Lib",
			authorizationService: getAuthService(),
			envAuthorization    : GlobalVars.PRE_ENVIRONMENT,
			tipoAccion          : "LANZAR_JOB",
			destino             : GlobalVars.ALM_JOB_CREATE_RELEASE_CONFIGLIB,
			canBeDisabled       : true,
			actionInTestingPipeline: true,
			parametros          :
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
						nombre: "originBranchParam",
						valor : branchName
					],
					[
						nombre: "commitIdParam",
						valor : commitId
					],
					[
						nombre: "mvnAdditionalParametersParam",
						valor : mvnAdditionalParameters
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

	
    def getCreateReleaseUpgradeTST() {
        return [
            nombre              : "Create Release - Upgrade TST",
            authorizationService: getAuthService(),
            envAuthorization    : GlobalVars.PRE_ENVIRONMENT,
            tipoAccion          : "LANZAR_JOB",
            destino             : GlobalVars.ALM_JOB_CREATE_RELEASE,
            canBeDisabled       : true,
            parametros          :
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
                        nombre: "deployToTstParam",
                        valor : true
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
                        nombre: "isArchetypeParam",
                        valor : isArchetype
                    ],
                    [
                        nombre: "archetypeModelParam",
                        valor : archetypeModel
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
                        nombre: "mvnAdditionalParametersParam",
                        valor : mvnAdditionalParameters
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

    def getPreparationStressTests() {
		return [
			nombre                 : "Prepare stress tests",
			tipoAccion             : "LANZAR_JOB",
			destino                : GlobalVars.ALM_JOB_STRESS_TEST,
            authorizationService   : getAuthService(),
            envAuthorization       : GlobalVars.PRE_ENVIRONMENT,
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
						valor : branchName
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
						nombre: "scaleCPUCoresParam",
						valor : "M"
					],
					[
						nombre: "scaleMemoryParam",
						valor : "M"
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

    @Override
    def getAcciones(boolean result) {
        if (result) {
            if (isMicro()) {
                return [
                    getCreateReleaseNonUpgradeTST(),
                    getCreateReleaseUpgradeTST(),
                    getPreparationStressTests()
                ]
            } else if (isConfigLib()) {
			    return [
				   getCreateReleaseConfigLib()
			    ]
		    } else {
                return [
                    getCreateReleaseNonUpgradeTST()
                ]
            }

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
