import com.project.alm.BmxStructure
import com.project.alm.BmxUtilities
import com.project.alm.DeployStructure
import com.project.alm.EchoLevel
import com.project.alm.MultipleDeploymentsState

def call(def poms = [:], String expectedVersion) {

    try {
        def pomVersions = retrieveAllArtifactVersionsFromPoms(poms, true)
        boolean hasMismatches = false
        def output = ""
        for (it in pomVersions) {
            printOpen("${it.key} has version ${it.value}", EchoLevel.ALL)
            boolean thereIsMismatch = it.value?.trim() && expectedVersion != it.value
            output = output + "[${thereIsMismatch ? ' FAIL ' : '  OK  '}] ${it.key} has version ${it.value}\n"
            if (thereIsMismatch) {
                hasMismatches = true
            }
        }
        if (hasMismatches) {
            error "We've found one or more versions in pom.xml that mismatches with the expected branch version\nExpected version was ${expectedVersion}\n${output}"
        }
    } catch (Exception e) {
        printOpen("checkAllPomVersions FAILED: ${e?.getMessage()}", EchoLevel.ALL)
        throw e
    }
}
