import com.caixabank.absis3.*

/**
 * Tipo de artefacto
 * SRV.LIB -  Library
 *
 */

def call(ADSPipelineData adsPipelineData, ClientInfo adsClientInfo) {

    printOpen("Preparing data to publish in catalog", EchoLevel.ALL)

    printOpen("Calculating target path for ${adsClientInfo.artifactId}", EchoLevel.ALL)
    def parsedDependencies = []
    def parsedEndpoints = []

    String clienteJava = ""
    String versionLog = ""
    String faq = ""
    String tutoriales = ""
    String glosario = ""
    String bestPractices = ""
    String config = ""

    //fixme what about documentation product?
    String typeVersion = ""

    printOpen("Feature Number: ${adsPipelineData.eventToPush}", EchoLevel.ALL)
    printOpen("Feature BranchType: ${adsPipelineData.branchStructure.branchType}", EchoLevel.ALL)

    if (MavenVersionUtilities.isRelease(adsClientInfo.artifactVersion)) {
        typeVersion = "RELEASE"
    } else if (MavenVersionUtilities.isSNAPSHOT(adsClientInfo.artifactVersion)) {
        typeVersion = "SNAPSHOT"
    } else if (MavenVersionUtilities.isRCVersion(adsClientInfo.artifactVersion)) {
        typeVersion = "RC"
    } else {
        typeVersion = "UNKNOWN"
    }

    def majorVersion = MavenVersionUtilities.getArtifactMajorVersion(adsClientInfo.artifactVersion)
    def minorVersion = MavenVersionUtilities.getArtifactMinorVersion(adsClientInfo.artifactVersion)
    def fixVersion = MavenVersionUtilities.getArtifactFixVersion(adsClientInfo.artifactVersion)

    def body = [
            type            : adsPipelineData.getGarArtifactType().getGarName(),
            aplicacion      : adsClientInfo.getApp(adsPipelineData.garArtifactType),
            nombreComponente: adsClientInfo.artifactId,
            major           : majorVersion,
            minor           : minorVersion,
            fix             : fixVersion,
            groupId         : adsClientInfo.groupId,
            buildCode       : adsPipelineData.buildCode,
            typeVersion     : typeVersion,
            sourceCode      : adsPipelineData.gitUrl,
            readme          : adsPipelineData.gitUrl,
            nexus           : adsPipelineData.getRouteToDeployedJar(),
            versionLog      : versionLog,
            clienteJava     : clienteJava,
            faq             : faq,
            tutoriales      : tutoriales,
            glosario        : glosario,
            bestPractices   : bestPractices,
            configuracion   : config,
            listDependencias: parsedDependencies,
            listEndPoints   : parsedEndpoints,
            feature         : adsPipelineData.getFeature("${majorVersion}.${minorVersion}.${fixVersion}")
    ]


    boolean weHaveToSendToCatalog =
            GlobalVars.SEND_TO_CATALOG //&&
    //((deployingToPRE && GlobalVars.PUSH_CATALOG_PRE) || (deployingToPRO && GlobalVars.PUSH_CATALOG_PRO))

    def url = idecuaRoutingUtils.catalogPipelineUrl();
    if (weHaveToSendToCatalog) {

        def toJson = {
            input ->
                groovy.json.JsonOutput.toJson(input)
        }
        printOpen("Sending " + toJson(body), EchoLevel.DEBUG)

        def response

        try {

            printOpen("Sending to GSA", EchoLevel.DEBUG)
            response = sendRequestToService('POST', url, "", body)

        } catch (Exception e) {
            throw new Exception("Unexpected response when connecting to GSA (${response?.status})! + ${e.getMessage()}")
        }

    } else {
        def toJson = {
            input ->
                groovy.json.JsonOutput.toJson(input)
        }

        printOpen('Skipping publish artifact in catalog ' + toJson(body), EchoLevel.DEBUG)
    }

}
