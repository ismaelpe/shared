import com.project.alm.EchoLevel
import com.project.alm.GlobalVars

def call(Map pipelineParams) {

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo

    boolean successPipeline = true

    pipeline {
        agent { node (absisJenkinsAgent(pipelineParams)) }
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
			stage('delete-stress-and-wiremock-server-pods') {
				steps {
					script {
                        initGlobalVars([loggerLevel: params.loggerLevel])

                        Date today = new Date().clearTime()
						Date priorDate = today - Integer.parseInt(params.cleanStressDays)
						
						currentBuild.displayName = "Cleaning ICP stress pods before ${priorDate}" 
						
                        printOpen("Cleaning ICP with parameters ", EchoLevel.INFO)
                        printOpen("Clean ICP: ${params.cleanStressDays}", EchoLevel.INFO)
                        printOpen("Clean Stress days before: ${priorDate}", EchoLevel.INFO)

                        undeployWiremockServerFromKubernetes(params.cleanStress, Integer.valueOf(params.cleanStressDays))
					}
				}
			}
        }
        post {
            success {
                script {
                    successPipeline = true
                    printOpen("SUCCESS", EchoLevel.INFO)
                }
            }
            failure {
                script {
                    successPipeline = false
                    printOpen("FAILURE", EchoLevel.ERROR)
                }
            }
            always {

                cleanWorkspace()

            }
        }
    }
}