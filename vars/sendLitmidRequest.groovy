import com.caixabank.absis3.*
import groovy.json.JsonOutput

/**
* Obtiene las traducciones de errors de la aplicacion
*/
def call(artifactName, typeApp) {
    def translationsMap = []
    def litmidDir = "litmid"
    def zipFileName = "$typeApp.${artifactName}.zip"
    def url = "$GlobalVars.LITMID_URL/$typeApp/$artifactName"

    printOpen("Litmid url is $url", EchoLevel.DEBUG)

    def command = buildCurlCommand("GET", url, zipFileName)

    try {
        sh "mkdir $litmidDir"
        printOpen("Dir $litmidDir created!", EchoLevel.DEBUG)

        response = checkEndpoint.executeCurlWithRetries(command, [
            maxRetries: GlobalVars.LITMID_API_REQUEST_MAX_RETRIES,
            kpiAlmEvent: new KpiAlmEvent(
                null, null,
                KpiAlmEventStage.UNDEFINED,
                KpiAlmEventOperation.LITMIDAPI_HTTP_CURL, null, null, null)
        ])
        
        if (response.status == '200') {	
            try {
                sh "unzip -o $zipFileName -d $litmidDir"

                translationsMap = retreiveLitmidLiterals(litmidDir, artifactName, typeApp)

                printOpen("Litmid Results: ${translationsMap}", EchoLevel.DEBUG)
            } catch (Exception exception) {
                printOpen("The file $zipFileName is empty, nothing to do!", EchoLevel.INFO)
            }			
        }
    } catch (Exception exception) {
        printOpen("Error detected: $exception", EchoLevel.ERROR)
    } finally {	
        sh "rm -rf $litmidDir"
        sh "rm -f $zipFileName"	

        printOpen("$litmidDir, $zipFileName deleted!!", EchoLevel.DEBUG)
    }

    return translationsMap;
}

/**
* Llamada a LITMID mediante CURL
* @param method
* @param url
* @param filename
* @param headers
* @return
*/
def buildCurlCommand(String method, String url, String filename, def headers = []) {
    def cmd = "curl -x http://$env.proxyHost:$env.proxyPort -k --write-out '%{http_code}' -o $filename -s " +
    "-X $method $url " +
    "--connect-timeout $GlobalVars.LITMID_API_MAX_TIMEOUT_PER_REQUEST "
    "-H accept:*/* " +
    "-H Content-Type:application/json"
        
    // Por si es necesario a futuro
    for(header in headers) {
        cmd += " -H ${header}"
    }

    return cmd
}
