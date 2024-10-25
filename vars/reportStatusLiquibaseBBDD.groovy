import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineData


def call(PomXmlStructure artifactPom, PipelineData pipeline, String liquibaseCommand) {
	String result=""
    printOpen(" El comando liquibase es el siguiente ${liquibaseCommand}", EchoLevel.ALL)
	
	boolean isHistory=false
	boolean isGenerateChangeLog=false
	
	//Antes de desplegar tenemos que borrar
	if (liquibaseCommand.indexOf('history')!=-1) {
		isHistory=true
	}
	if (liquibaseCommand.indexOf('generateChangeLog')!=-1) {
		isGenerateChangeLog=true
	}
	result = deployScriptToKubernetes(artifactPom,pipeline,"NA",artifactPom.artifactName,"NA",isHistory,isGenerateChangeLog)

	return result
}
