import com.project.alm.*
import hudson.Functions

def call( PomXmlStructure pomXml, PipelineData pipeline) {
	deployCloud(pomXml,pipeline,null,null)
}

def buildArtifactOnCloud(PipelineData pipeline, PomXmlStructure pomXml, String requestURL, def body, String method, String aplicacionGAR, String pollingRequestUrl )  {
	CloudApiResponse responseCloud=null
	try {

        try {

			responseCloud = sendRequestToCloudApi(requestURL,body,method,aplicacionGAR,pollingRequestUrl,true,false, pipeline, pomXml)

		} catch(java.io.NotSerializableException e) {

			responseCloud = sendRequestToCloudApi(requestURL,body,method,aplicacionGAR,pollingRequestUrl,true,false, pipeline, pomXml)

		}
		
		if (responseCloud.statusCode==500) {

            printOpen("Puntual error (status code: ${responseCloud.statusCode}). We are going to try it again...", EchoLevel.ERROR)
			responseCloud = sendRequestToCloudApi(requestURL,body,method,aplicacionGAR,pollingRequestUrl,true,false, pipeline, pomXml)

		}

	} catch(Exception e) {

        createMaximoAndThrow.deployBuildDockerImageFailure(pipeline, pomXml, e)

	}
	
	return responseCloud
}

def initProbeValues(CloudDeployStructure deployStructure, String artifactId) {	
  	//Validaremos las necesidades a nivel de probe
	if (env.Cloud_PROBE_SLOW!=null && env.Cloud_PROBE_SLOW.contains(artifactId)) {
		deployStructure.probeEspecial="SLOW"
		deployStructure.periodReadiness=env.Cloud_PROBE_PERIOD_READINESS
		deployStructure.periodLiveness=env.Cloud_PROBE_PERIOD_LIVENESS
		deployStructure.timeoutLiveness=env.Cloud_PROBE_TIMEOUT_LIVENESS
		deployStructure.timeoutReadiness=env.Cloud_PROBE_TIMEOUT_READINESS
		deployStructure.failureThresholdReadiness=env.Cloud_PROBE_FAILURE_READINESS
		deployStructure.failureThresholdLiveness=env.Cloud_PROBE_FAILURE_LIVENESS
		deployStructure.initialDelaySecondsLiveness=env.Cloud_PROBE_INITIAL_LIVENESS
		deployStructure.initialDelaySecondsReadiness=env.Cloud_PROBE_INITIAL_READINESS
    }else {
		deployStructure.probeEspecial="NORMAL"
		deployStructure.periodReadiness=env.Cloud_PROBE_PERIOD_READINESS_GENERAL
		deployStructure.periodLiveness=env.Cloud_PROBE_PERIOD_LIVENESS_GENERAL
		deployStructure.timeoutLiveness=env.Cloud_PROBE_TIMEOUT_LIVENESS_GENERAL
		deployStructure.timeoutReadiness=env.Cloud_PROBE_TIMEOUT_READINESS_GENERAL
		deployStructure.failureThresholdReadiness=env.Cloud_PROBE_FAILURE_READINESS_GENERAL
		deployStructure.failureThresholdLiveness=env.Cloud_PROBE_FAILURE_LIVENESS_GENERAL
		deployStructure.initialDelaySecondsLiveness=env.Cloud_PROBE_INITIAL_LIVENESS_GENERAL
		deployStructure.initialDelaySecondsReadiness=env.Cloud_PROBE_INITIAL_READINESS_GENERAL
		
	}
}


/**
 * Script que permite desplegar en bluemix el micro
 * @param pomXml 
 * @param pipeline
 * @return
 */
