import com.project.alm.EchoLevel

import java.util.regex.Matcher
import java.util.regex.Pattern

def call(String branchName, String operationId, String fileXMLIn, String fileXMLOut) {

    if (!branchName.contains(operationId)) {
        printOpen("${branchName} is not correct for operationId uploaded : ${operationId}", EchoLevel.ALL)
        throw new RuntimeException("Branch ${branchName} is not correct for operationId uploaded : ${operationId}")
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
                def pathFile = file.path
                if (pathFile.endsWith(".XML") || pathFile.endsWith(".xml")) {
                    if (!pathFile.equals(fileXMLIn) && !pathFile.equals(fileXMLOut)) {
                        printOpen("[${pathFile}] - Only IN and OUT xml are allowed", EchoLevel.ALL)
                        throw new RuntimeException("[${pathFile}] - Only IN and OUT xml are allowed")
                    }
                }
            }
        }
    }


    return true
}
