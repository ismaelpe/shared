import com.project.alm.EchoLevel
import com.project.alm.FileUtils
import com.project.alm.GitUtils
import com.project.alm.GlobalVars
import com.project.alm.Utilities

def call(int newCampaignCannaryPercentage, String environment) {

    def repoUrlAndBranch = GitUtils.getConfigRepoUrlAndBranch(environment)
	printOpen("The repoUrl is ${repoUrlAndBranch}", EchoLevel.ALL)

    GitRepositoryHandler git = new GitRepositoryHandler(this, repoUrlAndBranch.url, [checkoutBranch: repoUrlAndBranch.branch])

    String sanitizedConfigRepoPath = FileUtils.sanitizePath(git.gitProjectRelativePath)

    try {
		printOpen("Inicio del increase ${sanitizedConfigRepoPath} relative path ${git.gitProjectRelativePath}", EchoLevel.ALL)
        git.lockRepoAndDo({

            git.purge().pullOrClone([depth: 1])

            printOpen("Modifying the campaign cannary files", EchoLevel.ALL)
            increaseCampaignCannaryPercentage(sanitizedConfigRepoPath, newCampaignCannaryPercentage)

            printOpen("Pushing the campaign cannary files", EchoLevel.ALL)
            git.add('services/arch/api-gateway-1').commitAndPush("Config files for campaign cannary")
            iopCampaignCatalogUtils.increaseCannaryCampaign(newCampaignCannaryPercentage)

        })

    } catch (err) {

        echo Utilities.prettyException(err)
        throw err

    } finally {

        git.purge()

    }

}

private increaseCampaignCannaryPercentage(String baseFolder, int newPercentage) {

    FileUtils fileUtils = new FileUtils(this)

    String destinationFolder = baseFolder+"/services/arch/api-gateway-1"
    fileUtils.createPathIfNotExists(destinationFolder)
	String fileName = destinationFolder+"/api-gateway-1.yml"

    int actualIntPercentage = newPercentage
    //Tenemos que buscar el fichero
    printOpen("Increase the campaign cannary percentage to ${newPercentage}", EchoLevel.ALL)

    String existeArchAppYaml = sh(returnStdout: true, script: "ls ${fileName} | wc -l")

    existeArchAppYaml = (existeArchAppYaml.trim()).replaceAll('/n', '')

    printOpen("Existe el value ${existeArchAppYaml}", EchoLevel.ALL)

    if (existeArchAppYaml == '0') {

        //No existe el fichero lo voy a generar
        printOpen("Setting ${actualIntPercentage} to file ${fileName}", EchoLevel.ALL)
        sh "echo '${GlobalVars.CAMPAIGN_CANNARY_PERCENTAGE_ABSIS}: ${actualIntPercentage}' >  ${fileName}"

    } else {

        String appYaml = sh(returnStdout: true, script: "cat ${fileName}")
        String newYaml = ''
        String actualPercentage = ''

        appYaml.tokenize('\n').each { x ->
            if (x.contains(GlobalVars.CAMPAIGN_CANNARY_PERCENTAGE_ABSIS)) {
                actualPercentage = x - "${GlobalVars.CAMPAIGN_CANNARY_PERCENTAGE_ABSIS}: "
                actualIntPercentage = actualPercentage.trim().toInteger()
                printOpen("The actual Percentatge is ${actualIntPercentage} the new is ${newPercentage}", EchoLevel.ALL)
            } else {
                if (x != '') newYaml = newYaml + x + '\n'
            }
        }

        sh "echo ${newYaml} > ${fileName}"
        sh "echo '${GlobalVars.CAMPAIGN_CANNARY_PERCENTAGE_ABSIS}: ${newPercentage}' >>  ${fileName}"

    }

}
