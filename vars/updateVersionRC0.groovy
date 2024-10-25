import com.caixabank.absis3.PomXmlStructure

def call(PomXmlStructure pomXml, boolean isRC) {

    if (isRC)
        pomXml.artifactVersion = "${pomXml.getArtifactVersionWithoutQualifier()}-RC0"
    else
        pomXml.artifactVersion = "${pomXml.getArtifactVersionWithoutQualifier()}"

    upgradeVersion(pomXml)

}
