import com.project.alm.EchoLevel
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.ClientInfo
import com.project.alm.MavenVersionUtilities

def call(boolean initAppPortal, ClientInfo clientInfo, PipelineData pipelineData) {
	if (initAppPortal && notificationToAppPortalApplies()) {
		
				printOpen("Sending to AppPortal a Pipeline Update operation", EchoLevel.DEBUG)
		
				def url = idecuaRoutingUtils.pipelineUrlById(pipelineData.pipelineStructure.pipelineId)
				def body = [
						id                : "${pipelineData.pipelineStructure.pipelineId}",
						plataforma        : pipelineData.pipelineStructure.plataforma,
						tipo              : "${pipelineData.garArtifactType.getGarName()}",
						aplicacion        : "${clientInfo.getApp(pipelineData.garArtifactType)}",
						componente        : "${clientInfo.getArtifactId()}",
						versionMajor      : "${MavenVersionUtilities.getMajor(clientInfo.getArtifactVersion())}",
						versionMinor      : "${MavenVersionUtilities.getMinor(clientInfo.getArtifactVersion())}",
						versionFix        : "${MavenVersionUtilities.getPatch(clientInfo.getArtifactVersion())}",
						build             : "${pipelineData.buildCode}",
						branch            : "${pipelineData.branchStructure.branchName}"
				]
		
				def response = sendRequestToAppPortal('PUT', url, "", body, pipelineData, clientInfo)
		
				return response
			}
}

def call(boolean initAppPortal, PomXmlStructure pomXml, PipelineData pipelineData, String pipelineOrigId) {

    if (initAppPortal && notificationToAppPortalApplies()) {

        printOpen("Sending to AppPortal a Pipeline Update operation", EchoLevel.DEBUG)

        def url = idecuaRoutingUtils.pipelineUrlById(pipelineData.pipelineStructure.pipelineId)
        def body = [
                id                : "${pipelineData.pipelineStructure.pipelineId}",
                plataforma        : pipelineData.pipelineStructure.plataforma,
                tipo              : "${pipelineData.garArtifactType.getGarName()}",
                aplicacion        : "${pomXml.getApp(pipelineData.garArtifactType)}",
                componente        : "${pomXml.artifactName}",
                versionMajor      : "${pomXml.getArtifactMajorVersion()}",
                versionMinor      : "${pomXml.getArtifactMinorVersion()}",
                versionFix        : "${pomXml.getArtifactFixVersion()}",
                build             : "${pipelineData.buildCode}",
                branch            : "${pipelineData.branchStructure.branchName}",
                anteriorPipelineId: "${pipelineOrigId}"
        ]

        def response = sendRequestToAppPortal('PUT', url, "", body, pipelineData, pomXml)

        return response
    }
}
