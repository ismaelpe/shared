import com.project.alm.*
import groovy.json.JsonSlurperClassic
import com.project.alm.GlobalVars
import com.project.alm.PipelineBehavior


def call(PipelineData pipelineData,PomXmlStructure pomXmlStructure) {
	
	//Vamos a ver que queremos
	//Si es push con merge request no tenemos que hacer nada
	//Si es merge de una rama de feature tenemos que hacer todo

	def pipelineBehaviour = validateMR(pipelineData, pomXmlStructure)
	pipelineData.pipelineBehavior = pipelineBehaviour
	env.pipelineBehavior = pipelineBehaviour
	printOpen("Resultado de MR Abiertas ${pipelineBehaviour} ${pipelineData.gitAction} el valor del skip ${env.SKIP_BUILD_CI}", EchoLevel.DEBUG)
	
	if (pipelineBehaviour == PipelineBehavior.PUSH_OPENED_MR) {
		//No vamos a hacer nada de nada
		pipelineData.deployFlag = false
		pipelineData.commitLog = GlobalVars.GIT_TAG_CI_PUSH
	} else if (pipelineBehaviour == PipelineBehavior.NOT_FIRST_MR) {
		//Este debe hacer deploy
		pipelineData.deployFlag = true
	}
	//tenemos un problema con las MR de features de tipo BBDD, ya que
	//el ultimo commit sera siempre de la CI
	if (pipelineBehaviour == PipelineBehavior.NOT_FIRST_MR && pipelineData.isPushCI() && pipelineData.branchStructure.featureNumber.startsWith('BBDD') ) {
		//Es una merge y puede ser de un push nuestro... sino validamos quizas no va  funcionar
		pipelineData.commitLog = "Validar MR SQL"
	}

	//Deberiamos evaluar si tenemos el skip habilitado y estamos en PUSH sin MR
	if (env.SKIP_BUILD_CI!=null && env.SKIP_BUILD_CI == "true" && pipelineData.deployFlag == false && !pipelineData.branchStructure.featureNumber.startsWith('BBDD')) {
		//Ahora tenemos que revisar el estado en el que estamos de la pipeline
		//Si es PUSH sin MR vamos a abortar incluso el build	
		if (pipelineBehaviour == PipelineBehavior.PUSH_NO_MR) {
			//Deshabilitamos 
			printOpen("Vamos a deshabilitar el build ${pipelineBehaviour} y todo lo que hace la pipeline", EchoLevel.DEBUG)
			pipelineData.commitLog = GlobalVars.GIT_TAG_CI_PUSH
		}
		
	}

	return pipelineBehaviour
}
