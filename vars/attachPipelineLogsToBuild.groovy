import com.project.alm.EchoLevel
import com.project.alm.FileUtils
import com.project.alm.IClientInfo
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure

def call(def pomXmlOrIClientInfo = null, PipelineData pipelineData = null) {
    def attachLogs = env.ATTACH_LOGS_TO_PIPELINE_LOG && env.ATTACH_LOGS_TO_PIPELINE_LOG.toBoolean()
    def attachZips = env.ATTACH_ZIPS_TO_PIPELINE_LOG && env.ATTACH_ZIPS_TO_PIPELINE_LOG.toBoolean()

    if (attachLogs || attachZips) {

        String applicationName = ""

        if (pomXmlOrIClientInfo instanceof PomXmlStructure) {
            applicationName = "${((PomXmlStructure) pomXmlOrIClientInfo).getArtifactName()}_"
        } else if (pomXmlOrIClientInfo instanceof IClientInfo) {
            applicationName = "${((IClientInfo) pomXmlOrIClientInfo).getArtifactId()}_"
        }

        if (attachLogs) {
            printOpen("Attaching pipeline logs...", EchoLevel.INFO)

            FileUtils fileUtils = new FileUtils(this)
            fileUtils.copyFilesFromDirectoryToDirectory("${env.WORKSPACE}@tmp/pipelineLogs", "${env.WORKSPACE}/pipelineLogs", false)
            
            archiveArtifacts artifacts: "pipelineLogs/*", fingerprint: true, allowEmptyArchive: true
            fileUtils.removeDirectory("${env.WORKSPACE}/pipelineLogs")
        }

        if (attachZips) {
            printOpen("Attaching pipeline zipped workspace...", EchoLevel.INFO)
            
            sh ("cd ${env.WORKSPACE} && zip -rq ./${applicationName}Build_${env.BUILD_ID}_workspace.zip . -x '*.git*' -x 'target/*'")
            archiveArtifacts artifacts: "${applicationName}Build_${env.BUILD_ID}_workspace.zip", fingerprint: true, allowEmptyArchive: true
        }
    }
}
