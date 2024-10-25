package com.project.alm

import org.jenkinsci.plugins.workflow.job.WorkflowRun

class PipelineUtils implements Serializable {

    private final def scriptContext

    PipelineUtils(scriptContext) {
        this.scriptContext = scriptContext
    }

    List<PipelineRuns> getPipelineRuns() {

        Map<String, List> runsMap = new HashMap<>()
        List<PipelineRuns> runs = new ArrayList<>()

        Jenkins.instance.getItemByFullName(scriptContext.env.JOB_NAME)._getRuns().each { workflowRun ->
            workflowRun = workflowRun.value
            def displayName = workflowRun.getDisplayName().trim()
            def featureName = displayName?.lastIndexOf("_") == -1 ? displayName : displayName?.substring(displayName?.lastIndexOf("_") + 1)
            def isBuilding = workflowRun.isBuilding()
            scriptContext.printOpen("PipelineUtils.getPipelineRuns(): Storing ${displayName} with isBuilding = ${isBuilding} and featureName = ${featureName}", EchoLevel.ERROR)
            if (!runsMap.containsKey(featureName)) {
                runsMap.put(featureName, new ArrayList<>())
            }
            List workflowRunsList = runsMap.get(featureName)
            workflowRunsList.add(workflowRun)
        }

        for (def pipelineRun in runsMap) {
            runs.add(new PipelineRuns(pipelineRun.key, pipelineRun.value))
        }

        return runs
    }

    List<PipelineRuns> getBuildingPipelineRuns(List<PipelineRuns> pipelineRunsList) {

        List<PipelineRuns> processedPipelineRuns = new ArrayList<>()

        pipelineRunsList.each { PipelineRuns pipelineRuns ->
            List workflowRuns = new ArrayList<>()

            pipelineRuns.workflowRuns.each { workflowRun ->
                if (workflowRun.isBuilding()) {
                    workflowRuns.add(workflowRun)
                    scriptContext.printOpen("PipelineUtils.getBuildingPipelineRuns(): Storing building pipeline ${workflowRun.getDisplayName().trim()}", EchoLevel.INFO)
                }
            }

            processedPipelineRuns.add(new PipelineRuns(pipelineRuns.featureName, workflowRuns))
        }

        return processedPipelineRuns
    }

    PipelineRuns getCurrentPipelineRuns(List<PipelineRuns> pipelineRunsList) {
        for (PipelineRuns pipelineRuns : pipelineRunsList) {
            String buildDisplayName = (String) scriptContext.env.BUILD_DISPLAY_NAME.trim()
            if (buildDisplayName.contains(pipelineRuns?.featureName)) {
                scriptContext.printOpen("PipelineUtils.getCurrentPipelineRuns(): Found current pipeline with featureName = ${pipelineRuns.featureName}", EchoLevel.INFO)
                return pipelineRuns
            }
        }
        scriptContext.printOpen("PipelineUtils.getCurrentPipelineRuns(): Current pipeline not found! This is probably some kind of error...", EchoLevel.INFO)
        return new PipelineRuns("", new ArrayList<>())
    }

    void terminateConcurrentPipelines() {
        doTermOrDoKillConcurrentPipelines(false)
    }

    void killConcurrentPipelines() {
        doTermOrDoKillConcurrentPipelines(true)
    }

    static boolean commitLogHasDeployFlag(String commitLog = "") {

        return commitLog.indexOf(GlobalVars.DEPLOY_TAG, 0) != -1

    }

    static boolean commitLogHasDoNotCIFlag(String commitLog = "") {

        def flags = getCommitLogFlags(commitLog)
        return flags.contains(GlobalVars.DO_NOT_CI_TAG)

    }

    static boolean commitLogHasLogLevelAllFlag(String commitLog = "") {

        def flags = getCommitLogFlags(commitLog)
        return flags.contains(GlobalVars.LOG_LEVEL_ALL_TAG)

    }

    static boolean commitLogHasLogLevelDebugFlag(String commitLog = "") {

        def flags = getCommitLogFlags(commitLog)
        return flags.contains(GlobalVars.LOG_LEVEL_DEBUG_TAG)

    }

    static boolean commitLogHasLogLevelInfoFlag(String commitLog = "") {

        def flags = getCommitLogFlags(commitLog)
        return flags.contains(GlobalVars.LOG_LEVEL_INFO_TAG)

    }

    static String[] getCommitLogFlags(commitLog) {

        String[] commit = commitLog.toLowerCase().split(" ")
        if (commit.length > 1) {

            return commit[1].split(",")

        } else {

            return []

        }

    }

    private void doTermOrDoKillConcurrentPipelines(boolean doKill) {

        List<PipelineRuns> buildingRuns = getBuildingPipelineRuns(this.getPipelineRuns())
        PipelineRuns currentPipelineBuildingRuns = getCurrentPipelineRuns(buildingRuns)

        scriptContext.printOpen("PipelineUtils.doTermOrDoKillConcurrentPipelines(): Checking ${currentPipelineBuildingRuns.featureName} with ${currentPipelineBuildingRuns.workflowRuns.size()} jobs...", EchoLevel.INFO)

        currentPipelineBuildingRuns.workflowRuns.each { org.jenkinsci.plugins.workflow.job.WorkflowRun workflowRun ->
            Integer buildId = new Integer(scriptContext.env.BUILD_ID.trim())
            def workflowRunDisplayName = "${workflowRun?.getDisplayName().trim()}"
            Integer workflowRunBuildId = new Integer(workflowRunDisplayName.replace("#", "").replace("Build_", "").replace("_${currentPipelineBuildingRuns.featureName}", ""))

            scriptContext.printOpen("PipelineUtils.doTermOrDoKillConcurrentPipelines(): Checking ${currentPipelineBuildingRuns.featureName} against concurrent ${workflowRunDisplayName} job with ID ${workflowRunBuildId}...", EchoLevel.INFO)

            //We are not killing ourselves or builds that began after us
            //Neither we are killing builds in the form of #buildId as it's associated feature has not been yet discovered...
            if (workflowRunBuildId >= buildId || workflowRunDisplayName?.contains("#")) {
                return
            }
            if (doKill) {
                scriptContext.printOpen("PipelineUtils.doTermOrDoKillConcurrentPipelines(): Killing ${workflowRunDisplayName} job...", EchoLevel.INFO)
                workflowRun.doKill()
            } else {
                scriptContext.printOpen("PipelineUtils.doTermOrDoKillConcurrentPipelines(): Terminating ${workflowRunDisplayName} job...", EchoLevel.INFO)
                workflowRun.doTerm()
            }
        }
    }

    static class PipelineRuns implements Serializable {
        String featureName
        List<org.jenkinsci.plugins.workflow.job.WorkflowRun> workflowRuns

        PipelineRuns(String featureName, List workflowRuns) {
            this.featureName = featureName
            this.workflowRuns = workflowRuns
        }
    }
}
