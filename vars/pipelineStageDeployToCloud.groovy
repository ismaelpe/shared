import com.project.alm.EchoLevel
import com.project.alm.GarAppType
import com.project.alm.CloudStateUtility
import com.project.alm.KpiLifeCycleStage
import com.project.alm.KpiLifeCycleStatus
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.Strings
import com.project.alm.BmxUtilities

def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId, String deployCloudPhasesPattern) {

    sendStageStartToAppPortal(pomXmlStructure, pipelineData, stageId)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_STARTED, KpiLifeCycleStatus.OK)

    try {

        this.deployCloudPhases = deployCloudPhasesPattern.replace("<phase>", "pre")
        CloudStateUtility cloudStateUtility = deployCloud(pomXmlStructure, pipelineData)
        // Mostramos la url del micro desplegado para la consulta de las apps
        if (cloudStateUtility != null && cloudStateUtility.cloudDeployStructure != null) {
            String pathToMicro = BmxUtilities.calculatePathToMicro(pomXmlStructure, pipelineData.branchStructure, cloudStateUtility)
            printOpen("The component is ${pathToMicro}", EchoLevel.INFO)
            def returnUrlValue = cloudStateUtility.cloudDeployStructure.getUrlPrefixApiGateway() + "/" + pathToMicro + "/actuator/info"           
            printOpen("The url to access the microservice is ${returnUrlValue}", EchoLevel.INFO)
        }

        this.deployCloudPhases = deployCloudPhasesPattern.replace("<phase>", "post")
        kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_FINISHED, KpiLifeCycleStatus.OK)
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, stageId, null, pipelineData.bmxStructure.environment)
        printOpen("The cloudStateUtilitity ${cloudStateUtility}", EchoLevel.DEBUG)

        return cloudStateUtility

    } catch (Exception e) {

        printOpen("Error $e", EchoLevel.ERROR)

        String artifactAppAbort = pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name))
        this.resultDeployCloud = "KO"

        if (pipelineData.isCreateRelease()) {
            sendEmail(" Resultado ejecucion app ${artifactAppAbort} - ${pipelineData.getPipelineBuildName()}  KO - ${this.deployCloudPhases}", env.ALM_SERVICES_EMAIL_Cloud_DEPLOY_RESULT, "${artifactAppAbort} rama ${pipelineData.getPipelineBuildName()}", "KO en el paso ${this.deployCloudPhases}")
        }

        kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_FINISHED, KpiLifeCycleStatus.KO)
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, stageId, null, pipelineData.bmxStructure.environment, "error")
        abortPipelineCloud(pomXmlStructure, pipelineData, " Resultado ejecucion app ${artifactAppAbort} - ${pipelineData.getPipelineBuildName()}  KO", this.deployCloudPhases, e)

    }

}
