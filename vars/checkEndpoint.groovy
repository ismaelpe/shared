import com.project.alm.*

def call(String uri, Map parameters = [:]) {

    Map curlParameters = [
        insecure: false,
        environment: calculateEnvironment(uri)
    ]

    curlParameters += parameters

    boolean insecure = curlParameters.insecure

    printOpen("Verificando endpoint ${uri} ${insecure ? 'con' : 'sin'} --insecure", EchoLevel.DEBUG)
    def command = "curl -x http://${env.proxyHost}:${env.proxyPort} -s -o curlOutput.json -w \"%{http_code}\"${insecure ? ' --insecure ' : ' '}${uri} --connect-timeout ${GlobalVars.PRO_ENDPOINTS_CHECK_TIMEOUT}"
    def response = executeCurlWithRetries(command, curlParameters)
    printOpen("${uri} ha respondido HTTP ${response?.status} con body\n\n${response?.body}\n", EchoLevel.DEBUG)

    return response
}

def executeCurlWithRetries(String cmd, Map customParameters = [:]) {

    Map parameters = [
        maxRetries: GlobalVars.PRO_ENDPOINTS_CHECK_MAX_RETRIES,
        kpiAlmEvent: new KpiAlmEvent(
                null, null,
                KpiAlmEventStage.UNDEFINED,
                KpiAlmEventOperation.CHECK_ENDPOINT, null, null, null, customParameters?.environment)
    ]

    parameters += customParameters
    KpiAlmEvent kpiAlmEvent = parameters?.kpiAlmEvent

    def response
    def lastError
    int retryNumber = parameters?.maxRetries

    long singleCallDuration
    long wholeCallDuration

    long wholeCallStartMillis = new Date().getTime()
    long singleCallStartMillis

    try {

        waitUntil(initialRecurrencePeriod: 15000) {

            try {

                singleCallStartMillis = new Date().getTime()

                def responseStatusCode = sh(script: cmd, returnStdout:true)
                printOpen("Return status code: $responseStatusCode", EchoLevel.DEBUG)

                // Es posible que si alguna API no devuelve contenido el 'curlOutput.json' no se escriba en disco
                // causando error, por ello lo seteamos con contenido nulo si no existe
                def contentResponse
                try {
                    contentResponse = sh(script: "cat curlOutput.json", returnStdout:true)
                    sh(script: "rm curlOutput.json", returnStdout:true )
                } catch(Exception error) {
                    printOpen("curlOutput.json is empty", EchoLevel.DEBUG)
                    contentResponse = "{}"
                }               

                response = [status: responseStatusCode, body: contentResponse]

                long singleCallEndMillis =  new Date().getTime()
                singleCallDuration = singleCallEndMillis - singleCallStartMillis
                wholeCallDuration = singleCallEndMillis - wholeCallStartMillis

                kpiLogger(kpiAlmEvent.requestSuccess(singleCallDuration))
                kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))

                return true

            } catch(err) {

                long singleCallEndMillis =  new Date().getTime()
                singleCallDuration = singleCallEndMillis - singleCallStartMillis

                kpiLogger(kpiAlmEvent.requestFail(singleCallDuration))

                boolean shallWeStop = retryNumber-- == 0

                printOpen(Utilities.prettyException(err), EchoLevel.ERROR)

                if (shallWeStop) {

                    throw err

                }

                lastError = err

                sleep 5
                kpiLogger(kpiAlmEvent.retry())

                return false

            }

        }

    } catch (Exception e) {

        kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))

        throw e

    }

    return response
}

String calculateEnvironment(String uri) {

    if (uri?.toLowerCase().contains(".pro.")) return "pro"
    else if (uri?.toLowerCase().contains(".pre.")) return "pre"
    else if (uri?.toLowerCase().contains(".dev.") && (uri?.toLowerCase().contains("-de") || uri?.toLowerCase().contains("-us"))) return "eden"
    else if (uri?.toLowerCase().contains(".dev.")) return "dev"
    return null

}
