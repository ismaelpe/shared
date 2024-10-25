import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.FileUtils
import com.caixabank.absis3.GitUtils

def call(String nameApp) {

    String repoUri = GitUtils.getConfigSysRepoUrl("dev")

    GitRepositoryHandler git = new GitRepositoryHandler(this, repoUri, [gitProjectRelativePath: 'config-sys'])

    try {

        git.lockRepoAndDo({

            git.clearOutGitProjectRelativePath().pullOrClone([depth: 1])

            try {

                String backendFileTTSS = getConfigSysFile(nameApp, "1")
                printOpen("Se ha encontrado un fichero de configuración TTSS para este micro: ${backendFileTTSS}\nNo se hará ningún cambio.", EchoLevel.ALL)

            } catch (err) {

                if (err.getMessage()?.contains("No existe fichero de TTSS para generar el datasource")) {

                    printOpen("Parece que no hay fichero de TTSS para este micro. Crearemos uno nuevo apuntando a un datasource H2", EchoLevel.ALL)
                    createDataSourceTTSSFile(nameApp, null, FileUtils.sanitizePath(git.getGitProjectRelativePath()))
                    git.add().commitAndPush("Pushing default in-memory H2 datasource for ${nameApp}")

                } else {

                    throw err

                }
            }

        })

    } catch (err2) {

        throw err2

    } finally {

        git.purge()

    }

}

private void createDataSourceTTSSFile(String nameApp, String version, String baseDirectory = './') {

    String connectionName = "${nameApp.toLowerCase()}db"
    String filename = version ? "${nameApp.toUpperCase()}_${version}.yml" : "${nameApp.toUpperCase()}.yml"

    def datasourceConfiguration = [datasource: [enable: true, connections: ["${connectionName}": [jdbcDriver: 'org.h2.Driver', tenant: 'ALL'] ]]]

    writeYaml file: "${baseDirectory}/${filename}", data: datasourceConfiguration

}
