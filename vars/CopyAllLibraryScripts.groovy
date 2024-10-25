import com.project.alm.EchoLevel

String createTempLocation(String path) {
	String tmpDir = pwd tmp: true
	
	String sanitizedTmpDir = tmpDir.replace(' ', '\\ ')
	//sh "ls -la ${sanitizedTmpDir}"
	
	return tmpDir + File.separator + new File(path).getName()
}

String createTempDir() {
	String tmpDir = pwd tmp: true
	String sanitizedTmpDir = tmpDir.replace(' ', '\\ ')

	cleanTmp(sanitizedTmpDir)


	return tmpDir
}

def copyFile(def destDir, def fileName) {
	def destPath = CopyFileToTemp.buildDestPath(destDir, fileName)
	def exists = fileExists "${destPath}"
	if (exists) {
		sh("rm ${destPath}")
	}

	writeFile file: destPath, text: libraryResource(fileName)
}


def call() {
	String destDir = createTempDir()
	
	def contenidoFichero=sh(returnStdout: true,script: " ls -lart ${destDir}")
	
	String scriptsDir = pwd scripts: true
	String sanitizedScriptsDir = scriptsDir.replace(' ', '\\ ')
	
	def contenidoScripts=sh(returnStdout: true,script: " ls -lart ${sanitizedScriptsDir}")
	
	printOpen("Inicial. ScriptsDIR in ${scriptsDir}: ${contenidoScripts}", EchoLevel.DEBUG)
	
	printOpen("Inicial. DestDIR in ${destDir}: ${contenidoFichero}", EchoLevel.DEBUG)

	copyFile(destDir, 'scripts/curlUtils.sh')
	copyFile(destDir, 'scripts/buildArtifactICP.sh')
	copyFile(destDir, 'scripts/generateArtifactICP.sh')
	copyFile(destDir, 'scripts/openServicesKpi.sh')
	copyFile(destDir, 'scripts/sendCurlToICP.sh')
	copyFile(destDir, 'scripts/getLastDeployment.sh')
	copyFile(destDir, 'scripts/deployArtifactICP.sh')
	copyFile(destDir, 'scripts/getArtifactICP.sh')
	copyFile(destDir, 'scripts/validateMicroIsUp.sh')
	copyFile(destDir, 'scripts/getArtifactCatMsv.sh')
	copyFile(destDir, 'scripts/compareMicroIcpVsOcp.sh')
	copyFile(destDir, 'scripts/undeployApps.sh')
	sh("chmod 777 ${destDir}/*.sh")
	sh("mkdir -p ${destDir}/data")
	sh("chmod 777 ${destDir}/data")
	sh("mkdir -p ${destDir}/logs")
	sh("chmod 777 ${destDir}/logs")
	contenidoFichero=sh(returnStdout: true,script: " ls -lart ${destDir}")
	
		
	printOpen("DestDIR in ${destDir}: ${contenidoFichero}", EchoLevel.DEBUG)
	
	return destDir
}