import groovy.transform.Field
import com.project.alm.*
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams

@Field NexusUtils nexus

@Field String gitURL
@Field String gitCredentials
@Field String jenkinsPath

@Field String originBranch
@Field String pathToRepo
@Field String repoName
@Field boolean deployToTst
@Field String artifactSubType
@Field String artifactType
@Field String pipelineOrigId
@Field boolean isArchetype
@Field String archetypeModel
@Field String commitId
@Field String executionProfileParam
@Field String targetAlmFolderParam
@Field String user
@Field String loggerLevel
@Field String agentParam

@Field String[] mvnAdditionalParameters

@Field String deployCloudPhases
@Field String resultDeployCloud
@Field CloudStateUtility cloudStateUtilitity

@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean initAppPortal
@Field boolean successPipeline

@Field BmxStructure bmxStructurePRE
@Field def resultDeployInfoPRE
@Field def ancientMapInfoPRE
        
//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine

def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    nexus = new NexusUtils(this)

    gitURL = "https://git.svb.digitalscale.es/"
    gitCredentials = "GITLAB_CREDENTIALS"
    jenkinsPath = "alm/services"

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
    originBranch = params.originBranchParam
    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam
    deployToTst = params.deployToTstParam.toString().toBoolean()
    artifactSubType = params.artifactSubTypeParam
    artifactType = params.artifactTypeParam
    pipelineOrigId = params.pipelineOrigId
    isArchetype = params.isArchetypeParam.toString().toBoolean()
    archetypeModel = params.archetypeModelParam
    commitId = params.commitIdParam
    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam
    user = params.userId
    loggerLevel = params.loggerLevel
    agentParam = params.agent
    mvnAdditionalParameters = params.mvnAdditionalParametersParam?.split(",")

    deployCloudPhases = "01-pre-deploy"
    resultDeployCloud = "OK"
    cloudStateUtilitity = null
	
	almEvent = null
	initCallStartMillis = new Date().getTime()

    initAppPortal = false
    successPipeline = false

    /**
     * 1. Modificar la release del artefacto de RC a Release definitiva sin tag
     * 2. Compilar
     * 2. Desplegar a PRE
     * 2.5. Etiquetar cliente ¿? Publish , etc
     * 3. Push GIT + etiqueta
     */
    pipeline {      
		agent {	node (almJenkinsAgent(agentParam)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '50'))
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
            sendLogsToAppPortal = true

        }        
        stages {
            stage('get-git-repo') {
                steps {
                   getGitRepoStep()
                }
            }
			stage('check-Cloud-availiability'){
				when {
					expression { pipelineData.deployFlag && pipelineData.deployOnCloud }
				}
				steps {
					checkCloudAvailiabilityStep()
				}
			}
            stage('prepare-Release') {
                steps {
                    prepareReleaseStep()
                }
            }
            stage('validate-dependencies-version') {
                when {
                    expression { !pipelineData.isPushCI() }
                }
                steps {
                    validateDependenciesVersionStep()
                }
            }
            stage('validate-version') {
                when {
                    expression { pomXmlStructure.artifactSubType == ArtifactSubType.MICRO_APP }
                }
                steps {
                    validateVersionStep()
                }
            }		
			
			stage('deploy-bbdd-scripts') {
				when {
					expression { !pipelineData.isPushCI() && hasBBDD(pomXmlStructure,pipelineData,true) }
				}
				steps {
					deployBBBDDScriptsStep()					
				}
			}
            stage('build') {
                steps {
					buildStep()
                }
            }
			stage('checkmarx-scan') {
				when {
					expression { !pipelineData.isPushCI() }
				}
				steps {
					checkmarxScanStep()
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
            stage('copy-config-files') {
                when {
                    expression { pipelineData.deployFlag && pomXmlStructure.isMicro() && pipelineData.bmxStructure.usesConfigServer() }
                }
                steps {
                   copyConfigFiles()
                }
            }
            stage('deploy-micro-artifactory-cloud') {
                when {
                    expression { !pipelineData.isPushCI() && pipelineData.deployOnCloud }
                }
                steps {
                    deployMicroArtifactoryCloudStep()
                }
            }
            stage("deploy-to-cloud-cloud") {
                when {
                    expression { pipelineData.deployFlag && pipelineData.deployOnCloud }
                }
                steps {
                    deployToCloudCloudStep()
                }
            }
            stage("run-remote-it") {
                when {
                    expression { pipelineData.deployFlag && pipelineData.deployOnCloud }
                }
                steps {
                    runRemoteITStep()
                }
            }
			stage("run-remote-it-on-old-version") {
				when {
					expression { pipelineData.deployFlag && pipelineData.deployOnCloud && pipelineData.pipelineStructure.resultPipelineData!=null &&
						!pomXmlStructure.isLibrary() && 
						pipelineData.pipelineStructure.resultPipelineData.oldVersionInCurrentEnvironment!=null && 
						pipelineData.pipelineStructure.resultPipelineData.oldVersionInCurrentEnvironment!=""}
				}
				steps {
					runRemoteItOnOldVersionStep()
				}
			}
            stage('consolidate-cloud-cloud') {
                when {
                    expression { pipelineData.deployFlag && pipelineData.deployOnCloud }
                }
                steps {
                    consolidateCloudCloudStep()
                }
            }
			stage('clone-to-ocp-pre') {
				when{					
					expression { pipelineData.deployFlag && pipelineData.deployOnCloud }
				}
				steps {
					cloneToOcpPreStep()
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
            stage('apimanager-technicalservices-registration') {
                when {
                    expression { ifApiManagerTechnicalServiceRegistrationApplies(pipelineData, pomXmlStructure) }
                }
                steps {
                  apiManagerTechnicalServiceRegistrationStep()
                }
            }
            stage('copy-config-files-to-tst-cloud') {
                when {
                    expression { deployToTst && pipelineData.deployFlag && pipelineData.deployOnCloud && resultDeployCloud == "OK" }
                }
                steps {
                    copyConfigFilesToTstCloudStep()                    
                }
            }

            stage('deploy-to-tst-cloud') {
                when {
                    expression { deployToTst && pipelineData.deployFlag && pipelineData.deployOnCloud && resultDeployCloud == "OK" }
                }
                steps {
                    deployToCloudTstCloudStep()                    
                }
            }
            stage('apimanager-technicalservices-registration-tst') {
                when {
                    expression { ifApiManagerTechnicalServiceRegistrationApplies(pipelineData, pomXmlStructure) && deployToTst && resultDeployCloud == "OK" }
                }
                steps {
                    apimanagerTechnicalServicesRegistrationTstStep()                   
                }
            }
            stage('Generate archetype from project') {
                when {
                    expression { isArchetype && archetypeModel?.trim() }
                }
                steps {
                    generateArchetypeFromProjectStep()
                }
            }
            stage('Deploy archetype into Nexus') {
                when {
                    expression { isArchetype && archetypeModel?.trim() }
                }
                steps {
                    deployArchetypeIntoNexus()
                }
            }
            stage('publish-client') {
                when {
                    expression { pomXmlStructure.artifactSubType == ArtifactSubType.MICRO_APP }
                }
                steps {
                    publishClientStep()
                }
            }
            stage('push-Release-to-git') {
                steps {
                   pushReleaseToGitStep()
                }
            }
            stage('deploy-nexus') {
                when {
                    expression { !pipelineData.isPushCI() }
                }
                steps {
                    deployArtifactoryStep()
                }
            }
            stage('publish-artifact-catalog') {
                when {
                    expression { GlobalVars.BackEndAppPortal_ENABLED  }
                }
                steps {			
                    publishArtifactCatalogStep()					
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

    printOpen("Deploy to TST ${deployToTst} The value", EchoLevel.DEBUG)
    printOpen("Git Repository is ${pathToRepo} and branch is ${originBranch}", EchoLevel.DEBUG)
    pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false)
    pipelineData = new PipelineData(PipelineStructureType.RELEASE, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.commitId = commitId
    pipelineData.initFromRelease(pathToRepo, originBranch, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), repoName, isArchetype)
    pipelineData.pipelineStructure.resultPipelineData.version = pomXmlStructure.artifactVersion
    pipelineData.mvnAdditionalParameters = mvnAdditionalParameters
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam);
    pipelineData.setDefaultAgent(agentParam)
    pipelineData.pushUser = user

    pipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()

    // Comprobamos si hemos cargado del metodo anterior las propiedades del Jenkins
    pipelineData.mvnMuleParameters = loadMuleMvnParameters(pipelineData, pomXmlStructure)

    //FIXME Improve this by reading the file and creating a Map that is to be stored in pipelineData.jenkinsFileParams
    String jenkinsFile = readFile 'Jenkinsfile'
    if (jenkinsFile?.replace(" ", "").contains("cleanInstallBeforeDeploy:true")) {

        pipelineData.jenkinsFileParams = [maven: [cleanInstallBeforeDeploy: true]]

    }

    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_STARTED, KpiLifeCycleStatus.OK)
    almEvent = new KpiAlmEvent(
        pomXmlStructure, pipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_RELEASE)

    sendPipelineStartToAppPortal(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "100")
    initAppPortal = true

    //Buscamos version de arquitectura
    //Validamos que sea superior a la minima
    calculateArchVersionWithModules(pomXmlStructure)
    try {
        printOpen("The environment is ${pipelineData.bmxStructure.environment}", EchoLevel.DEBUG)
        if (pipelineData.branchStructure.branchType == BranchType.HOTFIX) {
            printOpen("It is a Hotfix branch, so architecture version is not validated.", EchoLevel.INFO)
        } else {
            printOpen("Validating architecture version...", EchoLevel.INFO)
            pomXmlStructure.validateArtifact(pipelineData.bmxStructure.environment)
            printOpen("Architecture version is OK", EchoLevel.INFO)

        }
        //pomXmlStructure.validateArtifact()

    } catch (Exception e) {
        printOpen("Error validating architecture version: ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "100", null, null, "error")
        throw e
    }

    debugInfo(pipelineParams, pomXmlStructure, pipelineData)

    //INIT AND DEPLOY
    initCloudDeploy(pomXmlStructure, pipelineData)

    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "100")

    printOpen( "deployToTstParam is " + deployToTst +
        "\nisArchetype is " + isArchetype +
        "\narchetypeModel is "+ archetypeModel,
        EchoLevel.DEBUG)

    }

/** 
 * Step checkCloudAvailiabilityStep
 */    
def checkCloudAvailiabilityStep() {
    printOpen("The artifact ${pomXmlStructure.artifactName} from group ${pomXmlStructure.groupId} the micro to deploy is ${pomXmlStructure.artifactMicro}", EchoLevel.DEBUG)
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "110")
    try {
        checkCloudAvailability(pomXmlStructure,pipelineData,"PRE","BOTH")
        if (deployToTst) {
            checkCloudAvailability(pomXmlStructure,pipelineData,"TST","DEPLOY")
        }
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "110")
    } catch (Exception e) {
        printOpen("Error checking Cloud availability: ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "110", null, null, "error")
        throw e
    }
}

/** 
 * Step prepareReleaseStep
 */    
def prepareReleaseStep() { 
    kpiLogger(pomXmlStructure, pipelineData,KpiLifeCycleStage.CREATE_RELEASE_STARTED, KpiLifeCycleStatus.OK)
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "200")
    
    try {
        validateBranch(pomXmlStructure.getArtifactVersionWithoutQualifier(), pipelineData.branchStructure)
    } catch (Exception e) {
        printOpen("Error preparing release: ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "200", null, null, "error")
        throw e
    }

    updateVersionForRelease(pomXmlStructure)
    calculatePreviousInstalledVersionInEnvironment(pipelineData, pomXmlStructure)
    pipelineData.buildCode = pomXmlStructure.artifactVersion
    currentBuild.displayName = "Creation_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "200")
}

/** 
 * Step validateDependenciesVersionStep
 */    
def validateDependenciesVersionStep() { 
    almPipelineStageValidateDependenciesVersion(pomXmlStructure, pipelineData, "210")
}

/** 
 * Step validateVersionStep
 */    
def validateVersionStep() {
    almPipelineStageValidateVersion(pomXmlStructure, pipelineData, "300")
}		

/** 
 * Step deployBBBDDScriptsStep
 */    
def deployBBBDDScriptsStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "406")
    printOpen("Deploying sql to the BBDD", EchoLevel.DEBUG)

    try {

        def missatge=generateSqlScriptRelease(pomXmlStructure,pipelineData,false)
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "406",missatge,null,"ended")

    }catch(Exception e) {

        printOpen("Error deploying BBDD scripts: ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "406", null, null, "error")
        throw e

    }
}

