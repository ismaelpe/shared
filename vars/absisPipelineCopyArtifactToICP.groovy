import com.project.alm.ArtifactSubType
import com.project.alm.ArtifactType
import com.project.alm.BranchStructure
import com.project.alm.BranchType
import com.project.alm.DevBmxStructure
import com.project.alm.DistributionModePRO
import com.project.alm.EchoLevel
import com.project.alm.GarAppType
import com.project.alm.GlobalVars
import com.project.alm.ICPDeployStructure
import groovy.transform.Field
import com.project.alm.ICPStateUtility
import com.project.alm.ICPVarPipelineCopyType
import com.project.alm.ICPWorkflowStates
import com.project.alm.MavenUtils
import com.project.alm.PipelineData
import com.project.alm.PipelineExecutionMode
import com.project.alm.PipelineStructureType
import com.project.alm.PomXmlStructure
import com.project.alm.PreBmxStructure
import com.project.alm.ProBmxStructure
import com.project.alm.Strings
import com.project.alm.TstBmxStructure
import com.project.alm.CustomExecutionMode

@Field Map pipelineParams

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean successPipeline

@Field String version
@Field String pathToRepo
@Field String repoName
@Field String artifactSubType
@Field String artifactType
@Field String originBranch
@Field String versionTag

@Field String pathFeature

@Field String executionMode
@Field String electionOriginArtifact
@Field String environmentDest

@Field String commitId
@Field String user
@Field String pipelineOrigIdVar
@Field boolean initGpl

@Field String actualVersionICP

@Field ICPStateUtility icpStateUtilitity

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

	successPipeline = false

    version = params.versionParam
    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam
    artifactSubType = params.artifactSubTypeParam
    artifactType = params.artifactTypeParam
    originBranch = params.originBranchParam
    versionTag = params.versionParam
    pathFeature = params.pathFeatureParam
    executionMode = params.executionModeParam
    electionOriginArtifact = params.electionOriginArtifactParam
    environmentDest = params.environmentDestParam
	commitId = params.commitIdParam
	user = params.userId
	pipelineOrigIdVar = params.pipelineOrigId

    initGpl = false

    actualVersionICP = null
	
    icpStateUtilitity = null
    /*
    * Pasos a seguir:
    * 0- Crear Folder
    * 1- Crear Repo
    */
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '50'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
			JNKMSV = credentials('JNKMSV-USER-TOKEN')
            ICP_CERT = credentials('icp-alm-pro-cert')
            ICP_PASS = credentials('icp-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
            sendLogsToGpl = true
        }
        //Atencion que en el caso que estemos en un MergeRequest... quizas solo debamos validar la issue
        stages {
            stage('init') {
                steps {
                    initStep()
                }
            }
			stage('check-ICP-availiability'){
				steps {
                    checkICPAvailiabilityStep()
				}
			}
            stage('build') {
                steps {
                    buildStep()
                }
            }
			stage('copy-config-files') {
				when {
					expression { (pipelineData.garArtifactType == GarAppType.DATA_SERVICE) }
				}
				steps {
                    copyConfigFilesStep()
				}
			}
            stage('deploy') {
                steps {
                    deployStep()
                }
            }
            stage('publish-artifact-catalog') {
                when {
                    expression { GlobalVars.GSA_ENABLED }
                }
                steps {
                    publishArtifactCatalogStep()
                }
            }
			stage('clone-to-ocp'){
				steps{
					cloneToOcp()
				}
			}
            stage('apimanager-technicalservices-registration') {
                when {
                    expression { ifApiManagerTechnicalServiceRegistrationApplies(pipelineData, pomXmlStructure) }
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
                endiPipelineAlwaysStep()
            }
        }
    }
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/**
 * Stage 'initStep'
 */
