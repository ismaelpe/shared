import com.project.alm.*

/**
 * Copia los ficheros de configuración existentes en el path <pre>src/main/resources</pre> en su carpeta correspondiente del repositorio de configuración
 * según si es microservicio de arquitectura o aplicativo
 *
 * Copia todos los ficheros que comiencen por <pre>application</pre> excepto el que contenga el profile <pre>standalone</pre>.
 *
 * Procesa tanto los microsevicios simples como los de tipo agregador
 *
 * @param pomXml info del pom.xml
 * @param pipeline info de la pipeline
 * @return void
 */
def call(PomXmlStructure pomXml, PipelineData pipeline, ICPStateUtility icpStateUtility) {

	printOpen("before runRemoteIT,remoteITOk is : ${pipeline.pipelineStructure.resultPipelineData.remoteITOk}", EchoLevel.ALL)
	printOpen("icpStateUtility : ${icpStateUtility}", EchoLevel.ALL)
	
	if (icpStateUtility!=null && icpStateUtility.icpDeployStructure!=null) {
		//String artifactId = BmxUtilities.calculateRoute(pomXml, pipeline.branchStructure)
		String artifactId = BmxUtilities.calculateArtifactId(pomXml,pipeline.branchStructure)
	
		DeployStructure deployStructure=pipeline.bmxStructure.getDeployStructure(GlobalVars.BMX_CD1)
		
		//Si es feature tenemos que poner la ruta calculada
		//Se arch tenems que poner la ruta delante
		
		String pathToMicro=icpStateUtility.icpDeployStructure.getSuffixedComponentName().replace("<componentName>", artifactId)
		
		if (pipeline.branchStructure.branchType == BranchType.FEATURE) {
			pathToMicro=icpStateUtility.pathFeature
		}else {
			if (icpStateUtility.sampleAppFlag) pathToMicro=BmxUtilities.calculateArtifactId(pomXml,pipeline.branchStructure,true).toLowerCase()
		}
		
		if (pomXml.isArchProject()) {
			pathToMicro="arch-service/"+pathToMicro
		}
		
		String icpDistCenter="ALL"
		if (pipeline.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1) {
			icpDistCenter="AZ1"
        } else if (pipeline.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2) {
			icpDistCenter="AZ2"
        }
        
        // Start: Check Centers Avaiability
        def az1IsUp = validateMicroIsUp(icpStateUtility.icpDeployStructure.getUrlActuatorPrefixTesting() + icpStateUtility.icpDeployStructure.getUrlSuffixTesting("AZ1"))
        def az2IsUp = validateMicroIsUp(icpStateUtility.icpDeployStructure.getUrlActuatorPrefixTesting() + icpStateUtility.icpDeployStructure.getUrlSuffixTesting("AZ2"))
        
        def icpDistCenterForTest = icpDistCenter
        if (icpDistCenter == "ALL") {
            if (az1IsUp) {
                printOpen("We're going to run the integration tests against cluster 1", EchoLevel.INFO)
                icpDistCenter = "AZ1"
            } else {
                if (az2IsUp) {
                    printOpen("We're going to run the integration tests against cluster 2", EchoLevel.INFO)
                    icpDistCenter = "AZ2"
                } else {
                    throwExceptionAndOpenMaximoIfApplicable(new Exception("${GlobalVars.ICP_ERROR_DEPLOY_KUBERNETES_DISABLED}"), pipeline, pomXml, [:])
                }
            }
        } else if (icpDistCenter == "AZ1" && !az1IsUp) {
            throwExceptionAndOpenMaximoIfApplicable(new Exception("${GlobalVars.ICP_ERROR_DEPLOY_ON_MITIGATED_CENTER}"), pipeline, pomXml, [:])
        } else if (icpDistCenter == "AZ2" && !az2IsUp) {
            throwExceptionAndOpenMaximoIfApplicable(new Exception("${GlobalVars.ICP_ERROR_DEPLOY_ON_MITIGATED_CENTER}"), pipeline, pomXml, [:])
        }
        
        if("pre".equals(pipeline.deployStructure.env.toLowerCase()) || "pro".equals(pipeline.deployStructure.env.toLowerCase())) {
			String microUrlGatewayForRefresh = icpStateUtility.icpDeployStructure.getUrlActuatorPrefixTesting() + icpStateUtility.icpDeployStructure.getUrlSuffixTesting(icpDistCenter) + "/" + pathToMicro
			runActuatorRefresh("${microUrlGatewayForRefresh}", pomXml, pipeline)
		}

        def microUrlGatewayForTesting
        if("pro".equals(pipeline.deployStructure.env.toLowerCase())) {
            def hasAffinity = validateMicroIsUp(icpStateUtility.icpDeployStructure.getUrlPrefixTesting())
            microUrlGatewayForTesting = icpStateUtility.icpDeployStructure.getUrlPrefixTesting(hasAffinity) + icpStateUtility.icpDeployStructure.getUrlSuffixIntegrationTesting(icpDistCenter, hasAffinity) + "/" + pathToMicro
  		} else {
            microUrlGatewayForTesting = icpStateUtility.icpDeployStructure.getUrlPrefixTesting() + icpStateUtility.icpDeployStructure.getUrlSuffixIntegrationTesting(icpDistCenter) + "/" + pathToMicro
        }
		printOpen("Running integration tests against ${microUrlGatewayForTesting}", EchoLevel.INFO)
        // End: Check Centers Avaiability
        
        def additionalParameters = ''
        pipeline.mvnAdditionalParameters.each { parameter ->
            additionalParameters += parameter && parameter != "null" ? "${parameter} " : ""
        }
		
		if(pipeline.onlyProductionTests) {
			withCredentials([string(credentialsId: "ALM_TOKEN_${icpStateUtility.icpDeployStructure.envICP.toUpperCase()}_V2", variable: 'tokenAbsis3')]) {
			
				additionalParameters += "-P it-pro "
				additionalParameters += "-Dskip-it=true "
				additionalParameters += "-Dauthorization-token=${tokenAbsis3} "

                //def cmd = "mvn <Default_Maven_Settings> -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} verify -Dmicro-url=${microUrlGatewayForTesting} -Dskip-ut=true ${additionalParameters} -Denvironment=${icpStateUtility.environment}"
				def cmd = "mvn <Default_Maven_Settings>  verify -Dmicro-url=${microUrlGatewayForTesting} -Dskip-ut=true ${additionalParameters} -Denvironment=${icpStateUtility.environment}"
                boolean weHaveToGenerateOpenApiClasses =
                    WorkspaceUtils.isThereASwaggerContract(this, pomXml) &&
                        ! WorkspaceUtils.areSwaggerContractClassesGenerated(this, pomXml)
                if ( ! weHaveToGenerateOpenApiClasses ) cmd += " -Dcodegen.skip=true "

                try {

                    runMavenGoalWithRetries(pomXml, pipeline, cmd, [
                        archiveLogIfMvnDurationExceeds: 5,
                        mavenTimeout: GlobalVars.MAVEN_INTEGRATION_TEST_RETRIES_TIMEOUT,
                        maxRetries: GlobalVars.MAVEN_INTEGRATION_TEST_MAX_RETRIES,
                        kpiAlmEvent: new KpiAlmEvent(
                            pomXml, pipeline,
                            KpiAlmEventStage.UNDEFINED,
                            KpiAlmEventOperation.MVN_RUN_IT_TESTS)
                    ])

                } catch (MavenGoalExecutionException mgee) {

                    throwExceptionAndOpenMaximoIfApplicable(mgee, pipeline, pomXml, ["micro-url":microUrlGatewayForTesting])

                }

			}
		} else {

            additionalParameters += "-Dskip-it=${pipeline.getExecutionMode().skipIntegrationTest()} "
			//def cmd = "mvn <Default_Maven_Settings> -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} verify -Dmicro-url=${microUrlGatewayForTesting} -Dskip-ut=true ${additionalParameters} -Denvironment=${icpStateUtility.environment}"
			def cmd = "mvn <Default_Maven_Settings>  verify -Dmicro-url=${microUrlGatewayForTesting} -Dskip-ut=true ${additionalParameters} -Denvironment=${icpStateUtility.environment}"
            boolean weHaveToGenerateOpenApiClasses =
                WorkspaceUtils.isThereASwaggerContract(this, pomXml) &&
                    ! WorkspaceUtils.areSwaggerContractClassesGenerated(this, pomXml)
            if ( ! weHaveToGenerateOpenApiClasses ) cmd += " -Dcodegen.skip=true "

            try {

                runMavenGoalWithRetries(pomXml, pipeline, cmd, [
                    archiveLogIfMvnDurationExceeds: 5,
                    mavenTimeout: GlobalVars.MAVEN_INTEGRATION_TEST_RETRIES_TIMEOUT,
                    maxRetries: GlobalVars.MAVEN_INTEGRATION_TEST_MAX_RETRIES,
                    kpiAlmEvent: new KpiAlmEvent(
                        pomXml, pipeline,
                        KpiAlmEventStage.UNDEFINED,
                        KpiAlmEventOperation.MVN_RUN_IT_TESTS)
                ])

            } catch (MavenGoalExecutionException mgee) {

                throwExceptionAndOpenMaximoIfApplicable(mgee, pipeline, pomXml, ["micro-url":microUrlGatewayForTesting])

            }

		}
	
	
		pipeline.pipelineStructure.resultPipelineData.remoteITOk=true
		
		printOpen("Integration tests ended with SUCCESS", EchoLevel.INFO)
		
		
	}


}