/** 
 * Step buildStep
 */    
def buildStep() { 
    //build the workspace
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.BUILD_STARTED, KpiLifeCycleStatus.OK)
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "400")
    printOpen("Building the branch", EchoLevel.DEBUG)

    try {

        buildWorkspace(pomXmlStructure, pipelineData)

    } catch (Exception e) {

        kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.BUILD_FINISHED, KpiLifeCycleStatus.KO)
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "400", null, null, "error")
        throw e

    }

    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "400")
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.BUILD_FINISHED, KpiLifeCycleStatus.OK)
    almPipelineStageValidateDependencyRestrictions(pomXmlStructure, pipelineData, "400")
}

/** 
 * Step checkmarxScanStep
 */    
def checkmarxScanStep() { 
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "403")
    try {
        checkmarxScanWorkspace(pomXmlStructure, pipelineData,true,null,GlobalVars.PRE_ENVIRONMENT.toUpperCase())
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "403")
    }catch(Exception e) {
        printOpen("Error during Checkmarx Scan: ${e.getMessage()}", EchoLevel.ERROR) 
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "403", null, null, "error")
        throw e
    }
}

/** 
 * Step errorTranslationsStep
 */    
def errorTranslationsStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "405")
    litmidTranslations(pomXmlStructure, pipelineData)
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "405")
}

