import groovy.transform.Field
import com.project.alm.*
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams

@Field NexusUtils nexus
@Field boolean initAppPortal
@Field boolean buildingSnapshot
@Field boolean provisioningLifecycle

@Field ASEPipelineData asePipelineData

@Field String contractGitCommit
@Field String gitUrl
@Field String originBranchParam
@Field String user
@Field String userEmail
@Field String nextEnvironment
@Field String yamlFilePath
@Field String artifactVersion

@Field String yamlMajorVersion
@Field String serviceName
@Field String exceptionNameTemplate

@Field String archetypeLastVersion

@Field ClientInfo clientInfo
@Field String aseClientArtifactId
@Field String aseClientVersion

@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters
    nexus = new NexusUtils(this)
    initAppPortal = false
    buildingSnapshot = false
    provisioningLifecycle = false

    //Job parameters
    contractGitCommit = params.contractGitCommit
    gitUrl = params.gitUrl
    originBranchParam = params.originBranchParam
    user = params.user
    userEmail = params.userEmail
    nextEnvironment = params.nextEnvironment ? params.nextEnvironment : ASEVars.DEV_ENVIROMENT
    yamlFilePath = params.yamlFilePath
    artifactVersion = params.artifactVersion

    almEvent = null
    initCallStartMillis = new Date().getTime()

    pipeline {
        agent { node(almJenkinsAgent(pipelineParams)) }
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
        }
        stages {
            stage('get-git-code') {
                steps {
                    getGitCodeStep()
                }
            }
            stage('get-git-info') {
                steps {
                    getGitInfoStep()
                }
            }
            stage('getting-last-committed-ase-contract') {
                when {
                    allOf {
                        expression { !asePipelineData.isPushCI() }
                        expression { asePipelineData.branchStructure.branchType == BranchType.FEATURE }
                    }
                }
                steps {
                    gettingLastCommittedAseContractStep()
                }
            }
            stage('prepare-variables-and-calculate-version') {
                when {
                    allOf {
                        expression { !asePipelineData.isPushCI() }
                        expression { yamlFilePath?.trim() }
                        expression { yamlMajorVersion?.trim() }
                        expression { serviceName?.trim() }
                    }
                }
                steps {
                    prepareVariablesAndCalculateVersionStep()
                }
            }
            stage('create-project-from-archetype') {
                when {
                    allOf {
                        expression { !asePipelineData.isPushCI() }
                        expression { archetypeLastVersion?.trim() }
                        expression { aseClientVersion?.trim() }
                    }
                }
                steps {
                    createProjectFromArchetypeStep()
                }
            }
            stage('deploying-new-project') {
                when {
                    allOf {
                        expression { !asePipelineData.isPushCI() }
                        expression { archetypeLastVersion?.trim() }
                        expression { aseClientVersion?.trim() }
                    }
                }
                steps {
                    deployingNewProjectStep()
                }
            }
            stage('publish-artifact-catalog') {
                when {
                    allOf {
                        expression { !asePipelineData.isPushCI() }
                        expression { yamlFilePath?.trim() }
                        expression { archetypeLastVersion?.trim() }
                        expression { aseClientVersion?.trim() }
                        expression { GlobalVars.BackEndAppPortal_ENABLED }
                    }
                }
                steps {
                    publishArtifactCatalogStep()
                }
            }
            stage('prepare-result-for-next-step') {
                when {
                    allOf {
                        expression { !asePipelineData.isPushCI() }
                        expression { yamlFilePath?.trim() }
                        expression { archetypeLastVersion?.trim() }
                        expression { aseClientVersion?.trim() }
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
                endPipelineAlwaysStep()
            }
        }
    }
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/**
 * Stage getGitCodeStep
 */
def getGitCodeStep() {
    initGlobalVars(pipelineParams)
    provisioningLifecycle = contractGitCommit && gitUrl && originBranchParam
    buildingSnapshot = ( ! provisioningLifecycle ) && env.gitlabMergeRequestLastCommit

    if (provisioningLifecycle) {
        printOpen("contractGitCommit has been informed as ${contractGitCommit}. This is a RC/RELEASE pipeline", EchoLevel.DEBUG)
        git branch: "${originBranchParam}", credentialsId: 'GITLAB_CREDENTIALS', url: "${gitUrl}"
    } else if (buildingSnapshot) {
        contractGitCommit = env.gitlabMergeRequestLastCommit
        gitUrl = env.GIT_URL
        user = env.gitlabUserEmail
        userEmail = env.gitlabUserEmail
        originBranchParam = env.GIT_BRANCH
        printOpen("contractGitCommit has NOT been informed. This is a FEATURE pipeline. Last commit was ${contractGitCommit}", EchoLevel.DEBUG)
    } else {
        error 'Required parameters are missing\n' +
            'Parameters dump:\n'
        "env: ${env}\n" +
            "params: ${params}"
    }
    new GitUtils(this, false).updateGitCode(contractGitCommit)
}

/**
 * Stage getGitInfoStep
 */
def getGitInfoStep() {
    if (buildingSnapshot) {
        asePipelineData = getInfoGitForASEPipeline(contractGitCommit, gitUrl, user, userEmail)
        asePipelineData.init(getBranchInfo(originBranchParam), ASEVars.APP_SUBTYPE, ASEVars.APP_TYPE)
    } else if (provisioningLifecycle) {
        asePipelineData = new ASEPipelineData(nextEnvironment, "${env.BUILD_TAG}")
        asePipelineData.initFromASEProvisioning(originBranchParam, gitUrl)
    }

    currentBuild.displayName = "Build_${env.BUILD_ID}_" + asePipelineData.getPipelineBuildName() + (asePipelineData.isPushCI() ? '_CI' : '')

    pomXmlStructure = analizePomXml(ASEVars.APP_TYPE, ASEVars.APP_SUBTYPE)
}

/**
 * Stage gettingLastCommittedAseContractStep
 */
def gettingLastCommittedAseContractStep() {
    if (ASEVars.DEV_ENVIROMENT == nextEnvironment) {
        printOpen('Calculating yamlFilePath from changesets...', EchoLevel.DEBUG)
        yamlFilePath = changesets('(add|edit)( )(contracts\\/.+_v[0-9]+.yaml)$', 3)?.trim()
                        } else {
        printOpen('yamlFilePath has been already informed via pipeline parameters.', EchoLevel.DEBUG)
    }
    if (yamlFilePath) {
        yamlMajorVersion = yamlFilePath.substring(yamlFilePath.lastIndexOf('_v') + 2, yamlFilePath.size() - 5).toInteger()
        serviceName = yamlFilePath.substring(yamlFilePath.lastIndexOf('/') + 1, yamlFilePath.size() - 12)
        aseClientArtifactId = ASEVars.CLIENT_ARTIFACT_ID.replace('<servicename>', serviceName.toLowerCase())
        exceptionNameTemplate = "Se${serviceName}{type}Exception"
    }

    printOpen((yamlFilePath?.trim() ? 'We have detected yaml changes. A new client will be generated...' : 'No yaml changes were found. Aborting...') +
                        "\nyamlFilePath: ${yamlFilePath}" +
                        "\nyamlMajorVersion: ${yamlMajorVersion}" +
                        "\nserviceName: ${serviceName}" +
                        "\naseClientArtifactId: ${aseClientArtifactId}" +
                        "\nexceptionNameTemplate: ${exceptionNameTemplate}"
                        , EchoLevel.INFO)
}

/**
 * Stage prepareVariablesAndCalculateVersionStep
 */
def prepareVariablesAndCalculateVersionStep() {
    archetypeLastVersion =
        nexus.getLastVersionNumber(
            ASEVars.ARCHETYPE_GROUP_ID,
            ASEVars.ARCHETYPE_ARTIFACT_ID,
            GlobalVars.NEXUS_RELEASES_REPO_NAME
        )

    printOpen("archetypeLastVersion: ${archetypeLastVersion}", EchoLevel.DEBUG)

    if (ASEVars.DEV_ENVIROMENT == nextEnvironment) {
        aseClientVersion =
            nexus.getLastVersionNumber(
                ASEVars.CLIENT_GROUP_ID,
                aseClientArtifactId,
                yamlMajorVersion as Integer,
                GlobalVars.NEXUS_RELEASES_REPO_NAME
            )

        aseClientVersion =
            aseClientVersion ?
                "${MavenVersionUtilities.incMinor(MavenVersionUtilities.getArtifactVersionWithoutQualifier(aseClientVersion))}-SNAPSHOT" :
                "${yamlMajorVersion}.0.0-SNAPSHOT"
    } else if (ASEVars.TST_ENVIRONMENT == nextEnvironment) {
        aseClientVersion = artifactVersion.replace('-SNAPSHOT', '-RC1')
    } else {
        aseClientVersion = artifactVersion?.substring(0, artifactVersion?.lastIndexOf('-'))
    }

    printOpen("aseClientVersion: ${aseClientVersion}", EchoLevel.DEBUG)

    clientInfo = new ClientInfo()
    clientInfo.groupId = ASEVars.CLIENT_GROUP_ID
    clientInfo.artifactId = aseClientArtifactId
    clientInfo.artifactVersion = aseClientVersion
    clientInfo.applicationName = ASEVars.GAR_APPLICATION_NAME
    clientInfo.artifactType = ArtifactType.valueOfType(ASEVars.APP_TYPE)
    clientInfo.artifactSubType = ArtifactSubType.valueOfSubType(ASEVars.APP_SUBTYPE)
    almEvent = new KpiAlmEvent(
        clientInfo, asePipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_ASE_BUILD)
    sendPipelineStartToAppPortal(clientInfo, asePipelineData, '')
    initAppPortal = true
    sendStageStartToAppPortal(clientInfo, asePipelineData, '010')
    sendStageEndToAppPortal(clientInfo, asePipelineData, '010')
    sendStageStartToAppPortal(clientInfo, asePipelineData, '020')
    sendStageEndToAppPortal(clientInfo, asePipelineData, '020')
    sendStageStartToAppPortal(clientInfo, asePipelineData, '030')
    sendStageEndToAppPortal(clientInfo, asePipelineData, '030')
}

/**
 * Stage createProjectFromArchetypeStep
 */
def createProjectFromArchetypeStep() {
    printOpen('mkdir generated-client', EchoLevel.DEBUG)
    sendStageStartToAppPortal(clientInfo, asePipelineData, '040')
    sh 'if [ -d generated-client ]; then rm -rf generated-client; fi && mkdir generated-client'
    printOpen("Creating project ${aseClientArtifactId} from Archetype ${ASEVars.ARCHETYPE_ARTIFACT_ID}", EchoLevel.INFO)

    def cmd = "cd generated-client && export JAVA_HOME=${GlobalVars.JAVA_HOME_11} && mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Only_Maven_Settings> archetype:generate -DgroupId=${ASEVars.CLIENT_GROUP_ID} -DartifactId=${aseClientArtifactId} -Dversion=${aseClientVersion.toString()} -DarchetypeGroupId=${ASEVars.ARCHETYPE_GROUP_ID} -DarchetypeArtifactId=${ASEVars.ARCHETYPE_ARTIFACT_ID} -DarchetypeVersion=${archetypeLastVersion} -DServiceName=${serviceName} -Dservicename=${serviceName.toLowerCase()}"
    runMavenCommand(cmd)
    sh "cp ${yamlFilePath} generated-client/${aseClientArtifactId}/contract/swagger-micro-contract.yaml"

    sendStageEndToAppPortal(clientInfo, asePipelineData, '040')
}

/**
 * Stage deployingNewProjectStep
 */
def deployingNewProjectStep() {
    sendStageStartToAppPortal(clientInfo, asePipelineData, '050')
    def mvnLog = null
    printOpen("Deploying ${aseClientArtifactId} project", EchoLevel.INFO)

    def cmd = "cd generated-client/${aseClientArtifactId} && export JAVA_HOME=${GlobalVars.JAVA_HOME_11} && mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Only_Maven_Settings> clean deploy -Dmaven.install.skip -Dmaven.deploy.skip=false -DgenerateBodyErrorWrappingExceptions=${exceptionNameTemplate} -Dcontract.package=com.project.alm.arch.backend.ase.seconnectorstarter.${serviceName.toLowerCase()}"

    mvnLog = runMavenGoalWithRetries(pomXmlStructure, asePipelineData, cmd, [
        kpiAlmEvent: new KpiAlmEvent(
            pomXml, pipeline,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.MVN_DEPLOY_ASE_SERVICE_NEXUS)
    ])

    def artifactDeployedOnNexus = NexusUtils.extractArtifactsFromLog(mvnLog)
    asePipelineData.routesToNexus = artifactDeployedOnNexus

    printOpen("artifactDeployedOnNexus: ${artifactDeployedOnNexus}", EchoLevel.INFO)
    printOpen("versionWithoutQualifier: ${MavenVersionUtilities.getArtifactVersionWithoutQualifier(aseClientVersion)}", EchoLevel.INFO)

    //Requerimos el build id
    asePipelineData.buildCode = NexusUtils.getBuildId(artifactDeployedOnNexus, aseClientVersion)
    printOpen("El buildCode es ${asePipelineData.buildCode}", EchoLevel.INFO)
    sendStageEndToAppPortal(clientInfo, asePipelineData, '050')
}

/**
 * Stage publishArtifactCatalogStep
 */
def publishArtifactCatalogStep() {
    printOpen('pipelineData:', EchoLevel.ALL)
    asePipelineData.toString()
    printOpen('branchStructure:', EchoLevel.ALL)
    asePipelineData.branchStructure.toString()
    sendStageStartToAppPortal(clientInfo, asePipelineData, '060')
    printOpen('Publishing artifact to catalog', EchoLevel.ALL)
    publishArtifactInCatalog(asePipelineData, clientInfo)
    sendStageEndToAppPortal(clientInfo, asePipelineData, '060')
}

/**
 * Stage prepareResultForNextStep
 */
def prepareResultForNextStep() {
    sendStageStartToAppPortal(clientInfo, asePipelineData, '070')
    ASELifecycleProvisioningResultPipelineData pipelineResult = asePipelineData.pipelineStructure.resultPipelineData
    pipelineResult.yamlFilePath = yamlFilePath
    pipelineResult.artifactGroupId = ASEVars.CLIENT_GROUP_ID
    pipelineResult.artifactId = aseClientArtifactId
    pipelineResult.artifactVersion = aseClientVersion

    pipelineResult.originBranch = asePipelineData.branchStructure.getBranchName()
    pipelineResult.userEmail = asePipelineData.pushUserEmail
    pipelineResult.user = asePipelineData.pushUser

    pipelineResult.contractGitCommit = contractGitCommit

    sendStageEndToAppPortal(clientInfo, asePipelineData, '070')
}

/**
 * Stage endPipelineSuccessStep
 */
def endPipelineSuccessStep() {
    script { printOpen('Pipeline has succeeded', EchoLevel.INFO) }
    updateGitlabCommitStatus name: '999-globalBuild', state: 'success'
    successPipeline = true
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis - initCallStartMillis))
    }

    if (initAppPortal) {
        sendPipelineEndedToAppPortal(initAppPortal, clientInfo, asePipelineData, successPipeline)
        sendPipelineResultadoToAppPortal(initAppPortal, clientInfo, asePipelineData, successPipeline)
    }
    printOpen('ase-lib info:', EchoLevel.ALL)
    printOpen("ase-lib groupId : ${ASEVars.CLIENT_GROUP_ID}", EchoLevel.ALL)
    printOpen("ase-lib artifactId : ${aseClientArtifactId}", EchoLevel.ALL)
    printOpen("ase-lib artifactVersion : ${aseClientVersion}", EchoLevel.ALL)
    printOpen("ase-lib contractGitCommit : ${contractGitCommit}", EchoLevel.ALL)
}

/**
 * Stage endPipelineFailureStep
 */
def endPipelineFailureStep() {
    updateGitlabCommitStatus name: '999-globalBuild', state: 'failed'
    successPipeline = false
    printOpen('Pipeline has failed', EchoLevel.ERROR)
    if ( almEvent != null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis - initCallStartMillis))
    }

    if (initAppPortal) {
        sendPipelineEndedToAppPortal(initAppPortal, clientInfo, asePipelineData, successPipeline)
        sendPipelineResultadoToAppPortal(initAppPortal, clientInfo, asePipelineData, successPipeline)
    }
}

/**
 * Stage endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    printOpen('yamlFilePath: ', EchoLevel.ALL)
    printOpen('yamlFilePath?.trim() ', EchoLevel.ALL)

    attachPipelineLogsToBuild(clientInfo, asePipelineData)
    cleanWorkspace()
}

