import com.project.alm.ArtifactSubType
import com.project.alm.ArtifactType
import com.project.alm.BranchStructure
import com.project.alm.BranchType
import com.project.alm.DevBmxStructure
import com.project.alm.EchoLevel
import com.project.alm.GarAppType
import com.project.alm.GlobalVars
import com.project.alm.ICPDeployStructure
import com.project.alm.ICPStateUtility
import com.project.alm.ICPVarPipelineCopyType
import com.project.alm.ICPWorkflowStates
import groovy.transform.Field
import com.project.alm.MavenUtils
import com.project.alm.PipelineData
import com.project.alm.PipelineStructureType
import com.project.alm.PomXmlStructure
import com.project.alm.PreBmxStructure
import com.project.alm.ProBmxStructure
import com.project.alm.Strings
import com.project.alm.TstBmxStructure
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData
@Field boolean successPipeline=false

@Field String pathToRepo = "${pathToRepoParam}"
@Field String repoName = "${repoParam}"

@Field String originBranch = "${originBranchParam}"
@Field String pipelineOrigId = "${pipelineOrigId}"

@Field String environmentDest = "${environmentDestParam}"

@Field String user = "${userId}"
@Field boolean sendToGpl = "${sendToGplParam}".toBoolean()
@Field boolean initGpl = false

@Field String group = "${groupParam}"
@Field String artifact = "${artifactParam}"
@Field String version = "${versionParam}"
@Field KpiAlmEvent almEvent = null
@Field long initCallStartMillis = new Date().getTime()

/**
 * Pipeline para desplegar micros prototipo en un entorno escogido
 * @param pipelineParams
 * @return
 */
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters
    
	successPipeline=false

    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam

    originBranch = params.originBranchParam
	pipelineOrigId = params.pipelineOrigId

    environmentDest = params.environmentDestParam

	user = params.userId
	sendToGpl = params.sendToGplParam.toString().toBoolean()
    initGpl = false
	
	group = params.groupParam
	artifact = params.artifactParam
	version = params.versionParam
	almEvent = null
	initCallStartMillis = new Date().getTime()

    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '20'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
			JNKMSV = credentials('JNKMSV-USER-TOKEN')
            ICP_CERT = credentials('icp-alm-pro-cert')
            ICP_PASS = credentials('icp-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
        }
        stages {
            stage('init') {
                steps {
					initStep()
                }
            }
			stage('check-ICP-availiability'){
				steps {
					checkICPAavailiabilityStep()
				}
			}
            stage('deploy-prototype') {
                steps {
					deployPrototypeStep()
                }
            }
        }
        post {
            success {
                endPipelineSuccessStep()
            }
            failure {
                endPipelineFailureStep()
            }
            always {
                endPipelineAlwaysStep()
            }        
        }
    }
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/**
 * Stage 'initStep'
 */
def initStep() {
	initGlobalVars(pipelineParams)

	pipelineData = new PipelineData(PipelineStructureType.DEPLOY_PROTOTYPE, "${env.BUILD_TAG}", env.JOB_NAME, null)
	
	BranchStructure branchStructure = getBranchInfo(originBranch)
	pipelineData.gitUrl = pathToRepo
	pipelineData.init(branchStructure, ArtifactSubType.MICRO_APP, ArtifactType.SIMPLE, false, null)
	printOpen("garArtifactType is ${pipelineData.garArtifactType.name}", EchoLevel.ALL)
	
	pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, false, ArtifactType.SIMPLE, ArtifactSubType.MICRO_APP, null, false)
		
	printOpen("pomXmlStructure.artifactName is ${pomXmlStructure.artifactName}", EchoLevel.ALL)
	printOpen("pomXmlStructure.artifactMicro is ${pomXmlStructure.artifactMicro}", EchoLevel.ALL)

	almEvent = new KpiAlmEvent(
		pomXmlStructure, pipelineData,
		KpiAlmEventStage.GENERAL,
		KpiAlmEventOperation.PIPELINE_DEPLOY_PROTO)
	
	if (sendToGpl) {
		sendPipelineStartToGPL(pomXmlStructure, pipelineData, pipelineOrigId)
		initGpl = true
		sendStageStartToGPL(pomXmlStructure, pipelineData, "100")
	}
	
	if (environmentDest != "NONE") {
		if (environmentDest == GlobalVars.TST_ENVIRONMENT) pipelineData.bmxStructure = new TstBmxStructure()
		else if (environmentDest == GlobalVars.PRE_ENVIRONMENT) pipelineData.bmxStructure = new PreBmxStructure()
		}

	currentBuild.displayName = "DeployPrototype_to_" + environmentDest + "_${env.BUILD_ID}_" + " " + artifact + " " + version

	
	if (sendToGpl) {
		sendStageEndToGPL(pomXmlStructure, pipelineData, "100")
	}
}

