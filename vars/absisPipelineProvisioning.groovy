import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GarAppType
import com.project.alm.GitUtils
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiLifeCycleStage
import com.project.alm.KpiLifeCycleStatus
import com.project.alm.Utilities
import com.project.alm.GlobalVars

@Field Map pipelineParams

@Field String checkout_dir
@Field String tempArtifact
@Field String empresa
@Field String modulo
@Field String nameApp
@Field String nameAppFromGAR
@Field String typeApp
@Field String pathToRepo
@Field String domain
@Field String gitBranch
@Field String isArq
@Field String appParams
@Field String existsPom
@Field String buildPath

@Field GitUtils gitUtils

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters
	
	/**
	 * Directorio temporal de descarga de artefacto
	 */
	checkout_dir = "checkout"
	
	/**
	 * Nombre del ZIP del initializer
	 */
	tempArtifact = "initializer_artifact"

    /**
     * El script en caso que no exista el dominio lo genera. Con esto te ahorras un mantenimiento de los folders de jenkins
     */

    /**
     * Se añade la empresa
     */
    empresa = params.empresaParam
    
    /**
     * Se añade el modulo
     */
    modulo = params.moduloParam


    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
    nameApp = params.nameAppParam
	nameAppFromGAR = params.nameAppParam
    typeApp = params.typeAppParam
    pathToRepo = params.pathToRepoParam
    domain = params.domainParam

    // Obtiene la version actualmente seteada en la configuración global del jenkins
    gitBranch = getSharedDefaultVersion()

    isArq = true
	appParams = null
	existsPom = false

    gitUtils = new GitUtils(this, false)

    /*
    * Pasos a seguir:
    * 0- Crear Folder
    * 1- Crear Repo
    * */
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '50'))
            timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            ICP_CERT = credentials('icp-alm-pro-cert')
            ICP_PASS = credentials('icp-alm-pro-cert-passwd')
			JNKMSV = credentials('JNKMSV-USER-TOKEN')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
        }
        //Atencion que en el caso que estemos en un MergeRequest... quizas solo debamos validar la issue
        stages {
            stage('download-repo') {
                steps {
                    downloadRepoStep()
                }
            }
            stage('create-default-branch') {
                steps {
                    createDefaultBranchStep()
                }
            }
			stage('call-absis-initializr') {
				when {
					expression { !existsPom }
				}
				steps {
                    callAbsisInitializrStep()
				}
			}
            stage('push-initial-configuration') {
                when {
                    expression { GarAppType.DATA_SERVICE.getGarName() == typeApp }
                }
                steps {
                    pushInitialConfigurationStep()
                }
            }
            stage('create-Job') {
                steps {
                    createJobStep()
                }
            }
            stage('reconfiguring-git-project') {
                steps {
                    reconfiguringGitProjectStep()
                }
            }
			stage('add-to-provisioning') {
				when {
					expression { !existsPom }
				}
				steps {
                    addToProvisioning()
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
 * Step downloadRepoStep
 */
def downloadRepoStep() {
    kpiLogger(KpiLifeCycleStage.PROVISIONING_STARTED, KpiLifeCycleStatus.OK, typeApp, nameApp)
                
    if (typeApp == "SRV.LIB" || typeApp == "SRV.DS" || typeApp == "SRV.MS" || typeApp == "SRV.BFF") {        
        if (domain.contains('/')) {
            def tokens = domain.tokenize('/')
            def contador = 0
            def domainAux

            tokens.each {
                if (contador == 0) {
                    domainAux = it
                    contador = 1
                }
            }
            domain = domainAux
        }
    }

    currentBuild.displayName = "Creating_${nameApp}"

    printOpen("empresa ${empresa}", EchoLevel.INFO)
    printOpen("modulo ${modulo}",EchoLevel.INFO)
    printOpen("nameApp ${nameApp}",EchoLevel.INFO)
    printOpen("typeApp ${typeApp}",EchoLevel.INFO)
    printOpen("pathToRepo ${pathToRepo}",EchoLevel.INFO)
    printOpen("domain ${domain}",EchoLevel.INFO)
    printOpen("gitBranch ${gitBranch}",EchoLevel.INFO)
}

/** 
 * Step createDefaultBranchStep
 */
def createDefaultBranchStep() {
    withCredentials([usernamePassword(credentialsId: 'GITLAB_CREDENTIALS', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {

        def gitProject = gitUtils.getSecuredGitRepoUrl(pathToRepo)

        sh "git -c http.sslVerify=false  clone --verbose ${gitProject} ${checkout_dir}"
		printOpen("git -c http.sslVerify=false  clone --verbose ${gitProject} ${checkout_dir}",EchoLevel.INFO)
		
		def existeRama=sh(returnStdout: true,script: "git ls-remote --heads ${gitProject} origin refs/heads/master")
		
		printOpen("Resultado de validacion de master ${existeRama}",EchoLevel.INFO)
		
		if (existeRama!=null) {
			if (existeRama.indexOf('master')==-1) {
				printOpen("No tiene master el repo",EchoLevel.INFO)
				sh "cd ${checkout_dir} && git checkout -b master"
			}
		}
        
        existsPom = fileExists "./${checkout_dir}/pom.xml"
    
        printOpen("Tenemos un pom en la raiz ${existsPom} vamos a intentar evitar llamar al initializr y eliminar el codigo", EchoLevel.INFO)
        String dirContent = sh(script: "ls -la *", returnStdout: true)
        printOpen("Directory content:\n${dirContent}", EchoLevel.DEBUG)

    }
}

/** 
 * Step callAbsisInitializrStep
 */
def callAbsisInitializrStep() {
    printOpen("Clean ${checkout_dir} dir", EchoLevel.ALL)
    sh "rm -rf  ./${checkout_dir}/*"
    
    appParams = sendRequestToInitializr(empresa,nameApp,typeApp,domain,tempArtifact)
    
    printOpen("Unzip ./${tempArtifact}.zip downloaded artifact in ./${tempArtifact}/", EchoLevel.ALL)
    sh "unzip -o ${tempArtifact}.zip -d ./${tempArtifact}"
    
    printOpen("Move ${tempArtifact}/${nameApp}/* to ${checkout_dir}", EchoLevel.ALL)
    sh "mv ${tempArtifact}/${nameApp}/*  ${checkout_dir}"
    
    printOpen("Descomprimido nuevo codigo generado usando AbsisInitializr", EchoLevel.ALL)
    // Descomprimimos en la ruta del proyecto, sobrescribiendo. Como dentro del zip ya tendrá
    sh "cd ${checkout_dir} && git config user.name ${GlobalVars.GIT_USER_NAME} && git config user.email ${GlobalVars.GIT_USER_EMAIL}"
    sh "cd ${checkout_dir} && git add ."
    sh "cd ${checkout_dir} && git commit -m 'First code generation with AbsisInitlizr to start testing with deploy' --allow-empty"
    sh "cd ${checkout_dir} && git -c http.sslVerify=false push origin master"
}

/** 
 * Step pushInitialConfigurationStep
 */
def pushInitialConfigurationStep() {
    absisPipelineStagePushInitialConfiguration(nameApp)
}

/** 
 * Step createJobStep
 */
def createJobStep() {
    try {
        def additionalParameters = getAdditionalParameters([ 
            nameApp: nameApp, 
            pathToRepo: pathToRepo, 
            domain: domain, 
            typeApp: typeApp, 
            modulo: modulo, 
            empresa: empresa.toLowerCase(),
            jenkinsPath: "alm/services",
            gitCredentials: "GITLAB_CREDENTIALS",
            createJobOndemand: "$env.CREATE_ONDEMAND_JOB_IN_PROVISIONING".toBoolean()
        ])

        printOpen("Params to call jobProvisioning: $additionalParameters", EchoLevel.INFO)
        
        jobDsl  scriptText: libraryResource('jobProvisioning.groovy'),
                useScriptText: true,
                ignoreExisting: true,
                additionalParameters: additionalParameters

        if (additionalParameters.createJobOndemand) {           
            if (additionalParameters.jenkinsPathModule) {
                buildPath = "$additionalParameters.jenkinsPathModule/$additionalParameters.nameApp-ondemand"
            } else {
                buildPath = "$additionalParameters.jenkinsPath/$additionalParameters.nameApp-ondemand"
            } 
        } else {
            buildPath = ""
        }

    }catch(Exception e) {
        printOpen("El error es de ${e}",EchoLevel.ERROR)
    }
}

/**
 * Calculate additional parameters
 */
def getAdditionalParameters(Map additionalParameters) {
    additionalParameters.isArq = true
    additionalParameters.jenkinsPathModule = ''

    if (additionalParameters.empresa.equals("corp") 
        && additionalParameters.empresa.equals("cbk") 
        && additionalParameters.empresa.equals("bpi")) {
        
        error "Company value not supported!!!"
    }

    /**
    * Init del nombre del proyecto jenkins acorde con los valores establecidos
    **/
    if (additionalParameters.typeApp == "Library" 
        || additionalParameters.typeApp == "SRV.LIB" 
        || additionalParameters.typeApp == "ARQ.LIB") {
        
        if (!additionalParameters.nameApp.contains('-lib')) {
            additionalParameters.nameApp = additionalParameters.nameApp + '-lib'
        }
    } else if (additionalParameters.typeApp == "MicroService" 
        || additionalParameters.typeApp == "SRV.MS"
        || additionalParameters.typeApp == "DataService" 
        || additionalParameters.typeApp == "SRV.DS" 
        || additionalParameters.typeApp == "ARQ.MIA" 
        || additionalParameters.typeApp == "SRV.BFF") {
        
        if (!additionalParameters.nameApp.contains('-micro')) {
            additionalParameters.nameApp = additionalParameters.nameApp + '-micro'
        }
    } else if (additionalParameters.typeApp == 'ARQ.MAP') {
        if (!additionalParameters.nameApp.contains('-plugin')) {
            additionalParameters.nameApp = additionalParameters.nameApp + '-plugin'
        }
    } else if (additionalParameters.typeApp == 'ARQ.CFG' || additionalParameters.typeApp == "SRV.CFG") {
        if (!additionalParameters.nameApp.contains('-conf')) {
            additionalParameters.nameApp = additionalParameters.nameApp + '-conf'
        }
    }

    // Comprobamos las tres tipologias disponibles
    if (additionalParameters.typeApp == "Library" 
        || additionalParameters.typeApp == "MicroService" 
        || additionalParameters.typeApp == "DataService"
        || additionalParameters.typeApp == "SRV.LIB"
        || additionalParameters.typeApp == "SRV.DS" 
        || additionalParameters.typeApp == "SRV.MS" 
        || additionalParameters.typeApp == "SRV.CFG" 
        || additionalParameters.typeApp == "SRV.BFF") {
        
        additionalParameters.isArq = false
        
        additionalParameters.jenkinsPath = additionalParameters.jenkinsPath + "/apps" + "/" + additionalParameters.empresa
       
        if (additionalParameters.typeApp == "Library" || additionalParameters.typeApp == "SRV.LIB") {
            additionalParameters.jenkinsPath = additionalParameters.jenkinsPath + "/common/" + additionalParameters.domain
        }
        if (additionalParameters.typeApp == "SRV.CFG") {
            additionalParameters.jenkinsPath = additionalParameters.jenkinsPath + "/conf/" + additionalParameters.domain
        }
        if (additionalParameters.typeApp == "MicroService" || additionalParameters.typeApp == "SRV.MS") {
            additionalParameters.jenkinsPath = additionalParameters.jenkinsPath + "/service/" + additionalParameters.domain
        }
        if (additionalParameters.typeApp  == "DataService" || additionalParameters.typeApp  == "SRV.DS") {
            additionalParameters.jenkinsPath = additionalParameters.jenkinsPath + "/data-service/" + additionalParameters.domain
        }
        if (additionalParameters.typeApp  == "SRV.BFF") {
            additionalParameters.jenkinsPath = additionalParameters.jenkinsPath + "/bff/" + additionalParameters.domain
        }       
        if (additionalParameters.empresaParam.equals("bpi")) {
            additionalParameters.jenkinsPathModule = additionalParameters.jenkinsPath + "/" + additionalParameters.modulo
        }
    } else {
        //Domain puede llegar con '/'
        if (additionalParameters.domain.contains('/')) {
            //El domain contiene una como minimo deberia parsear la informacion y sacar del domain y el folder.
            def tokens = additionalParameters.domain.tokenize('/')
            def contador = 0
            def domainAux

            tokens.each {
                if (contador == 0) {
                    domainAux = it
                    contador = 1

                } else if (contador == 1) {
                    modulo = it
                    contador = 2
                }
            }

            additionalParameters.domain = domainAux
        } 
        // IPE: No tiene sentido que seteemos a vacio si no contiene /
        //else {
        //    additionalParameters.domain = ''
        //}

        if (additionalParameters.empresa.equals("bpi")) {
            additionalParameters.jenkinsPath = additionalParameters.jenkinsPath + "/arch/bpi/" + additionalParameters.domain
        } else {
            additionalParameters.jenkinsPath = additionalParameters.jenkinsPath + "/arch/" + additionalParameters.domain
        }

        if (modulo != null && modulo != '') {
            additionalParameters.jenkinsPathModule = additionalParameters.jenkinsPath + "/" + modulo            
        }
    }

    return additionalParameters
}

/** 
 * Step reconfiguringGitProjectStep
 */
def reconfiguringGitProjectStep() {
    rebuildGitProject(pathToRepo - GlobalVars.gitlabDomain - '.git')
}

/** 
 * Step addToProvisioning
 */
def addToProvisioning() {
    def configuration = pathToRepo-".git"+"/tree/master/src/main/resources"
    
    def app = [
        application     : nameAppFromGAR,
        configuration   : configuration,
        groupId         : appParams.get("groupId"),
        name            : appParams.get("artifactId"),
        sourceCode      : pathToRepo,
        buildPath       : buildPath,
        srvDeployTypeId : "${GlobalVars.DEFAULT_DEPLOYMENT_TYPE}",
        srvTypeAppId    : typeApp
    ]

    printOpen("Create app in calatogue is: $app", EchoLevel.INFO)

    def response = sendRequestToAbsis3MS(
        'PUT',
        "${GlobalVars.URL_CATALOGO_ALM_PRO}/app",
        app,
        "${GlobalVars.CATALOGO_ALM_ENV}",
        [
            kpiAlmEvent: new KpiAlmEvent(
                null, null,
                KpiAlmEventStage.UNDEFINED,
                KpiAlmEventOperation.CATMSV_HTTP_CALL)
        ])
}

/** 
 * Step endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}

/** 
 * Step endPipelineSuccessStep
 */
def endPipelineSuccessStep() {
    kpiLogger(KpiLifeCycleStage.PROVISIONING_FINISHED, KpiLifeCycleStatus.OK, typeApp, nameApp)
    printOpen("Provisioning realizado con exito", EchoLevel.ALL)
    sendOnboardingEmail(typeApp, nameApp, pathToRepo)  
}

/** 
 * Step endPipelineFailureStep
 */
def endPipelineFailureStep() {
    kpiLogger(KpiLifeCycleStage.PROVISIONING_FINISHED, KpiLifeCycleStatus.KO, typeApp, nameApp)
    def mapEmailFields = Utilities.getEmailTemplate(this, [
        "type": "provisioningFailure",
        "gitUrl": pathToRepo,
        "appType": typeApp
    ])
    mail to: GlobalVars.EMAIL_REPORT,
        subject: mapEmailFields.subject,
        body: mapEmailFields.body
}


/**
 * Obtiene el job de provisioning de la rama configurada en la configuracion global del jenkins
 */
def getSharedDefaultVersion() {
    def libraryConfiguration = jenkins.model.Jenkins.getInstance().getDescriptor("org.jenkinsci.plugins.workflow.libs.GlobalLibraries").getLibraries().find { it.getName() == 'alm-services'}
    return libraryConfiguration.getDefaultVersion()
}
