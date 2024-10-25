import static java.util.Map.entry
import com.project.alm.GlobalVars
import com.project.alm.GplRequestStatus
import com.project.alm.GplUtilities
import groovy.json.JsonSlurper
import java.util.Date
import com.project.alm.*


class Maps {
	public static final String TYPE_MAVEN_PROJECT = "maven-project"
	public static final String LANGUAJE_JAVA = "java"
	public static final String JAVA_VERSION = "1.8"
	
	def static  commonParams = [
	"type"            : TYPE_MAVEN_PROJECT,
	"language"        : LANGUAJE_JAVA,
	"bootVersion"     : GlobalVars.INITITALIZR_DEFAULT_BOOT_VERSION,
	"javaVersion": JAVA_VERSION,
	"autocomplete"    : "",
	"generate-project": ""
	
	]
	
	
	public static  microServiceSimple = [
		"simpleProject"     : "true",
		"absisApp": "MICRO_SERVICE",
		"packaging": "jar",
		"description":"Micro_Service_aplicativo",
		"groupId": "com.project.absis.apps.service"
		
	]
	
	public static dataServiceSimple = [
		"simpleProject": "true",
		"absisApp": "DATA_SERVICE",
		"groupId": "com.project.absis.apps.dataservice",
		"description": "Dataservice_aplicativo",
		"packaging": "jar"
	]
	
	public static bffServiceSimple = [
		"simpleProject": "true",
		"absisApp": "BFF_SERVICE",
		"groupId": "com.project.absis.apps.bff",
		"description": "Backend_for_FrontEnd",
		"packaging": "jar"
	]
	
	
	public static appCommonLibSimple = [
		"simpleProject": "true",
		"absisApp": "APP_COMMON_LIB",
		"groupId": "com.project.absis.apps.lib",
		"description": "Librería_común_de_aplicaciones",
		"packaging": "jar"
	]
	
	//FIXME never used appCommonLibAgregador
	public static appCommonLibAgregador = [
		"simpleProject": "false",
		"aggregator": "APP_LIB",
		"groupId": "com.project.absis.apps.lib",
		"description": "Librería_común_de_aplicaciones_con_sample_app",
		"packaging": "jar"
	]
	
	public static  microArch = [
		"simpleProject"     : "false",
		"aggregator": "MICRO_ARCH",
		"packaging": "jar",
		"description":"Micro_Service_arquitectura",
		"groupId": "com.project.absis.arch"
		
	]
	
	public static  pluginArch = [
		"simpleProject"     : "false",
		"aggregator": "PLUGIN",
		"packaging": "jar",
		"description":"Plugin_arquitectura",
		"groupId": "com.project.absis.arch"
	]
	
	public static archConfig = [
		"simpleProject": "true",
		"absisApp": "ARCH_CFG",
		"groupId": "com.project.absis.arch.config.lib",
		"description": "Librería_de_configuracion_de_aplicaciones",
		"packaging": "jar"
	]
	
	
	public static srvConfig = [
		"simpleProject": "true",
		"absisApp": "SRV_CFG",
		"groupId": "com.project.absis.apps.config.lib",
		"description": "Librería_de_configuracion_de_aplicaciones",
		"packaging": "jar"
	]
	
	
	public static archCommonLib = [
		"simpleProject": "true",
		"absisApp": "ARCH_API_LIB",
		"groupId": "com.project.absis.arch",
		"description": "Librería_común_de_arquitectura",
		"packaging": "jar"
	]
	
	public static archStarterLib = [
		"simpleProject": "false",
		"aggregator": "ARCH_LIB",
		"groupId": "com.project.absis.arch",
		"description": "Librería_starter_de_arquitectura",
		"packaging": "jar"
	]
	
	public static  paramsSets =  [
			"SRV.MS":microServiceSimple,
			"SRV.DS":dataServiceSimple,
			"SRV.BFF":bffServiceSimple,
			"SRV.LIB":appCommonLibSimple,
			"ARQ.MIA":microArch,
			"ARQ.MAP":pluginArch,
			"ARQ.CFG":archConfig,
			"SRV.CFG":srvConfig,
			"ARQ.LIB":archStarterLib
			]
  }



