import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.cloudbees.groovy.cps.NonCPS
/**
 * Wrapper Echo for custom logger
 */
@NonCPS
def call(fileName, logger = null) {
    String catStdout = sh(script: "cat $fileName", returnStdout: true)
    printOpen("Printing file ${fileName}:\n${catStdout}", logger)
}
