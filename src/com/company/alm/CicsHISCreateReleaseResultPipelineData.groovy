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

class CicsHISCreateReleaseResultPipelineData extends CicsHISProvisioningResultPipelineData {


    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }


    CicsHISCreateReleaseResultPipelineData(String gitUrl) {
        super(gitUrl)
    }

    @Override
	def getAuthService() {
		return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
	}
	
	@Override
    def getAcciones(boolean result) {
        if (result) {
            return [
                    [
                            nombre              : "Deploy PRO",
                            authorizationService: getAuthService(),
                            envAuthorization    : "pro",
                            appCodeToAuthorize  : agileModuleId,
                            tipoAccion          : "LANZAR_JOB",
                            destino             : GlobalVars.ALM_JOB_CREATE_RC_CICS,
                            canBeDisabled       : true,
                            parametros          :
                                    [
                                            [
                                                    nombre: "pipelineOrigId",
                                                    valor : pipelineOrigId
                                            ],
                                            [
                                                    nombre: "dllFile",
                                                    valor : dllFile
                                            ],
                                            [
                                                    nombre: "svcPath",
                                                    valor : svcPath
                                            ],
                                            [
                                                    nombre: "starterArtifactId",
                                                    valor : starterArtifactId
                                            ],
                                            [
                                                    nombre: "starterArtifactGroupId",
                                                    valor : starterArtifactGroupId
                                            ],
                                            [
                                                    nombre: "starterArtifactVersion",
                                                    valor : starterArtifactVersion
                                            ],
                                            [
                                                    nombre: "cicsHisGroupId",
                                                    valor : cicsHisGroupId
                                            ],
                                            [
                                                    nombre: "cicsHisArtifactId",
                                                    valor : cicsHisArtifactId
                                            ],
                                            [
                                                    nombre: "cicsHisVersion",
                                                    valor : cicsHisVersion
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

    @Override
    def getDeployed() {
        return TrazabilidadGPLType.ALTA.toString()
    }

    @Override
    def getDisabledPolicy() {
        //return GlobalVars.DISABLED_POLICY_BY_BRANCH
        return GlobalVars.DISABLED_POLICY_NONE
    }
}
