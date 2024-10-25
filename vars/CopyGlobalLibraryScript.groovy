import com.project.alm.EchoLevel

String createTempLocation(String path) {
    String tmpDir = pwd tmp: true
	
	String sanitizedTmpDir = tmpDir.replace(' ', '\\ ')
	
	return tmpDir + File.separator + new File(path).getName()
}


def call(String fileName, boolean alternativePath, EchoLevel echoLevel = EchoLevel.ALL) {
	String fileNameTemp = fileName
	if (alternativePath) fileNameTemp = "other"+fileName
		
	def destPath = createTempLocation(fileNameTemp)
	
	writeFile file: destPath, text: libraryResource(fileName)
    printOpen("CopyGlobalLibraryScript: copied libraryResource ${fileName} to ${destPath}", echoLevel)
	return destPath
}

def call(String fileName, String destPath = null, EchoLevel echoLevel = EchoLevel.ALL) {
    printOpen("fileName ${fileName} destPath ${destPath}", echoLevel)
    destPath = destPath ? destPath + File.separator + fileName : createTempLocation(fileName)
    writeFile file: destPath, text: libraryResource(fileName)
    printOpen("CopyGlobalLibraryScript: copied libraryResource ${fileName} to ${destPath}", echoLevel)
    return destPath
}

def call (String content, String destPath , String fileName, EchoLevel echoLevel = EchoLevel.ALL) {
    printOpen("fileName ${fileName} destPath ${destPath}", echoLevel)
	destPath = destPath ? destPath + File.separator + fileName : createTempLocation(fileName)
	writeFile file: destPath, text: content
    printOpen("CopyGlobalLibraryScript: copied libraryResource ${fileName} to ${destPath}", echoLevel)
	return destPath
}

