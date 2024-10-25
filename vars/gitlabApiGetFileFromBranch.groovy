import com.caixabank.absis3.*
import groovy.json.JsonSlurperClassic

def call(PipelineData pipelineData, PomXmlStructure pomXml, def projectId, String relativeFilePath, String branch) {

    def encodedFilePath = URLEncoder.encode(relativeFilePath, "UTF-8")

    def requestUrl = "${GlobalVars.gitlabApiDomain}${projectId}/repository/files/${encodedFilePath}?ref=${branch}"

    GitlabAPIResponse fileInfo =
        sendRequestToGitLabAPI(pipelineData, pomXml,
            [
                url: requestUrl
            ]
        )

    printOpen("Retrieved JSON representation from ${requestUrl}: ${fileInfo.asJson}", EchoLevel.ALL)

    return fileInfo.asJson
}
