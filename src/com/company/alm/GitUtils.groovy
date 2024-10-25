package com.project.alm


class GitUtils {

    private final def scriptContext
    private final def sslVerify

    GitUtils(scriptContext, sslVerify = true) {
        this.scriptContext = scriptContext
        this.sslVerify = sslVerify
    }

    void checkNoFilesWithChangesPendingToBeMerged(String workingDirectory = "./") {

        def files = getFilenamesWithChangesPendingToBeMerged(workingDirectory)
        if (files) {
            scriptContext.error "We have found the following files with changes pending to be merged:\n\n${files}"
        }

    }

    String getFilenamesWithChangesPendingToBeMerged(String workingDirectory = "./") {

        def outputFilename = "${scriptContext.env.WORKSPACE}@tmp/shCommandExecutionOutput.log"

        try {


            int status = scriptContext.sh(returnStatus: true, script: "grep -rnw '${workingDirectory}' -e '<<<<<<< HEAD' > ${outputFilename}")
            String fullLog = scriptContext.readFile(outputFilename)
            scriptContext.sh "rm ${outputFilename}"

            if (status) {
                return ''
            }

            return fullLog.replace(":<<<<<<< HEAD", "")

        } catch (err) {

            scriptContext.printOpen("GitUtils.getFilenamesWithChangesPendingToBeMerged threw exception:", EchoLevel.ERROR)
            scriptContext.printOpen(err.getMessage(), EchoLevel.ERROR)

            def sw = new StringWriter()
            def pw = new PrintWriter(sw)
            err.printStackTrace(pw)
            scriptContext.printOpen(sw.toString(), EchoLevel.ERROR)
            throw err

        }

    }

    String getLastCommitId(String branchName, String workingDirectory = null) {

        def commitId
        def cmd = ""

        try {

            if (workingDirectory) {
               cmd = "cd ${workingDirectory} && "
            }

            cmd += "git log origin/${branchName} --format=\"%H\" -n 1"

            commitId = scriptContext.sh(script: cmd, returnStdout: true)
            commitId = commitId?.trim()

            scriptContext.printOpen("GitUtils.getLastCommitId: last commit of branch ${branchName} is ${commitId}", EchoLevel.ALL)

        } catch (err) {

            scriptContext.printOpen("GitUtils.getLastCommitId threw exception while retrieving last commit of branch ${branchName}\n\n${Utilities.prettyException(err)}", EchoLevel.ERROR)
            throw err

        }

        return commitId
    }

    void removeFromWorkspaceFilesNotAllowedInGitRepos(String baseDirectory = "./") {

        if (GlobalVars.GIT_FILE_REMOVAL_REPO_EXCLUSION_LIST.contains(baseDirectory)) {
            scriptContext.printOpen("GitUtils.removeFromWorkspaceFilesNotAllowedInGitRepos: ${baseDirectory} directory is in the deletion exclusion list. No files will be deleted", EchoLevel.DEBUG)
            return
        }

        GlobalVars.GIT_FILES_TO_BE_REMOVED.each() {

            scriptContext.printOpen("GitUtils.removeFromWorkspaceFilesNotAllowedInGitRepos: ${it} is not allowed to be uploaded to repos. It will be deleted", EchoLevel.DEBUG)
			String removeLog = scriptContext.sh(returnStdout: true, script: "cd ${baseDirectory} && rm -f ${it}")
			scriptContext.printOpen(removeLog, EchoLevel.DEBUG)
        }

    }

