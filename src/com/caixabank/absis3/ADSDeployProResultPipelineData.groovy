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

class ADSDeployProResultPipelineData extends ADSProvisioningResultPipelineData {

    ADSDeployProResultPipelineData(String gitUrl) {
        super(gitUrl)
    }
	
	@Override
	def getAuthServiceToInform() {
		return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
	}

    @Override
    def getAcciones(boolean result) {
        if (result) {
            return [
                    [
                            nombre              : "End Release",
                            authorizationService: getAuthService(),
                            envAuthorization    : GlobalVars.PRO_ENVIRONMENT,
                            authorizationServiceToInform: getAuthServiceToInform(),
                            tipoAccion          : "LANZAR_JOB",
                            destino             : GlobalVars.ALM_JOB_CIERRE_RELEASE_ADS,
                            parametros          :
                                    [
                                            [
                                                    nombre: "pipelineOrigId",
                                                    valor : pipelineOrigId
                                            ],
                                            [
                                                    nombre: "xmlFile",
                                                    valor : xmlFile
                                            ],
                                            [
                                                    nombre: "artifactId",
                                                    valor : artifactId
                                            ],
                                            [
                                                    nombre: "artifactGroupId",
                                                    valor : artifactGroupId
                                            ],
                                            [
                                                    nombre: "artifactVersion",
                                                    valor : artifactVersion
                                            ],
                                            [
                                                    nombre: "transactionId",
                                                    valor : transactionId
                                            ],
                                            [
                                                    nombre: "nextEnvironment",
                                                    valor : nextEnvironment
                                            ],
                                            [
                                                    nombre: "originBranchParam",
                                                    valor : originBranch
                                            ]
                                            ,
                                            [
                                                    nombre: "userEmail",
                                                    valor : userEmail
                                            ]
                                            ,
                                            [
                                                    nombre: "user",
                                                    valor : user
                                            ],
                                            [
                                                    nombre: "gitUrl",
                                                    valor : gitUrl
                                            ],
                                            [
                                                nombre: "loggerLevel",
                                                valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                                            ]
                                    ]
                    ]
            ]
        } else {
            return [
            ]
        }
    }

	def getAuthService() {
		return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
	};
	
    @Override
    def getDeployed() {
        return TrazabilidadGPLType.ALTA.toString()
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }
}
