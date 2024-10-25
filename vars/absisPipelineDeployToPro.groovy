import groovy.transform.Field
import com.caixabank.absis3.*
import com.caixabank.absis3.KpiAlmEvent
import com.caixabank.absis3.KpiAlmEventStage
import com.caixabank.absis3.KpiAlmEventOperation

@Field Map pipelineParams

@Field String gitURL
@Field String gitCredentials
@Field String jenkinsPath

@Field String version
@Field String oldVersionInCurrentEnvironment
@Field String pathToRepo
@Field String repoName
@Field String artifactSubType
@Field String artifactType
@Field String pipelineOrigId
@Field String originBranch
@Field String commitId
@Field String user
@Field String executionProfileParam
@Field String targetAlmFolderParam
@Field String currentDistributionMode
@Field boolean fastPath
@Field String loggerLevel
@Field String agentParam

@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field ICPStateUtility icpStateUtilitity
@Field AppDeploymentState center1State
@Field AppDeploymentState center2State
@Field boolean initGpl
@Field boolean successPipeline
@Field boolean hasAncientICP

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    gitURL = "https://git.svb.lacaixa.es/"
    gitCredentials = "GITLAB_CREDENTIALS"
    jenkinsPath = "absis3/services"

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
    version = params.versionParam
	oldVersionInCurrentEnvironment = params.oldVersionParam
    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam
    artifactSubType = params.artifactSubTypeParam
    artifactType = params.artifactTypeParam
    pipelineOrigId = params.pipelineOrigId
    originBranch = params.originBranchParam
    commitId = params.commitIdParam
    user = params.userId
    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam
    currentDistributionMode = params.nextDistributionMode
	fastPath = params.fastPathParam.toString().toBoolean()
    loggerLevel = params.loggerLevel
    agentParam = params.agent
	
	almEvent = null
	initCallStartMillis = new Date().getTime()

    icpStateUtilitity = null

    initGpl = false
    successPipeline = false
	hasAncientICP = false

    /**
     * 1. Recoger el artifact
     * 2. Copy config
     * 3. Desplegar a PRO
     * 3.5. Preparar Canario
     */
    pipeline {      
		agent {	node (absisJenkinsAgent(agentParam)) }
        options {        
            buildDiscarder(logRotator(numToKeepStr: '30'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
			JNKMSV = credentials('JNKMSV-USER-TOKEN')
            ICP_CERT = credentials('icp-absis3-pro-cert')
            ICP_PASS = credentials('icp-absis3-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
            executionProfile = "${executionProfileParam ? executionProfileParam : 'DEFAULT'}"
            logsReport = true
            sendLogsToGpl = true
        }
        //Atencion que en el caso que estemos en un MergeRequest... quizas solo debamos validar la issue
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
			stage('check-ICP-availiability'){
				when {
					expression { pipelineData.deployFlag && pomXmlStructure.isMicro() }
				}
				steps {
                    checkICPAvailiabilityStep()
				}
			}
            stage('error-translations') {
                when {
                    expression { !pipelineData.isPushCI() }
                }
                steps {
                    errorTranslationsStep()
                }
            }			
			stage('deploy-bbdd-scripts') {
				when {
					expression { !pipelineData.isPushCI() && hasBBDD(pomXmlStructure,pipelineData,true) }
				}
				steps {
                    deployBBDDScriptsStep()
				}
			}
            stage('copy-config-files') {
                when {
                    expression { pipelineData.deployFlag && pomXmlStructure.isMicro() }
                }
                steps {
                    copyConfigFilesStep()
                }
            }
            stage('deploy-to-cloud-icp') {
                when {
                    expression { pipelineData.deployFlag == true && pipelineData.deployOnIcp }
                }
                steps {
                    deployToCloudICPStep()
                }
            }
            stage('post-deploy') {
                when {
                    expression { pipelineData.deployFlag }
                }
                steps {
                    postDeployStep()
                }
            }
			stage('run-remote-it') {
				when {
					expression { pipelineData.deployFlag }
				}
				steps {
                    runRemoteITStep()
				}
			}
			stage('redirect-all-services-to-new-micro') {
				when {
					expression { pipelineData.deployFlag }
				}
				steps {
                    redirectAllAervicesToNewMicroStep()
				}
			}
			stage('clone-to-ocp-pro') {
				when{					
					expression { pipelineData.deployFlag == true  }
				}
				steps {
					cloneToOcpProStep()
				}
			}
			stage('verify-endpoints') {
				when {
					expression { pipelineData.deployFlag == true }
				}
				steps {
                    verifyEndpointsStep()
                }
            }
            stage('promote-mule-contract') {
                when {
                    expression { pomXmlStructure.contractVersion }
                }
                steps {
                    promoteMuleContractStep()
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
 * Step getGitRepoStep
 */
def getGitRepoStep() {
    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null
    
    pomXmlStructure = getGitRepo(pathToRepo, '', repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), version, true) 
    calculateArchVersionWithModules(pomXmlStructure)

    pipelineData = new PipelineData(PipelineStructureType.IOP_PRO, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.commitId = commitId
    pipelineData.initFromIOPPro(pathToRepo, originBranch, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), repoName)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam);
    pipelineData.distributionModePRO = currentDistributionMode
    pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName, pomXmlStructure.artifactType, pomXmlStructure.artifactSubType)

    calculatePreviousInstalledVersionInEnvironment(pipelineData,pomXmlStructure, oldVersionInCurrentEnvironment)
    printOpen("DistributionModePRO has been defined as: ${pipelineData.distributionModePRO}", EchoLevel.INFO)

    pipelineData.pushUser = user
    pipelineData.buildCode = pomXmlStructure.artifactVersion

    // Comprobamos si hemos cargado del metodo anterior las propiedades del Jenkins
    pipelineData.mvnMuleParameters = loadMuleMvnParameters(pipelineData, pomXmlStructure)
    
    almEvent = new KpiAlmEvent(
        pomXmlStructure, pipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_DEPLOY_PRO)
    
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_STARTED, KpiLifeCycleStatus.OK)
    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100")
    currentBuild.displayName = "Deploying_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    initGpl = true
    debugInfo(null, pomXmlStructure, pipelineData)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")

    //INIT AND DEPLOY
    if (pipelineData.deployFlag) initICPDeploy(pomXmlStructure, pipelineData)
}

/**
 * Step checkICPAvailiabilityStep
 */
def checkICPAvailiabilityStep(){
    printOpen("The artifact ${pomXmlStructure.artifactName}  from group ${pomXmlStructure.groupId} the micro to deploy is ${pomXmlStructure.artifactMicro}", EchoLevel.INFO)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "110")
    try {
        checkICPAvailability(pomXmlStructure,pipelineData,"PRO","DEPLOY")
        sendStageEndToGPL(pomXmlStructure, pipelineData, "110")
    } catch (Exception e) {
        printOpen("Error al comprobar la disponibilidad de ICP : ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "110", null, null, "error")
        throw e				
    }
}

/**
 * Step errorTranslationsStep
 */
def errorTranslationsStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "250")
    try {
        litmidTranslations(pomXmlStructure, pipelineData)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "250")
    } catch (Exception e) {
        printOpen("Error al realizar la traduccion de errores : ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "250", null, null, "error")   
        throw e
    }
}
 
