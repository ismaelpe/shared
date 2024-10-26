import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.PomXmlStructure
import com.project.alm.Utilities
import com.project.alm.PipelineData
import com.project.alm.WorkspaceUtils
import com.project.alm.PipelineBehavior
import com.project.alm.MavenUtils
import java.util.ArrayList

import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEvent

def getSchemaBBDD(String appGar) {
	def exists = fileExists "${GlobalVars.SQL_CONFIG_DIRECTORY}${GlobalVars.SQL_BBDD_INFO}"
	
	if (exists) {
		def fileInfoYaml = readYaml file: "${GlobalVars.SQL_CONFIG_DIRECTORY}${GlobalVars.SQL_BBDD_INFO}"
		
		if (fileInfoYaml!=null && fileInfoYaml.get('config') != null && fileInfoYaml.get('config').get('schema') != null) {
			return fileInfoYaml.get('config').get('schema')
		}else {
			return appGar
		}
	}else {		
		return appGar
	}
}

def getAppBBDDType(String appGar) {
	def exists = fileExists "${GlobalVars.SQL_CONFIG_DIRECTORY}${GlobalVars.SQL_BBDD_INFO}"
	def defaultBBDDType = "DS"
	
	if (exists) {
		def fileInfoYaml = readYaml file: "${GlobalVars.SQL_CONFIG_DIRECTORY}${GlobalVars.SQL_BBDD_INFO}"
		
		if (fileInfoYaml!=null && fileInfoYaml.get('config') != null && fileInfoYaml.get('config').get('type') != null) {
			return fileInfoYaml.get('config').get('type')
		}else {
			return defaultBBDDType
		}
	}else {
		return defaultBBDDType
	}
	return defaultBBDDType
}


