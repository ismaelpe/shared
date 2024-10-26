import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

@Field String dataSourceFile
@Field String artifactType
@Field String artifactSubType
@Field String targetBranch
@Field String gitLabActionType
@Field String deployCloudPhases
@Field String resultDeployCloud

@Field String lastCommitId
@Field String lastCommit
@Field boolean initGpl
@Field boolean successPipeline
@Field boolean ifProceed

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData

@Field BranchStructure branchStructure
@Field def pipelineBehaviour
@Field CloudStateUtility cloudStateUtilitity

@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call() {
    dataSourceFile = ""
    deployCloudPhases = "01-pre-deploy"
    resultDeployCloud = "OK"

    ifProceed = true
    initGpl = false
    successPipeline = false
    
    cloudStateUtilitity = null
    almEvent = null	
    
    pipelineBehaviour = PipelineBehavior.LIKE_ALWAYS

    initCallStartMillis = new Date().getTime()
    
    pipeline {
		agent {	node (almJenkinsAgent('standard')) }
        options {
            gitLabConnection('gitlab')
            buildDiscarder(logRotator(numToKeepStr: '40'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
			JNKMSV = credentials('JNKMSV-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            SONAR_TOKEN = credentials('sonartoken')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
            executionProfile = "${pipelineParams ? pipelineParams.get('executionProfile', 'DEFAULT') : 'DEFAULT'}"         
            logsReport = true
            sendLogsToGpl = true
        }        
        stages {
            stage('get-git-code') {
                steps {
                    getGitRepoStep()
                }
            }
            stage('get-git-info') {
                steps {                   
                   getGitInfoStep()
                }
            }
            stage('initAndValidate') {
                when {
                    expression { ifProceed }
                }
                steps {
                   initAndValidateStep()
                }
            }
            stage('deploy-if-prototype'){
				when {
					expression { ifProceed && pipelineData.branchStructure.branchType == BranchType.PROTOTYPE }
				}
				steps {
					deployIfPrototypeStep()
				}
			}
			stage('deploy-ddl-bbdd'){
				when {
					expression { ifProceed && hasBBDD(pomXmlStructure,pipelineData,false) && almPipelineBuildStageDeployDdlBbdd.shouldExecute(pipelineData)}
				}
				steps {
                    deployDdlBBDDStep()
				}
			}
            stage('validate-dependencies-version') {
                when {
                    expression { ifProceed && !pipelineData.isPushCI() && !pipelineData.isRebaseOfARelease }
                }
                steps {
                    validateDependenciesVersionStep()
                }
            }
			stage('validate-dependencies-restrictions') {
				when {
					expression { ifProceed && !pipelineData.isPushCI() && !pipelineData.isRebaseOfARelease }
				}
				steps {
					validateDependenciesRestrictiondsVersionStep()
				}
			}
            stage('update-version') {
                when {
                    expression { ifProceed && !pipelineData.isRebaseOfARelease && (pipelineData.branchStructure.branchType == BranchType.RELEASE || pipelineData.branchStructure.branchType == BranchType.HOTFIX || pipelineData.branchStructure.branchType == BranchType.CONFIGFIX) && !pipelineData.isPushCI() && pipelineData.gitAction != 'MERGE' && pomXmlStructure.isRCVersion() }
                }
                steps {
                   updateVersionStep()
                }
            }
            stage('validate-version') {
                when {
                    expression { ifProceed && !pipelineData.isRebaseOfARelease && !pipelineData.isPushCI() && pomXmlStructure.artifactSubType == ArtifactSubType.MICRO_APP }
                }
                steps {            
                    validateVersionStep()        
                }
            }
            stage('build') {
                when {
                    expression { ifProceed && !pipelineData.isPushCI() }
                }
                steps {
                    buildStep()
                }
            }
            stage('error-translations') {
                when {
                    expression { ifProceed && !pipelineData.isPushCI() }
                }
                steps {                   
                    errorTranslationsStep()
                }
            }          
            stage('push-release-to-git') {
                when {
                    expression { ifProceed && !pipelineData.isPushCI() && ((pipelineData.branchStructure.branchType == BranchType.RELEASE && pomXmlStructure.isRCVersion()) || (pipelineData.branchStructure.branchType == BranchType.HOTFIX && pomXmlStructure.isRCVersion()) || (pipelineData.branchStructure.branchType == BranchType.CONFIGFIX && pomXmlStructure.isRCVersion())) }
                }
                steps {
                    pushReleaseToGitStep()
                }
            }
            stage('deploy-micro-artifactory-cloud') {
                when {
                    expression { ifProceed && !pipelineData.isPushCI() }
                }
                steps {
                    deployMicroArtifactoryCloudStep()
                }
            }
            stage('copy-config-files') {
                when {
                    expression { ifProceed && !pipelineData.isPushCI() && (pipelineData.deployFlag || pipelineData.branchStructure.branchType == BranchType.CONFIGFIX) && pomXmlStructure.isMicro() && (pipelineData.bmxStructure.usesConfigServer() || pipelineData.literalsFlag || pipelineData.garArtifactType == GarAppType.DATA_SERVICE || pipelineData.garArtifactType == GarAppType.ARCH_MICRO) && !pipelineData.isRebaseOfARelease }
                }
                steps {
                    copyConfigFilesStep()
                }
            }
            stage("deploy-to-cloud") {
				when {
					expression { ifProceed && !pipelineData.isPushCI() && !pipelineData.isRebaseOfARelease }
				}
                steps {
					deployToCloudStep()
                }
            }
			stage('refresh-properties-configuration'){
				when {
					expression { ifProceed && !pipelineData.isPushCI() && pipelineData.branchStructure.branchType == BranchType.CONFIGFIX }
				}
				steps {	
                    refreshConfigurationStep()
				}
			}
			stage("consolidate-new-deploy") {
				when{
					//Validamos que el micro no este en DEV o que sea una sampleapp pendiente de borrar
					expression { ifProceed && resultDeployCloud=="OK" && !pipelineData.isPushCI() && !pipelineData.isRebaseOfARelease && (pomXmlStructure.artifactSampleApp!="" || (pipelineData.branchStructure.branchType != BranchType.MASTER && pipelineData.branchStructure.branchType != BranchType.FEATURE)) }
				}
				steps {
                    consolidateNewDeployStep()
				}
			}
			stage('clone-to-ocp') {
				when{
					expression { ifProceed && resultDeployCloud=="OK" && !pipelineData.isPushCI() && !pipelineData.isRebaseOfARelease  && pipelineData.branchStructure.branchType != BranchType.MASTER && pipelineData.branchStructure.branchType != BranchType.FEATURE }
				}
				steps {
                    cloneToOCPStep()
				}
			}/*
			stage('changelog-file') {
                when {
                    expression { pipelineData?.requiresChangelogPush() && ifProceed && !pipelineData.isPushCI() && pipelineParams.changelog && pipelineData.branchStructure.branchType == BranchType.MASTER && BranchType.FEATURE == pipelineData.originPushToMaster && "PUSH" == pipelineData.gitAction }
                }
                steps {
                    changelogFileStep()
                }
            }*/
            stage('deploy-nexus') {
                when {
                    expression { ifProceed && !pipelineData.isPushCI() && pipelineData.branchStructure.branchType != BranchType.CONFIGFIX }
                }
                steps {
                    deployArtifactoryStep()
                }
            }
            stage('publish-client') {
                when {
                    expression { ifProceed && !pipelineData.isRebaseOfARelease && !pipelineData.isPushCI() && pomXmlStructure.artifactSubType == ArtifactSubType.MICRO_APP && !pomXmlStructure.isRelease() && pipelineData.branchStructure.branchType != BranchType.CONFIGFIX && pipelineData.branchStructure.branchType != BranchType.FEATURE }
                }
                steps {
                    publishClientStep()
                }
            }
            stage('Generate archetype from project') {
                when {
                    expression { ifProceed && pipelineParams.get('isArchetype', false) && pipelineParams.get('archetypeModel')?.trim() && !pipelineData.isPushCI() }
                }
                steps {
                    generateArchetypeFromProjectStep()
                }
            }
            stage('Deploy archetype into Nexus') {
                when {
                    expression { ifProceed && pipelineParams.get('isArchetype', false) && pipelineParams.get('archetypeModel')?.trim() && !pipelineData.isPushCI() }
                }
                steps {
                    deployArchetypeIntoArtifactoryStep()
                }
            }
            stage('generate-report') {
                when {
                    expression { ifProceed && !pipelineData.isPushCI() }
                }
                steps {
                    generateReportStep()
                }
            }
            stage('publish-artifact-catalog') {
                when {
                    expression { ifProceed && GlobalVars.GSA_ENABLED && !pipelineData.isRebaseOfARelease && !pipelineData.isPushCI() && pipelineData.branchStructure.branchType != BranchType.FEATURE }
                }
                steps {
                    publishCatalogStep()
                }
            }
            stage('apimanager-technicalservices-registration') {
                when {
                    expression { ifApiManagerTechnicalServiceRegistrationApplies(pipelineData, pomXmlStructure) }
                }
                steps {
                    apiManagerTechnicalServiceRegistrationStep()
                }
            }
            stage('promote-mule-contract') {
                when {
                    expression { pomXmlStructure.contractVersion }
                }
                steps {
                    promoteMuleContractStep()
                }
            }
            stage('generate-logs-report') {
                steps {
                    generateLogReportsStep()
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
 * Stage 'get-git-code'
 */
def getGitRepoStep() {
    pipelineParams = [:]
    
    printOpen("Pipeline logs:\n$GlobalVars.PIPELINE_LOGS", EchoLevel.DEBUG)

    // Checkout project code...
    def git = new GitRepositoryHandler(this, params.pathToRepoParam, [gitProjectRelativePath: '.',verbose:true]).fastInitializeAndCheckout(params.originBranchParam)
    
    lastCommitId = git.getLastCommitId("origin/$params.originBranchParam")
	lastCommit = git.getLastCommitLog("origin/$params.originBranchParam")

    getGitRemoteInfo(lastCommitId)

    pipelineParams = Utilities.parseJenkinsFilePipelineParams(this, readFile("Jenkinsfile"), pipelineParams)
}

/**
 * Stage 'get-git-info'
 */
def getGitInfoStep() {
    initGlobalVars(pipelineParams)

    branchStructure = getBranchInfo(env.gitlabSourceBranch)
    pipelineData = getInfoGit(PipelineStructureType.NO_CI, branchStructure.branchType)
   
    pipelineData.jenkinsFileParams = pipelineParams

    pipelineParameters = pipelineParams?.get('mvnAdditionalParameters')
    if (pipelineParameters != null) {
        pipelineData.mvnAdditionalParameters = pipelineParams.get('mvnAdditionalParameters')
    }

    mvnAdditionalParameterEnv = "${env.MAVEN_ADITIONAL_PARAMS}"
    if (mvnAdditionalParameterEnv != null) {
        pipelineData.mvnAdditionalParameters.add(mvnAdditionalParameterEnv)
    }

    // Fin IPE: Enable Aditional parameters Logs for maven
    // call init pipeline with alwaysDeploy flag
	printOpen("Init LasCommitLog", EchoLevel.DEBUG)
	printOpen("Git Commit Log: $lastCommit", EchoLevel.DEBUG)
	printOpen("End LasCommitLog", EchoLevel.DEBUG)
	//Vamos a remplazar el CI_Pipeline
	if (lastCommit!=null && lastCommit.contains(GlobalVars.GIT_TAG_CI_PUSH)) {
		lastCommit=lastCommit.replaceAll('CI_Pipeline','JENKINS_PROCES')
	}
	printOpen("Git Commit Log: $lastCommit", EchoLevel.DEBUG)
	pipelineData.commitLog = lastCommit
	pipelineData.init(branchStructure, pipelineParams.subType, pipelineParams.type, pipelineParams.get('isArchetype', false), pipelineParams.get('archetypeModel', './'))
    pipelineData.commitId = lastCommitId	
    pipelineData.prepareExecutionMode(pipelineParams.get('executionProfile', 'DEFAULT'), pipelineParams.get('almFolder'), true);
    pipelineData.setDefaultAgent(pipelineParams ? pipelineParams.get('agent', 'standard') : 'standard')
    pipelineData.undeploySampleApp = false
   
    printOpen("Git CommitId: $lastCommitId", EchoLevel.DEBUG)
    printOpen("pipelineParams: $pipelineParams", EchoLevel.DEBUG)
	printOpen("pipelineData: $pipelineData.deployFlag", EchoLevel.DEBUG)
	
    //Vamos a settear del deploy Flag a true
	pipelineData.deployFlag=true
	
    printOpen( "isArchetype    is " + pipelineParams.get('isArchetype', false) +
        "\narchetypeModel is " + pipelineParams.get('archetypeModel') +
        "\ndomain         is ${pipelineData.domain}" +
        "\nsubDomain      is ${pipelineData.subDomain}" +
        "\ncompany        is ${pipelineData.company}",
        EchoLevel.DEBUG)

    if (PipelineBehavior.COMMITLOG_REQUESTED_NO_CI != pipelineData.pipelineBehavior && pipelineData.branchStructure.branchType == BranchType.FEATURE) {
        pipelineBehaviour = setPipelineFeatureBehaviour(pipelineData,pomXmlStructure)
    }

    if (pipelineData.commitLog == GlobalVars.GIT_TAG_CI_PUSH || pipelineData.isPushCI()) {
        printOpen("NO Enviamos el init al GitLab", EchoLevel.DEBUG)
        sendToGitLab = false
        if (pipelineBehaviour == PipelineBehavior.NOT_FIRST_MR) {
            updateCommitStatus(pipelineData, pomXmlStructure, 'running')
        }

    } else {
        printOpen("Enviamos el init al GitLab", EchoLevel.DEBUG)
        if (pipelineBehaviour == PipelineBehavior.NOT_FIRST_MR) {
            updateCommitStatus(pipelineData, pomXmlStructure, 'running')
        }
    }

    printOpen("Execution Mode: ${pipelineData.getExecutionMode()}", EchoLevel.DEBUG)
}

/**
 * Stage 'initAndValidate'
 */
def initAndValidateStep() {
    pomXmlStructure = analizePomXml(pipelineParams.type, pipelineParams.subType)

    // Comprobamos si hemos cargado del metodo anterior las propiedades del Jenkins
    pipelineData.mvnMuleParameters = loadMuleMvnParameters(pipelineData, pomXmlStructure)

    //Ya se dispone de la información necesaria para KPIs
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_STARTED, KpiLifeCycleStatus.OK)
    if (pomXmlStructure.isRelease()) {
        ifProceed = false
    }

    almEvent = new KpiAlmEvent(
        pomXmlStructure, pipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_BUILD)

    // Comprobamos si es release o hotfix y que la versión del POM no sea ni SNAPSHOT ni RC
    if (pipelineData.branchStructure.branchType == BranchType.RELEASE && pipelineData.branchStructure.branchName.startsWith('release/BBDD')) {
        //Vamos a validar si es una rama de tipo BBDD 
        //IOP Administrativa
        pipelineData.commitLog = GlobalVars.GIT_TAG_CI_PUSH
        pipelineData.deployFlag = false
        printOpen("Es una RC sobre una administrativa de BBDD", EchoLevel.INFO)
        
    }
    if (!MavenVersionUtilities.isSNAPSHOT(pomXmlStructure.artifactVersion) && !MavenVersionUtilities.isRCVersion(pomXmlStructure.artifactVersion) && (pipelineData.branchStructure.branchType == BranchType.RELEASE || pipelineData.branchStructure.branchType == BranchType.HOTFIX)) {
        // marcamos los flags que se usan para interpretar que sea CI para que no haga los siguientes stages
        pipelineData.commitLog = GlobalVars.GIT_TAG_CI_PUSH
        pipelineData.deployFlag = false
    } else {
        calculateArchVersionWithModules(pomXmlStructure)
        calculateBuildCode(pomXmlStructure, pipelineData)
        sendPipelineStartToGPL(pomXmlStructure, pipelineData, '')
        initGpl = true
        initAndValidate(pomXmlStructure,pipelineData,pipelineParams,branchStructure)
    }

    if (pipelineData.isRebaseOfARelease) {
        //Estamos en un push a una rama de release si el evento es un MERGE
        //Si estamos ante un PUSH no tneemos que hacer nada
        if (pipelineData.gitAction!=null &&  'MERGE'.equals(pipelineData.gitAction)) {
            printOpen("This is a MERGE event against a RELEASE branch", EchoLevel.INFO)
            updateCommitStatus(pipelineData, pomXmlStructure, 'running')
        }else {
            pipelineData.commitLog = GlobalVars.GIT_TAG_CI_PUSH
            pipelineData.deployFlag = false
        }
    }

}

/**
 * Stage 'deploy-if-prototype'
 */
def deployIfPrototypeStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "105")
    printOpen("Deploy prototype", EchoLevel.INFO)
    try {
        if (pipelineData.deployFlag) {
            deployNexus(pomXmlStructure, pipelineData)
            
            def result = deployPrototypeToKubernetes(pomXmlStructure,pipelineData)
            def messageUrl = "<a href='$result.urlPrototype'>$result.urlPrototype</a>"
            
            printOpen("Result deploy prototype: $result.messageDeploy", EchoLevel.INFO)
            printOpen("Prototype url: ${messageUrl}", EchoLevel.INFO)

            sendStageEndToGPL(pomXmlStructure, pipelineData, "105", null, pipelineData.bmxStructure.environment)
        }else {
            printOpen("We are doing nothing since there is no deploy flag", EchoLevel.INFO)
            sendStageEndToGPL(pomXmlStructure, pipelineData, "105")
        }
        ifProceed = false

    } catch (Exception e) {
        printOpen("Error deploying prototype: ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "105", null, null, "error")
        throw e
    }
}

/**
 * Stage 'deploy-ddl-bbdd'
 */
def deployDdlBBDDStep() {
    almPipelineBuildStageDeployDdlBbdd(pipelineData, pomXmlStructure)
}

/**
 * Stage 'validate-dependencies-version'
 */
def validateDependenciesVersionStep() {
    almPipelineStageValidateDependenciesVersion(pomXmlStructure, pipelineData, "200")
}

/**
 * Stage 'validate-dependencies-restrictions'
 */
def validateDependenciesRestrictiondsVersionStep() {
    almPipelineStageValidateDependencyRestrictions(pomXmlStructure, pipelineData, "250")
}

/**
 * Stage 'update-version'
 */
def updateVersionStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "300")
    updateVersion(pomXmlStructure)
    if (pomXmlStructure.isRCVersion()) {
        pipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()
    }
    sendStageEndToGPL(pomXmlStructure, pipelineData, "300")
}

/**
 * Stage 'validate-version'
 */
def validateVersionStep() {
    almPipelineStageValidateVersion(pomXmlStructure, pipelineData, "400")
}

/**
 * Stage 'build'
 */
def buildStep() {
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.BUILD_STARTED, KpiLifeCycleStatus.OK)

    sendStageStartToGPL(pomXmlStructure, pipelineData, "410")
 
    try {
        printOpen("Execution Mode: ${pipelineData.getExecutionMode()}", EchoLevel.DEBUG)

        buildWorkspace(pomXmlStructure, pipelineData)
    } catch (Exception e) {
        kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.BUILD_FINISHED, KpiLifeCycleStatus.KO)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "410", null, null, "error")
        throw e
    }
 
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.BUILD_FINISHED, KpiLifeCycleStatus.OK)

    sendStageEndToGPL(pomXmlStructure, pipelineData, "410")
}

