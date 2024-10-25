import com.project.alm.PomXmlStructure
import com.project.alm.ArtifactType
import com.project.alm.ArtifactSubType

def call(PomXmlStructure pomXmlStructure) {
    getArtifactFromNexusWithGAVs(pomXmlStructure.groupId, pomXmlStructure.artifactMicro, pomXmlStructure.artifactVersion, ".")
    return pomXmlStructure
}