/**
 * Step copyConfigFiles
 */
def copyConfigFiles() { 
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "410")

    //build the workspace
    printOpen("Building the branch", EchoLevel.DEBUG)
    // INICIO - IPE
    // Comentario: Si se definen las variables de entorno para Git esto no hace falta
    sh "git config http.sslVerify false"
    // FIN - IPE

    try {
        pushConfigFiles(pomXmlStructure, pipelineData, false, true)

        pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName, pomXmlStructure.artifactType, pomXmlStructure.artifactSubType)
        pipelineData.pipelineStructure.resultPipelineData.onlyConfig = true

        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "410")
    } catch (Exception e) {
        printOpen("Error copying configuration files: ${e.getMessage()}", EchoLevel.ERROR) 
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "410", null, null, "error")
        throw e
    }
   
}

/** 
 * Step deployMicroArtifactoryCloudStep
 */    
def deployMicroArtifactoryCloudStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "415")
    
    if (existsArtifactDeployed(pomXmlStructure,pipelineData)) {
        printOpen("We have found that Nexus already has the artifact. We'll not try to deploy.", EchoLevel.INFO)
    }else {
        deployMicrosNexus(pomXmlStructure, pipelineData)
    }

    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "415")
}

/** 
 * Step deployToCloudCloudStep
 */ 
