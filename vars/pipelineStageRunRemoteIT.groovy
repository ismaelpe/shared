import com.project.alm.EchoLevel
import com.project.alm.GarAppType
import com.project.alm.GlobalVars
import com.project.alm.CloudStateUtility
import com.project.alm.KpiLifeCycleStage
import com.project.alm.KpiLifeCycleStatus
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.Strings
import com.project.alm.BmxUtilities
import com.project.alm.MavenGoalExecutionException

def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId, String deployCloudPhasesPattern, CloudStateUtility cloudStateUtility) {

    if (GlobalVars.ALM_SERVICES_SIMPLIFIED_ALM_WHITELIST.contains(pomXmlStructure.artifactName)) {
        return
    }

    sendStageStartToGPL(pomXmlStructure, pipelineData, stageId)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.IT_TEST_STARTED, KpiLifeCycleStatus.OK, pipelineData.bmxStructure.environment)

    try {

        this.deployCloudPhases = deployCloudPhasesPattern.replace("<phase>", "pre")
        runRemoteITCloud(pomXmlStructure, pipelineData, cloudStateUtility)
        this.deployCloudPhases = deployCloudPhasesPattern.replace("<phase>", "post")
        kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.IT_TEST_FINISHED, KpiLifeCycleStatus.OK, pipelineData.bmxStructure.environment)
	
		def messageUrl = ""
		
		if (cloudStateUtility!=null && cloudStateUtility.cloudDeployStructure!=null) {
			String pathToMicro = BmxUtilities.calculatePathToMicro(pomXmlStructure,pipelineData.branchStructure,cloudStateUtility)
			returnUrlValue = cloudStateUtility.cloudDeployStructure.getUrlPrefixApiGateway()+"/"+pathToMicro + "/actuator/info"
			messageUrl = "The url to access the microservice is <a href='${returnUrlValue}'>${returnUrlValue}</a>"
			printOpen(messageUrl, EchoLevel.INFO)
		}
		
		sendStageEndToGPL(pomXmlStructure, pipelineData, stageId, env.sendLogsToGpl ? null : messageUrl)

    } catch (Exception e) {

        if(!e instanceof MavenGoalExecutionException) printOpen("Error executing Integration Tests: ${e}", EchoLevel.ERROR)
        
        String artifactAppAbort = pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name))
        this.resultDeployCloud = "KO"

        if (pipelineData.isCreateRelease()) {
            sendEmail(" Resultado ejecucion app ${artifactAppAbort} - ${pipelineData.getPipelineBuildName()}  KO - ${this.deployCloudPhases}", env.ALM_SERVICES_EMAIL_Cloud_DEPLOY_RESULT, "${artifactAppAbort} rama ${pipelineData.getPipelineBuildName()}", "KO en el paso ${this.deployCloudPhases}")
        }

        kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.IT_TEST_FINISHED, KpiLifeCycleStatus.KO, pipelineData.bmxStructure.environment)
        sendStageEndToGPL(pomXmlStructure, pipelineData, stageId, null, pipelineData.bmxStructure.environment, "error")
        abortPipelineCloud(pomXmlStructure, pipelineData, " Resultado ejecucion app ${artifactAppAbort} - ${pipelineData.getPipelineBuildName()}  KO", this.deployCloudPhases, e)

    }

}

def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId, String deployCloudPhasesPattern, CloudStateUtility cloudStateUtility, def whiteListAppsStr) {

    printOpen("PRO Integration tests' white list: ${whiteListAppsStr}", EchoLevel.ALL)
    printOpen("ArtifactName: ${pomXmlStructure.artifactName}", EchoLevel.ALL)
    List<String> whiteListApps = Arrays.asList(whiteListAppsStr.split(";"))
    if (whiteListApps.contains(pomXmlStructure.artifactName)) {

        pipelineData.onlyProductionTests = true
        call(pomXmlStructure, pipelineData, stageId, deployCloudPhasesPattern, cloudStateUtility)

    }

    if (GlobalVars.ALM_SERVICES_SIMPLIFIED_ALM_WHITELIST.contains(pomXmlStructure.artifactName)) {
        pipelineData.onlyProductionTests = false
    }

}
