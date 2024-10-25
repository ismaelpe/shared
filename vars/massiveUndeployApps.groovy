import com.project.alm.*

def call(String path, String namespace, String enviroment, String period, String action) {
	def command = "$path/undeployApps.sh $namespace $enviroment $period $action"
	
	printOpen(command, EchoLevel.INFO)
		
	//Recogemos el deploy en ICP
	def resultScript = null
	try {	
		resultScript = sh(returnStdout: true, script: command)
	} catch(e) {
		printOpen("Error ocurrido ${e}",EchoLevel.ERROR)
		generateShError("${path}",false)
	}
}
