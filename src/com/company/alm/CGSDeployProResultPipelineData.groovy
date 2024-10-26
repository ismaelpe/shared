package com.project.alm


import com.project.alm.GlobalVars
import com.project.alm.TrazabilidadAppPortalType

class CGSDeployProResultPipelineData extends CGSProvisioningResultPipelineData {

    CGSDeployProResultPipelineData(String gitUrl) {
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
                            destino             : GlobalVars.ALM_JOB_CIERRE_RELEASE_CGS,
                            parametros          :
                                    [
                                            [
                                                    nombre: "pipelineOrigId",
                                                    valor : pipelineOrigId
                                            ],
                                            [
                                                    nombre: "xmlFileIn",
                                                    valor : xmlFileIn
                                            ],
											[
													nombre: "xmlFileOut",
													valor : xmlFileOut
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
                                                    nombre: "operationId",
                                                    valor : operationId
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
        return TrazabilidadAppPortalType.ALTA.toString()
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }
}
