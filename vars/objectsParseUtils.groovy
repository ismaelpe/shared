import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.HttpRequestUtilities

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml


def toYamlString(def objectInfo, def cleanAfter = false) {
	def options = new DumperOptions()
	options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
	def yml = new Yaml(options)
	def resultInfo = yml.dump(objectInfo)
	
	if (cleanAfter) {
		resultInfo = resultInfo.replaceAll('\'','')	
	} 
	
	return resultInfo
}

def parseYamlObject(def yamlString) {
	return new Yaml().load(yamlString)	
}

def readJsonFromObject(def jsonObject) {
	//def JsonReturn = sh (returnStdout: true, script: "echo ${jsonObject} | jq ")
	def JsonReturn = HttpRequestUtilities.asObject(jsonObject)
	return JsonReturn
}

def readJsonFromFile(def path, EchoLevel echoLevel = EchoLevel.DEBUG) {
	
	//No tenemos jq
	//def JsonReturn = sh (returnStdout: true, script: "cat ${path} | jq ")
	def JsonReturn = null
	def contentResponse
	try {
		contentResponse = sh(script: "test -e ${path} && echo yes || echo no", returnStdout:true)
		contentResponse=contentResponse.replaceAll("\\n","")
		contentResponse=contentResponse.replaceAll("\\r","")


        printOpen("El fichero $path existe: ${contentResponse}", echoLevel)

		if ("yes".equals(contentResponse)) {
			contentResponse = sh(script: "cat ${path}", returnStdout:true)
            printOpen("El contenido es:\n${contentResponse}", echoLevel)
			JsonReturn=readJsonFromObject(contentResponse)
			sh(script: "rm ${path}", returnStdout:true )
		}else {
			contentResponse = "{}"
		}
	} catch(Exception error) {
        printOpen("curlOutput.json is empty", EchoLevel.ERROR)
		contentResponse = "{}"
	}
	
	
	return JsonReturn
}


def writeJsonToFile(def jsonObject, def path) {
    //sh (returnStdout: false, script: "jq ${jsonObject} > ${path} ")
	def jsonString=" el json ${groovy.json.JsonOutput.toJson(jsonObject)} "
	sh(script: "echo ${jsonString} >  ${path}", returnStdout:false)
}

def writeJsonToString(def jsonObject) {
	//sh (returnStdout: false, script: "jq ${jsonObject} > ${path} ")
	def jsonString="${groovy.json.JsonOutput.toJson(jsonObject)} "
	return jsonString
}
