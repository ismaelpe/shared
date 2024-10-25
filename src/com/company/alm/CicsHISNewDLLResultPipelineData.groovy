package com.project.alm

import com.project.alm.*

class CicsHISNewDLLResultPipelineData extends CicsHISProvisioningResultPipelineData {


    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }


    CicsHISNewDLLResultPipelineData(String gitUrl) {
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
                            nombre              : "Create Release Candidate",
                            authorizationService: getAuthService(),
                            envAuthorization    : "tst",
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
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }
}
