import com.project.alm.EchoLevel
import com.project.alm.PomXmlStructure

def call(PomXmlStructure pomXml) {

    printOpen("The actual Version is ${pomXml.artifactVersion}", EchoLevel.ALL)
    def revisionVersion = pomXml.artifactVersion
    pomXml.incFix()

    sh "git checkout master"

    return upgradeVersion(pomXml)

}
