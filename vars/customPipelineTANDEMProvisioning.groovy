import groovy.transform.Field
import com.project.alm.*
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams

@Field NexusUtils nexus

@Field boolean initAppPortal
@Field boolean successPipeline

@Field TANDEMPipelineData tandemPipelineData
@Field ClientInfo tandemClientInfo
@Field BranchStructure branchStructure
@Field PomXmlStructure pomXmlStructure
@Field String repo
@Field String repoAnterior

@Field String xmlFile
@Field String transactionId
@Field String artifactId
@Field String artifactGroupId
@Field String artifactVersion

@Field Environment nextEnvironment
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
    
    nexus = new NexusUtils(this)

    initAppPortal = false
    successPipeline = true

    //Job parameters
    xmlFile = params.xmlFile
    transactionId = params.transactionId
    artifactId = params.artifactId
    artifactGroupId = params.artifactGroupId
    artifactVersion = params.artifactVersion

    nextEnvironment = Environment.valueOf(params.nextEnvironment)
    gitUrl = params.gitUrl

    originBranch = params.originBranchParam
    pipelineOrigId = params.pipelineOrigId
    user = params.userId

    loggerLevel = params.loggerLevel

    almEvent = null
    initCallStartMillis = new Date().getTime()

    pipeline {
        agent { node(almJenkinsAgent(pipelineParams)) }
        options {
            gitLabConnection('gitlab')
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
            MAVEN_OPTS = ' '
			sendLogsToAppPortal = true
        }
        stages {
            stage('Initiate provisioning') {
                steps {
                    initiateProvisioningStep()
                }
            }
            stage('Download tandem artifact and Update Version') {
                steps {
                    downloadTandemArtifactAndUpdateVersionStep()
                }
            }
            stage('Push to config server') {
                when {
                    expression { Environment.TST != nextEnvironment }
                }
                steps {
                    pushToConfigServerStep()
                }
            }
            stage('Deploy new version TANDEM lib transaction') {
                when {
                    expression { Environment.PRO != nextEnvironment }
                }
                steps {
                    deployNewVersionTANDEMLibTransactionStep()
                }
            }
            stage('Publish artifact') {
                steps {
                    publishArtifactStep()
                }
            }
            stage('refresh-connector-configuration') {
                when {
                    expression { Environment.TST != nextEnvironment }
                }
                steps {
                    refreshConnectorConfigurationStep()
                }
            }
            stage('Prepare next steps') {
                steps {
                    prepareNextSteps()
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
 * Stage initiateProvisioningStep
 */
def initiateProvisioningStep() {
    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null
    printOpen("BUILD_TAG: ${env.BUILD_TAG}", EchoLevel.ALL)
	printOpen("ENV: ${nextEnvironment}", EchoLevel.ALL)
    tandemPipelineData = new TANDEMPipelineData(nextEnvironment, "${env.BUILD_TAG}", params)
    tandemPipelineData.initFromTANDEMProvisioning(originBranch, gitUrl)
	tandemPipelineData.pushUser = user
    printOpen("GIT URL: $gitUrl", EchoLevel.INFO)
    tandemPipelineData.setGitUrl(gitUrl)
    branchStructure = getBranchInfo(originBranch)
    printOpen("${branchStructure}", EchoLevel.ALL)
    pomXmlStructure = new PomXmlStructure()
    pomXmlStructure.setArtifactName(artifactId)
    printOpen("${pomXmlStructure.setArtifactName(artifactId)}", EchoLevel.INFO)
    currentBuild.displayName = "Build_${env.BUILD_ID}_" + tandemPipelineData.getPipelineBuildName() + '_' + transactionId + '_' + nextEnvironment
    
	tandemClientInfo = new ClientInfo()
    tandemClientInfo.setApplicationName("tandemtransaction")
    tandemClientInfo.setArtifactId(artifactId)
    tandemClientInfo.setArtifactVersion(artifactVersion)
    tandemClientInfo.setArtifactType(ArtifactType.SIMPLE)
    tandemClientInfo.setArtifactSubType(ArtifactSubType.ARCH_LIB)
    tandemClientInfo.setGroupId(artifactGroupId)
    
    sendPipelineStartToAppPortal(tandemClientInfo, tandemPipelineData, pipelineOrigId)
    initAppPortal = true
}

/**
 * Stage downloadTandemArtifactAndUpdateVersionStep
 */
def downloadTandemArtifactAndUpdateVersionStep() {
	sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '020')
	try {
	    printOpen("Downloading starter for transaction: ${transactionId} . artifactGroupId: ${artifactGroupId} artifactId: ${artifactId} artifactVersion ${artifactVersion}. Deploying in ${nextEnvironment}", EchoLevel.INFO)
	    getArtifactFromNexusWithGAVs(artifactGroupId, artifactId, artifactVersion, '.')
	
	    if (Environment.PRO != nextEnvironment) {
	        //feature version
	        def branchVersion = artifactVersion
	        def branchVersionWq = MavenVersionUtilities.getArtifactVersionWithoutQualifier(branchVersion)
	        printOpen("Branch version : $branchVersion", EchoLevel.INFO)
	        printOpen("Branch version  w/ qualifier: $branchVersionWq", EchoLevel.ALL)
	
	        //last release
	        def lastReleaseArtifactVersion = nexus.getLastVersionNumber(artifactGroupId, artifactId, null, GlobalVars.NEXUS_RELEASES_REPO_NAME, BuildType.FINAL)
	        def lastReleaseArtifactVersionWq = MavenVersionUtilities.getArtifactVersionWithoutQualifier(lastReleaseArtifactVersion)
	
	        printOpen("tandem-transaction-lib last release version : $lastReleaseArtifactVersion", EchoLevel.INFO)
	        printOpen("tandem-transaction-lib last release version w/ qualifier : $lastReleaseArtifactVersionWq", EchoLevel.ALL)
	
	        if (Environment.TST == nextEnvironment) {
	            if (lastReleaseArtifactVersionWq.compareTo(branchVersionWq) == 0) {
	                if (MavenVersionUtilities.isRCVersion(lastReleaseArtifactVersion)) {
	                    pomXmlStructure.setArtifactVersion(lastReleaseArtifactVersion)
	                    pomXmlStructure.incRC()
	                } else {
	                    throw new Exception("Release ${lastReleaseArtifactVersion} already exists in artifactory")
	                }
	            } else {
	                pomXmlStructure.setArtifactVersion(branchVersionWq + '-RC1')
	            }
	            artifactVersion = pomXmlStructure.getArtifactVersion()
	            tandemPipelineData.setBuildCode(pomXmlStructure.getArtifactVersionQualifier())
	        } else if (Environment.PRE == nextEnvironment) {
	            if (lastReleaseArtifactVersionWq.compareTo(branchVersionWq) == 0) {
	                if (MavenVersionUtilities.isRCVersion(lastReleaseArtifactVersion)) {
	                    pomXmlStructure.setArtifactVersion(branchVersionWq)
	                } else {
	                    throw new Exception("Release ${lastReleaseArtifactVersion} already exists in artifactory")
	                }
	            } else {
	                pomXmlStructure.setArtifactVersion(branchVersionWq)
	            }
	            artifactVersion = pomXmlStructure.getArtifactVersion()
	            tandemPipelineData.setBuildCode(artifactVersion)
	        } else {
	            throw new Exception('Invalid job for artifact version')
	        }
	        sh "mv ${artifactId}-${branchVersion}.jar ${artifactId}-${artifactVersion}.jar"
	    } else {
	        tandemPipelineData.setBuildCode(artifactVersion)
	    }
	
	    tandemClientInfo.setArtifactVersion(artifactVersion)
	
	    almEvent = new KpiAlmEvent(
	        tandemClientInfo, tandemPipelineData,
	        KpiAlmEventStage.GENERAL,
	        KpiAlmEventOperation.PIPELINE_TANDEM_PROVISIONING)
	
	    printOpen("tandemClientInfo = ${tandemClientInfo.toString()}", EchoLevel.INFO)
	    sendPipelineUpdateToAppPortal(true,tandemClientInfo, tandemPipelineData)
	    sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '020')
	} catch (Exception e) {
		printOpen(e.getMessage(), EchoLevel.ERROR)
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '020', null, null, "error")
		throw e
	}
}

/**
 * Stage pushToConfigServerStep
 */
def pushToConfigServerStep() {
    sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '050')

    if (Environment.TST == nextEnvironment) {
        repo = GlobalVars.GIT_CONFIG_REPO_URL_TST
        repoAnterior = GlobalVars.GIT_CONFIG_REPO_URL_TST
    } else if (Environment.PRE == nextEnvironment) {
        repo = GlobalVars.GIT_CONFIG_REPO_URL_PRE
        repoAnterior = GlobalVars.GIT_CONFIG_REPO_URL_TST
    } else if (Environment.PRO == nextEnvironment) {
        repo = GlobalVars.GIT_CONFIG_REPO_URL_PRO
        repoAnterior = GlobalVars.GIT_CONFIG_REPO_URL_PRE
    }

    FileUtils fileUtils = new FileUtils(this)
    GitRepositoryHandler gitActual = new GitRepositoryHandler(this, repo)
    GitRepositoryHandler gitAnterior = new GitRepositoryHandler(this, repoAnterior)
    
	try {
	    String sanitizedTandemConfPath = FileUtils.sanitizePath(gitActual.gitProjectRelativePath)
	    String sanitizedTandemConfPathAnterior = FileUtils.sanitizePath(gitAnterior.gitProjectRelativePath)
	
	    def arqDestFolder = "${sanitizedTandemConfPath}/services/tandem/tandem-app-transactions"
	    def arqOriginFolder = "${sanitizedTandemConfPathAnterior}/services/tandem/tandem-app-transactions"
	
	    printOpen("Pushing XML file to configServer in ${repo}", EchoLevel.INFO)

        gitActual.lockRepoAndDo({
            gitActual.pullOrClone([depth: 1])
            gitAnterior.pullOrClone([depth: 1])

            int lastSlash = xmlFile.lastIndexOf('/')
            def xmlFileName = xmlFile.substring(lastSlash + 1)

            fileUtils.createPathIfNotExists(arqDestFolder)
            fileUtils.copyFiles(arqOriginFolder + '/' + xmlFileName, arqDestFolder, false)

            gitActual.add('services/tandem/tandem-app-transactions').commitAndPush("Pushing ${xmlFile}")
        })
        sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '050')
    } catch (err) {
        printOpen(err.getMessage(), EchoLevel.ERROR)
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '050', null, null, "error")
        throw err
    } finally {
        gitActual.purge()
        gitAnterior.purge()
    }
}

