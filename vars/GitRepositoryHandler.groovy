import com.project.alm.*

class GitRepositoryHandler implements Serializable {

    private final def scriptContext
    private final String gitRepoUrl
    private final String checkoutBranch
    private final gitWorkspace
    private final sanitizedGitWorkspace
    private final Map parameters

    private final boolean sslVerify
    private final boolean verbose
    private final int showNLogEntries

    private String gitVersion
    private String gitRepoSecureUrl
    private String gitProjectRelativePath
    private sanitizedGitProjectRelativePath
    private GitUtils utils
    private FileUtils fileUtils
    private boolean initialized = false


    GitRepositoryHandler(scriptContext, String gitRepoUrl, Map parameters = [:]) {
        this.scriptContext = scriptContext
        this.gitRepoUrl = gitRepoUrl
        this.parameters = parameters
        this.checkoutBranch = parameters?.checkoutBranch ? parameters?.checkoutBranch : "master"
        this.gitWorkspace = parameters?.gitWorkspace ? parameters?.gitWorkspace : '.'
        this.sanitizedGitWorkspace = gitWorkspace.replace(' ', '\\ ')
        this.sslVerify = parameters?.sslVerify ? parameters?.sslVerify : false
        this.verbose = parameters?.verbose ? parameters?.verbose : true
        this.showNLogEntries = parameters?.showNLogEntries ? parameters?.showNLogEntries : 0
    }

    String getGitProjectRelativePath() {

        if ( ! initialized ) {
            initialize()
        }

        return gitProjectRelativePath

    }

    GitRepositoryHandler initialize() {

        this.gitVersion = scriptContext.sh(script: "git --version", returnStdout: true).replace("git version", "").trim()

        this.utils = new GitUtils(scriptContext, this.sslVerify)
        this.fileUtils = new FileUtils(scriptContext)
        this.gitRepoSecureUrl = this.utils.getSecuredGitRepoUrl(this.gitRepoUrl)
        this.gitProjectRelativePath = parameters?.gitProjectRelativePath ? parameters?.gitProjectRelativePath : getProjectNameFromGitUrl(gitRepoSecureUrl)
        this.sanitizedGitProjectRelativePath = gitProjectRelativePath.replace(' ', '\\ ')

        String relativeGitProjectPath = "${this.sanitizedGitWorkspace}/${this.sanitizedGitProjectRelativePath}"
        this.fileUtils.createPathIfNotExists(relativeGitProjectPath)

        initialized = true
        scriptContext.printOpen("GitRepositoryHandler.initialize(): ${this.gitRepoUrl} initialized. Git version is ${gitVersion}", EchoLevel.DEBUG)

        return this

    }

    GitRepositoryHandler fastInitializeAndCheckout(String branch) {
        this.gitVersion = scriptContext.sh(script: "git --version", returnStdout: true).replace("git version", "").trim()
        this.utils = new GitUtils(scriptContext, this.sslVerify)
        this.gitRepoSecureUrl = this.utils.getSecuredGitRepoUrl(this.gitRepoUrl)    
        this.gitProjectRelativePath = parameters?.gitProjectRelativePath ? parameters?.gitProjectRelativePath : getProjectNameFromGitUrl(gitRepoSecureUrl)
        this.sanitizedGitProjectRelativePath = gitProjectRelativePath.replace(' ', '\\ ')    
        
        def gitInitResult = scriptContext.sh(script: "git init --initial-branch=$branch", returnStdout: true).trim()
        
        scriptContext.printOpen("Git Init Result: $gitInitResult")

        scriptContext.withCredentials([scriptContext.usernamePassword(credentialsId: GlobalVars.GIT_CREDENTIAL_PROFILE_ID, passwordVariable: GlobalVars.GIT_CREDENTIAL_PASSWORD_VAR, usernameVariable: GlobalVars.GIT_CREDENTIAL_USER_VAR)]) {
            scriptContext.sh(script: "git config http.sslVerify $sslVerify")
            scriptContext.sh(script: "git config user.email $GlobalVars.GIT_USER_EMAIL")
            scriptContext.sh(script: "git config user.name $GlobalVars.GIT_USER_NAME")
            scriptContext.sh(script: "git config advice.detachedHead false")
            scriptContext.sh(script: "git fetch --no-tags --force --no-progress -- $gitRepoSecureUrl +refs/heads/$branch:refs/remotes/origin/$branch # timeout=10")
            scriptContext.sh(script: "git checkout origin/$branch")          
        }
        
        initialized = true
        scriptContext.printOpen("GitRepositoryHandler.initialize(): $gitRepoUrl initialized. Git version is $gitVersion", EchoLevel.DEBUG)
        return this
    }


