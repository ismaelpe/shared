import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.HttpRequestUtilities
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventErrorCode
import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventStatusType
import com.project.alm.KpiAlmEventSubOperation
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure

/**
 * Ejecuta el refresh
 *
 * @param microurl direccion http del micro
 * @return void
 */
def call(String microurl, PomXmlStructure pomXml, PipelineData pipelineData) {
	
	printOpen("Iniciando runActuatorRefresh", EchoLevel.ALL)

	def whiteListApps = "${env.ALM_SERVICES_SKIP_CHECK_HEALTH_REFRESH}".split(";")
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
                maxRetries: GlobalVars.Cloud_RUN_ACTUATOR_REFRESH_MAX_RETRIES,
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
