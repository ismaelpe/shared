import com.project.alm.*
import hudson.model.StringParameterValue
import groovy.json.JsonSlurperClassic
import com.project.alm.GarAppType

String getArtifactType(String groupId) {
	if (groupId.indexOf('.dataservice')!=-1) return GarAppType.DATA_SERVICE.name
	else if (groupId.indexOf('.arch')!=-1) return GarAppType.ARCH_MICRO.name
	else return GarAppType.MICRO_SERVICE.name
}

String getAppName(String artifactId) {
	String app=""
	
	app=artifactId-'-micro-server'
	app=app-'-micro'
	app=app.toLowerCase()
	
	return app
}

String getMajor(String version) {
	if (version.indexOf('.')!=-1) {
		return version.substring(0,version.indexOf('.'))
	}else {
		return version
	}
}


def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String datacenter,String environment) {
		
	String environmentUpperCase=""
	
	if (environment!=null) environmentUpperCase=environment.toUpperCase()
	
	String major=MavenVersionUtilities.getMajor(pomXmlStructure.getArtifactVersion())
	String minor=MavenVersionUtilities.getMinor(pomXmlStructure.getArtifactVersion())
	String fix=MavenVersionUtilities.getPatch(pomXmlStructure.getArtifactVersion())
	printOpen("Major: ${major}, minor: ${minor}, fix: ${fix}", EchoLevel.ALL)
	
	String type=pipelineData.garArtifactType.getGarName()
	String application=pomXmlStructure.getApp(pipelineData.garArtifactType)
	printOpen("Type: ${type}, Application: ${application}", EchoLevel.ALL)
	
	String typeVersion=""
	
	if (pomXmlStructure.artifactVersionQualifier == null || pomXmlStructure.artifactVersionQualifier == "") {
		typeVersion = "RELEASE"
	} else if (pomXmlStructure.isSNAPSHOT()) {
		typeVersion = "SNAPSHOT"
	} else if (pomXmlStructure.isRCVersion()) {
		typeVersion = "RC"
	} else { 
		typeVersion = "UNKNOWN"
	}
	
    def response=null
	
	response = sendRequestToAlm3MS(
        'GET',
        "${GlobalVars.URL_CATALOGO_ALM_PRO}/app/${type}/${application}/version/${major}/${minor}/${fix}/${typeVersion}/dependency/invokers/${environmentUpperCase}?recursive=true&ignoreFix=true",
        null,
        "${GlobalVars.CATALOGO_ALM_ENV}",
        [
            kpiAlmEvent: new KpiAlmEvent(
                pomXmlStructure, pipelineData,
                KpiAlmEventStage.UNDEFINED,
                KpiAlmEventOperation.CATALOG_HTTP_CALL)
        ])
		
	if (response.status == 200) {
		printOpen("Deploy realizado", EchoLevel.ALL)
		//def responseJson = new JsonSlurperClassic().parseText(response.content)
		def responseJson = response.content
		if(responseJson!=null) {
			responseJson.each{
				
				String appType=it.appType
				String majorV=getMajor(it.version)
				String app=it.application
				
				printOpen("${it.artifactId}", EchoLevel.ALL)
				printOpen("${it.version}", EchoLevel.ALL)
				printOpen("${it.groupId}", EchoLevel.ALL)
				
				printOpen("${app}", EchoLevel.ALL)
				printOpen("${majorV}", EchoLevel.ALL)
				printOpen("${appType}", EchoLevel.ALL)
				
				if (datacenter=='BOTH' || datacenter=='CD1') {

                    refreshConfigurationViaRefreshBus(GlobalVars.BMX_CD1, appType, app, majorV, '*', environment)

				}
				if (datacenter=='BOTH' || datacenter=='CD2') {

                    refreshConfigurationViaRefreshBus(GlobalVars.BMX_CD2, appType, app, majorV, '*', environment)

				}
			}
		}
	} else {
		printOpen("Error al proceder a la recuperacion de los invocantes", EchoLevel.ALL)
	}	
	
}
	
