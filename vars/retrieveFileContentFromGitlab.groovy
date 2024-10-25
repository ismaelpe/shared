import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure

def call(PipelineData pipelineData, PomXmlStructure pomXml, String fileRelativePath, String branch = "master") {

    def file = [:]

    try {

        def projectId = gitlabApiGetProjectId(pipelineData, pomXml)
        def fileMetadata = gitlabApiGetFileFromBranch(pipelineData, pomXml, projectId, fileRelativePath, branch)
        def fileBytes = fileMetadata?.content?.decodeBase64()
        def fileContent = new String(fileBytes, "UTF-8")

        file.put(fileRelativePath, fileContent)

        return file

    } catch (Exception e) {
        echo e?.getMessage()
        throw e
    }
}
