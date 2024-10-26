import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

@Field String dataSourceFile
@Field String artifactType
@Field String artifactSubType
@Field String targetBranch
@Field String gitLabActionType
@Field boolean initGpl
@Field boolean successPipeline

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData

@Field BranchStructure branchStructure
@Field PipelineBehavior pipelineBehaviour

@Field boolean sendToGitLab

//Pipeline unico que construye los artefactos de tipo config
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    dataSourceFile = ""
    initGpl = false
    successPipeline = false
    pipelineBehaviour = PipelineBehavior.LIKE_ALWAYS
    sendToGitLab = true
    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
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
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
            executionProfile = "${pipelineParams ? pipelineParams.get('executionProfile', 'DEFAULT') : 'DEFAULT'}"
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
            stage('initAndValidate') {
                when {
                    expression { !pipelineData.isPushCI() }
                }
                steps {
                    initAndValidateStep()
                }
            }
			stage('update-version') {
				when {
					expression { !pipelineData.isRebaseOfARelease && (pipelineData.branchStructure.branchType == BranchType.RELEASE || pipelineData.branchStructure.branchType == BranchType.HOTFIX || pipelineData.branchStructure.branchType == BranchType.CONFIGFIX) && !pipelineData.isPushCI() && pipelineData.gitAction != 'MERGE' }
				}
				steps {
					updateVersionStep()
				}
			}
            stage('copy-config-files') {
                when {
                    expression { !pipelineData.isPushCI() && pipelineData.deployFlag && pomXmlStructure.isConfigProject() && !pipelineData.isRebaseOfARelease && (pipelineData.branchStructure.branchType == BranchType.HOTFIX || pipelineData.branchStructure.branchType == BranchType.MASTER || pipelineData.branchStructure.branchType == BranchType.RELEASE) }
                }
                steps {
					copyConfigFilesStep()
                }
            }			
			stage('refresh-micros') {
				when {
					expression { !pipelineData.isPushCI() && pipelineData.deployFlag && pomXmlStructure.isConfigProject() && !pipelineData.isRebaseOfARelease && (pipelineData.branchStructure.branchType == BranchType.HOTFIX || pipelineData.branchStructure.branchType == BranchType.MASTER || pipelineData.branchStructure.branchType == BranchType.RELEASE) }
				}
				steps {
					refreshMicrosStep()
				}
			}
            stage('changelog-file') {
                when {
                    expression { !pipelineData.isPushCI() && pipelineParams.changelog && pipelineData.branchStructure.branchType == BranchType.MASTER && BranchType.FEATURE == pipelineData.originPushToMaster && "PUSH" == pipelineData.gitAction }
                }
                steps {
                   changelogFileStep()
                }
            }
            stage('push-release-to-git') {
                when {
                    expression { !pipelineData.isPushCI() && ((pipelineData.branchStructure.branchType == BranchType.RELEASE && pomXmlStructure.isRCVersion()) || (pipelineData.branchStructure.branchType == BranchType.HOTFIX && pomXmlStructure.isRCVersion())) }
                }
                steps {
                   pushReleaseToGitStep()
                }
            }
            stage('deploy-artifactory') {
                when {
                    expression { !pipelineData.isPushCI() && pipelineData.deployFlag && pomXmlStructure.isConfigProject() && !pipelineData.isRebaseOfARelease && (pipelineData.branchStructure.branchType == BranchType.HOTFIX || pipelineData.branchStructure.branchType == BranchType.MASTER || pipelineData.branchStructure.branchType == BranchType.RELEASE) }
                }
                steps {
                    deployArtifactoryStep()
                }
            }
            stage('publish-artifact-catalog') {
                when {
                    expression { GlobalVars.GSA_ENABLED && !pipelineData.isRebaseOfARelease && !pipelineData.isPushCI() && pipelineData.branchStructure.branchType != BranchType.FEATURE }
                }
                steps {
                   publishArtifactCatalogStep()
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
 * Stage 'getGitCodeStep'
 */
 def getGitCodeStep() {
    printOpen("env.gitlabMergeRequestLastCommit: ${env.gitlabMergeRequestLastCommit}", EchoLevel.INFO)
    new GitUtils(this, false).updateGitCode("${env.gitlabMergeRequestLastCommit}")
    printOpen("pipelineParams: ${pipelineParams.toString()}", EchoLevel.INFO)
    printOpen("Jenkinsfile is:", EchoLevel.INFO)
    sh "cat Jenkinsfile"
}

/**
 * Stage 'getGitInfoStep'
 */
def getGitInfoStep(){
    initGlobalVars(pipelineParams)
    //Which is the last commit message
    pipelineData = getInfoGit(PipelineStructureType.CI_CONFIGS)
    branchStructure = getBranchInfo()
    pipelineData.mvnAdditionalParameters = pipelineParams.get('mvnAdditionalParameters')
    pipelineData.init(branchStructure, pipelineParams.subType, pipelineParams.type, pipelineParams.get('isArchetype', false), pipelineParams.get('archetypeModel', './'))
    printOpen("log pipeline after init ${pipelineData.toString()}", EchoLevel.ALL)
    pipelineData.prepareExecutionMode(env.executionProfile, pipelineParams.get('almFolder'))
    pipelineData.setDefaultAgent(pipelineParams ? pipelineParams.get('agent', 'standard') : 'standard')
    currentBuild.displayName = "Build_${env.BUILD_ID}_" + pipelineData.getPipelineBuildName() + (pipelineData.isPushCI() ? "_CI" : "")


    if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {
        //Vamos a ver que queremos
        //Si es push o merge

        //Si es push con merge request no tenemos que hacer nada
        //Si es merge de una rama de feature tenemos que hacer todo
        if (PipelineBehavior.COMMITLOG_REQUESTED_NO_CI != pipelineData.pipelineBehavior) {
            pipelineBehaviour = validateMR(pipelineData, pomXmlStructure)
            pipelineData.pipelineBehavior = pipelineBehaviour
        }

        env.pipelineBehavior = pipelineBehaviour
        printOpen("Resultado de MR Abiertas ${pipelineBehaviour} ${pipelineData.gitAction}", EchoLevel.ALL)
        if (pipelineBehaviour == PipelineBehavior.PUSH_OPENED_MR || pipelineBehaviour == PipelineBehavior.COMMITLOG_REQUESTED_NO_CI) {
            //No vamos a hacer nada de nada
            pipelineData.deployFlag = false
            pipelineData.commitLog = GlobalVars.GIT_TAG_CI_PUSH
        } else if (pipelineBehaviour == PipelineBehavior.NOT_FIRST_MR) {
            //Este debe hacer deploy
            pipelineData.deployFlag = true
        }
    }

    if (pipelineData.commitLog == GlobalVars.GIT_TAG_CI_PUSH || pipelineData.isPushCI()) {
        printOpen("NO Enviamos el init al GitLab", EchoLevel.INFO)
        sendToGitLab = false

        if (pipelineBehaviour == PipelineBehavior.NOT_FIRST_MR) {
            updateCommitStatus(pipelineData, 'running')
        }

    } else {
        printOpen("Enviamos el init al GitLab", EchoLevel.ALL)

        if (pipelineBehaviour == PipelineBehavior.NOT_FIRST_MR) {
            updateCommitStatus(pipelineData, 'running')
        }
    }
}

/**
 * Stage 'initAndValidateStep'
 */
def initAndValidateStep() {          
    pomXmlStructure = analizePomXml(pipelineParams.type, pipelineParams.subType)
    //					    Comprobamos si es release o hotfix y que la versión del POM no sea ni SNAPSHOT ni RC
    if (!MavenVersionUtilities.isSNAPSHOT(pomXmlStructure.artifactVersion) && !MavenVersionUtilities.isRCVersion(pomXmlStructure.artifactVersion) && (pipelineData.branchStructure.branchType == BranchType.RELEASE || pipelineData.branchStructure.branchType == BranchType.HOTFIX)) {
    //							marcamos los flags que se usan para interpretar que sea CI para que no haga los siguientes stages
        pipelineData.commitLog = GlobalVars.GIT_TAG_CI_PUSH
        pipelineData.deployFlag = false
    } else {
        calculateBuildCode(pomXmlStructure, pipelineData)

        sendPipelineStartToGPL(pomXmlStructure, pipelineData, '')
        initGpl = true

        sendStageStartToGPL(pomXmlStructure, pipelineData, "100")

        currentBuild.displayName = "Build_${env.BUILD_ID}_" + pipelineData.getPipelineBuildName()
        debugInfo(pipelineParams, pomXmlStructure, pipelineData)

        try {
            printOpen("The environment is ${pipelineData.bmxStructure.environment}", EchoLevel.ALL)
            printOpen("Check vars:", EchoLevel.ALL)
            printOpen("${CicsVars.AGILEWORKS_VALIDATION_ENABLED} | ${pipelineData.branchStructure.branchType} | ${pipelineData.isBpiRepo()} | ${pipelineData.isBpiArchRepo()}", EchoLevel.ALL)
            if (CicsVars.AGILEWORKS_VALIDATION_ENABLED && pipelineData.branchStructure.branchType == BranchType.FEATURE && (pipelineData.isBpiRepo() || pipelineData.isBpiArchRepo())) {
                if (!pipelineData.getExecutionMode().skipAgileworksValidation()) {
                    sendAgileWorkAuthFeatureToGPL(pomXmlStructure, pipelineData, GlobalVars.DEV_ENVIRONMENT, null, "${branchStructure.featureNumber}", pipelineData.getPushUserEmail())
                } else {
                    printOpen("AgileWork skipped by Execution Profile: ${pipelineData.executionProfileName}", EchoLevel.ALL)
                }
            } else {
                printOpen("Not a BPI artifact. AgileWork will not be checked.", EchoLevel.ALL)
            }
        } catch (Exception e) {
            sendStageEndToGPL(pomXmlStructure, pipelineData, "100", e.getMessage(), null, "error")
            throw e
        }

        if (pomXmlStructure.isRelease() && (pipelineData.branchStructure.branchType == BranchType.RELEASE || pipelineData.branchStructure.branchType == BranchType.HOTFIX)) {
            //Es una MR de un cierre de release. No puede ser otra cosa
            //No se debe hacer deploy de ningun modo
            pipelineData.deployFlag = false
        }

        if (pomXmlStructure.isSNAPSHOT() && (pipelineData.branchStructure.branchType == BranchType.RELEASE || pipelineData.branchStructure.branchType == BranchType.HOTFIX)) {
            pipelineData.deployFlag = false
            pipelineData.isRebaseOfARelease = true
            pipelineData.pipelineStructure.resultPipelineData = new FeatureResultPipelineData(GlobalVars.TST_ENVIRONMENT, pipelineData.gitUrl, pipelineData.gitProject, false)
            pipelineData.pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(params)
        } else {
            try {
                validateThatUserHasNotChangedArtifactVersions(pipelineData, pomXmlStructure)
                validateBranch(pomXmlStructure.getArtifactVersionWithoutQualifier(), pipelineData.branchStructure)
                if (CicsVars.AGILEWORKS_VALIDATION_ENABLED &&
                    pipelineData.branchStructure.branchType == BranchType.FEATURE &&
                    (pipelineData.isBpiRepo() || pipelineData.isBpiArchRepo())) {
                    if (!pipelineData.getExecutionMode().skipAgileworksValidation()) {
                        sendAgileWorkAuthFeatureToGPL(pomXmlStructure, pipelineData, GlobalVars.DEV_ENVIRONMENT, null, "${branchStructure.featureNumber}", pipelineData.getPushUserEmail())
                    } else {
                        printOpen("AgileWork skipped by Execution Profile: ${pipelineData.executionProfileName}", EchoLevel.ALL)
                    }
                } else {
                    printOpen("Not a BPI artifact. AgileWork will not be checked.", EchoLevel.ALL)
                }

            } catch (Exception e) {
                sendStageEndToGPL(pomXmlStructure, pipelineData, "100", e.getMessage(), null, "error")
                throw e
            }
        }
        sendStageEndToGPL(pomXmlStructure, pipelineData, "100")
    }
}

/**
 * Stage 'updateVersionStep'
 */
def updateVersionStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "150")
    
    printOpen("Updating Version", EchoLevel.ALL)
    
    updateVersion(pomXmlStructure)
    
    if (pomXmlStructure.isRCVersion()) {
        pipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()
    }
    
    sendStageEndToGPL(pomXmlStructure, pipelineData, "150")
}

