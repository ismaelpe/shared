import com.caixabank.absis3.*

private call(String method, String url, String param, def body, Map parameters = [:]) {

    def request
    def response
    HttpRequestException lastFailure

    KpiAlmEvent kpiAlmEvent = parameters?.kpiAlmEvent

    long singleCallDuration
    long wholeCallDuration

    long wholeCallStartMillis = new Date().getTime()
    long singleCallStartMillis

    boolean requestWentOk = false
    def unrecoverableError

    def echoLevel = parameters?.echoLevel ? parameters?.echoLevel : EchoLevel.DEBUG

    try {

        int retryNumber = parameters?.maxRetries ? parameters.maxRetries : GlobalVars.HTTP_REQUEST_MAX_RETRIES
        int retryLoopTimeout = parameters?.retryLoopTimeout ? parameters.retryLoopTimeout : GlobalVars.HTTP_REQUEST_RETRY_LOOP_TIMEOUT
        int singleCallTimeout = parameters?.timeout ? parameters.timeout : GlobalVars.HTTP_REQUEST_SINGLE_CALL_TIMEOUT

        def consoleLogResponseBody = parameters?.consoleLogResponseBody ? parameters?.consoleLogResponseBody : false
        def customHeaders = parameters?.customHeaders ? parameters?.customHeaders : []
        def validResponseCodes = parameters?.validResponseCodes ? parameters?.validResponseCodes : '100:399'
        def ignoreSslErrors = parameters?.ignoreSslErrors ? parameters?.ignoreSslErrors : false

        def responseIsNotRetryableFunction = parameters?.responseIsNotRetryable ? parameters?.responseIsNotRetryable : { resp -> resp?.status == 404 }
        def errorIsNotRetryableFunction = parameters?.errorIsNotRetryable ? parameters?.errorIsNotRetryable : { exception -> true }

        param = param ? param : ""

        timeout(retryLoopTimeout) {

            waitUntil(initialRecurrencePeriod: 15000) {

                try {

                    printOpen( "Performing HTTP request to ${url} for method ${method} and param ${param}", echoLevel)

                    requestWentOk = false
                    singleCallStartMillis = new Date().getTime()

                    if (body == null) {

                        printOpen("Request: No Body!", echoLevel)
/*
                        response = httpRequest consoleLogResponseBody: consoleLogResponseBody,
                            contentType: 'APPLICATION_JSON',
                            httpMode: method,
                            url: url + param,
                            httpProxy: "${env.https_proxy}",
                            ignoreSslErrors: ignoreSslErrors,
                            validResponseCodes: '100:599',
                            timeout: singleCallTimeout,
                            customHeaders: customHeaders
                            */
							
						def urlParameters=[:]
						urlParameters.needsProxy=true
						urlParameters.url=url + param
						urlParameters.parseResponse=true
						urlParameters.inputData=null
						urlParameters.timeout=singleCallTimeout
						urlParameters.verb=method
						urlParameters.ignoreSslErrors=false
						urlParameters.validResponsesCodes=validResponseCodes
						urlParameters.headers=customHeaders
                        urlParameters.isJsonInputData=parameters.isJsonInputData
                        urlParameters.hideCommand=parameters.hideCommand
            									
						response=httpRequestUtils.send(urlParameters, echoLevel)

                    } else {
/*
                        request = sanitizeRequest(groovy.json.JsonOutput.toJson(body))

                        response = httpRequest consoleLogResponseBody: consoleLogResponseBody,
                            contentType: 'APPLICATION_JSON',
                            requestBody: request,
                            httpMode: method,
                            url: url + param,
                            httpProxy: "${env.https_proxy}",
                            ignoreSslErrors: ignoreSslErrors,
                            validResponseCodes: '100:599',
                            timeout: singleCallTimeout,
                            customHeaders: customHeaders*/
						
						printOpen("With body: ${body}", echoLevel)
							
						def urlParameters=[:]
						urlParameters.needsProxy=true
						urlParameters.url=url + param
						urlParameters.parseResponse=true
						urlParameters.inputData=body
						urlParameters.timeout=singleCallTimeout
						urlParameters.verb=method
						urlParameters.ignoreSslErrors=false
						urlParameters.validResponsesCodes=validResponseCodes
						urlParameters.headers=customHeaders
                        urlParameters.isJsonInputData=parameters.isJsonInputData
                        urlParameters.hideCommand=parameters.hideCommand
						
						response=httpRequestUtils.send(urlParameters, echoLevel)

                    }

                    requestWentOk = true

                    if (response == null || response?.statusCode == null) {

                        throw new HttpRequestException(request, response, "No response from the service")

                    } else if (requestWentOk) {

                        return true

                    } else {

                        throw new HttpRequestException(request, response,
                            "Unexpected HTTP code on response when sending request to the service (Response HTTP Code: ${response?.status})\n" +
                                "Response body:\n\n" +
                                "${HttpRequestUtilities.prettyPrint(response?.content)}")

                    }

                } catch (HttpRequestException hre) {

                    boolean shallWeStop = responseIsNotRetryableFunction(response) || retryNumber-- == 0

                    if (shallWeStop) {

                        throw hre

                    }

                    lastFailure = hre

                    if (kpiAlmEvent) {

                        kpiLogger(kpiAlmEvent.retry())

                    }

                    return false

                } catch (Exception e) {

                    if (kpiAlmEvent) {

                        long wholeCallEndMillis = new Date().getTime()
                        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                        kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))

                    }

                    if (errorIsNotRetryableFunction(e)) throw e

                    return false

                } finally {

                    if (kpiAlmEvent) {

                        long singleCallEndMillis = new Date().getTime()
                        singleCallDuration = singleCallEndMillis - singleCallStartMillis

                        if (requestWentOk) {

                            long wholeCallEndMillis = new Date().getTime()
                            wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                            kpiLogger(kpiAlmEvent.requestSuccess(singleCallDuration))
                            kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))

                        } else {

                            kpiAlmEvent.errorCode = response?.status == 404 ? KpiAlmEventErrorCode.HTTP_404 : KpiAlmEventErrorCode.UNDEFINED
                            kpiLogger(kpiAlmEvent.requestFail(singleCallDuration))

                        }

                    }

                }

            }

        }

    } catch(org.jenkinsci.plugins.workflow.steps.FlowInterruptedException fie) {

        unrecoverableError = fie

        if (lastFailure) {

            printOpen("The execution of the HTTP call with retries has failed due to an execution timeout.\n" +
                "Log of the last exception is attached:\n\n" +
                "${Utilities.prettyException(fie)}\n\n" +
                "${Utilities.prettyException(lastFailure)}", EchoLevel.ERROR)
            throw lastFailure

        }

        printOpen("The execution of the HTTP call with retries has failed due to an execution timeout.\n" +
            "No specific exception could not be catched:\n\n${Utilities.prettyException(fie)}", EchoLevel.ERROR)

        throw fie

    } catch(Exception e) {

        unrecoverableError = e

        printOpen("The execution of the HTTP call with retries has failed due to an unexpected exception:\n\n" +
            "${Utilities.prettyException(e)}", EchoLevel.ERROR)

        throw e

    } finally {

		if (response!=null) {
			printOpen("Response:\n${HttpRequestUtilities.prettyPrint(response)}", echoLevel)
		}

        if (kpiAlmEvent && !requestWentOk) {

            String errorCode = unrecoverableError instanceof org.jenkinsci.plugins.workflow.steps.FlowInterruptedException ?
                KpiAlmEventErrorCode.TIMEOUT_BLOCK_EXPIRED :
                KpiAlmEventErrorCode.UNDEFINED

            long wholeCallEndMillis = new Date().getTime()
            wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

            kpiLogger(kpiAlmEvent.callFail(wholeCallDuration, errorCode))

        }

    }

    return response

}

// We'll convert "valor":null strings to "valor":"null" in the JSON for attributes which we know for sure will cause an error in GPL
// We could sanitize all "valor":null strings but we do not know the impact
String sanitizeRequest(def request) {

    def sanitized = request.replace('{"nombre":"userEmail","valor":null}', '{"nombre":"userEmail","valor":"null"}')
    sanitized = sanitized.replace('{"nombre":"user","valor":null}', '{"nombre":"user","valor":"null"}')
    sanitized = sanitized.replace('{"nombre":"versionParam","valor":null}', '{"nombre":"versionParam","valor":"null"}')
    sanitized = sanitized.replace('{"nombre":"artifactParam","valor":null}', '{"nombre":"artifactParam","valor":"null"}')
    sanitized = sanitized.replace('{"nombre":"groupParam","valor":null}', '{"nombre":"groupParam","valor":"null"}')

    return sanitized
}