/**
 * Stage 'error-translations'
 */
def errorTranslationsStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "411")
 
    pipelineData.literalsFlag = litmidTranslations(pomXmlStructure, pipelineData)
 
    sendStageEndToGPL(pomXmlStructure, pipelineData, "411")
}

/**
 * Stage 'push-release-to-git'
 */
def pushReleaseToGitStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "414")
    tagVersion(pomXmlStructure, pipelineData, false, true)
    pushRepo(pomXmlStructure, pipelineData)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "414")
}

/**
 * Stage 'deploy-micro-artifactory-cloud'
 */
def deployMicroArtifactoryCloudStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "415")
    
    if (existsArtifactDeployed(pomXmlStructure,pipelineData)) {
        printOpen("We have found that Nexus already has the artifact. We'll not try to deploy.", EchoLevel.INFO)
    }else {
        deployMicrosNexus(pomXmlStructure, pipelineData)
    }
    
    sendStageEndToGPL(pomXmlStructure, pipelineData, "415")
}

/**
 * Stage 'copy-config-files'
 */
def copyConfigFilesStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "420")

    try {
        pushConfigFiles(pomXmlStructure, pipelineData, false, (pipelineData.bmxStructure.usesConfigServer() || pipelineData.literalsFlag))
        sendStageEndToGPL(pomXmlStructure, pipelineData, "420")
    } catch (Exception e) {
        printOpen("Error copying configuration files: ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "420", null, null, "error")
        throw e
    }
}

