import groovy.transform.Field
import com.project.alm.*

import java.util.regex.Pattern

@Field Map pipelineParams

@Field NexusUtils nexus

@Field String archetypeGroupId
@Field String archetypeArtifactId
@Field String groupId

@Field String converterlib

@Field TANDEMPipelineData tandemPipelineData
@Field ClientInfo tandemClientInfo
@Field PomXmlStructure pomXmlStructure
@Field String execJavaFile
@Field TandemTransactionDefinition tandemTransactionDefinition
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

@Field ArtifactType artifactType = ArtifactType.SIMPLE
@Field ArtifactSubType artifactSubtype = ArtifactSubType.ARCH_LIB

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    nexus = new NexusUtils(this)

    //Variables fijas
    archetypeGroupId = 'com.project.alm.arch.backend.tandem'
    archetypeArtifactId = 'tandemtransaction-archetype'
    groupId = "com.project.alm.tandem.transaction"

    //Variables del proyecto de provisionamiento (parent-pom, archetype y libreria de transformacion)
    converterlib = 'tandemtransaction-converter-lib'

    tandemClientInfo = new ClientInfo()

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
			sendLogsToAppPortal = true
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
            stage('getting-last-committed-tandem-file') {
                when {
                    allOf {
                        expression { !tandemPipelineData.isPushCI() }
                        expression { tandemPipelineData.branchStructure.branchType == BranchType.FEATURE }
                    }
                }
                steps {
                    gettingLastCommittedTandemFileStep()
                }
            }
            stage('checking-files-pushed') {
                when {
                    allOf {
                        expression { !tandemPipelineData.isPushCI() }
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
                        expression { !tandemPipelineData.isPushCI() }
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
                        expression { !tandemPipelineData.isPushCI() }
                        expression { tandemTransactionDefinition != null }
                    }
                }
                steps {
                    createTransactionModelStep()
                }
            }
            stage('create-project-from-archetype') {
                when {
                    allOf {
                        expression { !tandemPipelineData.isPushCI() }
                        expression { tandemTransactionDefinition != null }
                    }
                }
                steps {
                    createProjectFromArchetype()
                }
            }
            stage('push-to-config-server') {
                when {
                    allOf {
                        expression { !tandemPipelineData.isPushCI() }
                        expression { tandemTransactionDefinition != null }
                    }
                }
                steps {
                    pushToConfigServerStep()
                }
            }
            stage('removing-previous-directory') {
                when {
                    allOf {
                        expression { !tandemPipelineData.isPushCI() }
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
                        expression { !tandemPipelineData.isPushCI() }
                        expression { tandemTransactionDefinition != null }
                    }
                }
                steps {
                    deployingNewProjectStep()
                }
            }
            stage('publish-artifact-catalog') {
                when {
                    allOf {
                        expression { !tandemPipelineData.isPushCI() }
                        expression { artifactId?.trim() && tandemPipelineData.branchStructure.branchType == BranchType.FEATURE }
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
                        expression { !tandemPipelineData.isPushCI() }
                        expression { tandemTransactionDefinition != null }
                    }
                }
                steps {
                    refreshConnectorConfigurationStep()
                }
            }
            stage('prepare-result-for-next-step') {
                when {
                    allOf {
                        expression { !tandemPipelineData.isPushCI() }
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
	tandemPipelineData = infoGitToPipelineData(new TANDEMPipelineData(Environment.DEV,"${env.BUILD_TAG}"))
    def branchStructure = getBranchInfo()

    tandemPipelineData.init(branchStructure, artifactType, artifactSubtype)

    currentBuild.displayName = "Build_${env.BUILD_ID}_" + tandemPipelineData.getPipelineBuildName() + (tandemPipelineData.isPushCI() ? '_CI' : '')

    pomXmlStructure = analizePomXml(artifactType, artifactSubtype)
    pomXmlStructure.applicationName = 'tandemtransaction'
    tandemPipelineData.buildCode = "SNAPSHOT"
    //La aplicacion GAR de estos artefactos es la siguiente
    currentBuild.displayName = "Build_${env.BUILD_ID}_" + tandemPipelineData.getPipelineBuildName()
}

/**
 * Stage 'gettingLastCommittedTandemFileStep'
 */
def gettingLastCommittedTandemFileStep() {
	String changeSetFile = "${WORKSPACE}/changeSets.log"
    printOpen("List All The Files in a commit ${gitlabMergeRequestLastCommit}", EchoLevel.INFO)
    sh "git show --pretty=oneline --name-status ${gitlabMergeRequestLastCommit} > ${changeSetFile}"
	String changeSetContent = sh(returnStdout: true, script: "cat ${changeSetFile}")
	printOpen("Change set content: \n${changeSetContent}")

    def fileHandler = readFile changeSetFile
    def fileLines = fileHandler.readLines()
    int count = 0
    for (line in fileLines) {
        printOpen("${line}", EchoLevel.DEBUG)
        String[] str = line.split()
        String editType = str[0].trim()
        filePath = str[1].trim()
        printOpen('EditType: ' + editType, EchoLevel.DEBUG)
        if (!editType.toUpperCase().contains('D') && filePath.toUpperCase().contains('.XML') && !filePath.equals('pom.xml')) {
            printOpen('xmlFile: ' + filePath, EchoLevel.INFO)
            xmlFile = filePath
            count++
        } else {
            printOpen('Skip File: ' + filePath, EchoLevel.INFO)
        }
        printOpen('XML Count: '+ count, EchoLevel.ALL)
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

    checkBranchAndXmlFiles(tandemPipelineData.branchStructure.getBranchName(), transactionId, xmlFile)
}

/**
 * Stage 'prepareVariablesAndCalculateVersionStep'
 */
def prepareVariablesAndCalculateVersionStep() {
    printOpen("xmlFile: ${xmlFile}", EchoLevel.ALL)

    def cmd = "mvn  dependency:copy -Dartifact=${archetypeGroupId}:${converterlib}:${env.TANDEM_PROVISIONER_VERSION}:jar:jar-with-dependencies -DoutputDirectory=. -Dmdep.stripVersion=true -Dmdep.stripClassifier=true <Only_Maven_Settings> "
    runMavenCommand(cmd)

    execJavaFile = "${WORKSPACE}/${converterlib}.jar"

    printOpen("Library downloaded to parse XML defintion: ${execJavaFile}", EchoLevel.INFO)

    printOpen('Getting transaction XML filename', EchoLevel.ALL)
    artifactId = 'tandem-' + transactionId.toLowerCase() + '-lib'
    def xmlTransactionFilename = "${transactionId}D_v${transactionVersion}.xml"
    xmlTransactionFile = "${WORKSPACE}/cifn/repository/${xmlTransactionFilename}"
    printOpen("${xmlTransactionFile}", EchoLevel.ALL)
    modelFilePath = "${WORKSPACE}/${transactionId}_${transactionVersion}.yml"

    printOpen('Parsing and creating TandemTransactionDefinition', EchoLevel.ALL)
    Integer major = "${transactionVersion}".toInteger()

    String txLastReleaseVersion = nexus.getLastVersionNumber(groupId, artifactId, major, GlobalVars.NEXUS_RELEASES_REPO_NAME)

    String newTxVersion = txLastReleaseVersion?.trim() ?
        MavenVersionUtilities.incMinor(MavenVersionUtilities.getArtifactVersionWithoutQualifier(txLastReleaseVersion)) + '-SNAPSHOT' :
        "${major.toString()}.0.0-SNAPSHOT"

    tandemClientInfo.setApplicationName('tandemtransaction')
    tandemClientInfo.setArtifactId(artifactId)
    tandemClientInfo.setArtifactVersion(newTxVersion)
    tandemClientInfo.setArtifactType(artifactType)
    tandemClientInfo.setArtifactSubType(artifactSubtype)
    tandemClientInfo.setGroupId(groupId)
    almEvent = new KpiAlmEvent(
        pomXmlStructure, tandemPipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_TANDEM_BUILD)

    printOpen("${tandemClientInfo.toString()}", EchoLevel.ALL)
    printOpen("Creating the project ${artifactId} with version ${newTxVersion}", EchoLevel.INFO)
    tandemTransactionDefinition = new TandemTransactionDefinition(transactionId, transactionVersion, filePath, modelFilePath, artifactId, newTxVersion)
    artifactVersion = newTxVersion
    sendPipelineStartToAppPortal(tandemClientInfo, tandemPipelineData, '')
    initAppPortal = true
    sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '070')
    printOpen("artifactId : $artifactId", EchoLevel.INFO)
    printOpen("artifactVersion : $artifactVersion", EchoLevel.INFO)
    sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '070')
}

/**
 * Stage 'createTransactionModelStep'
 */
def createTransactionModelStep() {
    sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '150')
	try {
	    printOpen("Creating yaml from XML for ${tandemTransactionDefinition.getModelFilePath()}", EchoLevel.INFO)
	    sh "java -jar ${execJavaFile} -t ${tandemTransactionDefinition.getTransaction()} -v ${tandemTransactionDefinition.getTransactionVersion()} -o ${tandemTransactionDefinition.getModelFilePath()}"
	    sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '150')
	} catch (Exception e) {
		printOpen(e.getMessage(), EchoLevel.ERROR)
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '150', null, null, "error")
		throw e
	}
}

