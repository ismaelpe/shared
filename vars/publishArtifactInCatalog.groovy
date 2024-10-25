import com.project.alm.*
import com.project.alm.GlobalVars
import com.project.alm.GarAppType
import groovy.json.JsonSlurperClassic
import java.util.List
import java.util.ArrayList


/**
 *
{"almSecurity":"resource.test","nombreMetodo":"contratosFindById","type":"GET","url":"/api/clientes/{clienteId}/contratos/{id}"},
{"almSecurity":"resource.test","nombreMetodo":"contratosCreate","type":"POST","url":"/api/clientes/{clienteId}/contratos"},
 */
def convertEndpoints(def endpoints) {
	if (endpoints!=null) {
		def catEndpoints = new ArrayList()
		endpoints.each {
			it ->
			    def listAttributes = []
			    if (it.almSecurity) {
					listAttributes = [[name: 'almSecurity', value: "${it.almSecurity}"]]
				}
				def endpoint = [
					verb: it.type,
					url: it.url,
					method: it.nombreMetodo,
					type: 'I',
					annulability: 'N',
					listAttributes: listAttributes
				]
				catEndpoints.add(endpoint)
		}
		return catEndpoints
	}else {
		return []
	}
}

/**
 *
X*
 */
def convertFeatures(def feature, def environment) {
	if (feature!=null && feature.name) {
		def featureType = 'NON'
		if (feature.name.startsWith('BBDD')) {
			featureType = 'SQL'
		}
		
		def feat = [
			env: environment,
			name: feature.name,
			type: featureType
		]
		return [ feat ]
	}else {
		return []
	}
}
/**
 *
{"tipo":"ARQ.LIB","application":"almarchcompensation","componente":"alm-arch-compensation-starter","version":"1.16.0-SNAPSHOT"}
 */
def convertDependencies(def dependencies) {
	if (dependencies!=null) {
		def catalogDependencies = new ArrayList()
		dependencies.each {
			it ->
				def depenAlm3 = [
					artifactId: it.componente,
					groupId:"NoLoSabemos",
					version:it.version
				]
				catalogDependencies.add(depenAlm3)
		}
		return catalogDependencies
	}else {
		return []
	}
}

/**
 *
{"tipo":"ARQ.LIB","application":"almarchcompensation","componente":"alm-arch-compensation-starter","version":"1.16.0-SNAPSHOT"}
 */
def convertRetroDependencies(def dependencies) {
	if (dependencies!=null) {
		def catalogDependencies = new ArrayList()
		dependencies.each {
			it ->
				def depenAlm3 = [
					tipo: it.tipo,
					application: it.application,
					name: it.componente,					
					version:it.version
				]
				catalogDependencies.add(depenAlm3)
		}
		return catalogDependencies
	}else {
		return []
	}
}


def generateUrlToCatalog(String app, String type, String env) {
	
} 

def getApp(def body) {
	return  [
		application: body.aplicacion,
		configuration: body.configuracion,
		groupId: body.groupId,
		name: body.nombreComponente,
		sourceCode: body.sourceCode,
		buildPath: body.buildPath,
		srvDeployTypeId: GlobalVars.DEFAULT_DEPLOYMENT_TYPE,
		srvTypeAppId: body.type
	]
}

def getAppForUpdate(def body) {
	return  [
		application: body.aplicacion,
		configuration: body.configuracion,
		groupId: body.groupId,
		name: body.nombreComponente,
		sourceCode: body.sourceCode,
		buildPath: env.JOB_NAME,
		srvDeployTypeId: GlobalVars.DEFAULT_DEPLOYMENT_TYPE,
		srvTypeAppId: body.type
	]
}

