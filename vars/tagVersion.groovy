import com.project.alm.BranchType
import com.project.alm.EchoLevel
import com.project.alm.GitUtils
import com.project.alm.GlobalVars
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure

def call(PomXmlStructure pomXml, PipelineData pipeline, boolean createRelease, boolean commitCurrentStateBeforeTagging) {
    //withCredentials( [string(credentialsId: 'GITLAB_API_SECRET_TOKEN', variable: 'TOKEN')]){

    GitUtils gitUtils = new GitUtils(this, false)

    withCredentials([usernamePassword(credentialsId: 'GITLAB_CREDENTIALS', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
        // INICIO - IPE
        // Comentario: Si se definen las variables de entorno para Git esto no hace falta
		String removeSslVerifyLog = sh(returnStdout: true, script: "git config http.sslVerify false")
		printOpen(removeSslVerifyLog, EchoLevel.DEBUG)
        // FIN - IPE

        if (commitCurrentStateBeforeTagging) {
            def isHotfix = pipeline.branchStructure.branchType == BranchType.HOTFIX && pomXml.isRCVersion()
            def messageTag = "${GlobalVars.GIT_TAG_CR_PUSH} ${isHotfix ? GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_HOTFIX : GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_RC} ${pomXml.artifactVersion}"
            gitUtils.removeFromWorkspaceFilesNotAllowedInGitRepos()
			String gitAddAndCommitLogs = sh(returnStdout: true, script: "git add . && git commit -m '${messageTag}' ")
			printOpen(gitAddAndCommitLogs, EchoLevel.DEBUG)
            def commitId = sh(returnStdout: true, script: "git rev-parse HEAD").trim()
            pipeline.commitLog = "${commitId} ${messageTag}"
        }

        printOpen("El tag es de ${pipeline.commitLog} la url del git es de ${pipeline.gitUrl} ", EchoLevel.DEBUG)
        printOpen("The new tag is going to be v_${pomXml.artifactVersion} while the old tag was v_${pomXml.oldVersion}", EchoLevel.INFO)

        def tagExistente = sh(returnStdout: true, script: "git tag -l v_${pomXml.artifactVersion}")
        if (tagExistente != '') {
			String gitTagLog = sh(returnStdout: true, script: "git tag -d v_${pomXml.artifactVersion}")
			printOpen(gitTagLog, EchoLevel.DEBUG)
        }

        String message_Tag = GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_RC

        if (createRelease) message_Tag = GlobalVars.GIT_TAG_CI_PUSH_MESSAGE_RELEASE

		String gitTagLog2 = sh(returnStdout: true, script: "git tag -a v_${pomXml.artifactVersion} -m '${message_Tag} ${pomXml.artifactVersion}'")
		printOpen(gitTagLog2, EchoLevel.DEBUG)

        String urlGit = ''

        if (createRelease) urlGit = gitUtils.getSecuredGitRepoUrl(pipeline.gitUrl)
        else urlGit = gitUtils.getSecuredGitRepoUrl('')

        if (createRelease) tagExistente = sh(returnStdout: true, script: "git ls-remote --tags ${urlGit} | grep  v_${pomXml.artifactVersion} | grep -v RC | wc -l")
        else tagExistente = sh(returnStdout: true, script: "git ls-remote --tags ${urlGit} | grep  v_${pomXml.artifactVersion} | wc -l")

        tagExistente = tagExistente.replace('\n', '')
        printOpen("El numero de tags existentes al actual son ${tagExistente}", EchoLevel.DEBUG)

        if (tagExistente != '0') {
			printOpen("The tag v_${pomXml.artifactVersion} already exists. We are going to delete it.", EchoLevel.INFO)
			String removeExistingTagLog = sh(returnStdout: true, script: "git push ${urlGit} --delete v_${pomXml.artifactVersion}")
			printOpen(removeExistingTagLog, EchoLevel.DEBUG)
        }

		String createTagLog = sh(returnStdout: true, script: "git push ${urlGit} v_${pomXml.artifactVersion}")
		printOpen(createTagLog, EchoLevel.DEBUG)
		printOpen("The tag v_${pomXml.artifactVersion} has been created.", EchoLevel.INFO)
    }
}
