import com.project.alm.EchoLevel
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.GlobalVars
import com.project.alm.DeployStructure
import com.project.alm.BmxUtilities
import com.project.alm.ICPDeployStructure

import com.project.alm.ICPApiResponse

import java.util.Map

def call(Map versions) {
	printOpen("ARCH VERSIONS:", EchoLevel.ALL)
	versions.each {
		printOpen("${it.key}: ${it.value}", EchoLevel.ALL)
	}
}
