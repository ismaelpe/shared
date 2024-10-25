import groovy.json.JsonSlurper
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams
@Field PipelineData pipelineData
@Field PomXmlStructure pomXmlStructure
@Field KpiAlmEvent almEvent

@Field String gitURL
@Field String  gitCredentials
@Field String  jenkinsPath

@Field String artifactSubType
@Field String artifactType
@Field String originBranch
@Field String pathToRepo
@Field String repoName
@Field String userId
@Field String targetAlmFolder
@Field boolean isArchetype
@Field String archetypeModel
@Field String mvnAdditionalParameters
@Field String commitId
@Field String agent

@Field long initCallStartMillis

@Field boolean initGpl
@Field boolean successPipeline

@Field String microUrlGatewayForTesting

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call() {
    pipelineParams = Utilities.getRemoteJenkinsFileParams(params.pathToRepoParam, params.originBranchParam, "GITLAB_API_SECRET_TOKEN", false)

    pipelineParams.loggerLevel = params.loggerLevel

    initGpl = false
    sendToGitLab = false
    successPipeline = false
    agent = pipelineParams ? pipelineParams.get('agent', 'standard') : 'standard'
    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam
    originBranch = params.originBranchParam
    artifactSubType = params.artifactSubTypeParam
    artifactType = params.artifactTypeParam
    userId = params.userId    
    initCallStartMillis = new Date().getTime()
    isArchetype = params.isArchetypeParam.toString().toBoolean()
    archetypeModel = params.archetypeModelParam
    commitId = params.commitIdParam
    mvnAdditionalParameters = params.mvnAdditionalParametersParam?.split(",")
    microUrlGatewayForTesting = ""

    /**
     * 1. Recoger el artifact
     */
    pipeline {
        agent { node (absisJenkinsAgent(agent)) }
        options {
            gitLabConnection('gitlab')
            buildDiscarder(logRotator(numToKeepStr: '40'))
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
            gitURL = 'https://git.svb.lacaixa.es/'
            gitCredentials = 'GITLAB_CREDENTIALS'
            jenkinsPath = 'absis3/services'
            APIGW_TOKEN = credentials('ABSIS3_TOKEN_PRO')
            SONAR_TOKEN = credentials('sonartoken')
            sendLogsToGpl = true
        }
        //Atencion que en el caso que estemos en un MergeRequest... quizas solo debamos validar la issue
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
            stage('check-micro-version') {
                when {
                    expression { hasSampleApp(pipelineParams.type, pipelineParams.subType) }
                }
                steps {
                    checkMicroVersionStep()
                }
            }
            stage('build-and-test') {
                steps {
                    buildAndTestStep()
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
            stage('sonar-send-report') {
                when {
                    expression { pipelineData?.pipelineStructure?.resultPipelineData?.sonarQualityGateExecuted }
                }
                steps {
                    sonarSendReportStep()
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
 * Stage getGitRepoStep
 */
def getGitRepoStep() {
    initGlobalVars(pipelineParams)  // pipelineParams arrive as null

    printOpen("Extract GIT Repo ${pathToRepo} ${originBranch}", EchoLevel.DEBUG)

    // Descargamos el projecto
    GitRepositoryHandler git = new GitRepositoryHandler(this, pathToRepo, [gitProjectRelativePath: '.']).fastInitializeAndCheckout(originBranch)

    // Leemos el jenkins file y lo cargamos en el pipelineParams
    pipelineParams = Utilities.parseJenkinsFilePipelineParams(this, readFile("Jenkinsfile"), pipelineParams)

    // Creamos el pomXmlStructure
    pomXmlStructure = analizePomXml(pipelineParams.type, pipelineParams.subType)
    
    // Cargamos todas las  demas variables
    pipelineData = new PipelineData(PipelineStructureType.SONAR_SCAN_AND_SEND_REPORT, env.BUILD_TAG, env.JOB_NAME, params)
    
    BranchStructure branchStructure = new BranchStructure()
    branchStructure.branchName = originBranch
    branchStructure.init()

    pipelineData.gitUrl = pathToRepo
    pipelineData.pushUser = userId

    pipelineData.init(branchStructure, pipelineParams.subType, pipelineParams.type, pipelineParams.get('isArchetype', false), pipelineParams.get('archetypeModel', './'))
    pipelineData.commitId = git.getLastCommitId()
    pipelineData.mvnAdditionalParameters = mvnAdditionalParameters
    pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName)
    pipelineData.setDefaultAgent(agent)
    pipelineData.prepareExecutionMode(pipelineParams.get('executionProfile', 'DEFAULT'), pipelineParams.get('almFolder'));

    currentBuild.displayName = "QA_${env.BUILD_ID}_${pipelineData.getPipelineBuildName()} of $pomXmlStructure.artifactName"

     // Comprobamos si hemos cargado del metodo anterior las propiedades del Jenkins
    pipelineData.mvnMuleParameters = loadMuleMvnParameters(pipelineData, pomXmlStructure)

    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_STARTED, KpiLifeCycleStatus.OK)
    almEvent = new KpiAlmEvent(
        pomXmlStructure, pipelineData,
        KpiAlmEventStage.GENERAL,
        KpiAlmEventOperation.MVN_SONAR_SCAN)

    sendPipelineStartToGPL(pomXmlStructure, pipelineData, '')
    initGpl = true

}

/**
 * Stage 'check-micro-version'
 */
def checkMicroVersionStep() {
    
    printOpen("Checking version of deployed micro", EchoLevel.INFO)
    
    sendStageStartToGPL(pomXmlStructure, pipelineData, "100")

    try {
        // Get the JSON with app-id and info
        String namespace = pomXmlStructure.isArchProject() ? "ARCH" : "APP"
        String environment = pipelineData.bmxStructure.environment
        ICPDeployStructure deployStructure = new ICPDeployStructure('cxb-ab3cor','cxb-ab3app', environment)
        
        def icpAppNameAndInstance = calculateIcpAppAndInstanceName()
        def serviceIdICP = getServiceForComponent(icpAppNameAndInstance, deployStructure.envICP, namespace)

        if (serviceIdICP) {
            if (pomXmlStructure.isArchProject()) {
                serviceIdICP = "arch-service/" + serviceIdICP
            }

            microUrlGatewayForTesting = deployStructure.getUrlPrefixTesting() + deployStructure.getUrlSuffixIntegrationTesting("ALL") + "/" + serviceIdICP

            printOpen("La URL de la aplicación en cloud es  URL es: $microUrlGatewayForTesting", EchoLevel.DEBUG)
        } else {
            throw new Exception("No se ha podido obtener la url deployada de la aplicación.\nIntente desplegarla de nuevo y ejecutar de nuevo esta pipeline.")
        }
    } catch (Exception e) {
        sendStageEndToGPL(pomXmlStructure, pipelineData, "100", e.getMessage(), null, "error")
        throw e
    }

    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")
}

/**
 * Stage 'buildAndTestStep'
 */
def buildAndTestStep() {
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.BUILD_STARTED, KpiLifeCycleStatus.OK)

    sendStageStartToGPL(pomXmlStructure, pipelineData, "200")

    try {

        buildAndVerifyUTITWorkspace(pomXmlStructure, pipelineData, microUrlGatewayForTesting)

    } catch (Exception e) {

        kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.BUILD_FINISHED, KpiLifeCycleStatus.KO)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "200", null, null, "error")
        throw e
    }

    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.BUILD_FINISHED, KpiLifeCycleStatus.OK)


    sendStageEndToGPL(pomXmlStructure, pipelineData, "200")
}

