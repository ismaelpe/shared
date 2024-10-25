import com.caixabank.absis3.EchoLevel

def call() {
    
    printOpen ("Cleaning entire pipeline workspace...", EchoLevel.INFO)    
    
    if (getContext(hudson.FilePath)) {
        cleanWs()
        if (testDirExists("${env.WORKSPACE}@tmp")) {
            printOpen ("Deleting the ${env.WORKSPACE}@tmp", EchoLevel.DEBUG)
            cleanTmp("${env.WORKSPACE}@tmp")
        }
        if (testDirExists("${env.WORKSPACE}@script")) {
            printOpen ("Deleting the ${env.WORKSPACE}@script", EchoLevel.DEBUG)
            cleanTmp("${env.WORKSPACE}@script")
        }

        deleteDir()

    } else {

        printOpen ("No hudson.FilePath context available so we won't try to clean the workspace", EchoLevel.DEBUG)

    }

}

private boolean testDirExists(def dir) {
    def exists = sh(
            script: "test -d ${dir}",
            returnStatus: true
    )

    return (exists == 0)
}
