import com.caixabank.absis3.GlobalVars

def call() {

    def cmd = "mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Default_Maven_Settings> dependency:tree"
    runMavenCommand(cmd)

}
