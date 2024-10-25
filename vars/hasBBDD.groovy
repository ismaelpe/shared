import com.project.alm.*
import groovy.json.JsonSlurperClassic
import com.project.alm.GarAppType
import com.project.alm.GlobalVars


/**
 * Valida si tiene directorios sql
 * @param pomXml 
 * @param pipelineData
 * @param isReleaseTime
 * @return true si tiene BBDD false sinno tiene BBDD
 */
def call(PomXmlStructure pomXml,PipelineData pipelineData, boolean isReleaseTime = false) {
	
	
	printOpen("Evaluating the micro ${pipelineData.garArtifactType}", EchoLevel.ALL)
	if  (pipelineData.garArtifactType == GarAppType.DATA_SERVICE || pipelineData.garArtifactType == GarAppType.ARCH_MICRO) {
		//Posiblemente puede tener BBDD
		def exists = fileExists "sql"
		
		printOpen("has sql directory ${exists}?", EchoLevel.ALL)
		
		if  (exists) {
			//Es un DataService tiene el directorio sql
			if (isReleaseTime) {
				//Vamos a validar si tiene fichero
				String nombreFicheroRelease="${GlobalVars.SQL_RELEASE_DIRECTORY}changeSet-${pomXml.artifactVersion}.yml"
				exists = fileExists "${nombreFicheroRelease}"
				printOpen("has sql release file ${exists}? the file ${nombreFicheroRelease}", EchoLevel.ALL)
				
				if (exists) {
					return true
				}else {
					return false
				}
			}else {
				return true
			}			
		}else {
			return false			
		}		
	}else {
		return false
	}

}