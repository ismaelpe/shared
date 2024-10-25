import com.project.alm.*
import hudson.model.ParameterValue
import hudson.model.StringParameterValue

/**
 * Permite invocar al siguiente job definido para el resultado generado, para ello recupera el siguiente job y los parámetros
 * Se activa mediante el profile definido como invokeNextActionAuto que puede venir a través de los parametros del JenkinsFile o de un commitLog
 */
def call(PipelineData pipelineData, PomXmlStructure pomXmlStructure) {

    def acciones = pipelineData.pipelineStructure.resultPipelineData.getAcciones(true);
    NextJobOptions options = ObtainNextJobOptionsUtils.obtainNextJobInformation(pipelineData.getExecutionMode().actionFlag(), acciones)

    if (NextJobOptions.JobType.INVOKE_JOB_AUTO.equals(options.getJobType())) {
        printOpen("Using executionMode:" + pipelineData.getExecutionMode().toString(), EchoLevel.DEBUG)

        def jobParameters = []
        ParameterValue jobParameter = null;
        printOpen("Invoking due to executing job action to " + options.nextJobName, EchoLevel.DEBUG)

        for (BuildParameter param in options.parameters) {
            printOpen("Adding parameter" + param.toString(), EchoLevel.DEBUG)
            jobParameter = new StringParameterValue(param.name, param.value)
            jobParameters.add(jobParameter)
        }
        String jobName = pipelineData.getExecutionMode().parseJobName(options.nextJobName);
        build job: jobName, wait: false, parameters: jobParameters

    } else if (NextJobOptions.JobType.MERGE_AUTO.equals(options.getJobType())) {

        String mrTitle = calculateMRTitle(pipelineData, pomXmlStructure)
        mergeRequestToMaster(pipelineData, pomXmlStructure, 'master', true, true, false, "", mrTitle)

    } else {

        printOpen("options.nextJobName is ${options.nextJobName} so no invocation will take place", EchoLevel.DEBUG)

    }

}

private String calculateMRTitle(PipelineData pipelineData, PomXmlStructure pomXmlStructure) {

    switch(pipelineData.executionProfileName) {
        case PipelineExecutionMode.COMPLETE_TEST_AUTO:
            return "Upgrading parent version to ${pomXmlStructure.archVersion} and deploy with executionProfile[${PipelineExecutionMode.COMPLETE_TEST_AUTO}]"
        case PipelineExecutionMode.COMPLETE_TEST_AUTO_HOTFIX:
            return "Upgrading parent version to ${pomXmlStructure.archVersion} and deploy with executionProfile[${PipelineExecutionMode.COMPLETE_TEST_AUTO_HOTFIX}]"
        case PipelineExecutionMode.SCHEDULED_CORE_UPDATE:
            return "Upgrading parent version to ${pomXmlStructure.archVersion} and deploy with executionProfile[${PipelineExecutionMode.SCHEDULED_CORE_UPDATE}]"
        case PipelineExecutionMode.UPGRADE_CORE_AND_CREATE_RC:
            return "Upgrading parent version to ${pomXmlStructure.archVersion} and deploy with executionProfile[${PipelineExecutionMode.UPGRADE_CORE_AND_CREATE_RC}]"
        default:
            return "Merge request. End of version ${pomXmlStructure.artifactVersion}. Artifact ${pomXmlStructure.artifactName}"
    }

}
