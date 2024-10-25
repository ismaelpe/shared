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


class IncCannaryResultPipelineJobData extends ResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        if (isBpiRepo()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else {
            //Restablecer cuando GPL soporte el "succes=true/false"
            //return AuthorizationServiceToInformType.MAXIMO.toString()
             if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
			 else return AuthorizationServiceToInformType.MAXIMO.toString()
        }
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

    int cannaryPercentage = 0
    /**
     * false it is update of an existing artifact
     * true new artifact in the environment
     */

    IncCannaryResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map jobParameters) {
        super(environment, gitUrl, gitProject, deploy, jobName, jobParameters)
    }

    def getCierreRelease() {
        return [
            nombre                 : "End Release",
            authorizationService   : getAuthService(),
            authorizationServiceToInform: getAuthServiceToInform(),
            envAuthorization       : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_CIERRE_RELEASE,
            actionInTestingPipeline: true,
            canBeDisabled          : true,
            parametros             :
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
                        nombre: "versionParam",
                        valor : version
                    ],
					[
						nombre: "oldVersionParam",
						valor : oldVersionInCurrentEnvironment
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
                        valor : true
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

    def incCannary() {
        return [
            nombre              : "Increment Percentage (actual: ${cannaryPercentage} )",
            authorizationService: getAuthService(),
            authorizationServiceToInform: getAuthServiceToInform(),
            envAuthorization    : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion          : "LANZAR_JOB",
            destino             : GlobalVars.ALM_JOB_INCREMENT_CANNARY,
            canBeDisabled       : true,
            parametros          :
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
                        nombre: "repoNameParam",
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
						valor : oldVersionInCurrentEnvironment
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
                        nombre: "actualPercentatgeParam",
                        valor : cannaryPercentage
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
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
                ]
        ]
    }

    def rollbackCanary() {
        return [
            nombre              : "Canary Rollback (actual: ${cannaryPercentage} )",
            authorizationService: getAuthService(),
            authorizationServiceToInform: getAuthServiceToInform(),
            envAuthorization    : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion          : "LANZAR_JOB",
            destino             : GlobalVars.ALM_JOB_INCREMENT_CANNARY,
            canBeDisabled       : true,
            parametros          :
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
                        nombre: "repoNameParam",
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
                        valor : oldVersionInCurrentEnvironment
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
                        nombre: "actualPercentatgeParam",
                        valor : -1
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
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]
                ]
        ]
    }

    def getAccionesRollback() {
        return [
            nombre              : "Rollback Artifact",
            authorizationService: getAuthManualService(),
            authorizationServiceToInform: getAuthServiceToInform(),
            envAuthorization    : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion          : "LANZAR_JOB",
            destino             : GlobalVars.ALM_JOB_ROLLBACK,
            canBeDisabled       : true,
            parametros          :
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
						valor : oldVersionInCurrentEnvironment
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

    
    def getConfig(boolean isBeta = true) {
        String nombreAccion = "Get configuration"
        String nombreAccionBeta = " Beta"
        String nombreAccionEstable = " Stable"
        
        if (isBeta) {
            nombreAccion+= nombreAccionBeta
        } else {
            nombreAccion+= nombreAccionEstable
        }

        return [
            nombre                 : "${nombreAccion}",
            authorizationService   : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
            authorizationServiceToInform  : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
            envAuthorization       : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_ACTUATOR_ENV,
            actionInTestingPipeline: true,
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
                        nombre: "betaParam",
                        valor : isBeta
                    ],
                    [
                        nombre: "centerOneParam",
                        valor : false
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
            String componentNameAndMajor = "${component}-${MavenVersionUtilities.getMajor(version)}"
            if (CanaryUtilities.weHaveReachedFinalPercentage(cannaryPercentage, componentNameAndMajor)) {
                return [
                    getConfig(true),
                    getConfig(false),
                    getCierreRelease(),
                    rollbackCanary(),
                    getAccionesRollback()
                ]
            } else if (cannaryPercentage == 0) {
                return [
                    incCannary(),
                    getConfig(true),
                    getConfig(false),
                    getAccionesRollback()
                ]
            } else {
                return [
                    incCannary(),
                    getConfig(true),
                    getConfig(false),
                    rollbackCanary(),
                    getCierreRelease(),
                    getAccionesRollback()
                ]
            }
        } else {
            IopProResultPipelineJobData iopProResult = new IopProResultPipelineJobData()
            return [
                super.getLogError(),
                super.retry([
                    authorizationService: iopProResult.getAuthService(),
                    envAuthorization: retryEnvAuthorization ? retryEnvAuthorization : environment,
                    authorizationServiceToInform: iopProResult.getAuthServiceToInform()
                ])
            ]
        }
    }

    @Override
    def getDeployed() {
        return TrazabilidadGPLType.NADA.toString()
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "Cannary_OK"
        else return "Cannary_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }
}
