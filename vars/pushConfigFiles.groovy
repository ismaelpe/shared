import com.caixabank.absis3.ArtifactType
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.FileUtils
import com.caixabank.absis3.GitUtils
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineStructureType
import com.caixabank.absis3.GarAppType
import com.caixabank.absis3.Utilities

import hudson.Functions

def call(PomXmlStructure pomXml, PipelineData pipeline, boolean isOnlyCanary, boolean weHaveToUploadAppsConfig) {
    pushConfigFiles(pomXml, pipeline, isOnlyCanary, weHaveToUploadAppsConfig, mustGenerateDataSource(pipeline, pomXml))
}
/**
 * Copia los ficheros de configuración existentes en el path <pre>src/main/resources</pre> en su carpeta correspondiente del repositorio de configuración
 * según si es microservicio de arquitectura o aplicativo
 *
 * Copia todos los ficheros que comiencen por <pre>application</pre> excepto el que contenga el profile <pre>standalone</pre>.
 *
 * Procesa tanto los microsevicios simples como los de tipo agregador
 *
 * @param pomXml info del pom.xml
 * @param pipeline info de la pipeline
 * @param isOnlyCanary indica si solo estamos en canary
 * @param weHaveToUploadAppsConfig indica si hay que recargar la parte aplicativa
 * @return void
 */

def call(PomXmlStructure pomXml, PipelineData pipeline, boolean isOnlyCanary, boolean weHaveToUploadAppsConfig, boolean mustGenerateDataSource) {

    String envir = pipeline.bmxStructure.environment
    def configRepoUrlAndBranch = GitUtils.getConfigRepoUrlAndBranch(envir)
    
    
    printOpen("isOnlyCanary: ${isOnlyCanary} weHaveToUploadAppsConfig: ${weHaveToUploadAppsConfig} mustGenerateDataSource: ${mustGenerateDataSource}\n" +
        "Url: ${configRepoUrlAndBranch.url}", EchoLevel.DEBUG)

    GitRepositoryHandler git =
        new GitRepositoryHandler(this,
            configRepoUrlAndBranch.url,
            [
                checkoutBranch: configRepoUrlAndBranch.branch
            ]
        )

    try {
        
        if (mustGenerateDataSource) {

            generateDataSourceFile(pomXml, pipeline)

        }

        git.lockRepoAndDo({

            Map parameters = [
                isOnlyCanary: isOnlyCanary,
                weHaveToUploadAppsConfig: weHaveToUploadAppsConfig,
                mustGenerateDataSource: mustGenerateDataSource
            ]

            prepareAndPushAppAndArchConfigs(pomXml, pipeline, git, parameters)

        })

    } catch (err) {

        printOpen("We got an exception (pushConfigFiles)!\n\n${Functions.printThrowable(err)}", EchoLevel.ERROR)
        throw err

    } finally {

        git.purge()

    }

}

private void prepareAndPushAppAndArchConfigs(PomXmlStructure pomXml, PipelineData pipeline, GitRepositoryHandler git, Map parameters) {

    def srcFolderAndAppName = calculateDestFolderAndAppName(pomXml)

    FileUtils fileUtils = new FileUtils(this)

    boolean isOnlyCanary = parameters?.isOnlyCanary ? parameters?.isOnlyCanary : false
    boolean weHaveToUploadAppsConfig = parameters?.weHaveToUploadAppsConfig ? parameters?.weHaveToUploadAppsConfig : false
    boolean mustGenerateDataSource = parameters?.mustGenerateDataSource ? parameters?.mustGenerateDataSource : false

    String sourceFolder = srcFolderAndAppName.sourceFolder
    String appName = srcFolderAndAppName.appName

    git.clearOutGitProjectRelativePath().pullOrClone()

    String sanitizedSourceFolder = FileUtils.sanitizePath(sourceFolder)

    if (!isOnlyCanary) {

        String destFolder

        if (weHaveToUploadAppsConfig) {

            printOpen("Pushing the /apps config files of the ${appName}", EchoLevel.INFO)

            destFolder = "services/apps/" + appName
            String sanitizedDestFolder = FileUtils.sanitizePath(destFolder)

            String appConfigFolderOnRepo = "${git.getGitProjectRelativePath()}/${destFolder}"
            String sanitizedAppConfigFolderOnRepo = fileUtils.createPathIfNotExists(appConfigFolderOnRepo)

            fileUtils.cleanDirectoryContent(sanitizedAppConfigFolderOnRepo)
            fileUtils.copyFiles("${sanitizedSourceFolder}/*", "${sanitizedAppConfigFolderOnRepo}", true)

            cleanupFiles("${sanitizedAppConfigFolderOnRepo}")
            validateFiles("${sanitizedAppConfigFolderOnRepo}")

            boolean needsPush = (!mustGenerateDataSource && (pipeline.bmxStructure.environment != GlobalVars.PRO_ENVIRONMENT || !weHaveToUploadAppsConfig))

            git.add(sanitizedDestFolder)

            if (needsPush) git.commitAndPush("Config files for ${appName}")

        }

        if (mustGenerateDataSource) {

            printOpen("Pushing the /sys config files of the ${appName}", EchoLevel.INFO)
            destFolder = "services/sys/" + appName

            String sanitizedDestFolder = FileUtils.sanitizePath(destFolder)

            String appConfigFolderOnRepo = "${git.getGitProjectRelativePath()}/${sanitizedDestFolder}"

            String sanitizedAppConfigFolderOnRepo = fileUtils.createPathIfNotExists(appConfigFolderOnRepo)

            def theSameThatPreviousDataSource = sh(
                script: "diff ${sanitizedAppConfigFolderOnRepo}/application.yml ././${GlobalVars.TMP_FILE_GENERATED_DATASOURCE}",
                returnStatus: true
            )

            if (theSameThatPreviousDataSource != 0) {

                //Son diferentes
                printOpen("We have to generate the new config datasource file", EchoLevel.INFO)
                fileUtils.cleanDirectoryContent(sanitizedAppConfigFolderOnRepo)
                fileUtils.moveFiles("./${GlobalVars.TMP_FILE_GENERATED_DATASOURCE}", "${sanitizedAppConfigFolderOnRepo}/application.yml")

                boolean needsPush = (pipeline.bmxStructure.environment != GlobalVars.PRO_ENVIRONMENT || !weHaveToUploadAppsConfig)

                git.add(sanitizedDestFolder)

                if (needsPush) git.commitAndPush("Config files for ${appName}")

            } else {

                printOpen("The file is the same, we will use the previous file", EchoLevel.INFO)
                //Es el mismo fichero no se tiene que hacer nada
                if (weHaveToUploadAppsConfig) {

                    boolean needsPush = pipeline.bmxStructure.environment != GlobalVars.PRO_ENVIRONMENT

                    git.add(sanitizedDestFolder)

                    if (needsPush) git.commitAndPush("Config files for ${appName}")

                }
            }
        }
    }

    boolean weHaveToPushCanary = pipeline.bmxStructure.environment == GlobalVars.PRO_ENVIRONMENT && weHaveToUploadAppsConfig

    if (weHaveToPushCanary) {

        printOpen("Pushing the cannary files\n" +
            "pushConfigFile to PRO of the app ${appName}", EchoLevel.DEBUG)

        String arqDestFolder = "services/arch/" + appName

        String archConfigFolderOnRepo = "${git.getGitProjectRelativePath()}/${arqDestFolder}"

        String sanitizedArqDestFolder = fileUtils.createPathIfNotExists(archConfigFolderOnRepo)

        if (pipeline.pipelineStructureType == PipelineStructureType.INC_CANNARY)
            increaseCannaryPercentage("${sanitizedArqDestFolder}", pipeline.pipelineStructure.resultPipelineData.cannaryPercentage, false, appName)
        else
            increaseCannaryPercentage("${sanitizedArqDestFolder}", 0, false, appName)

        git.add(arqDestFolder).commitAndPush("Config files for ${appName}")

    }

}

