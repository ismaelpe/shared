import com.caixabank.absis3.*

class SurefireReportsHandler implements Serializable {

    static final String SCU_GIT_REPO_PATH = "build-surefire-reports"

    private final def scriptContext
    private final String projectRootPath
    private final String buildReportsBasePath
    private final PomXmlStructure pomXml
    private final SurefireReportsCopyUtils surefireCopyHelper
    private String buildReportsPath

    private SurefireReportsHandler(scriptContext, String projectRootPath, String buildReportsBasePath, PomXmlStructure pomXml) {
        this.scriptContext = scriptContext
        this.projectRootPath = projectRootPath
        this.buildReportsBasePath = buildReportsBasePath ? buildReportsBasePath : SCU_GIT_REPO_PATH
        this.pomXml = pomXml
        this.surefireCopyHelper = new SurefireReportsCopyUtils(scriptContext)
    }

    static init(scriptContext, String projectRootPath, String buildReportsBasePath, PomXmlStructure pomXml) {
        SurefireReportsHandler instance = new SurefireReportsHandler(scriptContext, projectRootPath, buildReportsBasePath, pomXml)
        instance.buildReportsPath = instance.calculateBuildReportsPath(buildReportsBasePath, pomXml.isApplication(), pomXml.getArtifactName(), pomXml.getArtifactVersion(), scriptContext.currentBuild.displayName)
        instance.buildReportsPath = instance.pullFromProjectAndGetPublishingPath()
        return instance
    }

    String getPublishingPath() {
        return buildReportsPath
    }

    String getSurefireReportsBaseURL() {
        buildReportsPath = buildReportsPath ? buildReportsPath.replace("${SCU_GIT_REPO_PATH}/", "") : ''
        return URLEncoder.encode(buildReportsPath, "UTF-8").replace("%2F", "/")
    }

    private String pullFromProjectAndGetPublishingPath() {

        scriptContext.printOpen("SurefireReportsHandler.pullFromProjectAndGetPublishingPath(): Calculated buildReportsPath is: ${buildReportsPath}", EchoLevel.INFO)
        List<SourceDestinationDirectory> sourceAndDestinationFolders = surefireCopyHelper.calculateSourceAndDestinationFolders(projectRootPath, buildReportsPath)
        surefireCopyHelper.copySurefireReportsFromBuiltProject(sourceAndDestinationFolders, true)
        boolean isDirectoryNotEmpty = scriptContext.fileExists("${scriptContext.env.WORKSPACE}/${buildReportsPath}")

        if (isDirectoryNotEmpty) {
            scriptContext.printOpen("SurefireReportsHandler.pullFromProjectAndGetPublishingPath(): ${buildReportsPath} has files to be published.", EchoLevel.INFO)
            return buildReportsPath
        } else {
            scriptContext.printOpen("SurefireReportsHandler.pullFromProjectAndGetPublishingPath(): ${buildReportsPath} has no files!! No surefire-reports to be published.", EchoLevel.INFO)
            return null
        }
    }

    private String calculateBuildReportsPath(String baseFolder, boolean isApplication, String artifactName, String artifactVersion, String buildDisplayName) {

        scriptContext.printOpen("SurefireReportsHandler.calculateBuildReportsPath(): Is this artifact an app? ${isApplication}", EchoLevel.INFO)

        String componentPath = baseFolder + "/jenkins-surefire-reports/services/"
        componentPath += (isApplication ? "apps" : "arch") + "/" + artifactName

        String destPath = componentPath + "/" + artifactVersion
        // PipelineData:344 forcing an extra space on currentBuild.displayName when BranchType.MASTER
        destPath += "/" + buildDisplayName.trim() + "-" + com.caixabank.absis3.DatesAndTimes.getLocalTimeDateNowAsString()

        scriptContext.printOpen("SurefireReportsHandler.calculateBuildReportsPath(): Component path is ${componentPath} and destination path is ${destPath}", EchoLevel.INFO)

        return destPath
    }

    void destroy() {
        scriptContext.printOpen("SurefireReportsHandler.destroy(): ${buildReportsBasePath} will be now deleted", EchoLevel.INFO)
        String sanitizedBuildReportsBasePath = buildReportsBasePath.replace(' ', '\\ ')
        scriptContext.sh "rm -rf ${sanitizedBuildReportsBasePath}"
    }

}
