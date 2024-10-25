import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.PomXmlStructure

def call(PomXmlStructure pomXmlStructure) {

    try {
        if (pomXmlStructure.archVersion == null) {

            def childPomXml = readFile "${WORKSPACE}/$pomXmlStructure.moduleNameWithArchVersionRef/pom.xml"
            printOpen("content of childPomXml.xml: ${childPomXml}", EchoLevel.DEBUG)
            def childProject = new XmlSlurper().parseText(childPomXml)

            if (GlobalVars.ALM_SERVICES_SIMPLIFIED_ALM_WHITELIST.contains(pomXmlStructure.artifactName)) {

                pomXmlStructure.archVersion = GlobalVars.MINIMUM_VERSION_ARCH_PRO
                printOpen("Calculated archVersion is ${GlobalVars.MINIMUM_VERSION_ARCH_PRO} as this component is on the simplified ALM whitelist", EchoLevel.INFO)

            } else {

                pomXmlStructure.archVersion = childProject.parent.version
                printOpen("Calculated archVersion is ${pomXmlStructure.archVersion}", EchoLevel.INFO)

            }

        }
    } catch (err) {
        String errorMessage = err.getMessage()
        printOpen("ERROR: ${errorMessage}", EchoLevel.ERROR)

        def sw = new StringWriter()
        def pw = new PrintWriter(sw)
        err.printStackTrace(pw)
        String stackTrace = sw.toString()
        printOpen("StackTrace: ${stackTrace}", EchoLevel.ERROR)
        throw err
    }

    return null
}

/**
 * Sobreescribe el atributo archVersion del pomXmlStructure, si este está vacio, con la versión del parent (de maven) en el caso de proyectos multimodulo
 * @param pomXmlStructure
 * @param dirPathOverWorkspace String. Path donde se tiene el proyecto descargado
 * @return void
 */
def call(PomXmlStructure pomXmlStructure, String dirPathOverWorkspace) {

    try {
        if (!pomXmlStructure.archVersion || !pomXmlStructure.archVersion.trim()) {
            def currentPath = "${env.WORKSPACE}/${dirPathOverWorkspace}"

            def childPomXml = readFile "${currentPath}/$pomXmlStructure.moduleNameWithArchVersionRef/pom.xml"
            printOpen("content of childPomXml.xml: ${childPomXml}", EchoLevel.DEBUG)
            def childProject = new XmlSlurper().parseText(childPomXml)
            pomXmlStructure.archVersion = childProject.parent.version
            printOpen("Calculated archVersion is ${pomXmlStructure.archVersion}", EchoLevel.INFO)
        }
    } catch (err) {
        String errorMessage = err.getMessage()
        printOpen("ERROR: ${errorMessage}", EchoLevel.ERROR)

        def sw = new StringWriter()
        def pw = new PrintWriter(sw)
        err.printStackTrace(pw)
        String stackTrace = sw.toString()
        printOpen("StackTrace: ${stackTrace}", EchoLevel.ERROR)
        throw err
    }

    return null
}
