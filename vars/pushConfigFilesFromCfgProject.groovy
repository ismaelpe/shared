import com.project.alm.EchoLevel
import com.project.alm.FileUtils
import com.project.alm.GitUtils
import com.project.alm.GlobalVars
import com.project.alm.PomXmlStructure
import com.project.alm.Utilities

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
 * @return void
 */
def call(PomXmlStructure pomXml, String environment) {

    printOpen("pomXml: ${pomXml} environment: ${environment}", EchoLevel.ALL)
    FileUtils fileUtils = new FileUtils(this)

    GitRepositoryHandler git =
        new GitRepositoryHandler(this,
            GitUtils.getConfigRepoUrlAndBranch(environment).url,
            [
                checkoutBranch: GitUtils.getConfigRepoUrlAndBranch(environment).branch
            ]
        )

    try {

        git.lockRepoAndDo({

            git.clearOutGitProjectRelativePath().pullOrClone()

            String sourceFolder = GlobalVars.RESOURCE_PATH
            String appName = pomXml.getAppNameWithMajorMinorVersion()
            printOpen("appName ${appName}", EchoLevel.ALL)

            String sanitizedSourceFolder = FileUtils.sanitizePath(sourceFolder)

            printOpen("Pushing the app config files of the ${appName}", EchoLevel.ALL)
            String dirContent = sh(script: "ls -la ${sanitizedSourceFolder}", returnStdout: true)
            printOpen("Directory content:\n${dirContent}", EchoLevel.DEBUG)

            String destFolder = "services/apps/" + appName
            String sanitizedDestFolder = FileUtils.sanitizePath(destFolder)

            String appConfigFolderOnRepo = "${git.getGitProjectRelativePath()}/${destFolder}"
            String sanitizedAppConfigFolderOnRepo = fileUtils.createPathIfNotExists(appConfigFolderOnRepo)

            printOpen("Deleting previous files", EchoLevel.ALL)
            fileUtils.cleanDirectoryContent(sanitizedAppConfigFolderOnRepo)

            fileUtils.copyFiles("${sanitizedSourceFolder}/*", "${sanitizedAppConfigFolderOnRepo}", true)

            cleanupFiles("${sanitizedAppConfigFolderOnRepo}")
            validateFiles("${sanitizedAppConfigFolderOnRepo}")

            printOpen("copia origen: ${sourceFolder}/*,  destino: ${appConfigFolderOnRepo}, appName:${appName}, enviroment:${environment}", EchoLevel.ALL)
            dirContent = sh(script: "ls -Rla ${sanitizedAppConfigFolderOnRepo}/", returnStdout: true)
            printOpen("Directory content:\n${dirContent}", EchoLevel.DEBUG)

            git.add(sanitizedDestFolder).commitAndPush("Config files for ${appName}")

        })

    } catch (err) {

        printOpen("We got an exception (pushConfigFilesFromCfgProject)!\n\n${Utilities.prettyException(err)}", EchoLevel.ALL)

    } finally {

        git.purge()

    }

}
