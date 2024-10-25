package com.caixabank.absis3


import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.TrazabilidadGPLType

class CGSCreateReleaseResultPipelineData extends CGSProvisioningResultPipelineData {

    @Override
    def getAuthService() {
        return AuthorizationServiceToInformType.INFORMATIVE_MAXIMO.toString()
    }

    @Override
    def getAuthServiceToInform() {
        //Restablecer cuando GPL soporte el "succes=true/false"
        //return AuthorizationServiceToInformType.MAXIMO.toString()
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }


    CGSCreateReleaseResultPipelineData(String gitUrl) {
        super(gitUrl)
    }

    @Override
    def getAcciones(boolean result) {
        if (result) {
            return [
                    [
                            nombre              : "Deploy PRO",
                            authorizationService: getAuthService(),
                            authorizationServiceToInform: getAuthServiceToInform(),
                            envAuthorization    : GlobalVars.PRO_ENVIRONMENT,
                            tipoAccion          : "LANZAR_JOB",
                            destino             : GlobalVars.ALM_JOB_CREATE_RC_CGS,
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

    @Override
    def getDeployed() {
        return TrazabilidadGPLType.ALTA.toString()
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }
}
