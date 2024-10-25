import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventErrorCode
import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventStatusType
import com.project.alm.KpiLifeCycleStage
import com.project.alm.KpiLifeCycleStatus
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.KpiData
import com.project.alm.KpiRequestStatus
import com.project.alm.KpiUtilities
import com.project.alm.SonarData
import groovy.json.JsonOutput


def call(PomXmlStructure pomXml, PipelineData pipelineData, KpiLifeCycleStage stage, KpiLifeCycleStatus status) {
    call(pomXml, pipelineData, stage, status, pipelineData.bmxStructure.environment);
}

def call(PomXmlStructure pomXml, PipelineData pipelineData, KpiLifeCycleStage stage, KpiLifeCycleStatus status, String environment) {

    KpiData kpi = new KpiData(pomXml, pipelineData, new SonarData(), stage, status, environment)
    call(kpi)

}

def call(PomXmlStructure pomXml, PipelineData pipelineData,
         KpiAlmEventStage almStage, KpiAlmEventOperation operation, KpiAlmEventStatusType statusType, KpiAlmEventErrorCode errorCode,
         String environment, Integer duration = null) {

    KpiAlmEvent kpi =
        new KpiAlmEvent(pomXml, pipelineData,
            almStage, operation,
            statusType, errorCode,
            environment, duration)
    call(kpi)

}

def call(KpiData[] kpiDataArray) {

    kpiDataArray.each {
        call(it)
    }

}

def call(KpiData kpiData) {

    GlobalVars.KPILOGGER_IS_NOW_ACTIVE = true

    def bodyData = []
    def response

    boolean isKpiAndSendingIsEnabled = GlobalVars.IS_KPI_ENABLED && !(kpiData instanceof KpiAlmEvent)
    boolean isKpiAlmEventAndSendingIsEnabled = GlobalVars.ABSIS3_SERVICES_SEND_ALM_METRICS && kpiData instanceof KpiAlmEvent

    if (isKpiAndSendingIsEnabled || isKpiAlmEventAndSendingIsEnabled) {

        KpiRequestStatus statusKpi = new KpiRequestStatus()

        boolean sonarDataWillBeSent = KpiLifeCycleStage.DEPLOY_FINISHED == kpiData.stage
        boolean weHavePomXmlAndPipelineData = kpiData?.pomXml && kpiData?.pipelineData

        if (sonarDataWillBeSent && weHavePomXmlAndPipelineData) {
            kpiData.sonarData = getInfoSonarKPI(kpiData?.pomXml, kpiData?.pipelineData)
        }

		withCredentials([usernamePassword(credentialsId: 'ALM_LOGCOLLECTOR_CREDENTIALS', passwordVariable: 'ALM_LOGCOLLECTOR_PASSWORD', usernameVariable: 'ALM_LOGCOLLECTOR_USERNAME')]) {
			timeout(time: GlobalVars.DEFAULT_KPI_REQUEST_RETRIES_TIMEOUT, unit: 'SECONDS') {
				waitUntil(initialRecurrencePeriod: 15000) {
	
					try {
	
						printOpen("Iteration ${statusKpi.iteration} at date ${new Date()}", EchoLevel.DEBUG)
	
						printOpen("Retrieving KPI data", EchoLevel.ALL)
	
						if (sonarDataWillBeSent) {
							printOpen("Sending sonar data: " + kpiData.sonarData.toString(), EchoLevel.DEBUG)
						}
	
						bodyData.push(kpiData.retrieveData("${env.BUILD_TAG}"))
	
						def collectorUrl = isKpiAlmEventAndSendingIsEnabled ? GlobalVars.URL_ALMMETRICS : GlobalVars.URL_KPI
			
						def customHeaders =
							isKpiAlmEventAndSendingIsEnabled ?
								[[name: 'Authorization', value: "Basic ${ALM_LOGCOLLECTOR_PASSWORD}"]] : []
						def body = JsonOutput.toJson(bodyData)
	
						printOpen("sending KPI data: " + kpiData.toString(), EchoLevel.ALL)
						
						def urlParameters=[:]
						urlParameters.needsProxy=true
						urlParameters.url=collectorUrl
						urlParameters.parseResponse=false
						urlParameters.inputData=bodyData
						urlParameters.verb='POST'
						urlParameters.ignoreSslErrors=false
						urlParameters.timeout=GlobalVars.KPI_TIMEOUT
						urlParameters.validResponsesCodes="${GlobalVars.KPI_VALID_RESPONSE_STATUS}"
						urlParameters.headers=customHeaders
												
						response=httpRequestUtils.send(urlParameters, EchoLevel.ALL)
						/*
						response =
							httpRequest consoleLogResponseBody: true,
								contentType: 'APPLICATION_JSON',
								httpMode: 'POST',
								customHeaders: customHeaders,
								requestBody: body,
								url: collectorUrl,
								validResponseCodes: "${GlobalVars.KPI_VALID_RESPONSE_STATUS}",
								httpProxy: "${env.https_proxy}",
								timeout: GlobalVars.KPI_TIMEOUT*/
	
						printOpen("[ELK KPI] Response to Sending kpi to ELK with ${response.toString()}", EchoLevel.ALL)

						GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false
	
						return KpiUtilities.evaluateResponse(response, statusKpi)
	
					} catch (e) {
	
						printOpen("[ELK KPI] error : ${e}", EchoLevel.ERROR)
						boolean shallWeRetry = KpiUtilities.evaluateResponse(response, statusKpi)
	
						if (!shallWeRetry && weHavePomXmlAndPipelineData) {
	
							createMaximoAndThrow.kpiLoggerRequestFailureButDoNotThrowException(kpiData, e)
	
						}

						GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

						return shallWeRetry
					}
				}
			}
		}

        if (response == null) printOpen("No response from ELK", EchoLevel.DEBUG)
        else {
            if (response.status == 200) {

				GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false
				
                return response

            } else {

                if (weHavePomXmlAndPipelineData) {
                    createMaximoAndThrow.kpiLoggerRequestFailureButDoNotThrowException(kpiData, response)					

                }

            }
        }
    } else {
        printOpen("KPI Logger is disabled!!!", EchoLevel.INFO)
    }

    GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

}

