package com.project.alm

class ReleaseResultPipelineJobData extends ResultPipelineData {
	boolean isDevops=true

	ReleaseResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map jobParameters) {
		super(environment, gitUrl, gitProject, deploy, jobName, jobParameters)
	}


	def getCierreRelease() {
		return [
			nombre              : "End Release",
			authorizationService: getAuthServiceCierre(),
			envAuthorization    : GlobalVars.PRO_ENVIRONMENT,
			tipoAccion          : "LANZAR_JOB",
			destino             : GlobalVars.ALM_JOB_CIERRE_RELEASE,
			canBeDisabled       : true,
			actionInTestingPipeline: true,
			actionWhenHotfixInTestingPipeline: true,
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
						valor : false
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
						valor : getAuthServiceCierre()
					],
					[
						nombre: "retryEnvAuthorization",
						valor : GlobalVars.PRO_ENVIRONMENT
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
			nombre              : "Deploy to Pro Config Lib",
			authorizationService: getAuthService(),
			envAuthorization    : GlobalVars.PRO_ENVIRONMENT,
			tipoAccion          : "LANZAR_JOB",
			destino             : GlobalVars.ALM_JOB_DEPLOY_PRO_CONFIGLIB,
			actionInTestingPipeline: true,
			actionWhenHotfixInTestingPipeline: true,
			canBeDisabled       : true,
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
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
				]
		]
	}


	@Override
	def getAuthService() {
		if (isBpiRepo()) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
		else if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
		else return AuthorizationServiceToInformType.MAXIMO.toString()
	}

	def getAuthServiceCierre() {
		if (isBpiRepo()) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
		else if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
		else return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
	}


	@Override
	def getAuthServiceToInform() {
		if (isBpiRepo()) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
		else if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
		return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
	}
	
	def getDeployCanaryOnBothCentersFastPath() {
		return [
			nombre                           : "IOP FastPath a PRO (Sin Cannary)",
			authorizationService             : getAuthService(),
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
						valor : ((oldVersionInNextEnvironment==null)?"":oldVersionInNextEnvironment)
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
						nombre: "retryAuthorizationService",
						valor : getAuthService()
					],
					[
						nombre: "retryEnvAuthorization",
						valor : GlobalVars.PRO_ENVIRONMENT
					],
					[
						nombre: "fastPathParam",
						valor : true
					],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
				]
		]
	}

	def getDeployCanaryOnBothCenters(String type) {
		return [
			nombre                           : "IOP Beta ${type}",
			authorizationService             : getAuthService(),
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
						valor : ((oldVersionInNextEnvironment==null)?"":oldVersionInNextEnvironment)
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
						nombre: "retryAuthorizationService",
						valor : getAuthService()
					],
					[
						nombre: "retryEnvAuthorization",
						valor : GlobalVars.PRO_ENVIRONMENT
					],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
				]
		]
	}

	def getDeployOnCenter1() {
		return [
			nombre                           : "IOP Centro 1",
			authorizationService             : getAuthService(),
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
						valor : ((oldVersionInNextEnvironment==null)?"":oldVersionInNextEnvironment)
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
	
	def createConfigLibFix() {
		return [
			nombre                 : "Create Fix " + incFix(version),
			tipoAccion             : "LANZAR_JOB",
			destino                : GlobalVars.ALM_JOB_CREATE_CONFIGLIBFIX,
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
	
	def getAccionesRollback(def onlyConfig ="false", def onlyDeploy = "true") {
		String nombreAccion="Rollback Artifact"
		if ("true".equals(onlyConfig) && "false".equals(onlyDeploy)) {
			nombreAccion=nombreAccion+ " (Only config)"
		}
		return [
			nombre       : "${nombreAccion}",
			tipoAccion   : "LANZAR_JOB",
			destino      : GlobalVars.ALM_JOB_ROLLBACK,
			canBeDisabled: true,
			parametros   :
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
						valor : GlobalVars.PRE_ENVIRONMENT
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
						valor : ((oldVersionInCurrentEnvironment==null)?"":oldVersionInCurrentEnvironment)
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
			if (isMicro() && isDevops==true) {
				def actions = [];

				actions.push(getAccionesNextDeployment("(Con Cannary)"))
				if (getAccionesNextDeploymentFastPath() != null) actions.push(getAccionesNextDeploymentFastPath())
				actions.push(getAccionesRollback())
				actions.push(createFix())
				actions.push(getPreparationStressTests())

				return actions;
			} else if (isConfigLib()) {
				return [
					getCierreReleaseConfigLib(),
					createConfigLibFix()
				]
			} else if  (isMicro() && isDevops==false) {
				return [
					getAccionesNextDeployment("(En campaña)"),
					getAccionesRollback(),
					createFix()
				]
			
			}else {
				return [
					getCierreRelease(),
					createFix()
				]
			}
		} else {
			if (isConfigLib() || version?.contains("-RC")) {
				ReleaseResultPipelineData releaseResult = new ReleaseResultPipelineData()
				return [
						super.getLogError(),
						super.retry([
							authorizationService: releaseResult.getAuthService(),
							envAuthorization: retryEnvAuthorization ? retryEnvAuthorization : environment
						])
					]
				
			} else {
				ReleaseResultPipelineData releaseResult = new ReleaseResultPipelineData()
				//Aqui tenemos que añadir el rollback 
				//si es micro
				//si se ha copiado la config
				if (isMicro()) {
					def actions =[
							super.getLogError(),
							super.retry([
								authorizationService: releaseResult.getAuthService(),
								envAuthorization: retryEnvAuthorization ? retryEnvAuthorization : environment
							])
				    ]
					
					
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
					if (isTagged) {
						actions += createFix()
					}
					return actions
					
					
				}else {
					def actions =[
						super.getLogError(),
						super.retry([
							authorizationService: releaseResult.getAuthService(),
							envAuthorization: retryEnvAuthorization ? retryEnvAuthorization : environment
						])
				    ]
					if (isTagged) {
						actions += createFix()
					}
					return actions
				}	
			}
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

	def getAccionesNextDeployment(String type) {

		if (nextDistributionMode == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1) {

			return getDeployOnCenter1()

		} else {

			return getDeployCanaryOnBothCenters(type)

		}

	}
	def getAccionesNextDeploymentFastPath() {

		if (nextDistributionMode != DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1) {

			return getDeployCanaryOnBothCentersFastPath()

		}

	}
	
}
