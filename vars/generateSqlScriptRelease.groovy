import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.WorkspaceUtils

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

def generateReleaseChangeSetArtifact(PomXmlStructure artifactPom, PipelineData pipeline) {
	String nombreFicheroRelease="${GlobalVars.SQL_RELEASE_DIRECTORY}changeSet-${artifactPom.artifactVersion}.yml"
	
	String content= sh(script: "cat ${nombreFicheroRelease}", returnStdout:true )

	printOpen("The content is ${content}", EchoLevel.ALL)
	
	
	def valuesDeployed=readYaml file: "${nombreFicheroRelease}"
	
	if (valuesDeployed!=null && valuesDeployed.get("changeSetsGroup")!=null) {
		String groupContract=artifactPom.groupId+".bbdd"
		
		if (existsArtifactDeployed(groupContract,artifactPom.artifactName,artifactPom.artifactVersion)) {
			printOpen("El artefacto ya existe, esto es un reintento... no hacemos nada", EchoLevel.ALL)
		}else {
			def groupChange=valuesDeployed.get("changeSetsGroup")
			
			def tempPath=createTempDir()
			String sanitizedTempDir = tempPath.replace(' ', '\\ ')
			sh "mkdir -p ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}"
                        sh "rm -rf ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/*"	
			
                        String pathToChangeSetPom = CopyGlobalLibraryScript(GlobalVars.LIQUIBASE_CHANGE_SET_POM_FILENAME, "${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}")
			
			for(int i=0;i<groupChange.size();i++) {
				String changeSet=groupChange.get(i)
				//Upload artifact
				
				printOpen("Getting changeSet ${changeSet}", EchoLevel.INFO)

                sh "sed -i 's/#ARTIFACT#/${artifactPom.artifactName}/g' ${pathToChangeSetPom}"
                sh "sed -i 's/#VERSION#/${changeSet}/g' ${pathToChangeSetPom}"
                sh "sed -i 's/#GROUP#/${groupContract}/g' ${pathToChangeSetPom}"
                sh "sed -i 's/#ARCHVERSION#/${artifactPom.archVersion}/g' ${pathToChangeSetPom}"

                //Download artifact
                try {

                    def cmd = "mvn dependency:copy -Dartifact=${groupContract}:${artifactPom.artifactName}:${changeSet} -DoutputDirectory=${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY} -f  ${pathToChangeSetPom} <Default_Maven_Settings> "
                    runMavenCommand(cmd)

                } catch(Exception e) {

                    throw new Exception("Invalid changeSet identificator. Check that ${changeSet} is correct")

                }

                String dirContent = sh(script: "ls -la ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/*", returnStdout: true)
                printOpen("Directory content:\n${dirContent}", EchoLevel.DEBUG)

                def exists = fileExists "${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/application.jar"
                if (exists == true) {
                    printOpen("Borramos el fichero application.jar", EchoLevel.ALL)
                    sh "rm  ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/application.jar"
                }

                sh "mv ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/*.jar ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/application.jar"

                sh "cd ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}"
                sh "unzip -v ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/application.jar"

                generateUnpackScript("${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}")
                printOpen("Executing the unpackScript", EchoLevel.ALL)
                sh "${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/unpackScript.sh"
                dirContent = sh(script: "ls -la ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/*", returnStdout: true)
                printOpen("Tmp content:\n${dirContent}", EchoLevel.DEBUG)
                dirContent = sh(script: "ls -la *", returnStdout: true)
                printOpen("Root content:\n${dirContent}", EchoLevel.DEBUG)

                sh "rm  ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/application.jar"

                //Tendriamos que modificar los ficheros al formato correlativo
                //para asegurar que son relativos dentro del mismo package set
                //En el changeSet tenemos version.IdChangeSet
                //1.0.0.IdChange
                def deleteSuffix = artifactPom.artifactVersion+ "."
                def suffixFiles = changeSet-deleteSuffix
				def suffixFiles1 = changeSet.substring(changeSet.lastIndexOf('.')+1)
				
                printOpen("We will delete the sql files ${suffixFiles} ${suffixFiles1}", EchoLevel.ALL)
                sh "cd ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY} && ls *${suffixFiles1}*.sql | awk '{ printf(\"%s %03d%s \\n\",\$1,\"${i}\",\$1) }' | xargs -n2 mv"

			}
			
			sh "mkdir -p ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/${GlobalVars.RESOURCE_PATH}"
			
			sh "mv ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/*.sql ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/${GlobalVars.RESOURCE_PATH}/ "
	
			pathToChangeSetPom = CopyGlobalLibraryScript(GlobalVars.LIQUIBASE_CHANGE_SET_POM_FILENAME, "${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}")
			
			sh "sed -i 's/#ARTIFACT#/${artifactPom.artifactName}/g' ${pathToChangeSetPom}"
			sh "sed -i 's/#VERSION#/${artifactPom.artifactVersion}/g' ${pathToChangeSetPom}"
			sh "sed -i 's/#GROUP#/${groupContract}/g' ${pathToChangeSetPom}"
			sh "sed -i 's/#ARCHVERSION#/${artifactPom.archVersion}/g' ${pathToChangeSetPom}"

            def cmd = "mvn clean deploy -Dmaven.install.skip <Default_Maven_Settings> -f  ${pathToChangeSetPom} -X "
            boolean weHaveToGenerateOpenApiClasses =
                WorkspaceUtils.isThereASwaggerContract(this, artifactPom) &&
                    ! WorkspaceUtils.areSwaggerContractClassesGenerated(this, artifactPom)
            if ( ! weHaveToGenerateOpenApiClasses ) cmd += " -Dcodegen.skip=true "

            commitLog = runMavenGoalWithRetries(artifactPom, pipeline, cmd, [
                kpiAlmEvent: new KpiAlmEvent(
                    artifactPom, pipeline,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.MVN_DEPLOY_SQL_SCRIPT_RELEASE_NEXUS)
            ])
			
            printOpen("The result is ${commitLog}", EchoLevel.INFO)
		}	
	}
}


def call(PomXmlStructure artifactPom, PipelineData pipeline, boolean isDeployToPro = false) {
	
	def result=""
	
	//Get ChangeSetFeature
	if (hasBBDD(artifactPom,pipeline,true)) {
		if (!isDeployToPro) {
			generateReleaseChangeSetArtifact(artifactPom,pipeline)
		}
		result=deployScriptToKubernetes(artifactPom,pipeline,artifactPom.groupId+".bbdd",artifactPom.artifactName,artifactPom.artifactVersion)
		
	}
	return result
}
