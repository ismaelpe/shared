 import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

//Mantener estos parametros/variables por si se deben generar estructuras de datos para enviar a AppPortal
@Field String gitURL
@Field String gitCredentials
@Field String jenkinsPath

@Field String originBranch
@Field String pathToRepo
@Field String repoName
@Field String artifactSubType
@Field String artifactType
@Field String pipelineOrigenId
@Field String enviroment
@Field String artifact
@Field String version
@Field String versionToRollbackTo
@Field String user
@Field String executionProfileParam
@Field String targetAlmFolderParam
@Field String currentDistributionMode
@Field String onlyConfig
@Field String deployFinished
@Field boolean ignoreExistingAncient
@Field boolean forceAllCenters
@Field String loggerLevel

@Field BmxStructure bmxStructure
@Field DeployStructure deployStructure
@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean successPipeline

@Field boolean existAncient
@Field String artifactId

//Pipeline para realizar el rollback de un servicio desplegado en BMX a su "ancient"
//Si el ancient no existe únicamente realizaremos un "delete" del servicio.
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    //Mantener estos parametros/variables por si se deben generar estructuras de datos para enviar a AppPortal
    gitURL = "https://git.svb.digitalscale.es/"
    gitCredentials = "GITLAB_CREDENTIALS"
    jenkinsPath = "alm/services"

    originBranch = params.originBranchParam
    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam
    artifactSubType = params.artifactSubTypeParam
    artifactType = params.artifactTypeParam
    pipelineOrigenId = params.pipelineOrigId
    user = params.userId
    enviroment = params.environmentParam
    artifact = params.artifactParam
    version = params.versionParam
	versionToRollbackTo = params.oldVersionParam
    ignoreExistingAncient = params.ignoreExistingAncientParam.toString().toBoolean()
    forceAllCenters = params.forceAllCentersParam.toString().toBoolean()
    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam
    currentDistributionMode = params.nextDistributionMode
	onlyConfig = params.onlyConfigParam
	deployFinished = params.deployFinishedParam
    loggerLevel = params.loggerLevel
    
    successPipeline = false

    existAncient = false
    artifactId = artifact + "-" + version

    /*
     * Pasos a seguir:
     *
     * 1 - Verificar si existe ancient.En caso de no existir ancient saltamos al paso 3.
     * 2 - En caso de existir ancient:
     * 	  a - Mapear con la ruta new al ancient.
     *    b - Hacer un scale para darle instancias.
     * 	  c - Testear.
     * 	  d	- Hacer un unmap de la ruta new al ancient.
     *    e - Mapear con las rutas finales reales al ancient.
     *    f - Hacer un unmap de las rutas finales reales al artifact actual.
     * 3 -Hacer delete del artifact actual y, en caso de existir ancient, renombrar ancient a artifact actual.
     *
     * */
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        environment {
            AppPortal = credentials('IDECUA-JENKINS-USER-TOKEN')
			JNKMSV = credentials('JNKMSV-USER-TOKEN')			
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
            https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
            executionProfile = "${executionProfileParam ? executionProfileParam : 'DEFAULT'}"
        }
        stages {
            stage('init-pipeline') {
                steps {
                    initPipelineStep()
                }
            }
             stage('coherence-validation') {
                steps {
                    coherenceValidationStep()
                }
            }
			stage('check-Cloud-availiability'){
				when {
					expression { isCloudEnabled(enviroment) && "false".equals(onlyConfig) }
				}
				steps {
                    checkCloudAvailiabilityStep()
				}
			}
			stage('restore-configuration') {
				when {
					expression { isCloudEnabled(enviroment) && versionToRollbackTo }
				}
				steps {                    
                    restoreConfigurationStep()
				}
			}
            stage('undeploy-artifact-from-cloud') {
                when {
                    expression { isCloudEnabled(enviroment) && "false".equals(onlyConfig)}
                }
                steps {
                    undeployArtifactFromCloud()
                }
            }
			stage('send-undeploy-to-catalog') {
				when {
					expression { isCloudEnabled(enviroment) && "false".equals(onlyConfig) && "true".equals(deployFinished) }
				}
				steps {
                    sendUndeployToCatalogStep()
				}
			}
            stage('send-ancient-version-restored-to-AppPortal') {
                when {
                    expression { existAncient && "false".equals(onlyConfig) && "true".equals(deployFinished) }
                }
                steps {
                    sendAncientVersionRestoredToAppPortalStep()
                }
            }
            stage('apimanager-technicalservices-registration') {
                when {
                    expression { ifApiManagerTechnicalServiceRegistrationApplies(pipelineData, pomXmlStructure) && "false".equals(onlyConfig) && "true".equals(deployFinished) }
                }
                steps {
                    apimanagerTechnicalservicesRegistrationStep()
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
    printOpen("El onlyConfig @${onlyConfig}@", EchoLevel.ALL)

    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null

    // Analizamos el pom del master para obtener el nombre del artefecto, no deberia de cambiar y es el que se usa para la conexcion con AppPortal
    PomXmlStructure pomXmlStructureAux = getGitRepo(pathToRepo, "master", repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false)

    pipelineData = new PipelineData(PipelineStructureType.ROLLBACK, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.initFromRollback(enviroment, pathToRepo, originBranch, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), repoName)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam);
    pomXmlStructure = new PomXmlStructure(ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), artifact, version, pomXmlStructureAux.artifactName)
    pipelineData.pushUser = user
    pipelineData.distributionModePRO = currentDistributionMode

    calculateBuildCode(pomXmlStructure, pipelineData)

    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_STARTED, KpiLifeCycleStatus.OK)
    sendPipelineStartToAppPortal(pomXmlStructure, pipelineData, pipelineOrigenId)
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "100");
    currentBuild.displayName = "Rollback_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    initAppPortal = true
    pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName)
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "100")
}

