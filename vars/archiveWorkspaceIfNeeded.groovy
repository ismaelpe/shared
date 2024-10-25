import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.EchoLevel

def call(PomXmlStructure pomXml, PipelineData pipeline) {
	def whiteListApps = "${env.ABSIS3_SERVICES_ARCHIVE_WORKSPACE}".split(";")
	if(Arrays.asList(whiteListApps).contains(pomXml.artifactName)) {
	    printOpen("Storing workspace in a zip file...",EchoLevel.INFO)
		zip zipFile: 'target.zip', archive: true, dir: 'target'
	} else {
		printOpen("This workspace is not going to be stored in a zip file.",EchoLevel.INFO)
	}
}