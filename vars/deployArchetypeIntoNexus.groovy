import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GitUtils
import com.caixabank.absis3.MavenUtils
import com.caixabank.absis3.PomXmlStructure

def call(String archetypeModel, PomXmlStructure pomXmlStructure = null) {

    printOpen("${archetypeModel}-based archetype is being deployed into Nexus.", EchoLevel.ALL)

    String gitVersion = sh(script: "git --version", returnStdout: true).replace("git version", "").trim()

    def cmd = "mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Default_Maven_Settings> -f ${archetypeModel}/target/generated-sources/archetype/pom.xml clean deploy"

    if (pomXmlStructure) {

        def deploymentRepo = MavenUtils.getDeploymentRepository(pomXmlStructure.artifactVersion)
        deploymentRepo = deploymentRepo.replace("::default::", "::")
        cmd = "$cmd $deploymentRepo"

    }

    runMavenCommand(cmd)

}
