package com.project.alm

abstract class ResultPipelineData {

    String environment
    String gitUrl
    String gitProject
    boolean deploy
    String component

    String pathRepo
    String repoName
    String pipelineOrigId
    String branchName
    String version
	String oldVersionInCurrentEnvironment
    String oldVersionInNextEnvironment

    String commitId

    String artifact
    ArtifactType artifactType
    ArtifactSubType artifactSubType

    String executionProfile
    String almSubFolder

    String mvnAdditionalParameters

    String retryAuthorizationService
    String retryEnvAuthorization
    String retryAuthorizationServiceToInform

    boolean deployFlag = false
    boolean hasDeployedToCloud = false
	boolean onlyConfig = false
	boolean isTagged = false
	boolean onlyDeploy = false
    boolean remoteITOk = false
	boolean isBBDD = false
	boolean isNowDeployed = false

    DistributionModePRO nextDistributionMode

    String jobName
    Map jobParameters

    String agent

    boolean isBpiRepo() {
        if (GitUtils.isBpiRepo(gitUrl) || GitUtils.isBpiArchRepo(gitUrl)) return true
        else return false
    }

	boolean isAuthExcluded() {
		def exclusionList = GlobalVars.ALM_SERVICES_SKIP_VALIDATION_AUTH_LIST.split(";")
		return "true".equals(GlobalVars.ALM_SERVICES_SKIP_VALIDATION_AUTH_ALL) || Arrays.asList(exclusionList).contains(component)
	}

	    public String incFix(String versionAIncrementar) {
        //La Version debe ser X.Y.Z-RCN
        //La siguiente debe ser X.Y+1.0-SNAPSHOT
        String version = versionAIncrementar
		String versionResultado = versionAIncrementar
		
        String major = null
        String minor = null
        String fix = null
		if(version != null) {
	        version.tokenize('.').each {
	            if (major == null) major = it
	            else if (minor == null) minor = it
	            else if (fix == null) fix = it
	        }
	        if (major != null && minor != null && fix != null) {
	            int fixValue = Integer.parseInt(fix) + 1
	            versionResultado = major + "." + minor + "." + fixValue
	            //if ( this?.revision ) { revision=this.artifactVersion }
	        }
		}

        return versionResultado
    }

    ResultPipelineData() {

    }

    ResultPipelineData(String environment, String gitUrl, String gitProject, boolean deploy) {
        this(environment, gitUrl, gitProject, deploy, "", new HashMap())

    }

