package com.caixabank.absis3

class ChangelogFeature {

    String feature
    String title
    String description

    public ChangelogFeature() {

    }

    public void parseLine(String line) {
        String parsedLine = line.replace(ChangelogFile.FEATURE_PREFIX, "")
        String[] featureInformation = parsedLine.split("-")
        this.feature = featureInformation[0].trim().toUpperCase()
        this.title = featureInformation[1].trim()
        this.description = featureInformation.length == 3 ? featureInformation[2].trim() : ""
    }

    public ChangelogFeature(String feature, String title, String description) {
        this.feature = feature
        this.title = title
        this.description = description
    }


    public static isFeatureLine(String line) {
        return line.startsWith(ChangelogFile.FEATURE_PREFIX)
    }

    @Override
    public String toString() {
        String result = ChangelogFile.FEATURE_PREFIX + feature?.trim() + " - " + title?.trim()
        result += description?.trim() && description?.trim() != "null" ? " - " + description.trim() + ChangelogFile.END_LINE : ChangelogFile.END_LINE
        return result
    }


}
