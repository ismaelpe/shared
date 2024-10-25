import groovy.transform.Field
import com.caixabank.absis3.*

@Field Map pipelineParams

@Field String gitURL = "https://git.svb.lacaixa.es/"
@Field String gitCredentials = "GITLAB_CREDENTIALS"
@Field String jenkinsPath = "absis3/services"

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
@Field boolean initGpl
@Field boolean successPipeline

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
    initGpl = false
    successPipeline = false
    
    /*
     * Pasos a seguir:
     * */    
    pipeline {      
		agent {	node (absisJenkinsAgent(agentParam)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
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
                    expression { GlobalVars.GSA_ENABLED }
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

    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100")
    initGpl = true

    printOpen("The environment is ${pipelineData.bmxStructure.environment}", EchoLevel.ALL)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")

    printOpen("isArchetype is ", EchoLevel.ALL)
}

/** 
 * Step prepareReleaseStep
 */
def prepareReleaseStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "200")
    validateBranch(pomXmlStructure.getArtifactVersionWithoutQualifier(), pipelineData.branchStructure)
    updateVersionForRelease(pomXmlStructure)
    pipelineData.buildCode = pomXmlStructure.artifactVersion
    currentBuild.displayName = "Creation_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    sendStageEndToGPL(pomXmlStructure, pipelineData, "200")
}

/** 
 * Step copyConfigFilesStep
 */
def copyConfigFilesStep() {
    printOpen("Building the branch In the create Release", EchoLevel.ALL)
    
    sh "git config http.sslVerify false"

    printOpen("log pipeline ${pipelineData.toString()}", EchoLevel.ALL)
    pushConfigFilesFromCfgProject(pomXmlStructure, GlobalVars.PRE_ENVIRONMENT)

    sendStageEndToGPL(pomXmlStructure, pipelineData, "410")
}

/** 
 * Step refreshMicrosStep
 */
def refreshMicrosStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "810")
    try {
        refreshDependencyConfig(pomXmlStructure,pipelineData,'BOTH',GlobalVars.PRE_ENVIRONMENT)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "810")
    }catch (Exception e) {
        sendStageEndToGPLAndThrowError(pomXmlStructure, pipelineData, "810",e)
    }
}

/** 
 * Step pushReleaseToGitStep
 */
def pushReleaseToGitStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "700")
    pushRepoUrl(pomXmlStructure, "${originBranch}", false, true, GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_RELEASE)
    tagVersion(pomXmlStructure, pipelineData, true, false)
    pipelineData.pipelineStructure.resultPipelineData.isTagged = true
    sendStageEndToGPL(pomXmlStructure, pipelineData, "700")
}

/** 
 * Step deployActifactoryStep
 */
def deployActifactoryStep() {            
    sendStageStartToGPL(pomXmlStructure, pipelineData, "800")
    deployNexus(pomXmlStructure, pipelineData)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "800")
    sendPipelineUpdateToGPL(initGpl, pomXmlStructure, pipelineData, '')
}

/** 
 * Step publishArtifactCatalogStep
 */
def publishArtifactCatalogStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "910")

    printOpen("publishing artifact in catalog", EchoLevel.ALL)

    if (pomXmlStructure.isMicro()) pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName)
    else pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactName, pomXmlStructure.artifactName)
    //set the buildCode to the final version
    pipelineData.setBuildCode(pomXmlStructure.artifactVersion)

    publishArtifactInCatalog(pipelineData, pomXmlStructure)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "910")    
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
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)

    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)

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
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
}
