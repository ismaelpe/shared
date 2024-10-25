package com.project.alm

class ChangelogFile {

    public static String FEATURE_PREFIX = " * "
    public static String VERSION_LINE = "## Version: "
    public static String END_LINE = "\n"
    public static String TITLE = "# Changelog file: "
    public static Comparator VERSION_COMPARATOR = { a, b ->
        def a1 = a.version.tokenize('.')*.toInteger(), b1 = b.version.tokenize('.')*.toInteger()
        for (i in 0..<[b1.size(), a1.size()].min())
            if (b1[i] != a1[i]) return b1[i] <=> a1[i]
        0
    }

    private String artifact;
    private String textFile;

    //Lista en orden
    //LinkedHashMap to know if the feature exists
    def changeLogVersionsMap = [:]
    def changeLogVersions = []

    public ChangelogFile(String artifact, String textFile) {
        this.textFile = textFile;
        this.artifact = artifact;
    }

    public void initialize() {
        String[] lines = textFile.split(END_LINE)
        boolean parsingVersion = true
        String currentVersionValue
        ChangelogVersion currentVersion
        for (String line in lines) {
            if (isVersionLine(line)) {
                currentVersionValue = parseVersionLine(line)
                currentVersion = new ChangelogVersion(currentVersionValue)
                changeLogVersionsMap.put(currentVersionValue, currentVersion)
                changeLogVersions.add(currentVersion)
            } else if (ChangelogFeature.isFeatureLine(line)) {
                currentVersion.addFeature(line)
            }
        }
    }

    public boolean addFeature(String version, String feature, String title, String description) {
        ChangelogVersion currentVersion = null
        currentVersion = changeLogVersionsMap.get(version)
        if (currentVersion == null) {
            currentVersion = new ChangelogVersion(version)
            changeLogVersionsMap.put(version, currentVersion)
            changeLogVersions.add(currentVersion)
        }

        return currentVersion.addFeature(feature, title, description);
    }

    private boolean isVersionLine(String line) {
        return line.contains(VERSION_LINE)
    }

    private String parseVersionLine(String line) {
        return line.replace(VERSION_LINE, "");
    }

    @Override
    public String toString() {
        String result = ChangelogFile.TITLE + this.artifact + ChangelogFile.END_LINE
        Collections.sort(changeLogVersions, VERSION_COMPARATOR)
        for (ChangelogVersion version in changeLogVersions) {
            result = result + version.toString()
        }
        return result
    }


}

