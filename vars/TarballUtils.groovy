import com.caixabank.absis3.*

class TarballUtils implements Serializable {

    static String tarDirectory2File(String pathToStore, String tarFilename, scriptContext) {

        String workingDirectory = scriptContext.sh(script: "pwd", returnStdout: true).split()[0].trim()
        return tarDirectory2File(pathToStore, tarFilename, scriptContext, workingDirectory)
    }

    static String tarDirectory2File(String pathToStore, String tarFilename, scriptContext, String workingDirectory) {

        String sanitizedPathToStore = pathToStore.replace(' ', '\\ ')
        workingDirectory = workingDirectory ? workingDirectory : scriptContext.sh(script: "pwd", returnStdout: true).split()[0].trim()

        String tarballPath = "${workingDirectory}/${tarFilename}"
        scriptContext.printOpen("TarballUtils.tarDirectory2File(): Storing ${pathToStore} into ${tarballPath}", EchoLevel.DEBUG)
        scriptContext.sh(script: "cd ${sanitizedPathToStore} && tar --create --verbose --file='${tarballPath}' *", returnStdout: true)

        return tarballPath
    }

}
