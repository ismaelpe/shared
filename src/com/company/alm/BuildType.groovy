package com.project.alm

import java.util.regex.Pattern

enum BuildType {

    SNAPSHOT("-SNAPSHOT", versionRegex + "(-SNAPSHOT)"),
    MILESTONE("-M", versionRegex + "(-M)(\\d+)"),
    RELEASE_CANDIDATE("-RC", versionRegex + "(-RC)(\\d+)"),
    FINAL("", versionRegex + "\$"),
    DEPRECATED("-DEPRECATED", versionRegex + "(-DEPRECATED)")

    static final String versionRegex = "(\\d+)(\\.)(\\d+)(\\.)(\\d+)"
    static final String subTypeRegex = "(-[A-Z]+)(\\d+)"

    private String buildSuffix
    private String regex

    private BuildType(String buildSuffix, String regex) {
        this.buildSuffix = buildSuffix
        this.regex = regex
    }

    boolean equalsName(String other) {
        return buildSuffix.equals(other)
    }

    static BuildType valueOfSubType(String other) {
        values().find { it.buildSuffix == other }
    }

    static BuildType valueOfVersion(String version) {
        values().find { version.matches(it.regex) }
    }

    String getBuildSuffix() {
        return this.buildSuffix
    }

    Pattern getPattern() {
        return Pattern.compile(this.regex)
    }
}

