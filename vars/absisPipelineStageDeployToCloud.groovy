import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GarAppType
import com.caixabank.absis3.ICPStateUtility
import com.caixabank.absis3.KpiLifeCycleStage
import com.caixabank.absis3.KpiLifeCycleStatus
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.Strings
import com.caixabank.absis3.BmxUtilities

def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId, String deployICPPhasesPattern) {

    sendStageStartToGPL(pomXmlStructure, pipelineData, stageId)
    kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_STARTED, KpiLifeCycleStatus.OK)

    try {

        this.deployICPPhases = deployICPPhasesPattern.replace("<phase>", "pre")
        ICPStateUtility icpStateUtility = deployICP(pomXmlStructure, pipelineData)
        // Mostramos la url del micro desplegado para la consulta de las apps
        if (icpStateUtility != null && icpStateUtility.icpDeployStructure != null) {
            String pathToMicro = BmxUtilities.calculatePathToMicro(pomXmlStructure, pipelineData.branchStructure, icpStateUtility)
            printOpen("The component is ${pathToMicro}", EchoLevel.INFO)
            def returnUrlValue = icpStateUtility.icpDeployStructure.getUrlPrefixApiGateway() + "/" + pathToMicro + "/actuator/info"           
            printOpen("The url to access the microservice is ${returnUrlValue}", EchoLevel.INFO)
        }

        this.deployICPPhases = deployICPPhasesPattern.replace("<phase>", "post")
        kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_FINISHED, KpiLifeCycleStatus.OK)
        sendStageEndToGPL(pomXmlStructure, pipelineData, stageId, null, pipelineData.bmxStructure.environment)
        printOpen("The icpStateUtilitity ${icpStateUtility}", EchoLevel.DEBUG)

        return icpStateUtility

    } catch (Exception e) {

        printOpen("Error $e", EchoLevel.ERROR)

        String artifactAppAbort = pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name))
        this.resultDeployICP = "KO"

        if (pipelineData.isCreateRelease()) {
            sendEmail(" Resultado ejecucion app ${artifactAppAbort} - ${pipelineData.getPipelineBuildName()}  KO - ${this.deployICPPhases}", env.ABSIS3_SERVICES_EMAIL_ICP_DEPLOY_RESULT, "${artifactAppAbort} rama ${pipelineData.getPipelineBuildName()}", "KO en el paso ${this.deployICPPhases}")
        }

        kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.DEPLOY_FINISHED, KpiLifeCycleStatus.KO)
        sendStageEndToGPL(pomXmlStructure, pipelineData, stageId, null, pipelineData.bmxStructure.environment, "error")
        abortPipelineICP(pomXmlStructure, pipelineData, " Resultado ejecucion app ${artifactAppAbort} - ${pipelineData.getPipelineBuildName()}  KO", this.deployICPPhases, e)

    }

}
