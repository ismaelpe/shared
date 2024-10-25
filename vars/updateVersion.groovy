import com.project.alm.PomXmlStructure

def call(PomXmlStructure pomXml) {

    if (pomXml.isRCVersion()) {
        //Si es SNAPSHOT va a petar
        String oldVersion = pomXml.artifactVersion
        pomXml.incRC()
        upgradeVersion(pomXml)
    }

}