import com.caixabank.absis3.ArtifactSubType
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure

def call(PipelineData pipelineData, PomXmlStructure pomXml) {

    boolean environmentIsNotEden = pipelineData?.bmxStructure?.environment?.toUpperCase() != "EDEN"
    
    boolean isEnabled =
        getBooleanPropertyOrDefault(env.ABSIS3_SERVICES_APIMANAGER_TECHNICALSERVICES_REGISTRATION_ENABLED, true)

    def exclusionList = env.ABSIS3_SERVICES_APIMANAGER_TECHNICALSERVICES_REGISTRATION_SKIP_LIST.split(";")

    return isEnabled &&
        !pipelineData?.isPushCI() &&
        !pipelineData?.isCIFeatureBranch() &&
        !(Arrays.asList(exclusionList).contains(pomXml?.artifactName)) &&
        environmentIsNotEden &&
        pomXml?.artifactSubType == ArtifactSubType.MICRO_APP
}

private boolean getBooleanPropertyOrDefault(def property, boolean defaultValue) {

    if (property==null) {
        return defaultValue
    } else if (property=="false") {
        return false
    } else if (property=="true") {
        return true
    }
    return defaultValue
}
