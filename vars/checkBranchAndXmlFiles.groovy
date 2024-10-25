import com.project.alm.EchoLevel
import com.cloudbees.groovy.cps.NonCPS

import java.util.regex.Matcher
import java.util.regex.Pattern

@NonCPS //access to currentBuild.changeSets requires this as per documentation
def call(String branchName, String transactionId, String fileXML) {

    if (!branchName.contains(transactionId)) {
        printOpen("${branchName} is not correct for transactionid uploaded : ${transactionId}", EchoLevel.ALL)
        throw new RuntimeException("Branch ${branchName} is not correct for transactionid uploaded : ${transactionId}")
    }


    def changeLogSets = currentBuild.changeSets

    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            printOpen("Git commit entry: ${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}", EchoLevel.ALL)
            def files = new ArrayList(entry.affectedFiles)
            for (int k = 0; k < files.size(); k++) {
                def file = files[k]
                printOpen("Commit entry file: ${file.editType.name} ${file.path}", EchoLevel.ALL)
                if ( ! ((String) file.path).startsWith("cifn/repository/") ) {
                    printOpen("We have detected a change unrelated to CIFN. Sometimes we get commit from the jenkinsSharedLibrary . We'll ignore it", EchoLevel.INFO)
                    continue
                }
                def pathFile = file.path
                if (pathFile.endsWith(".XML") || pathFile.endsWith(".xml")) {
                    if (!pathFile.equals(fileXML)) {
                        printOpen("[${pathFile}] - Not allowed to push more than one xml ", EchoLevel.ALL)
                        throw new RuntimeException("[${pathFile}] - Not allowed to push more than one xml ")
                    }
                }
            }
        }
    }


    return true
}
