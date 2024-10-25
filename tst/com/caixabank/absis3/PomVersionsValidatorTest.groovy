package com.caixabank.absis3

import org.junit.Test

class PomVersionsValidatorTest extends GroovyTestCase {


    private PomVersionsValidator validator = new PomVersionsValidator()

    @Test
    void testValidateVersions_simpleProjectWithSnapshots() {
        def project = new XmlSlurper().parseText(new File("./tst/resources/versions/simpleSnapshots.xml").getText())

        def validationResult = validator.validateDependencies(project)
        assertTrue(validationResult.hasSnapshots)
        assertFalse(validationResult.hasReleaseCandidates)
        assertEquals(["1.0.0-SNAPSHOT"], validationResult.snapshotsOrRcVersions)

    }

    @Test
    void testValidateVersions_simpleProjectWithTimestampedSnapshots() {
        def project = new XmlSlurper().parseText(new File("./tst/resources/versions/simpleSnapshots.xml").getText())

        def validationResult = validator.validateDependencies(project)
        assertTrue(validationResult.hasSnapshots)
        assertFalse(validationResult.hasReleaseCandidates)
        assertEquals(["1.0.0-20210616.101019-1"], validationResult.snapshotsOrRcVersions)

    }

    @Test
    void testValidateVersions_simpleProjectWithRCs() {
        def project = new XmlSlurper().parseText(new File("./tst/resources/versions/simpleRCs.xml").getText())

        def validationResult = validator.validateDependencies(project)
        assertFalse(validationResult.hasSnapshots)
        assertTrue(validationResult.hasReleaseCandidates)
        assertEquals(["1.3.4-RC"], validationResult.snapshotsOrRcVersions)
    }

    @Test
    void testValidateVersions_simpleProjectWithReleases() {
        def project = new XmlSlurper().parseText(new File("./tst/resources/versions/simpleRelease.xml").getText())

        def validationResult = validator.validateDependencies(project)
        assertFalse(validationResult.hasSnapshots)
        assertFalse(validationResult.hasReleaseCandidates)
        assertTrue(validationResult.snapshotsOrRcVersions.isEmpty())

    }

}