def call( PomXmlStructure pomXml, PipelineData pipeline, String buildIdMain, String newImageMain) {

    long wholeCallDuration
    long wholeCallStartMillis = new Date().getTime()

    KpiAlmEvent kpiAlmEvent =
        new KpiAlmEvent(
            pomXml, pipeline,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.Cloud_DEPLOY)
	if (pomXml.artifactMicro!="" || pomXml.artifactSampleApp!="") {
	    printOpen("Preparing deployment data...",EchoLevel.INFO)
		printOpen("Pom structure: ${pomXml.toString()}",EchoLevel.DEBUG)
        try {
            printOpen("The environment is ${pipeline.bmxStructure.environment} the other build is ${buildIdMain} the other image is ${newImageMain}", EchoLevel.DEBUG)
            printOpen("The component Id of the new componnet  The artifact is ${pomXml.artifactMicro} is Arch ${pomXml.isArchProject()} ${pomXml.artifactType}  ${pomXml.artifactSubType}", EchoLevel.INFO)
			String environment=pipeline.bmxStructure.environment
			
			CloudDeployStructure deployStructure=new CloudDeployStructure('cxb-ab3cor','cxb-ab3app',environment)
			pipeline.deployStructure = deployStructure
            boolean isApplicationWithNewHealthGroups = pomXml.isApplicationWithNewHealthGroups()
            printOpen("Cloud_CUSTOM_LIVENESSPROBE_APPLICATIONS: ${GlobalVars.Cloud_CUSTOM_LIVENESSPROBE_APPLICATIONS}\n" +
                "ArtifactName: ${pomXml.artifactName}\n" +
                "ArchVersion: ${pomXml.archVersion}\n" +
                "isApplicationWithNewHealthGroups: ${isApplicationWithNewHealthGroups}", EchoLevel.DEBUG)
            deployStructure.springProfilesActive =
                pomXml.archVersion.endsWith("-SNAPSHOT") ?
                    deployStructure.calculateSpringCloudActiveProfiles(isApplicationWithNewHealthGroups) :
                    deployStructure.calculateSpringCloudActiveProfiles(pipeline.garArtifactType.name, pipeline.company, isApplicationWithNewHealthGroups)

            printOpen("The url is ${deployStructure.url_int} ${deployStructure.url_ext} the environment ${deployStructure.env} environment Cloud ${deployStructure.envCloud}", EchoLevel.DEBUG)
		
			//Vemos si hay que hacer distribución por centro
			String cloudDistCenter="ALL"
			boolean isDeployByCenter = false
			deployStructure.idCenter=1
			if (pipeline.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_1) {
				cloudDistCenter="AZ1"
				isDeployByCenter = true
				deployStructure.idCenter=1
			} else if (pipeline.distributionModePRO == DistributionModePRO.SINGLE_CENTER_ROLLOUT_CENTER_2) {
				cloudDistCenter="AZ2"
				isDeployByCenter = true
				deployStructure.idCenter=2
			}
			
			deployStructure=getInfoAppFromCatalog(pomXml, pipeline, deployStructure)
			
			//Begin generateManifest
			//Esto puede provocar concurrencia en la generacion del fichero
			checkCloudAvailability(pomXml,pipeline,deployStructure.envCloud,"DEPLOY")
			if (buildIdMain==null) {
				checkCloudAvailability(pomXml,pipeline,deployStructure.envCloud,"BUILD")
			} 
			
			String newManifest = ''//Por ejemplo le pasamos centro 1
			//FIXME: Porque usamos un deployStructure de BMX en Cloud?
            newManifest = generateManifest(pomXml, pipeline.bmxStructure.getDeployStructure(GlobalVars.BMX_CD1),pipeline,true)
			
			
			boolean hasIngress = "PRO" == environment.toUpperCase() ? env.Cloud_APP_HAS_INGRESS!=null : env.Cloud_APP_HAS_INGRESS_PREVIOS!=null
			String ingressList = "PRO" == environment.toUpperCase() ? env.Cloud_APP_HAS_INGRESS : env.Cloud_APP_HAS_INGRESS_PREVIOS
			
			String artifactApp=pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) )
			if (hasIngress) {
				if (artifactApp!=null) {
					 deployStructure.hasIngress=ingressList.contains(artifactApp)
					 deployStructure.hasMtls=ingressList.contains(artifactApp)
					 deployStructure.needsSpecialVerifyDepth=false
					 deployStructure.needsSystemRoute=true
					 /*
					 String ingressMaxSize=null
					 String onlyMtls=null					
					 */
					 if (env.Cloud_INGRESS_MAX_SIZE!=null) {
						 deployStructure.ingressMaxSize=env.Cloud_INGRESS_MAX_SIZE
					 }					 
					 if (env.Cloud_INGRESS_CONNECT_TIMEOUT!=null) {
						 deployStructure.ingressConnectTimeout=env.Cloud_INGRESS_CONNECT_TIMEOUT
					 }
					 if (env.Cloud_INGRESS_READ_TIMEOUT!=null) {
						 deployStructure.ingressReadTimeout=env.Cloud_INGRESS_READ_TIMEOUT
					 }
					 if (env.Cloud_INGRESS_SEND_TIMEOUT!=null) {
						 deployStructure.ingressWriteTimeout=env.Cloud_INGRESS_SEND_TIMEOUT
					 }
				}				
			}
			
			if (pomXml.lowerThanMinCloudArchVersion() && !"tauxconnector".equals(artifactApp)) {
				deployStructure.withoutAnnotations=true
			}
			
			
			String manifestContent=sh(returnStdout: true, script: "cat ${newManifest}")
            printOpen("${deployStructure.initSecretsMemoryFromYaml(manifestContent)}", EchoLevel.DEBUG)
            printOpen("The memory is ${deployStructure.memory}\n" +
                "The secrets are ${deployStructure.secrets}",
                EchoLevel.DEBUG)
			//Nos interesa la memoria
			//Nos interesa los services que no sean configserver
			String secretsNeeded=""
			
			try {

				secretsNeeded=sh(returnStdout: true, script: "cat resources.yml")
                printOpen("The yaml of the secrets is ${secretsNeeded}", EchoLevel.DEBUG)
				deployStructure.initSecretsFromYamlResources(secretsNeeded)
                printOpen("The list of the volume secrets  ${deployStructure.volumeSecrets}", EchoLevel.INFO)

			} catch(Exception e) {
                printOpen("The app doesnt need special secrets ${e.getMessage()}", EchoLevel.DEBUG)
			}
			
			validateSecrets(deployStructure, pomXml, pipeline)
		
			CloudApiResponse response=null
			
			printOpen("The artifact is ${pomXml.artifactMicro} is Arch ${pomXml.isArchProject()}  ${pomXml.artifactSubType}",
				EchoLevel.DEBUG)
			
			def cloudAppResources=generateCloudResources(deployStructure.memory,environment.toUpperCase(),pomXml.isArchProject(),pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name)),pipeline.domain,pipeline.garArtifactType.name, pomXml.getMajorVersion(),pomXml.getCloudAppName() )

			pipeline.componentId=generateArtifactInCloud(pomXml,pipeline,cloudAppResources)
			Cloudk8sActualStatusInfo cloudActualStatusInfo=null
							
		    cloudActualStatusInfo=getActualDeploymentStatusOnCloud(pomXml,pipeline,deployStructure,true,cloudDistCenter,true)

			//pipeline.componentId=2323
			def component= MavenUtils.sanitizeArtifactName(pomXml.artifactName, pipeline.garArtifactType)

            printOpen("The actualStatusInfo on Cloud Environment is ${cloudActualStatusInfo.toString()}\n" +
                "The component Id of the new componnet ${pipeline.componentId} of the component ${component} The artifact is ${pomXml.artifactMicro} is Arch ${pomXml.isArchProject()}",
                EchoLevel.DEBUG)
		
			//"pro-registry.pro.caas.project.com/containers/pasdev/democonnecta22:latest
			def versionImage=pomXml.artifactVersion
			
			if (pomXml.artifactVersion.contains('SNAPSHOT')) {
				versionImage=versionImage+"-"+cloudActualStatusInfo.getNextImage()
			} 
		
			def artifactMicro = pomXml.artifactMicro
			
			if (pipeline.deployOnCloud && pomXml.artifactType==ArtifactType.AGREGADOR && pomXml.artifactSampleApp != "") {
				artifactMicro = pomXml.artifactSampleApp
			}
			
			if (pipeline.deployOnCloud && pomXml.artifactType==ArtifactType.AGREGADOR && pomXml.artifactMicro!="") {
				//Si hemos hecho deploy en Cloud ahora no tenemos que hacer deploy ya que lo hemos hecho antes
				artifactMicro=pomXml.artifactMicro
			}
			String versionArtifact = pomXml.artifactVersion
			
			if (pomXml.artifactVersion.contains('SNAPSHOT')) {
				
				versionArtifact=(versionArtifact-'SNAPSHOT')+pipeline.buildCode

                printOpen("The final artifact is ${versionArtifact}", EchoLevel.INFO)
			}
			
			String additionalBuildParam="" 
			if (deployStructure.isDb2 && getDb2Driver(deployStructure.envCloud)!=null) {
				additionalBuildParam=additionalBuildParam+",DB2_DRIVER_INPUT="+getDb2Driver(deployStructure.envCloud)
		  
				printOpen("Overriding DB2 driver...", EchoLevel.INFO)
			}
			
			def body = [
				extraArgs: "GROUP_ID=${pomXml.groupId},VERSION_ARTIFACT=${versionArtifact},ARTIFACT_ID=${artifactMicro}${additionalBuildParam}",
				version: "${versionImage}"
			]			
			//Deploy
			//Build del artefacto
			if (buildIdMain==null) {


                long wholeBuildImageStartMillis = new Date().getTime()

                try {
                    printOpen("Building docker image....", EchoLevel.INFO)
                    response = buildArtifactOnCloud(pipeline, pomXml, "v1/type/PCLD/application/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/build", body, "POST", "${pomXml.getCloudAppName()}", "v1/type/PCLD/application/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/build")
                    printOpen("Docker image has been created successfully.", EchoLevel.INFO)
                    long wholeBuildImageEndMillis = new Date().getTime()
                    long wholeBuildImageDuration = wholeBuildImageEndMillis - wholeBuildImageStartMillis

                    kpiLogger(
                        new KpiAlmEvent(pomXml, pipeline, KpiAlmEventStage.UNDEFINED, KpiAlmEventOperation.Cloud_BUILD_DOCKER_IMAGE)
                            .callSuccess(wholeBuildImageDuration)
                    )

                } catch (Exception e) {
                    long wholeBuildImageEndMillis = new Date().getTime()
                    long wholeBuildImageDuration = wholeBuildImageEndMillis - wholeBuildImageStartMillis

                    kpiLogger(
                        new KpiAlmEvent(pomXml, pipeline, KpiAlmEventStage.UNDEFINED, KpiAlmEventOperation.Cloud_BUILD_DOCKER_IMAGE)
                            .callAlmFail(wholeBuildImageDuration)
                    )

                    throw e

                }

			} else {

				response= new CloudApiResponse()
				def body1 = null
				if (buildIdMain=="NoTenemosID") {
					body1 = [
						imageRepo1: "${newImageMain}",
						id: "0"
					]
				}else {
					body1 = [
						imageRepo1: "${newImageMain}",
						id: "${buildIdMain}"
					]
				}

				response.statusCode=200
				response.body=body1
			}
			//Deploy del artefacto
            boolean wasError = !(response.statusCode>=200 && response.statusCode<300)
            printOpen("${response} De Cloud statusCode ${response.statusCode} body ${response.body}", wasError ? EchoLevel.ERROR : EchoLevel.DEBUG)

            String buildId = response?.body?.id

			if (!wasError) {
				//Validar las necesidades que debe tener esta pieza a nivel de probe
				initProbeValues(deployStructure, pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name)) )
				
				def firstCloudState=CloudWorkflowStates.NEW_DEPLOY
				
				if ( environment.toUpperCase() == "DEV" ) {
					firstCloudState=CloudWorkflowStates.ELIMINATE_CURRENT_APP
				}
				
				CloudStateUtility cloudStateUtility=new CloudStateUtility(pipeline,pomXml,response.body.imageRepo1, cloudActualStatusInfo, firstCloudState, MavenUtils.sanitizeArtifactName(pomXml.artifactName, pipeline.garArtifactType),pomXml.getArtifactMajorVersion())
				cloudStateUtility.suffixedComponentName=deployStructure.getSuffixedComponentName()
				cloudStateUtility.buildId=buildId
				cloudStateUtility.versionImage=versionImage
				cloudStateUtility.cloudResources=cloudAppResources
				cloudStateUtility.cloudResources.garArtifactType=pipeline.garArtifactType
				cloudStateUtility.cloudDeployStructure=deployStructure
				cloudStateUtility.initExtraRoute()
				cloudStateUtility.microType=deployStructure.microType
				
								
				if (pomXml.artifactSubType!=ArtifactSubType.MICRO_APP && pomXml.artifactSubType!=ArtifactSubType.MICRO_ARCH) {
					cloudStateUtility.sampleAppFlag=true
				}

				printOpen("The app is ${pomXml.getBmxAppId()} ", EchoLevel.INFO)
		
				
				if (cloudActualStatusInfo!=null && cloudActualStatusInfo.deployId!=null) cloudStateUtility.deployId=cloudActualStatusInfo.deployId
				
				body = [
					az: "${cloudDistCenter}",
					environment: "${deployStructure.envCloud.toUpperCase()}",
					values: "${deployStructure.getEnvVariables(pipeline.garArtifactType.name,pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) ),pomXml.getMajorVersion(),pipeline.domain,pipeline.subDomain,pipeline.company)}${cloudStateUtility.getChartValues()}"
				]

                printOpen("The body is ${body}", EchoLevel.DEBUG)
                printOpen("[Cloud-STATE] STATE OF THE Cloud ${cloudStateUtility.cloudAppState.toString()}", EchoLevel.DEBUG)
                
                printOpen("Deploying application to Kubernetes...", EchoLevel.INFO)
				
				response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)

                printOpen("el resultado es ${response}", EchoLevel.DEBUG)
                printOpen("cloudStateUtility=$cloudStateUtility", EchoLevel.DEBUG)

				if (response.statusCode>300) {

                    createMaximoAndThrow.cloudDeployException(pipeline, pomXml, response)

				} else {
					 pipeline.isNowDeployed = true

					 
					 
					 if (response!=null && response.body!=null)
						cloudStateUtility.deployId=response.body.id
					 else cloudStateUtility.deployId=0

                     printOpen("The deploy with id ${cloudStateUtility.deployId} ended successfully.", EchoLevel.INFO)
                     printOpen("Chart values:\n${cloudStateUtility.getChartValues()}", EchoLevel.DEBUG)

					 boolean isReady = false
					 
					 //Vamos a intentar solucionar el problema que tenemos... vamos a lanzar dos deploys.
					 try {

						 isReady=waitCloudDeploymentReady(pomXml,pipeline,deployStructure,cloudStateUtility.getNewColour(),cloudDistCenter)
					 } catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException e) {
						 if ("DEV".equals(deployStructure.envCloud.toUpperCase())) {
							 response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"DELETE","${pomXml.getCloudAppName()}","",false,false)
							 
							 if (response.statusCode>300) {
								 printOpen("The current deployment has been deleted.",EchoLevel.INFO)	
							 }
						 }
						 throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")															 														   
					 } catch (TooManyRestartsException e){ 
						 
						 printOpen("${e.getMessage()}}", EchoLevel.ERROR)
						 printOpen("We are going to delete the current deployment.",EchoLevel.INFO)
						 
						 response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"DELETE","${pomXml.getCloudAppName()}","",false,false)
						 
						 if (response.statusCode>300) {
							 printOpen("The current deployment has been deleted.",EchoLevel.INFO)							 
						 }
						 throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")
						 
					 } catch(Exception e) {

                         printOpen("Error at waitCloudDeploymentReady:\n${Utilities.prettyException(e, false)}", EchoLevel.DEBUG)

						 if (e.getMessage()!=null && e.getMessage().contains("DEPLOY FALLIDO")) {

							 response=sendRequestToCloudApi("v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",body,"POST","${pomXml.getCloudAppName()}","v1/application/PCLD/${pomXml.getCloudAppName()}/component/${pipeline.componentId}/deploy",true,true, pipeline, pomXml)

							 if (response.statusCode>300) {

                                 createMaximoAndThrow.cloudDeployException(pipeline, pomXml, response)

                             }
							 try {

								 isReady=waitCloudDeploymentReady(pomXml,pipeline,deployStructure,cloudStateUtility.getNewColour(),cloudDistCenter)

							 } catch(Exception e1) {

								 throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")
							 }
							 
						 } else {

							 throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")

						 }
					 }

					 if (isReady) printOpen("The deployment with id ${cloudStateUtility.deployId} ended successfully.", EchoLevel.INFO) 
					 
					 cloudStateUtility.cloudAppState=cloudStateUtility.getNextStateWorkflow()
					 
					 //Vamos a proceder a hacer el ELIMINATE NEW ROUTE
					 if (cloudStateUtility.cloudAppState==CloudWorkflowStates.ELIMINATE_NEW_ROUTE_TO_CURRENT_APP) {

						 deployCloudState(cloudStateUtility, pomXml, pipeline, cloudDistCenter)
						 cloudStateUtility.cloudAppState=cloudStateUtility.getNextStateWorkflow()

					 }
					 
					 //Validate the micro is up.... maybe the micro is too slow
					 String artifactId = BmxUtilities.calculateArtifactId(pomXml,pipeline.branchStructure)
					 
					 String pathToMicro=cloudStateUtility.cloudDeployStructure.getSuffixedComponentName().replace("<componentName>", artifactId)
					 
					 if (pipeline.branchStructure.branchType == BranchType.FEATURE) {

						 pathToMicro=cloudStateUtility.pathFeature

					 } else {

						 if (cloudStateUtility.sampleAppFlag) pathToMicro=BmxUtilities.calculateArtifactId(pomXml,pipeline.branchStructure,true).toLowerCase()

                     }
					 
					 if (pomXml.isArchProject()) {
						 pathToMicro = "arch-service/" + pathToMicro
					 }

                    // Start: Check Centers Avaiability
                    def az1IsUp = validateMicroIsUp(cloudStateUtility.cloudDeployStructure.getUrlActuatorPrefixTesting() + cloudStateUtility.cloudDeployStructure.getUrlSuffixTesting("AZ1"))
                    def az2IsUp = validateMicroIsUp(cloudStateUtility.cloudDeployStructure.getUrlActuatorPrefixTesting() + cloudStateUtility.cloudDeployStructure.getUrlSuffixTesting("AZ2"))
                  
                    if (cloudDistCenter == "ALL") {
                        if (az1IsUp) {
                            printOpen("We are going to check our application at cluster 1...", EchoLevel.INFO)
                            cloudDistCenter = "AZ1"
                        } else {
                            if (az2IsUp) {
                                printOpen("We are going to check our application at cluster 2...", EchoLevel.DEBUG)
                                cloudDistCenter = "AZ2"
                            } else {
                                throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_KUBERNETES_DISABLED}")
                            }
                        }
                    } else if (cloudDistCenter == "AZ1" && !az1IsUp) {
                        // Lanzar la exception para decir no que si es por centro y se ha seleccionado el centro mitigado que falle
                        throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_ON_MITIGATED_CENTER}")
                    } else if (cloudDistCenter == "AZ2" && !az2IsUp) {
                        // Lanzar la exception para decir no que si es por centro y se ha seleccionado el centro mitigado que falle
                        throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_ON_MITIGATED_CENTER}")
                    }
                    
                    // Check is micro Up
                    String microUrl = cloudStateUtility.cloudDeployStructure.getUrlActuatorPrefixTesting() + cloudStateUtility.cloudDeployStructure.getUrlSuffixTesting(cloudDistCenter) + "/" + pathToMicro
                    
                    if (!validateMicroIsUp(microUrl)) {
                         throw new Exception("${GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING}")
                    }
                    
                    printOpen("Our new deployment is UP and Running. Url: <a href='${microUrl}'>${microUrl}</a>", EchoLevel.DEBUG)
					//Si tenemos old.... y vamos por centro deberiamos cerrar old.
				}	
				return cloudStateUtility

			} else {

                createMaximoAndThrow.cloudDeployException(pipeline, pomXml, response)

			}
		
		} catch(Exception e) {

            long wholeCallEndMillis = new Date().getTime()
            wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

            boolean isAppFail = e?.getMessage().contains(GlobalVars.Cloud_ERROR_DEPLOY_NO_INSTANCE_AVAILABLE) ||
                e?.getMessage().contains(GlobalVars.Cloud_ERROR_DEPLOY_INSTANCE_REBOOTING)

            if (isAppFail) {
                printOpen(e.getMessage(), EchoLevel.ERROR)
                kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration))
            } else {
                printOpen(Functions.printThrowable(e), EchoLevel.ERROR)
                kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration))
            }

														  
			throw e

		}

	} else {

		//No existe micro dentro del artifact
		//no deplegamos contra bluemix
        printOpen("No micro to deploy!!!", EchoLevel.INFO)

        return null
	}

    printOpen("End Deploy stage", EchoLevel.DEBUG)

    long wholeCallEndMillis = new Date().getTime()
    wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

    kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))

	return null
}
