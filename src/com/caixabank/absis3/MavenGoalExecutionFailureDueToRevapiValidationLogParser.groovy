package com.caixabank.absis3


import java.util.regex.Matcher

class MavenGoalExecutionFailureDueToRevapiValidationLogParser implements MavenGoalExecutionFailureLogParser {

    def parseErrors(String log) {

        def patternForFailure = /(?s)(?=\[INFO] BUILD FAILURE.)(.*)(?<=\[ERROR] Consult the plugin output above for suggestions on how to ignore the found problems.)/
        Matcher matches = log =~ patternForFailure

        int numberOfMatches = matches.getCount()

        def failures = []

        if (numberOfMatches > 0) {

            def result = matches.getAt(0)[0].toString().trim()
            failures += removeBuildFailureLinesFromLog(result)

        }

        return failures

    }

    private static String removeBuildFailureLinesFromLog(String buildFailureCause) {

        def resultArr = buildFailureCause.split("\n")
        def filteredCause = ''

        def causeBeginning = getCauseBeginning(resultArr)
        def causeEnding = getCauseEnding(resultArr)

        for (int i = causeBeginning; i < causeEnding; i++) {
            filteredCause += "${resultArr[i]}\n"
        }

        return filteredCause.trim()
    }

    private static int getCauseBeginning(def resultArr) {
        for (int i = 0; i < resultArr.length; i++) {
            if (resultArr[i].contains('[INFO] BUILD FAILURE')) {
                return i + 5
            }
        }
        return -1
    }

    private static int getCauseEnding(def resultArr) {
        for (int i = 0; i < resultArr.length; i++) {
            if (resultArr[i].contains('[ERROR] Consult the plugin output above for suggestions on how to ignore the found problems.')) {
                return i - 1
            }
        }
        return -1
    }

}
