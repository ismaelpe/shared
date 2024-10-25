import com.project.alm.EchoLevel
import com.project.alm.ICPApiResponse

def call(ICPApiResponse responseFromICP) {
	String icpErrorMessage=""
	try {
		if (responseFromICP!=null && responseFromICP.body!=null) {
			printOpen("Error calling ICP with status code ${responseFromICP.statusCode} and body:\n ${responseFromICP.body}", EchoLevel.ERROR)
			//Tenemos un message error del API de ICP.... tenemos que reportar el error
			icpErrorMessage="${responseFromICP.body}"
			if (icpErrorMessage!=null) {
				//Reducimos la longitud del texto... puede ser enorme
				if (icpErrorMessage.size()>2000) return icpErrorMessage.substring(0,2000) 
				else return icpErrorMessage
			}			
		} else {
			printOpen("Error calling ICP", EchoLevel.ERROR)
		}
	}catch(Exception e) {
		printOpen("Ha dado error la recogida del mensaje de ICP", EchoLevel.ERROR)
	}
    return icpErrorMessage
}
