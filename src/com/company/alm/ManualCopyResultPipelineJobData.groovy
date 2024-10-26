package com.project.alm

class ManualCopyResultPipelineJobData extends ResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }

    int cannaryPercentage
    /**
     * false it is update of an existing artifact
     * true new artifact in the environment
     */
    boolean newVersion
	boolean microParam = true

	ManualCopyResultPipelineJobData(ResultPipelineData resultPipeline) {
		super(resultPipeline.environment, resultPipeline.gitUrl, resultPipeline.gitProject, resultPipeline.deploy, resultPipeline.jobName, resultPipeline.jobParameters)
		this.repoName=resultPipeline.repoName
		this.pipelineOrigId=resultPipeline.pipelineOrigId
		this.branchName=resultPipeline.branchName
		this.artifactType=resultPipeline.artifactType
		this.artifactSubType=resultPipeline.artifactSubType
		this.commitId=resultPipeline.commitId
		this.mvnAdditionalParameters=resultPipeline.mvnAdditionalParameters
	}
	

    ManualCopyResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map jobParameters) {
        super(environment, gitUrl, gitProject, deploy, jobName, jobParameters)
    }

    @Override
    def getAuthService() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()       
    }


    @Override
    def getAcciones(boolean result) {
        if (result) {
            // En caso de que los test vayan correctamente se ofrece la opción de hacer rollback en AppPortal.
            // enviamos una tupla con una accion dentro.
            return [
                getAccionesRollback()
            ]

        } else {
            return []
        }
    }


    @Override
    def getDeployed() {
        return TrazabilidadAppPortalType.ALTA.toString()        
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "MANUAL_COPY_OK"
        else return "MANUAL_COPY_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_NONE
    }

    @Override
    def retry(def additionalParameters = [:]) {
        Map dictionary = [:]
        dictionary += additionalParameters
        return dictionary
    }

    def getAccionesNextDeployment() {

       return []

    }

    def getAccionesRollback() {
        return [
            nombre                           : "Rollback Artifact",
            authorizationService             : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
            authorizationServiceToInform     : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString(),
            envAuthorization                 : GlobalVars.TST_ENVIRONMENT,
            tipoAccion                       : "LANZAR_JOB",
            destino                          : GlobalVars.ALM_JOB_ROLLBACK,
            canBeDisabled                    : true,
            actionWhenHotfixInTestingPipeline: true,
            parametros                       :
                [
                    [
                        nombre: "deployFinishedParam",
                        valor : onlyDeploy
                    ],
                    [
                        nombre: "onlyConfigParam",
                        valor : onlyConfig
                    ],
                    [
                        nombre: "pipelineOrigId",
                        valor : pipelineOrigId
                    ],

                    [
                        nombre: "environmentParam",
                        valor : GlobalVars.TST_ENVIRONMENT
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
                        valor : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
                    ],
                    [
                        nombre: "retryEnvAuthorization",
                        valor : GlobalVars.TST_ENVIRONMENT
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

}
