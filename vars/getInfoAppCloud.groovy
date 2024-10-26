import com.project.alm.*

def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String endpoint, String environment) {
  	String suffix=""

	if (pomXmlStructure.isArchProject()) {
		suffix="/arch-service"
	}

	String artifactRoute = BmxUtilities.calculateRoute(pomXmlStructure, pipelineData.branchStructure)

    // EN caso de que se trate de un rollback en TST de una SNAPSHOT no debería incluir "-dev".
    artifactRoute = pipelineData.bmxStructure.environment.toUpperCase().equals("TST") ? artifactRoute.replace("-dev", "") : artifactRoute

	def url = "https://k8sgateway.${environment}.cloud-1.alm.cloud.lacaixa.es$suffix/$artifactRoute/$endpoint"

    printOpen("Lanzando peticion contra $url", EchoLevel.INFO)

	def response = httpRequestUtils.send([url: url, verb: 'GET', ignoreSslErrors: true, needsProxy: true, parseResponse: true, validResponsesCodes: '200:600', timeout: 45])
	
	if (response.statusCode == 500) {
		return null
	} else {
		return response.content
	}
}
