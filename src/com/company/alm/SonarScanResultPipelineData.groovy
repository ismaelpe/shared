package com.project.alm

class SonarScanResultPipelineData extends ResultPipelineData {

    boolean ifSonarQualityGateOK = false
	
    boolean sonarQualityGateExecuted = false

    BranchType branchType
    boolean isArchetype
    String archetypeModel

    SonarScanResultPipelineData(String environment, String gitUrl, String gitProject, boolean deploy, BranchType branchType, boolean isArchetype, String archetypeModel) {
        super(environment, gitUrl, gitProject, deploy)
        this.branchType = branchType
        this.isArchetype = isArchetype
        this.archetypeModel = archetypeModel
    }
	
    def getNoneAuthServiceBpi() {
        if (isBpiRepo()) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
        else return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()

	}

	def getNoneAuthService() {
		return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
	}

    /** **************************************************************************************** */
    /** Actions From MASTER */
    /** **************************************************************************************** */
    def getAccionesCR() {
        return [
            nombre                 : "Create Release Candidate",
            authorizationService   : getNoneAuthService(),
            envAuthorization       : GlobalVars.TST_ENVIRONMENT,
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_CREATE_RC,
            canBeDisabled          : true,
            actionInTestingPipeline: true,
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
	
	def getAccionesInstalacionManual() {
		return [
			nombre                 : "Instalación manual del SNAPSHOT en el entorno de TST",
			authorizationService   : getNoneAuthService(),
			envAuthorization       : GlobalVars.TST_ENVIRONMENT,
			tipoAccion             : "LANZAR_JOB",
			destino                : GlobalVars.ALM_JOB_MANUAL_COPY,
			canBeDisabled          : false,
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
            authorizationService   : getNoneAuthService(),
            envAuthorization       : GlobalVars.TST_ENVIRONMENT,
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_CREATE_RC_CONFIGLIB,
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
    
    /** **************************************************************************************** */
    /** Actions From RC */
    /** **************************************************************************************** */
    def getCreateReleaseNonUpgradeTST() {
        return getCreateReleaseNonUpgradeTST(false)
    }

    def getCreateReleaseNonUpgradeTST(boolean automaticExecution) {
        return [
            nombre              : "Create Release - Non Upgrade TST",
            authorizationService: getAuthService(),
            envAuthorization    : GlobalVars.PRE_ENVIRONMENT,
            tipoAccion          : "LANZAR_JOB",
            destino             : GlobalVars.ALM_JOB_CREATE_RELEASE,
            actionInTestingPipeline: automaticExecution,
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

    def getCreateReleaseUpgradeTST() {
        return [
            nombre                 : "Create Release - Upgrade TST",
            authorizationService   : getAuthService(),
            envAuthorization       : GlobalVars.PRE_ENVIRONMENT,
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_CREATE_RELEASE,
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

    def getCreateReleaseConfigLib() {
        return [
            nombre              : "Create Release Config Lib",
            authorizationService: getNoneAuthService(),
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

    /** SONAR */
    def getAccionesSonar(altNombre = null) {
        return [
            nombre                 : altNombre ? altNombre : "Validate QA (Optional)",
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_SONAR,
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
            if (branchType == BranchType.MASTER) {
                return getAccionesFromMaster()
            } else if (branchType == BranchType.FEATURE) {
                return getAccionesFromFeature()
            } else if (branchType == BranchType.RELEASE) {
                return getAccionesFromReleaseCandidate()
            } else {
                return [
                super.getLogError(),
                retry()
            ]
            }
        } else {
            return [
                super.getLogError(),
                getAccionesSonar("Validate QA - Reintentar")
            ]
        }
    }

    def getAccionesFromMaster() {
        if (isMicro()) {
            return [
                getAccionesCR(),                    
                getAccionesInstalacionManual(),
                getAccionesSonar()
            ]
        } else if (isConfigLib()) {
            return [
                getAccionesCRConfigLib(),
                getAccionesSonar()
            ]
        } else {
            return [
                getAccionesCR(),
                getAccionesSonar()
            ]
        }
    }

    def getAccionesFromFeature() {        
        return [
            [
                nombre                  : "Create Merge Request",
                tipoAccion              : "URL_EXTERNA",
                destino                 : gitUrl,
                actionInTestingPipeline : true,
                actionInFeatureAutoMerge: true,
                upgradeCoreAndCreateRC  : true,
                canBeDisabled           : false
            ],
            getAccionesSonar()               
        ]
    }


    def getAccionesFromReleaseCandidate() {
        if (isMicro()) {           
            return [
                getCreateReleaseNonUpgradeTST(true),
                getCreateReleaseUpgradeTST()
            ]           
        } else if (isConfigLib()) {
            return [
                getCreateReleaseConfigLib()
            ]    
        } else {          
            return [
                getCreateReleaseNonUpgradeTST(true)
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