/**
 * Stage 'createProjectFromArchetype'
 */
def createProjectFromArchetype() {
    sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '250')
	try {
		sh 'mkdir tmp'
	    printOpen("Creating Project From Archetype for ${tandemTransactionDefinition.getArtifactId()}", EchoLevel.INFO)
	
	    def cmd = "cd tmp && mvn  <Only_Maven_Settings> archetype:generate -DgroupId=${groupId} -DartifactId=${tandemTransactionDefinition.getArtifactId()} -Dversion=${tandemTransactionDefinition.getVersion()} -DparentArtifactVersion=${env.TANDEM_PROVISIONER_VERSION} -Dtransaction=${tandemTransactionDefinition.getTransaction()} -DtransactionVersion=${tandemTransactionDefinition.getTransactionVersion()} -DarchetypeGroupId=${archetypeGroupId} -DarchetypeArtifactId=${archetypeArtifactId} -DarchetypeVersion=${env.TANDEM_PROVISIONER_VERSION}  -DmodelFilePath=${tandemTransactionDefinition.getModelFilePath()}"
	    runMavenCommand(cmd)
	
	    sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '250')
	} catch (Exception e) {
		printOpen(e.getMessage(), EchoLevel.ERROR)
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '250', null, null, "error")
		throw e
	}
}

