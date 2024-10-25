import groovy.transform.Field
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.ExportJobs
import com.caixabank.absis3.FileUtils
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.Utilities
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.yaml.snakeyaml.Yaml

@Field Map pipelineParams

@Field String gitRepoUrl

@Field ExportJobs exportJobs
@Field GitRepositoryHandler git

@Field String[] folders

@Field String jobBranch

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters
    
    jobBranch = params.jobBranch ? params.jobBranch : env.JENKINS_GIT_JOBS_BRANCH

    folders = ['folders', 'workflow', 'freestyleprojects', 'matrixconfiguration', 'matrixprojects', 'multibranch']

    pipeline {
        agent { label 'master' }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
        }
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
            stage('folder-structure') {
                steps {
                    folderStructureStep()
                }
            }
            stage('job-structure') {
                steps {
                    jobStructureStep()
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
 * Stage 'getGitRepoStep'
 */
def getGitRepoStep() {
    exportJobs = new ExportJobs(this)

    String enviroment = System.getProperty('jenkins.environment')

    gitRepoUrl = "https://git.svb.lacaixa.es/cbk/absis3/config/jenkins-${enviroment}.git"

    printOpen("Pull from '$enviroment' and '$jobBranch' branch, gitUrl is $gitRepoUrl", EchoLevel.INFO)

    git = new GitRepositoryHandler(this, gitRepoUrl, [checkoutBranch: jobBranch, gitProjectRelativePath: '.'])
    git.pullOrClone()
}

/**
 * Stage 'folderStructureStep'
 */
def folderStructureStep() {
    exportJobs.createFolderStructure(folders[0])
}

/**
 * Stage 'jobStructureStep'
 */
def jobStructureStep() {
    exportJobs.createJobStructure(folders.subList(1, folders.size() - 1))
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen('SUCCESS', EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen('FAILURE', EchoLevel.INFO)
}

/**
 * Stage 'endiPipelineAlwaysStep'
 */
def endiPipelineAlwaysStep() {
    cleanWorkspace()
}