def deployToCloudCloudStep() { 
    cloudStateUtilitity = almPipelineStageDeployToCloud(pomXmlStructure, pipelineData, "501", "01-<phase>-deploy")
}

/** 
 * Step runRemoteITStep
 */    
def runRemoteITStep() { 
    almPipelineStageRunRemoteIT(pomXmlStructure, pipelineData, "503", "03-<phase>-runRemoteIT-pre-consolidateNew", cloudStateUtilitity)
}

/** 
 * Step runRemoteItOnOldVersionStep
 */    
def runRemoteItOnOldVersionStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "504")

    def resultSyntetic=[:]
    executeSynteticTests(pomXmlStructure,pipelineData,resultSyntetic)
    if (resultSyntetic.doesntExists==true) {
        printOpen("No podemos pasar los Test Integracion", EchoLevel.INFO)
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "504", null, null, "warning")
    }else {
        if (resultSyntetic.resultOK==true) {
            printOpen("Los Test de Integracion han acabado OK", EchoLevel.INFO)
            sendStageEndToAppPortal(pomXmlStructure, pipelineData, "504")
        }else { 
            printOpen("Error - Config no retro compatible", EchoLevel.ERROR)
            sendStageEndToAppPortal(pomXmlStructure, pipelineData, "504", null, null, "error")
            //throw new Exception("Error - Config no retro compatible") 
        }
    }
}

/** 
 * Step consolidateCloudCloudStep
 */                    	
def consolidateCloudCloudStep() { 
    almPipelineStageConsolidateNewDeploy(pomXmlStructure, pipelineData, "505", "04-<phase>-consolidateNew", cloudStateUtilitity)
}

