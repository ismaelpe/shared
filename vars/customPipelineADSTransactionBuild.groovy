import groovy.transform.Field
import com.project.alm.*
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation
import java.util.regex.Pattern

@Field Map pipelineParams

@Field NexusUtils nexus

@Field String archetypeGroupId
@Field String archetypeArtifactId
@Field String groupId

@Field String converterlib

@Field ADSPipelineData adsPipelineData
@Field ClientInfo adsClientInfo
@Field PomXmlStructure pomXmlStructure
@Field String execJavaFile
@Field AdsTransactionDefinition adsTransactionDefinition
@Field String extensionSeparator
@Field String underslashSeparator
@Field String xmlFile

@Field String transactionId
@Field String transactionVersion
@Field String artifactId
@Field String xmlTransactionFile
@Field String filePath
@Field String modelFilePath

@Field String artifactVersion

@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

@Field boolean initAppPortal

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    nexus = new NexusUtils(this)

    //Variables fijas
    archetypeGroupId = 'com.project.alm.arch.backend.ads'
    archetypeArtifactId = 'adstransaction-archetype'
    groupId = ADSVars.LIB_GROUPID

    //Variables del proyecto de provisionamiento (parent-pom, archetype y libreria de transformacion)
    converterlib = 'adstransaction-converter-lib'

    adsClientInfo = new ClientInfo()

    //Variables calculadas a partir de inputs
    extensionSeparator = '.'
    underslashSeparator = '_'

    almEvent = null
    initCallStartMillis = new Date().getTime()

    pipeline {
        agent { node(almJenkinsAgent(pipelineParams)) }
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
        }        
        stages {
            stage('get-git-code') {
                when {
                    expression { env.gitlabMergeRequestLastCommit != null }
                }
                steps {
                    getGitCodeStep()
                }
            }
            stage('get-git-info') {
                steps {
                    getGitInfoStep()
                }
            }
            stage('getting-last-committed-ads-file') {
                when {
                    allOf {
                        expression { !adsPipelineData.isPushCI() }
                        expression { adsPipelineData.branchStructure.branchType == BranchType.FEATURE }
                    }
                }
                steps {
                    gettingLastCommittedAdsFileStep()
                }
            }
            stage('checking-files-pushed') {
                when {
                    allOf {
                        expression { !adsPipelineData.isPushCI() }
                        expression { xmlFile?.trim() }
                    }
                }
                steps {
                    checkingFilesPushedStep()
                }
            }
            stage('prepare-variables-and-calculate-version') {
                when {
                    allOf {
                        expression { !adsPipelineData.isPushCI() }
                        expression { transactionId != null }
                        expression { transactionVersion != null }
                    }
                }
                steps {
                    prepareVariablesAndCalculateVersionStep()
                }
            }
            stage('create-transaction-model') {
                when {
                    allOf {
                        expression { !adsPipelineData.isPushCI() }
                        expression { adsTransactionDefinition != null }
                    }
                }
                steps {
                    createTransactionModelStep()
                }
            }
            stage('create-project-from-archetype') {
                when {
                    allOf {
                        expression { !adsPipelineData.isPushCI() }
                        expression { adsTransactionDefinition != null }
                    }
                }
                steps {
                    createProjectFromArchetype()
                }
            }
            stage('push-to-config-server') {
                when {
                    allOf {
                        expression { !adsPipelineData.isPushCI() }
                        expression { adsTransactionDefinition != null }
                    }
                }
                steps {
                    pushToConfigServerStep()
                }
            }
            stage('removing-previous-directory') {
                when {
                    allOf {
                        expression { !adsPipelineData.isPushCI() }
                        expression { artifactId?.trim() }
                    }
                }
                steps {
                    removingPreviousDirectoryStep()
                }
            }
            stage('deploying-new-project') {
                when {
                    allOf {
                        expression { !adsPipelineData.isPushCI() }
                        expression { adsTransactionDefinition != null }
                    }
                }
                steps {
                    deployingNewProjectStep()
                }
            }
            stage('publish-artifact-catalog') {
                when {
                    allOf {
                        expression { !adsPipelineData.isPushCI() }
                        expression { artifactId?.trim() && adsPipelineData.branchStructure.branchType == BranchType.FEATURE }
                        expression { GlobalVars.BackEndAppPortal_ENABLED }
                    }
                }
                steps {
                    publishArtifactCatalogStep()
                }
            }
            stage('refresh-connector-configuration') {
                when {
                    allOf {
                        expression { !adsPipelineData.isPushCI() }
                        expression { adsTransactionDefinition != null }
                    }
                }
                steps {
                    refreshConnectorConfigurationStep()
                }
            }
            stage('prepare-result-for-next-step') {
                when {
                    allOf {
                        expression { !adsPipelineData.isPushCI() }
                        expression { xmlFile?.trim() }
                    }
                }
                steps {
                    prepareResultForNextStep()
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
 * Stage 'getGitCodeStep'
 */
def getGitCodeStep() {
    new GitUtils(this, false).updateGitCode("${env.gitlabMergeRequestLastCommit}")
}

/**
 * Stage 'getGitInfoStep'
 */
def getGitInfoStep() {
    initGlobalVars(pipelineParams)
    adsPipelineData = getInfoGitForADSPipeline()
    def branchStructure = getBranchInfo()

    adsPipelineData.init(branchStructure, ADSVars.APP_SUBTYPE, ADSVars.APP_TYPE)

    currentBuild.displayName = "Build_${env.BUILD_ID}_" + adsPipelineData.getPipelineBuildName() + (adsPipelineData.isPushCI() ? '_CI' : '')

    pomXmlStructure = analizePomXml(ADSVars.APP_TYPE, ADSVars.APP_SUBTYPE)
    pomXmlStructure.applicationName = 'adstransaction'
    adsPipelineData.buildCode = pomXmlStructure.artifactVersionWithoutQualifier
    //La aplicacion GAR de estos artefactos es la siguiente
    currentBuild.displayName = "Build_${env.BUILD_ID}_" + adsPipelineData.getPipelineBuildName()
}

/**
 * Stage 'gettingLastCommittedAdsFileStep'
 */
def gettingLastCommittedAdsFileStep() {
    //                            xmlFile = changesets('(add|edit)( )(.+D_v[0-9]+\\.xml)$', 3)
    printOpen("List All The Files in a commit ${gitlabMergeRequestLastCommit}", EchoLevel.DEBUG)
    sh "git show --pretty=oneline --name-status ${gitlabMergeRequestLastCommit} > ${WORKSPACE}/changeSets.log"
    sh "cat ${WORKSPACE}/changeSets.log"

    String changeSetFile = "${WORKSPACE}/changeSets.log"

    def fileHandler = readFile changeSetFile
    def fileLines = fileHandler.readLines()
    int count = 0
    for (line in fileLines) {
        printOpen("${line}", EchoLevel.DEBUG)
        String[] str = line.split()
        String editType = str[0].trim()
        filePath = str[1].trim()
        printOpen('EditType: ' + editType, EchoLevel.DEBUG)
        if (!editType.toUpperCase().contains('D') && filePath.toUpperCase().contains('.XML')) {
            printOpen('xmlFile: ' + filePath, EchoLevel.INFO)
            xmlFile = filePath
            count++
        } else {
            printOpen('Skip File: ' + filePath, EchoLevel.INFO)
        }
        printOpen('XML Count: ', EchoLevel.ALL)
        if (count > 1) {
            printOpen("Can't push more than one xml", EchoLevel.ERROR)
            throw new RuntimeException("Can't push more than one xml ")
        }
    }
    filePath = xmlFile

    printOpen(xmlFile?.trim() ? 'We have detected xml changes. A new client will be generated...' : 'No xml changes were found. Aborting...', EchoLevel.INFO)
    printOpen("xmlFile: ${xmlFile}", EchoLevel.INFO)
    printOpen("filePath: ${filePath}", EchoLevel.INFO)
}

/**
 * Stage 'checkingFilesPushedStep'
 */
def checkingFilesPushedStep() {
    int dot = xmlFile.lastIndexOf(extensionSeparator)
    int underSlash = xmlFile.lastIndexOf(underslashSeparator)
    int lastSlash = xmlFile.lastIndexOf('/')

    def preTransactionId = xmlFile.substring(lastSlash, underSlash)
    transactionId = preTransactionId.substring(1, preTransactionId.length() - 1)
    printOpen("transactionId: ${transactionId}", EchoLevel.ALL)

    def preVersion = xmlFile.substring(underSlash, dot)
    transactionVersion = preVersion.substring(2)
    printOpen("version: ${transactionVersion}", EchoLevel.ALL)

    checkBranchAndXmlFiles(adsPipelineData.branchStructure.getBranchName(), transactionId, xmlFile)
}

/**
 * Stage 'prepareVariablesAndCalculateVersionStep'
 */
def prepareVariablesAndCalculateVersionStep() {
    printOpen("xmlFile: ${xmlFile}", EchoLevel.ALL)

    //def cmd = "mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} dependency:copy -Dartifact=${archetypeGroupId}:${converterlib}:${env.PROVISIONER_VERSION}:jar:jar-with-dependencies -DoutputDirectory=. -Dmdep.stripVersion=true -Dmdep.stripClassifier=true <Only_Maven_Settings> "
    def cmd = "mvn  dependency:copy -Dartifact=${archetypeGroupId}:${converterlib}:${env.PROVISIONER_VERSION}:jar:jar-with-dependencies -DoutputDirectory=. -Dmdep.stripVersion=true -Dmdep.stripClassifier=true <Only_Maven_Settings> "
    runMavenCommand(cmd)

    execJavaFile = "${WORKSPACE}/${converterlib}.jar"

    printOpen("Library downloaded to parse XML defintion: ${execJavaFile}", EchoLevel.ALL)

    printOpen('Getting transaction XML filename', EchoLevel.ALL)
    artifactId = 'ads-' + transactionId.toLowerCase() + '-lib'
    def xmlTransactionFilename = "${transactionId}D_v${transactionVersion}.xml"
    xmlTransactionFile = "${WORKSPACE}/cifn/repository/${xmlTransactionFilename}"
    printOpen("${xmlTransactionFile}", EchoLevel.ALL)
    modelFilePath = "${WORKSPACE}/${transactionId}_${transactionVersion}.yml"

    if (adsPipelineData.branchStructure.branchType == BranchType.FEATURE) {
        printOpen('Normalizing some XML parameters [__COPTRM, __SNVERS]', EchoLevel.ALL)
        sh(returnStdout: true, script: "sed -i 's/<field name=\"dadesBasiques.subcodi\"/<field name=\"__COPTRM\"/' ${xmlTransactionFile}")
        sh(returnStdout: true, script: "sed -i 's/<field name=\"versioServeiNegoci\"/<field name=\"__SNVERS\"/' ${xmlTransactionFile}")

        def statusRepo = sh(returnStdout: true, script: 'git status')
        if (statusRepo.contains(xmlTransactionFilename)) {
            printOpen('Pushing normalized parameters to the git repository...', EchoLevel.ALL)
            def pushMessage = "${GlobalVars.GIT_TAG_CI_PUSH} ${GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_ADS_PARAMETERS_NORMALIZED}"
            pushRepoWithMessage(adsPipelineData, pushMessage, xmlTransactionFile)
        } else {
            printOpen('Nothing normalized. No git push will be done', EchoLevel.ALL)
        }
    }

    printOpen('Parsing and creating AdsTransactionDefinition', EchoLevel.ALL)
    Integer major = "${transactionVersion}".toInteger()

    String txLastReleaseVersion = nexus.getLastVersionNumber(groupId, artifactId, major, GlobalVars.NEXUS_RELEASES_REPO_NAME)

    String newTxVersion = txLastReleaseVersion?.trim() ?
        MavenVersionUtilities.incMinor(MavenVersionUtilities.getArtifactVersionWithoutQualifier(txLastReleaseVersion)) + '-SNAPSHOT' :
        "${major.toString()}.0.0-SNAPSHOT"

    adsClientInfo.setApplicationName('adstransaction')
    adsClientInfo.setArtifactId(artifactId)
    adsClientInfo.setArtifactVersion(newTxVersion)
    adsClientInfo.setArtifactType(ArtifactType.valueOfType(ADSVars.APP_TYPE))
    adsClientInfo.setArtifactSubType(ArtifactSubType.valueOfSubType(ADSVars.APP_SUBTYPE))
    adsClientInfo.setGroupId(groupId)
    almEvent = new KpiAlmEvent(
        pomXmlStructure, adsPipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_ADS_BUILD)

    printOpen("${adsClientInfo.toString()}", EchoLevel.ALL)
    printOpen("Creating the project ${artifactId} with version ${newTxVersion}", EchoLevel.ALL)
    adsTransactionDefinition = new AdsTransactionDefinition(transactionId, transactionVersion, filePath, modelFilePath, artifactId, newTxVersion)
    artifactVersion = newTxVersion
    sendPipelineStartToAppPortal(adsClientInfo, adsPipelineData, '')
    initAppPortal = true
    sendStageStartToAppPortal(adsClientInfo, adsPipelineData, '070')
    printOpen("artifactId : $artifactId", EchoLevel.ALL)
    printOpen("artifactVersion : $artifactVersion", EchoLevel.ALL)
    sendStageEndToAppPortal(adsClientInfo, adsPipelineData, '070')
}

/**
 * Stage 'createTransactionModelStep'
 */
def createTransactionModelStep() {
    sendStageStartToAppPortal(adsClientInfo, adsPipelineData, '150')
    printOpen("Creating yaml from XML for ${adsTransactionDefinition.getModelFilePath()}", EchoLevel.ALL)
    sh "java -jar ${execJavaFile} -t ${adsTransactionDefinition.getTransaction()} -v ${adsTransactionDefinition.getTransactionVersion()} -o ${adsTransactionDefinition.getModelFilePath()}"
    sendStageEndToAppPortal(adsClientInfo, adsPipelineData, '150')
}

/**
 * Stage 'createProjectFromArchetype'
 */
def createProjectFromArchetype() {
    sendStageStartToAppPortal(adsClientInfo, adsPipelineData, '250')
    sh 'mkdir tmp'
    printOpen("Creating Project From Archetype for ${adsTransactionDefinition.getArtifactId()}", EchoLevel.ALL)

    //def cmd = "cd tmp && mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Only_Maven_Settings> archetype:generate -DgroupId=${groupId} -DartifactId=${adsTransactionDefinition.getArtifactId()} -Dversion=${adsTransactionDefinition.getVersion()} -DparentArtifactVersion=${env.PROVISIONER_VERSION} -Dtransaction=${adsTransactionDefinition.getTransaction()} -DtransactionVersion=${adsTransactionDefinition.getTransactionVersion()} -DarchetypeGroupId=${archetypeGroupId} -DarchetypeArtifactId=${archetypeArtifactId} -DarchetypeVersion=${env.PROVISIONER_VERSION}  -DmodelFilePath=${adsTransactionDefinition.getModelFilePath()}"
    def cmd = "cd tmp && mvn  <Only_Maven_Settings> archetype:generate -DgroupId=${groupId} -DartifactId=${adsTransactionDefinition.getArtifactId()} -Dversion=${adsTransactionDefinition.getVersion()} -DparentArtifactVersion=${env.PROVISIONER_VERSION} -Dtransaction=${adsTransactionDefinition.getTransaction()} -DtransactionVersion=${adsTransactionDefinition.getTransactionVersion()} -DarchetypeGroupId=${archetypeGroupId} -DarchetypeArtifactId=${archetypeArtifactId} -DarchetypeVersion=${env.PROVISIONER_VERSION}  -DmodelFilePath=${adsTransactionDefinition.getModelFilePath()}"
    runMavenCommand(cmd)

    sendStageEndToAppPortal(adsClientInfo, adsPipelineData, '250')
}

/**
 * Stage 'pushToConfigServerStep'
 */
def pushToConfigServerStep() {
    sendStageStartToAppPortal(adsClientInfo, adsPipelineData, '300')

    String repoUrl = GlobalVars.GIT_CONFIG_REPO_URL_TST

    printOpen("Pushing XML file to configServer in ${repoUrl}", EchoLevel.ALL)

    FileUtils fileUtils = new FileUtils(this)
    GitRepositoryHandler git = new GitRepositoryHandler(this, repoUrl)

    String sanitizedConfigRepoPath = FileUtils.sanitizePath(git.gitProjectRelativePath)

    def arqDestFolder = "${sanitizedConfigRepoPath}/services/ads/ads-app-transactions"

    try {
        git.lockRepoAndDo({
            git.purge().pullOrClone([depth: 1])

            fileUtils.createPathIfNotExists(arqDestFolder)
            printOpen("xmlFile: ${adsTransactionDefinition.getXmlFile()}", EchoLevel.ALL)
            fileUtils.copyFiles("${adsTransactionDefinition.getXmlFile()}", arqDestFolder, false)

            git.add('services/ads/ads-app-transactions').commitAndPush("Pushing ${xmlFile}")
        })
    } catch (err) {
        echo Utilities.prettyException(err)
        throw err
    } finally {
        git.purge()
        sendStageEndToAppPortal(adsClientInfo, adsPipelineData, '300')
    }
}

/**
 * Stage 'removingPreviousDirectoryStep'
 */
def removingPreviousDirectoryStep() {
    sendStageStartToAppPortal(adsClientInfo, adsPipelineData, '350')
    String sanitizedDir = artifactId.replace(' ', '\\ ')
    printOpen('Listamos directorio previa elimiacion', EchoLevel.ALL)
    sh 'pwd'
    sh 'ls -la'
    sh "rm -rf temp/${sanitizedDir}"
    sendStageEndToAppPortal(adsClientInfo, adsPipelineData, '350', null, adsPipelineData.bmxStructure.environment)
}

/**
 * Stage 'deployingNewProjectStep'
 */
def deployingNewProjectStep() {
    sendStageStartToAppPortal(adsClientInfo, adsPipelineData, '400')
    def mvnLog = null
    printOpen("Deploying ${artifactId} project", EchoLevel.ALL)

    //def cmd = "cd tmp && mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Only_Maven_Settings> clean deploy -Dmaven.install.skip -Dmaven.deploy.skip=false -f ${artifactId}/pom.xml clean deploy"
    def cmd = "cd tmp && mvn  <Only_Maven_Settings> clean deploy -Dmaven.install.skip -Dmaven.deploy.skip=false -f ${artifactId}/pom.xml clean deploy"

    def deploymentRepo = MavenUtils.getDeploymentRepository(adsClientInfo.getArtifactVersion())

    mvnLog = runMavenGoalWithRetries(pomXmlStructure, adsPipelineData, cmd, [
        forceDeploymentRepo: deploymentRepo,
        kpiAlmEvent: new KpiAlmEvent(
            pomXmlStructure, adsPipelineData,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.MVN_DEPLOY_ADS_TRANSACTION_NEXUS)
    ])

    def artifactDeployedOnNexus = NexusUtils.extractArtifactsFromLog(mvnLog)
    adsPipelineData.routesToNexus = artifactDeployedOnNexus

    def versionWithoutQualifier = artifactVersion
    int index = versionWithoutQualifier.indexOf('-')
    if (index != -1) {
        versionWithoutQualifier = versionWithoutQualifier.substring(0, index)
    }
    printOpen("artifactDeployedOnNexus: $artifactDeployedOnNexus", EchoLevel.ALL)
    printOpen("starterArtifactId $artifactId", EchoLevel.ALL)
    printOpen("versionWithoutQualifier $versionWithoutQualifier", EchoLevel.ALL)

    if (MavenVersionUtilities.isSNAPSHOT(adsClientInfo.artifactVersion)) {
        //Requerimos el build id
        adsPipelineData.buildCode = NexusUtils.getBuildId(artifactDeployedOnNexus, artifactId + '-', versionWithoutQualifier + '-')
        printOpen('Es SNAPSHOT', EchoLevel.ALL)
    } else if (MavenVersionUtilities.isRCVersion(adsClientInfo.artifactVersion)) {
        //En caso de RC devolvemos RC0, RC1, RC2...
        adsPipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()
        printOpen('Es RC', EchoLevel.ALL)
    } else if (MavenVersionUtilities.isRelease(adsClientInfo.artifactVersion)) {
        //En caso de release devolvemos la versi�n en formato x.y.z
        adsPipelineData.buildCode = versionWithoutQualifier
        printOpen('Es RELEASE', EchoLevel.ALL)
    }
    printOpen("El buildCode es ${adsPipelineData.buildCode}", EchoLevel.ALL)
    sendStageEndToAppPortal(adsClientInfo, adsPipelineData, '400')
}

/**
 * Stage 'publishArtifactCatalogStep'
 */
def publishArtifactCatalogStep() {
    printOpen('pipelineData:', EchoLevel.ALL)
    adsPipelineData.toString()
    printOpen('branchStructure:', EchoLevel.ALL)
    printOpen("${adsPipelineData.branchStructure.toString()}", EchoLevel.ALL)
    sendStageStartToAppPortal(adsClientInfo, adsPipelineData, '450')
    printOpen('publishing artifact in catalog', EchoLevel.ALL)
    publishArtifactClientADSInCatalog(adsPipelineData, adsClientInfo)
    sendStageEndToAppPortal(adsClientInfo, adsPipelineData, '450')
//debugInfo(pipelineParams,pomXmlStructure, pipelineData)
}

/**
 * Stage 'refreshConnectorConfigurationStep'
 */
def refreshConnectorConfigurationStep() {
    sendStageStartToAppPortal(adsClientInfo, adsPipelineData, '470')
    refreshConfigurationViaRefreshBus('1', 'ARQ.MIA', 'adsconnector', '1', '*', ADSVars.DEV_ENVIROMENT)
    refreshConfigurationViaRefreshBus('2', 'ARQ.MIA', 'adsconnector', '1', '*', ADSVars.DEV_ENVIROMENT)
    refreshConfigurationViaRefreshBus('1', 'ARQ.MIA', 'adsconnector', '1', '*', ADSVars.TST_ENVIRONMENT)
    refreshConfigurationViaRefreshBus('2', 'ARQ.MIA', 'adsconnector', '1', '*', ADSVars.TST_ENVIRONMENT)
    sendStageEndToAppPortal(adsClientInfo, adsPipelineData, '470')
}

/**
 * Stage 'prepareResultForNextStep'
 */
def prepareResultForNextStep() {
    sendStageStartToAppPortal(adsClientInfo, adsPipelineData, '500')
    adsPipelineData.pipelineStructure.resultPipelineData.xmlFile = xmlFile
    adsPipelineData.pipelineStructure.resultPipelineData.artifactId = artifactId
    adsPipelineData.pipelineStructure.resultPipelineData.artifactGroupId = groupId
    adsPipelineData.pipelineStructure.resultPipelineData.artifactVersion = artifactVersion

    //Add transactionId to the artifact
    adsPipelineData.pipelineStructure.resultPipelineData.nextEnvironment = ADSVars.TST_ENVIRONMENT

    adsPipelineData.pipelineStructure.resultPipelineData.transactionId = transactionId

    adsPipelineData.pipelineStructure.resultPipelineData.originBranch = adsPipelineData.branchStructure.getBranchName()
    adsPipelineData.pipelineStructure.resultPipelineData.userEmail = adsPipelineData.pushUserEmail
    adsPipelineData.pipelineStructure.resultPipelineData.user = adsPipelineData.pushUser
    sendStageEndToAppPortal(adsClientInfo, adsPipelineData, '500')
//debugInfo(pipelineParams,pomXmlStructure, pipelineData)
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen('Pipeline has succeeded', EchoLevel.INFO)
    updateGitlabCommitStatus name: '999-globalBuild', state: 'success'
    def successPipeline = true
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis - initCallStartMillis))
    }

    if (xmlFile?.trim()) {
        sendPipelineEndedToAppPortal(initAppPortal, pomXmlStructure, adsPipelineData, successPipeline)
        sendPipelineResultadoToAppPortal(initAppPortal, pomXmlStructure, adsPipelineData, successPipeline)
    }
    printOpen('ads-lib info:', EchoLevel.ALL)
    printOpen("ads-libGroupId : $groupId", EchoLevel.ALL)
    printOpen("ads-libArtifactId : $artifactId", EchoLevel.ALL)
    printOpen("ads-artifactVersion : $artifactVersion", EchoLevel.ALL)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    updateGitlabCommitStatus name: '999-globalBuild', state: 'failed'
    def successPipeline = false
    printOpen('Pipeline has failed', EchoLevel.ERROR)
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis - initCallStartMillis))
    }

    if (xmlFile?.trim()) {
        sendPipelineEndedToAppPortal(initAppPortal, pomXmlStructure, adsPipelineData, successPipeline)
        sendPipelineResultadoToAppPortal(initAppPortal, pomXmlStructure, adsPipelineData, successPipeline)
    }
}

/**
 * Stage 'endiPipelineAlwaysStep'
 */
def endiPipelineAlwaysStep() {
    printOpen('xmlFile: ', EchoLevel.ALL)
    printOpen('xmlFile?.trim() ', EchoLevel.ALL)

    attachPipelineLogsToBuild(pomXmlStructure, adsPipelineData)
    cleanWorkspace()
}