    def lockRepoAndDo(def operation, def dealWithError = { err -> throw err }) {

        if ( ! initialized ) {
            initialize()
        }

        try {

            lockRepo(this.gitRepoUrl, true)
            operation()

        } catch(err) {

            dealWithError(err)

        } finally {

            lockRepo(this.gitRepoUrl, false)

        }

    }

    String pullOrClone(Map parameters = [:]) {

        if ( ! initialized ) {
            initialize()
        }

        scriptContext.printOpen("GitRepositoryHandler.pullOrClone(): We are going to update from git repo ${this.gitRepoUrl}", EchoLevel.DEBUG)

        String relativeGitProjectPath = "${this.sanitizedGitWorkspace}/${this.sanitizedGitProjectRelativePath}"

        scriptContext.printOpen("GitRepositoryHandler.pullOrClone(): relativeGitProjectPath is ${relativeGitProjectPath}", EchoLevel.INFO)

		def params = [checkoutBranch: this.checkoutBranch] + parameters
		
        try {
            final String[] gitProjectPathFileList = scriptContext.sh(script: "ls -1 ${relativeGitProjectPath}", returnStdout: true).split()
            boolean isGitProjectPathEmpty = gitProjectPathFileList.length == 0
			
            if (isGitProjectPathEmpty) {

                return cloneFromGit(params)

            } else {

                return pullFromGit(params)

            }

        } catch (err) {

            scriptContext.printOpen("GitRepositoryHandler.pullOrClone(): It seems that there was an error! We'll clean the project path and clone the repo again", EchoLevel.DEBUG)
            scriptContext.printOpen("${Utilities.prettyException(err)}", EchoLevel.DEBUG)

            clearOutGitProjectRelativePath()

            return cloneFromGit(params)
        }
    }

    GitRepositoryHandler pull(Map parameters = [:]) {

        if ( ! initialized ) {
            scriptContext.error "You should clone some repository first! Have you initialized this handler?"
        }

        String remote = parameters?.remote ? parameters?.remote : "origin"
        String remoteBranch = parameters?.remoteBranch ? parameters?.remoteBranch : null

        scriptContext.withCredentials([
            scriptContext.usernamePassword(credentialsId: GlobalVars.GIT_CREDENTIAL_PROFILE_ID, passwordVariable: GlobalVars.GIT_CREDENTIAL_PASSWORD_VAR, usernameVariable: GlobalVars.GIT_CREDENTIAL_USER_VAR)
        ]) {

            utils.removeFromWorkspaceFilesNotAllowedInGitRepos("${this.sanitizedGitWorkspace}")

            String cmd = remote && remoteBranch ? "git pull ${remote} ${remoteBranch} --no-edit" : "git pull --no-edit"

            if ( ! GitUtils.gitHandlerVersionEqualsOrExceeds(gitVersion, "2.0.0")) {

                cmd = remote && remoteBranch ? "GIT_MERGE_AUTOEDIT=no git pull ${remote} ${remoteBranch}" : "GIT_MERGE_AUTOEDIT=no git pull"

            }

            try {

                cmdExecutor([cmd])
                return this

            } catch(GitHandlerExecutionException ex) {

                if (ex.gitError.log.contains("Automatic merge failed; fix conflicts and then commit the result")) {

                    try {
                        def logResult = cmdExecutor([
                            'git log -n 1 --pretty=format:%B'
                        ])
                        def commitMessage = logResult.log

                        String gitAddCmd =
                            GitUtils.gitHandlerVersionEqualsOrExceeds(gitVersion, "2.0.0") ?
                                'git add .' : 'git add --all .'

                        cmdExecutor([
                            'git merge --abort',
                            'git reset HEAD^1',
                            'git stash -u',
                            cmd,
                            'git stash pop',
                            gitAddCmd,
                            "git commit -m '${commitMessage}'"
                        ])

                        return this

                    } catch (GitHandlerExecutionException ex2) {

                        throw new GitHandlerExecutionException(GitHandlerExecutionFailureError.fromGitError(ex2.gitError)
                            .errorType(GitHandlerExecutionFailureErrorType.CONFLICTS_AFTER_PULL),
                            "It seems we had an unrecoverable conflict in the local repo while doing git pull")

                    }

                }

                scriptContext.printOpen("We've got an unexpected exception!\\n\\n${Utilities.prettyException(ex)}", EchoLevel.ERROR)
                throw ex
            }

        }

    }

