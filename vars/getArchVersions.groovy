import com.project.alm.EchoLevel
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.GlobalVars
import com.project.alm.DeployStructure
import com.project.alm.BmxUtilities
import com.project.alm.CloudDeployStructure

import com.project.alm.CloudApiResponse

import java.util.Map

def call(Map appsRoutes, String envCloud, String namespace, String center) {
	Map versions=new HashMap<String, Map>()
	appsRoutes.each {
		Map routes = it.value
		routes.each {
			String microURL = getK8sGatewayURL(envCloud, center);
			if (namespace=="ARCH") microURL = microURL + "/arch-service"
			microURL = microURL + "/" + it.value
			try {
				def response = sendRequest("${microURL}/actuator/info","GET")
				def json = readJSON text: response.content
				if (json.build != null && json.build.almcore != null) {
					versions.put(it.value, json.build.almcore.version)
				} else {
					versions.put(it.value, "NO_ARCH_VERSION")
				}
			} catch (Exception e) {
				printOpen("Error: ${e.getMessage()}", EchoLevel.ERROR)
				versions.put(it.value, "ERROR")
			}
		}
	}
	return versions
}

public String getK8sGatewayURL(String envCloud, String center = "AZ1") {
	//new-democonnecta2-micro-2.dev.ap.intranet.cloud.lacaixa.es
	if (center=="AZ1"|| center=="ALL") return "https://k8sgateway."+envCloud.toLowerCase()+".cloud-1.alm.cloud.lacaixa.es"
	else return "https://k8sgateway."+envCloud.toLowerCase()+".cloud-2.alm.cloud.lacaixa.es"
}


def sendRequest(String url,String httpMethod) {

	def	response = httpRequest url: "${url}",
								httpProxy: "proxyserv.svb.lacaixa.es:8080",
								consoleLogResponseBody: true,
								contentType: 'APPLICATION_JSON',
								httpMode: "${httpMethod}",
								ignoreSslErrors: true
		
	return response

}
