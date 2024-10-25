import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineData
import java.util.Date
import com.caixabank.absis3.ClientCicsHISInfo

def call(ClientCicsHISInfo clientCicsHISInfo, PipelineData pipelineData, String pipelineOrigId) {

    if (notificationToGplApplies()) {
        def url = idecuaRoutingUtils.pipelineUrl();

        String commitId = ''

        if (env.gitlabMergeRequestLastCommit != null) commitId = env.gitlabMergeRequestLastCommit

        if (pipelineData.commitId != '' && pipelineData.commitId != null) {
            commitId = pipelineData.commitId
        }

        def body = [
                id                : "${pipelineData.pipelineStructure.pipelineId}",
                plataforma        : pipelineData.pipelineStructure.plataforma,
                tipo              : "${pipelineData.garArtifactType.getGarName()}",
                aplicacion        : "${clientCicsHISInfo.getApp(pipelineData.garArtifactType)}",
                componente        : "${clientCicsHISInfo.artifactName}",
                versionMajor      : "${clientCicsHISInfo.getArtifactMajorVersion()}",
                versionMinor      : "${clientCicsHISInfo.getArtifactMinorVersion()}",
                versionFix        : "${clientCicsHISInfo.getArtifactFixVersion()}",
                //build: "${env.BUILD_ID}",
                commit            : "${commitId}",
                branch            : "${pipelineData.branchStructure.branchName}",
                nombre            : pipelineData.pipelineStructure.nombre,
                estado            : "${GlobalVars.GPL_STATE_RUNNING}",
                fechaCreacion     : new Date(),
                anteriorPipelineId: "${pipelineOrigId}",
                stages            : pipelineData.pipelineStructure.getStages(),
                path              : "${env.JOB_NAME}",
                runId             : "${env.BUILD_NUMBER}",
                //serverUrl         : "${env.JENKINS_URL}",
                //jenkinsUserId     : "${GPL_USR}",
                //jenkinsUserToken  : "${GPL_PSW}"
				serverUrl         : "${env.JNKMSV_DEVPORTAL_URL}",
				jenkinsUserId     : "${JNKMSV_USR}",
				jenkinsUserToken  : "${JNKMSV_PSW}"
        ]

        def response = sendRequestToGpl('POST', url, "", body, pipelineData, clientCicsHISInfo)

        return response
    }
}