    GitRepositoryHandler checkout(String branch, Map parameters = [:]) {

        if ( ! initialized ) {
            scriptContext.error "You should clone some repository first! Have you initialized this handler?"
        }

        boolean force = parameters?.force ? parameters.force : false
        boolean newBranch = parameters?.newBranch ? parameters?.newBranch : false

        def params = ""

        if (force) {

            params += " -f "

        }

        if (newBranch) {

            params += " -b "

        }

        cmdExecutor(["git checkout ${params} ${branch}"])

        return this
    }

    GitRepositoryHandler add(String path = ".") {

        if ( ! initialized ) {
            scriptContext.error "You should clone some repository first! Have you initialized this handler?"
        }

        this.utils.removeFromWorkspaceFilesNotAllowedInGitRepos("${this.sanitizedGitProjectRelativePath}")

        if (GitUtils.gitHandlerVersionEqualsOrExceeds(gitVersion, "2.0.0")) {

            cmdExecutor(["git add '${path}'"])

        } else {

            cmdExecutor(["git add --all '${path}'"])

        }

        return this
    }

    GitRepositoryHandler commit(String commitMessage, Map parameters = [:]) {

        if ( ! initialized ) {
            scriptContext.error "You should clone some repository first! Have you initialized this handler?"
        }

        boolean allowEmpty = parameters?.allowEmpty ? parameters?.allowEmpty : false

        if (allowEmpty) {

            cmdExecutor(["git commit -m '${commitMessage}' --allow-empty"])

        } else {

            cmdExecutor(["git diff-index --quiet HEAD || git commit -m '${commitMessage}'"])

        }

        return this

    }

    GitRepositoryHandler commitAndPush(String commitMessage = "", Map parameters = [:]) {

        if ( ! initialized ) {
            scriptContext.error "You should clone some repository first! Have you initialized this handler?"
        }

        String remote = parameters?.remote ? parameters?.remote : "origin"
        String remoteBranch = parameters?.remoteBranch ? parameters?.remoteBranch : null
        boolean allowEmpty = parameters?.allowEmpty ? parameters?.allowEmpty : false
        scriptContext.printOpen("Before Commit", EchoLevel.DEBUG)
        if (allowEmpty) {

            this.commit(commitMessage, [allowEmpty: true])

        } else {

            this.commit(commitMessage)

        }

        String pushCmd = remoteBranch ? "git push ${remote} ${remoteBranch}" : "git push"

        try {

            cmdExecutor([pushCmd])

            return this

        } catch(GitHandlerExecutionException ex) {

            if (ex.gitError.log.contains("[rejected]") && (ex.gitError.log.contains("(fetch first)") || ex.gitError.log.contains("(non-fast-forward)"))) {

                pull(parameters)
                cmdExecutor([pushCmd])

                return this

            } else {

                scriptContext.printOpen("We've got an unexpected exception!\n\n${Utilities.prettyException(ex)}", EchoLevel.ERROR)
                throw ex

            }
        }
    }

