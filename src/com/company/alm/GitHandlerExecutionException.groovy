package com.project.alm


class GitHandlerExecutionException extends RuntimeException {

    final GitHandlerExecutionFailureError gitError

    GitHandlerExecutionException(GitHandlerExecutionFailureError gitError) {
        this.gitError = gitError
    }

    GitHandlerExecutionException(GitHandlerExecutionFailureError gitError, String message) {
        super(message)
        this.gitError = gitError
    }

    GitHandlerExecutionException(GitHandlerExecutionFailureError gitError, String message, Throwable cause) {
        super(message, cause)
        this.gitError = gitError
    }

    GitHandlerExecutionException(GitHandlerExecutionFailureError gitError, Throwable cause) {
        super(cause)
        this.gitError = gitError
    }

    GitHandlerExecutionException(GitHandlerExecutionFailureError gitError, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace)
        this.gitError = gitError
    }

    String prettyPrint() {

        return "GitHandlerExecutionException:\n"+
          "\n" +
          "\nError details: ${this.gitError.prettyPrint()}"+
          "\nException message: ${this.message ? this.message : '<No message>'}"

    }

}