def getArtifactName(def nameApp,def typeApp) {
  def nameArtifact = nameApp
  if (typeApp == "Library" || typeApp == "SRV.LIB" || typeApp == "ARQ.LIB") {
      if (!nameApp.contains('-lib')) nameArtifact = nameApp + '-lib'
  } else if (typeApp == "MicroService" || typeApp == "SRV.MS" || typeApp == "DataService" || typeApp == "SRV.DS" || typeApp == 'ARQ.MIA' || typeApp == "SRV.BFF") {
      if (!nameApp.contains('-micro')) nameArtifact = nameApp + '-micro'
  } else if (typeApp == 'ARQ.MAP') {
      if (!nameApp.contains('-plugin')) nameArtifact = nameApp + '-plugin'
  } else if (typeApp == 'ARQ.CFG' || typeApp == "SRV.CFG") {
    if (!nameApp.contains('-conf')) nameArtifact = nameApp + '-conf'
  }
  return nameArtifact
}
  
def prepareParams(String empresa,String nameApp,String typeApp,String domain) {
	
	empresa = empresa.toUpperCase()
	typeApp = typeApp.toUpperCase()
	
	//verificacion si type existe
	def existType = GarAppType.valueOfType(typeApp)
	if(!existType) {
		throw new Exception("GarAppType no reconocido:"+typeApp)
	}
	
	Map params = Maps.paramsSets
						.get(typeApp)
						.clone()
	def artifactId = getArtifactName(nameApp,typeApp)	
	
	def domainNormalized = domain.replaceAll('/', '.')
	
						
	params.putAll(Maps.commonParams)
	params.put("absisCompany",empresa)
	params.put("baseDir",nameApp)
	params.put("groupId", (params.get("groupId") + "." +empresa + "."+domainNormalized).toLowerCase())
	params.put("groupIdNoCompany",params.get("groupId"))
	params.put("artifactId",artifactId)
	params.put("name", nameApp)
	params.put("packageName",params.get("groupId"))
	return params
	
}
def call(String empresa,String nameApp,String typeApp,String domain) {
	sendRequestToInitializr(empresa,nameApp,typeApp,domain,nameApp)
}

def call(String empresa,String nameApp,String typeApp,String domain,String zipFileName) {

    printOpen("Performing ALM request to Initlizr", EchoLevel.ERROR)
	
	
	if(!empresa || !nameApp || !typeApp || !domain){
		throw new Exception("Parameters (empresa,nameApp,typeApp,domain) must not be null")
	}
	
	def paramsForApp = prepareParams( empresa, nameApp, typeApp, domain)
	
	String params = Utilities.paramMapToHTTPGetStringParam(
								paramsForApp
							  )
	
    def response = null
	GplRequestStatus statusGpl = new GplRequestStatus()
	def fecha = null
	
    timeout(GlobalVars.DEFAULT_ALM_MS_REQUEST_RETRIES_TIMEOUT) {
        waitUntil(initialRecurrencePeriod: 15000) {
            try {
                fecha = new Date()
                printOpen("Absis3 Initilizr iteration at date ${fecha}", EchoLevel.DEBUG)
					
					String url = GlobalVars.URL_ZIP_INITIALIZR_PRO + params

					response = httpRequest consoleLogResponseBody: false,
						httpMode: "GET",
						url: url ,
						httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
						outputFile: "${zipFileName}.zip"

						printOpen("Template de proyecto '${nameApp}' descargado desde el initializr", EchoLevel.ALL)
						

                return true
            } catch (Exception e) {
				printOpen(e.getMessage(), EchoLevel.ERROR)
                return GplUtilities.evaluateResponse(response, statusGpl)
            }
        }
    }


	if (response.status == 200 ) {
		//return the nameApp, if everything was fine 
        return paramsForApp
    } else throw new Exception("Unexpected response when sending request to Absis3 Initilizr! response (${response?.status})")

}


