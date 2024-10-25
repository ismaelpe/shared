import com.project.alm.BmxStructure
import com.project.alm.BmxUtilities
import com.project.alm.DeployStructure
import com.project.alm.MultipleDeploymentsState

def call(String filename) {

    def files = [:]

    try {
        def listFilesAsString = sh(returnStdout: true, script: "find . -name \"${filename}\" -not -path \"*src/*\" -not -path \"*target/*\" | cat")
        listFilesAsString.trim().tokenize('\n').each {
            def foundFilename = it.replace("./", "")
            def foundFileContent = readFile "${WORKSPACE}/${foundFilename}"
            files.put(it, foundFileContent)
        }
        return files

    } catch (Exception e) {
        echo e?.getMessage()
        throw e
    }
}
