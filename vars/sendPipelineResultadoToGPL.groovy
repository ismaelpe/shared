import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.PipelineData

def call(boolean initGpl, def pomXmlOrIClientInfo, PipelineData pipelineData, boolean success, boolean notifyDeployment = true) {

    if (initGpl && notificationToGplApplies()) {

        printOpen("Sending to GPL a Pipeline Result operation", EchoLevel.DEBUG)

        def url = idecuaRoutingUtils.pipelineResultUrl(pipelineData.pipelineStructure.pipelineId)
        def body = pipelineData.pipelineStructure.resultPipelineData.getResult(success, this)
        if (pipelineData.isRebaseOfARelease) {
            printOpen("Vaciamos las acciones porque es un update de una release o hotfix", EchoLevel.DEBUG)
            body.acciones = []
            body.disabledActionPolicy = GlobalVars.DISABLED_POLICY_NONE
        }

        //Parseamos el job destino si estamos en subfolder de alm
        if (body.acciones != null) {
            for (def it : body.acciones) {
                if ("LANZAR_JOB".equals(it["tipoAccion"]) && it["destino"]) {
                    printOpen("Paseando la accion con destino " + it["destino"], EchoLevel.DEBUG)
                    String newJobName = pipelineData.getExecutionMode().parseJobName(it["destino"]);
                    it["destino"] = newJobName
                }
            }

        } else {
            printOpen("No hay acciones", EchoLevel.DEBUG)

        }		
		

        sendRequestToGpl('PUT', url, "", body, pipelineData, pomXmlOrIClientInfo)
		
		//Si es un deploy tenemos que llamar al notifyDeployment
        if (notifyDeployment) {
		    sendPipelineNotifyDeploymentToGPL(initGpl, pomXmlOrIClientInfo, pipelineData, success )
        }
		

    } else {

        printOpen("Not send the result to GPL", EchoLevel.DEBUG)

	}
}
