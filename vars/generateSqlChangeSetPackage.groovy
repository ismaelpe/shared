import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.WorkspaceUtils
import com.project.alm.MavenUtils

import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEvent

String createTempDir() {
	String tmpDir = pwd tmp: true
	String sanitizedTmpDir = tmpDir.replace(' ', '\\ ')

	cleanTmp(sanitizedTmpDir)


	return tmpDir
}

def generateUnpackScript(String path) {
	sh "echo '#!/bin/bash' > ${path}/unpackScript.sh"
	sh "echo 'cd ${path}'  >> ${path}/unpackScript.sh"
	sh "echo 'jar -xvf ${path}/application.jar'  >> ${path}/unpackScript.sh"
	printOpen("The content of the script is:", EchoLevel.ALL)
	sh "cat ${path}/unpackScript.sh"
	sh "chmod 777 ${path}/unpackScript.sh"
	String dirContent = sh(script: "ls -lart ${path}/unpackScript.sh", returnStdout: true)
    printOpen("Directory content:\n${dirContent}", EchoLevel.DEBUG)
}
 
def call(PomXmlStructure artifactPom, PipelineData pipeline) {
	def versionRelease=""
	def generatusScriptOK=false
	/**
	 * Validamos que el autor d ela mr sea el usuario de jenkins
	 */
	def author=getMergeAuthor(artifactPom,pipeline,true)
	if (author!=null && author.name!=GlobalVars.JENKINS_GIT_USER) {
		throw new Exception("Merge incorrecta. Sólo se permiten Merges automáticas. Este usuario (${author.name}) no puede abrir MR sobre esta rama")
	}
	
	def title=getMergeTitle(artifactPom,pipeline)
	//Tenemos que recoger el id de la feature de BBDD
	def lastPosition=title.lastIndexOf(' ')
	def version = ""
	if (lastPosition!=-1) {
		version=title.substring(lastPosition).trim()
		printOpen("The version of the changeset is ${version} ", EchoLevel.ALL)
		versionRelease=version-'-SNAPSHOT'
		printOpen("The final version of the changeset is ${versionRelease} ", EchoLevel.ALL)
		
		def tempPath=createTempDir()
		String sanitizedTempDir = tempPath.replace(' ', '\\ ')
		sh "mkdir -p ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}"

        String groupContract=artifactPom.groupId+".bbdd"

        //Upload artifact
        String pathToChangeSetPom = CopyGlobalLibraryScript(GlobalVars.LIQUIBASE_CHANGE_SET_POM_FILENAME, "${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}")

        if (existsArtifactDeployed(groupContract,artifactPom.artifactName,versionRelease)) {

            printOpen("El artefacto, ya existe, no se debe publicar nada", EchoLevel.ALL)

        }else {

            sh "sed -i 's/#ARTIFACT#/${artifactPom.artifactName}/g' ${pathToChangeSetPom}"
            sh "sed -i 's/#VERSION#/${versionRelease}/g' ${pathToChangeSetPom}"
            sh "sed -i 's/#GROUP#/${groupContract}/g' ${pathToChangeSetPom}"
            sh "sed -i 's/#ARCHVERSION#/${artifactPom.archVersion}/g' ${pathToChangeSetPom}"

            sh "cat ${pathToChangeSetPom}"

            sh "cd ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}"

            //Download artifact
            def cmd = "mvn dependency:copy -Dartifact=${groupContract}:${artifactPom.artifactName}:${version} -DoutputDirectory=${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY} -f  ${pathToChangeSetPom} <Only_Maven_Settings> "
            def commitLog = runMavenCommand(cmd)

            printOpen("Download from maven ${commitLog}", EchoLevel.INFO)

            String dirContent = sh(script: "ls -la ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}", returnStdout: true)
            printOpen("Files at ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}:\n${dirContent}", EchoLevel.DEBUG)

            sh "mv ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/*.jar ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/application.jar"


            sh "unzip -v ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/application.jar"
            generateUnpackScript("${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}")
            printOpen("Executing the unpackScript", EchoLevel.ALL)
            sh "${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/unpackScript.sh"



            sh "rm  ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/application.jar"
            //
            sh "mkdir -p ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/${GlobalVars.RESOURCE_PATH}"
            printOpen("Eliminando contenido antiguo", EchoLevel.ALL)
            sh "rm ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/${GlobalVars.RESOURCE_PATH}/* || true"
            sh "mv ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/*.sql ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/${GlobalVars.RESOURCE_PATH}/ "

            cmd = "mvn clean deploy -Dmaven.install.skip <Default_Maven_Settings> -f  ${pathToChangeSetPom}"
            boolean weHaveToGenerateOpenApiClasses =
                WorkspaceUtils.isThereASwaggerContract(this, artifactPom) &&
                    ! WorkspaceUtils.areSwaggerContractClassesGenerated(this, artifactPom)
            if ( ! weHaveToGenerateOpenApiClasses ) cmd += " -Dcodegen.skip=true "

            def deploymentRepo = MavenUtils.getDeploymentRepository(versionRelease)
           
            commitLog = runMavenGoalWithRetries(artifactPom, pipeline, cmd, [
                forceDeploymentRepo: deploymentRepo,
                kpiAlmEvent: new KpiAlmEvent(
                    artifactPom, pipeline,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.MVN_DEPLOY_SQL_CHANGE_SET_PACKAGE_NEXUS)
            ])

            printOpen("The result is ${commitLog}", EchoLevel.ALL)

        }
        pipeline.eventToPush=versionRelease

        generatusScriptOK=true
		
	}
	if (generatusScriptOK) {
		 return "El identificador del ChangeSet a promocionar es el siguiente: ${versionRelease}"
	}else {
		throw new Exception("La integración no fa funcionado, La merge no parece correcta, comunique mediante foro el posible problema")
	}
	
}
