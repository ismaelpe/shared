import com.project.alm.GlobalVars
import com.project.alm.PomXmlStructure
import com.project.alm.EchoLevel

def call(PomXmlStructure pomXml, String branchDestination, boolean checkoutBranch, boolean avoidCI, String message, boolean allowEmpty = false) {

    GitRepositoryHandler git = new GitRepositoryHandler(this, null, [gitProjectRelativePath: './']).initialize()

    if (checkoutBranch) {
        git.checkout(branchDestination, [newBranch: true])
    }

    git.add()

    String gitStatus = sh(returnStdout: true, script: "git status")
    boolean thereIsSomethingToCommit = gitStatus.indexOf("nothing to commit, working tree clean") == -1

    if (thereIsSomethingToCommit || allowEmpty) {

        String commitMessage = "${avoidCI ? GlobalVars.GIT_TAG_CI_PUSH : GlobalVars.GIT_TAG_CR_PUSH} ${message} ${pomXml.artifactVersion}"
        printOpen("Pushing to git repo with the following commit message: <${commitMessage}>", EchoLevel.INFO)
        git.commitAndPush(commitMessage, [remoteBranch: branchDestination, allowEmpty: true])

    }

}
