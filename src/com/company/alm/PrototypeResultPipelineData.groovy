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


class PrototypeResultPipelineData extends ResultPipelineData {

	
	String groupParam
	String artifactParam
	String versionParam
	
	
    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }


    PrototypeResultPipelineData(String environment, String gitUrl, String gitProject, boolean deploy) {
        super(environment, gitUrl, gitProject, deploy)
    }

   
    def getAuthService() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }
	
	def getNoneAuthService() {
		return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
	}

    def accionDeployPrototypeTST() {
        return [
            nombre                 : "Deploy Prototype in TST",
            authorizationService   : getNoneAuthService(),
            envAuthorization       : GlobalVars.TST_ENVIRONMENT,
            tipoAccion             : "LANZAR_JOB",
            destino                : GlobalVars.ALM_JOB_DEPLOY_PROTOTYPE,
            canBeDisabled          : true,
            actionInTestingPipeline: true,
			upgradeCoreAndCreateRC: false,
            parametros             :
                [
					[
						nombre: "environmentDestParam",
						valor : GlobalVars.TST_ENVIRONMENT
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
                        nombre: "originBranchParam",
                        valor : branchName
                    ],
					[
						nombre: "pipelineOrigId",
						valor : pipelineOrigId
					],
					[
						nombre: "groupParam",
						valor : groupParam
					],
					[
						nombre: "artifactParam",
						valor : artifactParam
					],
					[
						nombre: "versionParam",
						valor : versionParam
					],
                    [
                        nombre: "loggerLevel",
                        valor : GlobalVars.CONSOLE_LOGGER_LEVEL
                    ]

                ]
        ]
    }
	

	def accionDeployPrototypePRE() {
		return [
			nombre                 : "Deploy Prototype in PRE",
			authorizationService   : getNoneAuthService(),
			envAuthorization       : GlobalVars.PRE_ENVIRONMENT,
			tipoAccion             : "LANZAR_JOB",
			destino                : GlobalVars.ALM_JOB_DEPLOY_PROTOTYPE,
			canBeDisabled          : true,
			actionInTestingPipeline: true,
			upgradeCoreAndCreateRC: false,
			parametros             :
				[
					[
						nombre: "environmentDestParam",
						valor : GlobalVars.PRE_ENVIRONMENT
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
						nombre: "originBranchParam",
						valor : branchName
					],
					[
						nombre: "pipelineOrigId",
						valor : pipelineOrigId
					],
					[
						nombre: "groupParam",
						valor : groupParam
					],
					[
						nombre: "artifactParam",
						valor : artifactParam
					],
					[
						nombre: "versionParam",
						valor : versionParam
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
	
	if (!result) {
		return []
	}else {
			
		return [
			accionDeployPrototypeTST(),
			accionDeployPrototypePRE()
		]
         
     	} 
    }



    @Override
    def getDeployed() {
		return TrazabilidadAppPortalType.NADA.toString()
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_BRANCH
    }

}
