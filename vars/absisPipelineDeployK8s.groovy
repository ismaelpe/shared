import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.MavenVersionUtilities
import com.project.alm.PipelineData
import com.project.alm.GlobalVars
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineData
import com.project.alm.BranchStructure
import com.project.alm.ArtifactType
import com.project.alm.ArtifactSubType
import com.project.alm.BranchType
import com.project.alm.GarAppType
import com.project.alm.GlobalVars
import com.project.alm.PipelineStructureType
import com.project.alm.ICPStateUtility
import com.project.alm.ICPDeployStructure
import com.project.alm.ICPWorkflowStates
import com.project.alm.ICPVarPipelineCopyType
import com.project.alm.DevBmxStructure
import com.project.alm.TstBmxStructure
import com.project.alm.PreBmxStructure
import com.project.alm.ProBmxStructure
import com.project.alm.RunRemoteITBmxStructure
import com.project.alm.ICPApiResponse

@Field Map pipelineParams

@Field PomXmlStructure pomXmlStructure
@Field PipelineData pipelineData

@Field String pathToRepo
@Field String repoName
@Field String icpApplicationId
@Field String majorVersion
@Field String artifactSubType
@Field String artifactType
@Field String originBranch

@Field String microservice
@Field String versionTag
@Field String environmentDest
@Field String electionOriginArtifact
@Field String executionMode

@Field String imageIcp


//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters

    pathToRepo = ""
    repoName = ""
    icpApplicationId = ""
    majorVersion = ""
    artifactSubType = "MICRO_ARCH"
    artifactType = "SIMPLE"
    originBranch = "master"

    microservice = params.Microservice
    versionTag = params.versionParam
    environmentDest = params.environmentDestParam
    electionOriginArtifact = params.electionOriginArtifactParam
    executionMode = params.executionModeParam

	imageIcp=""
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
	    options{ 
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
            timeout(time: 3, unit: 'HOURS')
        }
	    environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            ICP_CERT = credentials('icp-absis3-pro-cert')
            ICP_PASS = credentials('icp-absis3-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
		}		
		stages {			
			stage('init'){
				steps{
					initStep()
				}
			}		
			stage('build'){
				steps{
					buildStep()
				}
			}			
			stage('build-icp-image'){
				steps{
					buildIcpImageStep()
				}
			}			
			stage('deploy-icp-image'){
				steps{
					deployIcpImage()
				}
			}
		}
        post {           
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

	pathToRepo = "https://git.svb.lacaixa.es/cbk/absis3/services/arch/tool/${microservice}.git"
	repoName = microservice - "micro"
	icpApplicationId = "27587"
	if (microservice == "k8sapigateway-micro") {
		icpApplicationId="3921"
	}
	if (microservice == "jnkmsv-micro") {
		icpApplicationId="42533"
	}
	if (microservice == "redirecttodev-micro") {
		icpApplicationId="55714"
	}
	printOpen("microservice ${microservice} ${icpApplicationId}", EchoLevel.INFO)
	majorVersion = MavenVersionUtilities.getMajor(versionTag)
	printOpen( "El tag de la version es de #${versionTag}# ${versionTag.length()}", EchoLevel.INFO)
		
	if (ICPVarPipelineCopyType.valueOfVarPipelineCopyType(electionOriginArtifact)==ICPVarPipelineCopyType.ORIGIN_TAG) {
		printOpen("Con version ${versionTag} ", EchoLevel.ALL)
		pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), versionTag, true)
	}else {
		printOpen("Sin version", EchoLevel.ALL)
		pomXmlStructure = getGitRepo(pathToRepo, originBranch, repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false)
	}
	
	pipelineData = new PipelineData(PipelineStructureType.CI, "${env.BUILD_TAG}")
	BranchStructure branchStructure=getBranchInfo(originBranch)
	pipelineData.init(branchStructure, artifactSubType, artifactType,false, './')						
	pipelineData.initDomainProperties(pathToRepo)
	
	printOpen("domain ${pipelineData.domain}", EchoLevel.ALL)
	printOpen("subDomain ${pipelineData.subDomain}", EchoLevel.ALL)
	
	pipelineData.deployFlag = true
	pipelineData.deployOnIcp=true
		
	debugInfo(pipelineParams, pomXmlStructure, pipelineData)
	
	pipelineData.garArtifactType=pipelineData.initFromGitUrlGarApp(pathToRepo, ArtifactSubType.valueOfSubType(artifactSubType))
	
	String artifactApp=pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name) )

	
	if (environmentDest!="NONE") {
			if (environmentDest == GlobalVars.DEV_ENVIRONMENT) pipelineData.bmxStructure = new DevBmxStructure()
			else if (environmentDest == GlobalVars.TST_ENVIRONMENT) pipelineData.bmxStructure = new TstBmxStructure()
			else if (environmentDest == GlobalVars.PRE_ENVIRONMENT) pipelineData.bmxStructure = new PreBmxStructure()
			else if (environmentDest == GlobalVars.PRO_ENVIRONMENT) pipelineData.bmxStructure = new ProBmxStructure()
			
			//Las ramas no estan correctas... un deploy a un entorno que no sea el pertineente se tiene que comportar como tal
			//Por ejemplo si es una rama feature y se depliega en dev tiene que ser master
			//Solo una rama release se puede desplegar en tst,pre,pro
			
			if 	(environmentDest == GlobalVars.DEV_ENVIRONMENT) pipelineData.branchStructure.branchType = BranchType.MASTER
			else if (environmentDest == GlobalVars.TST_ENVIRONMENT ||
						environmentDest == GlobalVars.PRE_ENVIRONMENT ||
						environmentDest == GlobalVars.PRO_ENVIRONMENT) pipelineData.branchStructure.branchType = BranchType.RELEASE
	}
	
	if (ICPVarPipelineCopyType.valueOfVarPipelineCopyType(electionOriginArtifact)==ICPVarPipelineCopyType.ORIGIN_TAG){
		//Viene de un tag vamos a modificar el valor de la version
		pomXmlStructure.artifactVersion=versionTag
	}
	
	currentBuild.displayName = "Build_${env.BUILD_ID}_" + pipelineData.getPipelineBuildName() +" "+pomXmlStructure.getApp(GarAppType.valueOfType(pipelineData.garArtifactType.name) )+" "+pomXmlStructure.artifactVersion
		
}

