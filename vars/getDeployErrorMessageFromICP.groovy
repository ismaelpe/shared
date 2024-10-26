import com.project.alm.EchoLevel
import com.project.alm.CloudApiResponse

def call(CloudApiResponse responseFromCloud) {
	String cloudErrorMessage=""
	try {
		if (responseFromCloud!=null && responseFromCloud.body!=null) {
			printOpen("Error calling Cloud with status code ${responseFromCloud.statusCode} and body:\n ${responseFromCloud.body}", EchoLevel.ERROR)
			//Tenemos un message error del API de Cloud.... tenemos que reportar el error
			cloudErrorMessage="${responseFromCloud.body}"
			if (cloudErrorMessage!=null) {
				//Reducimos la longitud del texto... puede ser enorme
				if (cloudErrorMessage.size()>2000) return cloudErrorMessage.substring(0,2000) 
				else return cloudErrorMessage
			}			
		} else {
			printOpen("Error calling Cloud", EchoLevel.ERROR)
		}
	}catch(Exception e) {
		printOpen("Ha dado error la recogida del mensaje de Cloud", EchoLevel.ERROR)
	}
    return cloudErrorMessage
}
