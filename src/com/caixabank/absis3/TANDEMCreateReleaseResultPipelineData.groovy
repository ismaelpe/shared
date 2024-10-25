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

class TANDEMCreateReleaseResultPipelineData extends TANDEMProvisioningResultPipelineData {

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


    TANDEMCreateReleaseResultPipelineData(String gitUrl) {
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
