import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GarAppType
import com.caixabank.absis3.ICPStateUtility
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.Strings
import hudson.Functions

def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId, String deployICPPhasesPattern, ICPStateUtility icpStateUtility) {

    try {

        sendStageStartToGPL(pomXmlStructure, pipelineData, stageId)
        
        this.deployICPPhases = deployICPPhasesPattern.replace("<phase>", "pre")
        def microUrl = consolidateNewDeployICP(pomXmlStructure, pipelineData, icpStateUtility)
        this.deployICPPhases = deployICPPhasesPattern.replace("<phase>", "post")
        
		def messageUrl = ""
		
		if (microUrl != null && !"".equals(microUrl)){
			messageUrl = "The url to access the microservice is <a href='${microUrl}'>${microUrl}</a>"
			printOpen(messageUrl, EchoLevel.INFO)
		}
		
        sendStageEndToGPL(pomXmlStructure, pipelineData, stageId, env.sendLogsToGpl ? null : messageUrl, pipelineData.bmxStructure.environment)

    } catch (Exception e) {

        printOpen("Error consolidating new deployment:\n${Functions.printThrowable(e)}", EchoLevel.ERROR)

        String artifactAppAbort = pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name))
        this.resultDeployICP = "KO"

        if (pipelineData.isCreateRelease()) {
            sendEmail(" Resultado ejecucion app ${artifactAppAbort} - ${pipelineData.getPipelineBuildName()}  KO - ${this.deployICPPhases}", env.ABSIS3_SERVICES_EMAIL_ICP_DEPLOY_RESULT, "${artifactAppAbort} rama ${pipelineData.getPipelineBuildName()}", "KO en el paso ${this.deployICPPhases}")
        }

        sendStageEndToGPL(pomXmlStructure, pipelineData, stageId, null, pipelineData.bmxStructure.environment, "error")
        abortPipelineICP(pomXmlStructure, pipelineData, " Resultado ejecucion app ${artifactAppAbort} - ${pipelineData.getPipelineBuildName()}  KO", this.deployICPPhases, e)

    }

}