/**
 * Stage 'copyConfigFilesStep'
 */
def copyConfigFilesStep() { 
    sendStageStartToGPL(pomXmlStructure, pipelineData, "500")
    try {
        if (pipelineData.branchStructure.branchType == BranchType.HOTFIX || pipelineData.branchStructure.branchType == BranchType.RELEASE) {
            pushConfigFilesFromCfgProject(pomXmlStructure, GlobalVars.DEV_ENVIRONMENT)
            pushConfigFilesFromCfgProject(pomXmlStructure, GlobalVars.TST_ENVIRONMENT)
        } else if (pipelineData.branchStructure.branchType == BranchType.MASTER) {
            pushConfigFilesFromCfgProject(pomXmlStructure, GlobalVars.DEV_ENVIRONMENT)
        }
        sendStageEndToGPL(pomXmlStructure, pipelineData, "500")
    }catch (Exception e) {
            sendStageEndToGPLAndThrowError(pomXmlStructure, pipelineData, "500",e)
    }
}

/**
 * Stage 'refreshMicrosStep'
 */
def refreshMicrosStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "510")
    try {
        if (pipelineData.branchStructure.branchType == BranchType.HOTFIX || pipelineData.branchStructure.branchType == BranchType.RELEASE) {
            refreshDependencyConfig(pomXmlStructure,pipelineData,'BOTH', GlobalVars.DEV_ENVIRONMENT)
            refreshDependencyConfig(pomXmlStructure,pipelineData,'BOTH', GlobalVars.TST_ENVIRONMENT)
        } else if (pipelineData.branchStructure.branchType == BranchType.MASTER) {
            refreshDependencyConfig(pomXmlStructure,pipelineData,'BOTH', GlobalVars.DEV_ENVIRONMENT)
        }
        sendStageEndToGPL(pomXmlStructure, pipelineData, "510")
    }catch (Exception e) {
            sendStageEndToGPLAndThrowError(pomXmlStructure, pipelineData, "510",e)
    }
}

