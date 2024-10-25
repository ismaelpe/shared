import com.project.alm.EchoLevel
import groovy.json.JsonOutput

def call(translationsMap, params = null) {

    printOpen("Result Error Litmit: $translationsMap", EchoLevel.DEBUG)

    // Aqui tenemos que transformar el tranlationsMap al formato del error-management
    def errorManagementResultMap = []

    // Obtenemos todos los lenguajes que se han obtenido desde litmit y los traducimos al formato del error-management
    translationsMap.each {item -> 
        item.each { language -> 
            language.value.each { channelError -> 
                def channelsErrorStrArray = channelError.key.split("\\.")
                channelError.value.each { elementError -> 
                    def translation = [
                        type: "$params.domain/$elementError.key",
                        language: language.key,
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
        }
    }

    if (errorManagementResultMap) {
        def json = JsonOutput.prettyPrint(JsonOutput.toJson([translations: errorManagementResultMap]))

        writeFile file: params.sourceFile, text: json, encoding: "UTF-8"

        printOpen("$params.sourceFile overrided!", EchoLevel.INFO)
        printOpen("$json", EchoLevel.DEBUG)
    } else {
        printOpen("$params.sourceFile not overrided!", EchoLevel.DEBUG)
    }
   
}
