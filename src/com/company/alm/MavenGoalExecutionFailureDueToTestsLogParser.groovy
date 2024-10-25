package com.project.alm


import java.util.regex.Matcher

class MavenGoalExecutionFailureDueToTestsLogParser implements MavenGoalExecutionFailureLogParser {

    def parseErrors(String log) {

        def patternForFailure = /(?ms)(?=\[ERROR] Tests run:)(.*?^\n?$)/
        Matcher matches = log =~ patternForFailure

        int numberOfMatches = matches.getCount()

        def failures = []

        for (int i=0; i<numberOfMatches; i++) {

            failures += matches[i][0].toString().trim()

        }

        return failures

    }

}