/**
 * Stage 'deploy-to-cloud'
 */
def deployToCloudStep() {
    cloudStateUtilitity = almPipelineStageDeployToCloud(pomXmlStructure, pipelineData, "501", "01-<phase>-deploy")
}

/**
 * Stage 'refresh-properties-configuration'
 */
def refreshConfigurationStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "502")
    refreshConfigurationViaRefreshBus(pomXmlStructure,pipelineData, '1')
    refreshConfigurationViaRefreshBus(pomXmlStructure,pipelineData, '2')
    sendStageEndToGPL(pomXmlStructure, pipelineData, "502")
}

/**
 * Stage 'consolidate-new-deploy'
 */
def consolidateNewDeployStep() {
    almPipelineStageConsolidateNewDeploy(pomXmlStructure, pipelineData, "506", "04-<phase>-consolidateNew", cloudStateUtilitity)
}

/**
 * Stage 'clone-to-ocp'
 */
def cloneToOCPStep() {
	almPipelineStageCloneToOcp(pomXmlStructure, pipelineData)
}

/**
 * Stage 'changelog-file'
 */
def changelogFileStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "510")
    if (createChangeLogFromMergerRequestAPI(pipelineData, pomXmlStructure)) {
        printOpen("Creating a new entry at CHANGELOG.md file.", EchoLevel.INFO)
        pushRepoWithMessage(pomXmlStructure, pipelineData, GlobalVars.GIT_TAG_CI_PUSH + " Adding new entry in changelog due to merge request", "${WORKSPACE}/CHANGELOG.md")
    }
    printOpen("pipelineData: " + pipelineData +
        "\nbranchStructure: " + branchStructure +
        "\ntargetBranch: " + targetBranch,
        EchoLevel.DEBUG)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "510")
}

