import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GitUtils
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.ArtifactType
import com.caixabank.absis3.ArtifactSubType

def call(String gitRepoUrl, String originBranch, String repoParam, boolean prepareNewBranch, ArtifactType artifactType, ArtifactSubType artifactSubType, String tag, boolean fromTag, boolean clone = true, boolean isBBDD = false) {
    PomXmlStructure pomXmlStructure
    printOpen("The git Repo is ${gitRepoUrl}", EchoLevel.ALL)
	printOpen(" We have to clone ${clone} isBBDD ${isBBDD} fromTag ${fromTag} originBranc ${originBranch}", EchoLevel.ALL)
    withCredentials([usernamePassword(credentialsId: 'GITLAB_CREDENTIALS', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
        if (clone) { // Defaults enable. If needed, you can disable, passing false clone param
            String gitRepoSecuredUrl = new GitUtils(this, false).getSecuredGitRepoUrl(gitRepoUrl)
            sh "git -c http.sslVerify=false clone --verbose ${gitRepoSecuredUrl} . "
            sh "git -c http.sslVerify=false fetch --all --tags --prune"
        }


        String originNumber = '0'
        if (fromTag) {
            def tags = sh(returnStdout: true, script: "git tag -l v_${tag} ")
            printOpen(" The tags es ${tags}", EchoLevel.ALL)
            originNumber = sh(returnStdout: true, script: "git tag -l v_${tag} | wc -l")
        } else {
			printOpen("Before Getting the originBranch ${originBranch}", EchoLevel.ALL)
            originNumber = sh(returnStdout: true, script: "git branch -r | grep ${originBranch} | wc -l")
        }
		
		printOpen("The originNumber ${originNumber}", EchoLevel.ALL)

        if (originNumber == '0') {
            throw Exception('Error No Origin in Repo!!!!')
        } else {
            if (fromTag) {
                sh "git checkout  tags/v_${tag} "
            } else sh "git checkout  ${originBranch} "

            pomXmlStructure = analizePomXml(artifactType.toString(), artifactSubType.toString())
            printOpen("El artefacto es de ${pomXmlStructure.artifactName} la version es de ${pomXmlStructure.artifactVersion}", EchoLevel.ALL)

            if (prepareNewBranch && !isBBDD) {
				if (isBBDD) {
					sh "git checkout  -b release/BBDDv${pomXmlStructure.getArtifactVersionWithoutQualifier()} "
				}else {
					sh "git checkout  -b release/v${pomXmlStructure.getArtifactVersionWithoutQualifier()} "
				}                
            }
        }
    }
    return pomXmlStructure
}

/**
 * Clona repositorio y cambia a rama origen especificada,
 * go to specific commit revision in existing branch and gitrepourl
 * Crea rama nueva si se especifica el flag correspondiente
 * @param gitRepoUrl
 * @param originBranch
 * @param gitcommit
 * @param createBranch boolean Flag que especifica si se debe crear la branch nueva pasada en el parametro gitcommit
 * @return
 */
def call(String gitRepoUrl, String originBranch, String gitcommit, boolean createBranch) {

    printOpen("The git Repo is ${gitRepoUrl}", EchoLevel.ALL)

    GitRepositoryHandler git = new GitRepositoryHandler(this, gitRepoUrl, [gitProjectRelativePath: '.'])
    git.pullOrClone()

    def ls1 = sh(returnStdout: true, script: "ls ${workspace}/*  ")

    String originNumber = sh(returnStdout: true, script: "git branch -r | grep ${originBranch} | wc -l")

    if (originNumber == '0') {

        throw new Exception('Error No Origin in Repo!!!!')

    } else {

        sh "git checkout  ${originBranch} "
        if (createBranch) {

            git.checkout(originBranch, [newBranch: true])

        } else {

            git.checkout(gitcommit, [force: true])

        }
    }

}
