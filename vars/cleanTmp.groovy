import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars


/**
 * Eliminamos todos los contracts swagger que existen en tmp, fruto de la ejecucion del plugin de mule
 */
def call() {
    String tmp = System.getProperty('java.io.tmpdir')
    def contractsTemporal = "${tmp}/remote-swagger-micro-*.yaml"
    sh "rm -rf $contractsTemporal"
    printOpen("All files $contractsTemporal were deleted", EchoLevel.DEBUG)
}

/**
 * El directorio tmp no se puede vaciar totalmente ya que es usado por el jenkins de forma interna. 
 * Solo elimino el contenido que se cual es
 * @param sanitizedTmpDir String con el directorio	
 * @return no devuelve nada
 */
def call(String sanitizedTmpDir) {
    printOpen("Cleaning temporal folder ${sanitizedTmpDir}", EchoLevel.DEBUG)
    sh "rm -rf ${sanitizedTmpDir}/src"
    sh "rm -rf ${sanitizedTmpDir}/target"
    sh "rm -rf ${sanitizedTmpDir}/*.xml"
    sh "rm -rf ${sanitizedTmpDir}/*.yml"
	sh "rm -rf ${sanitizedTmpDir}/*.json"
    sh "rm -rf ${sanitizedTmpDir}/pipelineLogs/*.log"
    printOpen("Temporal folder ${sanitizedTmpDir} is clean", EchoLevel.DEBUG)

    String dirContent = sh(script: "ls -la ${sanitizedTmpDir}", returnStdout: true)
    printOpen("Directory content:\n${dirContent}", EchoLevel.DEBUG)
}
