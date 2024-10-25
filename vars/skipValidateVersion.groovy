import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.PomXmlStructure

//Solo buscaremos si tenemeos que hacer skip de la validacion del revapi
def call(PomXmlStructure pomXml) {
	
	String file = 'contract/' + pomXml.getArtifactVersionWithoutQualifier()+'.ignore'
	printOpen("Checking if the file ${file} exists...", EchoLevel.DEBUG)
	
		
	def fileExists = fileExists file
	
	if (!fileExists) {
		printOpen("The file ${file} doesn't exist.", EchoLevel.DEBUG)
		return false
	}else {
		printOpen("The file ${file} exists. Ignoring version control.", EchoLevel.ALL)
		return true
	}
	
}
