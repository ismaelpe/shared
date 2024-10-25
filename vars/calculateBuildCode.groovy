import com.caixabank.absis3.BranchType
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineData

def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData) {

    if (pomXmlStructure.isRCVersion()) {
        //En caso de RC devolvemos RC0, RC1, RC2... En caso de un aumento de RC durante la pipeline, se actualizar� dicho valor.
        pipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()
    } else if (pomXmlStructure.isRelease()) {
        //En caso de Release final devolvemos la versi�n X.Y.Z
        pipelineData.buildCode = pomXmlStructure.artifactVersion
    } else if (pomXmlStructure.isSNAPSHOT()) {
        if (pipelineData.branchStructure != null && pipelineData.branchStructure.branchType == BranchType.FEATURE) {
            //En caso de Feature devolvemos el ID de la feature
            pipelineData.buildCode = pipelineData.branchStructure.featureNumber
        } else if (pipelineData.branchStructure != null && (pipelineData.branchStructure.branchType == BranchType.RELEASE || pipelineData.branchStructure.branchType == BranchType.HOTFIX)) {
            //En caso de SNAPSHOT en ramas RELEASE o HOTFIX, c�mo no vamos ha hacer deploy en nexuws, devolvemos la versi�n sin el qualifier SNAPSHOT
            pipelineData.buildCode = pomXmlStructure.getArtifactVersionWithoutQualifier()
        } else {
            //En caso de SNAPSHOT en MASTER, se actualizar� con el buildCode una vez se haga deploy en nexus
            pipelineData.buildCode = "SNAPSHOT"
        }
    }
	printOpen("pipelineData.buildCode calculado es ${pipelineData.buildCode}", EchoLevel.ALL)

}