/**
 * Stage 'sonar-scan'
 */
def sonarScanStep() {
    absisPipelineStageSonarScan(pomXmlStructure, pipelineData, "300")
}

/**
 * Stage 'sonar-quality-gate'
 */
def sonarQualityGateStep() {
    absisPipelineStageSonarQualityGate(pomXmlStructure, pipelineData, "400")
}

/**
 * Stage 'send-sonar-report'
 */
def sonarSendReportStep() {
    sendStageStartToGPL(pomXmlStructure, pipelineData, "500")

        def sonarResult = pipelineData.pipelineStructure.resultPipelineData.ifSonarQualityGateOK

        try {       
            def garAppType = pipelineData.getGarArtifactType().getGarName()
            def garApp = pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name))
            def destinationEmails = garGetAppResponsiblesEmails(garAppCode: garApp, garAppType: garAppType, maxInternalResponsibles: "all")
            def msg
            
            if (userId) {
                def garUserInfo = idecuaRoutingUtils.getUsuarioInfoFromMatricula(userId)

                if (garUserInfo?.mail) {
                    destinationEmails += ", " + garUserInfo.mail
                } else {
                    msg = "El usuario que originó la peticion de sonar '$userId' no se ha encontrado en GAR"
                    printOpen(msg, EchoLevel.ALL)
                }
            } else {
                msg = "El usuario que originó la peticion de sonar no se ha informado como parameteo de la pipeline"
                printOpen(msg, EchoLevel.ALL)
            }

            printOpen("EMAILS: $destinationEmails", EchoLevel.INFO)

            def sonarProjectName="${pipelineData.getGarArtifactType().getGarName()}.${pomXmlStructure.getSpringAppName()}"
    
            sonarSendReport apigwToken: APIGW_TOKEN, sonarUrl: GlobalVars.SONAR_URL, sonarToken: SONAR_TOKEN, projectKey: sonarProjectName, emailSender: "sonar@caixabanktech.com", emailReceivers: destinationEmails
            
            def msgFinal
            if (msg) {
                msgFinal = msg + "y no se le ha enviado,\nsi se ha hecho a los responsables de su aplicación."
            } else {
                msgFinal = "Se le ha enviado un mail, junto con los responsables de la aplicación, con el resultado del análisis de sonar."
            }

            sendStageEndToGPL(pomXmlStructure, pipelineData, "500", msgFinal + "\n\nEl resultado de sonar es: " + sonarResult, null, sonarResult ? "ended" : "warning")
        } catch (Exception sendException) {
            def msg = "No se ha podido obtener de GAR la información necesaria para el envío del reporte.\n" + 
                    "Puede que que los responsables o el usuario en GAR no estén debidamente configurados.\n" + 
                    "Revise la configuración en D* de su proyecto.\n\n"
                    "El resultado de sonar es: " + sonarResult

            printOpen(msg, EchoLevel.ERROR)

            sendStageEndToGPL(pomXmlStructure, pipelineData, "500", msg, null, "warning")
        }
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen("SUCCESS", EchoLevel.INFO)
    successPipeline = true

    if ( almEvent!=null ) {
        long endCallStartMillis = new Date().getTime()
        kpiLogger(almEvent.pipelineSuccess(endCallStartMillis-initCallStartMillis))
    }

    // Marcamos a false el notifyDeployment para esta pipeline
    sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, successPipeline, false)
    sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, pipelineData?.pipelineEndsWithWarning ? "warning" : "ended")
    if (pipelineData.getExecutionMode().invokeNextActionAuto()) {
        printOpen("Modo test activado en fase de analisis de sonar", EchoLevel.INFO)
        invokeNextJob(pipelineData, pomXmlStructure)
    }

    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_FINISHED, KpiLifeCycleStatus.OK)

}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("FAILURE", EchoLevel.INFO)

    debugInfo(pipelineParams, pomXmlStructure, pipelineData)


    if (initGpl == false) {
        //init pipeline in GPL with minimun parameters
        sendPipelineStartToGPL(pipelineData, pipelineParams)
        initGpl = true
    }

    if ( almEvent!=null ) {
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
/* ************************************************************************************************************************************** *\
 * Aux Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/**
 * Get Deployed version in Cloud
 */
def getDeployedAppVersion() {
    
    def infoUrl = microUrlGatewayForTesting + "/" + GlobalVars.ENDPOINT_INFO
    
    printOpen("Calling ${infoUrl}", EchoLevel.DEBUG)
    
    def response = sh(script: "curl -k -x http://${env.proxyHost}:${env.proxyPort} ${infoUrl} --connect-timeout 45", returnStdout: true)
    
    printOpen("Response: ${response}", EchoLevel.DEBUG)
    
    def jsonResponse
    try {
        jsonResponse = new groovy.json.JsonSlurper().parseText(response)
    } catch (Exception e) {
        printOpen("Response is not JSON format", EchoLevel.ERROR)
    }
    
    if(jsonResponse != null && jsonResponse.build != null && jsonResponse.build.version != null) {
        return jsonResponse.build.version
    }
    
    return null
}

/**
 *  Calculate ICP APP NAME
 */
def calculateIcpAppAndInstanceName() {
    def majorVersionPom = pomXmlStructure.getArtifactMajorVersion()
    def garApp = pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name))
    def garAppName = garApp.toUpperCase()

    def icpAppName = "$garAppName$majorVersionPom"
    def icpInstanceName = "$garAppName$majorVersionPom"

    if (pipelineData.branchStructure.featureNumber) {
        printOpen("It's a feature with this number id '$pipelineData.branchStructure.featureNumber'", EchoLevel.DEBUG)
        
        // Añadimo el nombre de la feature
        icpInstanceName += pipelineData.branchStructure.featureNumber
        
        if(icpInstanceName.length() > GlobalVars.LIMIT_LENGTH_FOR_PODNAME_WITHOUT_K8_SUFFIX) {
            icpInstanceName = icpInstanceName.substring(0, GlobalVars.LIMIT_LENGTH_FOR_PODNAME_WITHOUT_K8_SUFFIX)
        }

        if (pomXmlStructure.itContainsSampleApp()) {
            printOpen("Contains a sample app", EchoLevel.DEBUG)
            icpAppName += "S"
            icpInstanceName += "S"
        }

        icpAppName += ".*[0-9]{8}E"
    } else {
        if (pomXmlStructure.itContainsSampleApp()) {
            printOpen("Contains a sample app", EchoLevel.DEBUG)
            icpAppName += "S"
            icpInstanceName += "S"
        }
    }

    return [
        icpAppName: icpAppName.toLowerCase(),
        icpInstanceName: icpInstanceName.toLowerCase()
    ]
}

