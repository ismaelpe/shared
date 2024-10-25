package com.caixabank.absis3

import org.junit.Test

class MavenVersionUtilitiesTest extends GroovyTestCase {

    @Test
    public void testIncrementeRCWithAnArtifactVersion() {
        String artifactVersion = "1.5.0-RC0"
        IncVersionResult result = MavenVersionUtilities.incRC(artifactVersion, null)
        assertEquals("1.5.0-RC1", result.artifactVersion);
    }

    @Test
    void testGivenRCIncrease_returnIncreasedVersion() {
        String artifactVersion = "1.5.1-RC0"
        String result = MavenVersionUtilities.incRC(artifactVersion)
        assertEquals("1.5.1-RC1", result)
    }

    @Test
    void testGivenRCNextVersion_returnIncreasedRCVersion() {
        String artifactVersion = "1.5.1-RC0"
        String result = MavenVersionUtilities.nextVersion(artifactVersion)
        assertEquals("1.5.1-RC1", result)
    }

    @Test
    void testGivenNonRCNextVersion_returnIncreasedMinorVersion() {
        String artifactVersion = "1.5.1"
        String result = MavenVersionUtilities.nextVersion(artifactVersion)
        assertEquals("1.6.0", result)
    }

    @Test
    void testGivenIncreaseMajor_returnIncreasedMajorVersion() {
        String artifactVersion = "1.5.1"
        String result = MavenVersionUtilities.incMajor(artifactVersion)
        assertEquals("2.0.0", result)
    }

    @Test
    void testGivenIncreaseMinor_returnIncreasedMinorVersion() {
        String artifactVersion = "1.5.1"
        String result = MavenVersionUtilities.incMinor(artifactVersion)
        assertEquals("1.6.0", result)
    }

    @Test
    void testGivenIncreasePatch_returnIncreasedPatchVersion() {
        String artifactVersion = "1.5.0"
        String result = MavenVersionUtilities.incPatch(artifactVersion)
        assertEquals("1.5.1", result)
    }

    @Test
    void testGivenRCVersion_returnTokenizedVersion() {
        String artifactVersion = "1.5.0-RC1"
        String[] result = MavenVersionUtilities.getTokenizedVersion(artifactVersion)
        assertArrayEquals(["1","5","0","RC1"] as String[], result)
    }

    @Test
    void testGivenNonRCVersion_returnTokenizedVersion() {
        String artifactVersion = "1.5.0"
        String[] result = MavenVersionUtilities.getTokenizedVersion(artifactVersion)
        assertArrayEquals(["1","5","0",""] as String[], result)
    }

    @Test
    void testGivenNonRCVersion_returnMajorVersion() {
        String artifactVersion = "1.5.0"
        String result = MavenVersionUtilities.getMajor(artifactVersion)
        assertEquals("1", result)
    }

    @Test
    void testGivenNonRCVersion_returnMinorVersion() {
        String artifactVersion = "1.5.0"
        String result = MavenVersionUtilities.getMinor(artifactVersion)
        assertEquals("5", result)
    }

    @Test
    void testGivenNonRCVersion_returnPatchVersion() {
        String artifactVersion = "1.5.0"
        String result = MavenVersionUtilities.getPatch(artifactVersion)
        assertEquals("0", result)
    }

    @Test
    void testGivenSNAPSHOTVersion_returnMajorVersion() {
        String artifactVersion = "1.5.0-SNAPSHOT"
        String result = MavenVersionUtilities.getMajor(artifactVersion)
        assertEquals("1", result)
    }

    @Test
    void testGivenSNAPSHOTVersion_returnMinorVersion() {
        String artifactVersion = "1.5.0-SNAPSHOT"
        String result = MavenVersionUtilities.getMinor(artifactVersion)
        assertEquals("5", result)
    }

    @Test
    void testGivenSNAPSHOTVersion_returnPatchVersion() {
        String artifactVersion = "1.5.0-SNAPSHOT"
        String result = MavenVersionUtilities.getPatch(artifactVersion)
        assertEquals("0", result)
    }

    @Test
    void testGivenRCVersion_returnQualifier() {
        String artifactVersion = "1.5.0-RC1"
        String result = MavenVersionUtilities.getQualifier(artifactVersion)
        assertEquals("RC1", result)
    }

}
