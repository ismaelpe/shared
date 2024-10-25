package com.project.alm

interface MavenGoalExecutionFailureLogParser {

    def parseErrors(String log);

}
