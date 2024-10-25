import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure

def call(PomXmlStructure pomXml, PipelineData pipeline) {
    def standardMessage = "${GlobalVars.GIT_TAG_CI_PUSH} ${GlobalVars.GIT_TAG_CI_PUSH_MESSAGE} ${pomXml.artifactVersion}"
    pushRepoWithMessage(pomXml, pipeline, standardMessage)

}
