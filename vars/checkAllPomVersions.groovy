import com.caixabank.absis3.BmxStructure
import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.DeployStructure
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.MultipleDeploymentsState

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
