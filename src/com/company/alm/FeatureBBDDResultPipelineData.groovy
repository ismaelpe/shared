package com.project.alm

import com.project.alm.BranchStructure
import com.project.alm.BranchType
import com.project.alm.PipelineStructureType
import com.project.alm.PipelineStructure
import com.project.alm.CIPipelineStructure
import com.project.alm.GlobalVars
import com.project.alm.GarAppType
import com.project.alm.ArtifactSubType
import com.project.alm.TrazabilidadGPLType

class FeatureBBDDResultPipelineData extends ResultPipelineData {
	
	String executionProfile=""
	String almSubFolder="" 

    @Override
    def getAuthServiceToInform() {
        //if (isBpiRepo()) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }


    FeatureBBDDResultPipelineData(String environment, String gitUrl, String gitProject, boolean deploy, String branch, ArtifactType artifactType, ArtifactSubType artifactSubType, String commitIdParam, String pipelineId) {
        super(environment, gitUrl, gitProject, deploy)
		branchName=branch
		commitId=commitIdParam
		pipelineOrigId=pipelineId
    }
   

    def getAuthService() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }

    def getAccionesValidateBBDD() {
        return [
            nombre                 : "Validate SQL scripts",
            authorizationService   : getAuthService(),
            envAuthorization       : GlobalVars.TST_ENVIRONMENT,
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_VALIDATE_BBDD_SCRIPTS,
            canBeDisabled          : true,
            actionInTestingPipeline: true,
			upgradeCoreAndCreateRC: true,
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
						nombre: "commandLiquibaseParam",
						valor : "validate"
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

	def getLiquibaseHistoryBBDD() {
		return getLiquibaseHistoryBBDD(GlobalVars.TST_ENVIRONMENT.toUpperCase())
	}
	def getLiquibaseHistoryBBDD(String environment) {
		return [
			nombre                 : "Get Liquibase ChangeSet History ${environment}",
			authorizationService   : getAuthService(),
			envAuthorization       : GlobalVars.TST_ENVIRONMENT,
			tipoAccion             : "LANZAR_JOB",
			destino                : GlobalVars.ALM_JOB_REPORT_LIQUIBASE,
			canBeDisabled          : false,
			actionInTestingPipeline: true,
			upgradeCoreAndCreateRC:  true,
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
						nombre: "commandLiquibaseParam",
						valor : "history"
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
						nombre: "envParam",
						valor : "${environment}"
					]
				]
		]
	}
	
	def getExportBBDD() {
		return getExportBBDD(GlobalVars.TST_ENVIRONMENT.toUpperCase())
	}
	def getExportBBDD(String environment) {
		return [
			nombre                 : "Export Database Schema ${environment}",
			authorizationService   : getAuthService(),
			envAuthorization       : GlobalVars.TST_ENVIRONMENT,
			tipoAccion             : "LANZAR_JOB",
			destino                : GlobalVars.ALM_JOB_REPORT_LIQUIBASE,
			canBeDisabled          : false,
			actionInTestingPipeline: true,
			upgradeCoreAndCreateRC:  true,
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
						nombre: "commandLiquibaseParam",
						valor : "generateChangeLog"
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
						nombre: "envParam",
						valor : "${environment}"
					]
				]
		]
	}

	def getAccionesReleaseBBDD(String env) {
		return [
			nombre                 : "Validate and Generate SQL Release",
			authorizationService   : getAuthService(),
			envAuthorization       : GlobalVars.TST_ENVIRONMENT,
			tipoAccion             : "LANZAR_JOB",
			destino                : GlobalVars.ALM_JOB_VALIDATE_BBDD_SCRIPTS,
			canBeDisabled          : true,
			actionInTestingPipeline: true,
			upgradeCoreAndCreateRC:  true,
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
						nombre: "commandLiquibaseParam",
						valor : "release"
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
						nombre: "envParam",
						valor : "${env}"
					]
				]
		]
	}
 

    @Override
    def getAcciones(boolean result) {
        if (result) {
            return [
                getAccionesValidateBBDD(),
                getAccionesReleaseBBDD(),
				getExportBBDD(),
				getLiquibaseHistoryBBDD(),				
				getExportBBDD(GlobalVars.PRE_ENVIRONMENT.toUpperCase()),
				getLiquibaseHistoryBBDD(GlobalVars.PRE_ENVIRONMENT.toUpperCase())			
            ]            
        } else {
            return []
        }
    }
  

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_NONE
    }
	
	@Override
	def getDeployed() {
		return TrazabilidadGPLType.NADA.toString()
	}

}
