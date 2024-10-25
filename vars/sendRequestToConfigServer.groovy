import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.GplRequestStatus
import com.caixabank.absis3.GplUtilities
import com.caixabank.absis3.HttpRequestUtilities
import groovy.json.JsonSlurper
import java.util.Date



def call(String method, String path, String environment,String datacenter) {

    printOpen("Performing ABSIS3 request to ConfigServer ${path} for method ${method}", EchoLevel.ALL)
 
    String environ=environment
    
    if (environment=='eden') environ='DEV'

    def response = null
    GplRequestStatus statusGpl = new GplRequestStatus()
    def fecha = null
    
    timeout(GlobalVars.DEFAULT_ABSIS3_MS_REQUEST_RETRIES_TIMEOUT) {
        waitUntil(initialRecurrencePeriod: 15000) {
            try {

                fecha = new Date()
                printOpen("Absis3 ConfigServer iteration at date ${fecha} the environment ${environ}", EchoLevel.ALL)
                
                withCredentials([usernamePassword(credentialsId: 'CONFIGSERVER_CREDENTIALS', usernameVariable: 'CONFIGSERVER_USERNAME', passwordVariable: 'CONFIGSERVER_PASSWORD')]) {
                    String url = GlobalVars.CONFIG_SERVER_URL.replace("{environment}",environ).replace("{datacenter}",datacenter)
                    url = url + path
                    
                    String microHttpBasicCredentials = "${CONFIGSERVER_USERNAME}:${CONFIGSERVER_PASSWORD}"
                    String auth = microHttpBasicCredentials.bytes.encodeBase64().toString()

                    response = httpRequest consoleLogResponseBody: true, contentType: 'APPLICATION_JSON', httpMode: method, url: url, httpProxy: "http://proxyserv.svb.lacaixa.es:8080", validResponseCodes: "${GlobalVars.ABSIS3_MS_VALID_RESPONSE_STATUS}", timeout: GlobalVars.ABSIS3_MS_TIMEOUT, customHeaders: [[name: 'Authorization', value: "Basic ${auth}"]]
                    printOpen("Invoking url [${method}] ${url}", EchoLevel.ALL)
                }

                return shallWeStopDoingRequests(response, statusGpl, 404)

            } catch (Exception e) {

                return GplUtilities.evaluateResponse(null, statusGpl)

            } finally {
                printOpen("Response:\n${response?.content}")
            }
        }
    }

    if (response == null || response?.status == null) {

        throw new Exception("No response from Absis3 ConfigServer")

        //call to endpoint /refresh-bus responsee is 204
    } else if (response.status == 200 || response.status == 204) {

        return response

    } else {

        throw new Exception("Unexpected HTTP code on response when sending request to OpenServices Config Server (Response HTTP Code: ${response?.status})\nResponse body:\n\n${HttpRequestUtilities.prettyPrint(response?.content)}")

    }

}

private shallWeStopDoingRequests(def response, GplRequestStatus statusGpl, int maxStatusCodeAllowed) {

    if (response.status <= maxStatusCodeAllowed) {

        return true

    } else {

        return GplUtilities.evaluateResponse(response, statusGpl)

    }

}
