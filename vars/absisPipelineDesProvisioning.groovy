import groovy.transform.Field
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GarAppType
import com.caixabank.absis3.GitUtils
import com.caixabank.absis3.KpiAlmEvent
import com.caixabank.absis3.KpiAlmEventOperation
import com.caixabank.absis3.KpiAlmEventStage
import com.caixabank.absis3.KpiLifeCycleStage
import com.caixabank.absis3.KpiLifeCycleStatus
import com.caixabank.absis3.Utilities
import com.caixabank.absis3.GlobalVars
import jenkins.model.*

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
@Field boolean isArq
@Field def appParams
@Field boolean existsPom

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
    checkout_dir = 'checkout'

    /**
     * Nombre del ZIP del initializer
     */
    tempArtifact = 'initializer_artifact'

    /**
     * El script en caso que no exista el dominio lo genera. Con esto te ahorras un mantenimiento de los folders de jenkins
     */

    /**
     * Se añade la empresa y el modulo
     */
    empresa = params.empresaParam
    modulo = params.moduloParam

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
    nameApp = params.nameAppParam
    nameAppFromGAR = params.nameAppParam
    typeApp = params.typeAppParam
    pathToRepo = params.pathToRepoParam
    domain = params.domainParam

    //Indica en que carpeta esta el job de provisioning.
    //Usado en el clone del branch de la jenkinslib
    //'stage' hace clone de la rama de stage, vacio o otro valor hace clone de la rama master.
    try {
        gitBranch = params.almFolder
    } catch (e) {
        gitBranch = ''
    }

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
        agent { node(absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '50'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            JNKMSV = credentials('JNKMSV-USER-TOKEN')
            ICP_CERT = credentials('icp-absis3-pro-cert')
            ICP_PASS = credentials('icp-absis3-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
        }
        //Atencion que en el caso que estemos en un MergeRequest... quizas solo debamos validar la issue
        stages {
            stage('remove-Job') {
                steps {
                   removeJobStep()
                }
            }
            stage('remove-from-catalog') {
                steps {
                   removeFromCatalog()
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
                endiPipelineAlwaysStep()
            }
        }
    }
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/**
 * Stage 'removeJobStep'
 */
def removeJobStep() {
    currentBuild.displayName = "Deleting_${nameApp}"
    deleteJobAndFolder(typeApp, domain, nameApp, empresa, modulo)
}

/**
 * Stage 'removeFromCatalog'
 */
def removeFromCatalog() {
    deleteJobAndFolder(typeApp, nameApp)
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    kpiLogger(KpiLifeCycleStage.DEPROVISIONING_FINISHED, KpiLifeCycleStatus.OK, typeApp, nameApp)
    printOpen('Desprovisión realizada con exito', EchoLevel.INFO)
    //sendOnboardingEmail(typeApp, nameApp, pathToRepo)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    kpiLogger(KpiLifeCycleStage.DEPROVISIONING_FINISHED, KpiLifeCycleStatus.KO, typeApp, nameApp)
    /*
                    def mapEmailFields = Utilities.getEmailTemplate(this, [
                        "type": "deProvisioningFailure",
                        "gitUrl": pathToRepo
                    ])
                    mail to: GlobalVars.EMAIL_REPORT,
                        subject: mapEmailFields.subject,
                        body: mapEmailFields.body*/
}

/**
 * Stage 'endiPipelineAlwaysStep'
 */
def endiPipelineAlwaysStep() {
    cleanWorkspace()
}
