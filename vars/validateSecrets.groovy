import com.project.alm.GlobalVars
import com.project.alm.ICPDeployStructure
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.EchoLevel

def validate(List secrets, String env, PomXmlStructure pomXml, PipelineData pipelineData) {

    String appName = pomXml.getICPAppName()

    long singleCallDuration
    long wholeCallDuration

    long wholeCallStartMillis = new Date().getTime()
    long singleCallStartMillis

    KpiAlmEvent kpiAlmEvent =
        new KpiAlmEvent(
            pomXml, pipelineData,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.ICP_SECRETS_VALIDATION)

    if (secrets!=null && secrets.size()>0) {
        printOpen("Validating secrets...", EchoLevel.INFO)
        
		for(int i=0;i<secrets.size();i++) {

            String secret=secrets.get(i)

            //Validar los secrets uno a uno contra ICP... necesitamos el namespace del micro
			int numRetry = GlobalVars.ICP_SECRET_VERIFICATION_MAX_RETRIES
			def response = null			

            //Se deben recorrer todos los secrets
			waitUntil(initialRecurrencePeriod: 15000) {

                singleCallStartMillis = new Date().getTime()

                def requestURL = "v2/api/application/PCLD/${appName}/environment/${env.toUpperCase()}/availabilityzone/ALL/credentials/${secret}"
                response = sendRequestToICPApi(
                    requestURL, null, "GET", "${appName}", "", false, false, null, null,
                    [
                        kpiAlmEvent: new KpiAlmEvent(
                            pomXml, pipelineData,
                            KpiAlmEventStage.ICP_SECRETS_VALIDATION,
                            KpiAlmEventOperation.ICPAPI_HTTP_CALL)
                    ])

                long singleCallEndMillis = new Date().getTime()
                singleCallDuration = singleCallEndMillis - singleCallStartMillis

                if (response.statusCode==200) {

                    kpiLogger(kpiAlmEvent.requestSuccess(singleCallDuration))
                    return true

                } else if (response.statusCode==404) {

                    kpiLogger(kpiAlmEvent.requestAppFail(singleCallDuration))

                    long wholeCallEndMillis = new Date().getTime()
                    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                    kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))

                    throw new Exception("${GlobalVars.ICP_ERROR_DEPLOY_NO_INSTANCE_AVAILABLE}    ${secret}")

                } else if (numRetry-- == 0) {

                    kpiLogger(kpiAlmEvent.requestAlmFail(singleCallDuration))

                    long wholeCallEndMillis = new Date().getTime()
                    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                    kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))

                    def url = "${GlobalVars.ICP_PRO}/api/publisher/${requestURL}"
                    createMaximoAndThrow.icpSecretsVerificationFailed(pipelineData, pomXml, secret, env, url, response)

                }

                return false
            }
		}
		printOpen("The secrets have been validated", EchoLevel.INFO)
	}

    long wholeCallEndMillis = new Date().getTime()
    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

    kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))
}

def call(List secrets, String env, PomXmlStructure pomXml, PipelineData pipelineData) {

    validate(secrets, env, pomXml, pipelineData)

}

def call(ICPDeployStructure icpDeployStructure, PomXmlStructure pomXml, PipelineData pipelineData) {

	validate(icpDeployStructure.secrets, icpDeployStructure.envICP, pomXml, pipelineData)
	validate(icpDeployStructure.volumeSecrets, icpDeployStructure.envICP, pomXml, pipelineData)
	
}
