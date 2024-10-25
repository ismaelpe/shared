import com.caixabank.absis3.*

def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String datacenter) {

    refreshConfigurationViaRefreshBus(pomXmlStructure, pipelineData, datacenter, '*')

}

def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String datacenter, String color) {

    String artifactApp = pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.getGarName()))
	
	refreshConfigurationViaRefreshBus(datacenter, pipelineData.garArtifactType.getGarName(), artifactApp, pomXmlStructure.getMajorVersion(), color, pipelineData.bmxStructure.environment)
	
}

/**
 * permite refrescar via cloud bus
 * @param dataCenterParam 1 o 2 centro de datos
 * @param appTypeParam ejemplo SRV.DS
 * @param appNameParam ejemplo demoarqalm ( nombre GAR )
 * @param appMajorVesionParam 
 * @param blueOrGreenParam B o G 
 * @param configServerEnvironmentParam entorno de config server. ejemplo: dev
 * @return
 */
def call(String dataCenterParam, String appTypeParam, String appNameParam, String appMajorVersionParam, String blueOrGreenParam, String configServerEnvironmentParam) {

    checkIfConfigServerIsAlive(configServerEnvironmentParam, dataCenterParam)
    doBusRefreshCall(dataCenterParam, appTypeParam, appNameParam, appMajorVersionParam, blueOrGreenParam, configServerEnvironmentParam)
    retrieveRefreshReport(configServerEnvironmentParam, dataCenterParam)

}

/**
 * solo debe llamarse para refrescar pods con service/ruta -beta
 * @param pomXml
 * @param pipeline
 * @return
 */
def call(PomXmlStructure pomXml, PipelineData pipeline) {

	String color = resolveColorForNewService(pomXml,pipeline)
    printOpen("RESOLVED color  is ${color}", EchoLevel.INFO)
	//centro 1 
	refreshConfigurationViaRefreshBus(pomXml,pipeline, '1',color)
	//centro 2
	refreshConfigurationViaRefreshBus(pomXml,pipeline, '2',color)

}


/**
 * resuelve el color de una service/ruta -beta 	
 * @param pomXml
 * @param pipeline
 * @return
 */
def resolveColorForNewService(PomXmlStructure pomXml,PipelineData pipeline) {

	String envICP = pipeline.bmxStructure.environment
	String componentName = MavenUtils.sanitizeArtifactName(pomXml.artifactName, pipeline.garArtifactType).toUpperCase() + pomXml.getArtifactMajorVersion()
	String namespace = pomXml.isArchProject() ? "ARCH" : "APP"
	String center = "ALL"
	
	Map valuesDeployed=getLastAppInfoICP(envICP,componentName, namespace,center)
	printAppICP(valuesDeployed)
	String color = null
	try {
		//el new es la ruta beta
		color = valuesDeployed["absis"]["services"]["envQualifier"]["new"]["targetColour"]
		
	}catch(Exception e) {
		throw new RuntimeException("cannot resolve color",e)
	}
	
	return color
}

private checkIfConfigServerIsAlive(String configServerEnvironmentParam, String dataCenterParam) {

    try {

        sendRequestToConfigServer('GET',"/actuator/info", configServerEnvironmentParam, dataCenterParam)

    } catch (Exception e) {

        throw new Exception("Refresh Bus call has failed! (config-server /actuator/info failed)", e)

    }

}

private doBusRefreshCall(String dataCenterParam, String appTypeParam, String appNameParam, String appMajorVersionParam, String blueOrGreenParam, String configServerEnvironmentParam) {

    if(dataCenterParam==null || appTypeParam==null || appNameParam==null || appMajorVersionParam==null || blueOrGreenParam==null || configServerEnvironmentParam==null ) {
        throw new RuntimeException("detected null params in refresh bus")
    }

    def destiny = ""
    if (dataCenterParam == '*') {

        destiny += "**:"

    } else {

        destiny += dataCenterParam+":"

        if (appTypeParam=='*') {

            destiny += "**:"

        } else {

            destiny +=  appTypeParam+":"

            if (appNameParam == '*') {

                destiny += "**:"

            } else {

                destiny += appNameParam+":"

                if (appMajorVersionParam == '*') {

                    destiny += "**:"

                } else {

                    destiny += appMajorVersionParam+":"

                    if (blueOrGreenParam == '*') {

                        destiny += "**:"

                    } else {

                        destiny += blueOrGreenParam+":"

                    }
                }
            }
        }
    }

    //finalmente el uuid que representa una instancia de pod lo dejamos en **
    destiny += "**"

    try {

        printOpen("DESTINY bus refresh is ${destiny}", EchoLevel.INFO)
        sendRequestToConfigServer('POST', "/actuator/bus-refresh/"+destiny, configServerEnvironmentParam, dataCenterParam)

    } catch (Exception e) {

        throw new Exception("Refresh Bus call has failed! (config-server /actuator/bus-refresh failed)", e)

    }

}

private retrieveRefreshReport(String configServerEnvironmentParam, String dataCenterParam) {

    int responsesSize = 0

    try {

        //el /actuator/absis-log-bus-operations nos dice si las llamada asincrona de refresh fue recibida por las instancias, estas devuelven un ACK
        def response = 	sendRequestToConfigServer('GET',"/actuator/absis-log-bus-operations", configServerEnvironmentParam, dataCenterParam)
        def json = new groovy.json.JsonSlurper().parseText(response.content)
		if(json[0]) {
			responsesSize = json[0].responses.size()
            printOpen("latest response acks size=${responsesSize}", EchoLevel.INFO)
		}

    } catch (Exception e) {

        throw new Exception("Refresh Bus call has failed! (config-server /actuator/absis-log-bus-operations)", e)

    }

    if (responsesSize == 0) {

        printOpen('WARNING cero respuestas en ultimo tracking de respuestas ACKs contestadas por la ultima llamada asincrona de refresh', EchoLevel.DEBUG)
        printOpen("Please check the last call to <config_server>/actuator/absis-log-bus-operations in your browser in order to track the last refresh-bus call", EchoLevel.DEBUG)

    }

}