/**
 * Step deployBBDDScriptsStep
 */
def deployBBDDScriptsStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "251")
    printOpen("Deploying sql to the BBDD", EchoLevel.INFO)
    try {
        def missatge=generateSqlScriptRelease(pomXmlStructure,pipelineData,true)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "251",missatge,null,"ended")
    }catch(Exception e) {
        printOpen("Error al generar BBDD Scripts: ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "251", null, null, "error")
        throw e
    }
}				

/**
 * Step copyConfigFilesStep
 */
def copyConfigFilesStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "300")
    
    //build the workspace
    printOpen("Building the branch", EchoLevel.INFO)
    try {
        // INICIO - IPE
        // Comentario: Si se definen las variables de entorno para Git esto no hace falta
        sh "git config http.sslVerify false"
        // FIN - IPE
        pushConfigFiles(pomXmlStructure, pipelineData, false, true)

        sendStageEndToGPL(pomXmlStructure, pipelineData, "300")

        pipelineData.pipelineStructure.resultPipelineData.onlyConfig = true
    } catch (Exception e) {
        printOpen("Error al copiar config files: ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "300", null, null, "error")
        throw e
    }
}

/**
 * Step deployToCloudICPStep
 */
def deployToCloudICPStep() {
    String artifactApp = pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name))
    sendStageStartToGPL(pomXmlStructure, pipelineData, "400")
    try {
        kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_STARTED, KpiLifeCycleStatus.OK, "BETA")
        String icpArch = ""
        if (pomXmlStructure.isArchProject()) icpArch = GlobalVars.ICP_APP_ARCH.toLowerCase()
        else icpArch = GlobalVars.ICP_APP_APPS.toLowerCase()

        String aplicacion = MavenUtils.sanitizeArtifactName(pomXmlStructure.artifactName, pipelineData.garArtifactType)
        def nameComponentInICP = aplicacion + pomXmlStructure.getArtifactMajorVersion()

        //String newImage="pro-registry.pro.caas.caixabank.com/containers/ab3cor/monitoring1:${version}"
        String newImage = "${env.ICP_REGISTRY_URL}/${icpArch}/${nameComponentInICP.toLowerCase()}:${version}"

        icpStateUtilitity = deployICP(pomXmlStructure, pipelineData, "NoTenemosID", newImage)
            
        sendEmail(" Resultado ejecucion app ${artifactApp} - ${pipelineData.getPipelineBuildName()}  OK ", env.ABSIS3_SERVICES_EMAIL_ICP_DEPLOY_RESULT, "${artifactApp} rama ${pipelineData.getPipelineBuildName()}", "OK en el paso DEPLOY")
        kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_FINISHED, KpiLifeCycleStatus.OK, "BETA")
     
        sendStageEndToGPL(pomXmlStructure, pipelineData, "400", null, pipelineData.bmxStructure.environment)
    } catch (Exception e) {
        sendEmail(" Resultado ejecucion app ${artifactApp} - ${pipelineData.getPipelineBuildName()}  KO - DEPLOY", env.ABSIS3_SERVICES_EMAIL_ICP_DEPLOY_RESULT, "${artifactApp} rama ${pipelineData.getPipelineBuildName()}", "KO en el paso DEPLOY")
        kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_FINISHED, KpiLifeCycleStatus.KO, "BETA")
        printOpen("Error en el deploy a ICP:  ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "400", null, null, "error")
        throw e
    }
}

