import com.project.alm.EchoLevel
import com.project.alm.PomXmlStructure

/**
 * Script que te devuelve el pomStructure de un repo especifico de una rama especifica, clonandolo a una carpeta con el nombre del proyecto
 * @param gitRepoUrl URL del repositorio git
 * @param originBranch Nombre de rama de la que se quiere extraer el POM
 * @param projectName Nombre del proyecto
 * @return PomXmlStructure
 */
def call(String gitRepoUrl, String originBranch, String projectName) {

    PomXmlStructure pomXmlStructure
    printOpen("The git Repo is ${gitRepoUrl}", EchoLevel.ALL)

    GitRepositoryHandler git = new GitRepositoryHandler(this, gitRepoUrl, [gitProjectRelativePath: projectName])

    printOpen("Extract GIT Repo ${gitRepoUrl}", EchoLevel.ALL)
    git.pullOrClone()

    String originNumber = sh(returnStdout: true, script: "cd ${projectName} && git branch -r | grep ${originBranch} | wc -l")

    if (originNumber == '0') {

        throw new Exception("Branch ${originBranch} not found in Repo!!!!")

    } else {

        git.checkout(originBranch)

        pomXmlStructure = analizePomXml("", "", projectName)

    }

    return pomXmlStructure
}
