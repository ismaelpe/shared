import groovy.json.JsonSlurperClassic
import com.caixabank.absis3.EchoLevel

// Use pomXmlStructure ifneeded
def call(pipelineData, pomXmlStructure = null) {
    def environment = getEnvironment(pipelineData.bmxStructure.environment)

    printOpen("Build workspace for '$environment'")

    if (pomXmlStructure.contractVersion) {
        if (env.ABSIS3_MULE_PROPERTIES) {
            printOpen("Exists 'contract.version=$pomXmlStructure.contractVersion' in pom.xml, build for '$environment' environment", EchoLevel.INFO)  
       
            def mapMuleParams = new JsonSlurperClassic().parseText(env.ABSIS3_MULE_PROPERTIES)
            def mapMvnBuildMuleParams = mapMuleParams.collectEntries{key, value -> ["-D$key", value]}
            
            // Seteamos el entorno cuando no sea dev y eden
            if (environment != 'dev' && environment != 'eden') {
                mapMvnBuildMuleParams.put("-DmuleEnvironment", environment)
            } else {
                printOpen("For dev or eden 'muleEnvironment' is not used", EchoLevel.INFO) 
            }

            def mapMvnClientMuleParams = [:]
            //mapMvnClientMuleParams.put("-Dcontract.package", pomXmlStructure.contractPackage)            
            mapMvnClientMuleParams.put("-Dcontract.version", pomXmlStructure.contractVersion)

            def muleParams = [ config: mapMvnBuildMuleParams, contract: mapMvnClientMuleParams]

            printOpen("Exists 'env.ABSIS3_MULE_PROPERTIES': $muleParams", EchoLevel.INFO) 
            return muleParams
        } else {
            throw new Exception("Required 'env.ABSIS3_MULE_PROPERTIES', check jenkins env vars!")
        }
    } else {
        return [:]
    }
}

def getEnvironment(String environment) {
    if (environment.equalsIgnoreCase("eden")) {
        return "dev"
    } else {
        return environment.toLowerCase()
    }
}

