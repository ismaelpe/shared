import groovy.transform.Field
import com.project.alm.*
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams

@Field NexusUtils nexus

@Field boolean initGpl
@Field boolean successPipeline

@Field CGSPipelineData cgsPipelineData
@Field ClientInfo cgsClientInfo
@Field BranchStructure branchStructure
@Field PomXmlStructure pomXmlStructure

@Field def repo
@Field def repoAnterior

@Field def xmlFileIn
@Field def xmlFileOut
@Field def operationId
@Field def artifactId
@Field def artifactGroupId
@Field def artifactVersion

@Field def nextEnvironment
@Field def gitUrl

@Field def originBranch
@Field def pipelineOrigId
@Field def userEmail
@Field def user

@Field def loggerLevel

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
    xmlFileIn = params.xmlFileIn
    xmlFileOut = params.xmlFileOut
    operationId = params.operationId
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
		agent {	node (almJenkinsAgent(pipelineParams)) }
        options { 
			gitLabConnection('gitlab')
            buildDiscarder(logRotator(numToKeepStr: '30'))
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
            MAVEN_OPTS = "-Dhttp.proxyHost=${proxyHost} -Dhttp.proxyPort=${proxyPort} -Dhttps.proxyHost=${proxyHost} -Dhttps.proxyPort=${proxyPort}"
        }
        stages {
            stage('Initiate provisioning') {
                steps {
                    initiateProvisioningStep()
                }
            }
            stage('Download CGS artifact and Update Version') {
                steps {
                    downloadCGSArtifactAndUpdateVersionStep()
                }
            }
            stage('Push to config server') {
                when {
                    expression { CGSVars.TST_ENVIRONMENT != nextEnvironment }
                }
                steps {
                    pushToConfigServerStep()
                }
            }
            stage('Deploy new version CGS lib operation') {
                when {
                    expression { CGSVars.PRO_ENVIRONMENT != nextEnvironment }
                }
                steps {
                    deployNewVersionCGSLibOperationStep()
                }
            }
            stage('Publish artifact') {
                steps {
                    publishArtifactStep()
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
                endiPipelineAlwaysStep()
            }
        }
    }
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/**
 * Stage 'initiateProvisioningStep'
 */
def initiateProvisioningStep() {
    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null
    printOpen("BUILD_TAG: ${env.BUILD_TAG}", EchoLevel.ALL)
    cgsPipelineData = new CGSPipelineData(nextEnvironment, "${env.BUILD_TAG}")
    cgsPipelineData.initFromCGSProvisioning(originBranch, gitUrl)
    printOpen("GIT URL: $gitUrl", EchoLevel.ALL)
    cgsPipelineData.setGitUrl(gitUrl)
    branchStructure = getBranchInfo(originBranch)
    printOpen("${branchStructure}", EchoLevel.ALL)
    pomXmlStructure = new PomXmlStructure()
    pomXmlStructure.setArtifactName(artifactId)
    printOpen("${pomXmlStructure.setArtifactName(artifactId)}", EchoLevel.ALL)
    currentBuild.displayName = "Build_${env.BUILD_ID}_" + cgsPipelineData.getPipelineBuildName() + "_" + operationId + "_" + nextEnvironment
}

/**
 * Stage 'downloadCGSArtifactAndUpdateVersionStep'
 */
