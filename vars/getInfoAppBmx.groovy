import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.GlobalVars
import com.project.alm.DeployStructure

def call(PomXmlStructure pomXml, PipelineData pipeline, String endPoint) {

    DeployStructure deployStructure = pipeline.bmxStructure.getDeployStructure(GlobalVars.BMX_CD1)

    try {
        return getInfoAppBmxTask(pomXml, pipeline.branchStructure, deployStructure, endPoint)
    } catch (e) {
        echo e.getMessage()
        throw e
    } finally {
    }
}
