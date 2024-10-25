import groovy.transform.Field
import com.caixabank.absis3.*
import com.caixabank.absis3.ICPStateUtility
import com.caixabank.absis3.ArtifactType

@Field Map pipelineParams

@Field String dataSourceFile
@Field String artifactType
@Field String artifactSubType
@Field String targetBranch
@Field String gitLabActionType
@Field boolean initGpl
@Field boolean successPipeline
@Field String deployICPPhases
@Field String resultDeployICP

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData

@Field BranchStructure branchStructure
@Field PipelineBehavior pipelineBehaviour
@Field ICPStateUtility icpStateUtilitity
@Field boolean sendToGitLab
@Field boolean ifProceed

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    dataSourceFile = ""
    artifactType
    artifactSubType
    targetBranch
    gitLabActionType
    initGpl = false
    successPipeline = false
    deployICPPhases = "01-pre-deploy"
    resultDeployICP = "OK"

    pipelineBehaviour = PipelineBehavior.LIKE_ALWAYS
    icpStateUtilitity = null
    sendToGitLab = true
	ifProceed = true
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        //Environment sobre el qual se ejecuta este tipo de job
        options {
            gitLabConnection('gitlab')
            buildDiscarder(logRotator(numToKeepStr: '10'))
			timestamps()
			timeout(time: 3, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            ICP_CERT = credentials('icp-absis3-pro-cert')
            ICP_PASS = credentials('icp-absis3-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
            executionProfile = "${pipelineParams ? pipelineParams.get('executionProfile', 'DEFAULT') : 'DEFAULT'}"
        }        
        stages {
            stage('test-icp-api') {
                steps {
                    testIcpApiStep()  
                }
            }
            stage('git-pull-repo') {
                steps {
                   gitPullRepoStep() 
                }
            }
            stage('build') {
                steps {
                    buildStep()
                }
            }
            stage('sonar-scan') {
                steps {                    
                    sonarScanStep()
                }
            }	
            stage('sonar-quality-gate') {
                steps {
                    sonarQualityGateStep()
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
 * Stage 'testIcpApiStep'
 */
def testIcpApiStep() {
    initGlobalVars(pipelineParams)
    configFileProvider([configFile(fileId: 'absis-icp-cert-pro', variable: 'CERTIFICATE')]) {
        withCredentials([string(credentialsId: 'ICP_CERT_PASSWD_PRE', variable: 'PASSWORD')])  {
            
            def command = "curl -k --write-out %{http_code} -o temp -s -X GET https://publisher-ssp-cldalm.pro.ap.intranet.cloud.lacaixa.es/api/publisher/features/environment/DEV/az/ALL/name/deploy --cert .$CERTIFICATE:$PASSWORD -H  accept:*/* -H  application_active:demoarqalm2-micro -H  Content-Type:application/json "
        
            echo sh(script: command, returnStdout: true)
        }
    }
}

/**
 * Stage 'testIcpApiStep'
 */
def gitPullRepoStep() {                   
    printOpen("The git Repo is ${gitRepoUrl}", EchoLevel.ALL)
    withCredentials([usernamePassword(credentialsId: 'GITLAB_CREDENTIALS', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
        String gitRepoSecuredUrl = getGitRepoUrl(gitRepoUrl)
        sh "git -c http.sslVerify=false  clone --depth 1 --verbose ${gitRepoSecuredUrl} . "
        sh "git checkout ${originBranch}"
    } 
}      

/**
 * Stage 'buildStep'
 */
def buildStep() {
    configFileProvider([configFile(fileId: 'absis3-maven-settings-with-singulares', variable: 'MAVEN_SETTINGS')]) {
        for (it = 0; it < iterations; it++) {
            printOpen("Building iteration: ${it}")
        
            /* Cuidado, deben mantenerse los dos parametros "-Dmaven.test.skip=true -DskipTests" para evitar la compilacion de tests pero tambien
                la ejecucion en caso de realizarse con un target ya generado anteriormente */
            mavenResult = sh (
                script: "mvn -Dhttp.proxyHost=$env.proxyHost -Dhttp.proxyPort=$env.proxyPort -Dhttps.proxyHost=$env.proxyHost -Dhttps.proxyPort=$env.proxyPort -s $MAVEN_SETTINGS  $GlobalVars.GLOBAL_MVN_PARAMS clean verify -Dskip-generate-static-docs=true -Dmaven.test.skip=true -DskipTests $env.MAVEN_ADITIONAL_PARAMS",
                returnStdout: true
            ).trim()
        
            printOpen("Building iteration: ${it} - ${mavenResult}")
        }
    }                        
}

/**
 * Stage 'sonarScanStep'
 */
def sonarScanStep() {
    for (it = 0; it < iterations; it++) {
        printOpen("Sonar Scan: ${it}")
        withSonarQubeEnv('sonarqube') {  
            configFileProvider([configFile(fileId: 'absis3-maven-settings-with-singulares', variable: 'MAVEN_SETTINGS')]) {
                sh "export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 && mvn --no-transfer-progress -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS  ${GlobalVars.GLOBAL_MVN_PARAMS} org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar -Dsonar.projectKey=${env.sonarProjectName} -Dsonar.projectName=${env.sonarProjectName}"
            }
        }
    }
}

/**
 * Stage 'sonarQualityGate'
 */
def sonarQualityGateStep() {
    def qg = waitForQualityGate()

    if (qg != null) {
        printOpen("sonar-quality-gate result is ${qg.status}", EchoLevel.ALL)
    }                        
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen("SUCCESS", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen("Pipeline has failed", EchoLevel.ERROR)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    printOpen("CLEAN", EchoLevel.ALL)
    cleanWs()
    cleanWorkspace()
}