def call(KpiLifeCycleStage stage, KpiLifeCycleStatus status, String type, String name) {

    GlobalVars.KPILOGGER_IS_NOW_ACTIVE = true

    def bodyData = []
    def response
    KpiRequestStatus statusKpi = new KpiRequestStatus()
    def fecha

    if (env.IS_KPI_ENABLED != null) {

        timeout(GlobalVars.DEFAULT_KPI_REQUEST_RETRIES_TIMEOUT) {
            waitUntil(initialRecurrencePeriod: 15000) {
                try {

                    fecha = new Date()
                    printOpen("Iteration ${statusKpi.iteration} at date ${fecha}", EchoLevel.DEBUG)

                    KpiData kpi = new KpiData(type, name, stage, status);
                    bodyData.push(kpi.retrieveData())

                    printOpen("sending KPI data: " + kpi.toString(), EchoLevel.ALL)
                    
					def urlParameters=[:]
					urlParameters.needsProxy=true
					urlParameters.url=GlobalVars.URL_KPI
					urlParameters.parseResponse=false
					urlParameters.inputData=bodyData
					urlParameters.verb='POST'
					urlParameters.ignoreSslErrors=false
					urlParameters.timeout=GlobalVars.KPI_TIMEOUT
					urlParameters.validResponsesCodes="${GlobalVars.KPI_VALID_RESPONSE_STATUS}"
					urlParameters.headers=[]
											
					response=httpRequestUtils.send(urlParameters, EchoLevel.DEBUG)
					/*
					response = httpRequest consoleLogResponseBody: true, 
					contentType: 'APPLICATION_JSON', 
					httpMode: 'POST', 
					requestBody: JsonOutput.toJson(bodyData), 
					url: GlobalVars.URL_KPI, 
					validResponseCodes: "${GlobalVars.KPI_VALID_RESPONSE_STATUS}", 
					httpProxy: "${env.https_proxy}", 
					timeout: GlobalVars.KPI_TIMEOUT*/
                    					
					printOpen("[ELK KPI] Response to Sending kpi to ELK with ${response}", EchoLevel.ALL)
					
                    return KpiUtilities.evaluateResponse(response, statusKpi)
                } catch (e) {
                    printOpen("[ELK KPI] error : ${e}", EchoLevel.ERROR)
                    return KpiUtilities.evaluateResponse(response, statusKpi)
                }
            }
        }

        if (response == null) printOpen("No response from ELK", EchoLevel.ERROR)
        else {
            if (response.status == 200) {
                return response
            } else printOpen("Unexpected response when sending request to ELK (${response.status})!", EchoLevel.ERROR)
        }
    } else {
        printOpen("KPI Logger is disabled!!!", EchoLevel.INFO)
    }

    GlobalVars.KPILOGGER_IS_NOW_ACTIVE = false

}
