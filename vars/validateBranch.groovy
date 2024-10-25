import com.caixabank.absis3.BranchStructure
import com.caixabank.absis3.BranchType
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars

def call(String artifactVersion, BranchStructure branch) {
    if (branch.branchType == BranchType.UNKNOWN) {
        //Error
        throw new RuntimeException("No se ha podido reconocer la rama: " + branch.branchName);
    }
    //Valida que la rama y la version actual del pom son correctas en las ramas releases.

    if (BranchType.RELEASE == branch.branchType && artifactVersion != null) {
        String branchNameWithArtifactVersion = "${GlobalVars.RELEASE_BRANCH}/v${artifactVersion}"
        String realBranchName = branch.branchName
        printOpen("Comparing branchName ${realBranchName} with calculated name ${branchNameWithArtifactVersion}", EchoLevel.INFO)
        if (!realBranchName.startsWith("release/BBDD") && !branchNameWithArtifactVersion.equals(realBranchName)) {
            throw new RuntimeException("Release branch name " + branch.branchName + " does not correspond to the artifact version " + artifactVersion);
        }

    }

    if (BranchType.HOTFIX == branch.branchType && artifactVersion != null) {
        String branchNameWithArtifactVersion = "${GlobalVars.HOTFIX_BRANCH}/v${artifactVersion}"
        String realBranchName = branch.branchName
        printOpen("Comparing branchName ${realBranchName} with calculated name ${branchNameWithArtifactVersion}", EchoLevel.INFO)
        if (!branchNameWithArtifactVersion.equals(realBranchName)) {
            throw new RuntimeException("Release branch name " + branch.branchName + " does not correspond to the artifact version " + artifactVersion);
        }

    }

}
