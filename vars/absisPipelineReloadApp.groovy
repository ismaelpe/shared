import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.BranchStructure
import com.caixabank.absis3.ArtifactType
import com.caixabank.absis3.ArtifactSubType
import com.caixabank.absis3.BranchType
import com.caixabank.absis3.GarAppType
import com.caixabank.absis3.GlobalVars
import groovy.transform.Field
import com.caixabank.absis3.PipelineStructureType
import com.caixabank.absis3.ICPStateUtility
import com.caixabank.absis3.ICPDeployStructure
import com.caixabank.absis3.ICPWorkflowStates

import com.caixabank.absis3.ICPVarPipelineCopyType

import com.caixabank.absis3.DevBmxStructure
import com.caixabank.absis3.TstBmxStructure
import com.caixabank.absis3.PreBmxStructure
import com.caixabank.absis3.ProBmxStructure
import com.caixabank.absis3.RunRemoteITBmxStructure

@Field Map pipelineParams

@Field String artifact
@Field String environmentDest
@Field String isArchService

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    artifact = params.artifactParam
	environmentDest = params.environmentDestParam
	isArchService = params.isArchServiceParam.toString().toBoolean()
    
    /*
    * Pasos a seguir:
    * 0- Crear Folder
    * 1- Crear Repo
    * */
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options{ 
            buildDiscarder(logRotator(numToKeepStr: '50'))
            timestamps()
            timeout(time: 1, unit: 'HOURS')
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
			stage('reload'){
				steps{
					reloadStep()
				}
			}
		}
	    post{
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
 * Stage 'reloadStep'
 */
def reloadStep() {
    //https://k8sgateway.dev.icp-1.absis.cloud.lacaixa.es/arch-service/adsconnector-micro-server-1-dev 
    //http://adsconnector-micro-server-1-dev.tst1.int.srv.caixabank.com
    String fileOut="responseApi.json"
    
    String route=""
    def responseStatusCode=null

    String arch=""
    if (isArchService) arch="arch-service/"

    route="k8sgateway."+environmentDest+".icp-1.absis.cloud.lacaixa.es/${arch}"+artifact+"/" + GlobalVars.ENDPOINT_REFRESH

    responseStatusCode=sh(script: "curl --write-out '%{http_code}' -o ${fileOut} -k -X POST -x http://${env.proxyHost}:${env.proxyPort} https://${route} --connect-timeout ${GlobalVars.ACTUATOR_REFRESH_TIMEOUT}",returnStdout: true)

    if (responseStatusCode!="200") throw new Exception("Error ${responseStatusCode}")

    String contentResponse= sh(script: "cat ${fileOut}", returnStdout:true )
    printOpen("Status Code ${responseStatusCode} body ${contentResponse}", EchoLevel.ALL)

    route="k8sgateway."+environmentDest+".icp-2.absis.cloud.lacaixa.es/${arch}"+artifact+"/" + GlobalVars.ENDPOINT_REFRESH
    responseStatusCode=sh(script: "curl --write-out '%{http_code}' -o ${fileOut} -k -X POST -x http://${env.proxyHost}:${env.proxyPort} https://${route} --connect-timeout ${GlobalVars.ACTUATOR_REFRESH_TIMEOUT}",returnStdout: true)
    if (responseStatusCode!="200") throw new Exception("Error ${responseStatusCode}")
    contentResponse= sh(script: "cat ${fileOut}", returnStdout:true )
    printOpen("Status Code ${responseStatusCode} body ${contentResponse}", EchoLevel.ALL)

    
    currentBuild.displayName = "Build_${env.BUILD_ID}_Reload_" +artifact +"_${environmentDest}"
    
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
   cleanWorkspace()
}

