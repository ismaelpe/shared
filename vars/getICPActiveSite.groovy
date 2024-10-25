import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.ICPDeployStructure


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
	
	ICPDeployStructure deployStructure=new ICPDeployStructure('cxb-ab3cor','cxb-ab3app',environment)
	
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
