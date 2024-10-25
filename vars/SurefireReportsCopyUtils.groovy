import com.caixabank.absis3.*

import javax.xml.transform.Source
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SurefireReportsCopyUtils implements Serializable {

    private final def scriptContext

    SurefireReportsCopyUtils(scriptContext) {
        this.scriptContext = scriptContext
    }

    List<SourceDestinationDirectory> calculateSourceAndDestinationFolders(String reportsDestinationBaseFolder) {
        this.calculateSourceAndDestinationFolders(null, reportsDestinationBaseFolder)
    }

    List<SourceDestinationDirectory> calculateSourceAndDestinationFolders(String reportsOriginBaseFolder, String reportsDestinationBaseFolder) {

        List<SourceDestinationDirectory> reportsPathsList = new ArrayList<>()

        reportsOriginBaseFolder = reportsOriginBaseFolder ? reportsOriginBaseFolder : '.'
        String surefireSubFolder = "/target/surefire-reports"
        String[] pathsSurefireReports = scriptContext.sh(
                script: "find ${reportsOriginBaseFolder} -type d -name 'surefire-reports'",
                returnStdout: true
        ).split("\n")

        for (String originPath in pathsSurefireReports) {
            String artifactPath = originPath.replace(surefireSubFolder, "")
            artifactPath = artifactPath.replace("${reportsOriginBaseFolder}", "")
            if (originPath.trim()) {
                reportsPathsList.(new SourceDestinationDirectory(originPath, reportsDestinationBaseFolder + artifactPath))
                scriptContext.printOpen("SurefireReportsCopyUtils.calculateSourceAndDestinationFolders(): Found location for Surefire reports: from ${originPath} to ${reportsDestinationBaseFolder + artifactPath}", EchoLevel.INFO)
            }
        }

        return reportsPathsList
    }

    void copySurefireReportsFromBuiltProject(List<SourceDestinationDirectory> sourceAndDestinationFolders, boolean wipeDestinationFolderBeforeCopy) {

        sourceAndDestinationFolders.each { sourceDestinationPath ->
            final String[] sourcePathFileList = scriptContext.sh(script: "ls -1d ${sourceDestinationPath.originPath}", returnStdout: true).split()
            boolean exists = sourcePathFileList.length != 0

            if (exists) {
                new FileUtils(this).createPathIfNotExists(sourceDestinationPath.destinationPath)
                if (wipeDestinationFolderBeforeCopy) {
                    this.cleanFolderContent(sourceDestinationPath.destinationPath)
                }
                FileUtils fileUtils = new FileUtils(scriptContext)
                fileUtils.copyFiles(sourceDestinationPath.originPath + "/*", sourceDestinationPath.destinationPath, true)
            } else {
                scriptContext.printOpen("SurefireReportsCopyUtils.copySurefireReportsFromBuiltProject(): ${sourceDestinationPath.originPath} does not exist! Aborting copy of Surefire reports", EchoLevel.INFO)
            }
        }
    }

    private void cleanFolderContent(String destPath) {
        scriptContext.printOpen("SurefireReportsCopyUtils.cleanFolderContent(): Cleaning folder ${destPath} content", EchoLevel.INFO)
        String sanitizedDestPath = destPath.replace(' ', '\\ ')
        scriptContext.sh "rm -rf ${sanitizedDestPath}/*"
    }

}
