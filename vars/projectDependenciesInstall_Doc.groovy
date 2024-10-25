/**
 *  Instala las dependencias del proyecto desde el repositorio nexus
 */

import com.caixabank.absis3.GlobalVars

def call() {
    /* Es necesario registrar el repositorio de nexus para bajar las dependencias privadas */
  //  sh "npm config set registry ${GlobalVars.nexusDownloadRepository}"
    sh 'npm install'
}