/**
 * Step postDeployStep
 */
def postDeployStep() {            
    sendStageStartToGPL(pomXmlStructure, pipelineData, "402")
    // Metadata for rollback action. This will be overwritten in the verify-endpoints stage
    pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName, pomXmlStructure.artifactType, pomXmlStructure.artifactSubType)
    //set flag to send trazability to IDECUA when deploy to cloud is ok
    pipelineData.pipelineStructure.resultPipelineData.hasDeployedToCloud = true
    sendStageEndToGPL(pomXmlStructure, pipelineData, "402")
    sendPipelineUpdateToGPL(initGpl, pomXmlStructure, pipelineData, '')
}

/**
 * Step runRemoteITStep
 */
def runRemoteITStep() {
    absisPipelineStageRunRemoteIT(pomXmlStructure, pipelineData, "500", "<phase>-runRemoteIT", icpStateUtilitity, "${GlobalVars.ABSIS3_SERVICES_EXECUTE_IT_PRO}")
}

/**
 * Step redirectAllAervicesToNewMicroStep
 */
def redirectAllAervicesToNewMicroStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "600")
    try {
        String icpArch = ""
        if (pomXmlStructure.isArchProject()) icpArch = GlobalVars.ICP_APP_ARCH.toLowerCase()
        else icpArch = GlobalVars.ICP_APP_APPS.toLowerCase()
        printOpen("icpArch: ${icpArch}", EchoLevel.INFO)

        String aplicacion = MavenUtils.sanitizeArtifactName(pomXmlStructure.artifactName, pipelineData.garArtifactType)
        def nameComponentInICP = aplicacion + pomXmlStructure.getArtifactMajorVersion()
        printOpen("nameComponentInICP: ${nameComponentInICP}", EchoLevel.INFO)

        //String newImage="pro-registry.pro.caas.caixabank.com/containers/ab3cor/monitoring1:${version}"
        String newImage = "${env.ICP_REGISTRY_URL}/${icpArch}/${nameComponentInICP.toLowerCase()}:${version}"
        printOpen("newImage: ${newImage}", EchoLevel.INFO)
        
        icpStateUtilitity = redirectICPServices(pomXmlStructure, pipelineData, "NoTenemosID", newImage, icpStateUtilitity)

        sendStageEndToGPL(pomXmlStructure, pipelineData, "600")
    } catch (Exception e) {
        sendEmail(" Resultado ejecucion app ${artifactApp} - ${pipelineData.getPipelineBuildName()}  KO - DEPLOY", env.ABSIS3_SERVICES_EMAIL_ICP_DEPLOY_RESULT, "${artifactApp} rama ${pipelineData.getPipelineBuildName()}", "KO en el paso REDIRECT SERVICES")
        printOpen("Error al intentar realizar el redireccionado al nuevo micro: ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "600", null, null, "error")
        throw e
    }
}

