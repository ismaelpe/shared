import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.BranchType
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PipelineStructureType

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