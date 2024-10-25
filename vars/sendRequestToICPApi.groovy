import com.caixabank.absis3.*
import com.cloudbees.groovy.cps.NonCPS
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import hudson.Functions

def getUrlApi(String fileOut,String method, String url, String aplicacionGAR) {
	return "curl -k --write-out %{http_code} -o ${fileOut} -s -X ${method} ${url} -H  accept:*/* -H  application_active:${aplicacionGAR} -H  Content-Type:application/json "
}

/**
 * Decide que hacer ante una excepcion
 * @param messageException Mensaje de la excepcion ocurrida en la peticion
 * @param url Contiene la url que de la peticion 
 * @param method Metodo de la peticion curl
 * @return Enum con los valores para resolver el tratamiento para la respuesta al metodo
 * 0 Devolver el ICPResponse con un id null para continuar con las peticiones
 * 1 Tenemos que devolver una excepcion, intentaremos un retry
 * 2 Este no es recuperable tenemos un error imposible de recuperar
 */
def abortOrRetry(String messageException,String url,String method){

    String result="2"

	if ((messageException.contains("exit code 56") || messageException.contains("exit code 16") || messageException.contains("exit code 52")) && method=="POST" && url.endsWith("deploy")) {
		result="0"
	}else if ((messageException.contains("exit code 56") || messageException.contains("exit code 16")) && method=="POST" && url.endsWith("build")){
		result="1"
	}else if (messageException.contains("exit code 7") || messageException.contains("exit code 2")) { 
		//Este es un error de conexion no ha podido ni hacer la peticion
		result="1"
	}else if (method=="GET") {
		//Este es un error de conexion no ha podido ni hacer la peticion
		result="1"	
	}else {
		result="2"
	}
		
	return result
}

@NonCPS
def validateTimeout(def timeStart) {
	def timeEnd = new Date()
	TimeDuration duration = TimeCategory.minus(timeEnd, timeStart)

    printOpen("${duration} Llevamos  ${duration.getMinutes()} minutos ${duration.getSeconds()} segundos esperando a ICP", EchoLevel.DEBUG)

	if (duration.getMinutes()>GlobalVars.TIMEOUT_ICP) {
        printOpen("TimeOut superado, parece que el timeout de jenkins no ha funcionado. Vamos a abortar", EchoLevel.ERROR)
		throw new Exception("Timeout superado ${GlobalVars.TIMEOUT_ICP}")
	}
}

/**
 * Se llama a la API de ICP indicada por la requestURL y se valida contra el endpoint mediante la pollingRequestUrl en caso que aplique mediante el supportsPolling
 * @param requestURL endpoint del API de ICP para ejecutar la accion
 * @param body objeto con el body para poder ejecutar el POST o PUT
 * @param method con el metodo Http para poder ejecutar contra la API
 * @param aplicacionGAR indicador con la aplicacion GAR asociada al componente a desplegar
 * @param pollingRequestUrl enpoint del API de ICP para poder validar que el endpoint asincrono ha sido ejecutado correctamente
 * @param supportsPolling indica que se tiene que ejecutar la validacion del endpoint para dar por OK el proceso
 * @param statusProcessValidate indica que atributo tienes que validar para validar conntra la url de polling el resultado del estado del proceso
 * @param pipelineData Utilizado para crear máximos. Si es nulo no se intentará crear máximo
 * @param pomXml Utilizado para crear máximos. Si es nulo no se intentará crear máximo
 * @return IcpApiResponse con el resultado del la peticion 
 */
