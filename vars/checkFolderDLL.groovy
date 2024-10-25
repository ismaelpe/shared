import com.caixabank.absis3.EchoLevel

import java.util.regex.Matcher
import java.util.regex.Pattern

def call(String branchName, String transactionId, String fileDLL) {

    if (!branchName.contains(transactionId)) {
        printOpen("${branchName} is not correct for transactionid uploaded : ${transactionId}. The branch name must have the format: feature/<agileworkId>_<transactionName>_<description>.", EchoLevel.ALL)
        throw new RuntimeException("Branch ${branchName} is not correct for transactionid uploaded : ${transactionId}")
    }

    Pattern pattern = Pattern.compile(".+his" + transactionId + ".+", Pattern.CASE_INSENSITIVE)
    Matcher matcher = pattern.matcher(fileDLL)
    if (!matcher.find()) {
        printOpen("${fileDLL} location is not correct ", EchoLevel.ALL)
        throw new RuntimeException("${fileDLL} location is not correct ")
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
                String editType = file.editType.name.toLowerCase()
                printOpen("Commit entry file: ${editType} ${file.path}", EchoLevel.ALL)
                if (!editType.equals("delete")) {
                    def pathFile = file.path
                    if (pathFile.endsWith(".DLL") || pathFile.endsWith(".dll")) {
                        if (!pathFile.equals(fileDLL)) {
                            printOpen("[${pathFile}] it can't push more than one dll ", EchoLevel.ALL)
                            throw new RuntimeException("[${pathFile}] it can't push more than one dll ")
                        }
                    }
                    if (pathFile.endsWith(".DLL") || pathFile.endsWith(".dll")) {
                        matcher = pattern.matcher(pathFile)
                        if (!matcher.find()) {
                            printOpen("${pathFile} location is not correct ", EchoLevel.ALL)
                            throw new RuntimeException("${pathFile} location is not correct ")
                        }
                    }
                }
            }
        }
    }


    return true
}
