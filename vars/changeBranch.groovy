import com.project.alm.GlobalVars
import com.project.alm.BranchType
import com.project.alm.PomXmlStructure
import com.project.alm.GlobalVars
import com.project.alm.PipelineData

def call(String branchDestination) {

    withCredentials([usernamePassword(credentialsId: 'GITLAB_CREDENTIALS', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {

        // INICIO - IPE
        // Comentario: Si se definen las variables de entorno para Git esto no hace falta
        sh "git config http.sslVerify false"
        // FIN - IPE
        sh "git checkout ${branchDestination} "

    }

}
