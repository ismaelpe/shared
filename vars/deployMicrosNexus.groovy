import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.BranchType
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.NexusUtils
import com.project.alm.ArtifactSubType
import com.project.alm.ArtifactType
import com.project.alm.WorkspaceUtils
import com.project.alm.MavenUtils

def call(PomXmlStructure pomXml, PipelineData pipeline) {
	//Solo despliega el micro aplicativo
    if (((pomXml.artifactSampleApp!=null && pomXml.artifactSampleApp!="") ||
		 (pomXml.artifactMicro!=null && pomXml.artifactMicro!="")) && 
		  pipeline.deployFlag == true && 
		 ((pipeline.branchStructure.branchType == BranchType.FEATURE && pipeline.deployOnCloud) || 
		  pipeline.branchStructure.branchType == BranchType.HOTFIX || pipeline.branchStructure.branchType == BranchType.MASTER || pipeline.branchStructure.branchType == BranchType.RELEASE)) {
        //Se aplicara el true en caso de deploy
        printOpen("Deploying micro to Artifactory...", EchoLevel.INFO)

		def cmd = ""
		def repositoryUrl = ""
		def repositoryName = ""
		if (pomXml.isSNAPSHOT()) {
			repositoryUrl = GlobalVars.MVN_SNAPSHOT_DEPLOYMENT_REPO_URL;
			repositoryName = GlobalVars.MVN_SNAPSHOT_DEPLOYMENT_REPO_NAME;
		} else {
			repositoryUrl = GlobalVars.MVN_RELEASE_DEPLOYMENT_REPO_URL;
			repositoryName = GlobalVars.MVN_RELEASE_DEPLOYMENT_REPO_NAME;
		}
		if (pomXml.artifactType==ArtifactType.AGREGADOR) {
			def artifactIdToDeploy = ""
			if(pomXml.artifactSampleApp != "") {
				artifactIdToDeploy = pomXml.artifactSampleApp
			} else {
				artifactIdToDeploy = pomXml.artifactMicro
			}
			
			def jarPath = ""
			if (fileExists("./${artifactIdToDeploy}/c:/temp/${artifactIdToDeploy}-${pomXml.artifactVersion}.jar")) {
				printOpen("Deploy jar from C:/temp folder", EchoLevel.ALL)
				jarPath = "./${artifactIdToDeploy}/c:/temp/${artifactIdToDeploy}-${pomXml.artifactVersion}.jar"
			} else {
				printOpen("Deploy jar from target folder", EchoLevel.ALL)
				jarPath = "./${artifactIdToDeploy}/target/${artifactIdToDeploy}-${pomXml.artifactVersion}.jar"
			}
			cmd = "mvn <Only_Maven_Settings> deploy:deploy-file -Durl=${repositoryUrl} -DrepositoryId=${repositoryName} -Dfile=${jarPath} -DpomFile=./${artifactIdToDeploy}/pom.xml -DgeneratePom=false"
		} else {
			def jarPath = ""
			if (fileExists("./c:/temp/${pomXml.artifactName}-${pomXml.artifactVersion}.jar")) {
				printOpen("Deploy jar from C:/temp folder", EchoLevel.ALL)
				jarPath = "./c:/temp/${pomXml.artifactName}-${pomXml.artifactVersion}.jar"
			} else {
				printOpen("Deploy jar from target folder", EchoLevel.ALL)
				jarPath = "./target/${pomXml.artifactName}-${pomXml.artifactVersion}.jar"
			}

			cmd = "mvn <Only_Maven_Settings> deploy:deploy-file -Durl=${repositoryUrl} -DrepositoryId=${repositoryName} -Dfile=${jarPath} -DpomFile=./pom.xml -DgeneratePom=false"
		}

        def commitLog = runMavenGoalWithRetries(pomXml, pipeline, cmd, [
            archiveLogIfMvnDurationExceeds: 20,
            kpiAlmEvent: new KpiAlmEvent(
                pomXml, pipeline,
                KpiAlmEventStage.UNDEFINED,
                KpiAlmEventOperation.MVN_DEPLOY_MICROS_NEXUS)
        ])

		if (pipeline.deployOnCloud) {
			printOpen("Se procede a recoger el build code", EchoLevel.ALL)
			
			def artifactDeployedOnNexus = NexusUtils.extractArtifactsFromLog(commitLog)
			
			if (artifactDeployedOnNexus != null) {
	
				pipeline.routesToNexus = artifactDeployedOnNexus
	
				artifactDeployedOnNexus.each {
					printOpen("${it}", EchoLevel.ALL)
				}
	
				if (pomXml.isSNAPSHOT()) {
					//Requerimos el build id
					pipeline.buildCode = NexusUtils.getBuildId(artifactDeployedOnNexus, pomXml.artifactName + '-', pomXml.getArtifactVersionWithoutQualifier() + '-')
					printOpen("pomXml.getArtifactVersionWithoutQualifier es ${pomXml.getArtifactVersionWithoutQualifier()}", EchoLevel.ALL)
					
				}
				printOpen("The micro has been deployed to artifactory. Build code: ${pipeline.buildCode}", EchoLevel.INFO)
			}
		}

    } else {
        printOpen("Skipping deploy Artifact in Artifactory...", EchoLevel.ALL)
    }
}