def call(def body, PipelineData pipelineData, PomXmlStructure pomXml, CloudStateUtility cloudStateUtilitity, boolean updateBuildPath = false) {
	def response = null
	//definimos la version de arquitectura... esto solo aplica a los artefactos
	//generados automaticamente... ADS, SE 
	def archVersion = GlobalVars.MINIMUM_VERSION_ARCH_PRO
	if (pomXml!=null) {
		archVersion=pomXml.archVersion
	}
	if (env.SEND_TO_ALM_CATALOG!="" && env.SEND_TO_ALM_CATALOG=="true") {
		try {
			printOpen("Sending data to Open's catalogue.", EchoLevel.INFO)
			
			def versionBody = [
				major 				: body.major,
				minor 				: body.minor,
				fix 				: body.fix,
				typeVersion 		: body.typeVersion,
				buildCode       	: body.buildCode,
				client     			: body.clienteJava,
				restdocs        	: body.restDocs,
				documentation 		: body.readme,
				javadoc 			: body.javaDoc,
				nexus 				: body.nexus,
				versionlog			: body.versionLog,
				archVersion			: archVersion,
				endpoints			: convertEndpoints(body.listEndPoints),
				features			: convertFeatures(body.feature, pipelineData.bmxStructure.environment),
				retroDependencies	: convertRetroDependencies(body.listDependencias)
			]
			
			printOpen("Data we are going to send to Open's catalogue:\n${versionBody}", EchoLevel.DEBUG)
			
			try {
			    response = updateVersionAppInCatalog(versionBody, body.type, body.aplicacion, pipelineData, pomXml)

				if (response.status == 200) {
					if (updateBuildPath) {
						printOpen("The app exist. Updating app...", EchoLevel.INFO)
						createOrUpdateAppInCatalog(getAppForUpdate(body), pipelineData, pomXml);
					} else {
						printOpen("The app exist. But no updating app, because updateBuildPath is $updateBuildPath...", EchoLevel.INFO)
					}
				} else if (response.status == 404) {
					printOpen("The app doesn't exist. Creating app...", EchoLevel.INFO)
					response = createOrUpdateAppInCatalog(getApp(body), pipelineData, pomXml);
					if (response.status == 200) {
						printOpen("Try update app version in catalog", EchoLevel.INFO)
						updateVersionAppInCatalog(versionBody, body.type, body.aplicacion, pipelineData, pomXml)
					}
				} else {
					printOpen("Unknown catalog response code $response.status", EchoLevel.INFO)
				}
				
				//Si el artefacto es un library o un Plugin... no se despliega en ningun sitio
				if (pomXml!=null && pipelineData.garArtifactType!=null && 
					pipelineData.garArtifactType!=GarAppType.ARCH_PLUGIN && 
					pipelineData.garArtifactType!=GarAppType.ARCH_LIBRARY &&  
					pipelineData.garArtifactType!=GarAppType.LIBRARY &&
					pipelineData.garArtifactType!=GarAppType.SRV_CONFIG &&
					pipelineData.garArtifactType!=GarAppType.ARCH_CONFIG) {
					deployArtifactInCatalog(body,pipelineData,pomXml,cloudStateUtilitity)
				}

			} catch(Exception ex) {
				
				def appBody = getApp(body)
				printOpen("Error sending data to Open's catalogue: ${ex.getMessage()}", EchoLevel.ERROR)
                throw ex
			}
			
		} catch(Exception ex) {
			printOpen("Error en el envio al catalogo de alm ", EchoLevel.ERROR)
			if (env.SEND_TO_ALM_CATALOG_REQUIRED!=null && env.SEND_TO_ALM_CATALOG_REQUIRED!="true") {
				throw new Exception("Unexpected response from CATALOG, services catalog ")
			}
		}
	}else {
		printOpen("Open's catalogue is currently offline", EchoLevel.INFO)
	}
}

def createOrUpdateAppInCatalog(def body, PipelineData pipelineData, PomXmlStructure pomXml) {
	printOpen("$body", EchoLevel.INFO)
	return sendRequestToAlm3MS(
		'PUT',
		"$GlobalVars.URL_CATALOGO_ALM_PRO/app",
		body,
		"$GlobalVars.CATALOGO_ALM_ENV",
		[
			kpiAlmEvent: new KpiAlmEvent(
				pomXml, pipelineData,
				KpiAlmEventStage.UNDEFINED,
				KpiAlmEventOperation.CATALOG_HTTP_CALL)
		])
		
}

def updateVersionAppInCatalog(def body, def type, def application, PipelineData pipelineData, PomXmlStructure pomXml) {
	printOpen("$body", EchoLevel.INFO)
	return sendRequestToAlm3MS(
		'PUT',
		"$GlobalVars.URL_CATALOGO_ALM_PRO/app/$type/$application/version",
		body,
		"$GlobalVars.CATALOGO_ALM_ENV",
		[
			kpiAlmEvent: new KpiAlmEvent(
				pomXml, pipelineData,
				KpiAlmEventStage.UNDEFINED,
				KpiAlmEventOperation.CATALOG_HTTP_CALL)
		])
}



