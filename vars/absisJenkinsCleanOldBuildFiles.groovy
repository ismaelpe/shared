import groovy.transform.Field
import com.project.alm.EchoLevel

@Field Map pipelineParams

/**
 * Pipeline SCM Script, que sirve para limpiar ficheros de builds anteriores a una semana, con el objetivo de liberar espacio en el filesystem de la maquina Jenkins
 * A través del agent "master", tiene montado en el path /jenkins su filesystem
 * @param pipelineParams
 * @return void
 */
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters

    pipeline {
        agent { label 'master' }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
        }
        stages {
            stage('remove') {
                steps {
                   removeStep()
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
 * Stage 'removeStep'
 */
def removeStep() {
    sh '''#!/bin/bash
         for i in $(find /var/jenkins_home/jobs/absis3/jobs -name "builds"); do find $i -type f -mtime +7 -not -path "*/libs/*" -exec rm -rfv -- '{}' \\; ; done
    '''
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen("Ejecución de pipeline finalizada satisfactoriamente", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen("Falló la ejecución del pipeline", EchoLevel.ERROR)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}