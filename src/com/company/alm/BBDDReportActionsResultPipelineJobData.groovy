package com.caixabank.absis3

import com.caixabank.absis3.BranchStructure
import com.caixabank.absis3.BranchType
import com.caixabank.absis3.PipelineStructureType
import com.caixabank.absis3.PipelineStructure
import com.caixabank.absis3.CIPipelineStructure
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.GarAppType
import com.caixabank.absis3.ArtifactSubType
import com.caixabank.absis3.TrazabilidadGPLType

class BBDDReportActionsResultPipelineJobData extends ResultPipelineData {
	
	def getAuthService() {
		return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
	}
	
	
	@Override
	def getAuthServiceToInform() {
		//if (isBpiRepo()) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
		return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
	}
	


    BBDDReportActionsResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map JobParameters) {
        super(environment, gitUrl, gitProject, deploy, jobName, JobParameters)
    }

    @Override
    def getAcciones(boolean result) {
       return [
		   getLiquibaseHistoryBBDD(),
		   getExportBBDD()
		   ]
    }

    @Override
    def getDeployed() {
        return TrazabilidadGPLType.NADA.toString()
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "REPORT_BBDD_STATUS_OK"
        else return "REPORT_BBDD_STATUS_OK"
    }
	
	@Override
	def getDisabledPolicy() {
		return GlobalVars.DISABLED_POLICY_NONE
	}
	
	def getLiquibaseHistoryBBDD() {
		return [
			nombre                 : "Get Liquibase ChangeSet History",
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
                    ]
				]
		]
	}
	
	def getExportBBDD() {
		return [
			nombre                 : "Export Database Schema",
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
                    ]
				]
		]
	}



}
