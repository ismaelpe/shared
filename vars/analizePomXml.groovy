import com.project.alm.EchoLevel
import com.project.alm.PomXmlStructure
import com.project.alm.ArtifactType
import com.project.alm.ArtifactSubType

def call(ArtifactType artifactType, ArtifactSubType artifactSubtype) {
	call(artifactType.name(), artifactSubtype.name())
}

def call(String artifactType, String artifactSubtype) {
    PomXmlStructure pomXmlStructure = new PomXmlStructure()

    try {
        def pomXml = readFile "${WORKSPACE}/pom.xml"

        def project = new XmlSlurper().parseText(pomXml)
	    def resultat = pomXmlStructure.initXmlStructure(artifactType, artifactSubtype, project)

    } catch (err) {
        printOpen("ERROR", EchoLevel.ALL)
        printOpen(err.getMessage(), EchoLevel.ERROR)

        def sw = new StringWriter()
        def pw = new PrintWriter(sw)
        err.printStackTrace(pw)
        printOpen(sw.toString(), EchoLevel.ERROR)
        throw err
    }

    return pomXmlStructure
}

/**
 * Devuelve el PomXmlStructure, pero pasandole adicionalmente el subdirectorio sobre el que está descargado el gitRepo
 * @param artifactType
 * @param artifactSubtype
 * @param dirPathOverWorkspace Subdirectorio sobre el Workspace donde estará el codigo git clonado
 * @return PomXmlStructure
 */
def call(String artifactType, String artifactSubtype, String dirPathOverWorkspace) {
    PomXmlStructure pomXmlStructure = new PomXmlStructure()
    String repoCurrentPath

    try {
        repoCurrentPath = "${env.WORKSPACE}/${dirPathOverWorkspace}"
        printOpen("parsing root pom.xml: ${repoCurrentPath}/pom.xml", EchoLevel.ALL)
        def pomXml = readFile "${repoCurrentPath}/pom.xml"
        printOpen("content of pom.xml: ${pomXml}", EchoLevel.ALL)

        def project = new XmlSlurper().parseText(pomXml)
        if (!artifactType && project.modules.size()) {
            // detectando si tiene modulos el pom, y si no está previamente seteado, seteamos a agregador
            artifactType = "AGREGADOR"
        }
        def result = pomXmlStructure.initXmlStructure(artifactType, artifactSubtype, project)

        printOpen(" EL VALOR ES DE ", EchoLevel.ALL)
        printOpen(" La property contract.package es: $pomXmlStructure.contractPackage", EchoLevel.ALL)

    } catch (err) {
        printOpen("ERROR", EchoLevel.ERROR)
        printOpen(err.getMessage(), EchoLevel.ERROR)

        def sw = new StringWriter()
        def pw = new PrintWriter(sw)
        err.printStackTrace(pw)
        printOpen(sw.toString(), EchoLevel.ERROR)
        throw err
    }

    return pomXmlStructure
}
