import com.project.alm.EchoLevel
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
import groovy.transform.Field
import com.project.alm.PipelineStructureType
import com.project.alm.CloudStateUtility
import com.project.alm.CloudDeployStructure
import com.project.alm.CloudWorkflowStates

import com.project.alm.CloudVarPipelineCopyType

import com.project.alm.DevBmxStructure
import com.project.alm.TstBmxStructure
import com.project.alm.PreBmxStructure
import com.project.alm.ProBmxStructure
import com.project.alm.RunRemoteITBmxStructure

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
		agent {	node (almJenkinsAgent(pipelineParams)) }
        options{ 
            buildDiscarder(logRotator(numToKeepStr: '50'))
            timestamps()
            timeout(time: 1, unit: 'HOURS')
        }
    	environment {
	    	GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
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
    //https://k8sgateway.dev.cloud-1.alm.cloud.lacaixa.es/arch-service/adsconnector-micro-server-1-dev 
    //http://adsconnector-micro-server-1-dev.tst1.int.srv.project.com
    String fileOut="responseApi.json"
    
    String route=""
    def responseStatusCode=null

    String arch=""
    if (isArchService) arch="arch-service/"

    route="k8sgateway."+environmentDest+".cloud-1.alm.cloud.lacaixa.es/${arch}"+artifact+"/" + GlobalVars.ENDPOINT_REFRESH

    responseStatusCode=sh(script: "curl --write-out '%{http_code}' -o ${fileOut} -k -X POST -x http://${env.proxyHost}:${env.proxyPort} https://${route} --connect-timeout ${GlobalVars.ACTUATOR_REFRESH_TIMEOUT}",returnStdout: true)

    if (responseStatusCode!="200") throw new Exception("Error ${responseStatusCode}")

    String contentResponse= sh(script: "cat ${fileOut}", returnStdout:true )
    printOpen("Status Code ${responseStatusCode} body ${contentResponse}", EchoLevel.ALL)

    route="k8sgateway."+environmentDest+".cloud-2.alm.cloud.lacaixa.es/${arch}"+artifact+"/" + GlobalVars.ENDPOINT_REFRESH
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

