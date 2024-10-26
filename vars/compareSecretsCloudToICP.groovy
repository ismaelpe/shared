import com.project.alm.*
import java.util.List
import java.util.ArrayList
import java.util.Map
import java.util.HashMap



def getSecretFromEnvNamespace(String env, String namespace) {
	Map secrets=new HashMap()
	
	def response=sendRequestToCloudApi("v2/api/application/PCLD/${namespace}/environment/${env.toUpperCase()}/availabilityzone/ALL/credentials",null,"GET","${namespace}","",false,false)
	
	if (response.statusCode==200) {
		response.body.each{
			String idCenter = "${it.az}"
			Map centerSecrets = new HashMap()
			secrets.put(idCenter,centerSecrets)
			
			it.secrets.each { 
				centerSecrets.put("${it.name}", true)					
			}					
		}
	}
	
	return secrets
}

def call(String envOrigin, String envDestiny) {
	
	Map ab3corOrigin=getSecretFromEnvNamespace(envOrigin, "AB3COR")
	Map ab3corDestiny=getSecretFromEnvNamespace(envDestiny, "AB3COR")
	Map ab3appOrigin=getSecretFromEnvNamespace(envOrigin, "AB3APP")
	Map ab3appDestiny=getSecretFromEnvNamespace(envDestiny, "AB3APP")
	printOpen("============================================", EchoLevel.ALL)
	printOpen(" ${envOrigin} vs ${envDestiny}", EchoLevel.ALL)
	printOpen("============================================", EchoLevel.ALL)

	
	printSecrets(compareSecrets(ab3corOrigin,ab3corDestiny),compareSecrets(ab3appOrigin,ab3appDestiny),envOrigin, envDestiny)
	
}


