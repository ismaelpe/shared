import com.caixabank.absis3.EchoLevel

def call() {
	return validateFiles("**")
}

def call(String path) {
    printOpen("Validate files in ${path}!!!", EchoLevel.INFO)
	final files = findFiles(glob: "**/${path}/*.yml")

	boolean markAsError = false
	def errorFiles = []
	for (def file : files) {
        printOpen("parsing file: ${file.path}", EchoLevel.DEBUG)

		try {
			datas = readYaml(file: "${file.path}")
		}
		catch (Exception e) {
            printOpen("Error parsing file: ${file.path}" + e.getMessage(), EchoLevel.ERROR)
			markAsError = true
			errorFiles.add(file.path)
		}
	}
	if(markAsError) {
		String errorText = "Error parsing yaml files:"
		for(def errorFile : errorFiles) {
			errorText = errorText + errorFile + "\n"
		}
		error errorText
	}
	return
}
