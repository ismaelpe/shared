import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.HttpRequestUtilities
import com.caixabank.absis3.KpiAlmEvent
import com.caixabank.absis3.KpiAlmEventErrorCode
import com.caixabank.absis3.KpiAlmEventOperation
import com.caixabank.absis3.KpiAlmEventStage
import com.caixabank.absis3.KpiAlmEventStatusType
import com.caixabank.absis3.KpiAlmEventSubOperation
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure

/**
 * Ejecuta el refresh
 *
 * @param microurl direccion http del micro
 * @return void
 */
def call(String microurl, PomXmlStructure pomXml, PipelineData pipelineData) {
	
	printOpen("Iniciando runActuatorRefresh", EchoLevel.ALL)

	def whiteListApps = "${env.ABSIS3_SERVICES_SKIP_CHECK_HEALTH_REFRESH}".split(";")
	List<String> list = Arrays.asList(whiteListApps)
	boolean skip = false
	for ( String item : list ) {
		if (microurl.contains(item)) {
			skip=true
			break
		}
	}
	if (skip) {
		 printOpen("Skip refresh check", EchoLevel.ALL)
		 return
	}

    def url = "${microurl}/" + GlobalVars.ENDPOINT_REFRESH

    printOpen("Checking application refresh at ${url}...", EchoLevel.DEBUG)

	try {

        def response = sendRequestToService('POST', url, null, null,
            [
                consoleLogResponseBody: true,
                maxRetries: GlobalVars.ICP_RUN_ACTUATOR_REFRESH_MAX_RETRIES,
                timeout: GlobalVars.ACTUATOR_REFRESH_TIMEOUT,
                retryLoopTimeout: GlobalVars.ACTUATOR_REFRESH_RETRY_LOOP_TIMEOUT,
                validResponseCodes: "100:299",
                kpiAlmEvent: new KpiAlmEvent(
                    pomXml, pipelineData,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.RUN_ACTUATOR_REFRESH),
                errorIsNotRetryable: { Exception exception -> ! isNoHttpResponseExceptionDueToFailureToRespond(exception.getMessage()) }
            ])
		//def response = sendRequest(url, [httpMode: 'POST', kpiAlmEvent: kpi])
        printOpen("Application refreshed.", EchoLevel.INFO)

        return response

	} catch (Exception e) {

        def message = e.getMessage()
        if (isNoHttpResponseExceptionDueToFailureToRespond(message)) {

            createMaximoAndThrow.genericFailureWhileDoingActuatorRefresh(pipelineData, pomXml, url, e)

        }

        printOpen("Error refreshing the application: ${message}", EchoLevel.ERROR)
		throw e
	}
	
}

boolean isNoHttpResponseExceptionDueToFailureToRespond(String log) {

    return log?.contains("org.apache.http.NoHttpResponseException") && log?.contains("failed to respond")

}
