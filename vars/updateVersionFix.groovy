import com.caixabank.absis3.PomXmlStructure

def call(PomXmlStructure pomXml) {

    pomXml.incFix()
    pomXml.artifactVersion = "${pomXml.getArtifactVersionWithoutQualifier()}-RC0"
    upgradeVersion(pomXml)

}
