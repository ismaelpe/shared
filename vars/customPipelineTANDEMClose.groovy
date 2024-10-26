import groovy.transform.Field
import com.project.alm.*
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams

@Field boolean initAppPortal
@Field boolean successPipeline

@Field TANDEMPipelineData tandemPipelineData
@Field ClientInfo clientTANDEMInfo
@Field BranchStructure branchStructure

//Job parameters
@Field String xmlFile
@Field String transactionId
@Field String artifactId
@Field String artifactGroupId
@Field String artifactVersion

@Field String gitUrl

@Field String originBranch
@Field String pipelineOrigId
@Field String user

@Field String loggerLevel

@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    initAppPortal = false
    successPipeline = true

    //Job parameters
    xmlFile = params.xmlFile
    transactionId = params.transactionId
    artifactId = params.artifactId
    artifactGroupId = params.artifactGroupId
    artifactVersion = params.artifactVersion

    gitUrl = params.gitUrl

    originBranch = params.originBranchParam
    pipelineOrigId = params.pipelineOrigId
    user = params.userId

    loggerLevel = params.loggerLevel
	
	almEvent = null
	initCallStartMillis = new Date().getTime()
    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
        //Environment sobre el cual se ejecuta este tipo de job
        options { 
			gitLabConnection('gitlab')
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
			MAVEN_OPTS = " "
            GIT_SSL_NO_VERIFY = 'true'
			sendLogsToAppPortal = true
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
    tandemPipelineData = new TANDEMClosePipelineData("${env.BUILD_TAG}", gitUrl, user, artifactVersion, params)
	tandemPipelineData.init(originBranch)
    currentBuild.displayName = "Build_${env.BUILD_ID}_" + tandemPipelineData.getPipelineBuildName() + "_" + transactionId + "_CLOSE"

    clientTANDEMInfo = new ClientInfo()
    clientTANDEMInfo.setApplicationName('tandemtransaction')
    clientTANDEMInfo.setArtifactId(artifactId)
    clientTANDEMInfo.setArtifactVersion(artifactVersion)
    clientTANDEMInfo.setArtifactType(ArtifactType.SIMPLE)
    clientTANDEMInfo.setArtifactSubType(ArtifactSubType.ARCH_LIB)
    clientTANDEMInfo.setGroupId(artifactGroupId)
    
    almEvent = new KpiAlmEvent(
        clientTANDEMInfo, tandemPipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_TANDEM_CLOSE)
    
    sendPipelineStartToAppPortal(clientTANDEMInfo, tandemPipelineData, pipelineOrigId)
    initAppPortal = true
}

/**
 * Stage 'closeReleaseStep'
 */
def closeReleaseStep() {
    sendStageStartToAppPortal(clientTANDEMInfo, tandemPipelineData, "200")
    printOpen("---------------------------", EchoLevel.ALL)
    printOpen("Clone git repo $gitUrl", EchoLevel.ALL)

    GitRepositoryHandler git = new GitRepositoryHandler(this, gitUrl)
	try {
        git.lockRepoAndDo({

            git.pullOrClone()

            def branchToMerge = tandemPipelineData.getBranchStructure().branchName

            git.checkout(branchToMerge).pull().checkout('master', [force: true]).pull()

            printOpen("Merge branch $branchToMerge into master", EchoLevel.ALL)
            git.cmdExecutor(["git merge -Xours $branchToMerge"])

            printOpen("Push changes to master", EchoLevel.ALL)
            git.commitAndPush().deleteBranchOnOrigin(branchToMerge)

        })
        sendStageEndToAppPortal(clientTANDEMInfo, tandemPipelineData, "200")
    } catch (err) {
		printOpen(err.getMessage(), EchoLevel.ERROR)
		sendStageEndToAppPortal(clientTANDEMInfo, tandemPipelineData, '200', null, null, "error")
		throw err
    } finally {
        git.purge()
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

    sendPipelineEndedToAppPortal(initAppPortal, clientTANDEMInfo, tandemPipelineData, successPipeline)
    sendPipelineResultadoToAppPortal(initAppPortal, clientTANDEMInfo, tandemPipelineData, successPipeline)
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
    sendPipelineEndedToAppPortal(initAppPortal, clientTANDEMInfo, tandemPipelineData, successPipeline)
    sendPipelineResultadoToAppPortal(initAppPortal, clientTANDEMInfo, tandemPipelineData, successPipeline)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(clientTANDEMInfo, tandemPipelineData)
    cleanWorkspace()
}
