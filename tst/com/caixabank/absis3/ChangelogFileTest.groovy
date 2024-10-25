package com.caixabank.absis3

import org.junit.Test
import groovy.util.GroovyTestCase
import static org.junit.Assert.*
import com.caixabank.absis3.ChangelogFile

class ChangelogFileTest extends GroovyTestCase {


    private ChangelogFile changeLogFile

    private static String OneVersion = "# Changelog file: Artifact\n" +
            "## Version: 1.0.0\n" +
            " * US1212 - TITLE - DESCRIPTION\n"

    private static String TwoFeatures = "# Changelog file: Artifact\n" +
            "## Version: 1.0.0\n" +
            " * US34545 - TITLE2 - DESCRIPTION2\n" +
            " * US1212 - TITLE - DESCRIPTION\n"
    private static String TwoVersion = "# Changelog file: Artifact\n" +
            "## Version: 2.0.0\n" +
            " * US34545 - TITLE2 - DESCRIPTION2\n" +
            "## Version: 1.0.0\n" +
            " * US1212 - TITLE - DESCRIPTION\n"

    private static String ThreeVersion = "# Changelog file: Artifact\n" +
            "## Version: 2.0.0\n" +
            " * US34545 - TITLE2 - DESCRIPTION2\n" +
            "## Version: 1.12.0\n" +
            " * US1212 - TITLE - DESCRIPTION\n" +
            "## Version: 1.0.0\n" +
            " * US1212 - TITLE - DESCRIPTION\n"

    private static String FourVersionUnordered = "# Changelog file: Artifact\n" +
            "## Version: 1.52.4\n" +
            " * US34545 - TITLE1524 - DESCRIPTION\n" +
            "## Version: 10.57.10\n" +
            " * US1212 - TITLE105710 - DESCRIPTION\n" +
            "## Version: 1.41.2\n" +
            " * US1212 - TITLE1412 - DESCRIPTION\n" +
            "## Version: 5.0.4\n" +
            " * US1212 - TITLE504 - DESCRIPTION\n" +
            "## Version: 0.702.25\n" +
            " * US1212 - TITLE070225 - DESCRIPTION\n" +
            "## Version: 1.64.3\n" +
            " * US1212 - TITLE1643 - DESCRIPTION\n"

    private static String FourVersion = "# Changelog file: Artifact\n" +
            "## Version: 10.57.10\n" +
            " * US1212 - TITLE105710 - DESCRIPTION\n" +
            "## Version: 5.0.4\n" +
            " * US1212 - TITLE504 - DESCRIPTION\n" +
            "## Version: 1.64.3\n" +
            " * US1212 - TITLE1643 - DESCRIPTION\n" +
            "## Version: 1.52.4\n" +
            " * US34545 - TITLE1524 - DESCRIPTION\n" +
            "## Version: 1.41.2\n" +
            " * US1212 - TITLE1412 - DESCRIPTION\n" +
            "## Version: 0.702.25\n" +
            " * US1212 - TITLE070225 - DESCRIPTION\n"


    @Test
    public void testGenerateEmptyFile() {
        changeLogFile = new ChangelogFile("Artifact", "");
        changeLogFile.initialize();
        assertToString(changeLogFile.toString(), "# Changelog file: Artifact\n")
    }

    @Test
    public void testGenerateWithOneVersion() {
        changeLogFile = new ChangelogFile("Artifact", "");
        changeLogFile.initialize();
        changeLogFile.addFeature("1.0.0", "US1212", "TITLE", "DESCRIPTION")
        assertToString(changeLogFile.toString(), OneVersion)
    }

    @Test
    public void testGenerateWithTwoFeatures() {
        changeLogFile = new ChangelogFile("Artifact", "");
        changeLogFile.initialize();
        changeLogFile.addFeature("1.0.0", "US1212", "TITLE", "DESCRIPTION")
        changeLogFile.addFeature("1.0.0", "US34545", "TITLE2", "DESCRIPTION2")
        assertToString(changeLogFile.toString(), TwoFeatures)
    }

    @Test
    public void testGenerateWithTwoVersion() {
        changeLogFile = new ChangelogFile("Artifact", "");
        changeLogFile.initialize();
        changeLogFile.addFeature("1.0.0", "US1212", "TITLE", "DESCRIPTION")
        changeLogFile.addFeature("2.0.0", "US34545", "TITLE2", "DESCRIPTION2")
        assertToString(changeLogFile.toString(), TwoVersion)
    }

    @Test
    public void testGenerateWithLoadedFileTwoVersion() {
        changeLogFile = new ChangelogFile("Artifact", TwoVersion);
        changeLogFile.initialize();
        assertToString(changeLogFile.toString(), TwoVersion)
    }

    @Test
    public void testAddRepeatedFeature() {
        changeLogFile = new ChangelogFile("Artifact", "");
        changeLogFile.initialize();
        changeLogFile.addFeature("1.0.0", "US1212", "TITLE", "DESCRIPTION")
        changeLogFile.addFeature("2.0.0", "US34545", "TITLE2", "DESCRIPTION2")
        boolean result = changeLogFile.addFeature("2.0.0", "US34545", "TITLE2", "DESCRIPTION2")
        assertFalse(result);
    }

    @Test
    public void testAddRepeatedFeatureFromScratch() {
        changeLogFile = new ChangelogFile("Artifact", TwoVersion);
        changeLogFile.initialize();
        boolean result = changeLogFile.addFeature("2.0.0", "US34545", "TITLE2", "DESCRIPTION2")
        assertFalse(result);
    }

    @Test
    public void testFourVersionFromScratch() {

        changeLogFile = new ChangelogFile("Artifact", FourVersionUnordered);
        changeLogFile.initialize();

        boolean result = changeLogFile.addFeature("5.0.4", "US1212", "TITLE504", "DESCRIPTION")
        assertToString(changeLogFile.toString(), FourVersion)

        assertFalse(result);
    }

    @Test
    public void testAlwaysTrue() {

        assertToString("true", "true")
    }
}
