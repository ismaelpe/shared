import com.caixabank.absis3.*

/**
 * Tipo de artefacto
 * SRV.LIB -  Library
 *
 */

def call(TANDEMPipelineData tandemPipelineData, ClientInfo tandemClientInfo) {

    printOpen("Preparing data to publish in catalog", EchoLevel.INFO)

    printOpen("Calculating target path for ${tandemClientInfo.artifactId}", EchoLevel.ALL)
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

    printOpen("Feature Number: ${tandemPipelineData.eventToPush}", EchoLevel.ALL)
    printOpen("Feature BranchType: ${tandemPipelineData.branchStructure.branchType}", EchoLevel.ALL)

    if (MavenVersionUtilities.isRelease(tandemClientInfo.artifactVersion)) {
        typeVersion = "RELEASE"
    } else if (MavenVersionUtilities.isSNAPSHOT(tandemClientInfo.artifactVersion)) {
        typeVersion = "SNAPSHOT"
    } else if (MavenVersionUtilities.isRCVersion(tandemClientInfo.artifactVersion)) {
        typeVersion = "RC"
    } else {
        typeVersion = "UNKNOWN"
    }

    def majorVersion = MavenVersionUtilities.getArtifactMajorVersion(tandemClientInfo.artifactVersion)
    def minorVersion = MavenVersionUtilities.getArtifactMinorVersion(tandemClientInfo.artifactVersion)
    def fixVersion = MavenVersionUtilities.getArtifactFixVersion(tandemClientInfo.artifactVersion)

    def body = [
            type            : tandemPipelineData.getGarArtifactType().getGarName(),
            aplicacion      : tandemClientInfo.getApp(tandemPipelineData.garArtifactType),
            nombreComponente: tandemClientInfo.artifactId,
            major           : majorVersion,
            minor           : minorVersion,
            fix             : fixVersion,
            groupId         : tandemClientInfo.groupId,
            buildCode       : tandemPipelineData.buildCode,
            typeVersion     : typeVersion,
            sourceCode      : tandemPipelineData.gitUrl,
            readme          : tandemPipelineData.gitUrl,
            nexus           : tandemPipelineData.getRouteToDeployedJar(),
            versionLog      : versionLog,
            clienteJava     : clienteJava,
            faq             : faq,
            tutoriales      : tutoriales,
            glosario        : glosario,
            bestPractices   : bestPractices,
            configuracion   : config,
            listDependencias: parsedDependencies,
            listEndPoints   : parsedEndpoints,
//            feature         : tandemPipelineData.getFeature("${majorVersion}.${minorVersion}.${fixVersion}"),
			feature         : null,
			buildPath       : env.JOB_NAME
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

            printOpen("Sending to GSA...", EchoLevel.INFO)
            response = sendRequestToService('POST', url, "", body)
			printOpen("The artefact has been published in catalog", EchoLevel.INFO)

        } catch (Exception e) {
            throw new Exception("Unexpected response when connecting to GSA (${response?.status})! + ${e.getMessage()}")
        }

    } else {
        def toJson = {
            input ->
                groovy.json.JsonOutput.toJson(input)
        }

        printOpen('Skipping publish artifact in catalog ' + toJson(body), EchoLevel.INFO)
    }

}