private Map calculateDestFolderAndAppName(PomXmlStructure pomXml) {

    String sourceFolder = GlobalVars.RESOURCE_PATH
    String appName = pomXml.getSpringAppName()
    printOpen("appName ${appName} artifactMicro ${pomXml.artifactMicro} artifactSampleApp ${pomXml.artifactSampleApp}", EchoLevel.DEBUG)
    if (pomXml.artifactType == ArtifactType.AGREGADOR) {
        if (pomXml.artifactMicro != "") {
            sourceFolder = pomXml.artifactMicro + "/" +  GlobalVars.RESOURCE_PATH
            appName = pomXml.artifactMicro + "-" + pomXml.artifactMajorVersion
        } else if (pomXml.artifactSampleApp != "") {
            sourceFolder = pomXml.artifactSampleApp + "/" +  GlobalVars.RESOURCE_PATH
            appName = pomXml.artifactSampleApp + "-" + pomXml.artifactMajorVersion
        }
    }

    return [
        sourceFolder: sourceFolder,
        appName: appName
    ]
}

private boolean mustGenerateDataSource(PipelineData pipelineData, PomXmlStructure pomXml) {
	//return pipelineData.garArtifactType == GarAppType.DATA_SERVICE || (pipelineData.garArtifactType == GarAppType.ARCH_MICRO && hasDatabaseService(pipelineData, pomXml));
	return pipelineData.garArtifactType == GarAppType.DATA_SERVICE
}

private String getManifestPathWithSuffix(String pathToOriginalManifest, String suffix) {
	String output = "./"+pathToOriginalManifest - 'manifest.yml'
	output = output + 'manifest-' + suffix + '.yml'
	return output

}

private boolean hasDatabaseService(PipelineData pipelineData, PomXmlStructure pomXml) {
	String pathToOriginalManifest = pomXml.getRouteToManifest()
	String manifestOfApp = null
	String environment = pipelineData.bmxStructure.environment
    printOpen("Environment: "+environment, EchoLevel.DEBUG)

	if (environment.equals(GlobalVars.EDEN_ENVIRONMENT) || environment.equals(GlobalVars.DEV_ENVIRONMENT)) {
		String manifestPathWithSufix = getManifestPathWithSuffix(pathToOriginalManifest, environment);
		try {
			String manifestExists = sh(returnStdout: true, script: "[ -f ${manifestPathWithSufix} ] && echo TRUE").trim()
            printOpen("manifestExists: [${manifestExists}]", EchoLevel.DEBUG)
			if("TRUE" == manifestExists) {
				pathToOriginalManifest = manifestPathWithSufix
                printOpen("Using "+environment+"'s manifest", EchoLevel.DEBUG)
			}
		}catch(Exception e) {
            printOpen("Manifest no existente", EchoLevel.ERROR)
		}
        printOpen("Path final al manifest original ${pathToOriginalManifest}", EchoLevel.INFO)
	}
	manifestOfApp = sh(returnStdout: true, script: "cat ${pathToOriginalManifest}")
    printOpen("Content of the original manifest:\n${manifestOfApp}", EchoLevel.DEBUG)

	boolean servicesFound = false
	boolean databaseSecretFound = false;
	manifestOfApp.tokenize('\n').each { x ->
		if (x.contains('services:')) {
            printOpen("services found", EchoLevel.DEBUG)
			servicesFound = true
		} else if (servicesFound && x.trim().endsWith("database")) {
			databaseSecretFound = true
		}
	}
	return databaseSecretFound;
}