private void throwExceptionAndOpenMaximoIfApplicable(MavenGoalExecutionException exception, PipelineData pipelineData, PomXmlStructure pomXml, Map parameters) {

    MavenGoalExecutionFailureError mavenError = exception.mavenError

    if (MavenGoalExecutionFailureErrorConditionals.isAnICPSSLEventualErrorOnITTest(exception) ||
        MavenGoalExecutionFailureErrorConditionals.isAContractServerSSLEventualErrorOnOpenApiGeneration(exception)) {

        createMaximoAndThrow.sslEventualErrorWhileDoingITTest(pipelineData, pomXml, mavenError, parameters['micro-url'])

    } else if (MavenGoalExecutionFailureErrorConditionals.isANexusDownloadFailureDueToPrematureEndOfContentLength(exception)) {

        createMaximoAndThrow.nexusDownloadExceptionDueToAConnectivityIssue(pipelineData, pomXml, mavenError)

    } else if (MavenGoalExecutionFailureErrorConditionals.isANexusDownloadFailureDueToANonPresentPluginDependencyThatWasPreviouslyOnNexus(exception)) {

        createMaximoAndThrow.nexusDownloadExceptionDueToANonPresentPluginDependencyThatWasPreviouslyOnNexus(pipelineData, pomXml, mavenError)

    }

    error "We got an unrecoverable maven goal execution failure:\n\n${mavenError.prettyPrint()}"
}
