import com.project.alm.ArtifactType
import com.project.alm.PipelineData
import com.project.alm.PomVersionsValidator
import com.project.alm.PomXmlStructure
import com.project.alm.GarAppType
import com.project.alm.*

/**
 *
 * Checks if artifact is dataservice, in this case if it has some other 
 * dataservice in dependency list , then it has to be in a white list,
 * in other case throw an exception
 *
 * @param pomXmlStructure the pomXml details
 * @param pipeline the pipeline details
 *
 * throws an error if dependencies are not correct
 */
def call(PomXmlStructure pomXmlStructure, PipelineData pipeline, Object whiteListDataServicesAllowedClients) {

    try {

        if (pipeline.garArtifactType == GarAppType.DATA_SERVICE) {

            printOpen("Validating dependencies for dataservices...", EchoLevel.INFO)

            String dependenciesPath = BackEndAppPortalUtilities.getDependeciesJsonFilePath(pomXmlStructure)
            printOpen("Reading dependencies from ${dependenciesPath}", EchoLevel.DEBUG)
            def exists = fileExists dependenciesPath

            List<String> artifactDataServicesDependecies = new ArrayList<String>()
            if (exists) {
                def dependenciesJson = readFile(file: dependenciesPath)
                artifactDataServicesDependecies = BackEndAppPortalUtilities.getDependeciesNamesByType(dependenciesJson, 'SRV.DS')
            } else {
                printOpen("Dependency file doesn't exist.", EchoLevel.ERROR)
            }


            if (artifactDataServicesDependecies.size() > 0) {


                List whiteList = Arrays.asList(whiteListDataServicesAllowedClients)
                StringBuilder logmessage = new StringBuilder()
                logmessage.append("allowed list of data services to be a dependency from other dataservice is:\n")
                for (Object item : whiteList) {
                    logmessage.append("- ${item.toString()}\n")
                }
                logmessage.append("current list of data services dependecies for this micro is:\n")
                for (Object item : artifactDataServicesDependecies) {
                    logmessage.append("- ${item.toString()}\n")
                }

                printOpen("${logmessage.toString()}", EchoLevel.DEBUG)

                for (String item : artifactDataServicesDependecies) {
                    if (!whiteList.contains(item)) {
                        String errorMessage = "Error: this application is a SRV.DS type and is using ${item} as dependency that is not in the allowed-list"
                        printOpen(errorMessage, EchoLevel.ERROR)
                        throw new RuntimeException(errorMessage)
                    } else {
                        printOpen("The dependency ${item} is in the allowed-list", EchoLevel.INFO)
                    }
                }

                printOpen("Dependencies are OK.", EchoLevel.INFO)

            } else {
                printOpen("This micro has no other dataservices as dependency", EchoLevel.INFO)
            }


        } else {
            printOpen("This is not a Data Service pipeline", EchoLevel.DEBUG)
        }

    } catch (err) {

        printOpen("ERROR: ${err.getMessage()}", EchoLevel.ERROR)

        def sw = new StringWriter()
        def pw = new PrintWriter(sw)
        err.printStackTrace(pw)
        printOpen("sw.toString()", EchoLevel.ERROR)
        throw err
    }
}

