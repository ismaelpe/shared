import com.caixabank.absis3.*
import java.util.List
import java.util.ArrayList
import groovy.json.JsonOutput



def call(String environment, String secret, String namespace) {

	
	def response = null
	String method=""
	String command=""
	
	if (namespace=="APP") {
		//Tenemos que validar primero ante todos si existe
		
		response=sendRequestToICPApi("v2/api/application/PCLD/AB3APP/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials/${secret}",null,"DELETE","AB3APP","",false,false)
		
		if (response.statusCode==200) {
			def outputS=null
			response.body.each{
				outputS=JsonOutput.prettyPrint(JsonOutput.toJson(it))
				printOpen("The secrets ${outputS}", EchoLevel.ALL)
			}
			
		}		
	}
	if (namespace=="ARCH") {
		response=sendRequestToICPApi("v2/api/application/PCLD/AB3COR/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials/${secret}",null,"DELETE","AB3COR","",false,false)
			
		if (response.statusCode==200) {
			def outputS=null
			response.body.each{
				outputS=JsonOutput.prettyPrint(JsonOutput.toJson(it))
				printOpen("The secrets ${outputS}", EchoLevel.ALL)
			}
		}
		
		
	}
	
	
	printOpen("******************************************************************", EchoLevel.ALL)
	printOpen("******************************************************************", EchoLevel.ALL)
	
}
