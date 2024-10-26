import groovy.transform.Field
import com.project.alm.*

@Field String cloudEnv = "${environmentParam}"

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    /*
     * Pasos a seguir:
     * */    
    pipeline {      
		agent {	node (almJenkinsAgent('light')) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
			JNKMSV = credentials('JNKMSV-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
            https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
            executionProfile = "DEFAULT"
        }
        //Atencion que en el caso que estemos en un MergeRequest... quizas solo debamos validar la issue
        stages {
            stage('push-to-config-server') {
                steps {
                    pushToConfigServerStep(cloudEnv)
                }
            }
        }
        post {
            always {
                cleanWorkspace()
            }
        }
    }
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

def pushToConfigServerStep(String environment) {
	String repoUrl;
	if (environment == 'TST') {
		repoUrl = GlobalVars.GIT_CONFIG_REPO_URL_TST
	} else if (environment == 'PRE') {
		repoUrl = GlobalVars.GIT_CONFIG_REPO_URL_PRE
	} else if (environment == 'PRO') {
		repoUrl = GlobalVars.GIT_CONFIG_REPO_URL_PRO
	} else {
		throw new Exception("Incorrect environment: "+environment)
	}

	printOpen("Compressing ADS files from ${repoUrl}", EchoLevel.INFO)

	FileUtils fileUtils = new FileUtils(this)
	GitRepositoryHandler git = new GitRepositoryHandler(this, repoUrl)

	String sanitizedConfigRepoPath = FileUtils.sanitizePath(git.gitProjectRelativePath)

	def transactionsOriginFolder = "${sanitizedConfigRepoPath}/services/ads/ads-app-transactions"
	def transactionsCompressedDestinationPath = "services/ads/ads-app-transactions-compressed"
	def transactionsCompressedDestinationFolder = "${sanitizedConfigRepoPath}/${transactionsCompressedDestinationPath}"

	try {
		git.lockRepoAndDo({
			git.purge().pullOrClone([depth: 1])
			fileUtils.createPathIfNotExists(transactionsCompressedDestinationFolder)
			String dirContent = sh(script: "ls -la ${transactionsOriginFolder}", returnStdout: true)
			printOpen("ADS transactions files:\n${dirContent}", EchoLevel.INFO)
			String zipFileName = "ads-app-transactions.zip"
			String zipFilePath = "${transactionsCompressedDestinationFolder}/${zipFileName}"
			if (fileExists(zipFilePath)) {
				printOpen("Zip file already exists at ${zipFilePath}. We are going to remove the current file.", EchoLevel.INFO)
				sh("rm -rf ${zipFilePath}")
			}
			zip zipFile: zipFilePath, archive: false, dir: transactionsOriginFolder
			printOpen("Pushing ads-app-transactions.zip to git", EchoLevel.INFO)
			git.add(transactionsCompressedDestinationPath).commitAndPush("Pushing compressed ADS transactions at ${transactionsCompressedDestinationPath}/${zipFileName}")
		})
	} catch (err) {
		echo Utilities.prettyException(err)
		throw err
	} finally {
		git.purge()
	}
}
