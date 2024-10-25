import com.project.alm.*

abstract class AbstractQueueHandler implements Serializable {

    final String QH_WORKSPACE = "/opt/jenkins/shared-queues"
    static final String QH_ASYNC_QUEUE_PATH = "async"
    static final String QH_SYNC_QUEUE_PATH = "sync"

    protected final def scriptContext
    protected final GitRepositoryHandler gitRepoHandler
    protected String lastFileAdded2SyncQueue
    protected final fileUtils

    AbstractQueueHandler(scriptContext, gitRepoUrl, gitRepoPath) {
        this.scriptContext = scriptContext
        this.gitRepoHandler = new GitRepositoryHandler(scriptContext, gitRepoUrl, [checkoutBranch: 'master', gitProjectRelativePath: gitRepoPath])
        this.fileUtils = new FileUtils(scriptContext)
    }

    void initQueuePaths() {
        this.scriptContext.printOpen("${this.getClass()}.initQueuePaths(): If not present, queue paths will be created", EchoLevel.INFO)
        this.fileUtils.createPathIfNotExists("${QH_WORKSPACE}/${this.getQueueName()}/${QH_ASYNC_QUEUE_PATH}")
        this.fileUtils.createPathIfNotExists("${QH_WORKSPACE}/${this.getQueueName()}/${QH_SYNC_QUEUE_PATH}")
    }

    AbstractQueueHandler send2SyncQueue(String tarballPath, int priority, boolean moveFile) {

        String syncQueuePath = "${QH_WORKSPACE}/${this.getQueueName()}/${QH_SYNC_QUEUE_PATH}"
        this.lastFileAdded2SyncQueue = send2Queue(syncQueuePath, tarballPath, priority, moveFile)

        return this
    }

    AbstractQueueHandler send2AsyncQueue(String tarballPath, int priority, boolean moveFile) {

        String aSyncQueuePath = "${QH_WORKSPACE}/${this.getQueueName()}/${QH_ASYNC_QUEUE_PATH}"
        send2Queue(aSyncQueuePath, tarballPath, priority, moveFile)

        return this
    }

    protected String send2Queue(String queuePath, String tarballPath, int priority, boolean moveFile) {

        String filename = tarballPath.substring(tarballPath.lastIndexOf("/") + 1)
        String filenameOnQueue = "${priority}-${com.project.alm.DatesAndTimes.getLocalTimeDateNowAsString()}-${filename}"
        String tarballPathOnQueue = "${queuePath}/${filenameOnQueue}"

        String sanitizedQueuePath = queuePath.replace(' ', '\\ ')
        String sanitizedTarballPath = tarballPath.replace(' ', '\\ ')
        String sanitizedTarballPathOnQueue = tarballPathOnQueue.replace(' ', '\\ ')

        if (moveFile) {
            scriptContext.sh(script: "mv ${sanitizedTarballPath} ${sanitizedTarballPathOnQueue}", returnStdout: true)
        } else {
            scriptContext.sh(script: "cp -p ${sanitizedTarballPath} ${sanitizedTarballPathOnQueue}", returnStdout: true)
        }

        scriptContext.printOpen("${this.getClass()}.send2Queue(): ${tarballPath} has been ${moveFile ? 'moved' : 'copied'} to ${tarballPathOnQueue}", EchoLevel.DEBUG)
        String dirContent = scriptContext.sh(script: "ls -la ${sanitizedQueuePath}", returnStdout: true)
        scriptContext.printOpen("Directory content:\n${dirContent}", EchoLevel.DEBUG)

        return filenameOnQueue
    }

    void triggerSyncPublishing() {
        this.triggerSyncPublishing(300)
    }

