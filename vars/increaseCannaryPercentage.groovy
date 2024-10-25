import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.BranchType
import com.project.alm.PomXmlStructure
import com.project.alm.GlobalVars
import com.project.alm.PipelineData

def call(String destinationFolder, int newPercentage, boolean increasePercentage, String appName) {
    int actualIntPercentage = newPercentage
    //Tenemos que buscar el fichero
    printOpen("Increase the ${appName} in the ${destinationFolder} whith the ${newPercentage} by the mode ${increasePercentage}", EchoLevel.INFO)

    String existeArchAppYaml = sh(returnStdout: true, script: "ls ${destinationFolder}/${appName}.yml | wc -l")

    existeArchAppYaml = (existeArchAppYaml.trim()).replaceAll('/n', '')

    printOpen("Existe el value ${existeArchAppYaml}", EchoLevel.DEBUG)
    if (existeArchAppYaml == '0') {
        //No existe el fichero lo voy a generar
        printOpen("Setting ${actualIntPercentage} to the app ${appName} in the file ${destinationFolder}/${appName}.yml", EchoLevel.DEBUG)
        sh "echo '${GlobalVars.CANNARY_PERCENTAGE_ABSIS}: ${actualIntPercentage}' >  ${destinationFolder}/${appName}.yml"
    } else {

        String appYaml = sh(returnStdout: true, script: "cat ${destinationFolder}/${appName}.yml")
        String newYaml = ''
        String actualPercentage = ''

        appYaml.tokenize('\n').each { x ->
            if (x.contains(GlobalVars.CANNARY_PERCENTAGE_ABSIS)) {

                actualPercentage = x - "${GlobalVars.CANNARY_PERCENTAGE_ABSIS}: "
                actualIntPercentage = actualPercentage.trim().toInteger()
                if (actualIntPercentage >= 100 && increasePercentage) return -1
                if (increasePercentage) {
                    actualIntPercentage = (actualIntPercentage + newPercentage > 100) ? 100 : actualIntPercentage + newPercentage
                } else {
                    actualIntPercentage = newPercentage
                }

                printOpen("The actual Percentatge is is ${x} the new is ${actualIntPercentage}", EchoLevel.INFO)
            } else {
                if (x != '') newYaml = newYaml + x + '\n'
            }
        }

        sh "echo ${newYaml} > ${destinationFolder}/${appName}.yml"
        sh "echo '${GlobalVars.CANNARY_PERCENTAGE_ABSIS}: ${actualIntPercentage}' >>  ${destinationFolder}/${appName}.yml"


    }

    return actualIntPercentage

}
