import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.MavenGoalExecutionException
import com.caixabank.absis3.MavenGoalExecutionFailureError
import com.caixabank.absis3.MavenGoalExecutionFailureErrorDecoder
import com.caixabank.absis3.MavenUtils
import com.caixabank.absis3.Utilities

def call(String mvnCommand, Map parameters = [:]) {

	try {

		boolean printLogAlways = parameters.get("printLogAlways", false)
		boolean printLogIfFails = parameters.get("printLogIfFails", true)
		boolean showOnlyMvnErrorsInLog = parameters.get("showOnlyMvnErrorsInLog", true)
		Integer showOnlyLastNLinesInLog = parameters.get("showOnlyLastNLinesInLog")
		//String maven_settings = parameters.get("withThisGlobalMavenSettings", "absis3-maven-settings-with-singulares")
		String maven_settings = GlobalVars.MVN_DEFAULT_SETTINGS
		
		configFileProvider([configFile(fileId: maven_settings, variable: 'MAVEN_SETTINGS')]) {

            String currentDate = Utilities.getActualDate("yyyyMMddHHmmss")
            String outputFilename = "${env.WORKSPACE}@tmp/pipelineLogs/${currentDate}mavenGoalExecution.log"

			def cmdWithSettings = mvnCommand.replace("<Default_Maven_Settings>", "-s $MAVEN_SETTINGS -B ${GlobalVars.GLOBAL_MVN_PARAMS}")
			cmdWithSettings = cmdWithSettings.replace("<Only_Maven_Settings>", "-s $MAVEN_SETTINGS -B")

            writeFile file: outputFilename, text: "Maven command:\n\n${cmdWithSettings}\n\nLog:\n\n"

			Integer status = sh(returnStatus: true, script: "#!/bin/bash -xe\n(set -o pipefail && ${cmdWithSettings} >> ${outputFilename})")

			String fullLog = readFile(outputFilename)

			if(printLogAlways) {

				def printableLog = fullLog
				if (showOnlyLastNLinesInLog != null) {
					printableLog = MavenUtils.reduceLogToNLines(printableLog, showOnlyLastNLinesInLog)
				}
				printOpen("Execution of maven command \nOutput:\n\n${printableLog}", EchoLevel.DEBUG)

			} else if (status) {

				if (printLogIfFails) {
					def printableLog = fullLog
					if (showOnlyLastNLinesInLog != null) {
						printableLog = MavenUtils.reduceLogToNLines(printableLog, showOnlyLastNLinesInLog)
					}
					if (showOnlyMvnErrorsInLog) {
						printableLog = MavenUtils.reduceLogToMvnErrorLines(printableLog)
					}
					printOpen("Execution of maven command has failed\nOutput:\n\n${printableLog}", EchoLevel.ERROR)
				}

				MavenGoalExecutionFailureError error = MavenGoalExecutionFailureErrorDecoder.getErrorFromLog(fullLog, status)
				throw new MavenGoalExecutionException(error)
			}
			return fullLog
		}

	} catch(err) {
		printOpen("The execution of the maven command has failed due to an unexpected exception:\n\n${Utilities.prettyException(err, false)}", EchoLevel.ERROR)
		throw err
	}
}
