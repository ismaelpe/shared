import com.project.alm.*

def call(PomXmlStructure pomXml, PipelineData pipeline, String endPoint, boolean suffixedComponentName) {

	String artifactId = BmxUtilities.calculateArtifactId(pomXml,pipeline.branchStructure,true).toLowerCase()
	ICPDeployStructure deployStructure=new ICPDeployStructure('cxb-ab3cor','cxb-ab3app',pipeline.bmxStructure.environment)

	printOpen("The artifactd is ${artifactId}", EchoLevel.ALL)
	
	printOpen("The pom is ${pomXml.getBmxAppId()}", EchoLevel.ALL)
	
	//Si es feature tenemos que poner la ruta calculada
	//Se arch tenems que poner la ruta delante
	if (suffixedComponentName) artifactId = deployStructure.getSuffixedComponentName().replace("<componentName>", artifactId)

    String actuatorRefreshUrlCenter1 = getActuatorRefreshUri(GlobalVars.BMX_CD1, pomXml.isArchProject(), artifactId, endPoint)
    refreshCenter(actuatorRefreshUrlCenter1, 200)

    String actuatorRefreshUrlCenter2 = getActuatorRefreshUri(GlobalVars.BMX_CD2, pomXml.isArchProject(), artifactId, endPoint)
    refreshCenter(actuatorRefreshUrlCenter2, 201)
	
	printOpen(" End Deploy stage", EchoLevel.ALL)
}

def getActuatorRefreshUri(String center, boolean isArchProject, String artifactId, String endPoint) {

    String urlPrefix = "https://k8sgateway.pro.icp-${center}.absis.cloud.lacaixa.es"
    if (isArchProject)  urlPrefix += "/arch-service"
    String actuatorRefreshUrl = "${urlPrefix}/${artifactId}/${endPoint}"

    return actuatorRefreshUrl

}

def refreshCenter(String actuatorRefreshUrl, int expectedHTTPOKCode) {

    int numInstancias=2
    int numInstanciasRecargadas=0

    for(int i=0;i<5 && numInstanciasRecargadas<numInstancias;i++) {

        def response = runActuatorRefresh.sendRequest(actuatorRefreshUrl, [httpMode: 'POST'])

        boolean requestWentOk = HttpRequestUtilities.responseCodeIsValid(response?.status, ["100:299"])
        int dummyStatusCode = response?.status

        if (requestWentOk) {

            printOpen("The response is ${response?.content}", EchoLevel.ALL)
            if (response?.content == "[]") dummyStatusCode = 201

        } else {

            printOpen("Refresh attempt failed!!!", EchoLevel.ALL)

        }

        if (dummyStatusCode == expectedHTTPOKCode) {

            numInstanciasRecargadas++

        } else printOpen("No se ha recargado nada", EchoLevel.ALL)

    }

}