/**
 * Stage 'changelogFileStep'
 */
def changelogFileStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "600")
    boolean featureAdded = createChangeLogFromMergerRequestAPI(pipelineData, pomXmlStructure)
    //createChangeLogFromMergeRequest(pipelineData, pomXmlStructure)
    if (featureAdded) {
        printOpen("This merge request has modified the CHANGELOG.md file", EchoLevel.ALL)
        pushRepoWithMessage(pomXmlStructure, pipelineData, GlobalVars.GIT_TAG_CI_PUSH + " Adding new entry in changelog due to merge request", "${WORKSPACE}/CHANGELOG.md")
    }
    printOpen("pipelineData: ", EchoLevel.ALL)
    printOpen("branchStructure: ", EchoLevel.ALL)
    printOpen("targetBranch: ", EchoLevel.ALL)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "600")
}

/**
 * Stage 'pushReleaseToGitStep'
 */
def pushReleaseToGitStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "700")
    printOpen("antes de tag: ", EchoLevel.ALL)
    tagVersion(pomXmlStructure, pipelineData, false, false)
    printOpen("luego de tag: ", EchoLevel.ALL)
    pushRepo(pomXmlStructure, pipelineData)
    printOpen("luego de pushRepo: ", EchoLevel.ALL)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "700")
}