def downloadCGSArtifactAndUpdateVersionStep() {
    printOpen("Downloading starter for operation: ${operationId} . artifactGroupId: ${artifactGroupId} artifactId: ${artifactId} artifactVersion ${artifactVersion}. Deploying in ${nextEnvironment}", EchoLevel.ALL)
    getArtifactFromNexusWithGAVs(artifactGroupId, artifactId, artifactVersion, ".")

    if (CGSVars.PRO_ENVIRONMENT != nextEnvironment) {
        //feature version
        def branchVersion = artifactVersion
        def branchVersionWq = MavenVersionUtilities.getArtifactVersionWithoutQualifier(branchVersion)
        printOpen("Branch version : $branchVersion", EchoLevel.ALL)
        printOpen("Branch version  w/ qualifier: $branchVersionWq", EchoLevel.ALL)

        //last release
        def lastReleaseArtifactVersion = nexus.getLastVersionNumber(artifactGroupId, artifactId, null, GlobalVars.NEXUS_RELEASES_REPO_NAME, BuildType.FINAL)
        def lastReleaseArtifactVersionWq = MavenVersionUtilities.getArtifactVersionWithoutQualifier(lastReleaseArtifactVersion)

        printOpen("cgs-operation-lib last release version : $lastReleaseArtifactVersion", EchoLevel.ALL)
        printOpen("cgs-operation-lib last release version w/ qualifier : $lastReleaseArtifactVersionWq", EchoLevel.ALL)

        if (CGSVars.TST_ENVIRONMENT == nextEnvironment) {
            if (lastReleaseArtifactVersionWq.compareTo(branchVersionWq) == 0) {
                if (MavenVersionUtilities.isRCVersion(lastReleaseArtifactVersion)) {
                    pomXmlStructure.setArtifactVersion(lastReleaseArtifactVersion)
                    pomXmlStructure.incRC()
                } else {
                    printOpen("There is a release version", EchoLevel.ALL)
                    throw new RuntimeException("There is a release version")
                }
            } else {
                pomXmlStructure.setArtifactVersion(branchVersionWq + "-RC1")
            }
            artifactVersion = pomXmlStructure.getArtifactVersion()
            cgsPipelineData.setBuildCode(pomXmlStructure.getArtifactVersionQualifier())
        } else if (CGSVars.PRE_ENVIRONMENT == nextEnvironment) {
            if (lastReleaseArtifactVersionWq.compareTo(branchVersionWq) == 0) {
                if (MavenVersionUtilities.isRCVersion(lastReleaseArtifactVersion)) {
                    pomXmlStructure.setArtifactVersion(branchVersionWq)
                } else {
                    printOpen("There is a release version", EchoLevel.ALL)
                    throw new RuntimeException("There is a release version")
                }
            } else {
                pomXmlStructure.setArtifactVersion(branchVersionWq)
            }
            artifactVersion = pomXmlStructure.getArtifactVersion()
            cgsPipelineData.setBuildCode(artifactVersion)
        } else {
            printOpen("Invalid job for artifact version", EchoLevel.ALL)
            throw new Exception("Invalid job for artifact version")
        }
        sh "mv ${artifactId}-${branchVersion}.jar ${artifactId}-${artifactVersion}.jar"
    } else {
        cgsPipelineData.setBuildCode(artifactVersion)
    }

    printOpen("artifactVersion: ${artifactVersion}", EchoLevel.ALL)
    cgsClientInfo = new ClientInfo()
    cgsClientInfo.setApplicationName(CGSVars.GPL_APPLICATION_NAME)
    cgsClientInfo.setArtifactId(artifactId)
    cgsClientInfo.setArtifactVersion(artifactVersion)
    cgsClientInfo.setArtifactType(ArtifactType.valueOfType(CGSVars.APP_TYPE))
    cgsClientInfo.setArtifactSubType(ArtifactSubType.valueOfSubType(CGSVars.APP_SUBTYPE))
    cgsClientInfo.setGroupId(artifactGroupId)
    
    almEvent = new KpiAlmEvent(
        cgsClientInfo, cgsPipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_CGS_PROVISIONING)
    
    printOpen("cgsClientInfo = ${cgsClientInfo.toString()}", EchoLevel.ALL)
    sendPipelineStartToGPL(cgsClientInfo, cgsPipelineData, pipelineOrigId)
    initGpl = true
    sendStageStartToGPL(cgsClientInfo, cgsPipelineData, "020")

    sendStageEndToGPL(cgsClientInfo, cgsPipelineData, "020")
}

/**
 * Stage 'pushToConfigServerStep'
 */
