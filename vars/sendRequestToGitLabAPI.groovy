import com.caixabank.absis3.GitlabAPIResponse
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.HttpRequestUtilities
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure

def call(Map parameters = [:]) {
    sendRequestToGitLabAPI(null, null, parameters)
}

def call(PipelineData pipelineData, PomXmlStructure pomXml, Map parameters = [:]) {

    if ( ! parameters.url ) {
        error "Cannot do call to Gitlab API with an empty URL!"
    }

    def httpMethod = parameters.httpMode ? parameters.httpMode : parameters.requestBody ? 'POST' : 'GET'
    def contentType = parameters.contentType ? parameters.contentType : 'application/json'
    def validResponseCodes = parameters.validResponseCodes ? parameters.validResponseCodes.split(",") : "100:400".split(",")
    def isJsonInputData = parameters.isJsonInputData ?: false

    def response
    int numberOfRetriesWithException = 0

    try {

        timeout(GlobalVars.GITLAB_API_REQUEST_RETRIES_TIMEOUT) {
            waitUntil(initialRecurrencePeriod: 15000) {
                try {
                    withCredentials([
                        usernamePassword(credentialsId: 'GITLAB_CREDENTIALS', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD'),
                        string(credentialsId: 'GITLAB_API_SECRET_TOKEN', variable: 'GITLAB_API_TOKEN')
                    ]) {

                        if (parameters.requestBody) {
							/*
                            response = httpRequest consoleLogResponseBody: true,
                                contentType: "${contentType}",
                                httpMode: "${httpMethod}",
                                ignoreSslErrors: true,
                                customHeaders: [[name: 'Private-Token', value: "${GITLAB_API_TOKEN}"], [name: 'Accept', value: "application/json"]],
                                validResponseCodes: '100:599',
                                requestBody: "${parameters.requestBody}",
                                url: "${parameters.url}",
                                httpProxy: "http://proxyserv.svb.lacaixa.es:8080"*/
								
							def urlParameters=[:]
								urlParameters.needsProxy=true
								urlParameters.url=parameters.url 
								urlParameters.parseResponse=false
								urlParameters.inputData=parameters.requestBody
								urlParameters.verb=httpMethod
								urlParameters.ignoreSslErrors=true
								urlParameters.validResponsesCodes='100:599'
								urlParameters.contentType=contentType
								urlParameters.accept='application/json'
								urlParameters.isJsonInputData=isJsonInputData
								urlParameters.headers=[[name: 'Private-Token', value: "${GITLAB_API_TOKEN}"]]
														
							response=httpRequestUtils.send(urlParameters)
							
                        } else {
							/*
                            response = httpRequest consoleLogResponseBody: true,
                                contentType: "${contentType}",
                                httpMode: "${httpMethod}",
                                ignoreSslErrors: true,
                                customHeaders: [[name: 'Private-Token', value: "${GITLAB_API_TOKEN}"], [name: 'Accept', value: "application/json"]],
                                validResponseCodes: '100:599',
                                url: "${parameters.url}",
                                httpProxy: "http://proxyserv.svb.lacaixa.es:8080"
								*/
							def urlParameters=[:]
								urlParameters.needsProxy=true
								urlParameters.url=parameters.url
								urlParameters.parseResponse=false							
								urlParameters.verb=httpMethod
								urlParameters.ignoreSslErrors=true
								urlParameters.validResponsesCodes='100:599'
								urlParameters.contentType=contentType
								urlParameters.accept='application/json'								
								urlParameters.headers=[[name: 'Private-Token', value: "${GITLAB_API_TOKEN}"]]
														
							response=httpRequestUtils.send(urlParameters)
                        }

                    }
                    return HttpRequestUtilities.responseCodeIsValid(response?.status, validResponseCodes)

                } catch (Exception e) {

                    if (numberOfRetriesWithException < GlobalVars.GITLAB_API_REQUEST_MAX_RETRIES_DUE_TO_EXCEPTION) {
                        numberOfRetriesWithException++
                        return false
                    }

                    if (response) {
						if (response.content) {
							throw new Exception("Unexpected exception(s) when trying to do a request to Git. Last error was:\n\nResponse HTTP Code: ${response?.status}\n\nResponse body:\n\n${HttpRequestUtilities.prettyPrint(response?.content)}")
						}else {
							throw new Exception("Unexpected exception(s) when trying to do a request to Git. Last error was:\n\nResponse HTTP Code: ${response?.status}\n\n")
						}
                    }

                    throw new Exception("Unexpected exception(s) when trying to do a request to Git. There was not enough time to do a single request")

                }
            }
        }

    } catch(org.jenkinsci.plugins.workflow.steps.FlowInterruptedException fie) {

        if (response) {
			if (response.content) {
				throw new Exception("Timeout when trying to do a request to Gitlab. Last error was:\n\nResponse HTTP Code: ${response?.status}\n\nResponse body:\n\n${HttpRequestUtilities.prettyPrint(response?.content)}")
			}else {
				throw new Exception("Timeout when trying to do a request to Gitlab. Last error was:\n\nResponse HTTP Code: ${response?.status}\n\n")
			}
        }

        throw new Exception("Timeout when trying to do a request to Gitlab. There was not enough time to do a single request")
    }

    return asGitlabAPIResponse(response)

}

private GitlabAPIResponse asGitlabAPIResponse(def response) {

    GitlabAPIResponse apiResponse = new GitlabAPIResponse()

    apiResponse.status = response.status
    apiResponse.content = response.content
	if (response.content==null) {
		apiResponse.asJson = HttpRequestUtilities.asObject(response.message)
	}else {
		apiResponse.asJson = HttpRequestUtilities.asObject(response.content)
	}


    return apiResponse

}
