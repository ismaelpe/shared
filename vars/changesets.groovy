import com.project.alm.EchoLevel

import java.util.regex.Matcher
import java.util.regex.Pattern

def call(String regex, int group) {

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
                Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                Matcher matcher = pattern.matcher(file.editType.name + " " + file.path)
                if (matcher.find()) {
                    return matcher.group(group)
                }
            }
        }
    }

    return null
}
