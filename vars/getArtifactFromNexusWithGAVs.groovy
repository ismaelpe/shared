import com.caixabank.absis3.EchoLevel

def call(String groupId, String artifactId, String version, String outputDirectory, boolean useBaseSnapshot = true) {

    //def cmd = "mvn dependency:copy -Dartifact=${groupId}:${artifactId}:${version} -DoutputDirectory=${outputDirectory} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Default_Maven_Settings> -Dmdep.useBaseVersion=${useBaseSnapshot}"
	def cmd = "mvn dependency:copy -Dartifact=${groupId}:${artifactId}:${version} -DoutputDirectory=${outputDirectory}  <Default_Maven_Settings> -Dmdep.useBaseVersion=${useBaseSnapshot}"
    runMavenCommand(cmd)

    printOpen("Artifact downloaded!!! ${groupId}:${artifactId}:${version}", EchoLevel.ALL)
}

def call(String groupId, String artifactId, String version, String classifier, String outputDirectory, boolean useBaseSnapshot = true) {

    def cmd = "mvn dependency:copy -Dartifact=${groupId}:${artifactId}:${version}:${classifier} -DoutputDirectory=${outputDirectory}  <Default_Maven_Settings> -Dmdep.useBaseVersion=${useBaseSnapshot}"
    runMavenCommand(cmd)

	printOpen("Artifact downloaded!!! ${groupId}:${artifactId}:${version}:${classifier}", EchoLevel.ALL)
}
