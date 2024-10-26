import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

@Field String userIdParam
@Field String enviromentParam
@Field String dataCenterParam
@Field String appNameParam
@Field String archAppNameParam
@Field boolean successPipeline
@Field List<SyntheticTestStructure> appsList
@Field List<SyntheticTestStructure> appsWithErrorsToReport
@Field StringBuilder logmessageReport

//Pipeline para realizar test sinteticos de servicios desplegados en BMX
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    userIdParam = params.userId
    enviromentParam = params.enviroment
    dataCenterParam = params.dataCenter
    appNameParam = params.appName
    archAppNameParam = params.archAppName
    successPipeline = false

    logmessageReport = new StringBuilder()

    pipeline {
        agent { node(almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
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
            stage('stage-init') {
                steps {
                    stageInitStep()
                }
            }
            stage('create-list') {
                steps {
                    createListStep()
                }
            }
            stage('stage-filter') {
                steps {
                    stageFilterStep()
                }
            }
            stage('stage-test-alive') {
                steps {
                    stageTestAliveStep()
                }
            }
            stage('stage-execute-tests') {
                steps {
                    stageExecuteTestsStep()
                }
            }
            stage('stage-report') {
                steps {
                    stageReportStep()
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
 * Stage 'stageInitStep'
 */
def stageInitStep() {
    String message = null
    message = '====================================================\n'
    message += '>\n'
    message += '>      1 - STAGE-INIT\n'
    message += ">      param user=${userIdParam}\n"
    message += ">      param enviroment=${enviromentParam}\n"
    message += ">      param dataCenter=${dataCenterParam}\n"
    message += ">      param appName=${appNameParam}\n"
    message += ">      param archAppName=${archAppNameParam}\n"
    message += '>\n'
    message += '>\n'
    message += '====================================================\n'
    printOpen("${message}", EchoLevel.INFO)
}

/**
 * Stage 'createListStep'
 */
def createListStep() {
    def whiteListArch = "${env.ALM_ARCH_SERVICES_ALLOWED_SYNTHETIC_TESTS}".split(';')
    def whiteListApps = "${env.ALM_APPS_SERVICES_ALLOWED_SYNTHETIC_TESTS}".split(';')

    appsList = new ArrayList<SyntheticTestStructure>()

    appsList = new BmxUtilities().addStringList(appsList, whiteListArch, enviromentParam, dataCenterParam, true)
    appsList = new BmxUtilities().addStringList(appsList, whiteListApps, enviromentParam, dataCenterParam, false)
}

/**
 * Stage 'stageFilterStep'
 */
def stageFilterStep() {
    String message = null
    message = '====================================================\n'
    message += '>\n'
    message += '>      2 - STAGE-FILTER\n'
    message += '>\n'
    message += '>\n'
    message += '====================================================\n'
    printOpen("${message}", EchoLevel.INFO)

    StringBuilder logger = new StringBuilder()

    //if appname or archAppNameParam is no blank
    if (appNameParam?.trim() || archAppNameParam?.trim()) {
        def selectedApps = appNameParam?.trim() ? "${appNameParam}".split(';') : []
        def selectedArchApps = archAppNameParam?.trim() ? "${archAppNameParam}"?.split(';') : []

        printOpen('filter selected list apps', EchoLevel.INFO)
        appsList.clear()
        appsList = new BmxUtilities().addStringList(appsList, selectedArchApps, enviromentParam, dataCenterParam, true)
        appsList = new BmxUtilities().addStringList(appsList, selectedApps, enviromentParam, dataCenterParam, false)
        printOpen("apps size -  ${appsList.size()}", EchoLevel.INFO)
    }

    logger = new StringBuilder()
    logger.append('All apps after filters:\n')
    for (SyntheticTestStructure item:appsList) {
        logger.append("- ${item.appName}\n")
    }
    printOpen(logger.toString(), EchoLevel.DEBUG)
}

/**
 * Stage 'stageTestAliveStep'
 */
def stageTestAliveStep() {
    String message = null
    message = '====================================================\n'
    message += '>\n'
    message += '>      3 - STAGE-TEST-ALIVE\n'
    message += '>\n'
    message += '>\n'
    message += '====================================================\n'
    printOpen("${message}", EchoLevel.INFO)

    //the goal in this 'for' is discard all endpoints that we cannot test : item.resultOK = false
    for (SyntheticTestStructure item : appsList) {
        printOpen("HTTP URL ${item.urlCloud}", EchoLevel.INFO)
        def url = item.urlCloud + '/actuator/info'
        def response = null

        try {
            def urlParameters = [:]
            urlParameters.needsProxy = true
            urlParameters.url = "${url}"
            urlParameters.parseResponse = true
            urlParameters.inputData = item

            response = httpRequestUtils.send(urlParameters)
        //response = httpRequest url: "${url}", httpProxy: "${env.https_proxy}"
        } catch (Exception e) {
            printOpen("Error ${e}", EchoLevel.ERROR)
            item.errorMessage = 'error maybe is not alive'
            item.resultOK = false
        }
        if (!item.resultOK) {
            continue
        }
        try {
            def json = response.content
            item.pomVersion = "${json.build.version}"
            item.pomArtifactId = "${json.build.artifact}"
            item.pomGroup = "${json.build.group}"
        } catch (Exception e) {
            item.errorMessage = 'error parsing /actuator/info maybe not enough info'
            item.resultOK = false
        }
    }
}

/**
 * Stage 'stageExecuteTestsStep'
 */
def stageExecuteTestsStep() {
    String message = null
    message = '====================================================\n'
    message += '>\n'
    message += '>      5 - STAGE-EXECUTE-TESTS\n'
    message += '>\n'
    message += '>\n'
    message += '====================================================\n'
    printOpen("${message}", EchoLevel.INFO)

    for (SyntheticTestStructure item : appsList) {
        if (item.resultOK) {
            StringBuilder loggerTest = new StringBuilder()
            loggerTest.append('**** EXECUTE INTEGRATION TEST FOR:\n')
            loggerTest.append("- artifactId:${item.pomArtifactId}\n")
            loggerTest.append("- version:${item.pomVersion}\n")
            loggerTest.append("- enpoint:${item.urlCloud}\n")
            printOpen(loggerTest.toString(), EchoLevel.DEBUG)

            //se vuelve a generar el pom en el disco porque fue machacado por el for del stage anterior
            generateSyntheticTestPom(item)

            String pathToSyntheticTestPom = item.pathToSyntheticTestPom
            String url = item.urlCloud
            String goal = 'verify'

            try {
                configFileProvider([configFile(fileId: 'alm-maven-settings-with-singulares', variable: 'MAVEN_SETTINGS')]) {
                    //Cloud
                    def cmd = ''
                    if (item.isArchMicro) {
                        cmd = "mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort}  -s $MAVEN_SETTINGS ${GlobalVars.GLOBAL_MVN_PARAMS} -f ${pathToSyntheticTestPom}  clean ${goal} -Dmicro-url=${item.urlCloud} -Dskip-it=false -Dskip-ut=true"
                    } else {
                        cmd = "mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort}  -s $MAVEN_SETTINGS ${GlobalVars.GLOBAL_MVN_PARAMS} -f ${pathToSyntheticTestPom}  clean ${goal} -Dmicro-url=${item.urlCloud} -Dskip-it=false -Dskip-ut=true -P micro-app"
                    }

                    runMavenCommand(cmd)
                }
            } catch (Exception e) {
                item.errorMessage = 'mvn verify finish with errors'
                item.resultOK = false
            }
        }
    }
}

/**
 * Stage 'stageReportStep'
 */
def stageReportStep() {
    String message = null
    message = '====================================================\n'
    message += '>\n'
    message += '>      5 - STAGE-REPORT\n'
    message += '>\n'
    message += '>\n'
    message += '====================================================\n'
    printOpen("${message}", EchoLevel.INFO)

    appsWithErrorsToReport = new BmxUtilities().cloneOnlyErrors(appsList)
    printOpen("size appsWithErrorsToReport -  ${appsWithErrorsToReport.size()}", EchoLevel.INFO)

    logmessageReport.append('**SYNTHETIC TEST RESULTS: apps with some error***\n')
    for (SyntheticTestStructure item : appsWithErrorsToReport) {
        logmessageReport.append("test.appName=         ${item.appName}\n")
        logmessageReport.append("test.pomArtifactId= ${item.pomArtifactId}\n")
        logmessageReport.append("test.pomVersion=     ${item.pomVersion}\n")
        logmessageReport.append("test.pomGroup=      ${item.pomGroup}\n")
        logmessageReport.append("test.urlCloud=          ${item.urlCloud}\n")
        logmessageReport.append("test.errorMessage=  ${item.errorMessage}\n")
        logmessageReport.append(' -------------- \n')
    }
    if (appsWithErrorsToReport.size() == 0) {
        logmessageReport.append('ANY ERROR FOUND!\n')
    }

    logmessageReport.append('**********************************************\n')
    printOpen(logmessageReport.toString(), EchoLevel.DEBUG)

    successPipeline = true
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen("Success pipeline ${successPipeline}", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen("Failure pipeline ${successPipeline}", EchoLevel.ERROR)
}

/**
 * Stage 'endiPipelineAlwaysStep'
 */
def endiPipelineAlwaysStep() {
    if (appsWithErrorsToReport.size() > 0) {
        String bodyEmail = '<p>Buenos dias, </p>Resultado de la última ejecución de test sintéticos</p>'
        bodyEmail = bodyEmail + "<p>Para más información ver ${env.BUILD_URL}</p>" + "<p>${Strings.toHtml(logmessageReport.toString())}</p>"
        printOpen("Text email: ${bodyEmail}", EchoLevel.INFO)

        emailext(body: "${bodyEmail}"
                            , mimeType: 'text/html'
                            , replyTo: ''
                            , from: "${GlobalVars.EMAIL_FROM_ALM}"
                            , recipientProviders: [[$class: 'DevelopersRecipientProvider']]
                            , to: "${env.ALM_SERVICES_EMAIL_SYNTHETIC_TEST_DISTRIBUTION_LIST}"
                            , subject: "[Alm3] Resultado Jenkins Test Sintéticos - enviroment:${enviromentParam} - dataCenter:${dataCenterParam} - ${appNameParam} - build:${env.BUILD_NUMBER}")
    }

    cleanWorkspace()
}