/**
 * Stage 'deployArtifactoryStep'
 */
def deployArtifactoryStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "800")
    deployNexus(pomXmlStructure, pipelineData)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "800")
    sendPipelineUpdateToGPL(initGpl, pomXmlStructure, pipelineData, '')
}

/**
 * Stage 'publishArtifactCatalogStep'
 */
def publishArtifactCatalogStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "950")
 
    pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName)
    printOpen("publishing artifact in catalog", EchoLevel.ALL)

    publishArtifactInCatalog(pipelineData, pomXmlStructure)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "950")
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure, pipelineData)
    cleanWorkspace()
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("El resultado del pipeline es ${successPipeline}", EchoLevel.ALL)
    if (pipelineBehaviour == PipelineBehavior.PUSH_OPENED_MR && sendToGitLab == true) {
        printOpen("No Enviamos final state running", EchoLevel.ALL)
        //updateGitlabCommitStatus name: '999-globalBuild', state: 'running'
    } else {
        if (sendToGitLab == true) {
            printOpen("Enviamos final state success", EchoLevel.ALL)
            if (pipelineBehaviour == PipelineBehavior.NOT_FIRST_MR) {
                updateCommitStatus(pipelineData, pomXmlStructure, 'success')
            }
            // updateGitlabCommitStatus name: '999-globalBuild', state: 'success'
        } else {
            if (pipelineBehaviour == PipelineBehavior.NOT_FIRST_MR) {
                updateCommitStatus(pipelineData, pomXmlStructure, 'success')
            }
            printOpen("No Enviamos final state", EchoLevel.ALL)
        }
    }
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    if (pipelineData.getExecutionMode().invokeNextActionAuto() && !pipelineData.isPushCI() && "MERGE" != pipelineData.gitAction) {
        printOpen("Modo test activado en fase de build", EchoLevel.ALL)
        invokeNextJob(pipelineData, pomXmlStructure)
    }
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("El resultado del pipeline es ${successPipeline}", EchoLevel.ALL)

    if (sendToGitLab == true) {
        printOpen("Enviamos final state failed", EchoLevel.ALL)
        if (pipelineBehaviour == PipelineBehavior.NOT_FIRST_MR) {
            updateCommitStatus(pipelineData, pomXmlStructure, 'failed')
        }
    } else {
        if (pipelineBehaviour == PipelineBehavior.NOT_FIRST_MR) {
            updateCommitStatus(pipelineData, pomXmlStructure, 'failed')
        }
        printOpen("No Enviamos final state", EchoLevel.ALL)
    }
    if (initGpl == false) {
        //init pipeline in GPL with minimun parameters
        sendPipelineStartToGPL(pipelineData, pipelineParams)
        initGpl = true
    }
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline)

    if (pipelineData.getExecutionMode() != null && pipelineData.getExecutionMode().isNotifyforFailureNeeded()) {
        //Mando email
        mail to: GlobalVars.EMAIL_REPORT,
            subject: "[Alm3] Resultado Jenkins Nocturne job: ${env.JOB_BASE_NAME} - ${currentBuild.result}",
            body: """
                Componente: ${env.JOB_BASE_NAME}
                Build: ${currentBuild.fullDisplayName}
                Resultado: ${currentBuild.result}
                URL de build: ${env.BUILD_URL}
                URL de Git: ${pipelineData.gitUrl}
                Rama Git que desencadenó el Build: ${pipelineData.branchStructure.branchName}
                Puedes ver la salida a consola completo aqui: ${env.BUILD_URL}/consoleFull
                """
    }
}
