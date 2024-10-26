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
    call(pomXml,pipeline,"")
}

def call(PomXmlStructure pomXml, PipelineData pipeline, String mvnInputParameters) {
    call(pomXml,pipeline,mvnInputParameters,false)
}
def call(PomXmlStructure pomXml, PipelineData pipeline, String mvnInputParameters, boolean isStressPackage) {

    printOpen("${pipeline.deployFlag} ${pipeline.deployOnCloud} ${pomXml.artifactType.toString()} ${pomXml.artifactSubType.toString()}", EchoLevel.DEBUG)

    if (isStressPackage || (pipeline.deployFlag == true && ( pipeline.branchStructure.branchType == BranchType.PROTOTYPE  || pipeline.branchStructure.branchType == BranchType.HOTFIX || pipeline.branchStructure.branchType == BranchType.MASTER || pipeline.branchStructure.branchType == BranchType.RELEASE) &&
        !( pipeline.deployOnCloud && ( !pipeline.branchStructure.branchType == BranchType.PROTOTYPE && pomXml.artifactType == ArtifactType.SIMPLE && (pomXml.artifactSubType == ArtifactSubType.MICRO_APP || pomXml.artifactSubType == ArtifactSubType.MICRO_ARCH) ))) 
        ) {
        //Se aplicara el true en caso de deploy
        //Esto se tiene que desplegar a menos que tenga la sample app
        def mvnParameters = mvnInputParameters


        //Se tiene que recoger el id del build solo si es SNAPSHOT
        def commitLog = null

        boolean testJarsApplyAsPerMicroType = (pomXml.artifactSubType == ArtifactSubType.MICRO_APP || pomXml.artifactSubType == ArtifactSubType.MICRO_ARCH || pomXml.artifactSubType == ArtifactSubType.SAMPLE_APP)
        boolean notWhitelistedForTestJars = ! env.ALM_SERVICES_IT_TEST_JARS_GENERATION_WHITELIST.contains(pomXml.artifactName)

        //Si se trata de un prototipo generamos el jar de prototype
        if (pipeline.branchStructure.branchType == BranchType.PROTOTYPE) {
            mvnParameters = " ${mvnParameters} -P generate-alm-prototype-jar "
            printOpen("Deploying prototype jar to Nexus", EchoLevel.INFO)
        //Si se trata de un micro y no una libreria entonces generamos los jars para tests de integracion, tienen sufijo test.jar
        //Si es un despliegue en Cloud, realmente lo hemos desplegado antes
        } else if (testJarsApplyAsPerMicroType && notWhitelistedForTestJars) {
            mvnParameters = " ${mvnParameters} -P generate-alm-it-test-jars "
            printOpen("Deploying artifact with tests jars to Artifactory...", EchoLevel.INFO)
        } else {
            printOpen("Deploying artifact without tests jars to Artifactory...", EchoLevel.INFO)
        }


        //-DskipTests ya que se hizo en fase de build, previa a este paso
        // No se despliega la sampleApp

        def archVersion = ""
        def archArtifactId = ""

        if (!pomXml.isArchArtifact()) {
            archVersion = " -DarchVersion=${pomXml.archVersion} "
            archArtifactId = " -DarchArtifactId=${GlobalVars.ARCH_ARTIFACT} "
        }

        def deployGoals = pipeline?.jenkinsFileParams?.maven?.cleanInstallBeforeDeploy ? "clean deploy" : "deploy"

        /* Cuidado, deben mantenerse los dos parametros "-Dmaven.test.skip=true -DskipTests" para evitar la compilacion de tests pero tambien
           la ejecucion en caso de realizarse con un target ya generado anteriormente */
        //def cmd = "mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Only_Maven_Settings> ${mvnParameters} ${archVersion} ${archArtifactId} ${deployGoals} -Dskip-generate-static-docs=${pipeline.getExecutionMode().skipJavadoc()} -Dmaven.test.skip=true -Dmaven.install.skip -DskipTests"
        def cmd = "mvn  <Only_Maven_Settings> ${mvnParameters} ${archVersion} ${archArtifactId} ${deployGoals} -Dskip-generate-static-docs=${pipeline.getExecutionMode().skipJavadoc()} -Dmaven.test.skip=true -Dmaven.install.skip -DskipTests"
        boolean weHaveToGenerateOpenApiClasses =
            WorkspaceUtils.isThereASwaggerContract(this, pomXml) &&
                ! WorkspaceUtils.areSwaggerContractClassesGenerated(this, pomXml)
        if ( ! weHaveToGenerateOpenApiClasses ) cmd += " -Dcodegen.skip=true "

        commitLog = runMavenGoalWithRetries(pomXml, pipeline, cmd, [
            archiveLogIfMvnDurationExceeds: 60,
            kpiAlmEvent: new KpiAlmEvent(
                pomXml, pipeline,
                KpiAlmEventStage.UNDEFINED,
                KpiAlmEventOperation.MVN_DEPLOY_NEXUS)
        ])
        
        printOpen("The artifact has been deployed to Artifactory", EchoLevel.INFO)

        if (pipeline.deployOnCloud && pomXml.artifactType==ArtifactType.SIMPLE) {
            printOpen("El buildCode ha sido recogido de forma previa al deploy", EchoLevel.DEBUG)
        }else {
            def artifactDeployedOnNexus = NexusUtils.extractArtifactsFromLog(commitLog)
                
            if (artifactDeployedOnNexus != null) {
        
                pipeline.routesToNexus = artifactDeployedOnNexus
            
                artifactDeployedOnNexus.each {
                    printOpen("${it}", EchoLevel.DEBUG)
                }
            
                if (pomXml.isSNAPSHOT()) {
                   //Requerimos el build id
                   pipeline.buildCode = NexusUtils.getBuildId(artifactDeployedOnNexus, pomXml.artifactName + '-', pomXml.getArtifactVersionWithoutQualifier() + '-')
                }
                printOpen("Build code: ${pipeline.buildCode}", EchoLevel.DEBUG)
            }
        }

    } else {
        printOpen("Skipping deploy Artifact in Artifactory", EchoLevel.INFO)
    }

}
