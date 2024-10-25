import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.FileUtils
import com.caixabank.absis3.GitUtils
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.Utilities

def call(PomXmlStructure pomXml, PipelineData pipeline) {

    deleteAppCertificates(pomXml, pipeline)
    deleteAppConfiguration(pomXml, pipeline)

}

private void deleteAppCertificates(PomXmlStructure pomXml, PipelineData pipeline) {

    //Delete certificates
    def certsRepoUrlAndBranch = GitUtils.getGitCertsRepoUrlAndBranch(pipeline.bmxStructure.environment)
	boolean weHaveDeletedSomething=false
    GitRepositoryHandler git = new GitRepositoryHandler(this, certsRepoUrlAndBranch.repoUrl, [checkoutBranch: certsRepoUrlAndBranch.branch])

    String sanitizedCertsFolder = FileUtils.sanitizePath(git.gitProjectRelativePath)

    try {

        git.lockRepoAndDo({

            git.purge().pullOrClone()

            String appName = pomXml.artifactName + "-" + pomXml.artifactMajorVersion

            def pathsToDelete = ['apps/', 'arch/']

            for (String folder : pathsToDelete) {

                String configFolder = "${sanitizedCertsFolder}/certs/" + folder
                printOpen("Deleting the app ${appName} certs files in '" + folder + "' folder", EchoLevel.ALL)
                int existAppConfig = sh(returnStatus: true, script: "ls -ld ${configFolder}${appName}")
                if (existAppConfig == 0) {

                    String folderToDelete = configFolder + appName
                    sh "rm -rf ${folderToDelete}"
					weHaveDeletedSomething=true
                } else {

                    printOpen("The ${appName} application has not certs files in '" + folder + "' folder in this environment", EchoLevel.ALL)

                }

            }
			printOpen("Before commit and push Cert", EchoLevel.ALL)
			if (weHaveDeletedSomething) {
				printOpen("We have deleted something, we need to add", EchoLevel.ALL)
				git.add()
		    }
            git.commitAndPush("Deleted Config files for ${appName}")
			printOpen("After commit and push Cert", EchoLevel.ALL)
        })

    } catch (err) {

        printOpen(Utilities.prettyException(err), EchoLevel.ERROR)
        throw err

    } finally {

        git.purge()

    }

}

private void deleteAppConfiguration(PomXmlStructure pomXml, PipelineData pipeline) {

    def repoUrlAndBranch = GitUtils.getConfigRepoUrlAndBranch(pipeline.bmxStructure.environment)

    GitRepositoryHandler git = new GitRepositoryHandler(this, repoUrlAndBranch.url, [checkoutBranch: repoUrlAndBranch.branch])

    String sanitizedConfigRepoPath = FileUtils.sanitizePath(git.gitProjectRelativePath)
    boolean weHaveDeletedSomething=false
    try {

        git.lockRepoAndDo({

            git.purge().pullOrClone()

            String appName = pomXml.artifactName + "-" + pomXml.artifactMajorVersion

            def pathsToDelete = ['apps/', 'arch/', 'sys/']

            for (String folder : pathsToDelete) {
                String configFolder = "${sanitizedConfigRepoPath}/services/" + folder
                printOpen("Deleting the app ${appName} config files in '" + folder + "' folder", EchoLevel.ALL)
                int existAppConfig = sh(returnStatus: true, script: "ls -ld ${configFolder}${appName}")
                if (existAppConfig == 0) {
                    String folderToDelete = configFolder + appName
                    sh "rm -rf ${folderToDelete}"
					weHaveDeletedSomething=true
                } else {
                    printOpen("The ${appName} application has not config files in '" + folder + "' folder in this environment", EchoLevel.ALL)
                }
            }
			printOpen("Before commit and push App", EchoLevel.ALL)
			if (weHaveDeletedSomething) {
				 printOpen("We have deleted something, we need to add", EchoLevel.ALL)
				 git.add()
		    }
            git.commitAndPush("Deleted Config files for ${appName}")
			printOpen("After commit and push App", EchoLevel.ALL)
        })

    } catch (err) {

        echo Utilities.prettyException(err)
        throw err

    } finally {

        git.purge()

    }

}
