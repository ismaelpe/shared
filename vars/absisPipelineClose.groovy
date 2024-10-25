import groovy.transform.Field
import com.caixabank.absis3.*
import com.caixabank.absis3.KpiAlmEvent
import com.caixabank.absis3.KpiAlmEventStage
import com.caixabank.absis3.KpiAlmEventOperation

@Field Map pipelineParams

//Mantener estos parametros/variables por si se deben generar estructuras de datos para enviar a GPL
@Field String gitURL
@Field String gitCredentials
@Field String jenkinsPath
@Field boolean initGpl

@Field String originBranch
@Field String pathToRepo
@Field String repoName
@Field String artifactSubType
@Field String artifactType
@Field String user

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean successPipeline 

@Field String enviroment
@Field String artifact
@Field String version
@Field String oldVersion

@Field boolean existAncient
@Field boolean isMicro
@Field String commitId

@Field String artifactId
@Field BmxStructure bmxStructure
@Field DeployStructure deployStructure
@Field def component

@Field String executionProfileParam
@Field String targetAlmFolderParam

@Field String loggerLevel
@Field String agentParam

@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

//Pipeline para realizar el cierre de una release de un servicio desplegado en BMX
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters

    //Mantener estos parametros/variables por si se deben generar estructuras de datos para enviar a GPL
    gitURL = "https://git.svb.lacaixa.es/"
    gitCredentials = "GITLAB_CREDENTIALS"
    jenkinsPath = "absis3/services"
    initGpl = false

    originBranch = params.originBranchParam
    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam
    artifactSubType = params.artifactSubTypeParam
    artifactType = params.artifactTypeParam
    user = params.userId

    successPipeline = false

    enviroment = params.environmentParam
    artifact = params.artifactParam
    version = params.versionParam
	oldVersion = params.oldVersionParam

    existAncient = params.existAncientParam.toString().toBoolean()
    isMicro = params.isMicroParam.toString().toBoolean()
    commitId = params.commitIdParam

    artifactId = artifact + "-" + version
    component = params.componentParam

    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam

    loggerLevel = params.loggerLevel
    agentParam = params.agent

	almEvent = null
	initCallStartMillis = new Date().getTime()
	
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
        }
        stages {
            stage('init-pipeline') {
                steps {
                    initPipelineStep()
                }
            }			
			stage('check-ICP-availiability'){
				when {
					expression { isMicro }
				}
				steps {
                    checkICPAvailabilityStep()
				}
			}
			stage('close-release-bycenter') {
				when {//Significa que es un micro y que es un upgrade de una minor existente
					expression { !isMicro }
				}
				steps {
                    closeReleaseBycenterStep()
				}
			}
            stage('close-release') {
                when {//Significa que es un micro y que es un upgrade de una minor existente
                    expression { isMicro }
                }
                steps {
                    closeReleaseStep()
                }
            }
			stage('clone-to-ocp-pro') {
				when{
					//Validamos que el micro no este en DEV
					expression { isMicro }
				}
				steps {
					cloneToOcpProStep()
				}
			}
            stage('copy-config-files') {
                when {
                    expression { isMicro && existAncient }
                }
                steps {
                    copyConfigFilesStep()
                }
            }
            stage('refresh-micro') {
                when {//Significa que es un micro, que es un upgrade de una minor existente y que el deploy a PRO se ha hecho con cannary (los deploys por centro no necesitan hacer refresh)
                    expression { isMicro && existAncient && !env.DEPLOY_MODE_SPECIAL.contains(pomXmlStructure.artifactName) }
                }
                steps {
                    refreshMicroStep()
                }
            }
            stage('apimanager-technicalservices-registration') {
                when {
                    expression { ifApiManagerTechnicalServiceRegistrationApplies(pipelineData, pomXmlStructure) }
                }
                steps {
                    apiManagerTechnicalServiceRegistrationStep()
                }
            }
            stage('create-MR') {
                steps {
                    createMRStep()
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
 * Stage initPipelineStep
 */
def initPipelineStep() {
    printOpen("Booleans ${existAncientParam} ${isMicroParam} ${existAncient} ${isMicro}", EchoLevel.DEBUG)
    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null
    //Borrar estos comentarios en caso que no se tengan que generar las estructuras de datos.
    printOpen("Extract GIT Repo ${pathToRepo} ${originBranch}", EchoLevel.INFO)

    //DisplayName
    pipelineData = new PipelineData(PipelineStructureType.CLOSE, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.initFromClose(enviroment, pathToRepo, originBranch, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), repoName)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam);
    pomXmlStructure = new PomXmlStructure(ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), artifact, version, component)
    pipelineData.commitId = commitId

    currentBuild.displayName = "Closing_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    pipelineData.pushUser = user

    pipelineData.buildCode = pomXmlStructure.artifactVersion
    printOpen("Branch Type: ${pipelineData.branchStructure.branchType}", EchoLevel.INFO)

    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100");
    almEvent = new KpiAlmEvent(
        pomXmlStructure, pipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_CLOSE_RELEASE)						
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_STARTED, KpiLifeCycleStatus.OK)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.CLOSE_STARTED, KpiLifeCycleStatus.OK)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_STARTED, KpiLifeCycleStatus.OK, "PRO")

    pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName, pomXmlStructure.artifactType, pomXmlStructure.artifactSubType, originBranch)
    pipelineData.pipelineStructure.resultPipelineData.oldVersionInCurrentEnvironment = oldVersion

    initGpl = true
    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")
}

/**
 * Stage checkICPAvailabilityStep
 */
