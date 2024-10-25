package com.project.alm

import com.project.alm.*

class TANDEMNewXMLResultPipelineData extends TANDEMProvisioningResultPipelineData {


    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }


    TANDEMNewXMLResultPipelineData(String gitUrl) {
        super(gitUrl)
    }


    @Override
    def getAcciones(boolean result) {
        if (result) {
            return [
                    [
                            nombre              : "Create Release Candidate",
                            authorizationService: AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
                            //FIXME: IDECUA is not progressing pipelines when this is set to MAXIMO. Double check what to use
                            // authorizationService: getAuthService(),
                            envAuthorization    : GlobalVars.TST_ENVIRONMENT,
                            tipoAccion          : "LANZAR_JOB",
                            destino             : GlobalVars.ALM_JOB_PROVISIONING_TANDEM,
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