/** 
 * Step cloneToOcpPreStep
 */    
def cloneToOcpPreStep() { 
    almPipelineStageCloneToOcp(pomXmlStructure, pipelineData, user)
}

/** 
 * Step postDeployStep
 */    
def postDeployStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "507")

    printOpen("env.DEPLOY_MODE_SPECIAL: ${env.DEPLOY_MODE_SPECIAL}", EchoLevel.DEBUG)

    if (pipelineData.deployStructure!=null) {
        printOpen("The deployment type is: ${pipelineData.deployStructure.cannaryType} and the global variable is: ${env.CAMPAIGN_CANARY_FEATURE_ENABLED}", EchoLevel.DEBUG)

        if (pipelineData.deployStructure.cannaryType == GlobalVars.CANARY_TYPE_CAMPAIGN && env.CAMPAIGN_CANARY_FEATURE_ENABLED!=null && "true".equals(env.CAMPAIGN_CANARY_FEATURE_ENABLED)) {
            //Tenemos que añadir el micro a la IOP
            printOpen("The following operations are restricted", EchoLevel.DEBUG)
            pipelineData.pipelineStructure.resultPipelineData.isDevops=false
        }
    }else {
        printOpen("It is a library", EchoLevel.DEBUG)
    }

    boolean deployOnSingleCenter = env.DEPLOY_MODE_SPECIAL.contains(pomXmlStructure.artifactName)
    printOpen("env.DEPLOY_MODE_SPECIAL.contains(${pomXmlStructure.artifactName}): ${deployOnSingleCenter}", EchoLevel.DEBUG)
    pipelineData.pipelineStructure.resultPipelineData.nextDistributionMode = DistributionPROResolver.determineFirstDistributionModeOnPRO(deployOnSingleCenter)
    printOpen("Distribution mode on PRO: ${pipelineData.pipelineStructure.resultPipelineData.nextDistributionMode}", EchoLevel.DEBUG)

    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "507")
}

/** 
 * Step apiManagerTechnicalServiceRegistrationStep
 */    
def apiManagerTechnicalServiceRegistrationStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "510")
    
    printOpen("Publishing swagger contract to API Manager (adpbdd-micro)", EchoLevel.DEBUG)
    publishSwaggerContract2ApiManager(pipelineData, pomXmlStructure)

    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "510")
}

/** 
 * Step copyConfigFilesAndDeployToCloudTstCloudStep
 */    
def copyConfigFilesToTstCloudStep() {
    try {
        sendStageStartToAppPortal(pomXmlStructure, pipelineData, "521")

        printOpen("Start copy config file to tst", EchoLevel.DEBUG)

        //Tenemos que usar el mismo id de BUILD i la misma imagen que ya tenemos calculada
        //En el el deploy a PRE
        bmxStructurePRE = pipelineData.bmxStructure
        resultDeployInfoPRE = pipelineData.resultDeployInfo
        ancientMapInfoPRE = pipelineData.ancientMapInfo

        def bmxStructure = new TstBmxStructure()
        pipelineData.bmxStructure = bmxStructure
        pipelineData.pipelineStructure.resultPipelineData.environment = bmxStructure.environment
       
        pushConfigFiles(pomXmlStructure, pipelineData, false, false)
       
    } catch (Exception e) {
        String artifactAppAbort = pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name))
        printOpen("En al copiar los ficheros de configuracion a tst ${e}", EchoLevel.DEBUG)
        kpiLogger(pomXmlStructure, pipelineData,KpiLifeCycleStage.DEPLOY_FINISHED, "KO")
        abortPipelineCloud(pomXmlStructure, pipelineData, " Resultado ejecucion app ${artifactAppAbort} - ${pipelineData.getPipelineBuildName()}  KO - ${deployCloudPhases} - eror al copiar los ficheros de configuración")
    } finally {
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "521")
    }
}

/** 
 * Step copyConfigFilesAndDeployToCloudTstCloudStep
 */    
