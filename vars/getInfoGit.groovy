import com.project.alm.GlobalVars
import com.project.alm.EchoLevel
import com.project.alm.BranchType
import com.project.alm.PipelineData
import com.project.alm.PipelineStructureType

PipelineData call(PipelineStructureType pipelineStructureType,  BranchType branchType = null) {
    printOpen("Init get info Git (" + pipelineStructureType + ")", EchoLevel.ALL)
    pipelineStructureType = pipelineStructureType == null ? PipelineStructureType.CI : pipelineStructureType
    PipelineData pipelineData = new PipelineData(pipelineStructureType, "${env.BUILD_TAG}")

    if (branchType) {
        if (branchType == BranchType.FEATURE) {
            printOpen("DEPLOY_TAG setted!")
            return infoGitToPipelineData(pipelineData, GlobalVars.DEPLOY_TAG) 
        } else {
            return infoGitToPipelineData(pipelineData, GlobalVars.IGNORE_TAG)    
        }
    } else {
        return infoGitToPipelineData(pipelineData)
    }
}