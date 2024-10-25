import com.project.alm.YamlUtils
import com.cloudbees.groovy.cps.NonCPS
import com.project.alm.EchoLevel
/**
 * Check the config files uploaded for a compoment.
 * @param destFolder
 * @return
 */

def call(String destFolder) {

	sh "find '${destFolder}' -type f ! -name application* -delete"
	sh "find '${destFolder}' -type f  -name *standalone* -delete"


	String sanitizedDestFolder = destFolder.replace(' ', '\\ ')
    String dirContent = sh(script: "ls -la ${sanitizedDestFolder}", returnStdout: true)
    printOpen("Directory content:\n${dirContent}", EchoLevel.DEBUG)
}