def pushToConfigServerStep() {
    sendStageStartToGPL(cgsClientInfo, cgsPipelineData, "050")

    if (CGSVars.TST_ENVIRONMENT == nextEnvironment) {
        repo = GlobalVars.GIT_CONFIG_REPO_URL_TST
        repoAnterior = GlobalVars.GIT_CONFIG_REPO_URL_TST
    } else if (CGSVars.PRE_ENVIRONMENT == nextEnvironment) {
        repo = GlobalVars.GIT_CONFIG_REPO_URL_PRE
        repoAnterior = GlobalVars.GIT_CONFIG_REPO_URL_TST
    } else if (CGSVars.PRO_ENVIRONMENT == nextEnvironment) {
        repo = GlobalVars.GIT_CONFIG_REPO_URL_PRO
        repoAnterior = GlobalVars.GIT_CONFIG_REPO_URL_PRE
    }
    
    FileUtils fileUtils = new FileUtils(this)
    GitRepositoryHandler gitActual = new GitRepositoryHandler(this, repo)
    GitRepositoryHandler gitAnterior = new GitRepositoryHandler(this, repoAnterior)

    String sanitizedCgsConfPath = FileUtils.sanitizePath(gitActual.gitProjectRelativePath)
    String sanitizedCgsConfPathAnterior = FileUtils.sanitizePath(gitAnterior.gitProjectRelativePath)

    def arqDestFolder = "${sanitizedCgsConfPath}/services/cgs/cgs-app-operations"
    def arqOriginFolder = "${sanitizedCgsConfPathAnterior}/services/cgs/cgs-app-operations"

    printOpen("Pushing XML file to configServer in ${repo}", EchoLevel.ALL)

    try {
        gitActual.lockRepoAndDo({
            gitActual.pullOrClone([depth: 1])
            gitAnterior.pullOrClone([depth: 1])

            int lastSlashIn = xmlFileIn.lastIndexOf("/");
            int lastSlashOut = xmlFileOut.lastIndexOf("/");

            def xmlFileInName = xmlFileIn.substring(lastSlashIn + 1);
            def xmlFileOutName = xmlFileOut.substring(lastSlashOut + 1);

            fileUtils.createPathIfNotExists(arqDestFolder)
            fileUtils.copyFiles(arqOriginFolder + "/" + xmlFileInName, arqDestFolder, false)
            fileUtils.copyFiles(arqOriginFolder + "/" + xmlFileOutName, arqDestFolder, false)

            gitActual.add('services/cgs/cgs-app-operations').commitAndPush("Pushing ${xmlFileIn} and ${xmlFileOut}")
        })
    } catch (err) {
        echo Utilities.prettyException(err)
        throw err
    } finally {
        gitActual.purge()
        gitAnterior.purge()
        sendStageEndToGPL(cgsClientInfo, cgsPipelineData, "050")
    }
}

/**
 * Stage 'deployNewVersionCGSLibOperationStep'
 */
def deployNewVersionCGSLibOperationStep() {
    sendStageStartToGPL(cgsClientInfo, cgsPipelineData, "060")
    deployFileNexus(artifactGroupId, artifactId, artifactVersion, "jar", "${artifactId}-${artifactVersion}.jar")
    sendStageEndToGPL(cgsClientInfo, cgsPipelineData, "060")
}

/**
 * Stage 'publishArtifactStep'
 */
def publishArtifactStep() {
    sendStageStartToGPL(cgsClientInfo, cgsPipelineData, "070")
    publishArtifactClientCGSInCatalog(cgsPipelineData, cgsClientInfo)
    sendStageEndToGPL(cgsClientInfo, cgsPipelineData, "070")
}

/**
 * Stage 'prepareNextSteps'
 */
def prepareNextSteps() {
    sendStageStartToGPL(cgsClientInfo, cgsPipelineData, "080")
    cgsPipelineData.pipelineStructure.resultPipelineData.xmlFileIn = xmlFileIn
    cgsPipelineData.pipelineStructure.resultPipelineData.xmlFileOut = xmlFileOut
    cgsPipelineData.pipelineStructure.resultPipelineData.artifactId = artifactId
    cgsPipelineData.pipelineStructure.resultPipelineData.artifactGroupId = artifactGroupId
    cgsPipelineData.pipelineStructure.resultPipelineData.artifactVersion = artifactVersion


    if (CGSVars.TST_ENVIRONMENT == cgsPipelineData.environment) {
        cgsPipelineData.pipelineStructure.resultPipelineData.nextEnvironment = CGSVars.PRE_ENVIRONMENT
    } else if (CGSVars.PRE_ENVIRONMENT == cgsPipelineData.environment) {
        cgsPipelineData.pipelineStructure.resultPipelineData.nextEnvironment = CGSVars.PRO_ENVIRONMENT
    } else if (CGSVars.PRO_ENVIRONMENT == cgsPipelineData.environment) {
        cgsPipelineData.pipelineStructure.resultPipelineData.nextEnvironment = CGSVars.CLOSE_PIPELINE
    }

    cgsPipelineData.pipelineStructure.resultPipelineData.operationId = operationId

    cgsPipelineData.pipelineStructure.resultPipelineData.originBranch = originBranch
    cgsPipelineData.pipelineStructure.resultPipelineData.userEmail = userEmail
    cgsPipelineData.pipelineStructure.resultPipelineData.user = user

    cgsPipelineData.pipelineStructure.resultPipelineData.pipelineOrigId = cgsPipelineData.pipelineStructure.pipelineId
    sendStageEndToGPL(cgsClientInfo, cgsPipelineData, "080")
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
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, cgsPipelineData, successPipeline)
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, cgsPipelineData, successPipeline)
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

    sendPipelineEndedToGPL(initGpl, pomXmlStructure, cgsPipelineData, successPipeline)
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, cgsPipelineData, successPipeline)
}

/**
 * Stage 'endiPipelineAlwaysStep'
 */
def endiPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure, cgsPipelineData)
    cleanWorkspace()
}