    GitRepositoryHandler deleteBranchOnOrigin(String branchName) {

        if ( ! initialized ) {
            scriptContext.error "You should clone some repository first! Have you initialized this handler?"
        }

        cmdExecutor(["git push --delete origin ${branchName}"])
        return this

    }

    GitRepositoryHandler purge() {

        boolean workspaceIsOnRoot = this.sanitizedGitWorkspace == '.'

        scriptContext.printOpen("GitRepositoryHandler.purge(): ${workspaceIsOnRoot ? this.gitProjectRelativePath : this.gitWorkspace} will be now deleted", EchoLevel.DEBUG)

        if (workspaceIsOnRoot) {

            scriptContext.sh "rm -rf ${this.sanitizedGitProjectRelativePath}"

        } else {

            scriptContext.sh "rm -rf ${this.sanitizedGitWorkspace}"

        }

        return this
    }

    private GitRepositoryHandler cloneFromGit(Map parameters = [:]) {

		String checkoutBranch = parameters.checkoutBranch ? parameters.checkoutBranch : this.checkoutBranch
		
		String depth = parameters.depth ? "--depth " + parameters.depth : ""
		
        String relativeGitProjectPath = "${this.sanitizedGitWorkspace}/${this.sanitizedGitProjectRelativePath}"

        scriptContext.printOpen("GitRepositoryHandler.cloneFromGit(): Trying clone of ${checkoutBranch} from ${this.gitRepoUrl} to ${relativeGitProjectPath}", EchoLevel.DEBUG)

        scriptContext.withCredentials([scriptContext.usernamePassword(credentialsId: GlobalVars.GIT_CREDENTIAL_PROFILE_ID, passwordVariable: GlobalVars.GIT_CREDENTIAL_PASSWORD_VAR, usernameVariable: GlobalVars.GIT_CREDENTIAL_USER_VAR)]) {

            executeCommandWithRetries("git -c http.sslVerify=${this.sslVerify} clone ${depth} ${this.verbose ? '--verbose' : ''} ${this.gitRepoSecureUrl} ${relativeGitProjectPath}")

            gitConfig(this.sslVerify, GlobalVars.GIT_USER_NAME, GlobalVars.GIT_USER_EMAIL)

            if (checkoutBranch != "master") {
                checkout(checkoutBranch)
            }

        }

        return this
    }

    private GitRepositoryHandler pullFromGit(Map parameters = [:]) {

		def params = [checkoutBranch: this.checkoutBranch] + parameters
		
        if ( ! initialized ) {
            scriptContext.error "You should clone some repository first! Have you initialized this handler?"
        }

        String relativeGitProjectPath = "${this.sanitizedGitWorkspace}/${this.sanitizedGitProjectRelativePath}"

        scriptContext.printOpen("GitRepositoryHandler.pullFromGit(): Trying pull of ${params.checkoutBranch} from ${this.gitRepoUrl} to ${relativeGitProjectPath}", EchoLevel.DEBUG)

        scriptContext.withCredentials([scriptContext.usernamePassword(credentialsId: GlobalVars.GIT_CREDENTIAL_PROFILE_ID, passwordVariable: GlobalVars.GIT_CREDENTIAL_PASSWORD_VAR, usernameVariable: GlobalVars.GIT_CREDENTIAL_USER_VAR)]) {

            if (params.checkoutBranch != "master") {
                scriptContext.sh "cd ${relativeGitProjectPath} && git -c http.sslVerify=${this.sslVerify} checkout ${params.checkoutBranch}"
            }

            scriptContext.sh "cd ${relativeGitProjectPath} && git -c http.sslVerify=${this.sslVerify} pull"
        }

        return this
    }

    private void gitConfig(boolean sslVerify, String name = GlobalVars.GIT_USER_NAME, String eMail = GlobalVars.GIT_USER_EMAIL) {

        def initGit = [
            "git config http.sslVerify ${sslVerify ? true : false}",
            "git config user.email ${eMail}",
            "git config user.name ${name}"
        ]

        cmdExecutor(initGit)

    }

