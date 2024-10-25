import com.project.alm.ApiManagerTechnicalServicesRequest
import com.project.alm.ApiManagerTechnicalServicesResponse
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars


def call(ApiManagerTechnicalServicesRequest request) {

    ApiManagerTechnicalServicesResponse response = new ApiManagerTechnicalServicesResponse()

    String technicalServicesInputAsJson = prepareJsonFrom(request)

    timeout(GlobalVars.DEFAULT_ABSIS3_MS_REQUEST_RETRIES_TIMEOUT) {
        waitUntil(initialRecurrencePeriod: 15000) {

            withCredentials([string(credentialsId: "${request.absis3TokenName}", variable: 'tokenAbsis3')]) {

                def profileHeader =
                    goesThroughExternalGateway(request.apiManagerUri) ?
                        "Authorization: Bearer ${tokenAbsis3}" :
                        "x-absis-auto-profile: CBK;OFFICE"

                def curlCall =
                    "curl --location --request POST -s -w \"%{http_code}\" \"${request.apiManagerUri}\" \\\n" +
                        "--proxy ${GlobalVars.proxyCaixa} \\\n" +
                        "--insecure \\\n" +
                        "--connect-timeout ${request.timeout} \\\n" +
                        "-H \"Content-Type: multipart/form-data\" \\\n" +
                        "-H \"${profileHeader}\" \\\n" +
                        "-F \"technicalServicesInput=${technicalServicesInputAsJson};type=application/json\""

                if (request.contractShouldBeSent) {

                    curlCall += " \\\n" +
                        "-F \"files=@${request.contractPath};filename=${request.technicalServicesInput.technicalServices.get(0).specificationFileName}\""

                }

                String curlResponse = sh(script: "${curlCall}", returnStdout: true).trim()

                String responseAsJson = curlResponse.substring(0, curlResponse.length() - 3)
                Integer statusCode = new Integer(curlResponse.substring(curlResponse.length() - 3))

                printOpen("Got response ${statusCode} from ApiManager TechnicalServices:\n\n${responseAsJson}", EchoLevel.DEBUG)

                response.statusCode = statusCode
                response.responseAsJson = responseAsJson

                if (statusCode >= 500) {

                    return false

                } else if (statusCode >= 400) {

                    error("sendRequestToApiManager.call: A client error has returned by the service:\n\n${responseAsJson}")

                } else {
                    
                    return true

                }
            }
        }
        
    }
    
    return response
}

private String prepareJsonFrom(ApiManagerTechnicalServicesRequest request) {

    //FIXME: You can get rid of this when JsonGenerator is available
    def technicalServicesCleanedList = []
    request.technicalServicesInput.technicalServices.each {
        technicalServicesCleanedList.add(it.properties.findAll { it.key != 'class' && (it.value || it.value != null) })
    }
    request.technicalServicesInput.technicalServices = technicalServicesCleanedList

    def technicalServicesInputMap = [
        "json" : request.technicalServicesInput.properties.findAll { it.key != 'class' && (it.value || it.value != null) }
    ]

    def technicalServicesInputAsJson = groovy.json.JsonOutput.toJson(technicalServicesInputMap.get("json"))

    printOpen("Performing ApiManager TechnicalServices registration for\n\n${technicalServicesInputAsJson}\n\nand contractPath ${request.contractPath}", EchoLevel.DEBUG)

    return technicalServicesInputAsJson.replace("\"", "\\\"")

}

private boolean goesThroughExternalGateway(String uri) {

    return uri.contains(".internal.caixabank.com")

}
