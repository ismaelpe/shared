package com.project.alm

import com.cloudbees.groovy.cps.NonCPS


class MavenGoalExecutionFailureErrorConditionals {

    public static defaultErrorIsNotRetryableFunction = { exception ->

        if (MavenGoalExecutionFailureErrorConditionals.isAnICPSSLEventualErrorOnITTest(exception)) {

            return false

        } else if (MavenGoalExecutionFailureErrorConditionals.isAContractServerSSLEventualErrorOnOpenApiGeneration(exception)) {

            return false

        } else if (MavenGoalExecutionFailureErrorConditionals.isANexusDownloadFailureDueToPrematureEndOfContentLength(exception)) {

            return false

        } else if (MavenGoalExecutionFailureErrorConditionals.isANexusDownloadFailureDueToANonPresentPluginDependencyThatWasPreviouslyOnNexus(exception)) {

            return false

        }

        return true
    }

    @NonCPS
    static String getErrorCode(Exception exception) {

        String errorCode = MavenGoalExecutionFailureErrorCodes.UNDEFINED

        def errorCodes = [
            isAContractServerSSLEventualErrorOnOpenApiGeneration(exception),
            isAnICPSSLEventualErrorOnITTest(exception),
            isANexusDownloadFailureDueToPrematureEndOfContentLength(exception),
            isANexusDownloadFailureDueToANonPresentPluginDependencyThatWasPreviouslyOnNexus(exception),
            isASonarScanFailureDueToAPluginDownloadIssue(exception),
            isASonarScanFailureDueToASonarConnectivityIssue(exception),
        ]

        for(String code : errorCodes) {
            if (code) {
                errorCode = code
                break
            }
        }

        return errorCode

    }

    @NonCPS
    static String isAContractServerSSLEventualErrorOnOpenApiGeneration(Exception exception) {

        if (! exception instanceof MavenGoalExecutionException) {
            return false
        }

        MavenGoalExecutionFailureError error = ((MavenGoalExecutionException) exception).mavenError

        if (error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.OPENAPI_GENERATION) {

            for(String errorLog in error.errors) {

                if (errorLog.contains('java.lang.RuntimeException: Unable to load URL ref: https://contractserver-micro-server') &&
                    errorLog.contains('Caused by: java.net.SocketException: Connection reset')) {
                    return MavenGoalExecutionFailureErrorCodes.CONTR_SRV_CONN_RESET
                }

            }

        }

        return null
    }

    @NonCPS
    static String isAnICPSSLEventualErrorOnITTest(Exception exception) {

        if (! exception instanceof MavenGoalExecutionException) {
            return false
        }

        MavenGoalExecutionFailureError error = ((MavenGoalExecutionException) exception).mavenError

        if (error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.INTEGRATION_TEST) {

            for(String errorLog in error.errors) {

                if (errorLog.contains('reactor.core.Exceptions$ReactiveException: io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after') &&
                    errorLog.contains('Caused by: io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after')) {
                    return MavenGoalExecutionFailureErrorCodes.IT_TEST_CONN_TIMEOUT
                }
                if (errorLog.contains('javax.net.ssl.SSLException: Connection reset') &&
                    errorLog.contains('Caused by: java.net.SocketException: Connection reset')) {
                    return MavenGoalExecutionFailureErrorCodes.IT_TEST_CONN_RESET
                }

            }

        }

        return null
    }

    @NonCPS
    static String isANexusDownloadFailureDueToPrematureEndOfContentLength(Exception exception) {

        if (! exception instanceof MavenGoalExecutionException) {
            return false
        }

        MavenGoalExecutionFailureError error = ((MavenGoalExecutionException) exception).mavenError

        if (error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.NEXUS_DOWNLOAD) {

            for(String errorLog in error.errors) {

                if (errorLog.contains('Premature end of Content-Length delimited message body')) {
                    return MavenGoalExecutionFailureErrorCodes.NEXUS_DWNLD_PREM_END
                }

            }

        }

        return null
    }

    @NonCPS
    static String isANexusDownloadFailureDueToANonPresentPluginDependencyThatWasPreviouslyOnNexus(Exception exception) {

        if (! exception instanceof MavenGoalExecutionException) {
            return false
        }

        MavenGoalExecutionFailureError error = ((MavenGoalExecutionException) exception).mavenError

        if (error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.NEXUS_DOWNLOAD) {

            for(String errorLog in error.errors) {

                if (errorLog.contains("Error resolving version for plugin 'org.codehaus.mojo:versions-maven-plugin'")) {
                    return MavenGoalExecutionFailureErrorCodes.NEXUS_DEPE_MISSING
                }

            }

        }

        return null
    }

    @NonCPS
    static String isASonarScanFailureDueToAPluginDownloadIssue(Exception exception) {

        if (! exception instanceof MavenGoalExecutionException) {
            return false
        }

        MavenGoalExecutionFailureError error = ((MavenGoalExecutionException) exception).mavenError

        if (error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.SONAR_SCAN_CONNECTIVITY_ISSUE) {

            for(String errorLog in error.errors) {

                if (errorLog.contains("Failed to execute goal org.sonarsource.scanner.maven:sonar-maven-plugin")) {
                    return MavenGoalExecutionFailureErrorCodes.SONAR_PLG_DOWLD
                }

            }

        }

        return null
    }

    @NonCPS
    static String isASonarScanFailureDueToASonarConnectivityIssue(Exception exception) {

        if (! exception instanceof MavenGoalExecutionException) {
            return false
        }

        MavenGoalExecutionFailureError error = ((MavenGoalExecutionException) exception).mavenError

        if (error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.SONAR_SCAN_CONNECTIVITY_ISSUE) {

            for(String errorLog in error.errors) {

                if (errorLog.contains("Failed to execute goal org.sonarsource.scanner.maven:sonar-maven-plugin") && errorLog.contains("Fail to request")) {
                    return MavenGoalExecutionFailureErrorCodes.SONAR_CONN_ISSUE
                }

            }

        }

        return null
    }

}
