import com.project.alm.GlobalVars
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure

def call(PomXmlStructure pomXml, PipelineData pipeline) {
    def standardMessage = "${GlobalVars.GIT_TAG_CI_PUSH} ${GlobalVars.GIT_TAG_CI_PUSH_MESSAGE} ${pomXml.artifactVersion}"
    pushRepoWithMessage(pomXml, pipeline, standardMessage)

}
