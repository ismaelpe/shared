import com.project.alm.*
import groovy.json.JsonSlurperClassic

/**
 * Tipos de artefactos aceptados por consola
 *
 * ARQ.MIA – Micro Architecture
 * ARQ.MAP – Micro Architecture Plugin
 * ARQ.LIB – Architecture Library
 * SRV.DS – Data Service
 * SRV.MS – Micro Service
 * SRV.LIB - Library
 *
 */

def call(PipelineData pipelineData, PomXmlStructure pomXml) {
	publishArtifactInCatalog(pipelineData,pomXml,null)
}


def call(PipelineData pipelineData, PomXmlStructure pomXml, CloudStateUtility cloudStateUtilitity, boolean updateBuildPath = false) {

    printOpen("Preparing data to publish in catalog", EchoLevel.INFO)

    printOpen("Calculating target path for ${pomXml.artifactName}", EchoLevel.DEBUG)

    String dependenciesPath = GlobalVars.JSON_DEPENDENCIES
    String sourceFolder = dependenciesPath

    if (pomXml.artifactType == ArtifactType.AGREGADOR) {
        printOpen("Artifact is agregador with subtype ${pomXml.artifactSubType}", EchoLevel.DEBUG)
        if (pomXml.artifactSubType == ArtifactSubType.MICRO_ARCH) {
            printOpen("Artifact is micro", EchoLevel.ALL)
            sourceFolder = pomXml.artifactMicro + "/" + dependenciesPath
        } else if (pomXml.artifactSubType == ArtifactSubType.PLUGIN) {
            printOpen("Artifact is sample app", EchoLevel.ALL)
            sourceFolder = pomXml.artifactSampleApp + "/" + dependenciesPath
        }
    }

    printOpen("Reading dependencies from ${sourceFolder}", EchoLevel.DEBUG)

    def exists = fileExists sourceFolder

    def parsedDependencies = []

    if (exists) {
        def dependenciesJson = readFile(file: sourceFolder)
        parsedDependencies = new JsonSlurperClassic().parseText(dependenciesJson)
    }

    String sourceFolderEndpoints = GlobalVars.JSON_ENDPOINTS

    printOpen("Reading endpoints from ${sourceFolderEndpoints}", EchoLevel.DEBUG)
    def parsedEndpoints = []
    //try {
    printOpen("Previo al endpoints 1 ", EchoLevel.ALL)

    boolean existsEndpoints = fileExists sourceFolderEndpoints

    printOpen("Previo al endpoints ${existsEndpoints} ", EchoLevel.ALL)

    if (existsEndpoints) {

        String endpointsJson = sh(returnStdout: true, script: "cat ${sourceFolderEndpoints} 2>/dev/null ")
        printOpen("Contenido ${endpointsJson}", EchoLevel.DEBUG)

        def jsonEndpoints = readFile(file: sourceFolderEndpoints)
        parsedEndpoints = new JsonSlurperClassic().parseText(jsonEndpoints)
    }

    String swagger = ""
    String clienteJava = ""
    String versionLog = ""
    String config = ""
	String sourceCode = ""
	String readme = ""
	String restDocs = ""
	String javaDoc = ""


    String tag = "master"

    if (pipelineData.branchStructure.branchType == BranchType.RELEASE || pipelineData.branchStructure.branchType == BranchType.HOTFIX || pipelineData.branchStructure.branchType == BranchType.CONFIGFIX) {
        tag = "v_" + pomXml.artifactVersion;
    }

    String gitUrl = pipelineData.gitUrl.substring(0, pipelineData.gitUrl.length() - 4)

	if (pipelineData.branchStructure.branchType == BranchType.CONFIGFIX) {

		config = gitUrl + "/tree/" + tag + "/${GlobalVars.RESOURCE_PATH}"

	} else if ((pomXml.isMicro() || pomXml.isLibrary()) && !pomXml.isSampleApp()) {
        if (pomXml.isApplication()) {
            clienteJava = pipelineData.getRouteToClientDeployedJar()
            swagger = gitUrl + "/blob/" + tag + "/contract/swagger-micro-contract.yaml"
		}
		if (pomXml.isMicro()) {
			String microUrl = pipelineData.deployStructure.getUrlPrefixApiGateway()
			if (pomXml.isArchProject()) {
				microUrl += "/arch-service"
			}
			microUrl += "/"+BmxUtilities.calculateArtifactId(pomXml,pipelineData.branchStructure,true).toLowerCase()
			restDocs = microUrl + "/ui/restdocs/index.html"
			javaDoc = microUrl + "/ui/javadoc/index.html"
		}
		
        versionLog = gitUrl + "/blob/" + tag + "/CHANGELOG.md"
		readme = gitUrl + "/blob/" + tag + "/README.md"
        config = gitUrl + "/tree/" + tag + "/${GlobalVars.RESOURCE_PATH}"
		sourceCode = gitUrl + ".git"
    } else {
		sourceCode = gitUrl + ".git"
	}

    //fixme what about documentation product?
    String typeVersion = ""

    printOpen("Feature Number: ${pipelineData.eventToPush}", EchoLevel.DEBUG)
    printOpen("Feature BranchType: ${pipelineData.branchStructure.branchType}", EchoLevel.DEBUG)

    if (pomXml.artifactVersionQualifier == null || pomXml.artifactVersionQualifier == "") {
        typeVersion = "RELEASE"
    } else if (pomXml.isSNAPSHOT()) {
        typeVersion = "SNAPSHOT"
    } else if (pomXml.isRCVersion()) {
        typeVersion = "RC"
    } else {
        typeVersion = "UNKNOWN"
    }

	
    def body = [
        gitUrl          : "${gitUrl}.git",
        type            : pipelineData.getGarArtifactType().getGarName(),
        aplicacion      : pomXml.getApp(pipelineData.garArtifactType),//pomXml.applicationName?.trim() ? pomXml.applicationName :  pomXml.artifactName,
        nombreComponente: pomXml.artifactName,
        major           : pomXml.artifactMajorVersion,
        minor           : pomXml.artifactMinorVersion,
        fix             : pomXml.artifactFixVersion,
        groupId         : pomXml.groupId,
        buildCode       : pipelineData.buildCode,
        typeVersion     : typeVersion,
        sourceCode      : sourceCode,
		readme          : readme,
		javaDoc         : javaDoc,
		restDocs        : restDocs,
        nexus           : pipelineData.getRouteToDeployedJar(),
        versionLog      : versionLog,
        clienteJava     : clienteJava,
        swagger         : GlobalVars.URL_API_PORTAL,
        configuracion   : config,
        listDependencias: parsedDependencies,
        listEndPoints   : parsedEndpoints,
        feature         : pipelineData.getFeature("${pomXml.artifactMajorVersion}.${pomXml.artifactMinorVersion}.${pomXml.artifactFixVersion}")
    ]

    boolean weHaveToSendToCatalog = GlobalVars.SEND_TO_CATALOG

    def url = idecuaRoutingUtils.catalogPipelineUrl();

    if (weHaveToSendToCatalog) {

        def response

        try {

            printOpen("Sending to GSA", EchoLevel.INFO)
            response = sendRequestToService('POST', url, "", body,
                [
                    kpiAlmEvent: new KpiAlmEvent(
                        pomXml, pipelineData,
                        KpiAlmEventStage.UNDEFINED,
                        KpiAlmEventOperation.CATALOG_HTTP_CALL)
                ])

 	    publishArtifactInCatMsv (body,pipelineData,pomXml,cloudStateUtilitity,updateBuildPath)

        } catch (Exception e) {
            printOpen(Utilities.prettyException(e), EchoLevel.ERROR)
            Utilities.prettyException(e)
            throw new Exception("Unexpected response when sending to GSA (${response?.status})! + ${e.getMessage()}", e)
        }


    } else {
        def toJson = {
            input ->
                groovy.json.JsonOutput.toJson(input)
        }

        printOpen('Skipping publish artifact in catalog ' + toJson(body), EchoLevel.INFO)
    }

}