/**
 * Stage deployNewVersionTANDEMLibTransactionStep
 */
def deployNewVersionTANDEMLibTransactionStep() {
    sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '060')
	try {
	    printOpen("Deploying new JAR to Artifactory...")
	    deployFileNexus(artifactGroupId, artifactId, artifactVersion, 'jar', "${artifactId}-${artifactVersion}.jar")
		printOpen("The new JAR has been deployed")
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '060')
	} catch (Exception e) {
		printOpen(e.getMessage())
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '060', null, null, "error")
		throw e
	}
}

/**
 * Stage publishArtifactStep
 */
def publishArtifactStep() {
    sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '070')
	try {
	    publishArtifactClientTANDEMInCatalog(tandemPipelineData, tandemClientInfo)
	    sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '070')
	} catch (Exception e) {
		printOpen(e.getMessage())
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '070', null, null, "error")
		throw e
	}
}

/**
 * Stage refreshConnectorConfigurationStep
 */
def refreshConnectorConfigurationStep() {
    sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '470')
	try {
		printOpen("Refreshing tandemconnector-micro...")
	    if (Environment.TST == nextEnvironment) {
	        refreshConfigurationViaRefreshBus('1', 'ARQ.MIA', 'tandemconnector', '1', '*', Environment.DEV.name())
	        refreshConfigurationViaRefreshBus('2', 'ARQ.MIA', 'tandemconnector', '1', '*', Environment.DEV.name())
	        refreshConfigurationViaRefreshBus('1', 'ARQ.MIA', 'tandemconnector', '1', '*', Environment.TST.name())
	        refreshConfigurationViaRefreshBus('2', 'ARQ.MIA', 'tandemconnector', '1', '*', Environment.TST.name())
	    } else if (Environment.PRE == nextEnvironment) {
	        refreshConfigurationViaRefreshBus('1', 'ARQ.MIA', 'tandemconnector', '1', '*', Environment.PRE.name())
	        refreshConfigurationViaRefreshBus('2', 'ARQ.MIA', 'tandemconnector', '1', '*', Environment.PRE.name())
	    } else if (Environment.PRO == nextEnvironment) {
	        refreshConfigurationViaRefreshBus('1', 'ARQ.MIA', 'tandemconnector', '1', '*', Environment.PRO.name())
	        refreshConfigurationViaRefreshBus('2', 'ARQ.MIA', 'tandemconnector', '1', '*', Environment.PRO.name())
	    }
		printOpen("Tandemconnector-micro has been refreshed")
	    sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '470')
	} catch (Exception e) {
		printOpen(e.getMessage())
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '470', null, null, "error")
		throw e
	}
}

