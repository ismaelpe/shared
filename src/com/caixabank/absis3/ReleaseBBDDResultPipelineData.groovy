package com.caixabank.absis3

import com.caixabank.absis3.Environment

class ReleaseBBDDResultPipelineData extends ResultPipelineData {

    @Override
    def getAuthServiceToInform() {
        if (isBpiRepo() && version.contains('-RC')) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }

	
	@Override
	def getAuthService() {		
		//return AuthorizationServiceToInformType.MAXIMO.toString()
		return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
	}
    /*
    @Override
    def getAuthServiceToInform() {

        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }*/

    boolean isArchetype
    String archetypeModel
	boolean ifSonarQualityGateOK = true


    ReleaseBBDDResultPipelineData() {
        super()
    }

    ReleaseBBDDResultPipelineData(String environment, String gitUrl, String gitProject, boolean deploy, boolean isArchetype, String archetypeModel) {
        super(environment, gitUrl, gitProject, deploy)
        this.isArchetype = isArchetype
        this.archetypeModel = archetypeModel
    }
  

	def getDeployToPre() {
		return [
			nombre              : "Deploy To PRE BBDD",
			authorizationService: getAuthService(),
			envAuthorization    : GlobalVars.PRE_ENVIRONMENT,
			tipoAccion          : "LANZAR_JOB",
			destino             : GlobalVars.ALM_JOB_DEPLOY_BBDD_RELEASE,
			actionInTestingPipeline: false,
			canBeDisabled       : true,
			parametros          :
				[
					[
						nombre: "envParam",
						valor : Environment.PRE
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
						nombre: "mvnAdditionalParametersParam",
						valor : mvnAdditionalParameters
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
            if (isMicro()) {
				
				return [
				   getDeployToPre()
			    ]
				
            } 
        } else {
            return [
                super.getLogError()
            ]
        }
    }

    @Override
    def getDeployed() {
        if (deployFlag) {
            if (hasDeployedToCloud) {
                if (remoteITOk) {
                    return TrazabilidadGPLType.ALTA.toString()
                } else {
                    return TrazabilidadGPLType.NADA.toString()
                }
            } else {
                return TrazabilidadGPLType.NADA.toString()
            }
        } else {
            return TrazabilidadGPLType.ALTA.toString()
        }
    }

    @Override
    def getResultFlag(boolean result) {
        if (result) return "CREATE_RELEASE_OK"
        else return "CREATE_RELEASE_KO"
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }
}
