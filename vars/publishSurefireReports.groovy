import com.project.alm.FileUtils
import com.project.alm.GitUtils
import com.project.alm.GlobalVars
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.SourceDestinationDirectory
import com.project.alm.Utilities
import com.sun.akuma.CLibrary

import javax.xml.transform.Source
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//TODO: Deprecate this
def call(PomXmlStructure pomXml, PipelineData pipelineData) {

    /*String result = ""

    if (!GlobalVars.PUSH_SUREFIRE) {
        printOpen("Surefire reports publishing has been temporarily disabled", EchoLevel.ALL)
        result = "Not published as PUSH_SUREFIRE is false"
        return result
    }
    printOpen("publishSurefireReports.groovy", EchoLevel.ALL)

    String catalogRepoUri = "https://git.svb.lacaixa.es/cbk/alm/services/documentation/services-catalog.git"
    String repositoryName = "services-catalog"
    FileUtils fileUtils = new FileUtils(this)

    GitRepositoryHandler git = new GitRepositoryHandler(this, repositoryName, catalogRepoUri)

    try {
        printOpen("publishSurefireReports.groovy", EchoLevel.ALL)

        git.lockRepoAndDo({

            String catalogFolder = "catalog"
            String sanitizedCatalogFolder = FileUtils.sanitizePath(catalogFolder)

            printOpen("Removing catalog folder in case it exists from previous build and cloning again", EchoLevel.ALL)

            git.purge().pullOrClone()

            String reportsCatalogBaseFolder = calculateDestinationBaseReportsFolderInWorkspace(git.gitProjectRelativePath, pomXml, pipelineData)
            List<SourceDestinationDirectory> sourceAndDestinationFolders = calculateSourceAndDestinationFolders(reportsCatalogBaseFolder)
            copySurefireReportsFromBuiltProject(sourceAndDestinationFolders)

            boolean isDirectoryNotEmpty = fileExists("${env.WORKSPACE}/" + reportsCatalogBaseFolder)

            if (isDirectoryNotEmpty) {

                def commitMessage = "Surefire reports have been published for ${pomXml.artifactName} and version ${pomXml.artifactVersion}"

                git.add().commit(commitMessage).pull()
                git.commitAndPush(commitMessage)

                printOpen("Surefire reports have been published successfully", EchoLevel.ALL)

                String surefireReportsUrl = catalogRepoUri.replace(".git", "").concat("/tree/master")
                surefireReportsUrl = surefireReportsUrl.concat(reportsCatalogBaseFolder.replace("catalog/", "/"))
                result = surefireReportsUrl

            } else {
                printOpen("${reportsCatalogBaseFolder} has no files!! No surefire-reports to be published to Git. Aborting...", EchoLevel.ALL)
            }

            return result
        })

    } catch (err) {

        printOpen("publishSurefireReports failed!\n\n${Utilities.prettyException(err)}", EchoLevel.ALL)
        throw err

    } finally {

        git.purge()

    }*/

}

private String calculateDestinationBaseReportsFolderInWorkspace(String baseFolder, PomXmlStructure pomXml, PipelineData pipelineData) {

    boolean isApplication = pomXml.isApplication()
    printOpen("Is this artifact an app? ${isApplication}", EchoLevel.ALL)

    String versionFolder = pomXml.artifactVersion

    String componentPath = baseFolder + "/jenkins-surefire-reports/services/"
    componentPath += (isApplication ? "apps" : "arch") + "/" + pomXml.artifactName

    String destPath = componentPath + "/" + versionFolder
    // PipelineData:344 forcing an extra space on currentBuild.displayName when BranchType.MASTER
    destPath += "/" + currentBuild.displayName.trim() + "-" + getLocalTimeDateNowAsString()

    printOpen("Component path is ${componentPath} and destination path is ${destPath}", EchoLevel.ALL)

    return destPath
}

private List<SourceDestinationDirectory> calculateSourceAndDestinationFolders(String reportsCatalogBaseFolder) {

    List<SourceDestinationDirectory> reportsPathsList = new ArrayList<>()
    String surefireSubFolder = "/target/surefire-reports"
    String[] pathsSurefireReports = sh(
            script: 'find . -type d -name "surefire-reports"',
            returnStdout: true
    ).split("\n")

    for (String originPath in pathsSurefireReports) {
        def artifactPath = originPath.replace(surefireSubFolder, "")
        artifactPath = artifactPath.replace(".", "")
        if (originPath.trim()) {
            reportsPathsList.add(new SourceDestinationDirectory(originPath, reportsCatalogBaseFolder + artifactPath))
            printOpen("Found location for Surefire reports: from ${originPath} to ${reportsCatalogBaseFolder + artifactPath}", EchoLevel.ALL)
        }
    }

    return reportsPathsList
}

private void copySurefireReportsFromBuiltProject(List<SourceDestinationDirectory> sourceAndDestinationFolders) {

    sourceAndDestinationFolders.each { sourceDestinationPath ->

        boolean exists = fileExists(sourceDestinationPath.originPath)

        if (exists) {
            FileUtils fileUtils = new FileUtils(this)
            fileUtils.createPathIfNotExists(sourceDestinationPath.destinationPath)
            fileUtils.cleanDirectoryContent(sourceDestinationPath.destinationPath)
            fileUtils.copyFiles(sourceDestinationPath.originPath + "/*", sourceDestinationPath.destinationPath, true)
        } else {
            printOpen("${sourceDestinationPath.originPath} does not exist! Aborting copy of Surefire reports", EchoLevel.ALL)
        }
    }
}

private String getLocalTimeDateNowAsString() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss-S")
    LocalDateTime timeDate = LocalDateTime.now()
    return timeDate.format(formatter)
}
