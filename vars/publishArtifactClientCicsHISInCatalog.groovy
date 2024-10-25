import com.project.alm.*
import groovy.json.JsonSlurperClassic

/**
 * Tipo de artefacto
 * SRV.LIB -  Library
 *
 */

def call(CicsHISPipelineData cicsPipelineData, ClientCicsHISInfo clientCicsHISInfo) {

    printOpen("Preparing data to publish in catalog", EchoLevel.ALL)

    printOpen("Calculating target path for ${clientCicsHISInfo.artifactName}", EchoLevel.ALL)
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

    printOpen("Feature Number: ${cicsPipelineData.eventToPush}", EchoLevel.ALL)
    printOpen("Feature BranchType: ${cicsPipelineData.branchStructure.branchType}", EchoLevel.ALL)

    if (clientCicsHISInfo.artifactVersionQualifier == null || clientCicsHISInfo.artifactVersionQualifier == "") {
        typeVersion = "RELEASE"
    } else if (clientCicsHISInfo.isSNAPSHOT()) {
        typeVersion = "SNAPSHOT"
    } else if (clientCicsHISInfo.isRCVersion()) {
        typeVersion = "RC"
    } else {
        typeVersion = "UNKNOWN"
    }

    def body = [
            type            : cicsPipelineData.getGarArtifactType().getGarName(),
            //type            : GarAppType.LIBRARY.getGarName(),
            aplicacion      : clientCicsHISInfo.getApp(cicsPipelineData.garArtifactType),//pomXml.applicationName?.trim() ? pomXml.applicationName :  pomXml.artifactName,
            nombreComponente: clientCicsHISInfo.artifactName,
            major           : clientCicsHISInfo.artifactMajorVersion,
            minor           : clientCicsHISInfo.artifactMinorVersion,
            fix             : clientCicsHISInfo.artifactFixVersion,
            groupId         : clientCicsHISInfo.groupId,
            buildCode       : cicsPipelineData.buildCode,
            typeVersion     : typeVersion,
            sourceCode      : cicsPipelineData.gitUrl,
            readme          : cicsPipelineData.gitUrl,
            nexus           : cicsPipelineData.getRouteToDeployedJar(),
            versionLog      : versionLog,
            clienteJava     : clienteJava,
            faq             : faq,
            tutoriales      : tutoriales,
            glosario        : glosario,
            bestPractices   : bestPractices,
            configuracion   : config,
            listDependencias: parsedDependencies,
            listEndPoints   : parsedEndpoints,
            feature         : cicsPipelineData.getFeature("${clientCicsHISInfo.artifactMajorVersion}.${clientCicsHISInfo.artifactMinorVersion}.${clientCicsHISInfo.artifactFixVersion}")
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
