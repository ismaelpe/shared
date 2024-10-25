import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

@Field String gitURL
@Field String gitCredentials
@Field String jenkinsPath

@Field String originBranch
@Field String pathToRepo
@Field String repoName
@Field String artifactSubType
@Field String artifactType
@Field String pipelineOrigId
@Field String executionProfileParam
@Field String targetAlmFolderParam
@Field String version
@Field String user
@Field String commitId
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
    executionProfileParam = params.executionProfileParam
    targetAlmFolderParam = params.targetAlmFolderParam
    version = params.versionParam
    user = params.userId
	commitId = params.commitIdParam
    loggerLevel = params.loggerLevel
    agentParam = params.agent

    initGpl = false
    successPipeline = false

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
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()                   
                }
            }
            stage('copy-config-files') {
                steps {
                    copyConfigFilesStep()
                }
            }
			stage('refresh-micros') {
				when {
					expression { (pipelineData.branchStructure.branchType == BranchType.HOTFIX) }
				}
				steps {
                    refreshMicrosStep()
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
 * Stage getGitRepoStep
 */
def getGitRepoStep() {
    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null
    printOpen("Extract GIT Repo ${pathToRepo} ${originBranch}", EchoLevel.ALL)
    pomXmlStructure = getGitRepo(pathToRepo, '', repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), version, true)
    calculateArchVersionWithModules(pomXmlStructure)

    pipelineData = new PipelineData(PipelineStructureType.IOP_PRO_CONFIGLIB, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.commitId = commitId
    pipelineData.initFromIOPPro(pathToRepo, originBranch, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), repoName)
    pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolderParam)
    printOpen("DistributionModePRO has been defined as: ${pipelineData.distributionModePRO}", EchoLevel.ALL)
    pipelineData.pushUser = user

    pipelineData.buildCode = pomXmlStructure.artifactVersion

    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100")
    currentBuild.displayName = "Deploying_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    initGpl = true

    debugInfo(null, pomXmlStructure, pipelineData)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")
}

/**
 * Stage copyConfigFilesStep
 */
def copyConfigFilesStep() {
    printOpen("Building the branch in deploy to pro Config Lib", EchoLevel.ALL)
    sh "git config http.sslVerify false"
    printOpen("log pipeline  ${pipelineData.toString()}", EchoLevel.ALL)

    pushConfigFilesFromCfgProject(pomXmlStructure, GlobalVars.PRO_ENVIRONMENT)

    pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactName, pomXmlStructure.artifactName, pomXmlStructure.artifactType, pomXmlStructure.artifactSubType)

    sendStageEndToGPL(pomXmlStructure, pipelineData, "300")
}

/**
 * Stage refreshMicrosStep
 */
def refreshMicrosStep() {
     sendStageStartToGPL(pomXmlStructure, pipelineData, "310")						
     try {
         if (pipelineData.branchStructure.branchType == BranchType.HOTFIX) {
             refreshDependencyConfig(pomXmlStructure,pipelineData,'BOTH',GlobalVars.PRO_ENVIRONMENT)
         }
         pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactName, pomXmlStructure.artifactName)
         publishArtifactInCatalog(pipelineData, pomXmlStructure)
	 sendStageEndToGPL(pomXmlStructure, pipelineData, "310")
     }catch (Exception e) {
         sendStageEndToGPLAndThrowError(pomXmlStructure, pipelineData, "310",e)
     }	
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
    printOpen("Se success el pipeline ${successPipeline}", EchoLevel.INFO)

    successPipeline = true
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)

    if (pipelineData.getExecutionMode().invokeNextActionAuto()) {
        printOpen("Modo test activado en fase de deploy to pro ConfigLib", EchoLevel.ALL)
        invokeNextJob(pipelineData, pomXmlStructure)
    }
}

/**
 * Stage endPipelineFailureStep
 */
def endPipelineFailureStep() {
    printOpen("Se failure el pipeline ${successPipeline}", EchoLevel.ERROR)
    successPipeline = false
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
}
