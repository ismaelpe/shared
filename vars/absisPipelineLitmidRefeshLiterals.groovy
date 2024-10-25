import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

@Field String artifactName
@Field String artifactType
@Field String majorVersion
@Field String typeApp
@Field String environmentConfigServer
@Field String user
@Field String targetAlmFolder

@Field String  checkoutDirConfigServer
@Field Map limitParamsMap
@Field Map mapResult

@Field ClientInfo clientInfo
@Field PipelineData pipelineData
@Field boolean initGpl
@Field boolean successPipeline

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    artifactName = params.artifactNameParam
    artifactType = params.artifactTypeParam
    majorVersion = params.majorVersionParam
    typeApp = params.typeAppParam
    environmentConfigServer = params.environmentParam
    user = params.userId
    targetAlmFolder = params.targetAlmFolderParam

    checkoutDirConfigServer = "config_server"
    limitParamsMap = [
        writeToDisk: false
    ]

    mapResult = []

    initGpl = false
    successPipeline = false

    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {            
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
            timeout(time: 3, unit: 'HOURS')
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
            executionProfile = "${pipelineParams ? pipelineParams.get('executionProfile', 'DEFAULT') : 'DEFAULT'}"
        }
        stages {
            stage('init-pipeline') {
                steps {
                    initPipelineStep()
                }
            } 
            stage('extract-litmid-literals') {
                steps {
                    extractLitmidLiteralsStep()
                }
            }
            stage('transform-litmid-literals') {
                steps {
                    transformLitmidLiteralsStep()
                }
            }
            stage('load-litmid-literals') {
                steps {
                    loadLitmidLiteralsStep()
                }
            }
            stage('refresh-micro-litmid-literals'){
                steps {
                    refreshMicroLitmidLiterals()
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
 * Stage 'initPipelineStep'
 */
def initPipelineStep() {
    initGlobalVars(pipelineParams)

    clientInfo = new ClientInfo()

    clientInfo.setApplicationName(artifactName.split("-")[0])
    clientInfo.setArtifactId(artifactName)
    clientInfo.setArtifactVersion("${majorVersion}.X.X")
    clientInfo.setArtifactType(ArtifactType.valueOfType(artifactType))
    clientInfo.setArtifactSubType(ArtifactSubType.MICRO_APP)
    clientInfo.setGroupId("com.caixabank.absis")

    printOpen("Client Info: $clientInfo", EchoLevel.INFO)

    pipelineData = new PipelineData(PipelineStructureType.REFRESH_LITERALS, env.BUILD_TAG, env.JOB_NAME, params)
    pipelineData.initVoidActions(null, "master", ArtifactSubType.MICRO_APP, null, GlobalVars.PRO_ENVIRONMENT.toString())
    
    pipelineData.pushUser = user
    pipelineData.garArtifactType = GarAppType.valueOfType(typeApp)
    pipelineData.branchStructure = new BranchStructure()
    pipelineData.branchStructure.branchName = 'master'
    pipelineData.branchStructure.init()

    sendPipelineStartToGPL(clientInfo, pipelineData, "")

    sendStageStartToGPL(clientInfo, pipelineData, "100");
    initGpl = true
    sendStageEndToGPL(clientInfo, pipelineData, "100")
}

/**
 * Stage 'extractLitmidLiteralsStep'
 */
def extractLitmidLiteralsStep() {
    sendStageStartToGPL(clientInfo, pipelineData, "200")
    
    printOpen("Extracting litmid literals", EchoLevel.INFO)
    def adaptedLitmidResponse = sendLitmidRequest(artifactName, typeApp)
    mapResult = adaptedLitmidResponse[LitmidLiteralType.APPLICATION.literalType()]

    sendStageEndToGPL(clientInfo, pipelineData,  "200")
}

/**
 * Stage 'transformLitmidLiteralsStep'
 */
def transformLitmidLiteralsStep() {
    sendStageStartToGPL(clientInfo, pipelineData, "300")

    printOpen("Transforming litmid literals", EchoLevel.INFO)
    mapResult = litmidApplicationLiteralsManagement(mapResult, limitParamsMap)

    sendStageEndToGPL(clientInfo, pipelineData, "300")
}

/**
 * Stage 'loadLitmidLiteralsStep'
 */
def loadLitmidLiteralsStep() {
    sendStageStartToGPL(clientInfo, pipelineData, "400")

    printOpen("Uploading litmid literals", EchoLevel.INFO)
    def configRepoUrlAndBranch = GitUtils.getConfigRepoUrlAndBranch(environmentConfigServer)
    printOpen("Url: $configRepoUrlAndBranch.url\nBranch: $configRepoUrlAndBranch.branch\nCheckout: $checkoutDirConfigServer", EchoLevel.INFO)

    GitRepositoryHandler git =  new GitRepositoryHandler(this,  configRepoUrlAndBranch.url, [gitProjectRelativePath: checkoutDirConfigServer, checkoutBranch: configRepoUrlAndBranch.branch])
    try {
        git.lockRepoAndDo({
            git.clearOutGitProjectRelativePath().pullOrClone()

            def microOrMicroServer = (artifactType == "SIMPLE" ? "" : "server-")
            def componentName = "$artifactName-$microOrMicroServer$majorVersion"

            litmidApplicationLiteralsManagement.updateYamlFile("./$checkoutDirConfigServer/services/apps/$componentName/application-litmid.yml", mapResult)

            git.add()
            git.commitAndPush("Automatic uploaded LitmidFile")
        })
    } catch (err) {
        printOpen("We got an exception (pushConfigFiles)!\n\n${Utilities.prettyException(err)}", EchoLevel.ERROR)
        throw err
    } finally {
        git.purge()

    }

    sendStageEndToGPL(clientInfo, pipelineData, "400")
}

/**
 * Stage 'refreshMicroLitmidLiterals'
 */
def refreshMicroLitmidLiterals() {
    sendStageStartToGPL(clientInfo, pipelineData, "500")

    printOpen("Refreshing Component: $typeApp.$artifactName-$majorVersion, dataCenter: 1, color: *", EchoLevel.INFO)
    refreshConfigurationViaRefreshBus("1", typeApp, artifactName, majorVersion, "*", environmentConfigServer)
    printOpen("Refreshing Component: $typeApp.$artifactName-$majorVersion, dataCenter: 2, color: *", EchoLevel.INFO)
    refreshConfigurationViaRefreshBus("2", typeApp, artifactName, majorVersion, "*", environmentConfigServer)

    sendStageEndToGPL(clientInfo, pipelineData, "500")
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("Se success el pipeline ${successPipeline}", EchoLevel.INFO)
    sendPipelineResultadoToGPL(initGpl, clientInfo, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, clientInfo, pipelineData, successPipeline)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("Se failure el pipeline ${successPipeline}", EchoLevel.ERROR)
    sendPipelineResultadoToGPL(initGpl, clientInfo, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, clientInfo, pipelineData, successPipeline)
}