def printSecrets(Map ab3cor, Map ab3app,String envOrigin, String envDestiny) {
	
	String value=""
	
	printOpen("===========================================================", EchoLevel.ALL)
	printOpen("            Secrets namespace ARQ							 ", EchoLevel.ALL)
	printOpen("===========================================================", EchoLevel.ALL)
	printOpen(" AZ1 ", EchoLevel.ALL)
	printOpen("EQUALS:", EchoLevel.ALL)
	ab3cor.get("AZ1_Same").keySet().each{
		value=value+"${it},"
	}
	printOpen("${value}", EchoLevel.ALL)
	value=""
	
	printOpen("-----------------------------------------------------------", EchoLevel.ALL)
	printOpen("DIFFERENT: NOT IN ${envDestiny}", EchoLevel.ALL)
	ab3cor.get("AZ1_Different_Origin").keySet().each{
		value=value+"${it},"
	}
	printOpen("${value}", EchoLevel.ALL)
	value=""
	printOpen("DIFFERENT: NOT IN ${envOrigin}", EchoLevel.ALL)
	ab3cor.get("AZ1_Different_Destiny").keySet().each{
		value=value+"${it},"
	}
	printOpen("${value}", EchoLevel.ALL)
	value=""
	printOpen("-----------------------------------------------------------", EchoLevel.ALL)
	printOpen(" AZ2 ", EchoLevel.ALL)
	printOpen("EQUALS:", EchoLevel.ALL)
	ab3cor.get("AZ2_Same").keySet().each{
		value=value+"${it},"
	}
	printOpen("${value}", EchoLevel.ALL)
	value=""
	printOpen("-----------------------------------------------------------", EchoLevel.ALL)
	printOpen("DIFFERENT: NOT IN ${envDestiny}", EchoLevel.ALL)
	ab3cor.get("AZ2_Different_Origin").keySet().each{
		value=value+"${it},"
	}
	printOpen("${value}", EchoLevel.ALL)
	value=""
	printOpen("DIFFERENT: NOT IN ${envOrigin}", EchoLevel.ALL)
	ab3cor.get("AZ2_Different_Destiny").keySet().each{
		value=value+"${it},"
	}
	printOpen("${value}", EchoLevel.ALL)
	value=""
	printOpen("-----------------------------------------------------------", EchoLevel.ALL)
	printOpen("===========================================================", EchoLevel.ALL)
	printOpen("            Secrets namespace APP							 ", EchoLevel.ALL)
	printOpen("===========================================================", EchoLevel.ALL)
	printOpen(" AZ1 ", EchoLevel.ALL)
	printOpen("   EQUALS:", EchoLevel.ALL)
	ab3app.get("AZ1_Same").keySet().each{
		value=value+"${it},"
	}
	printOpen("${value}", EchoLevel.ALL)
	value=""
	printOpen("-----------------------------------------------------------", EchoLevel.ALL)
	printOpen("   DIFFERENT: NOT IN ${envDestiny}", EchoLevel.ALL)
	ab3app.get("AZ1_Different_Origin").keySet().each{
		value=value+"${it},"
	}
	printOpen("${value}", EchoLevel.ALL)
	value=""
	printOpen("DIFFERENT: NOT IN ${envOrigin}", EchoLevel.ALL)
	ab3app.get("AZ1_Different_Destiny").keySet().each{
		value=value+"${it},"
	}
	printOpen("${value}", EchoLevel.ALL)
	value=""
	printOpen("-----------------------------------------------------------", EchoLevel.ALL)
	printOpen(" AZ2 ", EchoLevel.ALL)
	printOpen("   EQUALS:", EchoLevel.ALL)
	ab3app.get("AZ2_Same").keySet().each{
		value=value+"${it},"
	}
	printOpen("${value}", EchoLevel.ALL)
	value=""
	printOpen("-----------------------------------------------------------", EchoLevel.ALL)
	printOpen("   DIFFERENT: NOT IN ${envDestiny}", EchoLevel.ALL)
	ab3app.get("AZ2_Different_Origin").keySet().each{
		value=value+"${it},"
	}
	printOpen("${value}", EchoLevel.ALL)
	value=""
	printOpen("   DIFFERENT: NOT IN ${envOrigin}", EchoLevel.ALL)
	ab3app.get("AZ2_Different_Destiny").keySet().each{
		value=value+"${it},"
	}
	printOpen("${value}", EchoLevel.ALL)
	value=""
	printOpen("-----------------------------------------------------------", EchoLevel.ALL)
}


def compareSecrets(Map secretsOrigin,Map secretsDestiny) {

	
	Map theResultCenter = new HashMap()
	
	Map theSame=new HashMap()
	Map theDiferent=new HashMap()
	Map theDiferentD=new HashMap()
	
	Map secretsO=secretsOrigin["AZ1"]
	Map secretsD=secretsDestiny["AZ1"]
	
	secretsO.each{
		k,v -> 
			if (secretsD.containsKey(k)) theSame.put(k, true)
			else theDiferent.put(k, true)
	}
	
	secretsD.each{
		k,v ->
			if (!secretsO.containsKey(k)) theDiferentD.put(k, true)
	}
	
	theResultCenter.put("AZ1_Same",theSame)
	theResultCenter.put("AZ1_Different_Origin",theDiferent)
	theResultCenter.put("AZ1_Different_Destiny",theDiferentD)
	
	secretsO=secretsOrigin["AZ2"]
	secretsD=secretsDestiny["AZ2"]
	
	
	theSame=new HashMap()
	theDiferent=new HashMap()
	theDiferentD=new HashMap()
	
	secretsO.each{
		k,v ->
			if (secretsD.containsKey(k)) theSame.put(k, true)
			else theDiferent.put(k, true)
	}
	secretsD.each{
		k,v ->
			if (!secretsO.containsKey(k)) theDiferentD.put(k, true)
	}
	
	theResultCenter.put("AZ2_Same",theSame)
	theResultCenter.put("AZ2_Different_Origin",theDiferent)
	theResultCenter.put("AZ2_Different_Destiny",theDiferentD)
	
	return theResultCenter
}