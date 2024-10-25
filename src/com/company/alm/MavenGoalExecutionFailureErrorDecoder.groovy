package com.project.alm


import java.util.regex.Matcher

class MavenGoalExecutionFailureErrorDecoder {

    static MavenGoalExecutionFailureError getErrorFromLog(String log, Integer status = null) {

        MavenGoalExecutionFailureError error = new MavenGoalExecutionFailureError().log(log).status(status)

        if (isIntegrationTestError(log)) {

            error.errorType(MavenGoalExecutionFailureErrorType.INTEGRATION_TEST)
                .errors(new MavenGoalExecutionFailureDueToTestsLogParser().parseErrors(log))

        } else if (isBuildTestError(log)) {

            error.errorType(MavenGoalExecutionFailureErrorType.BUILD_TEST)
                .errors(new MavenGoalExecutionFailureDueToTestsLogParser().parseErrors(log))

        } else if (isGenericPluginError(log)) {

            error.errorType(MavenGoalExecutionFailureErrorType.REVAPI_VALIDATION)
                .errors(new MavenGoalExecutionFailureDueToRevapiValidationLogParser().parseErrors(log))

        } else if (isOpenApiGeneratorPluginCodeGenerationError(log)) {

            error.errorType(MavenGoalExecutionFailureErrorType.OPENAPI_GENERATION)
                .errors(new MavenGoalExecutionFailureDueToOpenApiGenerationLogParser().parseErrors(log))

        } else if (isNexusDependencyDownloadError(log)) {

            error.errorType(MavenGoalExecutionFailureErrorType.NEXUS_DOWNLOAD)
                .errors([log])

        } else if (isConnectionTimeoutWhileContactingSonar(log) ||
            isConnectionResetWhileDownloadingPluginFromSonar(log) ||
            isStreamResetWhileDownloadingPluginFromSonar(log)) {

            error.errorType(MavenGoalExecutionFailureErrorType.SONAR_SCAN_CONNECTIVITY_ISSUE)
                .errors([log])

        }

        return error

    }

    static boolean isIntegrationTestError(String log) {

        def patternForIntegrationTestFailure = /\[ERROR] Failed to execute goal.*\(integration-test\).*There are test failures.*/
        Matcher matches = log =~ patternForIntegrationTestFailure
        return matches.getCount() == 1

    }

    static boolean isBuildTestError(String log) {

        def patternForBuildTestFailure = /\[ERROR] Failed to execute goal.*\(default-test\).*There are test failures.*/
        Matcher matches = log =~ patternForBuildTestFailure
        return matches.getCount() == 1

    }

    // This is not optimal, but it's the implementation for revapi validation check we had already
    // If more cases of plugin failures have to dealt with. Revapi error parsing will be reconsidered
    static boolean isGenericPluginError(String log) {

        def patternForPluginFailure = /(?s)(?=\[INFO] BUILD FAILURE.)(.*)(?<=\[ERROR] Consult the plugin output above for suggestions on how to ignore the found problems.)/
        Matcher matches = log =~ patternForPluginFailure
        return matches.getCount() == 1

    }

    static boolean isOpenApiGeneratorPluginCodeGenerationError(String log) {

        def patternForPluginFailure = /(?s)(\[INFO] BUILD FAILURE.*\[ERROR] Failed to execute goal org\.openapitools:openapi-generator-maven-plugin.*Code generation failed)/
        Matcher matches = log =~ patternForPluginFailure
        return matches.getCount() == 1

    }

    static boolean isNexusDependencyDownloadError(String log) {

        def patternForGenericGoalNexusDownloadFailure = /(?s)(\[INFO] BUILD FAILURE.*\[ERROR] Failed to execute goal.*Failed to collect dependencies at)/
        Matcher matchesGenericGoal = log =~ patternForGenericGoalNexusDownloadFailure

        def patternForUnresolveableBuildExtensionNexusDownloadFailure = /(?s)(\[ERROR] The build could not read \d+ project.*\[ERROR]     Unresolveable build extension.*Failed to collect dependencies at)/
        Matcher matchesUnresolveableBuildExtension = log =~ patternForUnresolveableBuildExtensionNexusDownloadFailure

        def patternForResolvingVersionNexusPluginDownloadFailure = /(?s)(\[INFO] BUILD FAILURE.*\[ERROR] Error resolving version.*Plugin not found in any plugin repository)/
        Matcher matchesResolvingVersions = log =~ patternForResolvingVersionNexusPluginDownloadFailure


        return matchesGenericGoal.getCount() == 1 || matchesUnresolveableBuildExtension.getCount() == 1 || matchesResolvingVersions.getCount() == 1
    }

    static boolean isConnectionTimeoutWhileContactingSonar(String log) {

        def patternForPluginFailure = /(?s)(\[INFO] BUILD FAILURE.*\[ERROR] Failed to execute goal org\.sonarsource\.scanner\.maven:sonar-maven-plugin.*Fail to request.*timeout)/
        Matcher matches = log =~ patternForPluginFailure
        return matches.getCount() == 1

    }

    static boolean isConnectionResetWhileDownloadingPluginFromSonar(String log) {

        def patternForPluginFailure = /(?s)(\[INFO] BUILD FAILURE.*\[ERROR] Failed to execute goal org\.sonarsource\.scanner\.maven:sonar-maven-plugin.*Fail to download plugin.*Connection reset)/
        Matcher matches = log =~ patternForPluginFailure
        return matches.getCount() == 1

    }

    static boolean isStreamResetWhileDownloadingPluginFromSonar(String log) {

        def patternForPluginFailure = /(?s)(\[INFO] BUILD FAILURE.*\[ERROR] Failed to execute goal org\.sonarsource\.scanner\.maven:sonar-maven-plugin.*Fail to download plugin.*stream was reset)/
        Matcher matches = log =~ patternForPluginFailure
        return matches.getCount() == 1

    }

}
