import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.HttpRequestException
import com.caixabank.absis3.HttpResponseBody
import com.caixabank.absis3.HttpRequestUtilities

def getUrl(url, verb, fileOut, headersList, accept) {
	def headers = ''
	if (headersList.size()>0) {
		headersList.each{
			headers += " -H  '${it.name}:${it.value}'"
		}
	}else {
		headers=''
	}
	
	def urlCurl="curl --write-out '%{http_code}' -o ${fileOut} -s -X ${verb} \"${url}\" -H  accept:${accept} ${headers} "

	return urlCurl
}

def send(Map parameters=[:], EchoLevel echoLevel = EchoLevel.DEBUG){
	HttpResponseBody returnObject=new HttpResponseBody()

    def curl = ""
    String fileOutput = null

	try {
		def url = parameters.url ?: 'VoidUrl'
		def contentType = parameters.contentType ?: 'application/json'
		def proxy = parameters.needsProxy ?: false
		def noproxy = parameters.noproxyOption
		def verb = parameters.verb ?: 'GET'
		def timeout = parameters.timeout ?: 90
		def ignoreSslErrors = parameters.ignoreSslErrors ?: true
		def headers = parameters.headers ?: []
		def validResponsesCode = parameters.validResponsesCodes ?: '200:300'
		def verbose = parameters.verbose ?: 'false'
		def inputData = parameters.inputData ?: null
		def parseResponse = parameters.parseResponse ?: false
		def hideCommand = parameters?.hideCommand ? true : false
		def accept = parameters.accept ?: '*/*'
		def responseStatusCode = null	 
		def jsonInputData = "true"
	
		if (parameters.isJsonInputData!=null) {
			jsonInputData=parameters.isJsonInputData
		}
	
		if (url=='VoidUrl') throw new Exception('La url es obligatoria')

		fileOutput=CopyGlobalLibraryScript('',null,'outputCommandCurl.json', echoLevel)
		def fileCommand = null

		if (hideCommand) {
			curl += "set +x && " 
		} 

		curl += getUrl(url, verb, fileOutput, headers, accept)

		if (inputData!=null) {
			if ("true".equals(jsonInputData)) {
				fileCommand=jsonUtils.writeJsonToString(inputData)
				fileCommand = fileCommand.replace("\'", "'\\\''")
				curl = curl + " -d '${fileCommand}' "
			} else {
				curl = curl + " -d '${inputData}' "
			}
		}

		if (proxy) {
			curl = curl + " --proxy '${env.https_proxy}'"
		}

		if (noproxy) {
			curl = curl + " --noproxy '${noproxy}'"
		}

		if (ignoreSslErrors) {
			curl = curl + " --insecure "
		}
	
		curl = curl + "  -H  'Content-Type: ${contentType}' "
		curl = curl.toString()

		responseStatusCode= sh(script: curl, returnStdout: true)
		int statusCode
		statusCode = responseStatusCode as Integer
		
		int validStatusCodeMin=0
		int validStatusCodeMax=0
		
		def listStatusCodeAllowed=[]
		listStatusCodeAllowed.add(validResponsesCode)
		returnObject.statusCode=statusCode
        def jsonString1 = sh(script: "cat ${fileOutput}", returnStdout:true)
		if (HttpRequestUtilities.responseCodeIsValid(statusCode,listStatusCodeAllowed)) {
			if (parseResponse) {
				returnObject.content=jsonUtils.readJsonFromFile("${fileOutput}", echoLevel)
			}else {
				returnObject.message=jsonString1
			}
		} else {
		    String errorMessage = "Error en la peticion ${curl} El error es de ${statusCode} No esta en el rango de valores adecuados\n" +
                    "Response body:\n\n" +
                    "${HttpRequestUtilities.prettyPrint(jsonString1)}"
            printOpen(errorMessage, EchoLevel.ERROR)
            throw new HttpRequestException(inputData, jsonString1, errorMessage)
		}

	}catch(error) {
	    if(fileOutput) {
    	    def responseBody = sh(script: "cat ${fileOutput}", returnStdout:true)
    	    printOpen("Response body:\n${responseBody}", echoLevel)
    	}
		printOpen("Error en la ejecucion la httpRequest.\nCurl commmand $curl\nError: ${error}", EchoLevel.ERROR)
		throw error
	}

    //printOpen("Request succeeded: Curl Response:\n${returnObject.toString()}", echoLevel)

	return returnObject
	
}
