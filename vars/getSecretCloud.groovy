import com.project.alm.*
import java.util.List
import java.util.ArrayList
import groovy.json.JsonOutput



def call(String environment, String secret, String namespace) {

	
	def response = null
	String method=""
	String command=""
	
	if (namespace=="APP" || namespace=="BOTH") {
		//Tenemos que validar primero ante todos si existe
		
		if (secret!=null)
			response=sendRequestToCloudApi("v2/api/application/PCLD/AB3APP/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials/${secret}",null,"GET","AB3APP","",false,false)
		else 
			response=sendRequestToCloudApi("v2/api/application/PCLD/AB3APP/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials",null,"GET","AB3APP","",false,false)
			
		if (response.statusCode==200) {
			def outputS=null
			response.body.each{
				outputS=JsonOutput.prettyPrint(JsonOutput.toJson(it))
				printOpen("Cloud: The secrets ${outputS}", EchoLevel.INFO)
			}			
		} else {
			printOpen("Cloud: The secrets ${secret} on NOT Found ${namespace} on ${environment.toUpperCase()}", EchoLevel.INFO)
		}	
		if (secret!=null)
			response=sendRequestToCloudApi("v2/api/application/PCLD_MIGRATED/AB3APP/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials/${secret}",null,"GET","AB3APP","",false,false)
		else
			response=sendRequestToCloudApi("v2/api/application/PCLD_MIGRATED/AB3APP/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials",null,"GET","AB3APP","",false,false)
			
		if (response.statusCode==200) {
			def outputS=null
			response.body.each{
				outputS=JsonOutput.prettyPrint(JsonOutput.toJson(it))
				printOpen("OCP: The secrets ${outputS}", EchoLevel.INFO)
			}
			
		} else {
			printOpen("OCP: The secrets ${secret} on NOT Found ${namespace} on ${environment.toUpperCase()}", EchoLevel.INFO)
		}
	}
	if (namespace=="ARCH" || namespace=="BOTH") {
		if (secret!=null)
			response=sendRequestToCloudApi("v2/api/application/PCLD/AB3COR/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials/${secret}",null,"GET","AB3COR","",false,false)
		else 
			response=sendRequestToCloudApi("v2/api/application/PCLD/AB3COR/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials",null,"GET","AB3COR","",false,false)
			
		if (response.statusCode==200) {
			def outputS=null
			response.body.each{
				outputS=JsonOutput.prettyPrint(JsonOutput.toJson(it))
				printOpen("The secrets ${outputS} Cloud", EchoLevel.INFO)
			}
		} else {
			printOpen("Cloud: The secrets ${secret} on NOT Found ${namespace} on ${environment.toUpperCase()}", EchoLevel.INFO)
		}
		if (secret!=null)
			response=sendRequestToCloudApi("v2/api/application/PCLD_MIGRATED/AB3COR/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials/${secret}",null,"GET","AB3COR","",false,false)
		else
			response=sendRequestToCloudApi("v2/api/application/PCLD_MIGRATED/AB3COR/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials",null,"GET","AB3COR","",false,false)
			
		if (response.statusCode==200) {
			def outputS=null
			response.body.each{
				outputS=JsonOutput.prettyPrint(JsonOutput.toJson(it))
				printOpen("The secrets ${outputS} OCP", EchoLevel.INFO)
			}
		} else {
			printOpen("OCP: The secrets ${secret} on NOT Found ${namespace} on ${environment.toUpperCase()}", EchoLevel.INFO)
		}
		
	}
	
	
	printOpen("******************************************************************", EchoLevel.ALL)
	printOpen("******************************************************************", EchoLevel.ALL)
	
}
