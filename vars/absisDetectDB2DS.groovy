import groovy.transform.Field
import com.project.alm.SampleAppCleanMode
import com.project.alm.GlobalVars
import java.util.Date
import java.util.ArrayList
import com.project.alm.*
import groovy.json.JsonSlurperClassic

@Field boolean successPipeline

def updateDb2Flag(String DS) {

    printOpen("Getting fomr catmsv ${DS}", EchoLevel.DEBUG)
	
	def response = sendRequestToAbsis3MS(
		'GET',
		"${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/SRV.DS/${DS}",
		null,
		"${GlobalVars.CATALOGO_ABSIS3_ENV}",
		[
			kpiAlmEvent: new KpiAlmEvent(
				null, null,
				KpiAlmEventStage.UNDEFINED,
				KpiAlmEventOperation.CATMSV_HTTP_CALL)
		])
	
	if (response.status == 200) {
		//def DSObject= new JsonSlurperClassic().parseText(response.content)
		def DSObject= response.content
		//def DSObject = readJSON text: response.content
		
		if (!DSObject.isDB2) {
            printOpen("Updating the DS ${DS}", EchoLevel.DEBUG)
			DSObject.isDB2=true
			def response1 = sendRequestToAbsis3MS(
				'PUT',
				"${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app",
				DSObject,
				"${GlobalVars.CATALOGO_ABSIS3_ENV}",
				[
					kpiAlmEvent: new KpiAlmEvent(
						null, null,
						KpiAlmEventStage.UNDEFINED,
						KpiAlmEventOperation.CATMSV_HTTP_CALL)
				])
			if (response1.status == 200) {
                printOpen("Cataleg actualitzat correctament", EchoLevel.INFO)
			}else {
				throw new Exception("Error al actualitzar el cataleg")
			}
		}
		
	}else {
        printOpen("La aplicacion indicada no existe DS.${DS}", EchoLevel.ERROR)
	}
}

@Field Map pipelineParams
@Field boolean successPipeline

def call(Map pipelineParameters) {
    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
	pipelineParams = pipelineParameters
	successPipeline = true
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
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
            stage("detect-DB2") {
                steps {
                    script {

						String repoUri = GitUtils.getConfigSysRepoUrl( Environment.TST.name())
						
						GitRepositoryHandler git = new GitRepositoryHandler(this, repoUri, [gitProjectRelativePath: 'config-sys'])
						
						try {
                            printOpen("Cloning the config sys Repo", EchoLevel.DEBUG)
					
							git.pullOrClone([depth: 1])
							
							def db2Backends=sh(returnStdout: true, script: "cd ./config-sys && grep -i db2 *.yml | grep -v iSeries | awk '{ print \$1 }' | uniq | awk -F ':' '{ print \$1 }' | uniq")
							def backendsList=new ArrayList()
							db2Backends.trim().tokenize('\n').each {
								def backend = it-'.yml'
								backendsList.add(backend)
							}
                            printOpen("The backendsList is ${backendsList}", EchoLevel.DEBUG)
							for ( i in backendsList ) {
                                printOpen("updating ${i}", EchoLevel.DEBUG)
								updateDb2Flag(i)
								
							}							
							
						}catch(Exception ex) {
							throw ex
						}
								
						currentBuild.displayName = "Discovering DB2 DataServices"
						
                       
                    }
                }
            }

        }
        post {
            success {
                script {
                    successPipeline = true
                    printOpen("Is pipeline successful? ${successPipeline}", EchoLevel.INFO)
                }
            }
            failure {
                script {
                    successPipeline = false
                    printOpen("Is pipeline unsuccessful? ${successPipeline}", EchoLevel.ERROR)
                }
            }
            always {

                cleanWorkspace()

            }
        }
    }
}
