import com.project.alm.BranchType
import com.project.alm.GlobalVars
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.Strings


def call(PipelineData pipelineData, PomXmlStructure pomXmlStructure) {

    try {

        sendStageStartToAppPortal(pomXmlStructure, pipelineData, "110")
        pipelineData.deployFlag = false
        //Hacemos setting del nombre de la pipeline
        pipelineData.pipelineStructure.nombre="CI_BBDD_Pipeline"
        String result=""
        if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {
            result=generateSqlScript(pomXmlStructure, pipelineData)
        }else {
            result=generateSqlChangeSetPackage(pomXmlStructure,pipelineData)
            pipelineData.prepareResultData(pomXmlStructure.artifactVersion, pomXmlStructure.artifactMicro, pomXmlStructure.artifactName)
			pipelineData.pipelineStructure.resultPipelineData.isBBDD=true
        }
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "110",result,null,"ended")
        pipelineData.commitLog = GlobalVars.GIT_TAG_CI_PUSH

    } catch(Exception e) {
        sendStageEndToAppPortal(pomXmlStructure, pipelineData, "110", Strings.toHtml(e.getMessage()), null, "error")
        throw e
    }

}

boolean shouldExecute(PipelineData pipelineData) {

    //Si es DataService controlar el IdDe la rama Si el nombre de la feature es BBDD?
    return ((pipelineData.branchStructure.branchType == BranchType.FEATURE && pipelineData.branchStructure.featureNumber.startsWith('BBDD'))
        //Es una merge de feature
        || (pipelineData.branchStructure.branchType == BranchType.MASTER && pipelineData.eventToPush!=null && pipelineData.eventToPush.startsWith('BBDD')) )

}
