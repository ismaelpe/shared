import com.project.alm.*
import groovy.json.JsonSlurperClassic
import com.project.alm.GarAppType
import com.project.alm.GlobalVars



def call (PomXmlStructure pomXml, PipelineData pipeline){
	String artifactName=pomXml.artifactName
	
	if (pomXml.artifactType==ArtifactType.AGREGADOR && pomXml.artifactMicro!="") {
		//Si hemos hecho deploy en Cloud ahora no tenemos que hacer deploy ya que lo hemos hecho antes
		artifactName = pomXml.artifactMicro
	}	
	if (pomXml.artifactType==ArtifactType.AGREGADOR && pomXml.artifactSampleApp != "") {
		artifactName = pomXml.artifactSampleApp
	}
	
	return existsArtifactDeployed(pomXml.groupId,artifactName,pomXml.artifactVersion)	
}
/**
 * Permite validar si existe un artefacto desplegado en Nexus
 * @param group String con el grupo
 * @param artifact String con el id de artefacto
 * @param version String con la version
 * @return true si existe false sino existe
 */
def call(String groupContract, String artifactName, String version) {
	boolean exists=true
	printOpen("Validating if the ${groupContract}:${artifactName}:${version} is deployed?", EchoLevel.ALL)
	
	if (version.indexOf('SNAPSHOT')!=-1) {
		printOpen("The version is a SNAPSHOT exists = false", EchoLevel.ALL)
		exists = false
	}else {
		exists = new NexusUtils(this).exists(groupContract, artifactName, version);
	}
	return exists

}