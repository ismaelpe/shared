import com.caixabank.absis3.*

import java.util.regex.Pattern

def call(String currentVersion) {

    boolean isValidVersion = Pattern.compile(BuildType.versionRegex).matcher(currentVersion).find()

    if (!isValidVersion) {
        return "1.0.0"
    }

    String[] chars = currentVersion.split("\\.")
    Integer newMinor = new Integer(chars[1]) + 1
    chars[1] = newMinor.toString()

    return chars.join(".")
}