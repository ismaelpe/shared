import groovy.transform.Field
import com.caixabank.absis3.*

@Field Map pipelineParams

@Field String gitURL
@Field String gitCredentials
@Field String jenkinsPath

@Field String originBranch
@Field String pathToRepo
@Field String repoName
@Field String user

@Field String artifactType
@Field String artifactSubType

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean initGpl
@Field boolean successPipeline

//Pipeline que ejecuta el stage checkmarx on demand
//Recibe los siguientes parametros
//type: 
//originBranch : Ej master
//pathToRepoParam : url git
//repoParam : nombre del repo git
//userId : usuario ejecutor
//artifactTypeParam : tipo
//artifactSubTypeParam: subtipo
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) { 
	pipelineParams = pipelineParameters
      
    gitURL = "https://git.svb.lacaixa.es/"
    gitCredentials = "GITLAB_CREDENTIALS"
    jenkinsPath = "absis3/services"
    originBranch = params.originBranchParam
    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam
    user = params.userId
    artifactType = params.artifactTypeParam
    artifactSubType = params.artifactSubTypeParam
    initGpl = false
    successPipeline = false
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
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
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
			stage('checkmarx-scan') {
				steps {
                    checkmarxScanStep()
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
 * Stage 'getGitRepoStep'
 */
def getGitRepoStep() {
    initGlobalVars(pipelineParams)
    printOpen("Extract GIT Repo ${pathToRepo} ${originBranch}", EchoLevel.INFO)
    pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false)
}

/**
 * Stage 'checkmarxScanStep'
 */
def checkmarxScanStep() {
    printOpen("Iniciando stage checkmarx-scan", EchoLevel.INFO)
    currentBuild.displayName = "Checkmarx_scan_${pomXmlStructure.artifactVersion} of ${pomXmlStructure.artifactName}"
    pipelineData = new PipelineData()
    
    pipelineData.garArtifactType = PipelineData.initFromGitUrlGarApp(pathToRepo, ArtifactSubType.valueOfSubType(artifactSubType))
    pipelineData.pushUser = user
    
    checkmarxScanWorkspace(pomXmlStructure, pipelineData,false,originBranch,GlobalVars.TST_ENVIRONMENT.toUpperCase())
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("SUCCESS", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("FAILURE", EchoLevel.ERROR)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}
  

