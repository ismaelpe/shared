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


class CloseCampaignResultPipelineJobData extends ResultPipelineData {
    

    CloseCampaignResultPipelineJobData(String environment, String userId, String pipelineOrigId, String jobName, Map jobParameters) {
        this.environment = environment
        this.pipelineOrigId = pipelineOrigId
        this.jobName = jobName
        this.jobParameters = jobParameters
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
        return AuthorizationServiceToInformType.MAXIMO.toString()   }


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
	
	
	
	def incCampaignCannary(int cannaryPercentage) {
		return [
			nombre              : "Increment Percentage (actual: ${cannaryPercentage})",
            authorizationService: "INFORMATIVE_MAXIMO",
            authorizationServiceToInform: "INFORMATIVE_MAXIMO",
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
						valor : cannaryPercentage
					],
					[
						nombre: "targetAlmFolder",
						valor : almSubFolder
					],
					[
						nombre: "isRollbackParam",
						valor : "anotherOperation"
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
          return [ incCampaignCannary(0) ]
        } else {
            return [
				notifyCampaignCannary(),
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
        if (result) return "Close_Campaign_OK"
        else return "Close_Campaign_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }
}