def addDefaultDefinition(String scriptSql, String appGar) {
	def tablesInScript = sh(returnStdout: true, script: "cat ${scriptSql} | grep -i ' table ' | grep -i 'create ' | awk '{ print \$3}'   || true")
	def grantsInScript = ""
	def synonimsInScript = ""
	
	def extendedDefinition = ""
	
	List tablesArray = Utilities.splitStringToList(tablesInScript, "NonExisting")
	
	def schemaBBDD=getSchemaBBDD(appGar)
	def appBBDDType=getAppBBDDType(appGar)
	
	printOpen("El esquema es ${schemaBBDD} el tipo de esquema es de ${appBBDDType}", EchoLevel.ALL)
		
	if (tablesArray.size() > 0 && tablesArray[0]!=null &&  tablesArray[0]!="" ) {
		printOpen("Analizando tablas ${tablesArray}", EchoLevel.ALL)
		def table = ""
	
		tablesArray.each {
			
			
			table = "${it}"
			table = table.replaceAll(';', '')
			table = table.replace("(", "")
			
			printOpen("La tabla a modificar es la siguiente ${table}", EchoLevel.ALL)
			
			grantsInScript = grantsInScript + "GRANT DELETE ON ${table} TO ${appBBDDType}${schemaBBDD.toUpperCase()}_ROL_BAT;\n"
			grantsInScript = grantsInScript + "GRANT INSERT ON ${table} TO ${appBBDDType}${schemaBBDD.toUpperCase()}_ROL_BAT;\n"
			grantsInScript = grantsInScript + "GRANT SELECT ON ${table} TO ${appBBDDType}${schemaBBDD.toUpperCase()}_ROL_BAT;\n"
			grantsInScript = grantsInScript + "GRANT UPDATE ON ${table} TO ${appBBDDType}${schemaBBDD.toUpperCase()}_ROL_BAT;\n"
			grantsInScript = grantsInScript + "GRANT DELETE ON ${table} TO ${appBBDDType}${schemaBBDD.toUpperCase()}_ROL_APP;\n"
			grantsInScript = grantsInScript + "GRANT INSERT ON ${table} TO ${appBBDDType}${schemaBBDD.toUpperCase()}_ROL_APP;\n"
			grantsInScript = grantsInScript + "GRANT SELECT ON ${table} TO ${appBBDDType}${schemaBBDD.toUpperCase()}_ROL_APP;\n"
			grantsInScript = grantsInScript + "GRANT UPDATE ON ${table} TO ${appBBDDType}${schemaBBDD.toUpperCase()}_ROL_APP;\n"			
			grantsInScript = grantsInScript + "GRANT SELECT ON ${table} TO ${appBBDDType}${schemaBBDD.toUpperCase()}_ROL_LEC;\n"			
			
			synonimsInScript = synonimsInScript + "CREATE OR REPLACE SYNONYM O${schemaBBDD.toUpperCase()}1.${table} FOR I${schemaBBDD.toUpperCase()}1.${table};\n"
			synonimsInScript = synonimsInScript + "CREATE OR REPLACE SYNONYM C${schemaBBDD.toUpperCase()}1.${table} FOR I${schemaBBDD.toUpperCase()}1.${table};\n"
			synonimsInScript = synonimsInScript + "CREATE OR REPLACE SYNONYM A${schemaBBDD.toUpperCase()}1.${table} FOR I${schemaBBDD.toUpperCase()}1.${table};\n"
			synonimsInScript = synonimsInScript + "CREATE OR REPLACE SYNONYM C${schemaBBDD.toUpperCase()}2.${table} FOR I${schemaBBDD.toUpperCase()}1.${table};\n"
			synonimsInScript = synonimsInScript + "CREATE OR REPLACE SYNONYM P${schemaBBDD.toUpperCase()}1.${table} FOR I${schemaBBDD.toUpperCase()}1.${table};\n"
		}
		
	}
	
	def viewsInScript = sh(returnStdout: true, script: "cat ${scriptSql} | grep -i 'view ' | grep -i 'create' | grep -vi 'replace' | grep -vi 'materialized' | awk '{ print \$3}'   || true")
	def viewsInScriptReplace = sh(returnStdout: true, script: "cat ${scriptSql} | grep -i 'view ' | grep -i 'create' | grep -i 'replace' | grep -vi 'materialized' | awk '{ print \$5}'   || true")
		
	def viewsInScriptMaterialized = sh(returnStdout: true, script: "cat ${scriptSql} | grep -i 'view ' | grep -i 'create' | grep -vi 'replace' | grep -i 'materialized' | grep -vi 'view log' | awk '{ print \$4}'   || true")
	def viewsInScriptReplaceMaterialized = sh(returnStdout: true, script: "cat ${scriptSql} | grep -i 'view ' | grep -i 'create' | grep -i 'replace' | grep -i 'materialized' | grep -vi 'view log' | awk '{ print \$6}'   || true")
	
	List elementsArray = Utilities.splitStringToList(viewsInScript, "NonExisting")
	List elementsArrayReplace = Utilities.splitStringToList(viewsInScriptReplace, "NonExisting")
	
	List elementsArrayMaterialized = Utilities.splitStringToList(viewsInScriptMaterialized, "NonExisting")
	List elementsArrayReplaceMaterialized = Utilities.splitStringToList(viewsInScriptReplaceMaterialized, "NonExisting")
	
	if (elementsArray==null || elementsArray[0]==null ||  elementsArray[0]=="") {
		elementsArray=new ArrayList()
	}
	if (elementsArrayReplace==null || elementsArrayReplace[0]==null ||  elementsArrayReplace[0]=="") {
		elementsArrayReplace=new ArrayList()
	}
	if (elementsArrayMaterialized==null || elementsArrayMaterialized[0]==null ||  elementsArrayMaterialized[0]=="") {
		elementsArrayMaterialized=new ArrayList()
	}
	if (elementsArrayReplaceMaterialized==null || elementsArrayReplaceMaterialized[0]==null ||  elementsArrayReplaceMaterialized[0]=="") {
		elementsArrayReplaceMaterialized=new ArrayList()
	}
	elementsArray.addAll(elementsArrayReplace)
	elementsArray.addAll(elementsArrayMaterialized)
	elementsArray.addAll(elementsArrayReplaceMaterialized)
	if (elementsArray.size() > 0 && elementsArray[0]!=null &&  elementsArray[0]!="" ) {
		printOpen("Analizando views", EchoLevel.INFO)
		def view = ""
		
		elementsArray.each {
			view = "${it}"
			view = view.replaceAll(';', '')
			grantsInScript = grantsInScript + "GRANT SELECT ON ${view} TO ${appBBDDType}${schemaBBDD.toUpperCase()}_ROL_BAT;\n"
			grantsInScript = grantsInScript + "GRANT SELECT ON ${view} TO ${appBBDDType}${schemaBBDD.toUpperCase()}_ROL_APP;\n"
			grantsInScript = grantsInScript + "GRANT SELECT ON ${view} TO ${appBBDDType}${schemaBBDD.toUpperCase()}_ROL_LEC;\n"			
			
			synonimsInScript = synonimsInScript + "CREATE OR REPLACE SYNONYM O${schemaBBDD.toUpperCase()}1.${view} FOR I${schemaBBDD.toUpperCase()}1.${view};\n"
			synonimsInScript = synonimsInScript + "CREATE OR REPLACE SYNONYM C${schemaBBDD.toUpperCase()}1.${view} FOR I${schemaBBDD.toUpperCase()}1.${view};\n"
			synonimsInScript = synonimsInScript + "CREATE OR REPLACE SYNONYM A${schemaBBDD.toUpperCase()}1.${view} FOR I${schemaBBDD.toUpperCase()}1.${view};\n"
			synonimsInScript = synonimsInScript + "CREATE OR REPLACE SYNONYM C${schemaBBDD.toUpperCase()}2.${view} FOR I${schemaBBDD.toUpperCase()}1.${view};\n"
			synonimsInScript = synonimsInScript + "CREATE OR REPLACE SYNONYM P${schemaBBDD.toUpperCase()}1.${view} FOR I${schemaBBDD.toUpperCase()}1.${view};\n"
		}		
	} else {
		printOpen("No hay views", EchoLevel.INFO)
	}
	
	def procedureInScript = sh(returnStdout: true, script: "cat ${scriptSql} | grep -iE 'procedure |function |package ' | grep -i 'create ' | grep -vi 'replace ' | awk '{ print \$3}'   || true")
	def procedureInScriptReplace = sh(returnStdout: true, script: "cat ${scriptSql} | grep -iE 'procedure |function |package ' | grep -i 'create ' | grep -i 'replace ' | awk '{ print \$5}'   || true")
	
	elementsArray = Utilities.splitStringToList(procedureInScript, "NonExisting")
	elementsArrayReplace = Utilities.splitStringToList(procedureInScriptReplace, "NonExisting")
	if (elementsArray==null || elementsArray[0]==null ||  elementsArray[0]=="") {
		elementsArray=new ArrayList()
	}
	if (elementsArrayReplace==null || elementsArrayReplace[0]==null ||  elementsArrayReplace[0]=="") {
		elementsArrayReplace=new ArrayList()
	}
	elementsArray.addAll(elementsArrayReplace)
	
	if (elementsArray.size() > 0 && elementsArray[0]!=null &&  elementsArray[0]!="" ) {
		printOpen("Analizando views", EchoLevel.ALL)
		def proc = ""
		
		elementsArray.each {
			proc = "${it}"
			proc = proc.replaceAll(';', '')
			grantsInScript = grantsInScript + "GRANT EXECUTE ON ${proc} TO BE${appGar.toUpperCase()}_ROL_BAT;\n"			
			synonimsInScript = synonimsInScript + "CREATE OR REPLACE SYNONYM P${appGar.toUpperCase()}1.${proc} FOR I${appGar.toUpperCase()}1.${proc};\n"
		}
	}
	
	def sequenceInScript = sh(returnStdout: true, script: "cat ${scriptSql} | grep -iE 'sequence ' | grep -i 'create' | grep -vi 'replace' | awk '{ print \$3}'   || true")
	def sequenceInScriptReplace = sh(returnStdout: true, script: "cat ${scriptSql} | grep -iE 'sequence ' | grep -i 'create' | grep -i 'replace' | awk '{ print \$5}'   || true")
	
	elementsArray = Utilities.splitStringToList(sequenceInScript, "NonExisting")
	elementsArrayReplace = Utilities.splitStringToList(sequenceInScript, "NonExisting")
	if (elementsArray==null || elementsArray[0]==null ||  elementsArray[0]=="") {
		elementsArray=new ArrayList()
	}
	if (elementsArrayReplace==null || elementsArrayReplace[0]==null ||  elementsArrayReplace[0]=="") {
		elementsArrayReplace=new ArrayList()
	}
	elementsArray.addAll(elementsArrayReplace)
	
	if (elementsArray.size() > 0 && elementsArray[0]!=null &&  elementsArray[0]!="" ) {
		printOpen("Analizando views", EchoLevel.ALL)
		def sequence = ""
		
		elementsArray.each {
			sequence = "${it}"
			sequence = sequence.replaceAll(';', '')
			grantsInScript = grantsInScript + "GRANT SELECT ON ${sequence} TO ${appBBDDType}${schemaBBDD.toUpperCase()}_ROL_BAT;\n"
			grantsInScript = grantsInScript + "GRANT SELECT ON ${sequence} TO ${appBBDDType}${schemaBBDD.toUpperCase()}_ROL_APP;\n"
			synonimsInScript = synonimsInScript + "CREATE OR REPLACE SYNONYM P${schemaBBDD.toUpperCase()}1.${sequence} FOR I${schemaBBDD.toUpperCase()}1.${sequence};\n"
			synonimsInScript = synonimsInScript + "CREATE OR REPLACE SYNONYM O${schemaBBDD.toUpperCase()}1.${sequence} FOR I${schemaBBDD.toUpperCase()}1.${sequence};\n"
			synonimsInScript = synonimsInScript + "CREATE OR REPLACE SYNONYM A${schemaBBDD.toUpperCase()}1.${sequence} FOR I${schemaBBDD.toUpperCase()}1.${sequence};\n"
		}
	}
	
	return grantsInScript + synonimsInScript
}

