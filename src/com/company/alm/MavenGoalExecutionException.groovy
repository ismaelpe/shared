package com.project.alm


class MavenGoalExecutionException extends RuntimeException {

    MavenGoalExecutionFailureError mavenError

    MavenGoalExecutionException(MavenGoalExecutionFailureError mavenError) {
        this.mavenError = mavenError
    }

    MavenGoalExecutionException(MavenGoalExecutionFailureError mavenError, String message) {
        super(message)
        this.mavenError = mavenError
    }

    MavenGoalExecutionException(MavenGoalExecutionFailureError mavenError, String message, Throwable cause) {
        super(message, cause)
        this.mavenError = mavenError
    }

    MavenGoalExecutionException(MavenGoalExecutionFailureError mavenError, Throwable cause) {
        super(cause)
        this.mavenError = mavenError
    }

    MavenGoalExecutionException(MavenGoalExecutionFailureError mavenError, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace)
        this.mavenError = mavenError
    }

    String prettyPrint() {

        return "MavenGoalExecutionException:\n"+
          "\n" +
          "\nError details: ${this.mavenError.prettyPrint()}"+
          "\nException message: ${this.message ? this.message : '<No message>'}"

    }

}
