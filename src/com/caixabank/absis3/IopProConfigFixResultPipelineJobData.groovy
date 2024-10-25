package com.caixabank.absis3

class IopProConfigFixResultPipelineJobData extends ResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }

    IopProConfigFixResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map jobParameters) {
        super(environment, gitUrl, gitProject, deploy, jobName, jobParameters)
    }

    @Override
    def getAuthService() {
        //Cambiar BPI cuando se confirme si hay que enviar a AGILE_WORK para el Cannary/EndRelease/Rollback
        if (isBpiRepo()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else return AuthorizationServiceToInformType.INFORMATIVE_MAXIMO.toString()
    }

    def getAuthManualService() {
        //Cambiar BPI cuando se confirme si hay que enviar a AGILE_WORK para el Cannary/EndRelease/Rollback
        if (isBpiRepo()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else return AuthorizationServiceToInformType.MAXIMO.toString()
    }
    /*
     * En este caso actionInTestingPipeline: true aparece tanto en cierre release como en inccannary porque son situaciones disjuntas
     */

    def getCierreRelease() {
        return [
            nombre                        : "End Release",
            authorizationService          : getAuthService(),
            envAuthorization              : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion                    : "LANZAR_JOB",
            destino                       : GlobalVars.ALM_JOB_CIERRE_RELEASE,
            canBeDisabled                 : true,
            actionWhenConfigurationFixInTestingPipeline       : true,
            triggerIfComponentIsNewVersion: true,
            parametros                    :
                [
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],
                    [
                        nombre: "environmentParam",
                        valor : GlobalVars.PRO_ENVIRONMENT
                    ],
                    [
                        nombre: "pathToRepoParam",
                        valor : gitUrl
                    ],
                    [
                        nombre: "repoParam",
                        valor : gitProject
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
                        nombre: "originBranchParam",
                        valor : branchName
                    ],
                    [
                        nombre: "oldVersionParam",
                        valor : oldVersionInCurrentEnvironment ? oldVersionInCurrentEnvironment : ""
                    ],
                    [
                        nombre: "versionParam",
                        valor : version
                    ],
                    [
                        nombre: "artifactParam",
                        valor : artifact
                    ],
                    [
                        nombre: "commitIdParam",
                        valor : commitId
                    ],
                    [
                        nombre: "existAncientParam",
                        valor : false //OPR - Sending false due to ignore cannary's related steps
                    ],
                    [
                        nombre: "isMicroParam",
                        valor : true
                    ],
                    [
                        nombre: "componentParam",
                        valor : component
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
                        nombre: "retryAuthorizationService",
                        valor : getAuthService()
                    ],
                    [
                        nombre: "retryEnvAuthorization",
                        valor : GlobalVars.PRO_ENVIRONMENT
                    ],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
                ]
        ]
    }

    def deployConfigFixToProOnCenter2() {
        return [
            nombre                           : "Distribute ConfigFix PRO - Center 2",
            authorizationService             : getAuthService(),
            envAuthorization                 : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion                       : "LANZAR_JOB",
            destino                          : GlobalVars.ALM_JOB_DEPLOY_PRO_CONFIGFIX,
            actionInTestingPipeline          : true,
            actionWhenHotfixInTestingPipeline: true,
            canBeDisabled                    : true,
            parametros                       :
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
                        nombre: "repoParam",
                        valor : gitProject
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
                        nombre: "versionParam",
                        valor : version
                    ],
                    [
                        nombre: "originBranchParam",
                        valor : branchName
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
                        nombre: "nextDistributionMode",
                        valor : nextDistributionMode
                    ],
                    [
                        nombre: "retryAuthorizationService",
                        valor : getAuthService()
                    ],
                    [
                        nombre: "retryEnvAuthorization",
                        valor : GlobalVars.PRO_ENVIRONMENT
                    ],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
                ]
        ]
    }

    def createFix() {
        return [
            nombre                 : "Create Fix " + incFix(version),
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_CREATE_FIX,
            canBeDisabled          : true,
            parametros             :
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

    def createConfigFix() {
        return [
            nombre       : "Create Configuration Fix " + incFix(version),
            tipoAccion   : "LANZAR_JOB",
            destino      : GlobalVars.ALM_JOB_CREATE_CONFIGFIX,
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

    def getAccionesRollback() {
        return [
            nombre                           : "Rollback Artifact",
            authorizationService             : getAuthManualService(),
            authorizationServiceToInform     : getAuthServiceToInform(),
            envAuthorization                 : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion                       : "LANZAR_JOB",
            destino                          : GlobalVars.ALM_JOB_ROLLBACK,
            canBeDisabled                    : true,
            actionWhenHotfixInTestingPipeline: true,
            parametros                       :
                [
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],
                    [
                        nombre: "environmentParam",
                        valor : GlobalVars.PRO_ENVIRONMENT
                    ],
                    [
                        nombre: "ignoreExistingAncientParam",
                        valor : false
                    ],
                    [
                        nombre: "forceAllCentersParam",
                        valor : true
                    ],
                    [
                        nombre: "pathToRepoParam",
                        valor : gitUrl
                    ],
                    [
                        nombre: "repoParam",
                        valor : gitProject
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
                        nombre: "originBranchParam",
                        valor : branchName
                    ],
                    [
                        nombre: "versionParam",
                        valor : version
                    ],
                    [
                        nombre: "oldVersionParam",
                        valor : oldVersionInCurrentEnvironment ? oldVersionInCurrentEnvironment : ""
                    ],
                    [
                        nombre: "artifactParam",
                        valor : artifact
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
                        nombre: "nextDistributionMode",
                        valor : nextDistributionMode
                    ],
                    [
                        nombre: "retryAuthorizationService",
                        valor : getAuthManualService()
                    ],
                    [
                        nombre: "retryEnvAuthorization",
                        valor : GlobalVars.PRO_ENVIRONMENT
                    ],
                    [
                        nombre: "retryAuthorizationServiceToInform",
                        valor : getAuthServiceToInform()
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
			if (nextDistributionMode == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2) {
	            return [
					deployConfigFixToProOnCenter2(),
                    getAccionesRollback()
	            ]
        	} else {
				return [
					getCierreRelease()
				]
			}
        } else {
            return [
                super.getLogError(),
                retry([
                    authorizationService: retryAuthorizationService ? retryAuthorizationService : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
                    envAuthorization: retryEnvAuthorization ? retryEnvAuthorization : environment,
                    authorizationServiceToInform: retryAuthorizationServiceToInform ? retryAuthorizationServiceToInform : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
                ]),
                createFix(),
                createConfigFix()
            ]
        }
    }


    @Override
    def getDeployed() {
        return TrazabilidadGPLType.ALTA.toString()
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "Administrative_PRO_OK"
        else return "Administrative_PRO_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }

    @Override
    def retry(def additionalParameters = [:]) {
        Map dictionary = [
            nombre              : "Reintentar",
            authorizationService: getAuthService(),
            envAuthorization    : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion          : "LANZAR_JOB",
            destino             : jobName,
            canBeDisabled       : true,
            parametros          : []
        ]
        dictionary += additionalParameters

        jobParameters.each { key, value -> // Añadiendo los parametros del job recibidos
            dictionary.parametros.add(["nombre": key, "valor": value])
        }

        return dictionary
    }

}
