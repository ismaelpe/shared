import com.project.alm.*

def call(PomXmlStructure pomXml, PipelineData pipeline) {
    printOpen("Deploying Artifact from Workspace to Nexus!!!! ${pipeline.branchStructure.branchType}", EchoLevel.ALL)
    //Esto se tiene que desplegar a menos que tenga la sample app
    def mvnParameters = ""

    //Tenemos que detectar si existe una sample app dentro del plugin
    if (pomXml.artifactSampleApp != "") {
        mvnParameters = " -pl !${pomXml.artifactSampleApp} "
    }

    //La sample app no tiene que ser desplegada
    //Se tiene que recoger el id del build solo si es SNAPSHOT
    def commitLog = null
    //Si se trata de un micro y no una libreria entonces generamos los jars para tests de integracion, tienen sufijo test.jar

    boolean testJarsApplyAsPerMicroType = pomXml.artifactSubType == ArtifactSubType.MICRO_APP || pomXml.artifactSubType == ArtifactSubType.MICRO_ARCH || pomXml.artifactSubType == ArtifactSubType.SAMPLE_APP
    boolean notWhitelisted = ! env.ALM_SERVICES_IT_TEST_JARS_GENERATION_WHITELIST.contains(pomXml.artifactName)

    if (testJarsApplyAsPerMicroType && notWhitelisted) {
        mvnParameters = " ${mvnParameters} -P generate-alm-it-test-jars "
        printOpen("Deploying with tests jars", EchoLevel.ALL)
    } else {
        printOpen("Deploying without tests jars", EchoLevel.ALL)
    }

    def archVersion = ""
    def archArtifactId = ""
	printOpen("The skipMode is ${pipeline.getExecutionMode().skipTest()}", EchoLevel.ALL)
	
    if (!pomXml.isArchArtifact()) {
        archVersion = " -DarchVersion=${pomXml.archVersion} "
        archArtifactId = " -DarchArtifactId=${GlobalVars.ARCH_ARTIFACT} "
    }

    /* Cuidado, deben mantenerse los dos parametros "-Dmaven.test.skip=true -DskipTests" para evitar la compilacion de tests pero tambien
       la ejecucion en caso de realizarse con un target ya generado anteriormente */
    //def cmd = "mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Only_Maven_Settings> ${mvnParameters} ${archVersion} ${archArtifactId} clean deploy -Dmaven.install.skip -Dskip-generate-static-docs=${pipeline.getExecutionMode().skipJavadoc()} -Dmaven.test.skip=${pipeline.getExecutionMode().skipTest()} -DskipTests=${pipeline.getExecutionMode().skipTest()}"
	def cmd = "mvn <Only_Maven_Settings> ${mvnParameters} ${archVersion} ${archArtifactId} clean deploy -Dmaven.install.skip -Dskip-generate-static-docs=${pipeline.getExecutionMode().skipJavadoc()} -Dmaven.test.skip=${pipeline.getExecutionMode().skipTest()} -DskipTests=${pipeline.getExecutionMode().skipTest()}"
    
	boolean weHaveToGenerateOpenApiClasses =
        WorkspaceUtils.isThereASwaggerContract(this, pomXml) &&
            ! WorkspaceUtils.areSwaggerContractClassesGenerated(this, pomXml)
    if ( ! weHaveToGenerateOpenApiClasses ) cmd += " -Dcodegen.skip=true "

    commitLog = runMavenGoalWithRetries(pomXml, pipeline, cmd, [
        archiveLogIfMvnDurationExceeds: 40,
        kpiAlmEvent: new KpiAlmEvent(
            pomXml, pipeline,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.MVN_BUILD_WORKSPACE_DEPLOY_NEXUS)
    ])

	if (pipeline.branchStructure.branchType != BranchType.FEATURE) {
		def artifactDeployedOnNexus = NexusUtils.extractArtifactsFromLog(commitLog)
		
			if (artifactDeployedOnNexus != null) {
		
				pipeline.routesToNexus = artifactDeployedOnNexus
		
				artifactDeployedOnNexus.each {
					printOpen("${it}", EchoLevel.ALL)
				}
		
				if (pomXml.isSNAPSHOT()) {
					//Requerimos el build id
					pipeline.buildCode = NexusUtils.getBuildId(artifactDeployedOnNexus, pomXml.artifactName + '-', pomXml.getArtifactVersionWithoutQualifier() + '-')
				}
				printOpen("El buildCode es ${pipeline.buildCode}", EchoLevel.ALL)
		}
	}	

}
