package com.caixabank.absis3

interface MavenGoalExecutionFailureLogParser {

    def parseErrors(String log);

}
