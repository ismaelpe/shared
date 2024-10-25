import groovy.transform.Field
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.Utilities


@Field Map pipelineParams

@Field PomXmlStructure pomXmlStructure
@Field def gitProjectPath
@Field def gitProjectName
@Field def errorsList

/**
 * Pipeline SCM Script, que sirve para probar sin las pipelines del Jenkins se ejecutan correctamente.
 * Además:
 * prueba la conexión a gitlab, descargandose un proyecto (como default, el core)
 * prueba la conexion a Bluemix, a ambos centros.
 * @param pipelineParams
 * @return void
 */
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters

    gitProjectPath = env.gitProjectPath ? env.gitProjectPath : "cbk/absis3/services/arch/core/absis3core-lib"
    gitProjectName = Utilities.getNameOfProjectFromProjectPath(gitProjectPath)
    errorsList = []
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
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
        stages {
            stage('test-get-git-repo') {
                steps {
                    testGetGitRepoStep()
                }
            }
            stage('print-env-vars') {
                steps {
                    printEnvVarsStep()
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
 * Stage testGetGitRepoStep
 */
def testGetGitRepoStep() {
    initGlobalVars(pipelineParams)
    def gitRepoUrl = null
    printOpen("Descargamos projecto git ${gitProjectName}, para probar conectividad con gitlab", EchoLevel.INFO)
    try {
        gitRepoUrl = GlobalVars.gitlabDomain + gitProjectPath + ".git"
        pomXmlStructure = getPomFromGitRepo(gitRepoUrl, GlobalVars.MASTER_BRANCH, gitProjectName)
        printOpen("Descargado repositorio git correctamente", EchoLevel.INFO)
    } catch (Exception e) {
        printOpen("Falló la descarga del clonado de proyecto '${gitRepoUrl}' con error ${e.getMessage()} Este existe? ", EchoLevel.ERROR)
        errorsList.add(e)
    }
}

/**
 * Stage printEnvVarsStep
 */
def printEnvVarsStep() {
    printOpen("Mostramos la lista de variables de entorno recibidas:", EchoLevel.INFO)
    env.getEnvironment().each { key, value ->
        printOpen("${key} = ${value}", EchoLevel.INFO)
    }
}

/**
 * Stage endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    attachPipelineLogsToBuild(pomXmlStructure)
    cleanWorkspace()
}

/**
 * Stage endPipelineSuccessStep
 */
def endPipelineSuccessStep() {
    if (errorsList) {
        printOpen("Ejecución satisfactoria del pipeline, pero con errores:\n${errorsList.join("\n")}", EchoLevel.ERROR)
    } else {
        printOpen("Ejecución satisfactoria del pipeline.", EchoLevel.INFO)
    }
}

/**
 * Stage endPipelineFailureStep
 */
def endPipelineFailureStep() {
    printOpen("Falló la ejecución del pipeline", EchoLevel.ERROR)
}

     