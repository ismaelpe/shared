import com.caixabank.absis3.*

def call(PomXmlStructure pomXml, PipelineData pipeline, String command, Map parameters = [:]) {

    def commitLog
    MavenGoalExecutionException lastError

    long singleCallDuration
    long wholeCallDuration

    long wholeCallStartMillis = new Date().getTime()
    long singleCallStartMillis

    def unrecoverableError
    def errorIsNotRetryableFunction = parameters?.errorIsNotRetryable ?
        parameters?.errorIsNotRetryable :
        MavenGoalExecutionFailureErrorConditionals.defaultErrorIsNotRetryableFunction

    int archiveLogIfMvnDurationExceeds = parameters.archiveLogIfMvnDurationExceeds ? parameters.archiveLogIfMvnDurationExceeds : 7200

    KpiAlmEvent kpiAlmEvent = parameters?.kpiAlmEvent

    try {

        int retryNumber = parameters.mavenRetries ? parameters.mavenRetries : GlobalVars.MAVEN_GOAL_EXECUTION_MAX_RETRIES
        int mavenTimeout = parameters.mavenTimeout ? parameters.mavenTimeout : 90

        timeout(time: mavenTimeout, unit: 'MINUTES') {

            waitUntil(initialRecurrencePeriod: 15000) {

                try {

                    singleCallStartMillis = new Date().getTime()
                    
                    // Detectamos si en el comando existe la palabra deploy                    
                    def regex = ~/(:?.*)mvn(:?.*| )deploy(:?.*| )/

                    if (command ==~ regex) {                   
                        // Obtenemos el deployment repo para poder desplegar artefactos.
                        def deploymentRepo = parameters.get("forceDeploymentRepo", MavenUtils.getDeploymentRepository(pomXml.artifactVersion))
                        command = "$command $deploymentRepo"
                    }
                    
                    commitLog = runMavenCommand(command, parameters)

                    long singleCallEndMillis = new Date().getTime()
                    singleCallDuration = singleCallEndMillis - singleCallStartMillis

                    return true

                } catch (MavenGoalExecutionException mgee) {

                    long singleCallEndMillis = new Date().getTime()
                    singleCallDuration = singleCallEndMillis - singleCallStartMillis

                    boolean isAppException = errorIsNotRetryableFunction(mgee)

                    printOpen("isAppException: ${isAppException}", EchoLevel.DEBUG)

                    String errorCode = MavenGoalExecutionFailureErrorConditionals.getErrorCode(mgee)

                    if (kpiAlmEvent && isAppException) kpiLogger(kpiAlmEvent.requestAppFail(singleCallDuration, errorCode))
                    if (kpiAlmEvent && ! isAppException) kpiLogger(kpiAlmEvent.requestAlmFail(singleCallDuration, errorCode))

                    lastError = mgee

                    boolean shallWeStop = isAppException || retryNumber-- == 0

                    if (shallWeStop) {

                        throw mgee

                    }

                    if (kpiAlmEvent) kpiLogger(kpiAlmEvent.retry())

                    return false

                } catch (Exception e) {

                    long singleCallEndMillis = new Date().getTime()
                    singleCallDuration = singleCallEndMillis - singleCallStartMillis

                    kpiLogger(kpiAlmEvent.requestFail(singleCallDuration))
                    throw e

                }
            }

        }

    } catch(MavenGoalExecutionException mgee) {

        unrecoverableError = mgee
        throwExceptionAndOpenMaximoIfApplicable(mgee, pipeline, pomXml)
		
		throw mgee

    } catch(org.jenkinsci.plugins.workflow.steps.FlowInterruptedException fie) {

        unrecoverableError = fie

        if (lastError) {

            printOpen("The execution of the maven command with retries has failed due to an execution timeout:\n\n${Utilities.prettyException(fie)}\n\n${lastError.mavenError.prettyPrint()}", EchoLevel.ERROR)
            MavenGoalExecutionException mgee = new MavenGoalExecutionException(lastError.mavenError, fie)
            throwExceptionAndOpenMaximoIfApplicable(mgee, pipeline, pomXml)

        } else {
            printOpen("The execution of the maven command with retries has failed due to an execution timeout:\n\n${Utilities.prettyException(fie)}", EchoLevel.ERROR)
        }

        throw fie

    } catch(Exception e) {

        unrecoverableError = e

        printOpen("The execution of the maven command with retries has failed due to an unexpected exception:\n\n${Utilities.prettyException(e)}", EchoLevel.ERROR)
        throw e

    } finally {

        // if (unrecoverableError) env.attachLogsToPipelineLog = true

        if (kpiAlmEvent) {

            long wholeCallEndMillis = new Date().getTime()
            wholeCallDuration = wholeCallEndMillis - wholeCallStartMillis

            // if ((wholeCallDuration/(1000*60)) >= archiveLogIfMvnDurationExceeds) env.attachLogsToPipelineLog = true

            if (unrecoverableError) {
                
                String errorCode = calculateErrorCode(unrecoverableError)

                if (lastError) {

                    boolean isAppException = errorIsNotRetryableFunction(lastError)
                    errorCode = calculateErrorCode(lastError)

                    if (isAppException) kpiLogger(kpiAlmEvent.callAppFail(wholeCallDuration, errorCode))
                    if (!isAppException) kpiLogger(kpiAlmEvent.callAlmFail(wholeCallDuration, errorCode))

                } else {

                    kpiLogger(kpiAlmEvent.callFail(wholeCallDuration, errorCode))

                }

            } else {

                kpiLogger(kpiAlmEvent.requestSuccess(singleCallDuration))
                kpiLogger(kpiAlmEvent.callSuccess(wholeCallDuration))

            }

        }

    }

    return commitLog
}

private String calculateErrorCode(Exception ex) {

    if (ex instanceof org.jenkinsci.plugins.workflow.steps.FlowInterruptedException) {

        return KpiAlmEventErrorCode.TIMEOUT_BLOCK_EXPIRED

    } else if (ex instanceof MavenGoalExecutionException) {

        return MavenGoalExecutionFailureErrorConditionals.getErrorCode(ex)

    } else {

        KpiAlmEventErrorCode.UNDEFINED

    }

}

private void throwExceptionAndOpenMaximoIfApplicable(MavenGoalExecutionException exception, PipelineData pipelineData, PomXmlStructure pomXml) {

    MavenGoalExecutionFailureError mavenError = exception.mavenError

    if (MavenGoalExecutionFailureErrorConditionals.isAContractServerSSLEventualErrorOnOpenApiGeneration(exception)) {

        createMaximoAndThrow.sslEventualErrorWhileExecutingMavenGoal(pipelineData, pomXml, mavenError)

    } else if (MavenGoalExecutionFailureErrorConditionals.isASonarScanFailureDueToAPluginDownloadIssue(exception) ||
                MavenGoalExecutionFailureErrorConditionals.isASonarScanFailureDueToASonarConnectivityIssue(exception)) {

        createMaximoAndThrow.sonarScanExceptionDueToAConnectivityIssue(pipelineData, pomXml, mavenError)

    }

    error "We got an unrecoverable maven goal execution failure:\n\n${mavenError.prettyPrint()}"

}