def deployToCloudTstCloudStep() {
    try {
        sendStageStartToAppPortal(pomXmlStructure, pipelineData, "522")

        printOpen("The cloudStateUtility tst ${cloudStateUtilitity}", EchoLevel.DEBUG)

        kpiLogger(pomXmlStructure, pipelineData,KpiLifeCycleStage.DEPLOY_STARTED, KpiLifeCycleStatus.OK)
                                    
        cloudStateUtilitity = deployCloud(pomXmlStructure, pipelineData, cloudStateUtilitity.buildId, cloudStateUtilitity.newImage)
        deployCloudPhases = "03-pre-runRemoteIT-pre-consolidateNew"
        runRemoteITCloud(pomXmlStructure, pipelineData, cloudStateUtilitity)
        deployCloudPhases = "03-post-runRemoteIT-pre-consolidateNew"
        deployCloudPhases = "04-pre-consolidateNew"
        consolidateNewDeployCloud(pomXmlStructure, pipelineData, cloudStateUtilitity)
        deployCloudPhases = "04-post-consolidateNew"
        
        kpiLogger(pomXmlStructure, pipelineData,KpiLifeCycleStage.DEPLOY_FINISHED, KpiLifeCycleStatus.OK)

        pipelineData.bmxStructure = bmxStructurePRE
        pipelineData.pipelineStructure.resultPipelineData.environment = bmxStructurePRE.environment
        pipelineData.resultDeployInfo = resultDeployInfoPRE
        pipelineData.ancientMapInfo = ancientMapInfoPRE

        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "522")   
    } catch (Exception e) {
        String artifactAppAbort = pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name))
        
        resultDeployCloud = "KO"
        printOpen("Error en el deploy a Cloud, de momento nos comemos el error hasta que esto sea estable ${e}", EchoLevel.DEBUG)
        sendEmail(" Resultado ejecucion app ${artifactAppAbort} - ${pipelineData.getPipelineBuildName()}  KO - ${deployCloudPhases}", env.ALM_SERVICES_EMAIL_Cloud_DEPLOY_RESULT, "${artifactAppAbort} rama ${pipelineData.getPipelineBuildName()}", "KO en el paso ${deployCloudPhases}")
        kpiLogger(pomXmlStructure, pipelineData,KpiLifeCycleStage.DEPLOY_FINISHED, resultDeployCloud)
        
        abortPipelineCloud(pomXmlStructure, pipelineData, " Resultado ejecucion app ${artifactAppAbort} - ${pipelineData.getPipelineBuildName()}  KO - ${deployCloudPhases}")
        
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "522", null, pipelineData.bmxStructure.environment, "error") 
    }
}

/** 
 * Step apimanagerTechnicalServicesRegistrationTstStep
 */    
def apimanagerTechnicalServicesRegistrationTstStep() {
               
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "530")

    BmxStructure bmxStructurePRE = pipelineData.bmxStructure

    BmxStructure bmxStructure = new TstBmxStructure()
    pipelineData.bmxStructure = bmxStructure
    pipelineData.pipelineStructure.resultPipelineData.environment = bmxStructure.environment

    printOpen("Publishing swagger contract to API Manager (adpbdd-micro)", EchoLevel.DEBUG)
    publishSwaggerContract2ApiManager(pipelineData, pomXmlStructure)

    pipelineData.bmxStructure = bmxStructurePRE
    pipelineData.pipelineStructure.resultPipelineData.environment = bmxStructurePRE.environment

    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "530")
}

/** 
 * Step generateArchetypeFromProjectStep
 */    
def generateArchetypeFromProjectStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "540")
    generateArchetypeFromProject(archetypeModel)
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "540")
}

/** 
 * Step deployArchetypeIntoNexusStep
 */    
def deployArchetypeIntoNexusStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "550")
    deployArchetypeIntoNexus(archetypeModel, pomXmlStructure)
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "550")
}

/** 
 * Step publishClientStep
 */    
def publishClientStep() {
    //build the workspace
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "600")
    publishClient(pomXmlStructure, pipelineData)
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "600")
}

/** 
 * Step pushReleaseToGitStep
 */    
def pushReleaseToGitStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "700")
    pushRepoUrl(pomXmlStructure, "${originBranch}", false, true, GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_RELEASE)
    tagVersion(pomXmlStructure, pipelineData, true, false)
    pipelineData.pipelineStructure.resultPipelineData.isTagged = true
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "700")
}

/** 
 * Step deployArtifactoryStep
 */    
def deployArtifactoryStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "800")
    deployNexus(pomXmlStructure, pipelineData)
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "800")
    sendPipelineUpdateToAppPortal(initAppPortal, pomXmlStructure, pipelineData, '')
}

