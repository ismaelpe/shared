import com.project.alm.*
import static groovy.io.FileType.FILES
import groovy.json.JsonSlurper
import java.util.Map.Entry

/**
 * Lee todas las traducciones desde un directorio
 */
def call(String dirname, String domain, String component, String componentType) {
	printOpen("Reading dir ${dirname}", EchoLevel.ALL)
	
	def errorTranslationsMap = []
	
	def files = findFiles(glob: "${dirname}/*.json")	
	files.each { file -> 
		def fileErrorTranslationMap = readLitmidFile("${file}", domain, component, componentType, "error")
		errorTranslationsMap.addAll(fileErrorTranslationMap)
	}
	
	return [translations: errorTranslationsMap]
}

/**
 * Lee un fichero de traducciones de litmid
 * @param file
 * @return
 */
def readLitmidFile(String fileName, String domain, String component, String componentType, String tag) {
	def errorTranslationMap = []
	
	def idAplicacionStr = "idAplicacion"
	def tipoAplicacionStr = "tipoAplicacion"
	def fileContent = readFile fileName
	
	def translateFile = new JsonSlurper().parseText(fileContent)
	
	// EL contenido del fichero se debe corresponder con la aplicacion
	if (translateFile[idAplicacionStr] == component && translateFile[tipoAplicacionStr] == componentType) {
		translateFile.findAll{
			it.key != idAplicacionStr && it.key != tipoAplicacionStr
		}.each {
			errorTranslationMap.addAll(getLiteralsFromLanguage(it, domain, tag))
		}
		printOpen("File ${fileName} readed Ok", EchoLevel.ALL)
	} else {
		printOpen("The file ${fileName} has some incoherences check 'idAplication' and 'tipoAplicacion'", EchoLevel.ALL)
	}
	
	return errorTranslationMap
}

/**
 * Obtiene todos los literales de un mismo tag error, etc
 * @param tag
 * @param languageEntry
 * @return
 */
def getLiteralsFromLanguage(Entry languageEntry, String domain, String tag) {
	def errorManagementResultMap = []
	def language = languageEntry.key
	def channelsError = languageEntry.value[tag]
	
	channelsError.each{ channelError -> 
		def channelsErrorStrArray = channelError.key.split("\\.")
		
		channelError.value.each { elementError -> 
			def translation = [
				type: "${domain}/${elementError.key}",
				language: language,				
				titleMessage: elementError.value,
				detailMessage: elementError.value,
				channel: channelsErrorStrArray[0]
			]
		
			if (channelsErrorStrArray.size() >= 2 && channelsErrorStrArray[1] != "*") {
				translation['subChannel'] = channelsErrorStrArray[1]
			}
			
			if (channelsErrorStrArray.size() == 3 && channelsErrorStrArray[1] != "*") {
				translation['subSubChannel'] = channelsErrorStrArray[2]  
			}
			
			errorManagementResultMap.add(translation)
		}
	}	
	return errorManagementResultMap
}