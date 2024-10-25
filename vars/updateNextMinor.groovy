import com.project.alm.EchoLevel
import com.project.alm.PomXmlStructure

def call(PomXmlStructure pomXml) {

    printOpen("Upgrade version to next minor in master", EchoLevel.INFO)
    printOpen("The actual Version is ${pomXml.artifactVersion}", EchoLevel.ALL)
    def revisionVersion = pomXml.artifactVersion
    pomXml.incMinor()

    sh "git checkout master"

    return upgradeVersion(pomXml)

}