/**
 * Stage 'pushToConfigServerStep'
 */
def pushToConfigServerStep() {
    sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '300')
    
	String repoUrl = GlobalVars.GIT_CONFIG_REPO_URL_TST
    printOpen("Pushing XML file to configServer in ${repoUrl}", EchoLevel.INFO)
    GitRepositoryHandler git = new GitRepositoryHandler(this, repoUrl)
    
	try {
	
    	FileUtils fileUtils = new FileUtils(this)
	    String sanitizedConfigRepoPath = FileUtils.sanitizePath(git.gitProjectRelativePath)
	
	    def arqDestFolder = "${sanitizedConfigRepoPath}/services/tandem/tandem-app-transactions"

        git.lockRepoAndDo({
            git.purge().pullOrClone([depth: 1])

            fileUtils.createPathIfNotExists(arqDestFolder)
            printOpen("xmlFile: ${tandemTransactionDefinition.getXmlFile()}", EchoLevel.ALL)
            fileUtils.copyFiles("${tandemTransactionDefinition.getXmlFile()}", arqDestFolder, false)

            git.add('services/tandem/tandem-app-transactions').commitAndPush("Pushing ${xmlFile}")
        })
        sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '300')
    } catch (err) {
        printOpen(err.getMessage(), EchoLevel.ERROR)
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '300', null, null, "error")
        throw err
    } finally {
        git.purge()
    }
}

/**
 * Stage 'removingPreviousDirectoryStep'
 */
def removingPreviousDirectoryStep() {
    sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '350')
	try {
	    String sanitizedDir = artifactId.replace(' ', '\\ ')
	    printOpen('Listamos directorio previa elimiacion', EchoLevel.ALL)
	    sh 'pwd'
	    sh 'ls -la'
	    sh "rm -rf temp/${sanitizedDir}"
	    sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '350')
	} catch (Exception e) {
		printOpen(e.getMessage(), EchoLevel.ERROR)
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '350', null, null, "error")
		throw e
	}
}

