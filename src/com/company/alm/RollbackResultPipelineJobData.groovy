package com.caixabank.absis3

class RollbackResultPipelineJobData extends ResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }

    RollbackResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy, String branchName, String jobName, Map jobParameters) {
        super(environment, gitUrl, gitProject, deploy, jobName, jobParameters, branchName)
    }


    @Override
    def getAcciones(boolean result) {
        if (result) {
            if (GlobalVars.PRO_ENVIRONMENT == environment || GlobalVars.PRE_ENVIRONMENT == environment) {
                return [
                    createFix()
                ]
            }
            return []
        } else {
            return [
                super.getLogError(),
                super.retry([
                    authorizationService: retryAuthorizationService ? retryAuthorizationService : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
                    envAuthorization: retryEnvAuthorization ? retryEnvAuthorization : environment,
                    authorizationServiceToInform: retryAuthorizationServiceToInform ? retryAuthorizationServiceToInform : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
                ])
            ]
        }

    }

    @Override
    def getDeployed() {
        return TrazabilidadGPLType.BAJA.toString()
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "ROLLBACK_OK"
        else return "ROLLBACK_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }

    @Override
    String incFix(String versionAIncrementar) {
        String version
        if (branchName.indexOf(GlobalVars.HOTFIX_BRANCH) != -1) {
            // Leemos la versión del hotfix del branchName
            version = branchName.indexOf("v") == -1 ? versionAIncrementar : branchName[branchName.indexOf("v") + 1..-1]
        } else {
            version = versionAIncrementar
        }
        return super.incFix(version)
    }

    def createFix() {
        return [
            nombre       : "Create Fix " + incFix(version),
            tipoAccion   : "LANZAR_JOB",
            destino      : GlobalVars.ALM_JOB_CREATE_FIX,
            canBeDisabled: true,
            parametros   :
                [
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],
                    [
                        nombre: "pathToRepoParam",
                        valor : gitUrl
                    ],
                    [
                        nombre: "originBranchParam",
                        valor : "master"
                    ],
                    [
                        nombre: "repoParam",
                        valor : gitProject
                    ],
                    [
                        nombre: "versionParam",
                        valor : version
                    ],
                    [
                        nombre: "artifactSubTypeParam",
                        valor : artifactSubType
                    ],
                    [
                        nombre: "artifactTypeParam",
                        valor : artifactType
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
}