/**
 * Stage 'checkICPAavailiabilityStep'
 */
def checkICPAavailiabilityStep() {
	if (sendToGpl) {
		sendStageStartToGPL(pomXmlStructure, pipelineData, "200")
	}
	printOpen("The artifact ${artifact}  from group ${group} the micro to deploy is ${pomXmlStructure.artifactMicro}", EchoLevel.ALL)
	
	try {
		
		printOpen("environment is ${pipelineData.bmxStructure.environment}", EchoLevel.ALL)
		checkICPAvailability(pomXmlStructure,pipelineData,"CALCULATE","DEPLOY")
		
		if (sendToGpl) {
			sendStageEndToGPL(pomXmlStructure, pipelineData, "200")
		}
	} catch (Exception e) {
		if (sendToGpl) {
			sendStageEndToGPL(pomXmlStructure, pipelineData, "200", Strings.toHtml(e.getMessage()), null, "error")
		}
		throw e
	}
}

/**
 * Stage 'deployPrototypeStep'
 */
def deployPrototypeStep() {
	if (sendToGpl) {
		sendStageStartToGPL(pomXmlStructure, pipelineData, "300")
	}

	try {
		def result = deployPrototypeToKubernetes(group,artifact,version,pomXmlStructure,pipelineData,environmentDest)
		def messageUrl = "Url to access the microservice\n<a href='$result.urlPrototype'>$result.urlPrototype</a>\n$result.messageDeploy"
		
		printOpen("Result deploy prototype: $result.messageDeploy", EchoLevel.INFO)
		
		if (sendToGpl) {
			sendStageEndToGPL(pomXmlStructure, pipelineData, "300", messageUrl, pipelineData.bmxStructure.environment)
		}
	} catch (Exception e) {
		if (sendToGpl) {
			sendStageEndToGPL(pomXmlStructure, pipelineData, "300", Strings.toHtml(e.getMessage()), null, "error")
		}
		throw e
	}
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
	successPipeline=true
	if ( almEvent!=null ) {
		long endCallStartMillis = new Date().getTime()
		kpiLogger(almEvent.pipelineSuccess(endCallStartMillis-initCallStartMillis))
	}
	if (sendToGpl) {
		sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, true)
		sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, true)
	}
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
	successPipeline=false
	if ( almEvent!=null ) {
		long endCallStartMillis = new Date().getTime()
		kpiLogger(almEvent.pipelineFail(endCallStartMillis-initCallStartMillis))
	}
	if (sendToGpl) {
		if (initGpl == false) {
			//init pipeline in GPL with minimun parameters
			sendPipelineStartToGPL(pipelineData, pipelineParams)
			initGpl = true
		}
		sendPipelineResultadoToGPL(initGpl, pomXmlStructure, pipelineData, false)
		sendPipelineEndedToGPL(initGpl, pomXmlStructure, pipelineData, false)
	}
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
	attachPipelineLogsToBuild(pomXmlStructure)
	cleanWorkspace()
}

