import com.project.alm.EchoLevel
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.GlobalVars
import com.project.alm.NexusUtils

def call(PomXmlStructure pomXml, PipelineData pipeline, boolean localContracts = true) {
	//Validamos sino ha sido desplegado antes
	if (existsArtifactDeployed( "${pomXml.groupId}.contract",pomXml.artifactName,pomXml.artifactVersion)) {
		printOpen("The micro has benn deployed before", EchoLevel.INFO)
	}else {
        printOpen("Publishing Alm2 and Open clients. artifactId: ${pomXml.artifactName} version: ${pomXml.artifactVersion}", EchoLevel.INFO)

		if (pipeline.revapiStructure.tempDir) {
    		printOpen("Cleaning temporary dir before creating the clients...", EchoLevel.DEBUG)
			String sanitizedTempDir = pipeline.revapiStructure.tempDir.replace(' ', '\\ ')
			sh "rm -rf ${sanitizedTempDir}/src"
		}

		printOpen("Publishing OpenServices client...", EchoLevel.INFO)
		boolean isAlm2 = false

		// Generamos el contrato para OpenServices
		generateContractPom(pomXml, pipeline, isAlm2)
		
		// Es imporntante generar el contrato para OpenService ya que para las versiones mas actuales
		// se usara el contrato remoto y no en local en caso que proceda
		String commitLog = installDeployClientArtifact(pomXml, pipeline, false, isAlm2)
		pipeline.clientRoutesToNexus = NexusUtils.extractArtifactsFromLog(commitLog)
		printOpen("OpenServices client has been published", EchoLevel.INFO)
		

		// Generamos el contrato para Alm2
		printOpen("Publishing ALM2 client...", EchoLevel.INFO)
		isAlm2 = true
		
		generateContractPom(pomXml, pipeline, isAlm2)
		installDeployClientArtifact(pomXml, pipeline, false, isAlm2)
		printOpen("ALM2 client has been published", EchoLevel.INFO)

		// Borramos los contratos remotos
		cleanTmp()
	}
}

def generateClients(PomXmlStructure pomXml, PipelineData pipeline) {
	printOpen("Publishing OpenServices client...", EchoLevel.INFO)
	boolean isAlm2 = false

	generateContractPom(pomXml, pipeline, isAlm2)
	
	// Primero hemos de ejecutar la generación del cliente con version propia de la arq
	// Esto hará que si se recupera el contrato de mule se deposite en /tmp, para despues
	// poder usarlo para alm2 que siempre usara la version 1.19.0 (no tiene mule)
	String commitLog = installDeployClientArtifact(pomXml, pipeline, false, isAlm2)
	pipeline.clientRoutesToNexus = NexusUtils.extractArtifactsFromLog(commitLog)
	printOpen("OpenServices client has been published", EchoLevel.INFO)
	
	printOpen("Publishing ALM2 client...", EchoLevel.INFO)
	isAlm2 = true
	
	generateContractPom(pomXml, pipeline, isAlm2)
	installDeployClientArtifact(pomXml, pipeline, false, isAlm2)
	printOpen("ALM2 client has been published", EchoLevel.INFO)

	// Borramos los contratos remotos
	cleanTmp()
}
		
	
