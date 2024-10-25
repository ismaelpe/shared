import com.project.alm.BranchType
import com.project.alm.GlobalVars
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.EchoLevel

def call(PipelineData pipelineData, PomXmlStructure pomXmlStructure) {

    def versionOnBranch = null

    if (pipelineData.branchStructure.branchType == BranchType.RELEASE || pipelineData.branchStructure.branchType == BranchType.HOTFIX) {

        versionOnBranch = pipelineData?.branchStructure?.branchName?.replace("hotfix/v", "").replace("release/v", "")
		if (pipelineData.branchStructure.branchName!=null && pipelineData.branchStructure.branchName.startsWith("release/BBDD")) {
			versionOnBranch = false
		}

    }/* else if (pipelineData.branchStructure.branchType==BranchType.FEATURE) {
// Emitir un warning aqui
        def pomInMasterBranch = retrieveFileContentFromGitlab(pipelineData, pomXmlStructure, "pom.xml", "master")
        def versionOnBranchMap = retrieveAllArtifactVersionsFromPoms(pomInMasterBranch, true)
        versionOnBranch = versionOnBranchMap.entrySet().iterator().next().value

    }*/

    if (versionOnBranch) {
        printOpen("Check correct version at pom.xml", EchoLevel.INFO)
        checkAllPomVersions(
                retrieveRecursivelyFilesContentFromProject("pom.xml"),
                versionOnBranch
        )
    }

}
