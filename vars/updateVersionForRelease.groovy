import com.project.alm.PomXmlStructure

def call(PomXmlStructure pomXml) {
    pomXml.artifactVersion = "${pomXml.getArtifactVersionWithoutQualifier()}"
    upgradeVersion(pomXml)
}