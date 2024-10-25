import com.caixabank.absis3.*

def call(PipelineData pipelineData, PomXmlStructure pomXml) {

    String targetBranch = pipelineData.targetBranch
    String gitLabActionType = pipelineData.gitAction
    String version = pomXml.getArtifactVersionWithoutQualifier()
    String projectName = pomXml.getArtifactName()
    String title = "${env.gitlabMergeRequestTitle}"
    String description = "${env.gitlabMergeRequestDescription}"
    String feature = "${pipelineData.branchStructure.featureNumber}"

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