    GitRepositoryHandler clearOutGitWorkspace() {

        scriptContext.printOpen("GitRepositoryHandler.clearOutGitWorkspace(): Cleaning git workspace on: ${this.gitWorkspace}", EchoLevel.INFO)

        if (gitWorkspace == '.') {

            scriptContext.printOpen("No cleaning will be done on workspace as we are working on root!", EchoLevel.INFO)

        } else {
            
            fileUtils.removeDirectory(this.gitWorkspace)
            fileUtils.createPathIfNotExists(this.gitWorkspace)

        }

        return this
    }

    GitRepositoryHandler clearOutGitProjectRelativePath() {

        if ( ! initialized ) {
            initialize()
        }

        String relativeGitProjectPath = "${this.gitWorkspace}/${this.gitProjectRelativePath}"

        scriptContext.printOpen("GitRepositoryHandler.clearOutGitProjectRelativePath(): Wiping git project directory on: ${relativeGitProjectPath}", EchoLevel.INFO)
        fileUtils.removeDirectory(relativeGitProjectPath)
        fileUtils.createPathIfNotExists(relativeGitProjectPath)

        return this
    }

    String getLastCommitLog(String branchName = 'HEAD') {

        if ( ! initialized ) {
            scriptContext.error "You should clone some repository first! Have you initialized this handler?"
        }

        def result = cmdExecutor(["git log -n 1 --pretty=format:%B ${branchName}"])
        return ((String) result?.log).trim()

    }
	
	String getLastCommitId(String branchName = 'HEAD') {
		
				if ( ! initialized ) {
					scriptContext.error "You should clone some repository first! Have you initialized this handler?"
				}
		
				def result = cmdExecutor(["git rev-parse ${branchName}"])
				return ((String) result?.log).trim()
		
			}

    private String getProjectNameFromGitUrl(String gitRepoUrl) {

        String projectName = gitRepoUrl.substring(gitRepoUrl.lastIndexOf("/") + 1, gitRepoUrl.size())
        projectName = projectName.replace(".git", "")

        scriptContext.printOpen("GitRepositoryHandler.getProjectNameFromGitUrl(): Project name has been resolved to ${projectName}", EchoLevel.INFO)

        return projectName
    }

    private void lockRepo(String name, Boolean marked) {

        if ( ! name ) {
            scriptContext.printOpen("No repository URL specified for this handler. Unable to ${marked ? 'acquire' : 'release'} lock!", EchoLevel.INFO)
            return
        }

        def url = GlobalVars.CONTRACT_MICRO_URL + "/gitlab"

        def body = [
            name  : name,
            marked: marked,
        ]

        def bodyJson = groovy.json.JsonOutput.toJson(body)
        scriptContext.printOpen("Request: " + bodyJson, EchoLevel.DEBUG)

        int attemptNum = 0
        scriptContext.timeout(time: 5, unit: 'MINUTES') {
            scriptContext.retry(10) {
                if (++attemptNum > 1) {
                    scriptContext.sleep(time: 30000, unit: "MILLISECONDS")
                }
                scriptContext.printOpen("Trying to ${marked ? 'acquire' : 'release'} git repository lock for ${name}...", EchoLevel.DEBUG)
                scriptContext.httpRequest consoleLogResponseBody: true,
                    contentType: 'APPLICATION_JSON',
                    httpMode: 'POST',
                    requestBody: bodyJson,
                    url: url,
                    httpProxy: "http://proxyserv.svb.digitalscale.es:8080",
                    validResponseCodes: '200'
            }
        }

        scriptContext.printOpen("${name} repository lock ${marked ? 'acquired' : 'released'}!", EchoLevel.DEBUG)

    }