/**
 * Stage 'buildStep'
 */
def buildStep() {
	if ((ICPVarPipelineCopyType.valueOfVarPipelineCopyType(executionMode)==ICPVarPipelineCopyType.EX_MODE_DEPLOY ||
		ICPVarPipelineCopyType.valueOfVarPipelineCopyType(executionMode)==ICPVarPipelineCopyType.EX_MODE_ALL) &&
		ICPVarPipelineCopyType.valueOfVarPipelineCopyType(electionOriginArtifact)!=ICPVarPipelineCopyType.ORIGIN_TAG) {
		printOpen('deployMicroNexus', EchoLevel.INFO)
		buildWorkspace(pomXmlStructure, pipelineData)
		deployMicrosNexus(pomXmlStructure, pipelineData)
	}
}

/**
 * Stage 'buildIcpImageStep'
 */
def buildIcpImageStep() {
	//Build 
	def body = [
			extraArgs: "GROUP_ID=com.caixabank.absis.arch.tool,VERSION_ARTIFACT=${pomXmlStructure.artifactVersion},ARTIFACT_ID=${microservice}",
			version: "${pomXmlStructure.artifactVersion}"
	]	
	ICPApiResponse response=null
	if (microservice == "redirecttodev-micro") {
		response=sendRequestToICPApi("v1/type/PCLD/application/AB3APP/component/${icpApplicationId}/build",body,"POST","AB3APP","v1/type/PCLD/application/AB3APP/component/${icpApplicationId}/build",true,false, pipelineData, pomXmlStructure)
	}else {
		response=sendRequestToICPApi("v1/type/PCLD/application/AB3COR/component/${icpApplicationId}/build",body,"POST","AB3COR","v1/type/PCLD/application/AB3COR/component/${icpApplicationId}/build",true,false, pipelineData, pomXmlStructure)
	}
	
	if (response.statusCode<200 || response.statusCode>300) {
		throw new Exception("Error status code ${response.statusCode}")
	}else {
		imageIcp=response.body.imageRepo1
	}
	printOpen("La imagen es de ${imageIcp}",EchoLevel.INFO)
}

/**
 * Stage 'deployIcpImage'
 */