/**
 * Step cloneToOcpProStep
 */
def cloneToOcpProStep() {
    absisPipelineStageCloneToOcp(pomXmlStructure, pipelineData, user)
}

/**
 * Step verifyEndpointsStep
 */
def verifyEndpointsStep() {
    try {
        sendStageStartToGPL(pomXmlStructure, pipelineData, "700")
        
        //Tenemos que validar la foto...Pending
        echo checkEndpointsProICP(pomXmlStructure, pipelineData, pipelineData.distributionModePRO)

        //Deberemos detectar si tenemos ancient en cualquiera de los dos centros
        //Yo revisaria si estamos desplegando un centro o los dos
        AppDeploymentStateICP center1StateI = getMultipleDeploymentsStateFromICP(pomXmlStructure,pipelineData,"AZ1")
        AppDeploymentStateICP center2StateI = getMultipleDeploymentsStateFromICP(pomXmlStructure,pipelineData,"AZ2")

        printOpen("El resultado de las evaluaciones de los ancients es de ", EchoLevel.INFO)
        printOpen("ancientC1 ${center1StateI.getAncientApp()}", EchoLevel.INFO)
        printOpen("ancientC2 ${center2StateI.getAncientApp()}", EchoLevel.INFO)

        hasAncientICP=center1StateI.getAncientApp() && center2StateI.getAncientApp()

        AncientVersionInfo ancientC1=new AncientVersionInfo()
        AncientVersionInfo ancientC2=new AncientVersionInfo()

        ancientC1.hasAncient=center1StateI.getAncientApp()
        ancientC2.hasAncient=center2StateI.getAncientApp()

        //Vamos a settear los valores
        pipelineData.ancientMapInfo.put(GlobalVars.BMX_CD1,ancientC1)
        pipelineData.ancientMapInfo.put(GlobalVars.BMX_CD2,ancientC2)
        printOpen("El pipeline tiene el siguiente contenido ${pipelineData}", EchoLevel.INFO)

        pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName, pomXmlStructure.artifactType, pomXmlStructure.artifactSubType)
        
        if (pipelineData.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2) {
            //Hacemos setting de los valores para que el cierre no aplique el close
            pipelineData.pipelineStructure.resultPipelineData.microParam = false
        }
        
        //Deploy To catalog
        deployArtifactInCatMsv(null, pipelineData, pomXmlStructure, icpStateUtilitity, "BETA")
        if (pipelineData.deployStructure.cannaryType == GlobalVars.CANARY_TYPE_CAMPAIGN) {
            //Tenemos que añadir el micro a la IOP
            printOpen("Vamos a registrar el micro en la campaña", EchoLevel.INFO)
            iopCampaignCatalogUtils.addMicroToCampaign(pomXmlStructure,pipelineData)
        }else {
            printOpen("El micro no tiene deploy en cannary ${pipelineData.deployStructure.cannaryType} el valor del cannary ${GlobalVars.CATALOG_CAMPAIGN_CANNARY_TYPE}", EchoLevel.INFO)
        }
        
        sendStageEndToGPL(pomXmlStructure, pipelineData, "700", null, pipelineData.bmxStructure.environment)
    } catch (Exception e) {
        printOpen("Error en la verificación de endpoints: ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "700", null, null, "error")
        throw e
    }
}