    ResultPipelineData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map jobParameters) {
        this.environment = environment
        this.gitUrl = gitUrl
        this.jobName = jobName
        this.jobParameters = jobParameters

        if (gitUrl == null) this.gitProject = gitProject
        else {
            int index = gitUrl.lastIndexOf('/')

            if (index != -1) {
                this.gitProject = gitUrl.substring(index + 1) - '.git'
            } else this.gitProject = gitUrl - '.git'
        }
    }
	
	
	ResultPipelineData(String environment, String gitUrl, String gitProject, boolean deploy, String jobName, Map jobParameters, String branchName) {
		this.environment = environment
		this.gitUrl = gitUrl
		this.jobName = jobName
		this.jobParameters = jobParameters
		this.branchName = branchName
		
		if (gitUrl == null) this.gitProject = gitProject
		else {
			int index = gitUrl.lastIndexOf('/')

			if (index != -1) {
				this.gitProject = gitUrl.substring(index + 1) - '.git'
			} else this.gitProject = gitUrl - '.git'
		}
	}

    def isMicro() {
        if ((artifactType == ArtifactType.AGREGADOR && (artifactSubType == ArtifactSubType.PLUGIN_STARTER ||
            artifactSubType == ArtifactSubType.STARTER ||
            artifactSubType == ArtifactSubType.PLUGIN ||
            artifactSubType == ArtifactSubType.ARCH_LIB ||
			artifactSubType == ArtifactSubType.ARCH_LIB_WITH_SAMPLEAPP ||
            artifactSubType == ArtifactSubType.APP_LIB) ||
			artifactSubType == ArtifactSubType.APP_LIB_WITH_SAMPLEAPP) ||
            (artifactType == ArtifactType.SIMPLE && (artifactSubType == ArtifactSubType.ARCH_LIB ||
                artifactSubType == ArtifactSubType.APP_LIB)
            ) ||
            (artifactType == ArtifactType.SIMPLE && (artifactSubType == ArtifactSubType.ARCH_CFG ||
                artifactSubType == ArtifactSubType.SRV_CFG)
            )
        ) return false
        else return true
    }

    def isConfigLib() {
        if ((artifactType == ArtifactType.SIMPLE && (artifactSubType == ArtifactSubType.ARCH_CFG ||
            artifactSubType == ArtifactSubType.SRV_CFG)
        )
        ) return true
        else return false
    }

	def getTrazabilidad(boolean result, step) {
		return [
			resultado                   : getResultFlag(result),
			success                     : result,
			tipoTrazabilidad            : getDeployed(),
			entorno                     : environment,
			disabledActionPolicy        : getDisabledPolicy(),
			authorizationServiceToInform: getAuthServiceToInform(),
			acciones                    : getAccionesCustom(result, step)
		]
	}

    def getResult(boolean result, step) {
        return [
            resultado                   : getResultFlag(result),
            tipoTrazabilidad            : getDeployed(),
            entorno                     : environment,
            disabledActionPolicy        : getDisabledPolicy(),
            authorizationServiceToInform: getAuthServiceToInform(),
            acciones                    : getAccionesCustom(result, step)
        ]
    }

    def getAccionesCustom(boolean result, step) {
        def currentActions = getAcciones(result)
        if (currentActions != null) {
            currentActions.findAll{it['tipoAccion'] == "LANZAR_JOB"}.each{                
                it['serverUrl'] = step.env.JNKMSV_DEVPORTAL_URL
				it['jenkinsUserId'] = step.env.JNKMSV_USR
				it['jenkinsUserToken'] = step.env.JNKMSV_PSW
            }
        }
        return currentActions
    }

    def getResultFlag(boolean result) {
        if (result) return "CI_OK"
        else return "CI_KO"
    }

    def getLogError() {
        return [
            nombre       : "Ver Logs",
            tipoAccion   : "URL_EXTERNA",
            destino      : GlobalVars.JOB_DISPLAY_CONFLUENCE,
            canBeDisabled: false
        ]
    }

    def retry(def additionalParameters = [:]) { // Acción que se envia a AppPortal para reintentar un pipeline fallido
        Map dictionary = [
            nombre       : "Reintentar",
            tipoAccion   : "LANZAR_JOB",
            destino      : jobName,
            canBeDisabled: true,
            parametros   : []
        ]
        dictionary += additionalParameters
        jobParameters.each { key, value -> // Añadiendo los parametros del job recibidos
            dictionary.parametros.add(["nombre": key, "valor": value])
        }
        return dictionary
    }

    def storeRetryAuthorizationParameters(def pipelineParams = [:]) {

        retryAuthorizationService = pipelineParams?.retryAuthorizationService ? pipelineParams.retryAuthorizationService : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        retryEnvAuthorization = pipelineParams?.retryEnvAuthorization ? pipelineParams.retryEnvAuthorization : ""
        retryAuthorizationServiceToInform = pipelineParams?.retryAuthorizationServiceToInform ? pipelineParams.retryAuthorizationServiceToInform : AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()

    }

    abstract def getDisabledPolicy()

    abstract def getAcciones(boolean result)

    abstract def getDeployed()

    abstract def getAuthServiceToInform()

    def getAuthService() {
        if (isBpiRepo()) return AuthorizationServiceToInformType.AGILE_WORKS.toString()
        else if (isAuthExcluded()) return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
        else return AuthorizationServiceToInformType.MAXIMO.toString()
    }

    String toString() {
        return "ResultPipelineData:\n" +
            "\tclass: ${this.getClass()}\n" +
            "\tenvironment: ${environment}\n" +
            "\tgitUrl: ${gitUrl}\n" +
            "\tgitProject: ${gitProject}\n" +
            "\tdeploy: ${deploy}\n" +
            "\tcomponent: ${component}\n" +
            "\tpathRepo: ${pathRepo}\n" +
            "\trepoName: ${repoName}\n" +
            "\tpipelineOrigId: ${pipelineOrigId}\n" +
            "\tbranchName: ${branchName}\n" +
            "\tversion: ${version}\n" +
            "\tcommitId: ${commitId}\n" +
            "\tartifact: ${artifact}\n" +
            "\tartifactType: ${artifactType ? artifactType.toString() : ''}\n" +
            "\tartifactSubType: ${artifactSubType ? artifactSubType.toString() : ''}\n\n"
    }
}
