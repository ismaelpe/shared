import com.project.alm.GlobalVars
import com.project.alm.BranchType
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.NexusUtils
import com.project.alm.ArtifactSubType
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation
import com.project.alm.*

def call(PomXmlStructure pomXml, PipelineData pipeline,boolean sendEmailResults) {
	checkmarxScanWorkspace(pomXml,pipeline,sendEmailResults,null,GlobalVars.TST_ENVIRONMENT.toUpperCase())
}

def call(PomXmlStructure pomXml, PipelineData pipeline,boolean sendEmailResults, String branch, String checmarxEnv) {

	long wholeCallStartMillis = new Date().getTime()
	
	KpiAlmEvent kpiAlmEvent= new KpiAlmEvent(
		pomXml, pipeline,
		KpiAlmEventStage.UNDEFINED,
		KpiAlmEventOperation.CHECKMARX_SCAN)
	
	
	String disableCheckmarx = "${env.ABSIS3_SERVICES_SKIP_CHECKMARX_ALL}".toString()
	def whiteListApps = "${env.ABSIS3_SERVICES_SKIP_CHECKMARX_LIST}".split(";")
	long wholeCallDuration = 0

    printOpen("disableCheckmarx: ${disableCheckmarx}", EchoLevel.DEBUG)
    printOpen("whiteListApps: ${whiteListApps}", EchoLevel.DEBUG)
	
	
	
	if ("true".equals(disableCheckmarx) || Arrays.asList(whiteListApps).contains(pomXml.artifactName)) {
        printOpen("SKIP Checkmarx Scan the Workspace", EchoLevel.INFO)
	} else {
		
		def branchToAnalize = branch
		
		if (branchToAnalize==null) {
			branchToAnalize=pipeline.branchStructure.branchName
		}
		
		try {
            printOpen("DO Checkmarx Scan the Workspace!!", EchoLevel.INFO)

			String checkmarxProjectName="${pipeline.garArtifactType.getGarName()}.${pomXml.artifactName}"
            printOpen("checkmarxProjectName is ${checkmarxProjectName}", EchoLevel.DEBUG)
			

			def currentResult = 'SUCCESS'
			
			String artifactGarAppName = pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name))
            printOpen("artifactGarAppName ${artifactGarAppName}", EchoLevel.DEBUG)
			String artifactGarType = pipeline.garArtifactType.getGarName()
            printOpen("artifactGarType ${artifactGarType}", EchoLevel.DEBUG)
			
			def resultSecurityScan
			def checmarkEnvironment='BUILD'
			
			withCredentials([string(credentialsId: 'SECURITY_SCAN_KEY', variable: 'API_KEY')]) {
			    if (!pomXml.isLibrary()) {
					//No es una libreria
					//Tenemos que mirar hacia donde quiere desplegar para poder enviar contra checkmarx
					checmarkEnvironment=checmarxEnv
				}
				resultSecurityScan = securityScanGitlab subtype: artifactGarType, envBackend: GlobalVars.ENV_CHECKMARX, api_key: API_KEY, application: pomXml.getApp(pipeline.garArtifactType), branch: branchToAnalize,  component: artifactGarAppName, environment: checmarkEnvironment, source: pipeline.gitUrl, version: pomXml.artifactVersion, sync_type: "sync"
				printOpen("El resultado es del scan es el siguiente  ${resultSecurityScan}", EchoLevel.DEBUG)
			}
			
			if (resultSecurityScan.code == "2" && resultSecurityScan.mode == "1" ) {
				currentResult = 'FAILURE'
				printOpen("Checkmarx has worked correctly but we find some errors in validation. We going to block: ${resultSecurityScan.code}, the mode is ${resultSecurityScan.mode} and the repo is ${resultSecurityScan.report_url}", EchoLevel.ERROR)
				if (sendEmailResults) {
					String pipelineName = pipeline.getPipelineBuildName();
					printOpen("pipelineName ${pipelineName}", EchoLevel.DEBUG)
					sendCheckmarxResultByEmail(pipelineName,artifactGarAppName,artifactGarType, resultSecurityScan.report_url)
				}
			}else {
				printOpen("Checkmarx has worked correctly: ${resultSecurityScan.code}", EchoLevel.INFO)
			}

	        //deleteCheckmarxFilesFromWorkspace()

            printOpen("END Checkmarx Scan", EchoLevel.DEBUG)
			if (currentResult == 'FAILURE') {
                printOpen("Checkmarx has detected some problems with the code... the build will fail.", EchoLevel.ERROR)
				throw new Exception("Serious errors detected in vulnerability validation. Check the email.")
			}
			wholeCallDuration= (new Date().getTime()) - wholeCallStartMillis
			kpiLogger(kpiAlmEvent.requestSuccess(wholeCallDuration))
			kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))
			
		}catch (Exception ex) {
			wholeCallDuration= (new Date().getTime()) - wholeCallStartMillis
			kpiLogger(kpiAlmEvent.requestFail(wholeCallDuration))
			kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))
			throw ex
		}
	
	}

}


private void sendCheckmarxResultByEmail(String pipelineName,String artifactGarAppName, String artifactGarType, String reportUrl) {

    printOpen("sendCheckmarxResultByEmail BEGIN", EchoLevel.DEBUG)
    try {

            def emailsStr = idecuaRoutingUtils.getResponsiblesAppEmailList(artifactGarAppName,artifactGarType)
            String bodyEmail = "<p>La pipeline ha sido bloqueado por motivos de seguridad</p><p>Resultados Checkmarx: ${artifactGarAppName}.</p><p>Visualizar logs de la pipeline: ${GlobalVars.JOB_DISPLAY_CONFLUENCE}</p><p>Usted puede ver el reporte de los defectos reportados en el link adjunto <a>${reportUrl}</a>.</p><p>Saludos.</p>"
            String subjectEmail = "Reporte de Seguridad Checkmarx: ${artifactGarAppName} - ${pipelineName} Fallido "
            String from = GlobalVars.EMAIL_FROM_ALM
            String replyTo = GlobalVars.EMAIL_REPORT
            String attachmentsPattern= "**/Checkmarx/**"
            printOpen("sending email", EchoLevel.DEBUG)
            sendEmail(subjectEmail,from,emailsStr,  replyTo, attachmentsPattern, bodyEmail)


    } catch (Exception e) {
        printOpen("WARNING: Error when sending email to microservice responsibles: ${e.getMessage()}", EchoLevel.ERROR)
    }
    printOpen("sendCheckmarxResultByEmail END", EchoLevel.DEBUG)

}

private void deleteCheckmarxFilesFromWorkspace() {
    
    sh "rm -rf ./Checkmarx"

}
