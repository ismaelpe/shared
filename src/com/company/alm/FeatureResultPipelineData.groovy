package com.project.alm

import com.project.alm.BranchStructure
import com.project.alm.BranchType
import com.project.alm.PipelineStructureType
import com.project.alm.PipelineStructure
import com.project.alm.CIPipelineStructure
import com.project.alm.GlobalVars
import com.project.alm.GarAppType
import com.project.alm.ArtifactSubType
import com.project.alm.TrazabilidadGPLType

class FeatureResultPipelineData extends ResultPipelineData {

    FeatureResultPipelineData(String environment, String gitUrl, String gitProject, boolean deploy) {
        super(environment, gitUrl, gitProject, deploy)
    }

    @Override
    def getAcciones(boolean result) {
        if (result) {
			//Si es una rama FEATURE/BBDD no tiene sentido 
            return [
                    [
                            nombre                  : "Create Merge Request",
                            tipoAccion              : "URL_EXTERNA",
                            destino                 : gitUrl,
                            actionInTestingPipeline : true,
                            actionInFeatureAutoMerge: true,
							upgradeCoreAndCreateRC:   true,
                            canBeDisabled           : false

                    ],
                    [
                        nombre       : "Validate QA (Optional)",
			            tipoAccion   : "LANZAR_JOB",
			            destino      : GlobalVars.ALM_JOB_SONAR,
			            canBeDisabled: true,
			            parametros   :
                            [
                                [
                                    nombre: "pathToRepoParam",
                                    valor : gitUrl
                                ],
                                [
                                    nombre: "originBranchParam",
                                    valor : branchName
                                ],
                                [
                                    nombre: "repoParam",
                                    valor : gitProject
                                ],                    
                                [
                                    nombre: "pipelineOrigId",
                                    valor: pipelineOrigId
                                ],                    
                                [
                                    nombre: "mvnAdditionalParametersParam",
                                    valor: mvnAdditionalParameters
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
                    super.getLogError()
            ]
        }
    }

    @Override
    def getDeployed() {
        return TrazabilidadGPLType.NADA.toString()
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }

    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }

}
