import com.project.alm.EchoLevel
import com.project.alm.PipelineData
import groovy.json.JsonSlurper

def call(def pomXmlOrClientInfo, PipelineData pipelineData, String env, String appCode, String featureName, String userEmail) {

    if (notificationToGplApplies()) {

        printOpen("Sending to GPL an AgileWork authFeature validation for ${featureName}", EchoLevel.DEBUG)

        def url = idecuaRoutingUtils.agileWorkUrl()
        def param = prepareRequest(pipelineData, env, appCode, featureName, userEmail)

        def response = sendRequestToGpl('PUT', url, param, null, pipelineData, pomXmlOrClientInfo)

        checkIfAgileWorkPasses(response, featureName)

    } else {
        printOpen("Skipping AgileWork verification as pipeline has not been initiated", EchoLevel.DEBUG)
    }

}

def prepareRequest(PipelineData pipelineData, String env, String appCode, String featureName, String userEmail) {

    String pipelineOrigId = pipelineData.pipelineStructure.pipelineId

    def param = "?pipelineOrigId=${pipelineOrigId}&env=${env}&featureName=${featureName}&userId=${userEmail}"
    if (appCode) {
        param += "&appCode=${appCode}"
    }

    return param
}

def checkIfAgileWorkPasses(def response, String featureName) {

    printOpen("AGILEWORK AUTHFEATURE RESPONSE: " + response, EchoLevel.DEBUG)

    if (response.status == 200) {
        def json = new JsonSlurper().parseText(response.content)
        printOpen("Auth: ${json?.auth}", EchoLevel.ALL)
        printOpen("reasons: ${json?.reasons}", EchoLevel.ALL)

        if (json?.auth) {
            printOpen("AgileWork authFeature has passed (${featureName})!", EchoLevel.DEBUG)
            return
        }

        throw new Exception("AgileWork authFeature has NOT passed (${featureName})!\nMessage was:\n${json.reasons}")

    }

    printOpen("Response content: ${response?.content}", EchoLevel.ALL)
    throw new Exception("Unexpected response when validating AgileWork authFeature (${response.status})!")

}