def initStep() {
    initGlobalVars(pipelineParams)
    printOpen("El tag de la version es de #${versionTag}# ${versionTag.length()}", EchoLevel.INFO)

    if (ICPVarPipelineCopyType.valueOfVarPipelineCopyType(electionOriginArtifact) == ICPVarPipelineCopyType.ORIGIN_TAG) {
        printOpen("Con version ${versionTag} ", EchoLevel.DEBUG)
        pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), versionTag, true)
    } else {
        printOpen("sin version", EchoLevel.DEBUG)
        pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false)
    }

    pipelineData = new PipelineData(PipelineStructureType.CI, "${env.BUILD_TAG}", params)
    BranchStructure branchStructure = getBranchInfo(originBranch)
    
    printOpen("initFromCopy", EchoLevel.ALL)
    pipelineData.initFromCopy(branchStructure, ArtifactSubType.valueOfSubType(artifactSubType), ArtifactType.valueOfType(artifactType),environmentDest)
    pipelineData.pushUser = user
    pipelineData.manualCopyExecutionMode = ICPVarPipelineCopyType.valueOfVarPipelineCopyType(executionMode)
    pipelineData.manualCopyElectionOriginArtifact = ICPVarPipelineCopyType.valueOfVarPipelineCopyType(electionOriginArtifact)
    printOpen("endInitFromCopy", EchoLevel.ALL)
    pipelineData.initDomainProperties(pathToRepo)

    printOpen("domain ${pipelineData.domain}", EchoLevel.DEBUG)
    printOpen("subDomain ${pipelineData.subDomain}", EchoLevel.DEBUG)

    pipelineData.deployFlag = true
    pipelineData.deployOnIcp = true

    debugInfo(pipelineParams, pomXmlStructure, pipelineData)


    pipelineData.garArtifactType = pipelineData.initFromGitUrlGarApp(pathToRepo, ArtifactSubType.valueOfSubType(artifactSubType))
    calculateArchVersionWithModules(pomXmlStructure)
    String artifactApp = pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name))


    if (environmentDest != "NONE") {
        if (environmentDest == GlobalVars.DEV_ENVIRONMENT) pipelineData.bmxStructure = new DevBmxStructure()
        else if (environmentDest == GlobalVars.TST_ENVIRONMENT) pipelineData.bmxStructure = new TstBmxStructure()
        else if (environmentDest == GlobalVars.PRE_ENVIRONMENT) pipelineData.bmxStructure = new PreBmxStructure()
        else if (environmentDest == GlobalVars.PRO_ENVIRONMENT) pipelineData.bmxStructure = new ProBmxStructure()

        //Las ramas no estan correctas... un deploy a un entorno que no sea el pertineente se tiene que comportar como tal
        //Por ejemplo si es una rama feature y se depliega en dev tiene que ser master
        //Solo una rama release se puede desplegar en tst,pre,pro

        if (environmentDest == GlobalVars.DEV_ENVIRONMENT) pipelineData.branchStructure.branchType = BranchType.MASTER
        else if (environmentDest == GlobalVars.TST_ENVIRONMENT ||
            environmentDest == GlobalVars.PRE_ENVIRONMENT ||
            environmentDest == GlobalVars.PRO_ENVIRONMENT) pipelineData.branchStructure.branchType = BranchType.RELEASE
    }

    if (pipelineData.manualCopyElectionOriginArtifact == ICPVarPipelineCopyType.ORIGIN_TAG) {
        //Viene de un tag vamos a modificar el valor de la version
        pomXmlStructure.artifactVersion = versionTag
    }

    currentBuild.displayName = "Copy_to_" + environmentDest + "_${env.BUILD_ID}_" + " " + pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name)) + " " + pomXmlStructure.artifactVersion

    if (GarAppType.valueOfType(pipelineData.garArtifactType.name) == GarAppType.DATA_SERVICE) {
        generateDataSourceFile(pomXmlStructure, pipelineData)
        printOpen("VCAPS ${pipelineData.vcapsServiceIds}", EchoLevel.INFO)
    }
    
    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigIdVar)
    initGpl = true
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100")
    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")

}

def cloneToOcp() {
	absisPipelineStageCloneToOcp(pomXmlStructure, pipelineData,user,'false')
}
/**
 * Stage 'checkICPAvailiabilityStep'
 */
def checkICPAvailiabilityStep() {
    printOpen("The artifact ${pomXmlStructure.artifactName} from group ${pomXmlStructure.groupId} is deploying the micro ${pomXmlStructure.artifactMicro}", EchoLevel.DEBUG)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "105")
    try {
        checkICPAvailability(pomXmlStructure,pipelineData,"PRE","BOTH")

        // Comprobamos la version última antes del deploy. Ya que en caso contrario nos devolverá la misma version.
        def actualVersionICPResult = getInfoAppICP(pomXmlStructure, pipelineData, GlobalVars.ENDPOINT_INFO, environmentDest)
        if (actualVersionICPResult != null) {
            actualVersionICP = actualVersionICPResult.build.version
        } 
        sendStageEndToGPL(pomXmlStructure, pipelineData, "105")
    } catch (Exception e) {
        printOpen("Error checking ICP availiability: ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "105", null, null, "error")
        throw e
    }
}

