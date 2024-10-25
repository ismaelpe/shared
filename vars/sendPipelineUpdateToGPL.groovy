import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.ClientInfo
import com.caixabank.absis3.MavenVersionUtilities

def call(boolean initGpl, ClientInfo clientInfo, PipelineData pipelineData) {
	if (initGpl && notificationToGplApplies()) {
		
				printOpen("Sending to GPL a Pipeline Update operation", EchoLevel.DEBUG)
		
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
		
				def response = sendRequestToGpl('PUT', url, "", body, pipelineData, clientInfo)
		
				return response
			}
}

def call(boolean initGpl, PomXmlStructure pomXml, PipelineData pipelineData, String pipelineOrigId) {

    if (initGpl && notificationToGplApplies()) {

        printOpen("Sending to GPL a Pipeline Update operation", EchoLevel.DEBUG)

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

        def response = sendRequestToGpl('PUT', url, "", body, pipelineData, pomXml)

        return response
    }
}