/**
 * Stage 'promote-mule-contract'
 */
def promoteMuleContractStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "800")
 
    printOpen("Promote swagger contract to MuleSoft", EchoLevel.INFO)
    def result = promoteContract2MuleSoft(pipelineData, pomXmlStructure)

    sendStageEndToGPL(pomXmlStructure, pipelineData, "800", result, null, result ? "warning" : "ended")
}

/**
 * Step endPipelineSuccessStep
 */
def endPipelineSuccessStep() {
    printOpen("Se success el pipeline ${successPipeline}", EchoLevel.INFO)

    if (pipelineData.deployStructure != null) {
        pipelineData.pipelineStructure.resultPipelineData.cannaryType = pipelineData.deployStructure.cannaryType
    }
    printOpen("Next distribution mode is: ${pipelineData.pipelineStructure.resultPipelineData.nextDistributionMode}", EchoLevel.INFO)

    successPipeline = true
    
    if ( almEvent!=null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis-initCallStartMillis))						
    }					
    pipelineData.pipelineStructure.resultPipelineData.userId=user
 	printOpen("FastPath param is ${fastPath}", EchoLevel.INFO)
	if (fastPath) {
		pipelineData.pipelineStructure.resultPipelineData.isFastPath=true
		printOpen("Fast Path activated", EchoLevel.DEBUG)		
	}
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.OK)
    if (pipelineData.getExecutionMode().invokeNextActionAuto()) {
        printOpen("Modo test activado en fase de build", EchoLevel.INFO)
        invokeNextJob(pipelineData, pomXmlStructure)
        //Si estamos en rollout de centro 1 no tiene sentido invocar al cierre... aun queda centro 2
    } else if (!fastPath && !hasAncientICP && pipelineData.distributionModePRO != DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1) {
        printOpen("No ancient detected in any center. END RELEASE is being fired automatically...", EchoLevel.INFO)
        pipelineData.prepareExecutionMode('COMPONENT_NEW_VERSION', targetAlmFolderParam);
        invokeNextJob(pipelineData, pomXmlStructure)
    } else if (fastPath) {
        pipelineData.prepareExecutionMode('COMPONENT_NEW_VERSION', targetAlmFolderParam);
        invokeNextJob(pipelineData, pomXmlStructure)
    }
}

/**
 * Step endPipelineFailureStep
 */
def endPipelineFailureStep() {
    printOpen("Se failure el pipeline ${successPipeline}", EchoLevel.ERROR)

    successPipeline = false

    if ( almEvent!=null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis-initCallStartMillis))
    }
    if (pipelineData!=null && pipelineData.deployStructure != null) {
        pipelineData.pipelineStructure.resultPipelineData.isNowDeployed = pipelineData.isNowDeployed
    }
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)

    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.KO, GlobalVars.PRO_ENVIRONMENT)
}

/**
 * Step endPipelineSuccessStep
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}