/**
 * Stage 'buildStep'
 */
def buildStep() {

    sendStageStartToGPL(pomXmlStructure, pipelineData, "200")
    
    try {
        if (pipelineData.manualCopyExecutionMode == ICPVarPipelineCopyType.EX_MODE_ALL && pipelineData.manualCopyElectionOriginArtifact != ICPVarPipelineCopyType.ORIGIN_TAG) {
        pipelineData.pipelineDataExecutionMode=new CustomExecutionMode()
                pipelineData.pipelineDataExecutionMode.skipTest=true
                pipelineData.pipelineDataExecutionMode.skipIntegrationTest=true
                pipelineData.pipelineDataExecutionMode.skipJavadoc=true
            buildWorkspace(pomXmlStructure, pipelineData)
        }
        if ((pipelineData.manualCopyExecutionMode == ICPVarPipelineCopyType.EX_MODE_DEPLOY_IT || pipelineData.manualCopyExecutionMode == ICPVarPipelineCopyType.EX_MODE_DEPLOY ||
        pipelineData.manualCopyExecutionMode == ICPVarPipelineCopyType.EX_MODE_ALL) && pipelineData.manualCopyElectionOriginArtifact != ICPVarPipelineCopyType.ORIGIN_TAG) {
            deployMicrosNexus(pomXmlStructure, pipelineData)
        }
    } catch (Exception e) {
        sendStageEndToGPL(pomXmlStructure, pipelineData, "200", null, null, "error")
        throw e
    }
    
    sendStageEndToGPL(pomXmlStructure, pipelineData, "200")
    
}


/**
 * Stage 'copyConfigFilesStep'
 */
def copyConfigFilesStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "220")

    try {
        pushConfigFiles(pomXmlStructure, pipelineData, false, pipelineData.bmxStructure.usesConfigServer())
        sendStageEndToGPL(pomXmlStructure, pipelineData, "220")
    } catch (Exception e) {
        printOpen("Error pushing config files: ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "220", null, null, "error")
        throw e
    }

}

/**
 * Stage 'initiatePrdeployStepovisioningStep'
 */