def checkICPAvailabilityStep() {
    printOpen("The artifact ${pomXmlStructure.artifactName} from group ${pomXmlStructure.groupId} is deploying the micro ${pomXmlStructure.artifactMicro}", EchoLevel.DEBUG)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "110")
    try {
        checkICPAvailability(pomXmlStructure,pipelineData,"PRO","DEPLOY")
        sendStageEndToGPL(pomXmlStructure, pipelineData, "110")
    } catch (Exception e) {
        sendStageEndToGPL(pomXmlStructure, pipelineData, "110", Strings.toHtml(e.getMessage()), null, "error")
        throw e
    }
}

/**
 * Stage closeReleaseBycenterStep
 */
def closeReleaseBycenterStep() {
    //Es un micro desplegado por IOP por centro
    if (env.DEPLOY_MODE_SPECIAL.contains(pomXmlStructure.artifactName) ) {
        if (originBranch!=null && !originBranch.contains('configfix')) {
            sendStageStartToGPL(pomXmlStructure, pipelineData, "200")
            deployArtifactInCatMsv(null, pipelineData, pomXmlStructure, null , "PRO")
            sendStageEndToGPL(pomXmlStructure, pipelineData, "200")
        }
    }
}

/**
 * Stage closeReleaseStep
 */
def closeReleaseStep() {
    printOpen("---------------------------", EchoLevel.INFO)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "200")
    pipelineData.deployFlag == true
    initICPDeploy(pomXmlStructure, pipelineData)	
    def cannaryType=getCannaryType(pomXmlStructure,pipelineData)
    
    if (cannaryType==GlobalVars.CANARY_TYPE_CAMPAIGN) {
        printOpen("Es una IOP por campaña no puede cerrar hasta que tenga el cannary a 100 como minimo", EchoLevel.INFO)
        if (iopCampaignCatalogUtils.getCannaryCampaignValue(pomXmlStructure,pipelineData)>=100) {
            printOpen("Se puede cerrar el micro ya que la Campaña ha terminado", EchoLevel.INFO)
        }else {
            sendStageEndToGPL(pomXmlStructure, pipelineData, "200", "No se puede cerrar el micro hasta que la campaña finalice, le avisaran", null, "error")
            throw new Exception ("No se puede cerrar el micro hasta que la campaña finalice, le avisaran")
        }
    }					
    
    if (pipelineData.deployOnIcp) {
        closeBlueGreenICP(pomXmlStructure, pipelineData, existAncient)
        //Si es una rama del tipo configfix no se tiene que hacer nada.
        if (originBranch!=null && !originBranch.contains('configfix')) {
            deployArtifactInCatMsv(null, pipelineData, pomXmlStructure, null , "PRO")
        }							
        if (cannaryType==GlobalVars.CANARY_TYPE_CAMPAIGN) {
            printOpen("Procedemos a cerrar la version", EchoLevel.INFO)
            iopCampaignCatalogUtils.closeVersion(pomXmlStructure,pipelineData)
        }
    }
    sendStageEndToGPL(pomXmlStructure, pipelineData, "200")
    
}

/**
 * Stage cloneToOcpProStep
 */
def cloneToOcpProStep() {
    absisPipelineStageCloneToOcp(pomXmlStructure, pipelineData,user,'false')
}

/**
 * Stage copyConfigFilesStep
 */
def copyConfigFilesStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "300");
    
    printOpen("Reset canary release flag", EchoLevel.INFO)
    
    pushConfigFiles(pomXmlStructure, pipelineData, true, true, false)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "300")
}

/**
 * Stage refreshMicroStep
 */
def refreshMicroStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "400")

    pipelineData.deployFlag == true
    initICPDeploy(pomXmlStructure, pipelineData)

    if (pipelineData.deployOnIcp) refreshConfigurationViaRefreshBus(pomXmlStructure, pipelineData)
        
    sendStageEndToGPL(pomXmlStructure, pipelineData, "400")
}

/**
 * Stage apiManagerTechnicalServiceRegistrationStep
 */
def apiManagerTechnicalServiceRegistrationStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "550")
    
    printOpen("Publishing swagger contract to API Manager (adpbdd-micro)", EchoLevel.INFO)
    publishSwaggerContract2ApiManager(pipelineData, pomXmlStructure)

    sendStageEndToGPL(pomXmlStructure, pipelineData, "550")
}

/**
 * Stage createMRStep
 */
def createMRStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "600")
    if (!(pipelineData.getExecutionMode().invokeNextActionAuto() && ObtainNextJobOptionsUtils.hasNextJob(pipelineData.getExecutionMode().actionFlag(), pipelineData.pipelineStructure.resultPipelineData.getAcciones(true)))) {
        mergeRequestToMaster.mergeOnClose(pipelineData, pomXmlStructure, 'master')
    }
    sendStageEndToGPL(pomXmlStructure, pipelineData, "600")
}

/**
 * Stage endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}

/**
 * Stage endPipelineSuccessStep
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("SUCCESS", EchoLevel.INFO)
    if ( almEvent!=null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis-initCallStartMillis))						
    }
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_FINISHED, KpiLifeCycleStatus.OK, "PRO")
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.CLOSE_FINISHED, KpiLifeCycleStatus.OK)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.OK)
        
        
    if (pipelineData.getExecutionMode().invokeNextActionAuto() && !pipelineData.isPushCI()) {
        printOpen("Modo test activado en fase de cierre", EchoLevel.INFO)
        invokeNextJob(pipelineData, pomXmlStructure)
    }
}

/**
 * Stage endPipelineFailureStep
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("FAILURE", EchoLevel.ERROR)
    if ( almEvent!=null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis-initCallStartMillis))
    }
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_FINISHED, KpiLifeCycleStatus.KO, "PRO")
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.CLOSE_FINISHED, KpiLifeCycleStatus.KO)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.KO)
}