/** 
 * Step getGitRcoherenceValidationStepepoStep
 */
def coherenceValidationStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "105")

    pipelineData.deployFlag == true
    //initCloudDeploy(pomXmlStructure, pipelineData)	
    def cannaryType = getCannaryType(pomXmlStructure,pipelineData)
    def cannaryCampaignValue = iopCampaignCatalogUtils.getCannaryCampaignValue(pomXmlStructure,pipelineData)

    printOpen("El aplicativo es de $cannaryType y el cannary esta a $cannaryCampaignValue ...", EchoLevel.ALL)

    if (cannaryType==GlobalVars.CANARY_TYPE_CAMPAIGN) {
            // Eliminamos el micro de la campaña
        printOpen("Se procede a eliminar el micro de la campaña en el catálogo", EchoLevel.ALL)
        iopCampaignCatalogUtils.deleteMicroFromCampaign(pomXmlStructure,pipelineData)

        // Si el cannary global es mayor que cero tenemos que validar las depencias
        if (cannaryCampaignValue > 0) {
            try {
                // Validamos la campaña
                printOpen("Iniciamos la validación de la campaña en el catálogo.", EchoLevel.ALL)
                iopCampaignCatalogUtils.validateCampaign()
            }catch(Exception exception) {
                printOpen("Se añade de nuevo el micro en la campaña en el catálogo.", EchoLevel.ALL)
                iopCampaignCatalogUtils.addMicroToCampaign(pomXmlStructure,pipelineData)                                    
                sendStageEndToAppPortal(pomXmlStructure, pipelineData, "105", Strings.toHtml(exception.getMessage()), null, "error")
                throw exception
            }
        }
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "105")
    } else {
        //Tenemos que validar el micro
        def typeVersion=""
        if (pomXmlStructure.artifactVersionQualifier == null || pomXmlStructure.artifactVersionQualifier == "") {
            typeVersion = "RELEASE"
        } else if (pomXmlStructure.isSNAPSHOT()) {
            typeVersion = "SNAPSHOT"
        } else if (pomXmlStructure.isRCVersion()) {
            typeVersion = "RC"
        } else {
            typeVersion = "UNKNOWN"
        }
        try {
            iopDevopsCatalogUtils.validateCoherence(pipelineData.getGarArtifactType().getGarName(),pomXmlStructure.getApp(pipelineData.garArtifactType),pomXmlStructure.artifactMajorVersion,pomXmlStructure.artifactMinorVersion,pomXmlStructure.artifactFixVersion,typeVersion, enviroment)
            sendStageEndToAppPortal(pomXmlStructure, pipelineData, "105")
        } catch (Exception e) {
            sendStageEndToAppPortal(pomXmlStructure, pipelineData, "105", e.getMessage(), null, "warning")
        }
    }
    
}	

/** 
 * Step checkCloudAvailiabilityStep
 */
