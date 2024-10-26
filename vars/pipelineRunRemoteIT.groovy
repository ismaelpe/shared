import groovy.transform.Field
import com.project.alm.*
import com.project.alm.DistributionModePRO

@Field Map pipelineParams

@Field String gitURL
@Field String  gitCredentials
@Field String  jenkinsPath

@Field String  originType
@Field String  pathToRepo
@Field String  repoName
@Field String  versionOrBranch
@Field String  artifactSubType
@Field String  artifactType
@Field String  cloudEnv
@Field String  center
@Field String  endpoint
@Field PomXmlStructure pomXmlStructure

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    gitURL = 'https://git.svb.digitalscale.es/'
    gitCredentials = 'GITLAB_CREDENTIALS'
    jenkinsPath = 'alm/services'

    originType = params.originTypeParam
    pathToRepo = params.pathToRepoParam
    repoName = params.repoParam
    versionOrBranch = params.versionOrBranchParam
    artifactSubType = params.artifactSubTypeParam
    artifactType = params.artifactTypeParam
    cloudEnv = params.environmentParam
    center = params.centerParam
    endpoint = params.endpoint

    /**
     * 1. Recoger el artifact
     * 2. Copy config
     * 3. Desplegar a PRO
     * 3.5. Preparar Canario
     */
    pipeline {
        agent {    node(almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
            https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
        }
        //Atencion que en el caso que estemos en un MergeRequest... quizas solo debamos validar la issue
        stages {
            stage('get-git-repo') {
                steps {
                    getGitRepoStep()
                }
            }
            stage('run-remote-it') {
                steps {
                    runRemoteItStep()
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
 * Stage getGitRepoStep
 */
def getGitRepoStep() {
    initGlobalVars(pipelineParams)
    if (CloudVarPipelineCopyType.valueOfVarPipelineCopyType(originType) == CloudVarPipelineCopyType.ORIGIN_TAG) {
        pomXmlStructure = getGitRepo(pathToRepo, '', repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), versionOrBranch, true)
    }else if (CloudVarPipelineCopyType.valueOfVarPipelineCopyType(originType) == CloudVarPipelineCopyType.ORIGIN_BRANCH) {
        pomXmlStructure = getGitRepo(pathToRepo, versionOrBranch, repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false)
    }else {
        pomXmlStructure = getGitRepo(pathToRepo, 'master', repoName, false, ArtifactType.valueOfType(artifactType), ArtifactSubType.valueOfSubType(artifactSubType), '', false)
    }
    calculateArchVersionWithModules(pomXmlStructure)
}

/**
 * Stage runRemoteItStep
 */
def runRemoteItStep() {
    String url
    if ('APIGW-EXTERNO' == endpoint) {
        if (pomXmlStructure.isArchProject()) {
            url = "https://api.${cloudEnv.toLowerCase()}.internal.project.com/tech/alm-alm/arch-service/${pomXmlStructure.getBmxAppId()}"
        }else {
            url = "https://api.${cloudEnv.toLowerCase()}.internal.project.com/tech/alm-alm/${pomXmlStructure.getBmxAppId()}"
        }
    }else {
        if (pomXmlStructure.isArchProject()) {
            url = "https://k8sgateway.${cloudEnv.toLowerCase()}.cloud-${center}.alm.cloud.digitalscale.es/arch-service/${pomXmlStructure.getBmxAppId()}"
        }else {
            url = "https://k8sgateway.${cloudEnv.toLowerCase()}.cloud-${center}.alm.cloud.digitalscale.es/${pomXmlStructure.getBmxAppId()}"
        }
    }
    printOpen("Ejecutamos los tests de integracion contra el micro, cuya url es ${url}", EchoLevel.ALL)
    def additionalParameters = ''

    if ('pro'.equals(cloudEnv.toLowerCase())) {
        withCredentials([string(credentialsId: "ALM_TOKEN_${cloudEnv.toUpperCase()}_V2", variable: 'tokenAlm3')]) {
            additionalParameters += '-P it-pro '
            additionalParameters += '-Dskip-it=true '
            additionalParameters += "-Dauthorization-token=${tokenAlm3} "

            def cmd = "mvn <Default_Maven_Settings> -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} verify -Dmicro-url=${url} -Dskip-ut=true ${additionalParameters} -Denvironment=${cloudEnv}"
            boolean weHaveToGenerateOpenApiClasses =
                WorkspaceUtils.isThereASwaggerContract(this, pomXmlStructure) &&
                    ! WorkspaceUtils.areSwaggerContractClassesGenerated(this, pomXmlStructure)
            if ( ! weHaveToGenerateOpenApiClasses ) cmd += ' -Dcodegen.skip=true '
            performRunRemoteITWithRetries(cmd)
        }
    }else {
        withCredentials([string(credentialsId: "ALM_TOKEN_${cloudEnv.toUpperCase()}_V2", variable: 'tokenAlm3')]) {
            additionalParameters += "-Dauthorization-token=${tokenAlm3} "

            def cmd = "mvn <Default_Maven_Settings> -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} verify -Dmicro-url=${url} -Dskip-ut=true ${additionalParameters} -Denvironment=${cloudEnv} -Dskip-it=false"
            boolean weHaveToGenerateOpenApiClasses =
                WorkspaceUtils.isThereASwaggerContract(this, pomXmlStructure) &&
                    ! WorkspaceUtils.areSwaggerContractClassesGenerated(this, pomXmlStructure)
            if ( ! weHaveToGenerateOpenApiClasses ) cmd += ' -Dcodegen.skip=true '
            performRunRemoteITWithRetries(cmd)
        }
    }
}

/**
 * Stage endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}