/**
 * Stage 'deploy-nexus'
 */
def deployArtifactoryStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "525")
    deployNexus(pomXmlStructure, pipelineData)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "525")
    sendPipelineUpdateToGPL(initGpl, pomXmlStructure, pipelineData, '')
}

/**
 * Stage 'publish-client'
 */
def publishClientStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "530")
    publishClient(pomXmlStructure, pipelineData)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "530")
}

/**
 * Stage 'Generate archetype from project'
 */
def generateArchetypeFromProjectStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "540")
    generateArchetypeFromProject(pipelineParams.get('archetypeModel'))
    sendStageEndToGPL(pomXmlStructure, pipelineData, "540")
}

/**
 * Stage 'Deploy archetype into Nexus'
 */
def deployArchetypeIntoArtifactoryStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "550")
    deployArchetypeIntoNexus(pipelineParams.get('archetypeModel'), pomXmlStructure)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "550")
}

/**
 * Stage 'generate-report'
 */
def generateReportStep() {              
    sendStageStartToGPL(pomXmlStructure, pipelineData, "600")
    debugInfo(pipelineParams, pomXmlStructure, pipelineData)
    archiveWorkspaceIfNeeded(pomXmlStructure, pipelineData)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "600")
}

/**
 * Stage 'publish-artifact-catalog'
 */
def publishCatalogStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "910")     
    pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName)
    // Actualizamos el build path ya que estamos en el build ondemand
    // esto nos valdrá para ir actualizando el dato de las aplicaciones que aun no tiene este dato.
    publishArtifactInCatalog(pipelineData, pomXmlStructure, cloudStateUtilitity)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "910")
}

/**
 * Stage 'apimanager-technicalservices-registration'
 */
def apiManagerTechnicalServiceRegistrationStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "920")
 
    printOpen("Publishing swagger contract to API Manager (adpbdd-micro)", EchoLevel.INFO)
    publishSwaggerContract2ApiManager(pipelineData, pomXmlStructure)

    sendStageEndToGPL(pomXmlStructure, pipelineData, "920")
}

/**
 * Stage 'promote-mule-contract'
 */
def promoteMuleContractStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "930")
 
    printOpen("Promote swagger contract to MuleSoft", EchoLevel.INFO)
    def result = promoteContract2MuleSoft(pipelineData, pomXmlStructure)

    sendStageEndToGPL(pomXmlStructure, pipelineData, "930", result , null, result ? "warning" : "ended")
}

/**
 * Stage 'generate-logs-report'
 */
def generateLogReportsStep() {
    echo "Pipeline logs:\n${GlobalVars.PIPELINE_LOGS}"
}

