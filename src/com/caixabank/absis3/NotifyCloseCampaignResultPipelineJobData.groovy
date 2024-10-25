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


class NotifyCloseCampaignResultPipelineJobData extends ResultPipelineData {
    
    int cannaryPercentage = 0
	boolean areAppsPending=false

    NotifyCloseCampaignResultPipelineJobData(String environment, String userId, String pipelineOrigId, String jobName, Map jobParameters) {
        super()
		super.environment = environment
        super.pipelineOrigId = pipelineOrigId
        super.jobName = jobName
        super.jobParameters = jobParameters
    }
    
    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }

    @Override
    def getAuthService() {
        //return AuthorizationServiceToInformType.INFORMATIVE_MAXIMO.toString()
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }

    def getAuthManualService() {
        return AuthorizationServiceToInformType.MAXIMO.toString()
    }

	def notifyCampaignCannary() {
		return [
			nombre              : "Notify Close Campaign",
			authorizationService: getAuthService(),
			authorizationServiceToInform: getAuthServiceToInform(),
			envAuthorization    : GlobalVars.PRO_ENVIRONMENT,
			tipoAccion          : "LANZAR_JOB",
			destino             : GlobalVars.ALM_JOB_NOTIFY_CLOSE_CAMPAIGN,
			canBeDisabled       : true,
			parametros          :
				[
					[
						nombre: "pipelineOriginId",
						valor : pipelineOrigId
					],
					[
						nombre: "currentPercentage",
						valor : 0
					],
					[
						nombre: "targetAlmFolder",
						valor : almSubFolder
					],
					[
						nombre: "isRollbackParam",
						valor : "rollback"
					],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
				]
		]
	}
	
	def closeCampaignCannary() {
		return [
			nombre              : "Close Campaign",
            authorizationService: "INFORMATIVE_MAXIMO",
            authorizationServiceToInform: "INFORMATIVE_MAXIMO",
            envAuthorization    : GlobalVars.PRO_ENVIRONMENT,
			tipoAccion          : "LANZAR_JOB",
			destino             : GlobalVars.ALM_JOB_CLOSE_CAMPAIGN,
			canBeDisabled       : true,
			parametros          :
				[
					[
						nombre: "pipelineOriginId",
						valor : pipelineOrigId
					],
					[
						nombre: "targetAlmFolder",
						valor : almSubFolder
					],
					[
						nombre: "isRollbackParam",
						valor : "rollback"
					]	,
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
				]
			]
	}

	def resetCampaignCannary() {
		return [
			nombre              : "Rollback Cannary (actual: 100)",
			authorizationService: getAuthService(),
			authorizationServiceToInform: getAuthServiceToInform(),
			envAuthorization    : GlobalVars.PRO_ENVIRONMENT,
			tipoAccion          : "LANZAR_JOB",
			destino             : GlobalVars.ALM_JOB_INCREMENT_CAMPAIGN_CANNARY,
			canBeDisabled       : true,
			parametros          :
				[
					[
						nombre: "pipelineOriginId",
						valor : pipelineOrigId
					],
					[
						nombre: "currentPercentage",
						valor : 0
					],
					[
						nombre: "targetAlmFolder",
						valor : almSubFolder
					],
					[
						nombre: "isRollbackParam",
						valor : "rollback"
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
			if (areAppsPending==true) {
				return [
					notifyCampaignCannary(),
					resetCampaignCannary()
				]
			}else {
				return [
					closeCampaignCannary(),
					notifyCampaignCannary(),
					resetCampaignCannary()
				]	
			}
        } else {
            return [
                super.getLogError(),
                super.retry()
            ]
        }
    }

    @Override
    def getDeployed() {
        return TrazabilidadGPLType.NADA.toString()
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "Campaign_Cannary_OK"
        else return "Campaign_Cannary_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }
}