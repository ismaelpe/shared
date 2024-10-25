package com.caixabank.absis3

import java.util.regex.Matcher
import java.util.regex.Pattern

//FIXME: You may not use this as you may have CPS serialization issues
class ArtifactVersion {

    static final UNVERSIONED = "UNVERSIONED"

    Integer major
    Integer minor
    Integer patch
    BuildType buildType
    Integer buildTypeVersion

    ArtifactVersion(String version) {

        version = version == null ? "" : version.trim()

        Matcher versionMatcher = Pattern.compile(BuildType.versionRegex).matcher(version)
        if (!versionMatcher.find()) {
            throw new IllegalArgumentException("Invalid version number!")
        }

        this.major = Integer.parseInt(versionMatcher.group(1))
        this.minor = Integer.parseInt(versionMatcher.group(3))
        this.patch = Integer.parseInt(versionMatcher.group(5))

        this.buildType = BuildType.valueOfVersion(version)

        Matcher subTypeMatcher = Pattern.compile(BuildType.subTypeRegex).matcher(version)
        if (isSubVersionedType()) {

            if (!subTypeMatcher.find()) {
                throw new IllegalArgumentException("Invalid subtype version number!")
            }
            this.buildTypeVersion = Integer.parseInt(subTypeMatcher.group(2))

        } else {
            this.buildTypeVersion = null
        }
    }

    ArtifactVersion(Integer major, minor, patch, BuildType buildType, Integer buildTypeVersion) {
        this.major = major
        this.minor = minor
        this.patch = patch
        this.buildType = buildType
        this.buildTypeVersion = buildTypeVersion
        if (!hasVersion()) {
            throw new IllegalArgumentException("BuildType is not consistent!")
        }
    }

    boolean hasVersion() {
        return UNVERSIONED != this.toString()
    }

    boolean isSubVersionedType() {
        return BuildType.MILESTONE == this.buildType || BuildType.RELEASE_CANDIDATE == this.buildType
    }

    String toString() {
        if (this.buildType == null) {
            return UNVERSIONED
        }

        String version = major + "." + minor + "." + patch
        String versionType = this.buildType.getBuildSuffix() + (buildTypeVersion == null ? "" : buildTypeVersion.toString())
        String fullVersion = version + versionType

        Matcher versionMatcher = this.buildType.getPattern().matcher(fullVersion)
        if (versionMatcher.matches()) {
            return fullVersion
        }

        return UNVERSIONED
    }

    ArtifactVersion incrementMajorVersion() {
        return new ArtifactVersion(this.major + 1, 0, 0, buildType, buildTypeVersion)
    }

    ArtifactVersion incrementMinorVersion() {
        return new ArtifactVersion(this.major, this.minor + 1, 0, buildType, buildTypeVersion)
    }

    ArtifactVersion incrementPatchVersion() {
        return new ArtifactVersion(this.major, this.minor, this.patch + 1, buildType, buildTypeVersion)
    }

    ArtifactVersion incrementSubTypeVersion() {
        return new ArtifactVersion(this.major, this.minor, this.patch, buildType, buildTypeVersion + 1)
    }
}
