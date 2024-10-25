package com.project.alm

import com.project.alm.*

class ConfigLibFixResultPipelineData extends ResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        if (isBpiRepo() && version.contains('-RC')) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
        else if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }

    boolean isArchetype
    String archetypeModel


    ConfigLibFixResultPipelineData(String environment, String gitUrl, String gitProject, boolean deploy, boolean isArchetype, String archetypeModel) {
        super(environment, gitUrl, gitProject, deploy)
        this.isArchetype = isArchetype
        this.archetypeModel = archetypeModel
    }

   
    def getCreateReleaseConfigFix() {
        return [
                nombre              : "Create Configuration Lib Release",
                authorizationService: getAuthService(),
                envAuthorization    : GlobalVars.PRE_ENVIRONMENT,
                tipoAccion          : "LANZAR_JOB",
                destino             : GlobalVars.ALM_JOB_CREATE_RELEASE_CONFIGLIB,
                canBeDisabled       : true,
                parametros          :
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
                                        nombre: "artifactTypeParam",
                                        valor : artifactType
                                ],
                                [
                                        nombre: "artifactSubTypeParam",
                                        valor : artifactSubType
                                ],
                                [
                                        nombre: "pipelineOrigId",
                                        valor : pipelineOrigId
                                ],
                                [
                                        nombre: "commitIdParam",
                                        valor : commitId
                                ],
                                [
                                        nombre: "executionProfileParam",
                                        valor : executionProfile
                                ],
                                [
                                        nombre: "targetAlmFolderParam",
                                        valor : almSubFolder
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
            return []
        } else {
            return [
                    super.getLogError()
            ]
        }
    }

    @Override
    def getDeployed() {
		return TrazabilidadGPLType.ALTA.toString()
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "CREATE_CONFIG_RELEASE_OK"
        else return "CREATE_CONFIG_RELEASE_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }
}