/**
 * Stage 'deployingNewProjectStep'
 */
def deployingNewProjectStep() {
    sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '400')
    def mvnLog = null
    printOpen("Deploying ${artifactId} to Artifactory", EchoLevel.INFO)
	try {
	    //def cmd = "cd tmp && mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Only_Maven_Settings> clean deploy -Dmaven.install.skip -Dmaven.deploy.skip=false -f ${artifactId}/pom.xml clean deploy"
	    def cmd = "cd tmp && mvn  <Only_Maven_Settings> clean deploy -Dmaven.install.skip -Dmaven.deploy.skip=false -f ${artifactId}/pom.xml clean deploy"
	
	    def deploymentRepo = MavenUtils.getDeploymentRepository(tandemClientInfo.getArtifactVersion())
	
	    mvnLog = runMavenGoalWithRetries(pomXmlStructure, tandemPipelineData, cmd, [
	        forceDeploymentRepo: deploymentRepo,
	        kpiAlmEvent: new KpiAlmEvent(
	            pomXmlStructure, tandemPipelineData,
	            KpiAlmEventStage.UNDEFINED,
	            KpiAlmEventOperation.MVN_DEPLOY_TANDEM_TRANSACTION_NEXUS)
	    ])
	
	    def artifactDeployedOnNexus = NexusUtils.extractArtifactsFromLog(mvnLog)
	    tandemPipelineData.routesToNexus = artifactDeployedOnNexus
	
	    def versionWithoutQualifier = artifactVersion
	    int index = versionWithoutQualifier.indexOf('-')
	    if (index != -1) {
	        versionWithoutQualifier = versionWithoutQualifier.substring(0, index)
	    }
	    printOpen("artifactDeployedOnNexus: $artifactDeployedOnNexus", EchoLevel.DEBUG)
		printOpen("artifactId: $artifactId", EchoLevel.INFO)
	    printOpen("groupId: $groupId", EchoLevel.INFO)
	    printOpen("version: $artifactVersion", EchoLevel.INFO)
	
	    if (MavenVersionUtilities.isSNAPSHOT(tandemClientInfo.artifactVersion)) {
	        //Requerimos el build id
	        tandemPipelineData.buildCode = NexusUtils.getBuildId(artifactDeployedOnNexus, artifactId + '-', versionWithoutQualifier + '-')
	        printOpen('Es SNAPSHOT', EchoLevel.ALL)
	    } else if (MavenVersionUtilities.isRCVersion(tandemClientInfo.artifactVersion)) {
	        //En caso de RC devolvemos RC0, RC1, RC2...
	        tandemPipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()
	        printOpen('Es RC', EchoLevel.ALL)
	    } else if (MavenVersionUtilities.isRelease(tandemClientInfo.artifactVersion)) {
	        //En caso de release devolvemos la versi�n en formato x.y.z
	        tandemPipelineData.buildCode = versionWithoutQualifier
	        printOpen('Es RELEASE', EchoLevel.ALL)
	    }
	    printOpen("Build code: ${tandemPipelineData.buildCode}", EchoLevel.INFO)
		sendPipelineUpdateToAppPortal(true,tandemClientInfo, tandemPipelineData)
	    sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '400')		
	} catch (Exception e) {
		printOpen("Error deploying artifact: ${e}", EchoLevel.ERROR)
		printOpen("Error deploying artifact: ${e.getMessage()}", EchoLevel.ERROR)
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '400', null, null, "error")
		throw e
	}
}

/**
 * Stage 'publishArtifactCatalogStep'
 */
def publishArtifactCatalogStep() {
    printOpen('pipelineData:', EchoLevel.ALL)
    tandemPipelineData.toString()
    printOpen('branchStructure:', EchoLevel.ALL)
    printOpen("${tandemPipelineData.branchStructure.toString()}", EchoLevel.ALL)
    sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '450')
	try {
	    printOpen('publishing artifact in catalog', EchoLevel.ALL)
	    publishArtifactClientTANDEMInCatalog(tandemPipelineData, tandemClientInfo)
	    sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '450')
	} catch (Exception e) {
		printOpen(e.getMessage(), EchoLevel.ERROR)
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '450', null, null, "error")
		throw e
	}
}

