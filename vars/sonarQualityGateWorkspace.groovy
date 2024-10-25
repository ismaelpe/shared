import com.project.alm.*

def call(PomXmlStructure pomXml, PipelineData pipeline) {

    long wholeCallDuration

    long wholeCallStartMillis = new Date().getTime()

    KpiAlmEvent kpiAlmEvent =
        new KpiAlmEvent(
            pomXml, pipeline,
            KpiAlmEventStage.SONAR_QUALITY_GATE,
            KpiAlmEventOperation.SONAR_QUALITY_GATE_PLUGIN)

    script {
        boolean resultOK = false
        try {

            StringBuilder logmessage = new StringBuilder()

            logmessage.append("allowed list of artifact types to execute sonar-quality-gate:\n")
            for (ArtifactSubType item : GlobalVars.ALLOWED_SONAR_TYPES) {
                logmessage.append("- ${item.toString()}\n")
            }
            logmessage.append("current artifact type is ${pomXml.artifactSubType.toString()}\n")
            logmessage.append("current artifact name is ${pomXml.artifactName}\n")

            printOpen("${logmessage.toString()}", EchoLevel.DEBUG)
            if (GlobalVars.ALLOWED_SONAR_TYPES.contains(pomXml.artifactSubType)) {
                printOpen("Checking sonar quality gate...", EchoLevel.INFO)

               timeout(time: 10, unit: 'MINUTES') {
                    // Just in case something goes wrong, pipeline will be killed after a timeout
                    // Inicio - IPE
					def qg = null
					
					try {

                        printOpen("Intentamos usar el waitForQualityGate del plugin de sonar para recoger el analisis...", EchoLevel.DEBUG)

                        singleCallStartMillis = new Date().getTime()

                        qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv

                        long wholeCallEndMillis = new Date().getTime()
                        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                        kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))

					} catch(Exception exception) {

                        long wholeCallEndMillis = new Date().getTime()
                        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                        kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))

                        kpiAlmEvent.operation(KpiAlmEventOperation.SONAR_QUALITY_GATE_HTTP_REPORT_TASK)

                        printOpen("The plugin waitForQualityGate failed. We're going to ask sonar using the HTTP API.", EchoLevel.INFO)
                        
                        def props = readProperties interpolate: true, file: "${pomXml.getRouteToSonarReportTask()}"

                        printOpen("Route to sonar report task: ${props}", EchoLevel.DEBUG)
                        printOpen("Checking sonar report task status at ${props.ceTaskUrl}", EchoLevel.INFO)

                        wholeCallStartMillis = new Date().getTime()

                        def sonarTokenPrefix = "$SONAR_TOKEN:" // No quitar el : al final ver (https://community.sonarsource.com/t/using-rest-api-with-user-token/15229)
                        def sonarTokenB64 = Base64.getEncoder().encodeToString(sonarTokenPrefix.getBytes())
                       
                        def responseTask = httpRequestUtils.send([
                            verb: 'POST',
                            url: props.ceTaskUrl,
                            headers: [
                                [name: 'Authorization', value: "Basic $sonarTokenB64"]
                            ],
                            parseResponse: true,
                            needsProxy: false,
                            noproxyOption: '*' 
                        ])

                        wholeCallEndMillis = new Date().getTime()
                        wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                        def responseTaskJson = responseTask.content

                        // Aunque comprobemos el status a SUCCESS, asumimos que sonar habrá terminado de analizar
                        // ya que la pipeline hasta llegar aquí habrá tardado varios minutos en ejecutarse
                        if (responseTask.status == 200 && responseTaskJson.task.status == "SUCCESS") {

                            kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))
                            kpiAlmEvent.operation(KpiAlmEventOperation.SONAR_QUALITY_GATE_HTTP_ANALYSIS)

                            def analysisStatusUrl = "${GlobalVars.SONAR_URL}/api/qualitygates/project_status?analysisId=${responseTaskJson.task.analysisId}"
                            
                            printOpen("Sonar report task ended with SUCCESS. We are going to ask about the results of the analysis at ${analysisStatusUrl}", EchoLevel.INFO)

                            wholeCallStartMillis = new Date().getTime()

                            def responseAnalysis = httpRequestUtils.send([
                                verb: 'POST',
                                url: analysisStatusUrl,
                                headers: [
                                    [name: 'Authorization', value: "Basic $sonarTokenB64"]
                                ],
                                parseResponse: true,
                                needsProxy: false,
                                noproxyOption: '*' 
                            ])

                            //def responseAnalysis = httpRequest url: "${analysisStatusUrl}",
                            //                            httpProxy: "${env.http_proxy}",
                            //                            consoleLogResponseBody: true,
                            //                            contentType: 'APPLICATION_JSON',
                            //                            httpMode: "POST",
                            //                            ignoreSslErrors: true

                            wholeCallEndMillis = new Date().getTime()
                            wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

                            if (responseAnalysis.status == 200) {
                                //def responseAnalysisJson = readJSON text: responseAnalysis.content
                                def responseAnalysisJson = responseTask.content
                                
                                qg = responseAnalysisJson.projectStatus

                                kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))

                            } else {

                                kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))
                                throw new Exception("Ups!, parece que algo fue mal. El análisis de sonar ${props.ceTaskUrl} aún no está listo. Se abrirá un máximo al equipo de Sonar para gestionar esto.")

                            }

                        } else {

                            kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))
                            throw new Exception("Ups!, parece que algo fue mal. El análisis de sonar ${props.ceTaskUrl} aún no está listo. Se abrirá un máximo al equipo de Sonar para gestionar esto.")

                        }
					} finally {
                        if (qg != null) {
                            pipeline?.pipelineStructure?.resultPipelineData?.sonarQualityGateExecuted = true
                            printOpen("sonar-quality-gate result is ${qg.status}", EchoLevel.INFO)
                            printOpen("sonar-quality-gate url details: ${pipeline.testData}", EchoLevel.INFO)
                            resultOK = (qg.status == 'OK')
                        } else {
                            resultOk = false
                        }
                    }
                    // Fin - IPE
                }
            } else {
                printOpen("AppType ${pomXml.artifactSubType} is NOT allowed to execute sonar quality gate", EchoLevel.INFO)
                //we are not allowed to execute quality gate then we return resultOK = true

                resultOK = true
                //in order to continue next stages
            }

        } catch (Exception e) {
			//this error could be when sonar server is down

            long wholeCallEndMillis = new Date().getTime()
            wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

            kpiLogger(kpiAlmEvent.callFail(wholeCallDuration))
			
			throw e
        }

        return resultOK
    }
}