/**
 * Stage
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}

/**
 * Stage
 */
def endPipelineSuccessStep() {
    printOpen("SUCCESS", EchoLevel.INFO)
    successPipeline = true
    
    if (pipelineBehaviour == PipelineBehavior.PUSH_OPENED_MR && sendToGitLab == true) {
        printOpen("No enviamos final state running", EchoLevel.ALL)
        //updateGitlabCommitStatus name: '999-globalBuild', state: 'running'
    } else {
        if (pipelineBehaviour == PipelineBehavior.NOT_FIRST_MR) {
            try {
                updateCommitStatus(pipelineData, pomXmlStructure, 'success')
            } catch (Exception e) {
                printOpen("ERROR en el update de gitlab ${e}",EchoLevel.ERROR)
                successPipeline = false
            }
        }
    }

    if ( almEvent!=null) {
         long endCallStartMillis = new Date().getTime()
         
         if (successPipeline) {
             kpiLogger(almEvent.pipelineSuccess(endCallStartMillis-initCallStartMillis))
         }else {
             kpiLogger(almEvent.pipelineFail(endCallStartMillis-initCallStartMillis))
         }
    }
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, pipelineData?.pipelineEndsWithWarning ? "warning" : "ended")    
    if (pipelineData.getExecutionMode().invokeNextActionAuto() && "MERGE" != pipelineData.gitAction) {
        printOpen("Modo test activado en fase de build", EchoLevel.INFO)
        invokeNextJob(pipelineData, pomXmlStructure)
    }
    if (successPipeline) {
        kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.OK)
    }else {
        kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.KO)
    }
}

