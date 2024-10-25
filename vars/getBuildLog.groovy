import com.project.alm.EchoLevel
import com.project.alm.ICPApiResponse

def call(String app, String appICP, String buildId) {

	printOpen("Procedemos a consultar los logs de la build ${buildId}", EchoLevel.INFO)

	ICPApiResponse response = sendRequestToICPApi("v1/api/application/PCLD/${appICP}/component/${app.toUpperCase()}/build/${buildId}/logs?initialByte=0",null,"GET","${appICP}","",false,false, null, null, [printResponse: false])

	return response

}
