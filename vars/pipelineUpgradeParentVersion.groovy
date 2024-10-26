import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

@Field String gitURL
@Field String gitCredentials

@Field String rojects
@Field String parentVersion
@Field String branch
@Field String createRC

@Field String executionProfile

@Field GitRepositoryHandler[] projectGitHandlers

//Pipeline que permite actualizar el parent pom de N proyectos
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    gitURL = 'https://git.svb.digitalscale.es/'
    gitCredentials = 'GITLAB_CREDENTIALS'

    projects = params.projects
    parentVersion = params.parentVersion
    branch = params.branch
    createRC = params.createRC

    executionProfile = 'FEATURE_AUTO_MERGE_IF_OK'

    projectGitHandlers = []

    pipeline {
        agent { node(almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
        }
        environment {
            AppPortal = credentials('IDECUA-JENKINS-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
            https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
        }
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
            stage('calculate-profile') {
                steps {
                    calculateProfileStep()
                }
            }
            stage('upgrading-parent-version') {
                steps {
                    upgradingParentVersionStep()
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
 * Stage 'initDatagetGitRepoStepStep'
 */
def getGitRepoStep() {
    if (!parentVersion) {
        error('Parent Version is a mandatory field')
    }

    List parsedProjects = Utilities.splitStringToListWithSplitter(projects, ';')

    parsedProjects.each {
        def gitRepoUrl = gitURL + it + '.git'
        def finalBranch = branch ? branch : 'master'
        GitRepositoryHandler git = new GitRepositoryHandler(this, gitRepoUrl, [checkoutBranch: finalBranch])
        printOpen("Extract GIT Repo ${gitRepoUrl}", EchoLevel.DEBUG)
        git.pullOrClone()

        projectGitHandlers.add(git)
    }
}

/**
 * Stage 'calculateProfileStep'
 */
def calculateProfileStep() {
    if (createRC) {
        executionProfile = 'UPGRADE_CORE_AND_CREATE_RC'
    }
}

/**
 * Stage 'upgradingParentVersionStep'
 */
def upgradingParentVersionStep() {
    projectGitHandlers.each {
        GitRepositoryHandler git = (GitRepositoryHandler) it

        String featureBranchName = "${GlobalVars.FEATURE_BRANCH}/UpgradeParentVersion_${parentVersion}_build${env.BUILD_ID}"

        if ( ! branch ) {
            git.checkout(featureBranchName, [newBranch: true])
        }

        configFileProvider([configFile(fileId: 'alm-maven-settings-with-singulares', variable: 'MAVEN_SETTINGS')]) {
            sh "cd ${git.getGitProjectRelativePath()} && mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS ${GlobalVars.GLOBAL_MVN_PARAMS} versions:update-parent -DallowSnapshots=true -DparentVersion=[${parentVersion}] -DgenerateBackupPoms=false"
        }

        //Add and commit (permitimos empty por si ya estaba actualizado y no cambia que no falle)
        def finalBranch = branch ? branch : featureBranchName
        def commmitMessage = branch ? "Upgrading parent version to ${parentVersion}" : "Upgrading parent version to ${parentVersion} and deploy with executionProfile[${executionProfile}]"

        git.add().commitAndPush(commmitMessage, [remoteBranch: finalBranch, allowEmpty: true])
    }
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen('Se marca como success el pipeline', EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    attachPipelineLogsToBuild()
    printOpen('Se marca como failure el pipeline', EchoLevel.ERROR)
}

/**
 * Stage 'endiPipelineAlwaysStep'
 */
def endiPipelineAlwaysStep() {
    cleanWorkspace()
}