/** 
 * Step publishArtifactCatalogStep
 */    
def publishArtifactCatalogStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "910")
    printOpen("Publishing artifact in catalog", EchoLevel.ALL)

    if (pomXmlStructure.isMicro()) pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName)
    else pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactName, pomXmlStructure.artifactName)
    //set the buildCode to the final version
    pipelineData.setBuildCode(pomXmlStructure.artifactVersion)

    //Cuidado que aqui tenemos que tener en cuenta que va a hacer deploy a TST y PRE
    if (pipelineData!=null && pipelineData.deployStructure!=null) {
        pipelineData.deployStructure.envCloud=GlobalVars.PRE_ENVIRONMENT
    }
    publishArtifactInCatalog(pipelineData, pomXmlStructure,cloudStateUtilitity) 
    if (deployToTst) {
        BmxStructure bmxStructure = new TstBmxStructure()
        printOpen("Se ha desplegado la Release a  TST. Procedemos al deploy en el catalogo", EchoLevel.DEBUG)
        deployArtifactInCatalog(null,pipelineData,pomXmlStructure,cloudStateUtilitity,bmxStructure.environment)
    
    }
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "910")
}

/**
 * Stage 'promote-mule-contract'
 */
def promoteMuleContractStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "1000")
 
    printOpen("Promote swagger contract to MuleSoft", EchoLevel.INFO)
    def result = promoteContract2MuleSoft(pipelineData, pomXmlStructure)

    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "1000", result , null, result ? "warning" : "ended")
}

/** 
 * Step endPipelineSuccessStep
 */    
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("SUCCESS", EchoLevel.INFO)
    sendPipelineResultadoToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)
    if (deployToTst && pipelineData.deployFlag) {
        
        pipelineData.pipelineStructure.resultPipelineData.environment = GlobalVars.TST_ENVIRONMENT
        sendPipelineNotifyDeploymentToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline )
        //sendPipelineResultadoToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)
        pipelineData.pipelineStructure.resultPipelineData.environment = GlobalVars.PRE_ENVIRONMENT
        
    }										
    if ( almEvent!=null ) {
            long endCallStartMillis = new Date().getTime()
            kpiLogger(almEvent.pipelineSuccess(endCallStartMillis-initCallStartMillis))						 
    }					
    sendPipelineEndedToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)
    kpiLogger(pomXmlStructure, pipelineData,KpiLifeCycleStage.CREATE_RELEASE_FINISHED, KpiLifeCycleStatus.OK)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.OK)

    if (pipelineData.getExecutionMode().invokeNextActionAuto() && !pipelineData.isPushCI()) {
        printOpen("Modo test activado en fase de crear release", EchoLevel.DEBUG)
        invokeNextJob(pipelineData, pomXmlStructure)
    }
}

/** 
 * Step endPipelineFailureStep
 */    
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("FAILURE", EchoLevel.ERROR)
    if (pipeline!=null && pipelineData.pipelineStructure!=null && pipelineData.pipelineStructure.resultPipelineData!=null) {
        printOpen("onlyConfig ${pipelineData.pipelineStructure.resultPipelineData.onlyConfig}", EchoLevel.DEBUG)
        printOpen("isNowDeployed ${pipelineData.pipelineStructure.resultPipelineData.isNowDeployed}", EchoLevel.DEBUG)
        printOpen("artifactType ${pipelineData.pipelineStructure.resultPipelineData.artifactType.toString()}", EchoLevel.DEBUG)
        printOpen("artifactSubType ${pipelineData.pipelineStructure.resultPipelineData.artifactSubType.toString()}", EchoLevel.DEBUG)
    }      
    kpiLogger(pomXmlStructure, pipelineData,KpiLifeCycleStage.CREATE_RELEASE_FINISHED, KpiLifeCycleStatus.KO)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.KO)
    if ( almEvent!=null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis-initCallStartMillis))
    }
    if (pipelineData!=null && pipelineData.deployStructure != null) {
        pipelineData.pipelineStructure.resultPipelineData.isNowDeployed = pipelineData.isNowDeployed
    }
    sendPipelineResultadoToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)
}

/** 
 * Step endPipelineAlwaysStep
 */    
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}
