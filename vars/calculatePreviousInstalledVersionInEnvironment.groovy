import com.project.alm.*


def call(PipelineData pipelineData, PomXmlStructure pomXml) {
	call(pipelineData, pomXml, null)
}

def call(PipelineData pipelineData, PomXmlStructure pomXml, String oldVersionInCurrentEnvironment) {

    String artifactGarType = pipelineData.garArtifactType.getGarName()
    def url = idecuaRoutingUtils.instalationsUrl(artifactGarType, pomXml.getApp(pipelineData.garArtifactType), pomXml.artifactName)

    def response = sendRequestToAppPortal('GET', url, "", null, pipelineData, pomXml)

    if( ! oldVersionInCurrentEnvironment ) {

        oldVersionInCurrentEnvironment = BackEndAppPortalUtilities.getMostRecentVersionInEnvironment(pomXml.majorVersion, response.content, pipelineData.bmxStructure.environment)
		if (oldVersionInCurrentEnvironment == null) {
            oldVersionInCurrentEnvironment = ""
		}

	}
    printOpen("Old version of ${pomXml.artifactName} in ${pipelineData.bmxStructure.environment} is ${oldVersionInCurrentEnvironment}", EchoLevel.ALL)
	pipelineData.pipelineStructure.resultPipelineData.oldVersionInCurrentEnvironment = oldVersionInCurrentEnvironment

    String nextEnvironment = calculateNextEnvironment(pipelineData.bmxStructure.environment)
    if (nextEnvironment) {

        String oldVersionInNextEnvironment = BackEndAppPortalUtilities.getMostRecentVersionInEnvironment(pomXml.majorVersion, response.content, nextEnvironment)
        if (oldVersionInNextEnvironment == null) {
            oldVersionInNextEnvironment = ""
        }
        
        printOpen("Old version of ${pomXml.artifactName} in ${nextEnvironment} is ${oldVersionInNextEnvironment}", EchoLevel.ALL)
        pipelineData.pipelineStructure.resultPipelineData.oldVersionInNextEnvironment = oldVersionInNextEnvironment
    }

}

private calculateNextEnvironment(String environment) {

    switch(environment.toUpperCase()) {
        case 'DEV':
            return 'TST'
        case 'TST':
            return 'PRE'
        case 'PRE':
            return 'PRO'
        default:
            return ''
    }

}
