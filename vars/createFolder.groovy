import com.caixabank.absis3.GlobalVars

def call(String jenkinsPath, String domain) {
    folder("${jenkinsPath}") {
        displayName("${domain}")
        description("Folder for domain ${domain}")
    }
}