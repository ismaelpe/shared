import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.cloudbees.groovy.cps.NonCPS
/**
 * Wrapper Echo for custom logger
 */
@NonCPS
def call(fileName, logger = null) {
    String catStdout = sh(script: "cat $fileName", returnStdout: true)
    printOpen("Printing file ${fileName}:\n${catStdout}", logger)
}