    void triggerSyncPublishing(int timeout) {
        def response = scriptContext.httpRequest consoleLogResponseBody: true, httpMode: 'GET', url: this.getJenkinsUpdaterJobName(), validResponseCodes: '201'
        scriptContext.printOpen("${this.getClass()}.triggerSyncPublishing(): Response: " + response, EchoLevel.DEBUG)

        try {
            scriptContext.timeout(time: timeout, unit: 'SECONDS') {
                while (true) {
                    int pollingInterval = 5
                    scriptContext.printOpen("Waiting ${pollingInterval} seconds for ${lastFileAdded2SyncQueue} to be published...", EchoLevel.DEBUG)
                    scriptContext.sleep(pollingInterval)
                    if (syncQueueHasBeenPublished()) {
                        scriptContext.printOpen("${lastFileAdded2SyncQueue} has been published! Execution will continue...", EchoLevel.DEBUG)
                        break
                    }
                }
            }
            scriptContext.printOpen("Publishing finished!", EchoLevel.INFO)

        } catch (err) {
            scriptContext.printOpen("Publishing failed\n\nError:\n\n${err}", EchoLevel.ERROR)
            throw err
        }
    }

    void publishSyncQueue() {
        scriptContext.printOpen("${this.getClass()}.publishSyncQueue(): Synchronously publishing...", EchoLevel.DEBUG)
        String syncQueuePendingTarballs = this.getQueuePendingTarballList(QH_SYNC_QUEUE_PATH)
        this.publishTarballList(syncQueuePendingTarballs)
    }

    void triggerAsyncPublishing() {
        def response = scriptContext.httpRequest consoleLogResponseBody: true, httpMode: 'GET', url: this.getJenkinsUpdaterJobName(), validResponseCodes: '201'
        scriptContext.printOpen("${this.getClass()}.triggerAsyncPublishing(): Response: " + response, EchoLevel.DEBUG)
    }

    void publishAsyncQueue() {
        scriptContext.printOpen("${this.getClass()}.publishAsyncQueue(): Asynchronously publishing...", EchoLevel.DEBUG)
        String asyncQueuePendingTarballs = this.getQueuePendingTarballList(QH_ASYNC_QUEUE_PATH)
        this.publishTarballList(asyncQueuePendingTarballs)
    }

    void publishSyncAndAsyncQueues() {
        scriptContext.printOpen("${this.getClass()}.publishSyncAndAsyncQueues(): Synchronously and asynchronously publishing...", EchoLevel.DEBUG)
        String syncQueuePendingTarballs = this.getQueuePendingTarballList(QH_SYNC_QUEUE_PATH)
        String asyncQueuePendingTarballs = this.getQueuePendingTarballList(QH_ASYNC_QUEUE_PATH)
        this.publishTarballList(syncQueuePendingTarballs + asyncQueuePendingTarballs)
    }

    void publishTarballList(String pendingTarballs) {

        if (Strings.lsOrFindOutputToArray(pendingTarballs)?.isEmpty()) {
            scriptContext.printOpen("${this.getClass()}.publishTarballList(): No pending tarballs to be published! Aborting...", EchoLevel.INFO)
        } else {
            scriptContext.printOpen("${this.getClass()}.publishTarballList(): Publishing the current tarball listing to the Git repo...", EchoLevel.DEBUG)

            String gitRepoBaseFolder = gitRepoHandler.pullOrClone([depth: 1])
            untarPendingTarballsTo(gitRepoBaseFolder, pendingTarballs)

            commitAndPushTarballBundle(gitRepoBaseFolder, pendingTarballs)

            removeProcessedTarballsFromQueue(pendingTarballs)
        }
    }

    void commitAndPushTarballBundle(String gitRepoDirectory, String pendingTarballs) {

        try {
            def commitMessage = "Tarball bundle has been published\n\n${replaceFullPathByTarballType(pendingTarballs)}"
            gitRepoHandler.commitAndPush(commitMessage, gitRepoDirectory, true)
            scriptContext.printOpen("${this.getClass()}.commitAndPushTarballBundle(): Tarball list has been published successfully:\n\n${pendingTarballs}", EchoLevel.INFO)
        } catch (err) {
            scriptContext.printOpen("${this.getClass()}.commitAndPushTarballBundle(): commit has failed!\n\n${pendingTarballs}", EchoLevel.ERROR)
            scriptContext.printOpen("Exception was:\n\n${err}\n\n${err.getMessage()}\n\n${err.getCause()}", EchoLevel.ERROR)
        }
    }

