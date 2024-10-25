import com.caixabank.absis3.BuildType
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars

def call(String enviroment, String groupId, String artifactId, String version, String classifier) {


    boolean result = false

    String url = "${GlobalVars.NEXUS_URL}artifactory/api/storage/${GlobalVars.NEXUS_PUBLIC_REPO_NAME}/${groupId.replace(".", "/")}/${artifactId}/${version}/${artifactId}-${version}-${classifier}.jar"

    def response = null

    try {
        response = httpRequest url: "${url}", 
							   httpProxy: "http://proxyserv.svb.lacaixa.es:8080"
    } catch (Exception e) {
        printOpen(e.getMessage(), EchoLevel.ERROR)
        result = false
    }

    try {
        def json = new groovy.json.JsonSlurper().parseText(response.content)
        result = (json.downloadUri != null)
    } catch (Exception e) {
        printOpen(e.getMessage(), EchoLevel.ERROR)
        result = false
    }
    return result
}