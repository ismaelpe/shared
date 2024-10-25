import com.project.alm.EchoLevel
import com.project.alm.GarAppType
import com.project.alm.ICPStateUtility
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.Strings
import com.project.alm.GlobalVars
import hudson.Functions


def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String userId='NA', String dontGenerateImagesParam='false', String dontBuildImage='false', String command='CLONE') {
	absisPipelineStageCloneToOcp(pomXmlStructure.artifactVersion, 
		                         pomXmlStructure.artifactMicro, 
								 pomXmlStructure.getApp(pipelineData.garArtifactType),
								 pomXmlStructure.getICPAppName(), 
								 pipelineData.bmxStructure.environment.toUpperCase(), 
								 pipelineData.pushUser,
								 pipelineData.getGarArtifactType().getGarName(),
								 userId, 
								 dontGenerateImagesParam,
								 dontBuildImage,
								 command)
								 
}

def call(String artifactVersion, String artifactMicro, String garAppName, String namespace, String environment, String pushUser, String artifactType, String userId, String dontGenerateImagesParam, String dontBuildImage, String command='CLONE'){
	
//def call(PomXmlStructure pomXmlStructure, PipelineData pipelineData, String userId='NA', String dontGenerateImagesParam='false', String dontBuildImage='true') {

	String user=''
	
	//Validaremos si es necesario validar estos valores
    if (env.ENV_K8S_OCP!=null && env.ENV_K8S_OCP.contains(environment.toUpperCase())) {
		//versionParam&environmentParam&namespaceParam&typeParam&microParam&componentPomParam&multiplesMicrosParam
		if ('NA'.equals(userId)) {
			user=pushUser
		}else {
			user=userId
		}
		String jobClone=GlobalVars.ALM_JOB_CLONE_TO_OCP
		String fileOutput= CopyGlobalLibraryScript('',null,'outputCommand.json')

		String url_CloneToOCP="curl  -s -k --write-out '%{http_code}'  -o ${fileOutput}  --connect-timeout 30 --max-time 60  --retry 2 --retry-delay 10 -H  'accept: */*' -H  'Content-Type: application/json'  -X POST -u $JNKMSV_USR:$JNKMSV_PSW '$JNKMSV_DEVPORTAL_URL${jobClone}/buildWithParameters?versionParam=${artifactVersion}&environmentParam=${environment.toUpperCase()}&namespaceParam=${namespace}&typeParam=${artifactType}&multiplesMicrosParam=NA&componentPomParam=${artifactMicro}&microParam=${garAppName}&dontGenerateImagesParam=${dontGenerateImagesParam}&userId=${user}&numMaxMicros=1&ignoreBuild=${dontBuildImage}&ignoreStart=true&command=${command}'"
		
		printOpen("La url del micro es de ${url_CloneToOCP}",EchoLevel.INFO)
		
		def resultScript = sh( returnStdout: true, script: "${url_CloneToOCP}")
		printOpen("Results Scripts ${resultScript}:",EchoLevel.INFO)
		
		if ("201".equals(resultScript) || "200".equals(resultScript)) {
			printOpen("Peticion correctamente redirigida contra el entorno de clonado ${resultScript}",EchoLevel.INFO)
		}else{
			printOpen("Peticion MAL redirigida contra el entorno de clonado ${resultScript}. REVISA LOGS. No Vamos a abortar",EchoLevel.INFO)
		}
    }else{
 		printOpen("El entorno no tiene que ser clonado en OCP",EchoLevel.INFO)
    }	

}
