import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.DeployStructure
import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.ICPDeployStructure

import com.caixabank.absis3.ICPApiResponse

import java.util.Map

def call(Map versions) {
	printOpen("ARCH VERSIONS:", EchoLevel.ALL)
	versions.each {
		printOpen("${it.key}: ${it.value}", EchoLevel.ALL)
	}
}
