package com.caixabank.absis3

import com.caixabank.absis3.GlobalVars

class GitHandlerExecutionFailureError {

    String log
    Integer status
    def commands
    GitHandlerExecutionFailureErrorType errorType = GitHandlerExecutionFailureErrorType.UNKNOWN

    def errors = []

    static GitHandlerExecutionFailureError create() {
        return new GitHandlerExecutionFailureError()
    }

    static GitHandlerExecutionFailureError fromGitError(GitHandlerExecutionFailureError gitError) {
        return new GitHandlerExecutionFailureError()
            .status(gitError.status)
            .log(gitError.log)
            .commands(gitError.commands)
            .errorType(gitError.errorType)
    }

    GitHandlerExecutionFailureError log(String log) {
        this.log = log
        return this
    }

    GitHandlerExecutionFailureError status(Integer status) {
        this.status = status
        return this
    }

    GitHandlerExecutionFailureError commands(def commands) {
        this.commands = commands
        return this
    }

    GitHandlerExecutionFailureError errorType(GitHandlerExecutionFailureErrorType errorType) {
        this.errorType = errorType
        return this
    }
	
	GitHandlerExecutionFailureError sanitize(scriptContext) {
		def username = scriptContext.env[GlobalVars.GIT_CREDENTIAL_USER_VAR]
		def password = scriptContext.env[GlobalVars.GIT_CREDENTIAL_PASSWORD_VAR]
		def credential = "$username:$password"
		def obfuscated = "******:******"
		
		if (this.log != null && this.log.length() != 0) {
			this.log = this.log.replaceAll(credential, obfuscated)
		}	
		
		if (this.commands != null) {			
			this.commands.eachWithIndex { command, i ->  this.commands[i] = command.replaceAll(credential, obfuscated) }
		}
		
		return this
	}
	


    String prettyPrint() {

        return "GitHandlerExecutionFailureError:\n"+
          "\n"+
          "status: ${this.status}\n"+
          "errorType: ${this.errorType}\n"+
          "log: ${this.log}\n"+
          "commands: ${this.commands}\n"

    }

}