def deployIcpImage() {
	//Build
	printOpen("Se limpia el pipeline", EchoLevel.ALL)
	pipelineData.componentId = icpApplicationId

	def variableKubernetes="almlogcollector${majorVersion}"
	def variableKubernetesInstance="almlogcollector-micro-${majorVersion}"
		
	if (microservice == "k8sapigateway-micro") {
		variableKubernetes="k8sgateway"
		variableKubernetesInstance="k8sgateway"
	}

	if (microservice == "jnkmsv-micro") {
		variableKubernetes="jnkmsv"
		variableKubernetesInstance="jnkmsv"
	}

	if (microservice == "redirecttodev-micro") {
		variableKubernetes="redirecttodev"
		variableKubernetesInstance="redirecttodev"
	}
	def secrets=""
	if (microservice == "jnkmsv-micro") {
		secrets="    secrets:\n      - name: cbk-jenkins\n      - name: cbk-gitlab\n      - name: cbk-jnkmsv\n      - name: cbk-jenkins-token-vip\n      - name: cbk-jenkins-token-normal\n"
	} else if (microservice == "k8sapigateway-micro") {
		secrets="    secrets:\n      - name: cbk-k8sgateway\n"
	}

	def bodyDeploy=[
		az: "ALL",
		environment: "${environmentDest.toUpperCase()}",
		values: "local:\n  app:\n    ingress:\n      connectTimeout: 10\n      readTimeout: 240\n      sendTimeout: 240\n      defineTimeout: true\n      defineBodySize: true\n      maxBodySize: 30m\n      enabled: true\n      deploymentArea: absis\n      absis:\n        enabled: true\n      mtls:\n        enabled: true\n        needsSystemRoute: true\n        needsSpecialVerifyDepth: false\n    envVars:\n      - name: ABSIS_ICP_ENVIRONMENT\n        value: ${environmentDest.toLowerCase()}\n      - name: ABSIS_APP_ID\n        value: ${variableKubernetesInstance}\n      - name: ABSIS_ENVIRONMENT\n        value: ${environmentDest.toUpperCase()}\n      - name: ABSIS_APP_SUBDOMAIN\n        value: NO_SUBDOMAIN\n      - name: ABSIS_APP_COMPANY\n        value: CBK\n      - name: JAVA_OPTS\n        value: '-Dspring.cloud.config.failFast=false'\n      - name: nonProxyHosts\n        value: '*.cxb-pasdev-tst|*.cxb-ab3app-${environmentDest.toLowerCase()}|*.cxb-ab3cor-${environmentDest.toLowerCase()}'\n      - name: http.additionalNonProxyHosts\n        value: 'cxb-pasdev-${environmentDest.toLowerCase()},cxb-ab3app-dev,cxb-ab3cor-${environmentDest.toLowerCase()}'\n      - name: NO_PROXY\n        value: cxb-ab3cor-${environmentDest.toLowerCase()}\n      - name: CF_INSTANCE_INDEX\n        value: 1\n      - name: SPRING_PROFILES_ACTIVE\n        value: cloud,${environmentDest.toLowerCase()},icp\n${secrets}absis:\n  app:\n    loggingElkStack: absis30\n    replicas: 1\n    instance:  ${variableKubernetesInstance}\n    name: ${variableKubernetesInstance}\n  resources:\n    requests:\n      memory: 500Mi\n      cpu: 500m\n    limits:\n       memory: 786Mi\n       cpu: 1000m\n  apps:\n    envQualifier:\n      stable:\n        id:  ${variableKubernetes}\n        colour: G\n        image: ${imageIcp}:${pomXmlStructure.artifactVersion}\n        version: ${pomXmlStructure.artifactVersion}\n        stable: false\n        new: false\n        replicas: 1\n        requests_memory: 100Mi\n        requests_cpu: 25m\n        limits_memory: 600Mi\n        limits_cpu: 700m\n  services:\n    envQualifier:\n      stable:\n        id: ${variableKubernetesInstance}\n        targetColour: G\n"
	]
	
	printOpen("Deploy value ${bodyDeploy}", EchoLevel.ALL)
	
	ICPApiResponse response=null
	
	if (microservice == "redirecttodev-micro") {
		response=sendRequestToICPApi("v1/application/PCLD/AB3APP/component/${icpApplicationId}/deploy",bodyDeploy,"POST","AB3APP","v1/application/PCLD/AB3APP/component/${icpApplicationId}/deploy",true,true, pipelineData, pomXmlStructure)
	}else {
		response=sendRequestToICPApi("v1/application/PCLD/AB3COR/component/${icpApplicationId}/deploy",bodyDeploy,"POST","AB3COR","v1/application/PCLD/AB3COR/component/${icpApplicationId}/deploy",true,true, pipelineData, pomXmlStructure)
	}	
	
	ICPDeployStructure deployStructure=new ICPDeployStructure('cxb-ab3cor','cxb-ab3app',environmentDest)
	if (microservice == "redirecttodev-micro") {
		//Vamos a settear la AB3SPP
		pomXmlStructure.artifactSubType=ArtifactSubType.MICRO_APP
	}
	boolean isReady=waitICPDeploymentReady(pomXmlStructure,pipelineData,deployStructure,"G")
	//Wait
	String microUrl = microservice == "almlogcollector-micro" ?
		"https://k8sgateway.${environmentDest.toLowerCase()}.icp-1.absis.cloud.lacaixa.es/arch-service/${variableKubernetesInstance}" :
		"https://k8sgateway.${environmentDest.toLowerCase()}.icp-1.absis.cloud.lacaixa.es"
	
	if (isReady) 
		validateMicroIsUp(microUrl)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
	attachPipelineLogsToBuild(pomXmlStructure)
	cleanWorkspace()
}
