import com.caixabank.absis3.*
import static groovy.io.FileType.FILES
import groovy.json.JsonSlurper

/**
 * Lee todas las traducciones desde un directorio
 */
def call(dirname, artifactName, typeApp) {
    return [ 
             error      : findFiles(glob: "$dirname/*.json").collect { file -> readLitmidFile(file, artifactName, typeApp, LitmidLiteralType.ERROR) },
             aplicativo : findFiles(glob: "$dirname/*.json").collect { file -> readLitmidFile(file, artifactName, typeApp, LitmidLiteralType.APPLICATION) }
    ]
}

/**
 * Lee un fichero de traducciones de litmid y devuelve los dato de:
 * - aplictivo
 * - error
 * @param file
 * @return
 */
def readLitmidFile(file, artifactName, typeApp, literalType) {
    def idAplicacionStr = "idAplicacion"
    def tipoAplicacionStr = "tipoAplicacion"
    def translateFile = new JsonSlurper().parseText(readFile(file.path))

    // EL contenido del fichero se debe corresponder con la aplicacion
    if (translateFile[idAplicacionStr] == artifactName && translateFile[tipoAplicacionStr] == typeApp) {
        def language = file.name.split("\\.")[0]
        return ["$language": translateFile[language][literalType.literalType()]]
    } else {
        return []
    }
}
