import com.project.alm.*
import java.util.List
import java.util.ArrayList

def validateTieneElSecret(def clusterArrays) {
	
	def tieneSecret=false
	
	if (clusterArrays!=null) {
		for(Iterator iter = clusterArrays.iterator(); iter.hasNext();) {
			def cluster=iter.next()
			if (cluster.secrets!=null && cluster.secrets.size()>0) {				
				tieneSecret=true
				break	
			} else {				
				tieneSecret=false
				break
			}
		}		
	}else{
		return false
	}
	return tieneSecret
		
}

def validateSecretOCP(def response = null) {
	if (response.statusCode==200) {
		//[{"az":"AZ2","secrets":[],"scopeCredential":"APPLICATION"},{"az":"AZ1","secrets":[],"scopeCredential":"APPLICATION"}]
		//Esto es un secret en vault que no existe
		if (response.body!=null) {
			def validateSecretVar=validateTieneElSecret(response.body)
			if (validateTieneElSecret(response.body)) {
				return true
			}else{
				return false
			}				
		}else {
			return false
		}
	}else {
		return false
	}
	
}


def call(String environment, String secret, String username, String password, String uri, String namespace, String tipoApp, String app) {

	def body = [
		data: [
			[key: "password",value:  ((password?.trim()) ? "${password}" : null)],
			[key: "username",value:  ((password?.trim()) ? "${username}" : null)],
			[key: "uri",value:"${uri}"],
		],
		name: "${secret}",
		type: "CREDENTIALS"
	]
	
	def response = null
	def response1 = null
	String method=""
	String command=""
	def microOwnerOfTheSecret=null
	//Validaremos que no tengamos un error
	if ((secret.contains("mq") || secret.contains("kafka")) && (app==null || "".equals(app) || tipoApp==null || "".equals(tipoApp))) {
		throw new Exception("Esta creando un secret de mq o kafka... y no ha actualizado la app")
	}
	
	if (namespace=="APP" || namespace=="BOTH") {
		//Tenemos que validar primero ante todos si existe
		printOpen("El environment es de ${environment} el secret es de ${secret}", EchoLevel.ALL)
		response=sendRequestToICPApi("v2/api/application/PCLD/AB3APP/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials/${secret}",null,"GET","AB3APP","",false,false)
		if (response.statusCode==200) {
			method="PUT"
			command="v2/api/application/PCLD/AB3APP/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials/${secret}"
		}else {
			//no existe
			method="POST"
			command="v2/api/application/PCLD/AB3APP/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials"			
		}
		response=sendRequestToICPApi("${command}",body,method,"AB3APP","",false,false)
		if (response.statusCode==201 || response.statusCode==200) {
			printOpen("SECRET CREATED OR MODIFIED ${method} ICP. Procedemos contra OCP ", EchoLevel.INFO)
			printOpen("Ahora ejecutamos contra OCP. ", EchoLevel.INFO)
			
			response=sendRequestToICPApi("v2/api/application/PCLD_MIGRATED/AB3APP/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials/${secret}",null,"GET","AB3APP","",false,false)
			if (response.statusCode==200 && validateSecretOCP(response)) {
				method="PUT"
				command="v2/api/application/PCLD_MIGRATED/AB3APP/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials/${secret}"
			}else {
				//no existe
				method="POST"
				command="v2/api/application/PCLD_MIGRATED/AB3APP/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials"
			}
			response=sendRequestToICPApi("${command}",body,method,"AB3APP","",false,false)
			if (response.statusCode==201 || response.statusCode==200) {
				printOpen("SECRET CREATED OR MODIFIED ${method} OCP ", EchoLevel.INFO)
			}else {
				printOpen("ERROR CREATING SECRET", EchoLevel.ERROR)
				throw new Exception("The code ${response.body}")
			}			
		}else {
			printOpen("ERROR CREATING SECRET", EchoLevel.ERROR)
			throw new Exception("The code ${response.body}")			
		}	
	}
	if (namespace=="ARCH" || namespace=="BOTH") {
		printOpen("El environment es de ${environment} el secret es de ${secret}", EchoLevel.ALL)
		response=sendRequestToICPApi("v2/api/application/PCLD/AB3COR/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials/${secret}",null,"GET","AB3COR","",false,false)
		
		if (response.statusCode==200) {
			method="PUT"
			command="v2/api/application/PCLD/AB3COR/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials/${secret}"
		}else {
			//no existe
			method="POST"
			command="v2/api/application/PCLD/AB3COR/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials"
		}
		
		response=sendRequestToICPApi("${command}",body,method,"AB3COR","",false,false)
		if (response.statusCode==201 || response.statusCode==200) {
			printOpen("SECRET CREATED OR MODIFIED ${method} ICP", EchoLevel.INFO)
			
			response=sendRequestToICPApi("v2/api/application/PCLD_MIGRATED/AB3COR/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials/${secret}",null,"GET","AB3COR","",false,false)
			
			if (response.statusCode==200  && validateSecretOCP(response)) {
				method="PUT"
				command="v2/api/application/PCLD_MIGRATED/AB3COR/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials/${secret}"
			}else {
				//no existe
				method="POST"
				command="v2/api/application/PCLD_MIGRATED/AB3COR/environment/${environment.toUpperCase()}/availabilityzone/ALL/credentials"
			}
			
			response=sendRequestToICPApi("${command}",body,method,"AB3COR","",false,false)
			if (response.statusCode==201 || response.statusCode==200) {
				printOpen("SECRET CREATED OR MODIFIED ${method} OCP", EchoLevel.INFO)
			}else {
				printOpen("ERROR CREATING SECRET", EchoLevel.ERROR)
				throw new Exception("The code ${response.body}")
				
			}
		}else {
			printOpen("ERROR CREATING SECRET", EchoLevel.ERROR)
			throw new Exception("The code ${response.body}")
			
		}
	}
	
	if ((secret.contains("mq") || secret.contains("kafka")) && app!=null) {
		//Consultaremos el componente
		 response1 = sendRequestToAbsis3MS(
			 'GET',
			 "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${tipoApp}/${app}",			 
			 null,
			 "${GlobalVars.CATALOGO_ABSIS3_ENV}",			 
			 [
				 kpiAlmEvent: new KpiAlmEvent(
					 null, null,
					 KpiAlmEventStage.UNDEFINED,
					 KpiAlmEventOperation.CATMSV_HTTP_CALL)
			 ])
		 
		 if (response1.status == 200) {
			 //microOwnerOfTheSecret = readJSON text: response1.content
			 microOwnerOfTheSecret = response1.content
			 printOpen("El micro desplegado es ${microOwnerOfTheSecret.id}", EchoLevel.ALL)
		 }else {
			 throw new Exception("La aplicacion indicada no existe ${tipoApp}.${app}");
		 }
		//Consultamos el elemento
		if (secret.contains("-mq")) {
			printOpen("Contains mq", EchoLevel.ALL)
			microOwnerOfTheSecret.mqConnect=true
		}
		if (secret.contains("-kafka")) {
			printOpen("Contains kafka", EchoLevel.ALL)
			microOwnerOfTheSecret.kafkaConnect=true
			
			// Calculate FastData App ID
			def usernameRegex = /^o([a-z0-9]{1,32})1$/
			def matches = (username =~ usernameRegex)
			if(matches) {
				def fastdataApp = matches[0][1]
				printOpen("Application uses ${fastdataApp} FastData credentials", EchoLevel.ALL)
				microOwnerOfTheSecret.fastdataApp = fastdataApp
			} else {
				throw new Exception("Username does not match ${usernameRegex} pattern");
			}
			
			printOpen("Contains kafka ${microOwnerOfTheSecret}", EchoLevel.ALL)
	    }
		response = sendRequestToAbsis3MS(
			'PUT',
			"${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app",			
			microOwnerOfTheSecret,
			"${GlobalVars.CATALOGO_ABSIS3_ENV}",			
			[
				kpiAlmEvent: new KpiAlmEvent(
					null, null,
					KpiAlmEventStage.UNDEFINED,
					KpiAlmEventOperation.CATMSV_HTTP_CALL)
			])
		
		if (response.status == 200) {
			printOpen("Cataleg actualitzat correctament", EchoLevel.ALL)
		}else {
			printOpen("Error al actualitzar el cataleg", EchoLevel.ALL)
		}
	}
	printOpen("******************************************************************", EchoLevel.ALL)
	printOpen("******************************************************************", EchoLevel.ALL)
	
}
