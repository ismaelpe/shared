import com.project.alm.GlobalVars

def call(String method, String url, def body, String environment, Map parameters = [:]) {
	return sendRequestToAbsis3MS(method,url,body, environment, 404, parameters)
}

def call(String method, String url, def body, String environment, int maxStatusCodeAllowed, Map parameters = [:]) {

    String environ = environment == 'eden' ? 'DEV' : environment

    withCredentials([string(credentialsId: "ALM_TOKEN_${environ.toUpperCase()}_V2 ", variable: 'tokenAbsis3')]) {

        def customHeaders = parameters?.customHeaders ? parameters?.customHeaders : []
        customHeaders += [name: 'Authorization', value: "Bearer ${tokenAbsis3}"]

        parameters += [
            retryLoopTimeout: GlobalVars.DEFAULT_ALM_MS_REQUEST_RETRIES_TIMEOUT,
            customHeaders: customHeaders,
            consoleLogResponseBody: true,
            validResponseCodes: "100:"+maxStatusCodeAllowed,
            timeout: GlobalVars.ALM_MS_TIMEOUT
        ]
		return sendRequestToService(method, url, null, body, parameters)
    }
}
