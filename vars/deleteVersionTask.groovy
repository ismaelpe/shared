import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.BranchStructure
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.DeployStructure
import com.caixabank.absis3.AncientVersionInfo
import com.caixabank.absis3.Utilities

def call(DeployStructure deployStructure, PomXmlStructure artifactPom, String environment) {
    AncientVersionInfo ancientVersionInfo = new AncientVersionInfo(false)

    String newReleaseArtifact = artifactPom.artifactName + "-" + artifactPom.artifactMajorVersion
    if (environment == GlobalVars.DEV_ENVIRONMENT) {
        newReleaseArtifact += "-dev"
    }

    String releaseRoute = newReleaseArtifact
    String ancientArtifactName = 'ancient_' + newReleaseArtifact

    String appBmxName = sh(returnStdout: true, script: "cf apps | grep -i '${newReleaseArtifact}' | grep -vi 'ancient_' | awk '{print \$1}'")
    String ancientAppBmxName = sh(returnStdout: true, script: "cf apps | grep -i '${newReleaseArtifact}' | grep -i 'ancient_' | awk '{print \$1}'")
    printOpen("El componente a eleminar es ${appBmxName}", EchoLevel.ALL)

    //The blue && Green only applies to micros
    if (appBmxName != null && appBmxName != '') {
        //Por ahora no desmapeamos por si hay que hacer rollback de la baja
        /*printOpen("Unmapping the routes", EchoLevel.ALL)
        sh "cf unmap-route ${newReleaseArtifact} ${deployStructure.url_int} --hostname ${releaseRoute}"
        sh "cf unmap-route ${newReleaseArtifact} ${deployStructure.url_ext} --hostname ${releaseRoute}"

        if (deployStructure.environment==GlobalVars.PRO_ENVIRONMENT) {
            String newReleaseRoute=deployStructure.suffixedComponentName.replace("<componentName>", releaseRoute)
            //unmap de la route d ebeta
            sh "cf unmap-route ${newReleaseArtifact} ${deployStructure.url_int} --hostname ${newReleaseRoute}"
            sh "cf unmap-route ${newReleaseArtifact} ${deployStructure.url_ext} --hostname ${newReleaseRoute}"
        }*/

        //Si existe un ancient anterior, lo eliminamos antes de renombrar el actual
        if (ancientAppBmxName != null && ancientAppBmxName != '') {
            sh "cf delete -r -f ${ancientAppBmxName}"
        }
        sh "cf stop ${newReleaseArtifact}"
        sh "cf rename '${newReleaseArtifact}' '${ancientArtifactName}'"
        ancientVersionInfo.isRenamed = true
    }

    return ancientVersionInfo

}