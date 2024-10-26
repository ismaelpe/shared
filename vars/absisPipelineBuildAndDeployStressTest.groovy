import groovy.transform.Field
import com.project.alm.*

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
@Field Map pipelineParams
@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field BranchStructure branchStructure
@Field CloudStateUtility cloudStateUtilitity
@Field String classifierName

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters
    cloudStateUtilitity = null

    pipeline {
        agent {    node(almJenkinsAgent(pipelineParams)) }
        //Environment sobre el qual se ejecuta este tipo de job
        options {
            gitLabConnection('gitlab')
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
            timeout(time: 3, unit: 'HOURS')
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
            stage('init') {
                steps {
                    initStep()
                }
            }
            stage('build & deploy') {
                steps {
                    buildAndDeployStep()
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
    printOpen("${env.gitlabMergeRequestLastCommit}", EchoLevel.INFO)
    //updateGitCode("${env.gitlabMergeRequestLastCommit}")
    printOpen("pipelineParams: ${pipelineParams.toString()}", EchoLevel.DEBUG)
    printOpen('Jenkinsfile is:', EchoLevel.DEBUG)
    printFile('Jenkinsfile', EchoLevel.DEBUG)
}

/**
 * Stage 'initStep'
 */
def initStep() {
    initGlobalVars(pipelineParams)
    pipelineData = getInfoGit()
    branchStructure = getBranchInfo()
    pipelineData.init(branchStructure, pipelineParams.subType, pipelineParams.type, pipelineParams.get('isArchetype', false), pipelineParams.get('archetypeModel', './'))
    pomXmlStructure = analizePomXml(pipelineParams.type, pipelineParams.subType)
    if (pipelineData.commitLog && pipelineData.commitLog.indexOf(GlobalVars.STRESS_TEST_CLASSIFIER_TAG) != -1) {
        String textToLocate = GlobalVars.STRESS_TEST_CLASSIFIER_TAG + '['
        classifierName = pipelineData.commitLog.substring(pipelineData.commitLog.indexOf(textToLocate) + textToLocate.length(), pipelineData.commitLog.lastIndexOf(']'))
    }
    printOpen("Build Stress Test_${env.BUILD_ID} with classifier ${classifierName}", EchoLevel.INFO)
    currentBuild.displayName = "Build_Stress_Test_${env.BUILD_ID}"
}

/**
 * Stage 'buildAndDeployStep'
 */
def buildAndDeployStep() {
    printOpen('Building and deploying stress project to nexus', EchoLevel.INFO)
    String mvnParameter = ''
    if (classifierName) {
        mvnParameter = "-DclassifierName=${classifierName}"
    }
    deployNexus(pomXmlStructure, pipelineData, mvnParameter, true)
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
    printOpen('FAILURE', EchoLevel.ERROR)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    debugInfo(pipelineParams, pomXmlStructure, pipelineData)
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}

