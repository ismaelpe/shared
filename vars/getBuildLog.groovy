import com.project.alm.EchoLevel
import com.project.alm.CloudApiResponse

def call(String app, String appCloud, String buildId) {

	printOpen("Procedemos a consultar los logs de la build ${buildId}", EchoLevel.INFO)

	CloudApiResponse response = sendRequestToCloudApi("v1/api/application/PCLD/${appCloud}/component/${app.toUpperCase()}/build/${buildId}/logs?initialByte=0",null,"GET","${appCloud}","",false,false, null, null, [printResponse: false])

	return response

}
