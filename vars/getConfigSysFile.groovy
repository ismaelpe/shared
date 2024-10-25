import com.project.alm.*
import java.util.Map
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK

/**
 *
 * @param app info del pom.xml
 * @param version info de la pipeline
 */
def call(String application, String version) {
    //Validar que existe el fichero
    //Primer el ficherp app.toUpperCase()_version.yml
    //Despues el fichero app.toUpperCase().yml
	String appUpperCase = application.toUpperCase()
    printOpen("Validating " + appUpperCase + " version ", EchoLevel.ALL)
    def exists = sh(
            script: "ls ./config-sys/" + appUpperCase + "_" + version + ".yml",
            returnStatus: true
    )
    printOpen("The value is ", EchoLevel.ALL)
    if (exists == 0) {
        return "./config-sys/" + appUpperCase + "_" + version + ".yml"
    } else {
        exists = sh(
                script: "ls ./config-sys/" + appUpperCase + ".yml",
                returnStatus: true
        )
        printOpen("The value is ", EchoLevel.ALL)
        if (exists == 0) return "./config-sys/" + appUpperCase + ".yml"
        else throw new Exception("No existe fichero de TTSS para generar el datasource " + appUpperCase)
    }
}
