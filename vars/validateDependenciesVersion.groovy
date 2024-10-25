import com.project.alm.ArtifactType
import com.project.alm.EchoLevel
import com.project.alm.PipelineData
import com.project.alm.PomVersionsValidator
import com.project.alm.PomXmlStructure
import com.project.alm.GlobalVars
import com.project.alm.Utilities
import com.cloudbees.groovy.cps.NonCPS
/**
 *
 * Checks if there are snapshot or rc dependencies in the pom.xml to guarantee no
 * RC version is created with SNAPSHOT dependencies and no
 * RELEASE version is created with either SNAPSHOT or RC dependencies
 *
 * @param pomXmlStructure the pomXml details
 * @param pipeline the pipeline details
 *
 * throws an error if dependencies are not correct
 */
def call(PomXmlStructure pomXmlStructure, PipelineData pipeline) {

    try {
        printOpen("Validating dependencies version for ${pomXmlStructure.artifactName} ...", EchoLevel.INFO)

        boolean hasSnapshotDependencies = false
        boolean hasRCDependencies = false
        List<String> snapshotsOrRcVersions = new ArrayList<>()

        if (pomXmlStructure.artifactType == ArtifactType.SIMPLE) {

            printOpen("Project is SIMPLE, processing dependencies version for single module", EchoLevel.DEBUG)

            def pomXml = readFile('pom.xml')

            def validationResult = validateDependencies(pomXml)

            printOpen("validationResult: ${validationResult}", EchoLevel.ALL)

            hasSnapshotDependencies = validationResult.hasSnapshots
            hasRCDependencies = validationResult.hasReleaseCandidates
            snapshotsOrRcVersions = validationResult.snapshotsOrRcVersions

        } else {

            printOpen("Project is AGGREGATOR, processing dependencies version for each module", EchoLevel.DEBUG)

            pomXmlStructure.moduleNames.each {

                printOpen("Processing module: ${it}", EchoLevel.ALL)

                def modulePomXml = readFile("${it}/pom.xml")

                def validationResult = validateDependencies(modulePomXml)

                printOpen("validationResult for ${it}: ${validationResult}", EchoLevel.INFO)

                hasSnapshotDependencies = hasSnapshotDependencies || validationResult.hasSnapshots
                hasRCDependencies = hasRCDependencies || validationResult.hasReleaseCandidates
                snapshotsOrRcVersions.addAll(validationResult.snapshotsOrRcVersions)
            }

            printOpen("Dependencies validation: hasSnapshots? ${hasSnapshotDependencies}", EchoLevel.DEBUG)
            printOpen("Dependencies validation: hasRCs? ${hasRCDependencies}", EchoLevel.DEBUG)
            printOpen("Dependencies validation: incorrect versions? ${snapshotsOrRcVersions}", EchoLevel.DEBUG)

        }

		if("true".equals(GlobalVars.ABSIS3_SERVICES_SKIP_VALIDATION_DEPENDENCIES_ALL) || isInExclusionList(pomXmlStructure.artifactName)) {
			printOpen("Skipping dependencies validation for ${pomXmlStructure.artifactName}", EchoLevel.INFO)
		} else {
	        if (hasSnapshotDependencies && (pipeline.isCreateRC() || pipeline.isCIReleaseBranch())) {
	            printOpen("You cannot build a Release Candidate with SNAPSHOT versions in your pom.xml", EchoLevel.ERROR)
	            printOpen("Versions found: ${snapshotsOrRcVersions}", EchoLevel.ERROR)
	            throw new RuntimeException("pom.xml contains dependencies with SNAPSHOT versions, fix it and try again")
	        } else if ((hasRCDependencies || hasSnapshotDependencies) && pipeline.isCreateRelease()) {
	            printOpen("You cannot build a Release with SNAPSHOT and/or Release Candidate versions in your pom.xml", EchoLevel.ERROR)
	            printOpen("Versions found: ${snapshotsOrRcVersions}", EchoLevel.ERROR)
	            throw new RuntimeException("pom.xml contains dependencies with SNAPSHOTS or RC versions, fix it and try again")
	        }
            printOpen("The dependencies are OK", EchoLevel.INFO)
		}


    } catch (err) {
        printOpen(Utilities.prettyException(err), EchoLevel.ERROR)
        throw err

    }
}

def isInExclusionList(String component) {
	printOpen("List of skipped components", EchoLevel.DEBUG)
	def exclusionList = GlobalVars.ABSIS3_SERVICES_SKIP_VALIDATION_DEPENDENCIES_LIST.split(";")
	printOpen("La lista de exclusiones es la siguiente ${Arrays.asList(exclusionList)}", EchoLevel.DEBUG)
	printOpen("La lista contiene el valor ${component} ${Arrays.asList(exclusionList).contains(component)}", EchoLevel.DEBUG)
	
	return Arrays.asList(exclusionList).contains(component)
}

@NonCPS
def validateDependencies(String pomXmlString) {

    PomVersionsValidator validator = new PomVersionsValidator()
    def project = new XmlSlurper().parseText(pomXmlString)

    return validator.validateDependencies(project)

}