/**
 * Stage 'refreshConnectorConfigurationStep'
 */
def refreshConnectorConfigurationStep() {
    sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '470')
	try {
	    refreshConfigurationViaRefreshBus('1', 'ARQ.MIA', 'tandemconnector', '1', '*', Environment.DEV.name())
	    refreshConfigurationViaRefreshBus('2', 'ARQ.MIA', 'tandemconnector', '1', '*', Environment.DEV.name())
	    refreshConfigurationViaRefreshBus('1', 'ARQ.MIA', 'tandemconnector', '1', '*', Environment.TST.name())
	    refreshConfigurationViaRefreshBus('2', 'ARQ.MIA', 'tandemconnector', '1', '*', Environment.TST.name())
	    sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '470')
	} catch (Exception e) {
		printOpen(e.getMessage(), EchoLevel.ERROR)
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '470', null, null, "error")
		throw e
	}
}

/**
 * Stage 'prepareResultForNextStep'
 */
def prepareResultForNextStep() {
    sendStageStartToAppPortal(tandemClientInfo, tandemPipelineData, '500')
	try {
	    tandemPipelineData.pipelineStructure.resultPipelineData.xmlFile = xmlFile
	    tandemPipelineData.pipelineStructure.resultPipelineData.artifactId = artifactId
	    tandemPipelineData.pipelineStructure.resultPipelineData.artifactGroupId = groupId
	    tandemPipelineData.pipelineStructure.resultPipelineData.artifactVersion = artifactVersion
	
	    //Add transactionId to the artifact
	    tandemPipelineData.pipelineStructure.resultPipelineData.nextEnvironment = Environment.TST.name()
	
	    tandemPipelineData.pipelineStructure.resultPipelineData.transactionId = transactionId
	
	    tandemPipelineData.pipelineStructure.resultPipelineData.originBranch = tandemPipelineData.branchStructure.getBranchName()
	    tandemPipelineData.pipelineStructure.resultPipelineData.userEmail = tandemPipelineData.pushUserEmail
	    tandemPipelineData.pipelineStructure.resultPipelineData.user = tandemPipelineData.pushUser
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '500')
	} catch (Exception e) {
		printOpen(e.getMessage(), EchoLevel.ERROR)
		sendStageEndToAppPortal(tandemClientInfo, tandemPipelineData, '500', null, null, "error")
		throw e
	}
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen('SUCCEDED', EchoLevel.INFO)
    updateGitlabCommitStatus name: '999-globalBuild', state: 'success'
    def successPipeline = true
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis - initCallStartMillis))
    }

    if (xmlFile?.trim()) {
    	sendPipelineResultadoToAppPortal(initAppPortal, pomXmlStructure, tandemPipelineData, successPipeline)
        sendPipelineEndedToAppPortal(initAppPortal, pomXmlStructure, tandemPipelineData, successPipeline)
    }
    printOpen('tandem-lib info:', EchoLevel.INFO)
    printOpen("tandem-libGroupId : $groupId", EchoLevel.INFO)
    printOpen("tandem-libArtifactId : $artifactId", EchoLevel.INFO)
    printOpen("tandem-artifactVersion : $artifactVersion", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    updateGitlabCommitStatus name: '999-globalBuild', state: 'failed'
    def successPipeline = false
    printOpen('FAILED', EchoLevel.ERROR)
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis - initCallStartMillis))
    }

    if (xmlFile?.trim()) {
    	sendPipelineResultadoToAppPortal(initAppPortal, pomXmlStructure, tandemPipelineData, successPipeline)
        sendPipelineEndedToAppPortal(initAppPortal, pomXmlStructure, tandemPipelineData, successPipeline)
    }
}

/**
 * Stage 'endiPipelineAlwaysStep'
 */
def endiPipelineAlwaysStep() {
    printOpen('xmlFile: ', EchoLevel.ALL)
    printOpen('xmlFile?.trim() ', EchoLevel.ALL)

    attachPipelineLogsToBuild(pomXmlStructure, tandemPipelineData)
    cleanWorkspace()
}