    Map cmdExecutor(def commands, def sanitizedGitProjectRelativePath = this.sanitizedGitProjectRelativePath, def sanitizedGitWorkspace = this.sanitizedGitWorkspace) {

        scriptContext.withCredentials([
            scriptContext.usernamePassword(credentialsId: GlobalVars.GIT_CREDENTIAL_PROFILE_ID, passwordVariable: GlobalVars.GIT_CREDENTIAL_PASSWORD_VAR, usernameVariable: GlobalVars.GIT_CREDENTIAL_USER_VAR)
        ]) {
			scriptContext.printOpen("commands: ${commands}", EchoLevel.DEBUG)
            String cmd = cmdComposer(commands, sanitizedGitProjectRelativePath, sanitizedGitWorkspace)
			scriptContext.printOpen("cmdComposer: ${cmd}", EchoLevel.DEBUG)

            String outputFilename = "${scriptContext.env.WORKSPACE}@tmp/gitHandlerExecutionOutput.log"

            Integer status = scriptContext.sh(returnStatus: true, script: "#!/bin/bash -xe\n(set -o pipefail && ((${cmd}) > ${outputFilename}))")

            String fullLog = scriptContext.readFile(outputFilename)

            scriptContext.printOpen("fullLog: ${fullLog}", EchoLevel.DEBUG)

            scriptContext.sh "rm ${outputFilename}"

            def executionResult = [
                status: status,
                log: fullLog,
                commands: commands
            ]

            if (this.verbose) scriptContext.printOpen("Git execution completed:\n\n${executionResult}", EchoLevel.DEBUG)

            if ( ! status ) {
                return executionResult
            }
			
            GitHandlerExecutionFailureError gitError =
                GitHandlerExecutionFailureError.create()
                .status(status)
                .log(fullLog)
                .commands(commands)
                .errorType(GitHandlerExecutionFailureErrorType.UNKNOWN)
				.sanitize(scriptContext)
            scriptContext.printOpen("Caught git execution error!\n\n${gitError.prettyPrint()}", EchoLevel.ERROR)

            String message = "We got an error code different from 0 when executing a command\n\n${gitError.prettyPrint()}"
            throw new GitHandlerExecutionException(gitError, message)

        }

    }

    private String cmdComposer(def commands, def sanitizedGitProjectRelativePath = this.sanitizedGitProjectRelativePath, def sanitizedGitWorkspace = this.sanitizedGitWorkspace) {

        String cmd = "cd ${sanitizedGitWorkspace}/${sanitizedGitProjectRelativePath}"

        if (this.showNLogEntries) cmd += " && git log -n ${this.showNLogEntries}"

        for (String command : commands) {
            cmd += " && ${command}"
        }

        if (this.showNLogEntries) cmd += " && git log -n ${this.showNLogEntries}"

        cmd = cmd.replace("git push", "git -c http.sslVerify=${this.sslVerify} push")
        cmd = cmd.replace("git pull", "git -c http.sslVerify=${this.sslVerify} pull")
        cmd = cmd.replace("git clone", "git -c http.sslVerify=${this.sslVerify} clone")
        cmd = cmd.replace("git fetch", "git -c http.sslVerify=${this.sslVerify} fetch")
        cmd = cmd.replace("git log", "git -c http.sslVerify=${this.sslVerify} log")
        cmd = cmd.replace("git checkout", "git -c http.sslVerify=${this.sslVerify} checkout")

        return cmd
    }

    private void executeCommandWithRetries(String cmd) {

        int retryNumber = GlobalVars.GIT_COMMAND_MAX_RETRIES_DUE_TO_EXCEPTION

        scriptContext.waitUntil(initialRecurrencePeriod: 15000) {

            boolean pathIsCurrentDirectory = this.sanitizedGitProjectRelativePath == "." || this.sanitizedGitProjectRelativePath == "./"

            String stdOutFilename = pathIsCurrentDirectory ? "../stdout.txt" : "stdout.txt"

            try {

                Utilities.runShCapturingError(scriptContext, [
                    commandString: cmd,
                    stdOutFilename: stdOutFilename
                ])

                return true

            } catch(err) {

                boolean shallWeStop = --retryNumber == 0

                scriptContext.printOpen("Shell command execution failed: ${Utilities.prettyException(err)}", EchoLevel.ERROR)

                if (shallWeStop) {

                    scriptContext.printOpen("We've got an unexpected exception!\n\n${Utilities.prettyException(err)}", EchoLevel.ERROR)
                    throw err

                }

                return false

            }

        }

    }

}