def deployStep() {

    sendStageStartToGPL(pomXmlStructure, pipelineData, "300")
    
    String deployICPPhases = "01-pre-deploy"
    String artifactApp = pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name))
    try {

        //deploy the project                            
        if (pipelineData.manualCopyExecutionMode == ICPVarPipelineCopyType.EX_MODE_ALL ||
            pipelineData.manualCopyExecutionMode == ICPVarPipelineCopyType.EX_MODE_DEPLOY ||
            pipelineData.manualCopyExecutionMode == ICPVarPipelineCopyType.EX_MODE_DEPLOY_IT) {
            printOpen("Deploying micro", EchoLevel.INFO)
            icpStateUtilitity = deployICP(pomXmlStructure, pipelineData)
        } else {

            icpStateUtilitity = new ICPStateUtility(pipelineData, pomXmlStructure, "a", "b", "B", "1", null, null, ICPWorkflowStates.NEW_DEPLOY, MavenUtils.sanitizeArtifactName(pomXmlStructure.artifactName, pipelineData.garArtifactType), pomXmlStructure.getArtifactMajorVersion())
            ICPDeployStructure deployStructure = new ICPDeployStructure('cxb-ab3cor', 'cxb-ab3app', pipelineData.bmxStructure.environment)
            deployStructure.springProfilesActive =
                pomXmlStructure.archVersion.endsWith("-SNAPSHOT") ?
                    deployStructure.calculateSpringCloudActiveProfiles(pomXmlStructure.isApplicationWithNewHealthGroups()) :
                    deployStructure.calculateSpringCloudActiveProfiles(pipelineData.garArtifactType.name, pipelineData.company, pomXmlStructure.isApplicationWithNewHealthGroups())

            icpStateUtilitity.suffixedComponentName = deployStructure.getSuffixedComponentName()
            icpStateUtilitity.icpDeployStructure = deployStructure

            if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {
                icpStateUtilitity.pathFeature = pathFeature
            }
        }
        deployICPPhases = "02-post-deploy-pre-runRemoteIT"
        if (pipelineData.manualCopyExecutionMode == ICPVarPipelineCopyType.EX_MODE_ALL ||
            pipelineData.manualCopyExecutionMode == ICPVarPipelineCopyType.EX_MODE_ONLY_IT ||
            pipelineData.manualCopyExecutionMode == ICPVarPipelineCopyType.EX_MODE_DEPLOY_IT)
            printOpen("Run Integration Tests", EchoLevel.INFO)
            runRemoteITICP(pomXmlStructure, pipelineData, icpStateUtilitity)

        deployICPPhases = "03-post-runRemoteIT-pre-consolidateNew"
        if (pipelineData.manualCopyExecutionMode == ICPVarPipelineCopyType.EX_MODE_ALL ||
            pipelineData.manualCopyExecutionMode == ICPVarPipelineCopyType.EX_MODE_DEPLOY ||
            pipelineData.manualCopyExecutionMode == ICPVarPipelineCopyType.EX_MODE_DEPLOY_IT)
            consolidateNewDeployICP(pomXmlStructure, pipelineData, icpStateUtilitity)
        deployICPPhases = "04-post-consolidateNew"

        deployICPPhases = "05-post-deployICP"

        sendStageEndToGPL(pomXmlStructure, pipelineData, "300", null, environmentDest)
        sendPipelineUpdateToGPL(initGpl, pomXmlStructure, pipelineData, '')
                                   
    } catch (Exception e) {
        printOpen("Error en el deploy a ICP, de momento nos comemos el error hasta que esto sea estable ${e}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "300", null, null, "error")
        throw e
        
    }
}

/**
 * Stage 'publishArtifactCatalogStep'
 */
def publishArtifactCatalogStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "400")
    
    pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName)
    pipelineData.gitUrl = pathToRepo

    publishArtifactInCatalog(pipelineData, pomXmlStructure, icpStateUtilitity)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "400")
} 

/**
 * Stage 'apimanagerTechnicalservicesRegistrationStep'
 */
def apimanagerTechnicalservicesRegistrationStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "405")

    printOpen("Publishing swagger contract to API Manager (adpbdd-micro)", EchoLevel.INFO)
    publishSwaggerContract2ApiManager(pipelineData, pomXmlStructure)

    sendStageEndToGPL(pomXmlStructure, pipelineData, "405")
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    successPipeline=true

    // Info para la rollbackartifactory
    pipelineData.pipelineStructure.resultPipelineData.pathRepo = pathToRepo
    pipelineData.pipelineStructure.resultPipelineData.gitUrl = pipelineData.gitUrl
    pipelineData.pipelineStructure.resultPipelineData.gitProject = repoName

    // Indicamos la versión actual , en caso de ser snapshot le quitamos el qualify para que rollback no añada "-dev".
    // en la URL del micro en TST.
    def versionToGPL = versionParam =~ /SNAPSHOT/ ? versionTag.split('-')[0] : versionTag
    pipelineData.pipelineStructure.resultPipelineData.version = versionToGPL

    pipelineData.pipelineStructure.resultPipelineData.oldVersionInCurrentEnvironment = actualVersionICP
    pipelineData.pipelineStructure.resultPipelineData.onlyDeploy = true; // FOrzamos a que se actualice GPL con la nueva version.

    pipelineData.pipelineStructure.resultPipelineData.nextDistributionMode = DistributionModePRO.CANARY_ON_ALL_CENTERS
    pipelineData.pipelineStructure.resultPipelineData.executionProfile = PipelineExecutionMode.DEFAULT_MODE
    pipelineData.pipelineStructure.resultPipelineData.almSubFolder = ""

    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, true)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, true)
    
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline=false
    
    if (initGpl == false) {
        //init pipeline in GPL with minimun parameters
        sendPipelineStartToGPL(pipelineData, pipelineParams)
        initGpl = true
    }
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, false)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, false)
    
}

/**
 * Stage 'endiPipelineAlwaysStep'
 */
def endiPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}

