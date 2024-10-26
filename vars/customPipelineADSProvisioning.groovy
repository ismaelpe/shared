import groovy.transform.Field
import com.project.alm.*
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams

@Field NexusUtils nexus

@Field boolean initGpl
@Field boolean successPipeline

@Field ADSPipelineData adsPipelineData
@Field ClientInfo adsClientInfo
@Field BranchStructure branchStructure
@Field PomXmlStructure pomXmlStructure
@Field String repo
@Field String repoAnterior

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
    
    nexus = new NexusUtils(this)

    initGpl = false
    successPipeline = true

    //Job parameters
    xmlFile = params.xmlFile
    transactionId = params.transactionId
    artifactId = params.artifactId
    artifactGroupId = params.artifactGroupId
    artifactVersion = params.artifactVersion

    nextEnvironment = params.nextEnvironment
    gitUrl = params.gitUrl

    originBranch = params.originBranchParam
    pipelineOrigId = params.pipelineOrigId
    userEmail = params.userEmail
    user = params.user

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
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            JNKMSV = credentials('JNKMSV-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
            https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
            MAVEN_OPTS = ' '
        }
        stages {
            stage('Initiate provisioning') {
                steps {
                    initiateProvisioningStep()
                }
            }
            stage('Download ads artifact and Update Version') {
                steps {
                    downloadAdsArtifactAndUpdateVersionStep()
                }
            }
            stage('Push to config server') {
                when {
                    expression { ADSVars.TST_ENVIRONMENT != nextEnvironment }
                }
                steps {
                    pushToConfigServerStep()
                }
            }
            stage('Deploy new version ADS lib transaction') {
                when {
                    expression { ADSVars.PRO_ENVIRONMENT != nextEnvironment }
                }
                steps {
                    deployNewVersionADSLibTransactionStep()
                }
            }
            stage('Publish artifact') {
                steps {
                    publishArtifactStep()
                }
            }
            stage('refresh-connector-configuration') {
                when {
                    expression { ADSVars.TST_ENVIRONMENT != nextEnvironment }
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
    adsPipelineData = new ADSPipelineData(nextEnvironment, "${env.BUILD_TAG}", params)
    adsPipelineData.initFromADSProvisioning(originBranch, gitUrl)
    printOpen("GIT URL: $gitUrl", EchoLevel.ALL)
    adsPipelineData.setGitUrl(gitUrl)
    branchStructure = getBranchInfo(originBranch)
    printOpen("${branchStructure}", EchoLevel.ALL)
    pomXmlStructure = new PomXmlStructure()
    pomXmlStructure.setArtifactName(artifactId)
    printOpen("${pomXmlStructure.setArtifactName(artifactId)}", EchoLevel.ALL)
    currentBuild.displayName = "Build_${env.BUILD_ID}_" + adsPipelineData.getPipelineBuildName() + '_' + transactionId + '_' + nextEnvironment
}

/**
 * Stage downloadAdsArtifactAndUpdateVersionStep
 */
def downloadAdsArtifactAndUpdateVersionStep() {
    printOpen("Downloading starter for transaction: ${transactionId} . artifactGroupId: ${artifactGroupId} artifactId: ${artifactId} artifactVersion ${artifactVersion}. Deploying in ${nextEnvironment}", EchoLevel.ALL)
    getArtifactFromNexusWithGAVs(artifactGroupId, artifactId, artifactVersion, '.')

    if (ADSVars.PRO_ENVIRONMENT != nextEnvironment) {
        //feature version
        def branchVersion = artifactVersion
        def branchVersionWq = MavenVersionUtilities.getArtifactVersionWithoutQualifier(branchVersion)
        printOpen("Branch version : $branchVersion", EchoLevel.ALL)
        printOpen("Branch version  w/ qualifier: $branchVersionWq", EchoLevel.ALL)

        //last release
        def lastReleaseArtifactVersion = nexus.getLastVersionNumber(artifactGroupId, artifactId, null, GlobalVars.NEXUS_RELEASES_REPO_NAME, BuildType.FINAL)
        def lastReleaseArtifactVersionWq = MavenVersionUtilities.getArtifactVersionWithoutQualifier(lastReleaseArtifactVersion)

        printOpen("ads-transaction-lib last release version : $lastReleaseArtifactVersion", EchoLevel.ALL)
        printOpen("ads-transaction-lib last release version w/ qualifier : $lastReleaseArtifactVersionWq", EchoLevel.ALL)

        if (ADSVars.TST_ENVIRONMENT == nextEnvironment) {
            if (lastReleaseArtifactVersionWq.compareTo(branchVersionWq) == 0) {
                if (MavenVersionUtilities.isRCVersion(lastReleaseArtifactVersion)) {
                    pomXmlStructure.setArtifactVersion(lastReleaseArtifactVersion)
                    pomXmlStructure.incRC()
                } else {
                    printOpen('There is a release version', EchoLevel.ALL)
                    throw new RuntimeException('There is a release version')
                }
            } else {
                pomXmlStructure.setArtifactVersion(branchVersionWq + '-RC1')
            }
            artifactVersion = pomXmlStructure.getArtifactVersion()
            adsPipelineData.setBuildCode(pomXmlStructure.getArtifactVersionQualifier())
        } else if (ADSVars.PRE_ENVIRONMENT == nextEnvironment) {
            if (lastReleaseArtifactVersionWq.compareTo(branchVersionWq) == 0) {
                if (MavenVersionUtilities.isRCVersion(lastReleaseArtifactVersion)) {
                    pomXmlStructure.setArtifactVersion(branchVersionWq)
                } else {
                    printOpen('There is a release version', EchoLevel.ALL)
                    throw new RuntimeException('There is a release version')
                }
            } else {
                pomXmlStructure.setArtifactVersion(branchVersionWq)
            }
            artifactVersion = pomXmlStructure.getArtifactVersion()
            adsPipelineData.setBuildCode(artifactVersion)
        } else {
            printOpen('Invalid job for artifact version', EchoLevel.ALL)
            throw new Exception('Invalid job for artifact version')
        }
        sh "mv ${artifactId}-${branchVersion}.jar ${artifactId}-${artifactVersion}.jar"
    } else {
        adsPipelineData.setBuildCode(artifactVersion)
    }

    printOpen("artifactVersion: ${artifactVersion}", EchoLevel.ALL)
    adsClientInfo = new ClientInfo()
    adsClientInfo.setApplicationName(ADSVars.GPL_APPLICATION_NAME)
    adsClientInfo.setArtifactId(artifactId)
    adsClientInfo.setArtifactVersion(artifactVersion)
    adsClientInfo.setArtifactType(ArtifactType.valueOfType(ADSVars.APP_TYPE))
    adsClientInfo.setArtifactSubType(ArtifactSubType.valueOfSubType(ADSVars.APP_SUBTYPE))
    adsClientInfo.setGroupId(artifactGroupId)

    almEvent = new KpiAlmEvent(
        adsClientInfo, adsPipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_ADS_PROVISIONING)

    printOpen("adsClientInfo = ${adsClientInfo.toString()}", EchoLevel.ALL)
    sendPipelineStartToGPL(adsClientInfo, adsPipelineData, pipelineOrigId)
    initGpl = true
    sendStageStartToGPL(adsClientInfo, adsPipelineData, '020')

    sendStageEndToGPL(adsClientInfo, adsPipelineData, '020')
}

/**
 * Stage pushToConfigServerStep
 */
def pushToConfigServerStep() {
    sendStageStartToGPL(adsClientInfo, adsPipelineData, '050')

    if (ADSVars.TST_ENVIRONMENT == nextEnvironment) {
        repo = GlobalVars.GIT_CONFIG_REPO_URL_TST
        repoAnterior = GlobalVars.GIT_CONFIG_REPO_URL_TST
    } else if (ADSVars.PRE_ENVIRONMENT == nextEnvironment) {
        repo = GlobalVars.GIT_CONFIG_REPO_URL_PRE
        repoAnterior = GlobalVars.GIT_CONFIG_REPO_URL_TST
    } else if (ADSVars.PRO_ENVIRONMENT == nextEnvironment) {
        repo = GlobalVars.GIT_CONFIG_REPO_URL_PRO
        repoAnterior = GlobalVars.GIT_CONFIG_REPO_URL_PRE
    }

    FileUtils fileUtils = new FileUtils(this)
    GitRepositoryHandler gitActual = new GitRepositoryHandler(this, repo)
    GitRepositoryHandler gitAnterior = new GitRepositoryHandler(this, repoAnterior)

    String sanitizedAdsConfPath = FileUtils.sanitizePath(gitActual.gitProjectRelativePath)
    String sanitizedAdsConfPathAnterior = FileUtils.sanitizePath(gitAnterior.gitProjectRelativePath)

    def arqDestFolder = "${sanitizedAdsConfPath}/services/ads/ads-app-transactions"
    def arqOriginFolder = "${sanitizedAdsConfPathAnterior}/services/ads/ads-app-transactions"

    printOpen("Pushing XML file to configServer in ${repo}", EchoLevel.ALL)

    try {
        gitActual.lockRepoAndDo({
            gitActual.pullOrClone([depth: 1])
            gitAnterior.pullOrClone([depth: 1])

            int lastSlash = xmlFile.lastIndexOf('/')
            def xmlFileName = xmlFile.substring(lastSlash + 1)

            fileUtils.createPathIfNotExists(arqDestFolder)
            fileUtils.copyFiles(arqOriginFolder + '/' + xmlFileName, arqDestFolder, false)

            gitActual.add('services/ads/ads-app-transactions').commitAndPush("Pushing ${xmlFile}")
        })
    } catch (err) {
        echo Utilities.prettyException(err)
        throw err
    } finally {
        gitActual.purge()
        gitAnterior.purge()
        sendStageEndToGPL(adsClientInfo, adsPipelineData, '050')
    }
}

/**
 * Stage deployNewVersionADSLibTransactionStep
 */
def deployNewVersionADSLibTransactionStep() {
    sendStageStartToGPL(adsClientInfo, adsPipelineData, '060')
    deployFileNexus(artifactGroupId, artifactId, artifactVersion, 'jar', "${artifactId}-${artifactVersion}.jar")
    sendStageEndToGPL(adsClientInfo, adsPipelineData, '060')
}

/**
 * Stage publishArtifactStep
 */
def publishArtifactStep() {
    sendStageStartToGPL(adsClientInfo, adsPipelineData, '070')
    publishArtifactClientADSInCatalog(adsPipelineData, adsClientInfo)
    sendStageEndToGPL(adsClientInfo, adsPipelineData, '070')
}

/**
 * Stage refreshConnectorConfigurationStep
 */
def refreshConnectorConfigurationStep() {
    sendStageStartToGPL(adsClientInfo, adsPipelineData, '470')

    if (ADSVars.TST_ENVIRONMENT == nextEnvironment) {
        refreshConfigurationViaRefreshBus('1', 'ARQ.MIA', 'adsconnector', '1', '*', ADSVars.DEV_ENVIROMENT)
        refreshConfigurationViaRefreshBus('2', 'ARQ.MIA', 'adsconnector', '1', '*', ADSVars.DEV_ENVIROMENT)
        refreshConfigurationViaRefreshBus('1', 'ARQ.MIA', 'adsconnector', '1', '*', ADSVars.TST_ENVIRONMENT)
        refreshConfigurationViaRefreshBus('2', 'ARQ.MIA', 'adsconnector', '1', '*', ADSVars.TST_ENVIRONMENT)
    } else if (ADSVars.PRE_ENVIRONMENT == nextEnvironment) {
        refreshConfigurationViaRefreshBus('1', 'ARQ.MIA', 'adsconnector', '1', '*', ADSVars.PRE_ENVIRONMENT)
        refreshConfigurationViaRefreshBus('2', 'ARQ.MIA', 'adsconnector', '1', '*', ADSVars.PRE_ENVIRONMENT)
    } else if (ADSVars.PRO_ENVIRONMENT == nextEnvironment) {
        refreshConfigurationViaRefreshBus('1', 'ARQ.MIA', 'adsconnector', '1', '*', ADSVars.PRO_ENVIRONMENT)
        refreshConfigurationViaRefreshBus('2', 'ARQ.MIA', 'adsconnector', '1', '*', ADSVars.PRO_ENVIRONMENT)
    }

    sendStageEndToGPL(adsClientInfo, adsPipelineData, '470')
}

/**
 * Stage prepareNextSteps
 */
def prepareNextSteps() {
    sendStageStartToGPL(adsClientInfo, adsPipelineData, '080')
    adsPipelineData.pipelineStructure.resultPipelineData.xmlFile = xmlFile
    adsPipelineData.pipelineStructure.resultPipelineData.artifactId = artifactId
    adsPipelineData.pipelineStructure.resultPipelineData.artifactGroupId = artifactGroupId
    adsPipelineData.pipelineStructure.resultPipelineData.artifactVersion = artifactVersion

    if (ADSVars.TST_ENVIRONMENT == adsPipelineData.environment) {
        adsPipelineData.pipelineStructure.resultPipelineData.nextEnvironment = ADSVars.PRE_ENVIRONMENT
    } else if (ADSVars.PRE_ENVIRONMENT == adsPipelineData.environment) {
        adsPipelineData.pipelineStructure.resultPipelineData.nextEnvironment = ADSVars.PRO_ENVIRONMENT
    } else if (ADSVars.PRO_ENVIRONMENT == adsPipelineData.environment) {
        adsPipelineData.pipelineStructure.resultPipelineData.nextEnvironment = ADSVars.CLOSE_PIPELINE
    }

    adsPipelineData.pipelineStructure.resultPipelineData.transactionId = transactionId

    adsPipelineData.pipelineStructure.resultPipelineData.originBranch = originBranch
    adsPipelineData.pipelineStructure.resultPipelineData.userEmail = userEmail
    adsPipelineData.pipelineStructure.resultPipelineData.user = user

    adsPipelineData.pipelineStructure.resultPipelineData.pipelineOrigId = adsPipelineData.pipelineStructure.pipelineId
    sendStageEndToGPL(adsClientInfo, adsPipelineData, '080')
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

    sendPipelineEndedToGPL(initGpl, pomXmlStructure, adsPipelineData, successPipeline)
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, adsPipelineData, successPipeline)
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

    sendPipelineEndedToGPL(initGpl, pomXmlStructure, adsPipelineData, successPipeline)
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, adsPipelineData, successPipeline)
}

/**
 * Stage endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure, adsPipelineData)
    cleanWorkspace()
}
