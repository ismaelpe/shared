import groovy.transform.Field
import com.project.alm.*
import com.project.alm.GarAppType

@Field Map pipelineParams

@Field String artifactSubType
@Field String artifactType
@Field String user
@Field String pipelineOrigenId
@Field String enviroment
@Field String artifact
@Field String pathToRepo
@Field String repoName
@Field String version
@Field String executionProfileParam
@Field String targetAlmFolderParam

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean successPipeline
@Field String componentId
@Field String componentName
@Field String appICPId
@Field String appICP
@Field boolean initGpl

//Pipeline para realizar la baja de una version de un servicio desplegado en
// El ancient no se desinstalará por si es necesario realizar rollback de la baja
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    //Mantener estos parametros/variables por si se deben generar estructuras de datos para enviar a GPL
    artifactSubType = params.artifactSubTypeParam
    artifactType = params.artifactTypeParam
    user = params.userId
    pipelineOrigenId = params.pipelineOrigId
    enviroment = params.environmentParam
    artifact = params.artifactParam
    pathToRepo = params.repoParam
	repoName = params.repoParam
    version = params.versionParam
    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam

	successPipeline = false
	initGpl = false
    /*
     * Pasos a seguir:
     *
     * 1 - Verificar si el componente+version está instalado en bluemix
     * 2 - Verificar en GSA si existen dependencias inversas instaladas en el entorno
     * 3 -
     * 1 - Verificar si existe ancient. En caso de no existir pasar al paso 3
     * 2 - En caso de existir ancient:
     * 	  a - Eliminar ancient
     * 3 - Renombrar artifact actual a ancient.
     * */
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
			JNKMSV = credentials('JNKMSV-USER-TOKEN')
            ICP_CERT = credentials('icp-alm-pro-cert')
            ICP_PASS = credentials('icp-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
            executionProfile = "${executionProfileParam ? executionProfileParam : 'DEFAULT'}"
        }
        stages {
            stage('init-pipeline') {
                steps {
                    initPipelineStep()
                }
            }
            stage('validate-component-and-dependecies') {
                steps {
                    validateComponentAndDependeciesStep()
                }
            }
            stage('delete-component-from-environment') {
                when {
                    expression { componentId != null }
                }
                steps {
                    deleteComponentFromEnvironmentStep()
                }
            }
			stage('undeploy-artifact-from-icp') {
				when {
					expression { componentId != null }
				}
				steps {
                    undeployArtifactFromIcpStep()
				}
			}
            stage('delete-config-files') {
                when {
                    expression { componentId != null }
                }
                steps {
                    deleteConfigFilesStep()
                }
            }
            stage('apimanager-technicalservices-registration') {
                when {
                    expression { ifApiManagerTechnicalServiceRegistrationApplies(pipelineData, pomXmlStructure) }
                }
                steps {
                    apimanagerTechnicalservicesTegistrationStep()
                }
            }
        }
        post {
            success {
                endPipelineSuccessStep()
            }
            failure {
                endPipelineFailureStep()
            }
            always {
                endPipelineAlwaysStep()
            }
        }
    }
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/** 
 * Step initPipelineStep
 */
def initPipelineStep() {
    initGlobalVars(pipelineParams)
    def idApp=MavenUtils.sanitizeArtifactName(artifact, GarAppType.MICRO_SERVICE)
    def appTo=getInfoAppFromCatalog(GarAppType.MICRO_SERVICE.name,idApp)
    pathToRepo=appTo.sourceCode
    printOpen("El identificador de la app es la siguiente ${idApp} la url de git ${appTo.sourceCode} el path to Repo ${pathToRepo}", EchoLevel.ALL)
    pipelineData = new PipelineData(PipelineStructureType.DELETE, "${env.BUILD_TAG}", params)
    pipelineData.initFromComponentVersion(pathToRepo, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), repoName, enviroment, version, artifact)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam);
    pomXmlStructure = new PomXmlStructure(ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), artifact, version, artifact)
    componentName = MavenUtils.sanitizeArtifactName(pomXmlStructure.artifactName, pipelineData.garArtifactType).toUpperCase() + pomXmlStructure.getArtifactMajorVersion()
    
    appICPId=pomXmlStructure.getICPAppId()
    appICP=pomXmlStructure.getICPAppName()
    
    debugInfo(pipelineParams, pomXmlStructure, pipelineData)
    
    currentBuild.displayName = "Delete_Component_Version_${pomXmlStructure.artifactMajorVersion} of ${pomXmlStructure.artifactName}"
    
    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigenId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100");
    initGpl = true
    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")
}

/** 
 * Step validateComponentAndDependeciesStep
 */
def validateComponentAndDependeciesStep() {
    //Validar contra el Enpoint de GSA si existen dependencias inversas del componente
    sendStageStartToGPL(pomXmlStructure, pipelineData, "200")
    printOpen("Procedemos a buscar el componente ${componentName}", EchoLevel.INFO)
	//aps/api/publisher/v2/api/application/PCLD/AB3APP/component/
    //ICPApiResponse response=sendRequestToICPApi("v1/application/${appICPId}/component",null,"GET","${appICP}","",false,false, pipelineData, pomXmlStructure)
	try {
		ICPApiResponse response=sendRequestToICPApi("v2/api/application/PCLD/${appICP}/component/${componentName}",null,"GET","${appICP}","",false,false, pipelineData, pomXmlStructure)
	    if (response.statusCode>=200 && response.statusCode<300) {
			//No nos interesa el id del componente
			componentId=1		
	    }
	}catch (Exception e) {
		componentId=null
	}
    sendStageEndToGPL(pomXmlStructure, pipelineData, "200")
}

/** 
 * Step deleteComponentFromEnvironmentStep
 */
def deleteComponentFromEnvironmentStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "300")
    //Only delete when enviroment is PRO
    deleteAppICP(componentName, componentId, appICPId, appICP, enviroment.toUpperCase(), 'ALL', enviroment == GlobalVars.PRO_ENVIRONMENT )
    sendStageEndToGPL(pomXmlStructure, pipelineData, "300", null, pipelineData.bmxStructure.environment)
}

/** 
 * Step undeployArtifactFromIcpStep
 */
def undeployArtifactFromIcpStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "305")
    undeployArtifactInCatMsv(pipelineData,pomXmlStructure,enviroment,false)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "305")
}

/** 
 * Step deleteConfigFilesStep
 */
def deleteConfigFilesStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "310")
    deleteAppConfigFiles(pomXmlStructure, pipelineData)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "310")
}

/** 
 * Step apimanagerTechnicalservicesTegistrationStep
 */
def apimanagerTechnicalservicesTegistrationStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "500")
 
    printOpen("Publishing swagger contract to API Manager (adpbdd-micro)", EchoLevel.ALL)
    publishSwaggerContract2ApiManager(pipelineData, pomXmlStructure)

    sendStageEndToGPL(pomXmlStructure, pipelineData, "500")
}

/** 
 * Step endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}

/** 
 * Step endPipelineSuccessStep
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("El resultado del pipeline es ${successPipeline}", EchoLevel.ALL)
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
}

/** 
 * Step endPipelineFailureStep
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("El resultado del pipeline es ${successPipeline}", EchoLevel.ALL)
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
}

