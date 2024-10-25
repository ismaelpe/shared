import com.project.alm.*

def call(PomXmlStructure artifactPom, BranchStructure branchStructure, DeployStructure deployStructure, String endpoint) {
    String artifactRoute = BmxUtilities.calculateRoute(artifactPom, branchStructure)
    printOpen("Lanzando peticion contra http://${artifactRoute}.${deployStructure.getVipaPerCenter()}/${endpoint}", EchoLevel.ALL)

    def response = sh(script: "curl -x http://${env.proxyHost}:${env.proxyPort} http://${artifactRoute}.${deployStructure.getVipaPerCenter()}/${endpoint} --connect-timeout 45", returnStdout: true)
    return new groovy.json.JsonSlurper().parseText(response)
}