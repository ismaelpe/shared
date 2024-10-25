import groovy.transform.Field
import com.caixabank.absis3.*

@Field Map pipelineParams

@Field NexusUtils nexus

@Field String archetypeGroupId
@Field String archetypeArtifactId
@Field String groupId
@Field String libraryOperationVersion

@Field String mvnParameters
@Field String converterlib

@Field CGSPipelineData cgsPipelineData
@Field ClientInfo cgsClientInfo
@Field PomXmlStructure pomXmlStructure

@Field String execJavaFile
@Field CGSOperationDefinition cgsOperationDefinition
@Field String extensionSeparator
@Field String underslashSeparator
@Field String xmlFileIn
@Field String xmlFileOut

@Field String operationId
@Field String operationVersion
@Field String artifactId
@Field String filePathIn
@Field String filePathOut
@Field String modelFilePath

@Field String artifactVersion

@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

@Field String initGpl

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    nexus = new NexusUtils(this)

    //Variables fijas
    archetypeGroupId = 'com.caixabank.absis.arch.backend.cgs'
    archetypeArtifactId = 'cgsoperation-archetype'
    groupId = CGSVars.LIB_GROUPID
    libraryOperationVersion = env.CGS_PROVISIONER_VERSION

    //Variables del proyecto de provisionamiento (parent-pom, archetype y libreria de transformacion)
    mvnParameters = ''
    converterlib = 'cgsoperation-converter-lib'

    cgsClientInfo = new ClientInfo()

    //Variables calculadas a partir de inputs
    extensionSeparator = '.'
    underslashSeparator = '_'

    almEvent = null
    initCallStartMillis = new Date().getTime()

    pipeline {
        agent { node(absisJenkinsAgent(pipelineParams)) }
        //Environment sobre el qual se ejecuta este tipo de job
        options {
            gitLabConnection('gitlab')
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
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
        }
        //Atencion que en el caso que estemos en un MergeRequest... quizas solo debamos validar la issue
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
            stage('getting-last-committed-cgs-file') {
                when {
                    allOf {
                        expression { !cgsPipelineData.isPushCI() }
                        expression { cgsPipelineData.branchStructure.branchType == BranchType.FEATURE }
                    }
                }
                steps {
                    getLastCommittedCGSFileStep()
                }
            }
            stage('checking-files-pushed') {
                when {
                    allOf {
                        expression { !cgsPipelineData.isPushCI() }
                        expression { xmlFileIn?.trim() || xmlFileOut?.trim() }
                    }
                }
                steps {
                    checkingFilesPushedStep()
                }
            }
            stage('prepare-variables-and-calculate-version') {
                when {
                    allOf {
                        expression { !cgsPipelineData.isPushCI() }
                        expression { operationId != null }
                        expression { operationVersion != null }
                    }
                }
                steps {
                    prepareVariablesAndCalculateVersionStep()
                }
            }
            stage('create-operation-model') {
                when {
                    allOf {
                        expression { !cgsPipelineData.isPushCI() }
                        expression { cgsOperationDefinition != null }
                    }
                }
                steps {
                    createOperationModelStep()
                }
            }
            stage('create-project-from-archetype') {
                when {
                    allOf {
                        expression { !cgsPipelineData.isPushCI() }
                        expression { cgsOperationDefinition != null }
                    }
                }
                steps {
                    createProjectFromArchetypeStep()
                }
            }
            stage('push-to-config-server') {
                when {
                    allOf {
                        expression { !cgsPipelineData.isPushCI() }
                        expression { cgsOperationDefinition != null }
                    }
                }
                steps {
                    pushToConfigServerStep()
                }
            }
            stage('removing-previous-directory') {
                when {
                    allOf {
                        expression { !cgsPipelineData.isPushCI() }
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
                        expression { !cgsPipelineData.isPushCI() }
                        expression { cgsOperationDefinition != null }
                    }
                }
                steps {
                    deployNewProjectStep()
                }
            }
            stage('publish-artifact-catalog') {
                when {
                    allOf {
                        expression { !cgsPipelineData.isPushCI() }
                        expression { artifactId?.trim() && cgsPipelineData.branchStructure.branchType == BranchType.FEATURE }
                        expression { GlobalVars.GSA_ENABLED }
                    }
                }
                steps {
                    plublishArtifactCatalogStep()
                }
            }

            stage('prepare-result-for-next-step') {
                when {
                    allOf {
                        expression { !cgsPipelineData.isPushCI() }
                        expression { xmlFileIn?.trim() || xmlFileOut?.trim() }
                    }
                }
                steps {
                    prepareResultForNextStepStep()
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
 Stage 'getGitCodeStep'
 */
def getGitCodeStep() {
    new GitUtils(this, false).updateGitCode("${env.gitlabMergeRequestLastCommit}")
}

/**
 Stage 'getGitRepogetGitInfoStagerStep'
 */
def getGitInfoStep() {
    initGlobalVars(pipelineParams)
    cgsPipelineData = getInfoGitForCGSPipeline()
    def branchStructure = getBranchInfo()
    cgsPipelineData.init(branchStructure, CGSVars.APP_SUBTYPE, CGSVars.APP_TYPE)

    currentBuild.displayName = "Build_${env.BUILD_ID}_" + cgsPipelineData.getPipelineBuildName() + (cgsPipelineData.isPushCI() ? '_CI' : '')

    pomXmlStructure = analizePomXml(CGSVars.APP_TYPE, CGSVars.APP_SUBTYPE)
    pomXmlStructure.applicationName = 'cgsoperation'
    //La aplicacion GAR de estos artefactos es la siguiente
    currentBuild.displayName = "Build_${env.BUILD_ID}_" + cgsPipelineData.getPipelineBuildName()
}

/**
 Stage 'getLastCommittedCGSFileStep'
 */
def getLastCommittedCGSFileStep() {
    //                            xmlFile = changesets('(add|edit)( )(.+D_v[0-9]+\\.xml)$', 3)
    printOpen("List All The Files in a commit ${gitlabMergeRequestLastCommit}", EchoLevel.ALL)
    sh "git show --pretty= --name-status ${gitlabMergeRequestLastCommit} > ${WORKSPACE}/changeSets.log"
    sh "cat ${WORKSPACE}/changeSets.log"

    String changeSetFile = "${WORKSPACE}/changeSets.log"

    def fileHandler = readFile changeSetFile
    def fileLines = fileHandler.readLines()
    int count = 0
    for (line in fileLines) {
        echo line
        String[] str = line.split()
        String editType = str[0].trim()
        def filePath = str[1].trim()
        printOpen('EditType: ', EchoLevel.ALL)
        if (filePath.toUpperCase().contains('_IN') && filePath.toUpperCase().contains('.XML')) {
            printOpen('xmlFileIn: ', EchoLevel.ALL)
            xmlFileIn = filePath
            count++
        } else if (filePath.toUpperCase().contains('_OUT') && filePath.toUpperCase().contains('.XML')) {
            printOpen('xmlFileOut: ', EchoLevel.ALL)
            xmlFileOut = filePath
            count++
        } else if (filePath.toUpperCase().contains('VCABXMLP') || filePath.toUpperCase().contains('VINXMLP') || filePath.toUpperCase().contains('VOUTXMLP')) {
            printOpen('XML not supported for NOWI transactions', EchoLevel.ALL)
            throw new RuntimeException('XML not supported. Check the following documentation to make the necessary changes: https://confluence.cloud.lacaixa.es/confluence/display/serArqMcrsvcs/CGS+Provisioning#CGSProvisioning-ProcedimientoparalastransaccionesNOWI')
        } else {
            printOpen('Skip File: ', EchoLevel.ALL)
        }
        printOpen('XML Count: ', EchoLevel.ALL)
        if (count > 2) {
            printOpen("Can't push more than two xml", EchoLevel.ALL)
            throw new RuntimeException("Can't push more than two xml ")
        }
    }
    filePathIn = xmlFileIn
    filePathOut = xmlFileOut

    if (xmlFileIn?.trim() || xmlFileOut?.trim()) {
        printOpen('We have detected xml changes. A new client will be generated...', EchoLevel.ALL)
    } else {
        printOpen('No xml changes were found. Aborting...', EchoLevel.ALL)
    }
    printOpen("xmlFileIn: ${xmlFileIn} && xmlFileOut: ${xmlFileOut}", EchoLevel.ALL)
    printOpen("filePathIn: ${filePathIn} && filePathOut: ${filePathOut}", EchoLevel.ALL)
}

/**
 Stage 'checkingFilesPushedStep'
 */
def checkingFilesPushedStep() {
    def xmlFile
    if (xmlFileIn?.trim()) {
        xmlFile = xmlFileIn
    } else if (xmlFileOut?.trim()) {
        xmlFile = xmlFileOut
    } else { //Quizas sobra ya que se comprueba en el when de arriba que al menos uno este relleno
        throw new RuntimeException('XML not found')
    }

    int dot = xmlFile.lastIndexOf(extensionSeparator)
    int lastSlash = xmlFile.lastIndexOf('/')
    int underSlashLast = xmlFile.lastIndexOf(underslashSeparator)
    int underSlashFirst = xmlFile.indexOf(underslashSeparator, lastSlash)

    def route = xmlFile.substring(0, lastSlash + 1)
    def preOperationId = xmlFile.substring(lastSlash, underSlashFirst)
    operationId = preOperationId.substring(1)
    printOpen("operationId: ${operationId}", EchoLevel.ALL)

    def preVersion = xmlFile.substring(underSlashLast, dot)
    operationVersion = preVersion.substring(2)
    printOpen("version: ${operationVersion}", EchoLevel.ALL)

    if (!xmlFileIn?.trim()) {
        xmlFileIn = route + operationId + '_IN_v' + operationVersion + '.xml'
        filePathIn = xmlFileIn
    } else if (!xmlFileOut?.trim()) {
        xmlFileOut = route + operationId + '_OUT_v' + operationVersion + '.xml'
        filePathOut = xmlFileOut
    }

    checkCGSBranchAndXmlFiles(cgsPipelineData.branchStructure.getBranchName(), operationId, xmlFileIn, xmlFileOut)
}

/**
 Stage 'prepareVariablesAndCalculateVersionStep'
 */
def prepareVariablesAndCalculateVersionStep() {
    printOpen("xmlFileIn: ${xmlFileIn} && xmlFileOut: ${xmlFileOut}", EchoLevel.ALL)

    def cmd = "mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} dependency:copy -Dartifact=${archetypeGroupId}:${converterlib}:${libraryOperationVersion}:jar:jar-with-dependencies -DoutputDirectory=. -Dmdep.stripVersion=true -Dmdep.stripClassifier=true <Only_Maven_Settings> "
    runMavenCommand(cmd)

    execJavaFile = "${WORKSPACE}/${converterlib}.jar"

    printOpen("Library downloaded to parse XML defintion: ${execJavaFile}", EchoLevel.ALL)

    printOpen('Getting operation XML filename', EchoLevel.ALL)
    artifactId = 'cgs-' + operationId.toLowerCase() + '-lib'
    modelFilePath = "${WORKSPACE}/${operationId}_${operationVersion}.yml"

    printOpen('Parsing and creating CGSOperationDefinition', EchoLevel.ALL)
    Integer major = "${operationVersion}".toInteger()

    String txLastReleaseVersion = nexus.getLastVersionNumber(groupId, artifactId, major, GlobalVars.NEXUS_RELEASES_REPO_NAME)

    String newTxVersion = txLastReleaseVersion?.trim() ?
        MavenVersionUtilities.incMinor(MavenVersionUtilities.getArtifactVersionWithoutQualifier(txLastReleaseVersion)) + '-SNAPSHOT' :
        "${major.toString()}.0.0-SNAPSHOT"

    cgsClientInfo.setApplicationName('cgsoperation')
    cgsClientInfo.setArtifactId(artifactId)
    cgsClientInfo.setArtifactVersion(newTxVersion)
    cgsClientInfo.setArtifactType(ArtifactType.valueOfType(CGSVars.APP_TYPE))
    cgsClientInfo.setArtifactSubType(ArtifactSubType.valueOfSubType(CGSVars.APP_SUBTYPE))
    cgsClientInfo.setGroupId(groupId)
    printOpen("${cgsClientInfo.toString()}", EchoLevel.ALL)
    printOpen("Creating the project ${artifactId} with version ${newTxVersion}", EchoLevel.ALL)
    cgsOperationDefinition = new CGSOperationDefinition(operationId, operationVersion, filePathIn, filePathOut, modelFilePath, artifactId, newTxVersion)
    artifactVersion = newTxVersion

    almEvent = new KpiAlmEvent(
        pomXmlStructure, cgsPipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_CGS_BUILD)

    sendPipelineStartToGPL(cgsClientInfo, cgsPipelineData, '')
    initGpl = true
    sendStageStartToGPL(cgsClientInfo, cgsPipelineData, '070')
    printOpen("artifactId : $artifactId", EchoLevel.ALL)
    printOpen("artifactVersion : $artifactVersion", EchoLevel.ALL)
    sendStageEndToGPL(cgsClientInfo, cgsPipelineData, '070')
}

/**
 Stage 'createOperationModelStep'
 */
def createOperationModelStep() {
    sendStageStartToGPL(cgsClientInfo, cgsPipelineData, '150')
    printOpen("Creating yaml from XML for ${cgsOperationDefinition.getModelFilePath()}", EchoLevel.ALL)
    sh "java -jar ${execJavaFile} -t ${cgsOperationDefinition.getOperation()} -v ${cgsOperationDefinition.getOperationVersion()} -o ${cgsOperationDefinition.getModelFilePath()}"
    sendStageEndToGPL(cgsClientInfo, cgsPipelineData, '150')
}

/**
 Stage 'createProjectFromArchetypeStep'
 */
def createProjectFromArchetypeStep() {
    sendStageStartToGPL(cgsClientInfo, cgsPipelineData, '250')
    sh 'mkdir tmp'
    printOpen("Creating Project From Archetype for ${cgsOperationDefinition.getArtifactId()}", EchoLevel.ALL)

    def cmd = "cd tmp && mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Only_Maven_Settings> archetype:generate -DgroupId=${groupId} -DartifactId=${cgsOperationDefinition.getArtifactId()} -Dversion=${cgsOperationDefinition.getVersion()} -DparentArtifactVersion=${libraryOperationVersion} -Doperation=${cgsOperationDefinition.getOperation()} -DoperationVersion=${cgsOperationDefinition.getOperationVersion()} -DarchetypeGroupId=${archetypeGroupId} -DarchetypeArtifactId=${archetypeArtifactId} -DarchetypeVersion=${libraryOperationVersion}  -DmodelFilePath=${cgsOperationDefinition.getModelFilePath()} "
    runMavenCommand(cmd)

    sendStageEndToGPL(cgsClientInfo, cgsPipelineData, '250')
}

/**
 Stage 'pushToConfigServerStep'
 */
def pushToConfigServerStep() {
    sendStageStartToGPL(cgsClientInfo, cgsPipelineData, '300')

    String repoUrl = GlobalVars.GIT_CONFIG_REPO_URL_TST

    printOpen("Pushing XML file to configServer in ${repoUrl}", EchoLevel.ALL)

    FileUtils fileUtils = new FileUtils(this)
    GitRepositoryHandler git = new GitRepositoryHandler(this, repoUrl)

    String sanitizedConfigRepoPath = FileUtils.sanitizePath(git.gitProjectRelativePath)

    def arqDestFolder = "${sanitizedConfigRepoPath}/services/cgs/cgs-app-operations"

    try {
        git.lockRepoAndDo({
            git.purge().pullOrClone([depth: 1])
            fileUtils.createPathIfNotExists(arqDestFolder)
            printOpen("xmlFileIn: ${cgsOperationDefinition.getXmlFileIn()} && xmlFileOut: ${cgsOperationDefinition.getXmlFileOut()}", EchoLevel.ALL)
            fileUtils.copyFiles("${cgsOperationDefinition.getXmlFileIn()}", arqDestFolder, false)
            fileUtils.copyFiles("${cgsOperationDefinition.getXmlFileOut()}", arqDestFolder, false)

            git.add('services/cgs/cgs-app-operations').commitAndPush("Pushing ${xmlFileIn} and ${xmlFileOut}")
        })
    } catch (err) {
        echo Utilities.prettyException(err)
        throw err
    } finally {
        git.purge()
        sendStageEndToGPL(cgsClientInfo, cgsPipelineData, '300')
    }
}

/**
 Stage 'removingPreviousDirectoryStep'
 */
def removingPreviousDirectoryStep() {
    sendStageStartToGPL(cgsClientInfo, cgsPipelineData, '350')
    String sanitizedDir = artifactId.replace(' ', '\\ ')
    printOpen('Listamos directorio previa elimiacion', EchoLevel.ALL)
    sh 'pwd'
    sh 'ls -la'
    sh "rm -rf temp/${sanitizedDir}"
    sendStageEndToGPL(cgsClientInfo, cgsPipelineData, '350', null, cgsPipelineData.bmxStructure.environment)
}

/**
 Stage 'def deployNewProjectStep() {
'
 */
def deployNewProjectStep() {
    sendStageStartToGPL(cgsClientInfo, cgsPipelineData, '400')
    def mvnLog = null
    printOpen("Deploying ${artifactId} project", EchoLevel.ALL)

    def cmd = "cd tmp && mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Only_Maven_Settings> clean deploy -Dmaven.install.skip -Dmaven.deploy.skip=false -f ${artifactId}/pom.xml clean deploy"

    def deploymentRepo = MavenUtils.getDeploymentRepository(cgsClientInfo.getArtifactVersion())

    mvnLog = runMavenGoalWithRetries(pomXmlStructure, cgsPipelineData, cmd, [
        forceDeploymentRepo: deploymentRepo,
        kpiAlmEvent: new KpiAlmEvent(
            pomXmlStructure, cgsPipelineData,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.MVN_DEPLOY_GCS_OPERATION_NEXUS)
    ])

    def artifactDeployedOnNexus = NexusUtils.extractArtifactsFromLog(mvnLog)
    cgsPipelineData.routesToNexus = artifactDeployedOnNexus

    def versionWithoutQualifier = artifactVersion
    int index = versionWithoutQualifier.indexOf('-')
    if (index != -1) {
        versionWithoutQualifier = versionWithoutQualifier.substring(0, index)
    }
    printOpen("artifactDeployedOnNexus: $artifactDeployedOnNexus", EchoLevel.ALL)
    printOpen("starterArtifactId $artifactId", EchoLevel.ALL)
    printOpen("versionWithoutQualifier $versionWithoutQualifier", EchoLevel.ALL)

    if (MavenVersionUtilities.isSNAPSHOT(cgsClientInfo.artifactVersion)) {
        //Requerimos el build id
        cgsPipelineData.buildCode = NexusUtils.getBuildId(artifactDeployedOnNexus, artifactId + '-', versionWithoutQualifier + '-')
        printOpen('Es SNAPSHOT', EchoLevel.ALL)
    } else if (MavenVersionUtilities.isRCVersion(cgsClientInfo.artifactVersion)) {
        //En caso de RC devolvemos RC0, RC1, RC2...
        cgsPipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()
        printOpen('Es RC', EchoLevel.ALL)
    } else if (MavenVersionUtilities.isRelease(cgsClientInfo.artifactVersion)) {
        //En caso de release devolvemos la versi�n en formato x.y.z
        cgsPipelineData.buildCode = versionWithoutQualifier
        printOpen('Es RELEASE', EchoLevel.ALL)
    }
    printOpen("El buildCode es ${cgsPipelineData.buildCode}", EchoLevel.ALL)
    sendStageEndToGPL(cgsClientInfo, cgsPipelineData, '400')
}

/**
 Stage 'plublishArtifactCatalogStep'
 */
def plublishArtifactCatalogStep() {
    printOpen('pipelineData:', EchoLevel.ALL)
    cgsPipelineData.toString()
    printOpen('branchStructure:', EchoLevel.ALL)
    printOpen("${cgsPipelineData.branchStructure.toString()}", EchoLevel.ALL)
    sendStageStartToGPL(cgsClientInfo, cgsPipelineData, '450')
    printOpen('publishing artifact in catalog', EchoLevel.ALL)
    publishArtifactClientCGSInCatalog(cgsPipelineData, cgsClientInfo)
    sendStageEndToGPL(cgsClientInfo, cgsPipelineData, '450')
}

/**
 Stage 'prepareResultForNextStepStep'
 */
def prepareResultForNextStepStep() {
    sendStageStartToGPL(cgsClientInfo, cgsPipelineData, '500')
    cgsPipelineData.pipelineStructure.resultPipelineData.xmlFileIn = xmlFileIn
    cgsPipelineData.pipelineStructure.resultPipelineData.xmlFileOut = xmlFileOut
    cgsPipelineData.pipelineStructure.resultPipelineData.artifactId = artifactId
    cgsPipelineData.pipelineStructure.resultPipelineData.artifactGroupId = groupId
    cgsPipelineData.pipelineStructure.resultPipelineData.artifactVersion = artifactVersion

    //Add operationId to the artifact
    cgsPipelineData.pipelineStructure.resultPipelineData.nextEnvironment = CGSVars.TST_ENVIRONMENT

    cgsPipelineData.pipelineStructure.resultPipelineData.operationId = operationId

    cgsPipelineData.pipelineStructure.resultPipelineData.originBranch = cgsPipelineData.branchStructure.getBranchName()
    cgsPipelineData.pipelineStructure.resultPipelineData.userEmail = cgsPipelineData.pushUserEmail
    cgsPipelineData.pipelineStructure.resultPipelineData.user = cgsPipelineData.pushUser
    sendStageEndToGPL(cgsClientInfo, cgsPipelineData, '500')
}

/**
 Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    updateGitlabCommitStatus name: '999-globalBuild', state: 'success'
    printOpen('Pipeline has succeeded', EchoLevel.INFO)

    def successPipeline = true
    if (almEvent != null) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis - initCallStartMillis))
    }

    if (xmlFileIn?.trim() || xmlFileOut?.trim()) {
        sendPipelineEndedToGPL(initGpl, pomXmlStructure, cgsPipelineData, successPipeline)
        sendPipelineResultadoToGPL(initGpl, pomXmlStructure, cgsPipelineData, successPipeline)
    }
    printOpen('cgs-lib info:', EchoLevel.ALL)
    printOpen("cgs-libGroupId : $groupId", EchoLevel.ALL)
    printOpen("cgs-libArtifactId : $artifactId", EchoLevel.ALL)
    printOpen("cgs-artifactVersion : $artifactVersion", EchoLevel.ALL)
}

/**
 Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    updateGitlabCommitStatus name: '999-globalBuild', state: 'failed'
    def successPipeline = false
    printOpen('Pipeline has failed', EchoLevel.ERROR)
    if (almEvent != null) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis - initCallStartMillis))
    }

    if (xmlFileIn?.trim() || xmlFileOut?.trim()) {
        sendPipelineEndedToGPL(initGpl, pomXmlStructure, cgsPipelineData, successPipeline)
        sendPipelineResultadoToGPL(initGpl, pomXmlStructure, cgsPipelineData, successPipeline)
    }
}

/**
 Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    printOpen('xmlFileIn: ', EchoLevel.ALL)
    printOpen('xmlFileIn?.trim() ', EchoLevel.ALL)
    printOpen('xmlFileOut: ', EchoLevel.ALL)
    printOpen('xmlFileOut?.trim() ', EchoLevel.ALL)

    attachPipelineLogsToBuild(pomXmlStructure, cgsPipelineData)
    cleanWorkspace()
}