def call(String requestURL, def body, String method, String aplicacionGAR, String pollingRequestUrl,
         boolean supportsPolling, boolean statusProcessValidate,
         PipelineData pipelineData = null, PomXmlStructure pomXml = null, Map parameters = [:], String outputCommand = "outputCommand.json") {
	String fileCommand=""
	ICPApiResponse responseIcp= new ICPApiResponse()	
	String id=""
    String command=""
    String contentResponse=""

    Boolean printResponse = parameters?.printResponse != null && parameters?.printResponse instanceof Boolean ? parameters.printResponse : true

    KpiAlmEvent kpiAlmEvent = parameters?.kpiAlmEvent ?
            parameters?.kpiAlmEvent :
            new KpiAlmEvent(
                pomXml, pipelineData,
                KpiAlmEventStage.UNDEFINED,
                KpiAlmEventOperation.ICPAPI_HTTP_CALL)

    long singleCallDuration
    long wholeCallDuration

    long wholeCallStartMillis
    long singleCallStartMillis

	try {
		singleCallStartMillis = new Date().getTime()
		wholeCallStartMillis = new Date().getTime()
		def toJson = {
			input ->
			groovy.json.JsonOutput.toJson(input)
		}

		if (body!=null) {
			fileCommand=CopyGlobalLibraryScript(toJson(body),null,'fileCommand.json')
		}

		String fileOutput=CopyGlobalLibraryScript('',null,outputCommand)
		int statusCode=0
			
        command=getUrlApi(fileOutput, method, "${GlobalVars.ICP_PRO}/api/publisher/${requestURL}", aplicacionGAR) + " --cert $env.ICP_CERT:$env.ICP_PASS "+" --noproxy '*' "
    
  
        if (body != null) {
            command = command + "-d @${fileCommand}"
			def inputJsonTo=sh(script: "cat ${fileCommand}", returnStdout:true )
			printOpen("Calling API: ${command}", EchoLevel.DEBUG)
			printOpen("With body: ${inputJsonTo}", EchoLevel.DEBUG)
        } else {
        	printOpen("Calling API: ${command}", EchoLevel.DEBUG)
        }

        body = null //Hacemos setting a null para que no tenga que serializar si se hace resuming
        
        def responseStatusCode= null
        
        boolean abortExecution=false
        boolean returnResponse=false
        def errorAbortExecution

        wholeCallStartMillis = new Date().getTime()

        retry(GlobalVars.DEFAULT_RETRY_DELETE_APP_POLICY) {

            try {

                singleCallStartMillis = new Date().getTime()
                responseStatusCode= sh(script: command, returnStdout: true)

                long singleCallEndMillis = new Date().getTime()
                singleCallDuration = singleCallEndMillis - singleCallStartMillis

            } catch(error) {

                long singleCallEndMillis = new Date().getTime()
                singleCallDuration = singleCallEndMillis - singleCallStartMillis

                kpiLogger(kpiAlmEvent.requestFail(singleCallDuration))

                printOpen("Error en la ejecucion al script de la API\n${Functions.printThrowable(error)}", EchoLevel.DEBUG)

                String responseBody = sh(script: "cat ${fileOutput}",returnStdout: true)
                printOpen("Datos respuesta (si están disponibles):\nHTTP status: ${responseStatusCode}\nBody: ${responseBody}", EchoLevel.DEBUG)

                //Define the next step
                def abortRetry=abortOrRetry(error.message,requestURL,method)
                
                if (abortRetry=="0") {

                    def bodyStatus56 = [
                        id: 0
                    ]
                    responseIcp.statusCode=200
                    responseIcp.body=bodyStatus56
                    //Es un POST y ha ejecutado la peticion
                    printOpen("Returning the error in ICP ${toJson(bodyStatus56)}", EchoLevel.DEBUG)

                    returnResponse=true

                    return responseIcp

                } else if (abortRetry=="1") {

                    kpiLogger(kpiAlmEvent.retry())

                    printOpen("Retry:\n${Functions.printThrowable(error)}", EchoLevel.ERROR)
                    throw error

                } else {

                    abortExecution=true
                    errorAbortExecution=error

                } 
            
            }			
        }	
        if (returnResponse) {

            long wholeCallEndMillis = new Date().getTime()
            wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

            kpiLogger(kpiAlmEvent.requestFail(singleCallDuration))
            kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))

            return responseIcp
        }

        if (abortExecution) throw errorAbortExecution 		
        statusCode = responseStatusCode as Integer


		responseIcp.statusCode=statusCode
		contentResponse= sh(script: "cat ${fileOutput}", returnStdout:true )
		
		if (statusCode>=200 && statusCode<300) {
			//RequestOK
			if (contentResponse!=null && contentResponse!="") {

                long wholeCallEndMillis = new Date().getTime()
                wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                kpiLogger(kpiAlmEvent.requestSuccess(singleCallDuration))
                kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))

                printOpen("ICP API Request result:\nStatus Code ${statusCode}\nbody:\n${printResponse ? contentResponse : 'printResponse is false. Log will not be shown'}", EchoLevel.DEBUG)

				def json=readJSON file: fileOutput
				responseIcp.body=json
				id=responseIcp.body.id				
				//Hacemos setting a null para que no tenga que serializar si se hace resuming
				json=null
				if (supportsPolling) responseIcp.body=null

			}

            // Call and Poll are considered as different operations
            kpiAlmEvent.operation(KpiAlmEventOperation.ICPAPI_HTTP_POLL)

			if (supportsPolling) {

				def timeStart = new Date()

				timeout(GlobalVars.TIMEOUT_MAX_ICP) {
					waitUntil(initialRecurrencePeriod: 15000) {
						
						//Controlarem el timeout que sembla que de vegades no funciona
						//el propi de la utilitat de waituntil
						validateTimeout(timeStart)
						//Validar que la duration en minutos no supere el timeout + 1

                        command=getUrlApi(fileOutput,'GET',"${GlobalVars.ICP_PRO}/api/publisher/${pollingRequestUrl}/${id}",aplicacionGAR)+" --cert $env.ICP_CERT:$env.ICP_PASS "

                        wholeCallStartMillis = new Date().getTime()

                        //Probamos con retry
                        retry(GlobalVars.DEFAULT_RETRY_DELETE_APP_POLICY) {

                            try {

                                singleCallStartMillis = new Date().getTime()

                                responseStatusCode = sh(script: command,returnStdout: true)

                                long singleCallEndMillis = new Date().getTime()
                                singleCallDuration = singleCallEndMillis - singleCallStartMillis

                                statusCode = responseStatusCode as Integer

                            } catch(Exception e) {

                                long singleCallEndMillis = new Date().getTime()
                                singleCallDuration = singleCallEndMillis - singleCallStartMillis

                                kpiLogger(kpiAlmEvent.requestFail(singleCallDuration))
                                kpiLogger(kpiAlmEvent.retry())

                                throw e

                            }

                        }

										
						contentResponse= sh(script: "cat ${fileOutput}", returnStdout:true )

                        long wholeCallEndMillis = new Date().getTime()
                        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

						if (statusCode>=200 && statusCode<300) {

							def json1=readJSON file:fileOutput
							
                            kpiLogger(kpiAlmEvent.requestSuccess(singleCallDuration))
                            kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))

                            printOpen("ICP API Polling result:\nStatus Code ${statusCode}\nProcess status ${statusProcessValidate}\nbody:\n${printResponse ? contentResponse : 'printResponse is false. Log will not be shown'}", EchoLevel.DEBUG)

							String resultProcessStatus=null
	
							if (statusProcessValidate) resultProcessStatus=json1.statusProcess
							else resultProcessStatus=json1.status

                            //Dado que la API ha respondido, consideramos las NOK como SUCCESS también
							if (resultProcessStatus=='OK' || resultProcessStatus=='NOK') {

                                responseIcp.body=json1
								 
								 if (resultProcessStatus=='NOK') responseIcp.statusCode=500
								 else responseIcp.statusCode=200
								 
								 return true

							} else {

                                return false

                            }

						} else {

                            printOpen("ICP API Polling result:\nStatus Code ${statusCode}\nProcess status ${statusProcessValidate}\nbody:\n${contentResponse}", EchoLevel.DEBUG)

							responseIcp.statusCode=statusCode
							
							if (contentResponse!=null) {
								def json1=readJSON text: contentResponse
								responseIcp.body=json1
							}
							
							kpiLogger(kpiAlmEvent.requestFail(singleCallDuration))
							kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))

							return true
						}						
						
					}
				}

			} else {
				return responseIcp
			}			
		} else {

            kpiLogger(kpiAlmEvent.requestFail(singleCallDuration))
            kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))

			if (contentResponse!=null && contentResponse!="") {

                printOpen("ICP API Request result:\nStatus Code ${statusCode}\nbody:\n${contentResponse}", EchoLevel.DEBUG)

				def json=readJSON text: contentResponse
				return new ICPApiResponse(statusCode,json)

			} else {

				return new ICPApiResponse(statusCode,null)

			}

		}

	} catch(java.io.NotSerializableException e) {

        long singleCallEndMillis = new Date().getTime()
        singleCallDuration = singleCallEndMillis - singleCallStartMillis
        long wholeCallEndMillis = new Date().getTime()
        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

        kpiLogger(kpiAlmEvent.requestFail(singleCallDuration))
        kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))

		printOpen("Error no serializable... vamos a relanzar" + Functions.printThrowable(e), EchoLevel.ERROR)
		throw e

	} catch(org.jenkinsci.plugins.workflow.steps.FlowInterruptedException fie) {

        long singleCallEndMillis = new Date().getTime()
        singleCallDuration = singleCallEndMillis - singleCallStartMillis
        long wholeCallEndMillis = new Date().getTime()
        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

        kpiLogger(kpiAlmEvent.requestFail(singleCallDuration))
        kpiLogger(kpiAlmEvent.callFail(wholeCallDuration, KpiAlmEventErrorCode.TIMEOUT_BLOCK_EXPIRED))

        String logCommand = command.replaceAll("${env.ICP_CERT}","*****").replaceAll("${env.ICP_PASS}","*****")

        Exception ex = new RuntimeException(
            "La pipeline se ha interrumpido porque se ha alcanzado el timeout sin que se haya podido confirmar que la operación solicitada ha tenido éxito.\n" +
                "El último comando lanzado fue:\n\n${logCommand}\n\n" +
                "La última respuesta recibida del servicio fue:\n\n${contentResponse}", fie)

        printOpen("Error en el sendToICP:\\n${Functions.printThrowable(ex)}", EchoLevel.ERROR)

        if (pipelineData == null || pomXml == null) { throw ex }
        createMaximoAndThrow.icpDeployException(pipelineData, pomXml, ex)

    } catch(Exception e) {

        long singleCallEndMillis = new Date().getTime()
        singleCallDuration = singleCallEndMillis - singleCallStartMillis
        long wholeCallEndMillis = new Date().getTime()
        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

        kpiLogger(kpiAlmEvent.requestFail(singleCallDuration))
        kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))

        printOpen("Error en el sendToICP:\n${Functions.printThrowable(e)}", EchoLevel.ERROR)

        if (pipelineData == null || pomXml == null) { throw e }
        createMaximoAndThrow.icpDeployException(pipelineData, pomXml, e)

	}

	return responseIcp
}

