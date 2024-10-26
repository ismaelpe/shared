import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.CloudDeployStructure


def checkUrl(def microUrl) {
	def urlParameters=[:]
	
	urlParameters.needsProxy=true
	urlParameters.url=microUrl
	urlParameters.parseResponse=false
	urlParameters.timeout=10
	urlParameters.verb='GET'
	urlParameters.ignoreSslErrors=false
	urlParameters.validResponsesCodes='200:300'
	
	def response=httpRequestUtils.send(urlParameters)
	if (response.statusCode==200) {
		return true
	}else {
		return false
	}
}

def call(String environment) {
	
	CloudDeployStructure deployStructure=new CloudDeployStructure('cxb-ab3cor','cxb-ab3app',environment)
	
	String microUrl= deployStructure.getUrlActuatorPrefixTesting() + deployStructure.getUrlSuffixTesting()+"/"+GlobalVars.ENDPOINT_INFO
	
	boolean siteIsRunning=checkUrl(microUrl)
	if (siteIsRunning) {
		 printOpen("El site 1 funciona ", EchoLevel.INFO)
		 return "1"
	}else {
		printOpen("El site 2 ni lo validamos... no tenemos mas ",EchoLevel.INFO)
		return "2"
	}

}