    String getGitRepoURL() {
        String gitRepoUrl = this.getGitRepoUrl().replace(".git", "").concat("/tree/master")
        return gitRepoUrl
    }

    void destroy() {
        gitRepoHandler.purge()
    }

    protected boolean syncQueueHasBeenPublished() {
        String cmdOutput = scriptContext.sh(script: "find ${QH_WORKSPACE}/${this.getQueueName()}/${QH_SYNC_QUEUE_PATH} -name '${lastFileAdded2SyncQueue}'", returnStdout: true)
        return !(cmdOutput.contains(lastFileAdded2SyncQueue))
    }

    protected String replaceFullPathByTarballType(String tarballsList) {
        tarballsList = tarballsList.replace("${QH_WORKSPACE}/${this.getQueueName()}/${QH_ASYNC_QUEUE_PATH}/", "[ASYNC] ")
        tarballsList = tarballsList.replace("${QH_WORKSPACE}/${this.getQueueName()}/${QH_SYNC_QUEUE_PATH}/", "[SYNC] ")
        return tarballsList
    }

    protected String getQueuePendingTarballList(String queuePath) {

        String cmdOutput = scriptContext.sh(script: "find ${QH_WORKSPACE}/${this.getQueueName()}/${queuePath} -name '*.tar' | sort", returnStdout: true)
        scriptContext.printOpen("${this.getClass()}.getQueuePendingTarballList(): Listing the pending tarballs in the ${queuePath} queue...\n\n${cmdOutput}", EchoLevel.DEBUG)
        return cmdOutput
    }

    protected void untarPendingTarballsTo(String gitRepoBaseFolder, String asyncQueuePendingTarballs) {

        for (String tarballFilename in Strings.lsOrFindOutputToArray(asyncQueuePendingTarballs)) {
            scriptContext.printOpen("${this.getClass()}.untarPendingTarballsTo(): Untaring ${tarballFilename} to ${gitRepoBaseFolder}...", EchoLevel.DEBUG)
            String cmdOutput = scriptContext.sh(script: "tar -xvf ${tarballFilename} -C ${gitRepoBaseFolder}", returnStdout: true)
            scriptContext.printOpen("${cmdOutput}", EchoLevel.DEBUG)
        }
    }

    protected String[] removeProcessedTarballsFromQueue(String processedTarballs) {

        scriptContext.printOpen("${this.getClass()}.removeProcessedTarballsFromQueue(): Deleting processed tarballs...\n\n${processedTarballs}", EchoLevel.DEBUG)

        processedTarballs = processedTarballs.replace("\n", " ").trim()
        String sanitizedProcessedTarballs = processedTarballs.replace(' ', '\\ ')

        String rmCmdOutput
        try {
            rmCmdOutput = scriptContext.sh(script: "rm ${sanitizedProcessedTarballs}", returnStdout: true)
            scriptContext.printOpen("${this.getClass()}.removeProcessedTarballsFromQueue(): ${processedTarballs} have been deleted", EchoLevel.INFO)
        } catch (java.lang.Throwable error) {
            scriptContext.printOpen("[WARNING] ${this.getClass()}.removeProcessedTarballsFromQueue(): It seems there was an issue while deleting ${tarballFilename}\nCommand output was:\n\n${rmCmdOutput}\n\nError was:\n\n${error}", EchoLevel.ERROR)
        }
    }

    abstract String getGitRepoUrl()

    abstract String getQueueName()

    abstract String getJenkinsUpdaterJobName()

}
