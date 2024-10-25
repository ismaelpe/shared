import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage
import groovy.json.JsonSlurper


def getPodsInfo(String namespace, String environment, String center) {
    def url = "${GlobalVars.ICP_PRO}/api/publisher/v1/api/application/PCLD/${namespace}/environment/${environment}/az/${center}?object=POD"

    def command = buildCurlCommand("GET", url, env.ICP_CERT, env.ICP_PASS)

    def response = checkEndpoint.executeCurlWithRetries(command, [
        maxRetries: GlobalVars.ICP_API_REQUEST_MAX_RETRIES,
        kpiAlmEvent: new KpiAlmEvent(
            null, null,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.ICPAPI_HTTP_CURL, null, null, null, environment)
    ])

    if (response.status == '200') {
        return [status: response.status, body: new JsonSlurper().parseText(response.body)]
    }
    error "getPodsInfo failed\nstatus: ${response.status}\nbody:${response.body}"
    
}

def restartPod(String componentName, String podId, String namespace, String environment, String center) {

    int retryNumber = GlobalVars.ICP_API_REQUEST_MAX_RETRIES
    componentName = componentName.toUpperCase()

    def url = "${GlobalVars.ICP_PRO}/api/publisher/v1/api/application/PCLD/${namespace}/component/${componentName}/environment/${environment}/az/${center}/pod/${podId}/restart"
   
    waitUntil(initialRecurrencePeriod: 15000) {
        def command = buildCurlCommand("DELETE", url, env.ICP_CERT, ICP_PASS)

        def response = checkEndpoint.executeCurlWithRetries(command, [
            maxRetries: GlobalVars.ICP_API_REQUEST_MAX_RETRIES,
            kpiAlmEvent: new KpiAlmEvent(
                            null, null,
                            KpiAlmEventStage.UNDEFINED,
                            KpiAlmEventOperation.ICPAPI_HTTP_CURL, null, null, null, environment)
        ])

        if (response.status != '200') {

            printOpen("restartPod failed\nstatus: ${response?.status}\nbody:${response?.body}", EchoLevel.ALL)

            boolean shallWeStop = --retryNumber == 0

            if (shallWeStop) throw new RuntimeException("restartPod failed\nstatus: ${response?.status}\nbody:${response?.body}")

            return false
        }

        printOpen("restartPod succeeded", EchoLevel.ALL)
        return true
    }

}

def buildCurlCommand(String method, String url, def headers = [], String CERTIFICATE, String PASSWORD) {
    def cmd = "curl -k --write-out '%{http_code}' -o curlOutput.json -s " +
        "-X ${method} ${url} --connect-timeout ${GlobalVars.ICP_API_MAX_TIMEOUT_PER_REQUEST} " +
        "--cert $CERTIFICATE:$PASSWORD " +
        "-H accept:*/* -H Content-Type:application/json"

    for(header in headers) {
        cmd += " -H ${header}"
    }

    return cmd    
}
