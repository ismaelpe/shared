import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.PipelineData
import com.project.alm.Strings


def call(def pomXmlOrIClientInfo, PipelineData pipelineData, String stageId) {
    call(pomXmlOrIClientInfo, pipelineData, stageId, null, null, null)
}

def call(def pomXmlOrIClientInfo, PipelineData pipelineData, String stageId, String log) {
    call(pomXmlOrIClientInfo, pipelineData, stageId, log, null, null)
}

def call(def pomXmlOrIClientInfo, PipelineData pipelineData, String stageId, String log, String environment) {
    call(pomXmlOrIClientInfo, pipelineData, stageId, log, environment, null)
}

def call(PipelineData pipelineData, String garAppType, String garAppName, String stageId) {
    call(pipelineData, garAppType, garAppName, stageId, null, null, null)
}

def call(PipelineData pipelineData, String garAppType, String garAppName, String stageId, String log) {
    call(pipelineData, garAppType, garAppName, stageId, log, null, null)
}

def call(PipelineData pipelineData, String garAppType, String garAppName, String stageId, String log, String environment) {
    call(pipelineData, garAppType, garAppName, stageId, log, environment, null)
}

def call(def pomXmlOrIClientInfo, PipelineData pipelineData, String stageId, String log, String environment, String state) {

    if (notificationToGplApplies()) {
        stageId = pipelineData?.pipelineStructure?.pipelineId+stageId

        printOpen("Sending to GPL a Pipeline Stage End Operation: ${stageId}", EchoLevel.DEBUG)

        def url = idecuaRoutingUtils.stageUrl(stageId)
        def estado = "ended"

        if (state != null) estado = state

        def body = [
            id        : stageId,
            estado    : estado,
            fechaFinal: new Date(),
        ]
        

        if (log) {
            if(env.sendLogsToGpl) {
                log ="############ Main Log ############<br><pre>"+log+"</pre>"
                log += Strings.toHtml("\n\n"+
                                        "############ Complete Logs ############\n"+
                                        GlobalVars.STAGE_LOGS)
            } else {
                log="<pre>"+log+"</pre>"
            }
            body.put("log", prepareLogBeforeSendingIt(log))
        } else {
            if(env.sendLogsToGpl) {
                log = Strings.toHtml(GlobalVars.STAGE_LOGS)
                body.put("log", prepareLogBeforeSendingIt(log))
            }
        }

        if (environment) {
            body.put("entorno", environment.toUpperCase())
        }
    
        try {
            def response = sendRequestToGpl('PUT', url, "", body, pipelineData, pomXmlOrIClientInfo)
    
            return response
        } catch (Exception e) {
            body.remove("log")
            def response = sendRequestToGpl('PUT', url, "", body, pipelineData, pomXmlOrIClientInfo)
            return response
        }
    }
}

def call(PipelineData pipelineData, String garAppType, String garAppName, String stageId, String log, String environment, String state) {

    if (notificationToGplApplies()) {
        stageId = pipelineData?.pipelineStructure?.pipelineId+stageId

        printOpen("Sending to GPL a Pipeline Stage End Operation: ${stageId}", EchoLevel.DEBUG)

        def url = idecuaRoutingUtils.stageUrl(stageId)
        def estado = "ended"

        if (state != null) estado = state

        def body = [
            id        : stageId,
            estado    : estado,
            fechaFinal: new Date(),
        ]

        if (log) {
            if(env.sendLogsToGpl) {
                log ="############ Main Log ############<br><pre>"+log+"</pre>"
                log += Strings.toHtml("\n\n"+
                                        "############ Complete Logs ############\n"+
                                        GlobalVars.STAGE_LOGS)
            } else {
                log="<pre>"+log+"</pre>"
            }
            body.put("log", prepareLogBeforeSendingIt(log))
        } else {
            if(env.sendLogsToGpl) {
                log = Strings.toHtml(GlobalVars.STAGE_LOGS)
                body.put("log", prepareLogBeforeSendingIt(log))
            }
        }

        if (environment) {
            body.put("entorno", environment.toUpperCase())
        }
        
        try {
            def response = sendRequestToGpl('PUT', url, "", body, garAppName, garAppType)
    
            return response
        } catch (Exception e) {
            body.remove("log")
            def response = sendRequestToGpl('PUT', url, "", body, garAppName, garAppType)
            return response
        }
    }
}

String prepareLogBeforeSendingIt(String log) {
    if (log.length() > GlobalVars.DEVELOPER_PORTAL_LOG_MAX_SIZE) {
        return log.substring(0, GlobalVars.DEVELOPER_PORTAL_LOG_MAX_SIZE) + "...<br>... this log is too long ..."
    } else {
        return log
    }
    
}