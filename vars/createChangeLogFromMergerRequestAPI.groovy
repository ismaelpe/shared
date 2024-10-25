import com.project.alm.*

def call(PipelineData pipelineData, PomXmlStructure pomXml) {

    String version = pomXml.getArtifactVersionWithoutQualifier()
    String projectName = pomXml.getArtifactName()
    String title = null// "${env.gitlabMergeRequestTitle}"
    String description = null//"${env.gitlabMergeRequestDescription}"
    String feature = "${pipelineData.eventToPush}"

    if (pipelineData.originPushBranchToMaster == null) return false

    /**
     * Validar en la MR
     */

    def projectId = gitlabApiGetProjectId(pipelineData, pomXml)

    GitlabAPIResponse mergeRequests =
        sendRequestToGitLabAPI(pipelineData, pomXml,
            [
                url: "${GlobalVars.gitlabApiDomain}${projectId}/merge_requests?state=merged&sort=desc&source_branch=${pipelineData.originPushBranchToMaster}",
                contentType: "application/x-www-form-urlencoded"
            ]
        )
	if (mergeRequests.status>400) {
		printOpen("Error en la consulta contra el API de OCP ${mergeRequests.status} El contenido es ${mergeRequests.content}",EchoLevel.INFO)
		return false
	}else {
		printOpen("El resultado es de  ${mergeRequests.status} El rcontenido es ${mergeRequests.content}  ${mergeRequests.asJson}",EchoLevel.INFO)
	}
	
    if (mergeRequests.asJson.size() > 0) {

        printOpen("Existen MR mergeadas sobre la misma rama, cojemos la primera ${mergeRequests.asJson.get(0)}", EchoLevel.ALL)

        title = mergeRequests.asJson.getAt(0).title
        description = mergeRequests.asJson.getAt(0).description

    } else return false


    //Recuperamos el fichero, y vemos si se ha creado el separador de linea, sino lo creamos
    def fileName = "${WORKSPACE}/CHANGELOG.md"
    def fileExists = fileExists fileName
    if (!fileExists) {
        printOpen("Creating changelog file due to it does not exist", EchoLevel.ALL)
        writeFile file: fileName, text: ""
    }

    String textFile = readFile fileName

    printOpen("Changelog with file: $textFile", EchoLevel.ALL)
    printOpen("New information identified for changelog, Version: $version Feature: $feature Title: $title Description: $description", EchoLevel.ALL)

    ChangelogFile changeLogFile = null
    String finalText = null
    changeLogFile = new ChangelogFile(projectName, textFile)
    changeLogFile.initialize()

    boolean featureAdded = changeLogFile.addFeature(version, feature, title, description)

    finalText = changeLogFile.toString()
    if (featureAdded) {
        printOpen("Final changelog file:\n$finalText", EchoLevel.ALL)
        writeFile file: fileName, text: "$finalText"
    } else {
        printOpen("Feature has already added in changelog, generated content is:\n$finalText", EchoLevel.ALL)
    }
    return featureAdded

}

