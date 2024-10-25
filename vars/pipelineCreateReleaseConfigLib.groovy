import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

@Field String gitURL = "https://git.svb.digitalscale.es/"
@Field String gitCredentials = "GITLAB_CREDENTIALS"
@Field String jenkinsPath = "alm/services"

@Field String originBranch
@Field String pathToRepo
@Field String repoName
@Field String artifactSubType
@Field String artifactType
@Field String pipelineOrigId
@Field boolean isArchetype
@Field String commitId
@Field String executionProfileParam
@Field String targetAlmFolderParam
@Field String user
@Field String loggerLevel
@Field String agentParam

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean initAppPortal
@Field boolean successPipeline

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    gitURL = "https://git.svb.digitalscale.es/"
    gitCredentials = "GITLAB_CREDENTIALS"
    jenkinsPath = "alm/services"

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
    originBranch = params.originBranchParam
    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam
    artifactSubType = params.artifactSubTypeParam
    artifactType = params.artifactTypeParam
    pipelineOrigId = params.pipelineOrigId
    isArchetype = false
    commitId = params.commitIdParam
    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam
    user = params.userId
    loggerLevel = params.loggerLevel
    agentParam = params.agent

    /**
     * 1. Modificar la release del artefacto de RC a Release definitiva sin tag
     * 2. Compilar
     * 2. Desplegar a PRE
     * 2.5. Etiquetar cliente ¿? Publish , etc
     * 3. Push GIT + etiqueta
     */
    initAppPortal = false
    successPipeline = false
    
    /*
     * Pasos a seguir:
     * */    
    pipeline {      
		agent {	node (almJenkinsAgent(agentParam)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
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
        //Atencion que en el caso que estemos en un MergeRequest... quizas solo debamos validar la issue
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
            stage('prepare-Release') {
                steps {
                    prepareReleaseStep()
                }
            }
            stage('copy-config-files') {
                steps {
                    copyConfigFilesStep()
                }
            }
            stage('refresh-micros') {
            	when {
            		expression { !pipelineData.isPushCI() && (pipelineData.branchStructure.branchType == BranchType.HOTFIX || pipelineData.branchStructure.branchType == BranchType.RELEASE) }
            	}
            	steps {
                    refreshMicrosStep()            		
            	}
            }
            stage('push-Release-to-git') {
                steps {
                    pushReleaseToGitStep()
                }
            }
            stage('deploy-artifactory') {
                when {
                    expression { !pipelineData.isPushCI() }
                }
                steps {
                    deployActifactoryStep()
                }
            }
            stage('publish-artifact-catalog') {
                when {
                    expression { GlobalVars.BackEndAppPortal_ENABLED }
                }
                steps {
                    publishArtifactCatalogStep()
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

    printOpen("Extract GIT Repo ${pathToRepo} ${originBranch}", EchoLevel.ALL)
    pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false)
    pipelineData = new PipelineData(PipelineStructureType.RELEASE_CONFIGLIB, "${env.BUILD_TAG}", params)
    pipelineData.commitId = commitId
    pipelineData.initFromRelease(pathToRepo, originBranch, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), repoName, isArchetype)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam);
    pipelineData.pushUser = user

    pipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()

    sendPipelineStartToAppPortal(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "100")
    initAppPortal = true

    printOpen("The environment is ${pipelineData.bmxStructure.environment}", EchoLevel.ALL)
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "100")

    printOpen("isArchetype is ", EchoLevel.ALL)
}

/** 
 * Step prepareReleaseStep
 */
def prepareReleaseStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "200")
    validateBranch(pomXmlStructure.getArtifactVersionWithoutQualifier(), pipelineData.branchStructure)
    updateVersionForRelease(pomXmlStructure)
    pipelineData.buildCode = pomXmlStructure.artifactVersion
    currentBuild.displayName = "Creation_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "200")
}

/** 
 * Step copyConfigFilesStep
 */
def copyConfigFilesStep() {
    printOpen("Building the branch In the create Release", EchoLevel.ALL)
    
    sh "git config http.sslVerify false"

    printOpen("log pipeline ${pipelineData.toString()}", EchoLevel.ALL)
    pushConfigFilesFromCfgProject(pomXmlStructure, GlobalVars.PRE_ENVIRONMENT)

    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "410")
}

/** 
 * Step refreshMicrosStep
 */
def refreshMicrosStep() {
    sendStageStartToAppPortal(pomXmlStructure, pipelineData, "810")
    try {
        refreshDependencyConfig(pomXmlStructure,pipelineData,'BOTH',GlobalVars.PRE_ENVIRONMENT)
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "810")
    }catch (Exception e) {
        sendStageEndToAppPortalAndThrowError(pomXmlStructure, pipelineData, "810",e)
    }
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
 * Step deployActifactoryStep
 */
def deployActifactoryStep() {            
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

    printOpen("publishing artifact in catalog", EchoLevel.ALL)

    if (pomXmlStructure.isMicro()) pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName)
    else pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactName, pomXmlStructure.artifactName)
    //set the buildCode to the final version
    pipelineData.setBuildCode(pomXmlStructure.artifactVersion)

    publishArtifactInCatalog(pipelineData, pomXmlStructure)
    sendStageEndToAppPortal(pomXmlStructure, pipelineData, "910")    
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
    printOpen("Se marca el pipeline como ok", EchoLevel.ALL)
    sendPipelineResultadoToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)

    sendPipelineEndedToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)

    if (pipelineData.getExecutionMode().invokeNextActionAuto() && !pipelineData.isPushCI()) {
        printOpen("Modo test activado en fase de crear release", EchoLevel.ALL)
        invokeNextJob(pipelineData, pomXmlStructure)
    }
}

/** 
 * Step endPipelineFailureStep
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("Pipeline has failed", EchoLevel.ERROR)
    sendPipelineResultadoToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToAppPortal(initAppPortal, pomXmlStructure, pipelineData, successPipeline)
}
