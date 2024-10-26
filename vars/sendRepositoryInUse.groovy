import com.project.alm.GlobalVars

//FIXME deprecate this
def call(String name, Boolean marked) {

    /*def url = GlobalVars.CONTRACT_MICRO_URL + "/gitlab"

    def body = [
            name  : name,
            marked: marked,
    ]

    def bodyJson = groovy.json.JsonOutput.toJson(body)
    println("Request: " + bodyJson)

    int attemptNum = 0
    timeout(time: 5, unit: 'MINUTES') {
        retry(10) {
            if (++attemptNum > 1) {
                sleep(time: 30000, unit: "MILLISECONDS")
            }
            def response = httpRequest consoleLogResponseBody: true, contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: bodyJson, url: url, httpProxy: "http://proxyserv.svb.digitalscale.es:8080", validResponseCodes: '200'
            println("Response: " + response)
        }
    }*/
}
