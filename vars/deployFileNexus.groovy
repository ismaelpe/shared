import com.caixabank.absis3.*

/**
 * Sube un fichero generando un pom.xml
 * En base a la version calcula si debe ir a snapshots o releases
 */
def call(String groupId, String artifactId, String version, String packaging, String file) {

    def repositoryId
    
    if (MavenVersionUtilities.isSNAPSHOT(version)) {
        repositoryId = GlobalVars.NEXUS_SNAPSHOTS_REPO_NAME
        repositoryIdServer = "maven-snapshots"
    } else {
        repositoryId = GlobalVars.NEXUS_RELEASES_REPO_NAME
        repositoryIdServer = "maven-releases"
    }

    def repositoryUrl = GlobalVars.NEXUS_URL + "artifactory/" + repositoryId

    def deploymentRepo = MavenUtils.getDeploymentRepository(artifactId)

    //def cmd = "mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Default_Maven_Settings> deploy:deploy-file -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${version} -DrepositoryId=${repositoryIdServer} -Durl=${repositoryUrl} -DgeneratePom=true -Dpackaging=${packaging} -Dfile=${file} ${deploymentRepo}"
	def cmd = "mvn <Default_Maven_Settings> deploy:deploy-file -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${version} -DrepositoryId=${repositoryIdServer} -Durl=${repositoryUrl} -DgeneratePom=true -Dpackaging=${packaging} -Dfile=${file} ${deploymentRepo}"
	
    runMavenCommand(cmd)
}

def call(String groupId, String artifactId, String version, String packaging, String file, PipelineData pipeline) {

    def mvnLog = null
    def repositoryId

    if (MavenVersionUtilities.isSNAPSHOT(version)) {
        repositoryId = GlobalVars.NEXUS_SNAPSHOTS_REPO_NAME
        repositoryIdServer = "maven-snapshots"
    } else {
        repositoryId = GlobalVars.NEXUS_RELEASES_REPO_NAME
        repositoryIdServer = "maven-releases"
    }

    def repositoryUrl = GlobalVars.NEXUS_URL + "artifactory/" + repositoryId

    def deploymentRepo = MavenUtils.getDeploymentRepository(artifactId)

    def cmd = "mvn <Only_Maven_Settings> deploy:deploy-file -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${version} -DrepositoryId=${repositoryIdServer} -Durl=${repositoryUrl} -DgeneratePom=true -Dpackaging=${packaging} -Dfile=${file} ${deploymentRepo}"
    mvnLog = runMavenCommand(cmd)

    def artifactDeployedOnNexus = NexusUtils.extractArtifactsFromLog(mvnLog)
    pipeline.routesToNexus = artifactDeployedOnNexus
}

def call(String groupId, String artifactId, String version, String packaging, String file, String pomfile, PipelineData pipeline) {

	def mvnLog=null
	def repositoryId

    if (MavenVersionUtilities.isSNAPSHOT(version)) {
        repositoryId = GlobalVars.NEXUS_SNAPSHOTS_REPO_NAME
        repositoryIdServer = "maven-snapshots"
    } else {
        repositoryId = GlobalVars.NEXUS_RELEASES_REPO_NAME
        repositoryIdServer = "maven-releases"
    }

	def repositoryUrl=GlobalVars.NEXUS_URL + "artifactory/" + repositoryId

    def deploymentRepo = MavenUtils.getDeploymentRepository(artifactId)

    def cmd = "mvn <Only_Maven_Settings> deploy:deploy-file -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${version} -DrepositoryId=${repositoryIdServer} -Durl=${repositoryUrl} -DgeneratePom=true -Dpackaging=${packaging} -Dfile=${file} -DpomFile=${pomfile} ${deploymentRepo}"
    mvnLog = runMavenCommand(cmd)

	def artifactDeployedOnNexus=NexusUtils.extractArtifactsFromLog(mvnLog)
	pipeline.routesToNexus=artifactDeployedOnNexus
}

