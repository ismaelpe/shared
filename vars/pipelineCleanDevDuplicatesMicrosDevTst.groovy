import groovy.transform.Field
import com.project.alm.*
import groovy.json.JsonSlurperClassic
import com.project.alm.GlobalVars
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation

@Field Map pipelineParams
@Field String user
@Field String targetAlmFolder
@Field String newVersionChart
@Field String newVersionDocker
@Field String numberOfMicrosToClean
@Field String microsToClean

@Field int maxDeaysToClean

@Field ClientInfo clientInfo
@Field PipelineData pipelineData
@Field boolean initGpl
@Field boolean successPipeline
@Field def duplicatedMicrosDev
@Field def existentMicrosTst
@Field def appsPendientes

@Field KpiAlmEvent almEvent
@Field long initCallStartMillis

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters

	user = params.userId
	targetAlmFolder = params.targetAlmFolder
	newVersionChart = params.newVersionChartParam
	newVersionDocker = params.newVersionDockerParam
	numberOfMicrosToClean = params.numberOfMicrosToCleanParam
	microsToClean = microsToCleanParam	
	maxDeaysToClean = maxDaysToCleanParam as Integer

	initGpl = false
	successPipeline = false
	duplicatedMicrosDev = null
	existentMicrosTst = null
	appsPendientes=null

	almEvent = null
	initCallStartMillis = new Date().getTime()
    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
		options {
			buildDiscarder(logRotator(numToKeepStr: '30'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
		}
		environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
			JNKMSV = credentials('JNKMSV-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
			http_proxy = "${GlobalVars.proxyCaixa}"
			https_proxy = "${GlobalVars.proxyCaixa}"
			proxyHost = "${GlobalVars.proxyCaixaHost}"
			proxyPort = "${GlobalVars.proxyCaixaPort}"
			executionProfile = "${executionProfileParam ? executionProfileParam : 'DEFAULT'}"
		}
		//Atencion que en el caso que estemos en un MergeRequest... quizas solo debamos validar la issue
		stages {
			stage('init-data') {
				steps {
					initDataStep()
				}
			}
			stage('get-dev-micros-not-redirected'){
				steps {
					getDevMicrosNotRedirectedStep()
				}
			}
			stage('get-tst-micros'){
				steps {
					getTstMicrosStep()
				}
			}
			stage('clean-micros-redirected'){
				steps {
					cleanMicrosRedirectedStep()
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
 * Stage 'initDataStep'
 */
def initDataStep() {
	initGlobalVars(pipelineParams)
	clientInfo = new ClientInfo()
	clientInfo.setApplicationName(GlobalVars.CAMPAIGN_GAR_APP)
	clientInfo.setArtifactId(GlobalVars.CAMPAIGN_GAR_APP)
	String date = new Date(System.currentTimeMillis()).format("yyyyMMddHHmmss");
	clientInfo.setArtifactVersion("0.0." + date)
	clientInfo.setArtifactType(ArtifactType.SIMPLE)
	clientInfo.setArtifactSubType(ArtifactSubType.GLOBAL_PIPELINE)
	clientInfo.setGroupId("com.project.alm")

	pipelineData = new PipelineData(PipelineStructureType.JOB_CLEAN_DEV_DUPLICATED_PODS, "${env.BUILD_TAG}", env.JOB_NAME, params)
	pipelineData.pushUser = user
	//Cambiar por entorno de DEV para hacer pruebas
	pipelineData.initGlobalPipeline(GlobalVars.PRO_ENVIRONMENT.toString())
	pipelineData.prepareExecutionMode(env.executionProfile, targetAlmFolder);

	almEvent = new KpiAlmEvent(
		clientInfo, pipelineData,
		KpiAlmEventStage.GENERAL,
		KpiAlmEventOperation.PIPELINE_JOB_CLEAN_DEV_DUPLICATED_PODS)
	pipelineData.pipelineStructure.pipelineId = pipelineData.pipelineStructure.pipelineId + "-" + pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage
	initGpl = true
}

/**
 * Stage 'getDevMicrosNotRedirectedStep'
 */
def getDevMicrosNotRedirectedStep() {
	try {
		duplicatedMicrosDev=redirectCatalogUtils.getMicrosNotRedirected(GlobalVars.DEV_ENVIRONMENT.toUpperCase())	
		duplicatedMicrosDev=redirectCatalogUtils.cleanMicrosOnlyOld(duplicatedMicrosDev,maxDeaysToClean)
	}catch(Exception e) {
		printOpen("rror en recuperacio de micros en DEV ${e.getMessage()}", EchoLevel.ALL)
		throw e
	}
}

/**
 * Stage 'initDatgetTstMicrosStepaStep'
 */
def getTstMicrosStep() {
	try {
		existentMicrosTst=redirectCatalogUtils.getMicrosNotRedirected(GlobalVars.TST_ENVIRONMENT.toUpperCase())	
	}catch(Exception e) {
		printOpen("Error en recuperacio de micros en TST ${e.getMessage()}", EchoLevel.ALL)
		throw e
	}
}

/**
 * Stage 'cleanMicrosRedirectedStep'
 */
def cleanMicrosRedirectedStep() {
	try {
		duplicatedMicrosTst=redirectCatalogUtils.redirectAndNotify(duplicatedMicrosDev,existentMicrosTst,numberOfMicrosToClean.toInteger(),microsToClean,newVersionChart.toInteger(),newVersionDocker.toInteger())
	}catch(Exception e) {
		printOpen("Error en la redireccion de los micros de DEV a TST ${e.getMessage()}", EchoLevel.ALL)
		throw e
	}
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
	successPipeline = true
	printOpen("Se success el pipeline ${successPipeline}", EchoLevel.INFO)
	if ( almEvent!=null ) {
		long endCallStartMillis = new Date().getTime()
		kpiLogger(almEvent.pipelineSuccess(endCallStartMillis-initCallStartMillis))
	}
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
	printOpen("Se failure el pipeline ${successPipeline}", EchoLevel.ERROR)
	successPipeline = false 
	if ( almEvent!=null ) {
		long endCallStartMillis = new Date().getTime()
		kpiLogger(almEvent.pipelineFail(endCallStartMillis-initCallStartMillis))
	}	
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
	printOpen("Cleaning pipeline", EchoLevel.ALL)
	cleanWs()
	cleanWorkspace()
}

	