import com.caixabank.absis3.*
import groovy.json.JsonSlurperClassic

def call(PipelineData pipelineData, PomXmlStructure pomXml) {

    def relativeProjectUri = pipelineData.getGitUrlProjectRelative()
    def projectPathUrlEncoded = URLEncoder.encode(relativeProjectUri, "UTF-8")

    GitlabAPIResponse projectInfo =
        sendRequestToGitLabAPI(pipelineData, pomXml,
            [
                url: "${GlobalVars.gitlabApiDomain}${projectPathUrlEncoded}"
            ]
        )

    def projectId = projectInfo.asJson.id

    printOpen("Id of the project ${projectId}", EchoLevel.ALL)

    return projectId
}
