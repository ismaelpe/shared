import groovy.transform.Field
import com.project.alm.*

@Field Map pipelineParams

@Field String typeApp
@Field String nameApp
@Field String environment
@Field String instance

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    typeApp = params.typeApp
    nameApp = params.nameApp
    environment = params.environment
    instance = params.instance


    pipeline {
        agent {	node (absisJenkinsAgent(pipelineParams)) }

        stages {
            stage('Get DB Info') {
                steps {
                    getDBInfo()
                }
            }
            stage('Generate Git Files') {
                steps {
                    generateGitFiles()
                }
            }
            stage('Generate Secrets') {
                steps {
                    generateSecrets()
                }
            }
        }
    }
}

def getDBInfo(){
    echo "Recovering DB Info..."
    echo "typeApp: ${typeApp}"
    echo "nameApp: ${nameApp}"
    echo "environment: ${environment}"
    echo "instance: ${instance}"
}

def generateGitFiles(){
    echo "Generating Git Files..."
}

def generateSecrets(){
    echo "Generating Secrets..."
}
