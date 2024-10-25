import com.project.alm.GitUtils
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.EchoLevel

def call(PomXmlStructure pomXml, PipelineData pipeline, String message, String gitAdd = ".") {
	
    GitRepositoryHandler git = new GitRepositoryHandler(this, null, [gitProjectRelativePath: './']).initialize()

    String urlGit = new GitUtils(this, false).getSecuredGitRepoUrl('')

    printOpen("Pushing to git repo with the following commit message: <${message}>", EchoLevel.INFO)

	git.add(gitAdd).commitAndPush(message, [allowEmpty: true, remote: urlGit, remoteBranch: "HEAD:${pipeline.branchStructure.branchName}"])
	
}

def call(PipelineData pipeline, String message, String gitAdd = ".") {
    pushRepoWithMessage(null, pipeline, message, gitAdd)
}
