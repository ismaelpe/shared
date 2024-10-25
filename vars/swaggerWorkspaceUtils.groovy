import com.caixabank.absis3.*



def isThereASwaggerContract( PomXmlStructure pomXml) {
	String rootFolder = WorkspaceUtils.getRootFolderPath(env.WORKSPACE, pomXml)
	def contractPath = "${rootFolder}/contract/swagger-micro-contract.yaml"
	printOpen("Contract path: ${contractPath}", EchoLevel.ALL)
	return fileExists("${contractPath}")

}

def areSwaggerContractClassesGeneratedIn(String root, PomXmlStructure pomXml) {
	
	String contractPackage = pomXml.contractPackage
	try {

		if ( ! contractPackage ) return false

		String rootFolder = WorkspaceUtils.getRootFolderPath(root, pomXml)
		contractPackage = contractPackage.replace(".", "/")
		def contractApiPath = "${rootFolder}/src/main/java/${contractPackage}/api/domain"
		printOpen("El path donde deberia estar el codigo: ${root}", EchoLevel.ALL)
		printOpen("Contract path: ${contractApiPath}", EchoLevel.ALL)
		def pathExists = fileExists("${contractApiPath}")

		printOpen("Contract path: ${contractApiPath} and the value is ${pathExists}  ", EchoLevel.ALL)
		
		if (pathExists) {

			printOpen("Path exists ls ${contractApiPath} | wc -l", EchoLevel.ALL)
			def numberOfFiles = sh(returnStdout: true, script: "ls ${contractApiPath} | wc -l").trim()
			printOpen("The number of files ${numberOfFiles}", EchoLevel.ALL)
			if (numberOfFiles?.toInteger() > 0) {

				return true

			}

		}

	} catch(err) {

		// In case of any error in the check, we'll assume is not present
		return false

	}


	return false
}