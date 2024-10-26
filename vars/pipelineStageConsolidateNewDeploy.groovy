import com.project.alm.EchoLevel
import com.project.alm.GarAppType
import com.project.alm.CloudStateUtility
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.Strings
import hudson.Functions

def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId, String deployCloudPhasesPattern, CloudStateUtility cloudStateUtility) {

    try {

        sendStageStartToAppPortal(pomXmlStructure, pipelineData, stageId)
        
        this.deployCloudPhases = deployCloudPhasesPattern.replace("<phase>", "pre")
        def microUrl = consolidateNewDeployCloud(pomXmlStructure, pipelineData, cloudStateUtility)
        this.deployCloudPhases = deployCloudPhasesPattern.replace("<phase>", "post")
        
		def messageUrl = ""
		
		if (microUrl != null && !"".equals(microUrl)){
			messageUrl = "The url to access the microservice is <a href='${microUrl}'>${microUrl}</a>"
			printOpen(messageUrl, EchoLevel.INFO)
		}
		
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, stageId, env.sendLogsToAppPortal ? null : messageUrl, pipelineData.bmxStructure.environment)

    } catch (Exception e) {

        printOpen("Error consolidating new deployment:\n${Functions.printThrowable(e)}", EchoLevel.ERROR)

        String artifactAppAbort = pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name))
        this.resultDeployCloud = "KO"

        if (pipelineData.isCreateRelease()) {
            sendEmail(" Resultado ejecucion app ${artifactAppAbort} - ${pipelineData.getPipelineBuildName()}  KO - ${this.deployCloudPhases}", env.ALM_SERVICES_EMAIL_Cloud_DEPLOY_RESULT, "${artifactAppAbort} rama ${pipelineData.getPipelineBuildName()}", "KO en el paso ${this.deployCloudPhases}")
        }

        sendStageEndToAppPortal(pomXmlStructure, pipelineData, stageId, null, pipelineData.bmxStructure.environment, "error")
        abortPipelineCloud(pomXmlStructure, pipelineData, " Resultado ejecucion app ${artifactAppAbort} - ${pipelineData.getPipelineBuildName()}  KO", this.deployCloudPhases, e)

    }

}
