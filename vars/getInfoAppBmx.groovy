import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.DeployStructure

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