    String getSecuredGitRepoUrl(String gitUrl) {

        String urlRepo = ''

        scriptContext.withCredentials([
            scriptContext.usernamePassword(credentialsId: 'GITLAB_CREDENTIALS', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')
        ]) {
            if (gitUrl) {

                urlRepo = "https://${scriptContext.env.GIT_USERNAME}:${scriptContext.env.GIT_PASSWORD}@" + gitUrl.replace('https://', '')

            } else if (scriptContext.env.GIT_URL) {

                urlRepo = "https://${scriptContext.env.GIT_USERNAME}:${scriptContext.env.GIT_PASSWORD}@" + scriptContext.env.GIT_URL.replace('https://', '')

            }

            scriptContext.printOpen("The url is ${urlRepo}", EchoLevel.ALL)
        }

        return urlRepo
    }

    void updateGitCode(String gitlabMergeRequestLastCommit) {

        scriptContext.printOpen("Downloading code from git [commitId: ${gitlabMergeRequestLastCommit}] ...", EchoLevel.INFO)
        
        String checkoutLog = scriptContext.sh(returnStdout: true, script: "git checkout -f ${gitlabMergeRequestLastCommit}")
        scriptContext.printOpen("Checkout log:\n${checkoutLog}", EchoLevel.DEBUG)
        
        scriptContext.printOpen("The code has been downloaded", EchoLevel.INFO)
    }

    /**
     * STATIC METHODS
     */

    static String getProjectFromUrl(String gitUrl) {
        String gitProject = ""

        if (gitUrl == null) return gitUrl

        int index = gitUrl.lastIndexOf('/')

        if (index != -1) {
            gitProject = gitUrl.substring(index + 1) - '.git'
        } else gitProject = gitUrl - '.git'

        return gitProject
    }


    static boolean isBpiRepo(String gitUrl) {
        if (gitUrl != null && (gitUrl.indexOf('cbk/absis3/services/apps/bpi') != -1 || gitUrl.indexOf('cbk/absis3/services/definitions/cics') != -1)) return true
        else return false
    }

    static boolean isBpiArchRepo(String gitUrl) {
        if (gitUrl != null && gitUrl.indexOf('cbk/absis3/services/arch/bpi') != -1) return true
        else return false
    }

    static boolean isCbkRepo(String gitUrl) {
        if (gitUrl.indexOf('cbk/absis3/services/apps/cbk') != -1) return true
        else return false
    }

    static boolean isCbkArchRepo(String gitUrl) {
        if (gitUrl.indexOf('cbk/absis3/services/arch/cbk') != -1) return true
        else return false
    }

    static String getDomainFromUrl(String gitUrl) {

        if (gitUrl == null) return ""
        def lastIndex = gitUrl.lastIndexOf('/')
        if (lastIndex != -1) {
            //Necesiamos la penultima
            String valor = gitUrl.substring(0, gitUrl.lastIndexOf('/'))
            if (isBpiRepo(gitUrl)) {
                //si es url bpi el domain lo tienen antes
                lastIndex = valor.lastIndexOf('/')
                valor = valor.substring(0, lastIndex)
            }
            lastIndex = valor.lastIndexOf('/')

            if (lastIndex != -1) {
                return valor.substring(lastIndex + 1)
            } else return ""

        } else return ""
    }

    static String getCompanyFromUrl(String gitUrl) {

        if (gitUrl == null) return ""

        if (isBpiRepo(gitUrl) || isBpiArchRepo(gitUrl)) {
            return "BPI"
        } else if (isCbkRepo(gitUrl) || isCbkArchRepo(gitUrl)) {
            return "CBK"
        }

        return "CORP"
    }

    static Map getConfigRepoUrlAndBranch(String environment) {

        String repoUrl = GlobalVars.GIT_CONFIG_REPO_URL_TST

        String branch = "master"

        switch (environment) {
            case GlobalVars.EDEN_ENVIRONMENT:
                branch = "dev"
                break
            case GlobalVars.DEV_ENVIRONMENT:
                branch = "dev"
                break
            case GlobalVars.TST_ENVIRONMENT:
                break
            case GlobalVars.PRE_ENVIRONMENT:
                repoUrl = GlobalVars.GIT_CONFIG_REPO_URL_PRE
                break
            case GlobalVars.PRO_ENVIRONMENT:
                repoUrl = GlobalVars.GIT_CONFIG_REPO_URL_PRO

        }

        return [
            url: repoUrl,
            branch: branch
        ]
    }

    static Map getGitCertsRepoUrlAndBranch(String environment) {

        String repoUrl = GlobalVars.GIT_CERTS_REPO_URL_TST

        String branch = "master"

        switch (environment) {
            case GlobalVars.EDEN_ENVIRONMENT:
                branch = "dev"
                break
            case GlobalVars.DEV_ENVIRONMENT:
                branch = "dev"
                break
            case GlobalVars.TST_ENVIRONMENT:
                break
            case GlobalVars.PRE_ENVIRONMENT:
                repoUrl = GlobalVars.GIT_CERTS_REPO_URL_PRE
                break
            case GlobalVars.PRO_ENVIRONMENT:
                repoUrl = GlobalVars.GIT_CERTS_REPO_URL_PRO

        }

        return [
            repoUrl: repoUrl,
            branch: branch
        ]

    }

    static String getConfigSysRepoUrl(String environment) {

        switch (environment) {
            case GlobalVars.DEV_ENVIRONMENT:
                return GlobalVars.GIT_CONFIG_REPO_TTSS_URL_TST
            case GlobalVars.TST_ENVIRONMENT:
                return GlobalVars.GIT_CONFIG_REPO_TTSS_URL_TST
            case GlobalVars.PRE_ENVIRONMENT:
                return GlobalVars.GIT_CONFIG_REPO_TTSS_URL_PRE
            case GlobalVars.PRO_ENVIRONMENT:
                return GlobalVars.GIT_CONFIG_REPO_TTSS_URL_PRO
            default:
                return GlobalVars.GIT_CONFIG_REPO_TTSS_URL_TST

        }

    }

    static boolean gitHandlerVersionEqualsOrExceeds(String gitVersion, String version) {

        String[] handlerVersion = gitVersion.split("\\.")
        String[] enquiredVersion = version.split("\\.")

        int handlerMajor = handlerVersion[0] as Integer
        int handlerMinor = handlerVersion[1] as Integer
        int handlerFix = handlerVersion[2] as Integer

        int enquiredMajor = enquiredVersion[0] as Integer
        int enquiredMinor = enquiredVersion[1] as Integer
        int enquiredFix = enquiredVersion[2] as Integer

        if (handlerMajor > enquiredMajor) return true
        else if (handlerMajor < enquiredMajor) return false

        if (handlerMinor > enquiredMinor) return true
        else if (handlerMinor < enquiredMinor) return false

        if (handlerFix > enquiredFix) return true
        else if (handlerFix < enquiredFix) return false

        return true
    }

}