def checkCloudAvailiabilityStep() {
    printOpen("The artifact ${pomXmlStructure.artifactName}  from group ${pomXmlStructure.groupId} the micro to deploy is ${pomXmlStructure.artifactMicro}", EchoLevel.ALL)
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "110")
    try {
        checkCloudAvailability(pomXmlStructure,pipelineData,enviroment.toUpperCase(),"DEPLOY")
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "110")
    } catch (Exception e) {
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "110", Strings.toHtml(e.getMessage()), null, "error")
        throw e
    }
}

/** 
 * Step restoreConfigurationStep
 */
def restoreConfigurationStep() {           
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "150")
    getGitRepo(pathToRepo, '', repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), versionToRollbackTo, true, false)
    sh "git config http.sslVerify false"
    pushConfigFiles(pomXmlStructure, pipelineData, false, true)
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "150")
}

/** 
 * Step undeployArtifactFromCloud
 */
def undeployArtifactFromCloud() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "210")
    printOpen("Haciendo undeploy y comprobando si se ha restaurado el ancient del componente", EchoLevel.ALL)
    existAncient = undeployCloud(pomXmlStructure, pipelineData, enviroment, ignoreExistingAncient, forceAllCenters)
    printOpen("Exist ancient? ${existAncient}", EchoLevel.ALL)
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "210")
}

/** 
 * Step getGitsendUndeployToCatalogStepRepoStep
 */
def sendUndeployToCatalogStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "220")
    printOpen("Hacemos undeploy en el catalog", EchoLevel.ALL)
    undeployArtifactInCatalog(pipelineData,pomXmlStructure,enviroment,true)
    printOpen("Fin Hacemos undeploy en el catalog", EchoLevel.ALL)
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "220")
}

/** 
 * Step sendAncientVersionRestoredToAppPortalStep
 */
def sendAncientVersionRestoredToAppPortalStep() {
    //Finalizamos el Pipeline de AppPortal de Rollback
    sendPipelineEndedToAppPortal(initAppPortal, pomXmlStructure, pipelineData, true)
    sendPipelineResultadoToAppPortal(initAppPortal, pomXmlStructure, pipelineData, true)

    //Obtenemos la version del ancient que acabamos de arrancar
    printOpen("Obtenemos la versión del ancient instalada", EchoLevel.ALL)
    if (isCloudEnabled(enviroment)) {
        version = getInfoAppCloud(pomXmlStructure, pipelineData, GlobalVars.ENDPOINT_INFO, enviroment).build.version
    }
    printOpen("Version obtenida del ancient: ${version}", EchoLevel.ALL)
    //Iniciamos el Pipeline de instalación del ancient de AppPortal (es unicamente informativo ya que unicamente crea un nuevo pipeline y lo finaliza para poder enviar la Trazabilidad = I)
    pipelineData = new PipelineData(PipelineStructureType.ROLLBACK_FINISH, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.initFromNonGit(enviroment, pathToRepo, originBranch, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), repoName)
    pomXmlStructure = new PomXmlStructure(ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), artifact, version, repoName)
    pipelineData.pushUser = user

    //Cambiamos el Id del pipeline para que no se machaque en AppPortal
    pipelineData.pipelineStructure.pipelineId = pipelineData.pipelineStructure.pipelineId + "-2"
    sendPipelineStartToAppPortal(pomXmlStructure, pipelineData, pipelineData.pipelineStructure.pipelineId)
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "100");
    currentBuild.displayName = "Rollback_Finish_${version} of ${pomXmlStructure.artifactName}"
    initAppPortal = true
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "100")
}

/** 
 * Step apimanagerTechnicalservicesRegistrationStep
 */
def apimanagerTechnicalservicesRegistrationStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "200")
    printOpen("Publishing swagger contract to API Manager (adpbdd-micro)", EchoLevel.ALL)
    publishSwaggerContract2ApiManager(pipelineData, pomXmlStructure)
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "200")
}

/** 
 * Step endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure, pipelineData)
    cleanWorkspace() 
}

/** 
 * Step endPipelineSuccessStep
 */
def endPipelineSuccessStep() {
    printOpen("Se success el pipeline ${successPipeline}", EchoLevel.INFO)

    successPipeline = true
    sendPipelineResultadoToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)

    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.OK)
}

/** 
 * Step endPipelineFailureStep
 */
def endPipelineFailureStep() {
    printOpen("Se failure el pipeline ${successPipeline}", EchoLevel.ERROR)

    successPipeline = false
    sendPipelineResultadoToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)

    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.KO)
}