def containsRestrictedElements(String scriptSql) {
	return containsRestrictedElements(scriptSql, false)
}

def containsRestrictedElements(String scriptSql, boolean isValidation) {
	
	printOpen("${env.BBDD_MANUAL_VALIDATION} se debe aplicar la validacion manual ${scriptSql}", EchoLevel.ALL)
	if (env.BBDD_MANUAL_VALIDATION!=null) {
		printOpen("Tenemos que obligar a las validaciones manuales sea o no un script restringido", EchoLevel.ALL)
		return "Está activada la obligación de validación manual"
	}
	
	def triggersInScript = sh(returnStdout: true, script: "grep -i ' trigger ' ${scriptSql} | grep -i 'create ' | awk '{ print \$3}'   || true")
	printOpen("Los triggers del script son ${triggersInScript}", EchoLevel.ALL)
	List triggersArray = Utilities.splitStringToList(triggersInScript, "NonExisting")
	
	printOpen("Tamaño conjunto triggers size ${triggersArray.size()}", EchoLevel.ALL)
	printOpen("Tamaño conjunto triggers array  ${triggersArray}", EchoLevel.ALL)
	
	String resultado = ""
	

	if (triggersArray.size() > 0 && triggersArray[0]!=null &&  triggersArray[0]!="") {
		//Uix tenemos un trigger esto no puede ser promocionado
		resultado = "<p>No puede ser promocionado contiene triggers. Debera ser validado.</p> "
	}
	def blobsInScript = sh(returnStdout: true, script: "grep -i ' blob' ${scriptSql} | awk '{ print \$1}'   || true")
	printOpen("Los blobs del script son ${blobsInScript}", EchoLevel.ALL)
	List blobsArray = Utilities.splitStringToList(blobsInScript, "NonExisting")
	
	printOpen("Tamaño conjunto blobs size ${blobsArray.size()}", EchoLevel.ALL)
	printOpen("Tamaño conjunto blobs array  ${blobsArray}", EchoLevel.ALL)

	if (blobsArray.size() > 0 && blobsArray[0]!=null &&  blobsArray[0]!="") {
		//Uix tenemos un trigger esto no puede ser promocionado
		resultado = resultado + "<p>No puede ser promocionado contiene blobs. Debera ser validado.</p> "
	}
	
	def clobsInScript = sh(returnStdout: true, script: "grep -i ' clob' ${scriptSql} | awk '{ print \$1}'   || true")
	printOpen("Los clobs del script son ${blobsInScript}", EchoLevel.ALL)
	List clobsArray = Utilities.splitStringToList(clobsInScript, "NonExisting")
	
	printOpen("Tamaño conjunto clobs size ${clobsArray.size()}", EchoLevel.ALL)
	printOpen("Tamaño conjunto clobs array  ${clobsArray}", EchoLevel.ALL)

	if (clobsArray.size() > 0 && clobsArray[0]!=null &&  clobsArray[0]!="") {
		//Uix tenemos un trigger esto no puede ser promocionado
		resultado = resultado + "<p>No puede ser promocionado contiene clobs. Debera ser validado.</p>"
	}
	
	if (resultado!="") {
		//def circuitoValidacion = URLEncoder.encode("https://confluence.cloud.digitalscale.es/confluence/pages/viewpage.action?pageId=213801742", "UTF-8")
		def circuitoValidacion = "https://confluence.cloud.digitalscale.es/confluence/pages/viewpage.action?pageId=213801742"
	    if (isValidation) {
			resultado = "<p><pre> Este script contiene <b>elementos restringidos</b>.\n Cuando seleccione la opción de Validate And Generate SQL scripts no se podrá promocionar automáticamente sino que deberá requerir acción manual por parte de terceros.\n Valore si los elementos restingidos son necesarios. </pre></p>"
	    }else{
			resultado = "<p><pre>"+resultado + "<b>Siga el procedimiento documentado para proceder a la validacion ${circuitoValidacion}</b></pre></p>"
	    }	
		return resultado
	}else {
		return null
	}
	
}

