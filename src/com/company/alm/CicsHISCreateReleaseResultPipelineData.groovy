package com.project.alm

import com.project.alm.BranchStructure
import com.project.alm.BranchType
import com.project.alm.PipelineStructureType
import com.project.alm.PipelineStructure
import com.project.alm.CIPipelineStructure
import com.project.alm.GlobalVars
import com.project.alm.GarAppType
import com.project.alm.ArtifactSubType
import com.project.alm.TrazabilidadAppPortalType

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
        return TrazabilidadAppPortalType.ALTA.toString()
    }

    @Override
    def getDisabledPolicy() {
        //return GlobalVars.DISABLED_POLICY_BY_BRANCH
        return GlobalVars.DISABLED_POLICY_NONE
    }
}
