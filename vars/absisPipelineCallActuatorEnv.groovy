import groovy.transform.Field
import com.project.alm.*
import groovy.json.JsonSlurperClassic
import java.util.HashMap


@Field Map pipelineParams
@Field String version
@Field String pathToRepo
@Field String originBranch
@Field String artifactSubType
@Field String artifactType
@Field String pipelineOrigId
@Field String user
@Field boolean isBeta
@Field boolean isCenterOne
@Field String executionProfile
@Field String targetAlmFolder
@Field String loggerLevel
@Field String agentParam
@Field String icpEnv
@Field String prettySystemResult

//Pipeline para realizar consultar la configuración de un micro llamando al endpoint del actuator/env
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {

    pipelineParams = pipelineParameters

    version = params.versionParam
    pathToRepo = params.pathToRepoParam
    originBranch = params.originBranchParam
    artifactSubType = params.artifactSubTypeParam
    artifactType = params.artifactTypeParam
    pipelineOrigId = params.pipelineOrigId
    isBeta = params.betaParam.toString().toBoolean() 
    isCenterOne = params.centerOneParam.toString().toBoolean()
    user = params.userId

    executionProfile = params.executionProfileParam
    targetAlmFolder = params.targetAlmFolderParam

    loggerLevel = params.loggerLevel
    agentParam = params.agent

    icpEnv = GlobalVars.PRO_ENVIRONMENT

    prettySystemResult = ""

    pipeline {
        agent {    node(absisJenkinsAgent(pipelineParams)) }
        options {
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
            executionProfile = "${executionProfileParam ? executionProfileParam : 'DEFAULT'}"
            logsReport = true
            sendLogsToGpl = true
        }
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
            stage('get-application-properties') {
                steps {
                    stageExecuteActuatorEnvStep()
                }
            }
            stage('get-system-properties') {
                steps {
                    stageGetSystemPropertiesStep()
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

    initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null
    
    pomXmlStructure = getGitRepo(pathToRepo, '', '', false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), version, true)
    
    pipelineData = new PipelineData(PipelineStructureType.CALL_ACTUATOR_ENV, "${env.BUILD_TAG}", env.JOB_NAME, params)
    pipelineData.initVoidActions(pathToRepo, originBranch, ArtifactSubType.valueOfSubType(artifactSubType), null, icpEnv)
	pipelineData.setDefaultAgent(agentParam)

    pipelineData.buildCode = pomXmlStructure.artifactVersion
    pipelineData.pushUser = user
    
    almEvent = new KpiAlmEvent(
        pomXmlStructure, pipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.PIPELINE_ACTUATOR_ENV)
    
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_STARTED, KpiLifeCycleStatus.OK)
    sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100")
    initGpl = true
    debugInfo(pipelineParams, pomXmlStructure, pipelineData)
    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")

}



/**
 * Stage 'stageExecuteActuatorEnvStep'
 */
def stageExecuteActuatorEnvStep() {
    
    sendStageStartToGPL(pomXmlStructure, pipelineData, "200")

    String type=pipelineData.garArtifactType.getGarName()
	String application=pomXmlStructure.getApp(pipelineData.garArtifactType)

    printOpen("ArtifactName: ${pomXmlStructure.artifactName}, ArtifactMicro: ${pomXmlStructure.artifactMicro}", EchoLevel.DEBUG)

    String url = null

    if (isCenterOne) {
        url = GlobalVars.K8S_CENTER_URL.replace("{environment}",icpEnv).replace("{datacenter}","1") + "/"
    } else {
        url = GlobalVars.K8S_URL.replace("{environment}",icpEnv) + "/" 
    }

    String archPath = "arch-service/"

    if (pipelineData.garArtifactType == GarAppType.ARCH_MICRO) url += archPath 

    String major = MavenVersionUtilities.getMajor(version)

    if (isBeta) {
        url += "${pomXmlStructure.artifactMicro}-${major}-beta/"
    } else {
        url += "${pomXmlStructure.artifactMicro}-${major}/"
    }

    url += GlobalVars.ENDPOINT_ENV

    String fileOutput= CopyGlobalLibraryScript('',null,'outputCommand.json')
	def responseStatusCode=null
    def contentResponse=null
    int statusCode

    int iteration = 0

    String prettyResult = ""

    try {
        timeout(GlobalVars.DEFAULT_ABSIS3_MS_REQUEST_RETRIES_TIMEOUT) {
            waitUntil(initialRecurrencePeriod: 15000) {

                withCredentials([usernamePassword(credentialsId: "K8SGATEWAY_${icpEnv.toUpperCase()}", usernameVariable: 'K8SGATEWAY_USERNAME', passwordVariable: 'K8SGATEWAY_PASSWORD')]) {
                    
                    String microBasicCredentials = "${K8SGATEWAY_USERNAME}:${K8SGATEWAY_PASSWORD}"
                    String auth = microBasicCredentials.bytes.encodeBase64().toString()

                    def customHeaders = "Authorization: Basic ${auth}"
                    def command="curl -k --write-out '%{http_code}' -o ${fileOutput} -s -X GET ${url} --proxy ${GlobalVars.proxyCaixa} --connect-timeout ${GlobalVars.ABSIS3_MS_TIMEOUT} -H  'Content-Type: application/json' -H \"${customHeaders}\""

                    printOpen("Invoking url ['GET'] ${url}", EchoLevel.DEBUG)
                    
			        retry(GlobalVars.ACTUATOR_ENV_MAX_RETRIES) {
				        responseStatusCode = sh(script: command, returnStdout: true)
                        sh "cat ${fileOutput}"
                    }

			    }

                if (responseStatusCode == null) {

                    throw new Exception("No response from microservice")

                }

                statusCode = responseStatusCode as Integer
                contentResponse = sh(script: "cat ${fileOutput}", returnStdout:true)

                if (statusCode == 200) {

                    return true

			    } else {

                    iteration += 1
                    boolean stopCondition = false
                    if (iteration == GlobalVars.HTTP_REQUEST_MAX_RETRIES) stopCondition = true
                    if (stopCondition) throw new Exception("Unexpected HTTP code on response when sending request to OpenServices Microservice (Response HTTP Code: ${statusCode})\nResponse body:\n\n${HttpRequestUtilities.prettyPrint(contentResponse)}")
                    return false

                }
		    }

        }

        def json = new JsonSlurperClassic().parseText(contentResponse)

        if (json.propertySources!=null && json.propertySources.size()>0) {

            json.propertySources.each { propSource->

                if (propSource.properties != null && propSource.properties.size()>0) {

                    printOpen("Get propertySource: ${propSource}",EchoLevel.DEBUG)
                    
                    Boolean systemProp = false
                    if (propSource.name.equals("systemEnvironment") || propSource.name.equals("systemProperties")) {
                        systemProp = true
                    }
                                
                    Map main = new HashMap();

                    propSource.properties.each { k, v -> 

                        printOpen("Get property: ${k}",EchoLevel.ALL)

                        String[] keys = k.split("\\.")
                        int size = keys.size()
                        
                        if (size>2) {

                            Map mapIt = new HashMap();

                            for(int i=0;i<size-1;i++) {

                                if(i == 0) {
                                    //first
                                    if (!main.containsKey(keys[i])) {
                                        Map mapaVacio = new HashMap();
                                        main.put(keys[i], mapaVacio)
                                    }

                                    mapIt = main.get(keys[i])

                                } else if (i == size-2) {
                                    //last
                                    if (!mapIt.containsKey(keys[i]+"."+keys[i+1])) {
                                        mapIt.put(keys[i]+"."+keys[i+1], v.value)
                                    }

                                } else {

                                    if (!mapIt.containsKey(keys[i])) {
                                        Map mapaVacio = new HashMap();
                                        mapIt.put(keys[i], mapaVacio)
                                    }

                                    mapIt = mapIt.get(keys[i])

                                }
                            }

                        } else {
                            if(!main.containsKey(k)) {
                                main.put(k, v.value)
                            }
                            
                        }
                        
                    }

                    def result = getActuatorEnvLogs(main)

                    if (systemProp) {
                        prettySystemResult +=  "## ${propSource.name} ##\n"
                        prettySystemResult += result + "\n\n"
                    } else {
                        prettyResult +=  "## ${propSource.name} ##\n"
                        prettyResult += result + "\n\n"
                    }
                               
                }
                                
            }
        }

    } catch (Exception e) {

        printOpen("Error al consultar la configuración: ${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "200", null, null, "error")
        throw e

    }

    sendStageEndToGPL(pomXmlStructure, pipelineData, "200", prettyResult)
		
}

def stageGetSystemPropertiesStep() {

    sendStageStartToGPL(pomXmlStructure, pipelineData, "210")

    if (prettySystemResult == "") {
        printOpen("Error al consultar la configuración", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "210", null, null, "error")
        throw new Exception("Error al recuperar las propiedades del sistema")
    }
    
    sendStageEndToGPL(pomXmlStructure, pipelineData, "210", prettySystemResult)

}



/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen('SUCCESS', EchoLevel.INFO)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, true)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen('FAILURE', EchoLevel.ERROR)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, false)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}