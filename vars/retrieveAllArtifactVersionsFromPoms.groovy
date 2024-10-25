def call(def poms = [:], boolean removeQualifier = false) {

    def pomsWithVersion = [:]

    try {
        for (it in poms) {
            def pomXml = new XmlSlurper().parseText(it.value)
            def version = pomXml.version.text()
            if (removeQualifier) {
                version = version =~ /(?m)^(\d+\.)?(\d+\.)?(\*|\d+)/
                version = version.find() ? version[0][0] : ''
            }
            pomsWithVersion.put(it.key, version)
        }
        return pomsWithVersion

    } catch (Exception e) {
        printOpen(e.getMessage(), EchoLevel.ERROR)
        throw e
    }
}
