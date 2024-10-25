import com.caixabank.absis3.*
import com.caixabank.absis3.JavaVersionType

def call(PomXmlStructure pomXml, PipelineData pipeline) {

    printOpen("Sonar Scan the Workspace!!!. ${pipeline.branchStructure.branchType}", EchoLevel.DEBUG)

    withSonarQubeEnv('sonarqube') {

        StringBuilder logmessage = new StringBuilder()
        logmessage.append("allowed list of artifact types to execute sonar-scan:\n")
        for (ArtifactSubType item : GlobalVars.ALLOWED_SONAR_TYPES) {
            logmessage.append("- ${item.toString()}\n")
        }
        logmessage.append("current artifact type is ${pomXml.artifactSubType.toString()}\n")

        printOpen("${logmessage.toString()}", EchoLevel.DEBUG)

        String sonarProjectName="${pipeline.garArtifactType.getGarName()}.${pomXml.getSpringAppName()}"

        printOpen("sonarProjectName is ${sonarProjectName}", EchoLevel.DEBUG)

        if (GlobalVars.ALLOWED_SONAR_TYPES.contains(pomXml.artifactSubType)) {

            printOpen("AppType is allowed to execute sonar scan. Sending data to Sonar...", EchoLevel.INFO)
			
            //def cmd = "mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Default_Maven_Settings> org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar -Dsonar.projectKey=${sonarProjectName} -Dsonar.projectName=${sonarProjectName} "
			def cmd = "mvn  <Default_Maven_Settings> org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar -Dsonar.projectKey=${sonarProjectName} -Dsonar.projectName=${sonarProjectName} "
			boolean weHaveToGenerateOpenApiClasses =
                WorkspaceUtils.isThereASwaggerContract(this, pomXml) &&
                    ! WorkspaceUtils.areSwaggerContractClassesGenerated(this, pomXml)
            if ( ! weHaveToGenerateOpenApiClasses ) cmd += " -Dcodegen.skip=true "

            runMavenGoalWithRetries(pomXml, pipeline, cmd, [
                archiveLogIfMvnDurationExceeds: 10,
                kpiAlmEvent: new KpiAlmEvent(
                    pomXml, pipeline,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.MVN_SONAR_SCAN)
            ])

            sh "cat ${pomXml.getRouteToSonarReportTask()}"
            def props = readProperties file: "${pomXml.getRouteToSonarReportTask()}"
            printOpen("properties=${props}", EchoLevel.DEBUG)
            pipeline.testData = props['dashboardUrl']
            printOpen("pipeline.testData=${pipeline.testData}", EchoLevel.DEBUG)
            
            printOpen("Sonar data has been send.", EchoLevel.INFO)

        } else {
            printOpen("AppType is NOT allowed to execute sonar scan. Skipping sonar scan...", EchoLevel.INFO)
        }

    }

}
