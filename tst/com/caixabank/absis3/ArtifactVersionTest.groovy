package com.caixabank.absis3

import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class ArtifactVersionTest extends GroovyTestCase {

    @Test
    void testGenerateSnapshotVersionFromString() {

        ArtifactVersion artifactVersion = new ArtifactVersion("1.2.3-SNAPSHOT")

        assertNotNull(artifactVersion)

        assertEquals(1, artifactVersion.getMajor())
        assertEquals(2, artifactVersion.getMinor())
        assertEquals(3, artifactVersion.getPatch())
        assertEquals(BuildType.SNAPSHOT, artifactVersion.getBuildType())
        assertNull(artifactVersion.getBuildTypeVersion())

        assertEquals("1.2.3-SNAPSHOT", artifactVersion.toString())
    }

    @Test
    void testGenerateSnapshotVersionFromUntrimmedString() {

        ArtifactVersion artifactVersion = new ArtifactVersion("    1.2.3-SNAPSHOT    ")

        assertNotNull(artifactVersion)

        assertEquals(1, artifactVersion.getMajor())
        assertEquals(2, artifactVersion.getMinor())
        assertEquals(3, artifactVersion.getPatch())
        assertEquals(BuildType.SNAPSHOT, artifactVersion.getBuildType())
        assertNull(artifactVersion.getBuildTypeVersion())

        assertEquals("1.2.3-SNAPSHOT", artifactVersion.toString())
    }

    @Test
    void testGenerateSnapshotVersionFromUntrimmedStringWithLineJumps() {

        ArtifactVersion artifactVersion = new ArtifactVersion("    1.2.3-SNAPSHOT  \n  ")

        assertNotNull(artifactVersion)

        assertEquals(1, artifactVersion.getMajor())
        assertEquals(2, artifactVersion.getMinor())
        assertEquals(3, artifactVersion.getPatch())
        assertEquals(BuildType.SNAPSHOT, artifactVersion.getBuildType())
        assertNull(artifactVersion.getBuildTypeVersion())

        assertEquals("1.2.3-SNAPSHOT", artifactVersion.toString())
    }

    @Test
    void testGenerateMilestoneVersionFromString() {

        ArtifactVersion artifactVersion = new ArtifactVersion("1.2.3-M23")

        assertNotNull(artifactVersion)

        assertEquals(1, artifactVersion.getMajor())
        assertEquals(2, artifactVersion.getMinor())
        assertEquals(3, artifactVersion.getPatch())
        assertEquals(BuildType.MILESTONE, artifactVersion.getBuildType())
        assertEquals(23, artifactVersion.getBuildTypeVersion())

        assertEquals("1.2.3-M23", artifactVersion.toString())
    }

    @Test
    void testGenerateReleaseCandidateVersionFromString() {

        ArtifactVersion artifactVersion = new ArtifactVersion("1.2.3-RC42")

        assertNotNull(artifactVersion)

        assertEquals(1, artifactVersion.getMajor())
        assertEquals(2, artifactVersion.getMinor())
        assertEquals(3, artifactVersion.getPatch())
        assertEquals(BuildType.RELEASE_CANDIDATE, artifactVersion.getBuildType())
        assertEquals(42, artifactVersion.getBuildTypeVersion())

        assertEquals("1.2.3-RC42", artifactVersion.toString())
    }

    @Test
    void testGenerateFinalVersionFromString() {

        ArtifactVersion artifactVersion = new ArtifactVersion("1.2.3")

        assertNotNull(artifactVersion)

        assertEquals(1, artifactVersion.getMajor())
        assertEquals(2, artifactVersion.getMinor())
        assertEquals(3, artifactVersion.getPatch())
        assertEquals(BuildType.FINAL, artifactVersion.getBuildType())
        assertNull(artifactVersion.getBuildTypeVersion())

        assertEquals("1.2.3", artifactVersion.toString())
    }

    @Test
    void testGenerateDeprecatedVersionFromString() {

        ArtifactVersion artifactVersion = new ArtifactVersion("1.2.3-DEPRECATED")

        assertNotNull(artifactVersion)

        assertEquals(1, artifactVersion.getMajor())
        assertEquals(2, artifactVersion.getMinor())
        assertEquals(3, artifactVersion.getPatch())
        assertEquals(BuildType.DEPRECATED, artifactVersion.getBuildType())
        assertNull(artifactVersion.getBuildTypeVersion())

        assertEquals("1.2.3-DEPRECATED", artifactVersion.toString())
    }

    @Test
    void testIncrementSnapshotMajorVersion() {

        ArtifactVersion artifactVersion = new ArtifactVersion("1.2.3-SNAPSHOT").incrementMajorVersion()

        assertNotNull(artifactVersion)

        assertEquals(2, artifactVersion.getMajor())
        assertEquals(0, artifactVersion.getMinor())
        assertEquals(0, artifactVersion.getPatch())
        assertEquals(BuildType.SNAPSHOT, artifactVersion.getBuildType())
        assertNull(artifactVersion.getBuildTypeVersion())

        assertEquals("2.0.0-SNAPSHOT", artifactVersion.toString())
    }

    @Test
    void testIncrementReleaseCandidateMinorVersion() {

        ArtifactVersion artifactVersion = new ArtifactVersion("1.2.3-RC85").incrementMinorVersion()

        assertNotNull(artifactVersion)

        assertEquals(1, artifactVersion.getMajor())
        assertEquals(3, artifactVersion.getMinor())
        assertEquals(0, artifactVersion.getPatch())
        assertEquals(BuildType.RELEASE_CANDIDATE, artifactVersion.getBuildType())
        assertEquals(85, artifactVersion.getBuildTypeVersion())

        assertEquals("1.3.0-RC85", artifactVersion.toString())
    }

    @Test
    void testIncrementFinalPatchVersion() {

        ArtifactVersion artifactVersion = new ArtifactVersion("1.2.3").incrementPatchVersion()

        assertNotNull(artifactVersion)

        assertEquals(1, artifactVersion.getMajor())
        assertEquals(2, artifactVersion.getMinor())
        assertEquals(4, artifactVersion.getPatch())
        assertEquals(BuildType.FINAL, artifactVersion.getBuildType())
        assertNull(artifactVersion.getBuildTypeVersion())

        assertEquals("1.2.4", artifactVersion.toString())
    }

    @Test
    void testIncrementMilestoneSubTypeVersion() {

        ArtifactVersion artifactVersion = new ArtifactVersion("1.2.3-M4").incrementSubTypeVersion()

        assertNotNull(artifactVersion)

        assertEquals(1, artifactVersion.getMajor())
        assertEquals(2, artifactVersion.getMinor())
        assertEquals(3, artifactVersion.getPatch())
        assertEquals(BuildType.MILESTONE, artifactVersion.getBuildType())
        assertEquals(5, artifactVersion.getBuildTypeVersion())

        assertEquals("1.2.3-M5", artifactVersion.toString())
    }
}