/**
 * Verifica si la librería tiene sample app
 */
def hasSampleApp(typeStr, subTypeStr) {
    def type = ArtifactType.valueOfType(typeStr)
    def subtype = ArtifactSubType.valueOfSubType(subTypeStr)

    return !(type == ArtifactType.SIMPLE && (subtype == ArtifactSubType.ARCH_LIB || subtype == ArtifactSubType.APP_LIB || subtype == ArtifactSubType.ARCH_CFG || subtype == ArtifactSubType.SRV_CFG))
}

/**
 * Obtiene el servicio de ICP para formar correctamente la url del micro
 */
def getServiceForComponent(Map icpAppNameAndInstance, String environment, String namespace) {
	def service = null
	def appICPId = GlobalVars.ICP_APP_ID_APPS
	def appICP = GlobalVars.ICP_APP_APPS
	
	//Vamos a recuperar la info de la app en ICP
	if ("ARCH" == namespace) {
		 appICP = GlobalVars.ICP_APP_ARCH
		 appICPId = GlobalVars.ICP_APP_ID_ARCH
	}

    ICPApiResponse response = sendRequestToICPApi("v1/application/$appICPId/component", null, "GET", appICP, "", false, false)

	if (response.statusCode >= 200 && response.statusCode < 300 && response.body != null && response.body.size() >= 1) {
	    printOpen("Query ICP Component was succefull $appICPId, $appICP", EchoLevel.ALL)
        printOpen("Use $icpAppNameAndInstance.icpAppName query to filter response ", EchoLevel.ALL)
        
        def componentResultIds = sh(script: "jq '.[] | select(.name | match(\"^(?i)$icpAppNameAndInstance.icpAppName\$\")) | .id' $env.WORKSPACE@tmp/outputCommand.json | jq -s", returnStdout: true)
        def componentIds = new JsonSlurper().parseText(componentResultIds)
        printOpen("ComponentID to Check: $componentIds", EchoLevel.ALL)

        if (componentIds) {
            def opts = new DumperOptions()
            opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
            Yaml yaml= new Yaml(opts)
            
            for(def componentId in componentIds) { 
                printOpen("Get Deployment for component '$appICP'", EchoLevel.ALL)
                
                ICPApiResponse deploymentResponse = sendRequestToICPApi("v1/application/PCLD/$appICP/component/$componentId/deploy/current/environment/${environment.toUpperCase()}/az/ALL", null, "GET", appICP, "", false, false)

                if (deploymentResponse.statusCode >= 200 && deploymentResponse.statusCode < 300 && deploymentResponse.body != null && deploymentResponse.body.size() >= 1) {
                    def deployment = yaml.load(deploymentResponse.body.values)
                    printOpen("Deployment $deployment", EchoLevel.ALL)
                    printOpen("Check deployment instance '${deployment?.absis?.app?.instance?.toLowerCase()}' - Calculated '$icpAppNameAndInstance.icpInstanceName'", EchoLevel.ALL)
                    if (icpAppNameAndInstance.icpInstanceName == deployment?.absis?.app?.instance?.toLowerCase()) {
                        service = deployment?.absis?.services?.envQualifier?.stable?.id
                        printOpen("Match find for this instanceId '$icpAppNameAndInstance.icpInstanceName' with componentId '$componentId' using filter '$icpAppNameAndInstance.icpAppName", EchoLevel.ALL)
                        break;
                    }
                } else {
                    printOpen("The ERROR will be controlled in other", EchoLevel.ALL)
                }
            } 

            if (!service) {
                printOpen("APP NOT FOUND")
            }
        } else {
            printOpen("APP NOT FOUND - List of componentIds was empty!!")
        }
    } else {
        printOpen("APP NOT FOUND - Query ICP has '$response.statusCode'!!")
    }

	return service	
}