/**
 * Stage 
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("FAILURE", EchoLevel.INFO)

    //FIXME jplatasv DE53170 - Resolver el tema del null gitUrl al hacer sendPipelineStartToGPL cuando reproduzcamos el error
    debugInfo(pipelineParams, pomXmlStructure, pipelineData)

    if (initGpl == false) {
        //init pipeline in GPL with minimun parameters
        sendPipelineStartToGPL(pipelineData, pipelineParams)
        initGpl = true
    }
    
    if ( almEvent!=null) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineFail(endCallStartMillis-initCallStartMillis))                        
    }
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)

    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.KO)
    
    if (pipelineData.getExecutionMode().isNotifyforFailureNeeded()) {
        //Mando email
        def mapEmailFields = Utilities.getEmailTemplate(this, [
            "type": "updateCoreNocturneJobInBuildFailure",
            "gitUrl": pipelineData.gitUrl,
            "branchName": pipelineData.branchStructure.branchName
        ])
        mail to: GlobalVars.EMAIL_REPORT,
            subject: mapEmailFields.subject,
            body: mapEmailFields.body
    }
}

/**
 * Obtiene la información del commit por si esta involucrado en algún commit
 */
def getGitRemoteInfo(commitId) {
    def pathToRepoParamEncoded = URLEncoder.encode(pathToRepoParam.substring(GlobalVars.gitlabDomain.length(), params.pathToRepoParam.length() - 4), "UTF-8")

    def gitProjectApiUrl = "$GlobalVars.gitlabApiDomain$pathToRepoParamEncoded"
    
    //def hasOpenedMR = sendRequestToGitLabAPI([httpMode: "GET", url: "$gitProjectApiUrl/merge_requests?source_branch=$pipelineParams.projectinfo.originBranchParamEncoded"])
    def hasOpenedMR = sendRequestToGitLabAPI([httpMode: "GET", url: "$gitProjectApiUrl/repository/commits/$commitId/merge_requests"])

    if (hasOpenedMR.status == 200) {  
        // The endpoint above always return a list empty or fully
        def openedMRList = hasOpenedMR.asJson.findAll{it -> it.state == "opened" }
        
        if (openedMRList.size() > 0) {
            // Setup Gitlab - From MR
            setUpGitLabEnvVars(gitProjectApiUrl, params, openedMRList.first(), true)
        } else {
            // Setup Gitlab - PUSH
            setUpGitLabEnvVars(gitProjectApiUrl, params, null, false)
        }
    } else {
        // Setup Gitlab - PUSH
        setUpGitLabEnvVars(gitProjectApiUrl, params, null, false)      
    }
}
