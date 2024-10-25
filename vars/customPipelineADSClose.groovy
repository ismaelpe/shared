import groovy.transform.Field
import com.project.alm.*
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams

@Field boolean initGpl
@Field boolean successPipeline

@Field ADSPipelineData adsPipelineData
@Field ClientInfo clientADSInfo
@Field BranchStructure branchStructure

//Job parameters
@Field String xmlFile
@Field String transactionId
@Field String artifactId
@Field String artifactGroupId
@Field String artifactVersion

@Field String nextEnvironment
@Field String gitUrl

@Field String originBranch
@Field String pipelineOrigId
@Field String userEmail
@Field String user

@Field String loggerLevel

@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    initGpl = false
    successPipeline = true

    //Job parameters
    xmlFile = params.xmlFile
    transactionId = params.transactionId
    artifactId = params.artifactId
    artifactGroupId = params.artifactGroupId
    artifactVersion = params.artifactVersion

    nextEnvironment = params.nextEnvironment
    gitUrl = params.gitUrl}

    originBranch = params.originBranchParam
    pipelineOrigId = params.pipelineOrigId
    userEmail = params.userEmail
    user = params.user

    loggerLevel = params.loggerLevel
	
	almEvent = null
	initCallStartMillis = new Date().getTime()
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        //Environment sobre el cual se ejecuta este tipo de job
        options { 
			gitLabConnection('gitlab')
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
			MAVEN_OPTS = " "
            GIT_SSL_NO_VERIFY = 'true'
        }
        stages {
            stage('init-pipeline') {
                steps {
                    initPipelineStep()
                }
            }
            stage('close-release') {
                steps {
                    closeReleaseStep()
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
    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null
    printOpen("BUILD_TAG: ${env.BUILD_TAG}", EchoLevel.ALL)
    adsPipelineData = new ADSPipelineData(nextEnvironment, "${env.BUILD_TAG}", params)
    adsPipelineData.initFromADSProvisioning(originBranch, gitUrl)
    printOpen("GIT URL: ", EchoLevel.ALL)
    adsPipelineData.setGitUrl(gitUrl)
    printOpen("GIT URL: ", EchoLevel.ALL)
    branchStructure = getBranchInfo(originBranch)
    currentBuild.displayName = "Build_${env.BUILD_ID}_" + adsPipelineData.getPipelineBuildName() + "_" + transactionId + "_" + nextEnvironment
    adsPipelineData.pushUser = user

    adsPipelineData.setBuildCode(artifactVersion)

    clientADSInfo = new ClientInfo()
    clientADSInfo.setApplicationName(ADSVars.GPL_APPLICATION_NAME)
    clientADSInfo.setArtifactId(artifactId)
    clientADSInfo.setArtifactVersion(artifactVersion)
    clientADSInfo.setArtifactType(ArtifactType.valueOfType(ADSVars.APP_TYPE))
    clientADSInfo.setArtifactSubType(ArtifactSubType.valueOfSubType(ADSVars.APP_SUBTYPE))
    clientADSInfo.setGroupId(artifactGroupId)
    
    almEvent = new KpiAlmEvent(
        clientADSInfo, adsPipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_ADS_CLOSE)
    
    sendPipelineStartToGPL(clientADSInfo, adsPipelineData, pipelineOrigId)
    initGpl = true
}

/**
 * Stage 'closeReleaseStep'
 */
def closeReleaseStep() {
    sendStageStartToGPL(clientADSInfo, adsPipelineData, "200")
    printOpen("---------------------------", EchoLevel.ALL)
    printOpen("Clone git repo $gitUrl", EchoLevel.ALL)

    GitRepositoryHandler git = new GitRepositoryHandler(this, gitUrl)

    try {

        git.lockRepoAndDo({

            git.pullOrClone()

            def branchToMerge = adsPipelineData.getBranchStructure().branchName

            git.checkout(branchToMerge).pull().checkout('master', [force: true]).pull()

            printOpen("Merge branch $branchToMerge into master", EchoLevel.ALL)
            git.cmdExecutor(["git merge -Xours $branchToMerge"])

            printOpen("Push changes to master", EchoLevel.ALL)
            git.commitAndPush().deleteBranchOnOrigin(branchToMerge)

        })

    } catch (err) {

        echo Utilities.prettyException(err)
        throw err

    } finally {

        git.purge()
        sendStageEndToGPL(clientADSInfo, adsPipelineData, "200")

    }
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen("Pipeline has succeeded", EchoLevel.INFO) 
    successPipeline = true 
    if ( almEvent!=null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis-initCallStartMillis))
    }

    sendPipelineEndedToGPL(initGpl, clientADSInfo, adsPipelineData, successPipeline)
    sendPipelineResultadoToGPL(initGpl, clientADSInfo, adsPipelineData, successPipeline)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("Pipeline has failed", EchoLevel.ERROR)
    if ( almEvent!=null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis-initCallStartMillis))						
    }    
    sendPipelineEndedToGPL(initGpl, clientADSInfo, adsPipelineData, successPipeline)
    sendPipelineResultadoToGPL(initGpl, clientADSInfo, adsPipelineData, successPipeline)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(clientADSInfo, adsPipelineData)
    cleanWorkspace()
}
