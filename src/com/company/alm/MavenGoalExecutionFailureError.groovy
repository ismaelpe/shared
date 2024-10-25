package com.caixabank.absis3

class MavenGoalExecutionFailureError {

    String log
    Integer status
    MavenGoalExecutionFailureErrorType mavenGoalExecutionFailureErrorType = MavenGoalExecutionFailureErrorType.UNKNOWN

    def errors = []


    MavenGoalExecutionFailureError log(String log) {
        this.log = log
        return this
    }

    MavenGoalExecutionFailureError status(Integer status) {
        this.status = status
        return this
    }

    MavenGoalExecutionFailureError errorType(MavenGoalExecutionFailureErrorType mavenGoalExecutionFailureErrorType) {
        this.mavenGoalExecutionFailureErrorType = mavenGoalExecutionFailureErrorType
        return this
    }

    MavenGoalExecutionFailureError errors(def errors) {
        this.errors = errors
        return this
    }

    String prettyPrint() {

        return "MavenGoalExecutionFailureError:\n"+
          "\n"+
          "status: ${this.status}\n"+
          "mavenGoalExecutionFailureErrorType: ${this.mavenGoalExecutionFailureErrorType}\n"+
          "errors: ${this.errors}\n"

    }

}
