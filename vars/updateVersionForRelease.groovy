import com.caixabank.absis3.PomXmlStructure

def call(PomXmlStructure pomXml) {
    pomXml.artifactVersion = "${pomXml.getArtifactVersionWithoutQualifier()}"
    upgradeVersion(pomXml)
}