def containsForbiddenElements(String scriptSql) {	
	
	def dblinksInScript = sh(returnStdout: true, script: "grep -i ' database ' ${scriptSql} | grep -i ' link ' | grep -i 'create' | awk '{ print \$3}'   || true")
	printOpen("Los DBLINKS del script son ${dblinksInScript}", EchoLevel.ALL)
	List dblinkssArray = Utilities.splitStringToList(dblinksInScript, "NonExisting")
	
	printOpen("Tamaño conjunto dblinks size ${dblinkssArray.size()}", EchoLevel.ALL)
	printOpen("Tamaño conjunto dblinks array  ${dblinkssArray}", EchoLevel.ALL)

	if (dblinkssArray.size() > 0 && dblinkssArray[0]!=null &&  dblinkssArray[0]!="") {
		//Uix tenemos un trigger esto no puede ser promocionado
		return "No puede ser promocionado contiene database links, son elementos prohibidos."
	}
	
	//Validamos grants programaticos
	def grantsInScript = sh(returnStdout: true, script: "grep -i 'grant ' ${scriptSql} | grep -i ' to ' | awk '{ print \$3}'   || true")
	printOpen("Los GRANTS del script son ${grantsInScript}", EchoLevel.ALL)
	List grantsArray = Utilities.splitStringToList(grantsInScript, "NonExisting")
	
	printOpen("Tamaño conjunto grants size ${grantsArray.size()}", EchoLevel.ALL)
	printOpen("Tamaño conjunto grants array  ${grantsArray}", EchoLevel.ALL)

	if (grantsArray.size() > 0 && grantsArray[0]!=null &&  grantsArray[0]!="") {
		//Uix tenemos un trigger esto no puede ser promocionado
		return "No puede ser promocionado contiene grants, son elementos prohibidos."
	}else {
		//Esta bueno no debe ser parada
		return null
	}
	
	
}

