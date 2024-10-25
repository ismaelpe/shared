package com.project.alm


import java.util.regex.Matcher

class MavenGoalExecutionFailureDueToOpenApiGenerationLogParser implements MavenGoalExecutionFailureLogParser {

    def parseErrors(String log) {

        def patternForUnableToLoadContractServerFailure = /(?ms)\[WARNING] Exception while reading:\njava\.lang\.RuntimeException: Unable to load URL ref: https:\/\/contractserver-micro-server-\d+\.pro\.int\.srv\.caixabank\.com.*?^\[/
        Matcher matches = log =~ patternForUnableToLoadContractServerFailure

        if (matches.getCount()) {
            return parseUnableToLoadContractServerURLErrors(matches)
        }

        return []
    }

    def parseUnableToLoadContractServerURLErrors(Matcher matches) {

        int numberOfMatches = matches.getCount()

        def failures = []

        for (int i=0; i<numberOfMatches; i++) {

            def failure = matches[i].toString().trim()
            failures += reduceLogToCausedByes(failure)

        }

        return failures

    }

    def reduceLogToCausedByes(String log) {

        def logPieces = log.split("\n")
        def reducedLog = logPieces[0]+"\n"+logPieces[1]

        for (int i=2; i < logPieces.length; i++) {

            if (logPieces[i].startsWith("Caused by:")) {

                reducedLog += '\n'+logPieces[i]

            }

        }

        return reducedLog
    }

}