/**
 * Stage prepareNextSteps
 */
def prepareNextSteps() {
    sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '080')
	try {
	    tandemPipelineData.pipelineStructure.resultPipelineData.xmlFile = xmlFile
	    tandemPipelineData.pipelineStructure.resultPipelineData.artifactId = artifactId
	    tandemPipelineData.pipelineStructure.resultPipelineData.artifactGroupId = artifactGroupId
	    tandemPipelineData.pipelineStructure.resultPipelineData.artifactVersion = artifactVersion
	
	    if (Environment.TST == tandemPipelineData.environment) {
	        tandemPipelineData.pipelineStructure.resultPipelineData.nextEnvironment = Environment.PRE.name()
	    } else if (Environment.PRE == tandemPipelineData.environment) {
	        tandemPipelineData.pipelineStructure.resultPipelineData.nextEnvironment = Environment.PRO.name()
	    }
	
	    tandemPipelineData.pipelineStructure.resultPipelineData.transactionId = transactionId
	
	    tandemPipelineData.pipelineStructure.resultPipelineData.originBranch = originBranch
	
	    tandemPipelineData.pipelineStructure.resultPipelineData.pipelineOrigId = tandemPipelineData.pipelineStructure.pipelineId
	    sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '080')
	} catch (Exception e) {
		printOpen(e.getMessage())
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '080', null, null, "error")
		throw e
	}
}

/**
 * Stage endPipelineSuccessStep
 */
def endPipelineSuccessStep() {
    printOpen('Pipeline has succeeded', EchoLevel.INFO)
    successPipeline = true
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis - initCallStartMillis))
    }

    sendPipelineResultadoToAppPortal(initAppPortal, tandemClientInfo, tandemPipelineData, successPipeline)
    sendPipelineEndedToAppPortal(initAppPortal, tandemClientInfo, tandemPipelineData, successPipeline)
}

/**
 * Stage endPipelineFailureStep
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen('Pipeline has failed', EchoLevel.ERROR)
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis - initCallStartMillis))
    }

    sendPipelineResultadoToAppPortal(initAppPortal, tandemClientInfo, tandemPipelineData, successPipeline)
    sendPipelineEndedToAppPortal(initAppPortal, tandemClientInfo, tandemPipelineData, successPipeline)
}

/**
 * Stage endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure, tandemPipelineData)
    cleanWorkspace()
}