def call(PipelineData pipelineData, IClientInfo clientInfo) {

    printOpen("Preparing data to publish in catalog", EchoLevel.ALL)
    printOpen("Calculating target path for ${clientInfo.getArtifactId()}", EchoLevel.ALL)

    def parsedDependencies = []
    def parsedEndpoints = []

    String clienteJava = ""
    String versionLog = ""
    String faq = ""
    String tutoriales = ""
    String glosario = ""
    String bestPractices = ""
    String config = ""

    String unqualifiedVersion = MavenVersionUtilities.getArtifactVersionWithoutQualifier(clientInfo.getArtifactVersion())

    printOpen("Feature Number: ${pipelineData.eventToPush}", EchoLevel.DEBUG)
    printOpen("Feature BranchType: ${pipelineData.branchStructure.branchType}", EchoLevel.DEBUG)

    def body = [
        type            : pipelineData.getGarArtifactType().getGarName(),
        aplicacion      : clientInfo.getApp(pipelineData.garArtifactType),
        nombreComponente: clientInfo.getArtifactId(),
        major           : MavenVersionUtilities.getMajor(clientInfo.getArtifactVersion()),
        minor           : MavenVersionUtilities.getMinor(clientInfo.getArtifactVersion()),
        fix             : MavenVersionUtilities.getPatch(clientInfo.getArtifactVersion()),
        groupId         : clientInfo.getGroupId(),
        buildCode       : pipelineData.buildCode,
        typeVersion     : MavenVersionUtilities.getQualifier(clientInfo.getArtifactVersion()),
        sourceCode      : pipelineData.gitUrl,
        readme          : pipelineData.gitUrl,
        nexus           : pipelineData.getRouteToDeployedJar(),
        versionLog      : versionLog,
        clienteJava     : clienteJava,
        faq             : faq,
        tutoriales      : tutoriales,
        glosario        : glosario,
        bestPractices   : bestPractices,
        configuracion   : config,
        listDependencias: parsedDependencies,
        listEndPoints   : parsedEndpoints,
        feature         : pipelineData.getFeature(unqualifiedVersion)
    ]

    def response =  sendRequestToCatalog(body)
	
    //Enviem la publicacio contra el cataleg de Msv
    publishArtifactInCatMsv (body,pipelineData,null,null)
	
	
    return response

}

def sendRequestToCatalog(def body) {

    boolean weHaveToSendToCatalog = GlobalVars.SEND_TO_CATALOG

    if (weHaveToSendToCatalog) {

        def url = idecuaRoutingUtils.catalogPipelineUrl()

        printOpen("Body to be sent to GSA URL ${url}:\n\n${body}", EchoLevel.ALL)

        def response

        try {

            response = sendRequestToService('POST', url, "", body,
                [
                    kpiAlmEvent: new KpiAlmEvent(
                        null, null,
                        KpiAlmEventStage.UNDEFINED,
                        KpiAlmEventOperation.CATALOG_HTTP_CALL)
                ])

        } catch (Exception e) {
            throw new Exception("Unexpected response when connecting to GSA (${response?.status})! + ${e.getMessage()}")
        }

        return response

    } else {
        def toJson = { input -> groovy.json.JsonOutput.toJson(input) }
        body = toJson(body)
        printOpen("Skipping artifact publication in catalog:\n\n${body}", EchoLevel.ALL)
    }
}