def commitChangesToGit(PomXmlStructure artifactPom, PipelineData pipeline) {
	//Tenemos que subir las modificaciones al git otra vez
	pushRepoWithMessage(artifactPom, pipeline, "${GlobalVars.GIT_TAG_CI_PUSH} Added script to the database","." )
}

def isPush() {
	PipelineBehavior pipelineBeha=PipelineBehavior.valueOfSubType(env.pipelineBehavior)
	boolean result=false
	if (PipelineBehavior.PUSH_OPENED_MR.equalsName(env.pipelineBehavior) || 
		PipelineBehavior.PUSH_NO_MR.equalsName(env.pipelineBehavior))  result=true
	printOpen("The result is ${result} The behaviour ${env.pipelineBehavior} ", EchoLevel.ALL)
	return result
}

def isMergeOpen(def onlyNotMR = false, def includeFirstMR = false) {
	PipelineBehavior pipelineBeha=PipelineBehavior.valueOfSubType(env.pipelineBehavior)
	
	
	if (onlyNotMR) {
		 if (PipelineBehavior.NOT_FIRST_MR== pipelineBeha) { 
			 return true
		 }else {
			if (includeFirstMR) {
				if (PipelineBehavior.FIRST_MR== pipelineBeha)	return true
			}
			return false			
		 }
	}else {
		if (PipelineBehavior.NOT_FIRST_MR == pipelineBeha || PipelineBehavior.PUSH_OPENED_MR== pipelineBeha) return true
		else return false
	}
}

