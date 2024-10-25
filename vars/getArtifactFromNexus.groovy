import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.ArtifactType
import com.caixabank.absis3.ArtifactSubType

def call(PomXmlStructure pomXmlStructure) {
    getArtifactFromNexusWithGAVs(pomXmlStructure.groupId, pomXmlStructure.artifactMicro, pomXmlStructure.artifactVersion, ".")
    return pomXmlStructure
}