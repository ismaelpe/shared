import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.PomXmlStructure

def call(PomXmlStructure pomXml) {

    printOpen("The actual Version is ${pomXml.artifactVersion}", EchoLevel.ALL)
    def revisionVersion = pomXml.artifactVersion
    pomXml.incFix()

    sh "git checkout master"

    return upgradeVersion(pomXml)

}
