import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

@Field NexusUtils nexus = new NexusUtils(this)

@Field PomXmlStructure pomXmlStructure
@Field String gitRepoUrl
@Field List<String> projectsList
@Field List<String> projectsOutdateCoreUpdatedList
@Field List<String> projectsFailed
@Field List<String> failedProjectsGitRepoPaths
@Field String lastVersionCore

@Field String overridenVersion
@Field String BuildType

@Field List<String> projectsListTrimmed

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    nexus = new NexusUtils(this)

    projectsOutdateCoreUpdatedList = []
    projectsFailed = []
    failedProjectsGitRepoPaths = []

    overridenVersion = params.forceVersion ? params.forceVersion.trim() : null
    buildType = params.versionTypeAutoSelection ? params.versionTypeAutoSelection.trim() : 'SNAPSHOT'

    projectsListTrimmed = []

    projectsList = params.projects ? params.projects.split(GlobalVars.PROJECTS_LIST_SEPARATOR) : []
    //Nos debe llegar el listado de proyectos separado por puntoycoma
    projectsList.each { projectsListTrimmed.add(it.trim()) }
    projectsList = projectsListTrimmed

    pipeline {
        agent { node(almJenkinsAgent(pipelineParams)) }
        options {
            gitLabConnection('gitlab')
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
            https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
        }
        stages {
            stage('get-core-version') {
                when {
                    expression { projectsList || true }
                }
                steps {
                    getCoreVersionStep()
                }
            }
            stage('check-projects') {
                when {
                    expression { projectsList }
                }
                steps {
                    checkProjectsStep()
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
 * Stage 'getCoreVersionStep'
 */
def getCoreVersionStep() {
    printOpen("Listado de proyectos a verificar:\n${projectsList.join('\n')}", EchoLevel.INFO)

    if (overridenVersion) {
        lastVersionCore = overridenVersion
        printOpen("La versión del core se ha forzado a: ${lastVersionCore}", EchoLevel.INFO)
    } else {
        if (buildType == 'RELEASE') {
            lastVersionCore = nexus.getLastVersionNumber(GlobalVars.ALM_CORE_GROUPID, GlobalVars.ALM_CORE_ARTIFACTID, null, GlobalVars.NEXUS_RELEASES_REPO_NAME, BuildType.FINAL)
            printOpen("La última versión RELEASE del core es: ${lastVersionCore}", EchoLevel.INFO)
        } else if (buildType == 'RC') {
            lastVersionCore = nexus.getLastVersionNumber(GlobalVars.ALM_CORE_GROUPID, GlobalVars.ALM_CORE_ARTIFACTID, null, GlobalVars.NEXUS_RELEASES_REPO_NAME, BuildType.RELEASE_CANDIDATE)
            printOpen("La última versión RC del core es: ${lastVersionCore}", EchoLevel.INFO)
        } else {
            lastVersionCore = nexus.getLastVersionNumber(GlobalVars.ALM_CORE_GROUPID, GlobalVars.ALM_CORE_ARTIFACTID, null, GlobalVars.NEXUS_PUBLIC_REPO_NAME, BuildType.SNAPSHOT)
            printOpen("La última versión SNAPSHOT del core es: ${lastVersionCore}", EchoLevel.INFO)
        }
    }
}

/**
 * Stage 'checkProjectsStep'
 */
def checkProjectsStep() {
    projectsList.each() {
        printOpen("Comprobando versión del core de proyecto '${it}'", EchoLevel.INFO)

        gitRepoUrl = GlobalVars.gitlabDomain + it + '.git'
        pomXmlStructure = getPomFromGitRepo(gitRepoUrl, GlobalVars.MASTER_BRANCH, it)
        if (!pomXmlStructure.archVersion || !pomXmlStructure.archVersion.trim()) {
            // Si la versión de arquitectura viene vacia porque es un proyecto multimodulo
            calculateArchVersionWithModules(pomXmlStructure, it)
        }

        withCredentials([
            usernamePassword(credentialsId: GlobalVars.GIT_CREDENTIAL_PROFILE_ID, passwordVariable: GlobalVars.GIT_CREDENTIAL_PASSWORD_VAR, usernameVariable: GlobalVars.GIT_CREDENTIAL_USER_VAR)
        ]) {
            if (lastVersionCore == pomXmlStructure.archVersion) {
                printOpen("Proyecto '${it}' ya estaba con la última versión del core (${pomXmlStructure.archVersion}). Redesplegamos master", EchoLevel.INFO)
                sh "cd ${it} && git add . && git commit -m 'deploy with executionProfile[SCHEDULED_CORE_UPDATE]' --allow-empty"
                sh "cd ${it} && git -c http.sslVerify=false push origin ${GlobalVars.MASTER_BRANCH}"
            // Redespliega master
            } else {
                printOpen("Proyecto '${it}' outdated respecto a la versión actual del core. Está con la versión ${pomXmlStructure.archVersion}. Actualizamos a la versión ${lastVersionCore}", EchoLevel.INFO)
                String branchName = "${GlobalVars.FEATURE_BRANCH}/UpgradeParentVersion_${lastVersionCore}_${env.BUILD_ID}"
                sh "cd ${it} && git checkout -b ${branchName}" // Creamos la rama feature
                try {
                    configFileProvider([configFile(fileId: 'alm-maven-settings-with-singulares', variable: 'MAVEN_SETTINGS')]) {
                        sh "cd ${it} && mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS  ${GlobalVars.GLOBAL_MVN_PARAMS} versions:update-parent -DallowSnapshots=true -DparentVersion=[${lastVersionCore}] -DgenerateBackupPoms=false"
                    } // Actualizamos versión de Parent con la versión del Core última
                    sh "cd ${it} && git add . && git commit -m 'Upgrading parent version to ${lastVersionCore} and deploy with executionProfile[SCHEDULED_CORE_UPDATE]' --allow-empty"
                    sh "cd ${it} && git -c http.sslVerify=false push origin ${branchName}"
                    projectsOutdateCoreUpdatedList.add(it)
                } catch (Exception e) {
                    printOpen("No se pudo actualizar proyecto '${it}'. El error fue: ${e.getMessage()}", EchoLevel.ERROR)
                    projectsFailed.add(it)
                    failedProjectsGitRepoPaths.add(gitRepoUrl)
                }
            }
        }
    }
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen('Pipeline de actualización de versión de core finalizado', EchoLevel.INFO)
    if (projectsOutdateCoreUpdatedList) {
        printOpen("Proyectos desactualizados que fueron actualizados:\n${projectsOutdateCoreUpdatedList.join('\n')}", EchoLevel.INFO)
    }
    if (projectsFailed) {
        printOpen("Proyectos fallidos:\n${projectsFailed.join('\n')}", EchoLevel.ERROR)
        def mapEmailFields = Utilities.getEmailTemplate(this, [
                            'type': 'updateCoreNocturneJobFailure',
                            'gitUrl': failedProjectsGitRepoPaths
                        ])
        mail to: GlobalVars.EMAIL_REPORT,
                            subject: mapEmailFields.subject,
                            body: mapEmailFields.body
    }
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen('Falló la ejecución del pipeline', EchoLevel.ERROR)
}

/**
 * Stage 'endiPipelineAlwaysStep'
 */
def endiPipelineAlwaysStep() {
    cleanWorkspace()
}
