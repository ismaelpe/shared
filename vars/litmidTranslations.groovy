import com.project.alm.*
import static groovy.io.FileType.FILES
import groovy.json.JsonSlurper

def call(def pomXmlStructure, PipelineData pipelineData) {
    def component = pomXmlStructure.artifactName
    def componentType = pipelineData.garArtifactType.getGarName()
    def domain = pipelineData.domain
    def sourceFile = publishErrorManagementTranslations.getSourcePath(pomXmlStructure, GlobalVars.JSON_ERROR_MANAGEMENT_PUT)
    
    // Obtenemos todos las traducciones tanto de error como literales
    printOpen("Downloading LITMID errors and translations...", EchoLevel.INFO)
    def adaptedLitmidResponse = sendLitmidRequest(component, componentType)

    // Errors
    def erroresLitmidMap   = adaptedLitmidResponse[LitmidLiteralType.ERROR.literalType()]
    
    printOpen("LITMID errors and translations have been downloaded.", EchoLevel.INFO)
    
    // Actualizamos los errores en el errormanagement-micro
    // Este paso lo que hará es sustituir/crear el fichero errormanagement-put con el contenido anteriormente
    // descargado
    litmidErrorsLiteralsManagement(erroresLitmidMap, [domain: domain, sourceFile: sourceFile])

    printOpen("Uploading error translations to errormanagement-micro...", EchoLevel.INFO)

    publishErrorManagementTranslations(pipelineData,pomXmlStructure)
    
    printOpen("Error translations have been uploaded to errormanagement-micro", EchoLevel.INFO)
    
    printOpen("Uploading application literals to config-server...", EchoLevel.INFO)
    
    // Literals
    def literalesLitmidMap = adaptedLitmidResponse[LitmidLiteralType.APPLICATION.literalType()]

    // Actualizamos los literales en el fichero application-cloud.yaml
    // Este paso lo que hará es sustituir/crear el fichero src/resources/application-cloud.yaml con el contenido anteriormente
    // descargado   
    def literalsMap = litmidApplicationLiteralsManagement(literalesLitmidMap, [writeToDisk: true, litmitFile: './src/main/resources/application-litmid.yml'])
    def hasLiterals = !literalsMap.literals.isEmpty()
    if (hasLiterals) {
        printOpen("Application literals have been updated to config-server", EchoLevel.INFO)
    } else {
        printOpen("No application literals to update to config-server", EchoLevel.INFO) 
    }

    return hasLiterals
}
