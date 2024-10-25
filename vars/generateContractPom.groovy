import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.Utilities
import com.caixabank.absis3.MavenVersionUtilities


def call(PomXmlStructure pomXml, PipelineData pipeline, boolean clientAbsis2) {
	
	printOpen("Generating Revapi POM for artifactId: ${pomXml.artifactName} version: ${pomXml.artifactVersion}", EchoLevel.INFO)

	//1. Generamos el cliente con revapi + plugin
	//2. La generacion se publica en Artifactory.
	//3. Cliente Absis3
	//4. Cliente Absis2

	String groupContract = "${pomXml.groupId}.contract"

	String tempDir = CopyFileToTemp('contract/' + GlobalVars.SWAGGER_CONTRACT_FILENAME, GlobalVars.SWAGGER_CONTRACT_FILENAME)
	pipeline.revapiStructure.tempDir = tempDir
	
	String pathToRevapiPom = CopyGlobalLibraryScript(GlobalVars.REVAPI_POM_FILENAME, tempDir)

	String sanitizedTempDir = tempDir.replace(' ', '\\ ')

	String dirContent = sh(script: "ls -la ${sanitizedTempDir}", returnStdout: true)
    printOpen("Files in ${sanitizedTempDir}:\n${dirContent}", EchoLevel.DEBUG)

	String artifactName = pomXml.artifactName
	String archVersion = pomXml.archVersion

	if (clientAbsis2) {
		artifactName = "${artifactName}Absis2"
		if (!Utilities.isLowerThan(MavenVersionUtilities.getArtifactVersionWithoutQualifier(pomXml.archVersion), GlobalVars.MAX_VERSION_ARCH_NEXUS)) {
			archVersion = GlobalVars.MAX_VERSION_ARCH_NEXUS
			pipeline.revapiStructure.remoteSwaggerContractPath = getRemoteSwaggerContractFile(pipeline)
		}
	}

	printOpen("Maven details for client ${groupContract} ${artifactName} ${pomXml.artifactVersion}", EchoLevel.ALL)

	sh "sed -i 's/#ARTIFACT#/${artifactName}/g' ${pathToRevapiPom}"
	sh "sed -i 's/#VERSION#/${pomXml.artifactVersion}/g' ${pathToRevapiPom}"
	sh "sed -i 's/#GROUP#/${groupContract}/g' ${pathToRevapiPom}"
	sh "sed -i 's/#ARCHVERSION#/${archVersion}/g' ${pathToRevapiPom}"

	printOpen("Saving temporary directory ${tempDir}", EchoLevel.ALL)
	
}

def getRemoteSwaggerContractFile(PipelineData pipeline) {
	String tmpSwaggerRemote = System.getProperty('java.io.tmpdir') + "/remote-swagger-micro-*.yaml"
   	def filesContracts = steps.sh(script: "ls -lr $tmpSwaggerRemote | awk '{ print \$9 }'", returnStdout: true)

	if (filesContracts) {
		return filesContracts.trim().split("\n")[0]		
	} else {
		return null
	}
}

