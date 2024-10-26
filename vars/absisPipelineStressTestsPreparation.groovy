import com.project.alm.ArtifactSubType
import com.project.alm.ArtifactType
import com.project.alm.BranchType
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.CloudAppResources
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiLifeCycleStage
import com.project.alm.KpiLifeCycleStatus
import com.project.alm.PipelineData
import com.project.alm.PipelineStructureType
import com.project.alm.PomXmlStructure
import com.project.alm.Strings

def call(Map pipelineParams) {

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo

	PomXmlStructure pomXmlStructure
    PipelineData pipelineData

    boolean successPipeline = false
    boolean initGpl = false

    String cloudEnv = GlobalVars.PRE_ENVIRONMENT
	
	def originBranch = "${originBranchParam}"
	def pathToRepo = "${pathToRepoParam}"
	def repoName = "${repoParam}"
	def artifactType = "${artifactTypeParam}"
	def artifactSubType = "${artifactSubTypeParam}"
	String requestedCPU = "${scaleCPUCoresParam}"
	String requestedMemory = "${scaleMemoryParam}"
	def pipelineOrigId = "${pipelineOrigId}"
	def commitId = "${commitIdParam}"
	String userId = "${userIdParam}"?.trim() ? "${userIdParam}" : "AB3ADM"
	def loggerLevel = "${loggerLevel}"
	def agentParam = "${agent}"

    Map valuesDeployed = null

    pipeline {
        agent { node (almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
        }
        //Environment sobre el qual se ejecuta este tipo de job
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            JNKMSV = credentials('JNKMSV-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
			sendLogsToGpl = true
        }
        stages {
			stage('get-git-repo') {
				steps {
					script {
						initGlobalVars([loggerLevel: loggerLevel])  // pipelineParams arrive as null

						printOpen("Extract GIT Repo ${pathToRepo} ${originBranch}", EchoLevel.DEBUG)
						pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false)
						pipelineData = new PipelineData(PipelineStructureType.STRESS_TESTS_PREPARATION, "${env.BUILD_TAG}", env.JOB_NAME, null)
						pipelineData.commitId = commitId
						pipelineData.initVoidActions(pathToRepo, originBranch, ArtifactSubType.valueOfSubType(artifactSubType), repoName, GlobalVars.PRE_ENVIRONMENT)
						pipelineData.setDefaultAgent(agentParam)
						pipelineData.pushUser = userId

						pipelineData.buildCode = pomXmlStructure.getArtifactVersionQualifier()

						currentBuild.displayName = "${pomXmlStructure.artifactName+pomXmlStructure.getMajorVersion()} of ${cloudEnv.toUpperCase()} and namespace ${pomXmlStructure.getCloudAppName()}"
						kpiLogger(pomXmlStructure, pipelineData, KpiLifeCycleStage.PIPELINE_STARTED, KpiLifeCycleStatus.OK)
						almEvent = new KpiAlmEvent(
							pomXmlStructure, pipelineData,
							KpiAlmEventStage.GENERAL,
							KpiAlmEventOperation.PIPELINE_STRESS_TEST_PREPARATION)
						
						sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
						sendStageStartToGPL(pomXmlStructure, pipelineData, "100")
						initGpl = true

						debugInfo(pipelineParams, pomXmlStructure, pipelineData)

						//INIT AND DEPLOY
						initCloudDeploy(pomXmlStructure, pipelineData)

						sendStageEndToGPL(pomXmlStructure, pipelineData, "100")

					}
				}
			}
			stage("prepare-wiremock-server") {
				steps {
					script {
						sendStageStartToGPL(pomXmlStructure, pipelineData, "200")
						try {
							printOpen("Preparing wiremock server...", EchoLevel.INFO)
							deployWiremockServerToKubernetes(pomXmlStructure, pipelineData, cloudEnv)
							sendStageEndToGPL(pomXmlStructure, pipelineData, "200", null, cloudEnv)
						} catch (Exception e) {
							sendStageEndToGPL(pomXmlStructure, pipelineData, "200", null, cloudEnv, "error")
							throw e
						}
					}
				}
			}
			stage("prepare-stress-micro") {
				steps {
					script {
						sendStageStartToGPL(pomXmlStructure, pipelineData, "300")
						try {
							deployStressMicroToKubernetes(pomXmlStructure, pipelineData, requestedCPU, requestedMemory, cloudEnv)
							sendStageEndToGPL(pomXmlStructure, pipelineData, "300", null, cloudEnv)
						} catch (Exception e) {
							sendStageEndToGPL(pomXmlStructure, pipelineData, "300", null, cloudEnv, "error")
							throw e
						}
					}
				}
			}
        }
        post {
            success {
                script {
                    printOpen("SUCCESS", EchoLevel.INFO)
					sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, true)
                }
            }
            failure {
                script {
                    printOpen("FAILURE", EchoLevel.INFO)
					sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, false)
                }
            }
            always {

                cleanWorkspace()

            }
        }
    }
}
