import com.project.alm.EchoLevel
import com.project.alm.GitUtils
import com.project.alm.MavenUtils
import com.project.alm.PomXmlStructure

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
