import com.caixabank.absis3.*

/**
 * Tipo de artefacto
 * SRV.LIB -  Library
 *
 */

def call(CGSPipelineData cgsPipelineData, ClientInfo cgsClientInfo) {

    printOpen("Preparing data to publish in catalog", EchoLevel.ALL)

    printOpen("Calculating target path for ${cgsClientInfo.artifactId}", EchoLevel.ALL)
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

    printOpen("Feature Number: ${cgsPipelineData.eventToPush}", EchoLevel.ALL)
    printOpen("Feature BranchType: ${cgsPipelineData.branchStructure.branchType}", EchoLevel.ALL)

    if (MavenVersionUtilities.isRelease(cgsClientInfo.artifactVersion)) {
        typeVersion = "RELEASE"
    } else if (MavenVersionUtilities.isSNAPSHOT(cgsClientInfo.artifactVersion)) {
        typeVersion = "SNAPSHOT"
    } else if (MavenVersionUtilities.isRCVersion(cgsClientInfo.artifactVersion)) {
        typeVersion = "RC"
    } else {
        typeVersion = "UNKNOWN"
    }

    def majorVersion = MavenVersionUtilities.getArtifactMajorVersion(cgsClientInfo.artifactVersion)
    def minorVersion = MavenVersionUtilities.getArtifactMinorVersion(cgsClientInfo.artifactVersion)
    def fixVersion = MavenVersionUtilities.getArtifactFixVersion(cgsClientInfo.artifactVersion)

    def body = [
            type            : cgsPipelineData.getGarArtifactType().getGarName(),
            aplicacion      : cgsClientInfo.getApp(cgsPipelineData.garArtifactType),
            nombreComponente: cgsClientInfo.artifactId,
            major           : majorVersion,
            minor           : minorVersion,
            fix             : fixVersion,
            groupId         : cgsClientInfo.groupId,
            buildCode       : cgsPipelineData.buildCode,
            typeVersion     : typeVersion,
            sourceCode      : cgsPipelineData.gitUrl,
            readme          : cgsPipelineData.gitUrl,
            nexus           : cgsPipelineData.getRouteToDeployedJar(),
            versionLog      : versionLog,
            clienteJava     : clienteJava,
            faq             : faq,
            tutoriales      : tutoriales,
            glosario        : glosario,
            bestPractices   : bestPractices,
            configuracion   : config,
            listDependencias: parsedDependencies,
            listEndPoints   : parsedEndpoints,
            feature         : cgsPipelineData.getFeature("${majorVersion}.${minorVersion}.${fixVersion}")
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