def sendEmailToAT(PomXmlStructure artifactPom, PipelineData pipeline,def erroresValidacion) {

	def app = artifactPom.getApp(pipeline.garArtifactType)
	def date = new Date()
	String datePart = date.format("dd/MM/yyyy")
	def to = "arquitectura.tecnica@project.com"
	
	String bodyEmail = "<p>Buenos dias, </p><p>La app ${app} va a requerir de una validación en su rama ${pipeline.branchStructure.branchName} en la fecha ${datePart}.</p>"+
	                   "<p>Tienen la merge request en la siguiente url del git. ${pipeline.gitUrl}</p>"+
	                   "<p>Elementos restringidos ${erroresValidacion}</p><p>Les hemos indicado el procedimiento, para que se proceda a validar su Merge request</p><p>Saludos.</p>"
		
	emailext(body: "${bodyEmail}"
			, mimeType: 'text/html'
			, recipientProviders: [[$class: 'DevelopersRecipientProvider']]
			, replyTo: ''
			, from: "${GlobalVars.EMAIL_FROM_ALM}"			
			, to: "${to}"
			, subject: "[Alm3-SRV-Validacion BBDD] Validación script BBDD ${app} ${datePart} ")
}

def call(PomXmlStructure artifactPom, PipelineData pipeline) {
	
	String result=""
	String groupContract=artifactPom.groupId+".bbdd"
	String version= (artifactPom.artifactVersion-"-SNAPSHOT")+"."+pipeline.branchStructure.featureNumber+"-SNAPSHOT"
	
    printOpen("generate Sql Script ${env.pipelineBehavior} comentario ${pipeline.commitLog}", EchoLevel.ALL)
	
	def exists = fileExists "${GlobalVars.SQL_INPUT_DIRECTORY}${GlobalVars.SQL_SCRIPT_FILENAME}" 
	boolean isPushVar=isPush()
	
	printOpen("generate Sql Script ${env.pipelineBehavior} comentario ${pipeline.commitLog} existeElFicher ${exists} isPush ${isPushVar}", EchoLevel.ALL)
	
	if (isMergeOpen(true,true)) {
		def author=getMergeAuthor(artifactPom,pipeline)
		if (author!=null && author.name!=GlobalVars.JENKINS_GIT_USER) {
			throw new Exception("Merge incorrecta. Sólo se permiten Merges automáticas. Este usuario (${author.name}) no puede abrir MR sobre esta rama")
		}
	}
	
	if (exists && (((pipeline.commitLog.indexOf('deploy') !=-1 || pipeline.commitLog.indexOf('validate') !=-1)  && isPush()) || isMergeOpen(true) )) {
		
		String tempDir = CopyFileToTemp(GlobalVars.SQL_INPUT_DIRECTORY + GlobalVars.SQL_SCRIPT_FILENAME, GlobalVars.SQL_SCRIPT_FILENAME)
		
		String sanitizedTempDir = tempDir.replace(' ', '\\ ')
		
		String dirContent = sh(script: "ls -la ${sanitizedTempDir}", returnStdout: true)
        printOpen("Files in ${sanitizedTempDir}:\n${dirContent}", EchoLevel.DEBUG)
		
		sh "mkdir -p ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}"
	
		//Generate Output
		def scriptSqlInput = readFile(file: "${sanitizedTempDir}/${GlobalVars.SQL_SCRIPT_FILENAME}")
	
		printOpen("El script es de ${scriptSqlInput}", EchoLevel.ALL)
		
		def forbiddenElements=containsForbiddenElements("${sanitizedTempDir}/${GlobalVars.SQL_SCRIPT_FILENAME}")
		
		if (forbiddenElements!=null) {
			throw new Exception	("Elementos incorrectos. No puede ser desplegado un script con estos elementos ${forbiddenElements}")
		}
		
		def scriptSqlOutput = "--liquibase formatted sql\n--changeset ${artifactPom.artifactName}:${pipeline.branchStructure.featureNumber}.${scriptSqlInput.hashCode()}\n\n${scriptSqlInput}"
		
		boolean existsArtifact=true
	
		printOpen("El script es de ${scriptSqlOutput}", EchoLevel.ALL)
		def existOutput = fileExists "${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/${GlobalVars.RESOURCE_PATH}"
		if (existOutput) {
			sh "rm ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/${GlobalVars.RESOURCE_PATH}/* || true"
			printOpen("Eliminamos contenido antiguo", EchoLevel.ALL)
		}	 
		sh "mkdir -p ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/${GlobalVars.RESOURCE_PATH}"
		//Creamos el directorio de output
		sh "mkdir -p ${GlobalVars.SQL_OUTPUT_DIRECTORY}"
		//Copiamos los scripts anteriores de esta feature donde toca
		
        sh "cp ${GlobalVars.SQL_OUTPUT_DIRECTORY}*_${pipeline.branchStructure.featureNumber}.sql ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/${GlobalVars.RESOURCE_PATH}/ 2>/dev/null || :"
		
		scriptSqlOutput=scriptSqlOutput+"\n"+addDefaultDefinition("${sanitizedTempDir}/${GlobalVars.SQL_SCRIPT_FILENAME}",artifactPom.getApp(pipeline.garArtifactType))

				
		String numberOfFile=sh(returnStdout: true, script: "ls ${GlobalVars.SQL_OUTPUT_DIRECTORY}*_${pipeline.branchStructure.featureNumber}.sql | wc -l")
		numberOfFile=numberOfFile.replace('\n', '')
		printOpen("The number of files ${numberOfFile.padLeft(4,'0')}", EchoLevel.ALL)
		def changeSetId = artifactPom.artifactName+"SQL"+numberOfFile.padLeft(4,'0')+scriptSqlInput.hashCode()+ "_"+ pipeline.branchStructure.featureNumber+".sql"
			
		printOpen("El script es de ${scriptSqlOutput}", EchoLevel.ALL)
		
		writeFile file: "${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/${GlobalVars.RESOURCE_PATH}/${changeSetId}", text: "${scriptSqlOutput}"
		writeFile file: "${GlobalVars.SQL_OUTPUT_DIRECTORY}/${changeSetId}", text: "${scriptSqlOutput}"
		
		dirContent = sh(script: "ls -la .", returnStdout: true)
        printOpen("Directory content:\n${dirContent}", EchoLevel.DEBUG)
		
		sh "rm ${GlobalVars.SQL_INPUT_DIRECTORY}/${GlobalVars.SQL_SCRIPT_FILENAME}"
		
		//sh "jar cf ${sanitizedTempDir}/${artifactPom.artifactMicro}.${pipeline.branchStructure.featureNumber}.${artifactPom.artifactVersion}.jar ${sanitizedTempDir}/bbddscript"

        String pathToChangeSetPom = CopyGlobalLibraryScript(GlobalVars.LIQUIBASE_CHANGE_SET_POM_FILENAME, "${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}")

        sh "sed -i 's/#ARTIFACT#/${artifactPom.artifactName}/g' ${pathToChangeSetPom}"
        sh "sed -i 's/#VERSION#/${version}/g' ${pathToChangeSetPom}"
        sh "sed -i 's/#GROUP#/${groupContract}/g' ${pathToChangeSetPom}"
        sh "sed -i 's/#ARCHVERSION#/${artifactPom.archVersion}/g' ${pathToChangeSetPom}"

        sh "cat ${pathToChangeSetPom}"

        def cmd = "mvn clean deploy -Dmaven.install.skip <Default_Maven_Settings> -f  ${pathToChangeSetPom}"
        boolean weHaveToGenerateOpenApiClasses =
            WorkspaceUtils.isThereASwaggerContract(this, artifactPom) &&
                ! WorkspaceUtils.areSwaggerContractClassesGenerated(this, artifactPom)
        if ( ! weHaveToGenerateOpenApiClasses ) cmd += " -Dcodegen.skip=true "

		def deploymentRepo = MavenUtils.getDeploymentRepository(version)

		runMavenGoalWithRetries(artifactPom, pipeline, cmd, [
			forceDeploymentRepo: deploymentRepo,
			kpiAlmEvent: new KpiAlmEvent(
				artifactPom, pipeline,
				KpiAlmEventStage.UNDEFINED,
				KpiAlmEventOperation.MVN_DEPLOY_SQL_SCRIPT_NEXUS)
		])

        //Antes de desplegar tenemos que borrar

		printOpen("Domain: "  + pipeline.domain, EchoLevel.INFO)
		printOpen("VCAPS: "  + pipeline.vcapsServiceIds, EchoLevel.INFO)		

        result = deployScriptToKubernetes(artifactPom,pipeline,groupContract,artifactPom.artifactName,version)

		//Deploy del artefacto
		dirContent = sh(script: "ls -la ${sanitizedTempDir}", returnStdout: true)
        printOpen("Files at ${sanitizedTempDir}:\n${dirContent}", EchoLevel.DEBUG)
		
		dirContent = sh(script: "ls -la ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}", returnStdout: true)
        printOpen("Files at ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}:\n${dirContent}", EchoLevel.DEBUG)
		
		sh "rm -rf ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}/*"
		sh "rmdir ${sanitizedTempDir}/${GlobalVars.SQL_GENERATOR_TEMP_DIRECTORY}"
		
		
		sh "git status"
		
		commitChangesToGit(artifactPom,pipeline)
	}
	
	if (exists && pipeline.commitLog.indexOf('release') !=-1) throw new Exception("No puede promocionar un script y hacer la release a la vez. Solo se ha promocionado el script")
	
	if (pipeline.commitLog.indexOf('validate')!=-1 && isPush()) {
		result = containsRestrictedElements("${GlobalVars.SQL_INPUT_DIRECTORY}${GlobalVars.SQL_SCRIPT_FILENAME}",true)
		if (result!=null) {
			 throw new Exception(result)
		}else {
			result = containsRestrictedElements("${GlobalVars.SQL_OUTPUT_DIRECTORY}*_${pipeline.branchStructure.featureNumber}.sql",true)
			if (result!=null) {
				throw new Exception(result)
			}
			return "Script correcto"
		}
	}else if (pipeline.commitLog.indexOf('release')!=-1 && isPush()) {

		
		result = containsRestrictedElements("${GlobalVars.SQL_INPUT_DIRECTORY}${GlobalVars.SQL_SCRIPT_FILENAME}")
		printOpen("Restricted elements ${result}", EchoLevel.ALL)
		if (result==null) {
			//Esto puede promocionar intentaremos generar una MR validada y con el merge automatico
			result = containsRestrictedElements("${GlobalVars.SQL_OUTPUT_DIRECTORY}*_${pipeline.branchStructure.featureNumber}.sql")
			printOpen("Restricted elements ${result}", EchoLevel.ALL)
			if (result==null) {
				if (!isMergeOpen()) {
					
					mergeRequestToMaster(pipeline, artifactPom, 'master', true, true, false, "ChangeSet can be promoted to the next environment", "The changeset is OK ${version}")
					result="Script correcto, paquete preparado para ser promocionado al siguiente entorno"
				}else {
					printOpen("Tenemos MR abierta solo se deberia validar solo que el fichero sea correcto en caso contrario petar", EchoLevel.ALL)
					result="Script correcto pero con MR abierta, el equipo responsable puede aceptar la validación"
				}
			}else{
				if (!isMergeOpen()) {
					mergeRequestToMaster(pipeline, artifactPom, 'master', false, false,true, result, "AT Teams must validate this MR of the changeset ${version}" )
					sendEmailToAT(artifactPom,pipeline,result)
					throw new Exception	("Elementos incorrectos. Debe ser aprovado ${result}")				
				}else {
					printOpen("Tenemos MR abierta solo se deberia validar solo que el fichero sea correcto en caso contrario petar", EchoLevel.ALL)
					result="Script correcto pero con MR abierta, el equipo responsable puede aceptar la validación"
				}
			}
		}else {
			sendEmailToAT(artifactPom,pipeline,result)
			//Esto no puede ser promocionado, deberemos generar una MR pendiente de validar			
			mergeRequestToMaster(pipeline, artifactPom, 'master', false, false, true, result , "AT Teams must validate this MR of the changeset ${version}")
			throw new Exception	("Elementos incorrectos. Debe ser aprovado ${result}")
		}
	}

	return result
}
