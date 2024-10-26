package com.project.alm
import com.project.alm.Environment

class DeployReleaseResultPipelineJobData extends ResultPipelineData {
    boolean isDevops=true

    DeployReleaseResultPipelineJobData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map jobParameters) {
        super(environment, gitUrl, gitProject, deploy, jobName, jobParameters)
    }

	@Override
	def getDeployed() {
		return TrazabilidadAppPortalType.NADA.toString()
	}
	@Override
	def getDisabledPolicy() {
		return GlobalVars.DISABLED_POLICY_NONE
	}

    @Override
    def getAuthService() {
        if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else return AuthorizationServiceToInformType.MAXIMO.toString()		
    }

    def getAuthServiceCierre() {
        if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }


    @Override
    def getAuthServiceToInform() {
        if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }
	

    def getDeployBBDD() {
        return [
            nombre                           : "Deploy To PRO",
            authorizationService             : getAuthService(),
            envAuthorization                 : GlobalVars.PRO_ENVIRONMENT,
            tipoAccion                       : "LANZAR_JOB",
            destino                          : GlobalVars.ALM_JOB_DEPLOY_BBDD_RELEASE,
            actionInTestingPipeline          : true,
            actionWhenHotfixInTestingPipeline: true,
            canBeDisabled                    : true,
            parametros                       :
                [
					[
						nombre: "envParam",
						valor : Environment.PRO
					],
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

	def createRC() {
		return [
			nombre                           : "Create BBDD RC",
			authorizationService             : getAuthServiceCierre(),
			envAuthorization                 : GlobalVars.PRO_ENVIRONMENT,
			tipoAccion                       : "LANZAR_JOB",
			destino                          : GlobalVars.ALM_JOB_CREATE_BBDD_RC,
			actionInTestingPipeline          : true,
			actionWhenHotfixInTestingPipeline: true,
			canBeDisabled                    : true,
			parametros                       :
				[
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
			
			if (Environment.valueOfType(environment)==Environment.PRE) {
				//Deploy a PRO
				return [getDeployBBDD()]
				
			}else {
				//Cierre Release
				return [createRC()]
			}		
           
        } else {
			return []
		}
    }
	
}
