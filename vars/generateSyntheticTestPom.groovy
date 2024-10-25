import com.caixabank.absis3.*


def call(SyntheticTestStructure syntheticTestStructure) {

    String pathToSyntheticTestPom = CopyGlobalLibraryScript(GlobalVars.SYNTHETIC_TEST_POM_FILENAME, null)

    String sanitizedTempDir = pathToSyntheticTestPom.replace(' ', '\\ ')

    syntheticTestStructure.pathToSyntheticTestPom = pathToSyntheticTestPom

    String pomArtifactId = syntheticTestStructure.pomArtifactId
    String pomVersion = syntheticTestStructure.pomVersion
    String pomGroup = syntheticTestStructure.pomGroup


    sh "sed -i 's/#ARTIFACT#/${pomArtifactId}/g' ${pathToSyntheticTestPom}"
    sh "sed -i 's/#VERSION#/${pomVersion}/g' ${pathToSyntheticTestPom}"
    sh "sed -i 's/#GROUP#/${pomGroup}/g' ${pathToSyntheticTestPom}"


}
