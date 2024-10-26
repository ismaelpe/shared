import com.project.alm.EchoLevel
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions

/**
 * Transforma el YML de Litmit a un formato sencillo que puedan usar las apps
 */
def call(translationsMap, params = null) {
    printOpen("Result Application Litmit: $translationsMap", EchoLevel.DEBUG)

    def literals = [:]

    translationsMap.each {item -> 
        item.each { language ->
            language.value.each { channelApp -> 
                def channelAppStrArray = channelApp.key.split("\\.")
                
                def channel = channelAppStrArray[0]
                def subChannel = channelAppStrArray[1]
                def subSubChannel = channelAppStrArray[2]
                
                //  Creamos la estructura de soporte cuando es necesario
                if (channel != "*" && subChannel == "*" && subSubChannel == "*") {
                    literals = createChannelStructure(literals, channel)
                    literals.get("channels").get(channel).get("translations").put(language.key.toString(), channelApp.value)
                }
                
                if (channel != "*" && subChannel != "*" && subSubChannel == "*") {
                    literals = createChannelSubStructure(createChannelStructure(literals, channel), channel, subChannel)
                    literals.get("channels").get(channel).get("channels").get(subChannel).get("translations").put(language.key.toString(), channelApp.value)
                }
                
                if (channel != "*" && subChannel != "*" && subSubChannel != "*" ) {
                    literals = createChannelSubSubStructure(createChannelSubStructure(createChannelStructure(literals, channel), channel, subChannel), channel, subChannel, subSubChannel)
                    literals.get("channels").get(channel).get("channels").get(subChannel).get("channels").get(subSubChannel).get("translations").put(language.key.toString(), channelApp.value)
                }
            }
        }
    }

    def literalsMap = [literals: literals]

    if (params?.writeToDisk) {
        updateYamlFile(params.litmitFile, literalsMap)
    }
    
    return literalsMap
}

def createChannelStructure(literals, channel) {
    if (literals.get("channels") == null) {
        literals.put("channels", [:])
    }
    if (literals.get("channels").get(channel) == null) {
        literals.get("channels").put(channel, [translations: [:]])
    }
    return literals
}

def createChannelSubStructure(literals, channel, subChannel) {
    if (literals.get("channels").get(channel).get("channels") == null) {
        literals.get("channels").get(channel).put("channels", [:])
    }
    if (literals.get("channels").get(channel).get("channels").get(subChannel) == null) {
        literals.get("channels").get(channel).get("channels").put(subChannel, [translations: [:]])
    }	
    return literals
}

def createChannelSubSubStructure(literals, channel, subChannel, subSubChannel) {
    if (literals.get("channels").get(channel).get("channels").get(subChannel).get("channels") == null) {
        literals.get("channels").get(channel).get("channels").get(subChannel).put("channels", [:])
    }
    if (literals.get("channels").get(channel).get("channels").get(subChannel).get("channels").get(subSubChannel) == null) {
        literals.get("channels").get(channel).get("channels").get(subChannel).get("channels").put(subSubChannel, [translations: [:]])
    }
    return literals
}

def createYaml() {
    def options = new DumperOptions()
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
    return new Yaml(options)
}

/**
 * Modifica el fichero YML agregandoleo o modificando la sección de literales.
 */
def updateYamlFile(fileToUpdate, literalsMap) {
    if (!literalsMap.literals.isEmpty()) {
        def yaml = createYaml()

        // Si no existe el fichero lo creamos.
        def rootMap = [:]
        
        def exists = fileExists fileToUpdate
        
        if (exists) {
            // Si existe el fichero lo cargamos
            def yamlString = readFile fileToUpdate
            rootMap = yaml.load(yamlString)
        } 
        
        if (rootMap.containsKey("alm")) {
            def almMap = rootMap.get("alm")
            if (almMap.containsKey("litmid")) {
                almMap.remove("litmid")
            }
            almMap.put("litmid", literalsMap)
        } else {
            rootMap.put("alm", [litmid: literalsMap])
        }

        printOpen("Modification result: $rootMap", EchoLevel.DEBUG)
        writeFile file: fileToUpdate, text: yaml.dump(rootMap)
        sh ("cat $fileToUpdate")
    } else {
        printOpen("No literals to update!!", EchoLevel.DEBUG)
    }
}
