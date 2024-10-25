package com.caixabank.absis3

class MavenVersionUtilities {
	
	public static final String VERSION_TOKEN = ".";

    public static boolean isRelease(String version) {
        return !isRCVersion(version) && !isSNAPSHOT(version)
    }

    public static boolean isRCVersion(String version) {
        if (version.indexOf("-RC") != -1) return true
        else return false
    }

    public static boolean isSNAPSHOT(String version) {
        return version.contains('SNAPSHOT')
    }

    public static boolean isHOTFIX(String version) {
        return getArtifactFixVersion(version) != 0
    }

    public static String getArtifactVersionQualifier(String version) {
        int index = version.indexOf('-')
        return index < 0 ? "" : version.substring(index + 1)
    }

    public static String getArtifactMajorVersion(String version) {

        int index = version.indexOf('.')

        if (index == -1) return version
        else return version.substring(0, index)
    }

    public static String getArtifactMinorVersion(String version) {

        String versionWithoutQualifier = getArtifactVersionWithoutQualifier(version);
        int index = versionWithoutQualifier.indexOf('.')

        if (index == -1) return "0"
        else {
            int index1 = versionWithoutQualifier.indexOf('.', index + 1)
            if (index1 == -1) return versionWithoutQualifier.substring(index + 1)
            else return versionWithoutQualifier.substring(index + 1, index1)
        }
    }

    public static String getArtifactFixVersion(String version) {
        String versionWithoutQualifier = getArtifactVersionWithoutQualifier(version);
        int index = versionWithoutQualifier.indexOf('.')

        if (index == -1) return "0"
        else {
            int index1 = versionWithoutQualifier.indexOf('.', index + 1)
            if (index1 == -1) return "0"
            else {
                return versionWithoutQualifier.substring(index1 + 1)
            }
        }
    }

    public static String getArtifactVersionWithoutQualifier(String version) {
        String majorVersion

        int index = version.indexOf('-')
        if (index == -1) return version
        return version.substring(0, index)
    }

    public static IncVersionResult incRC(String artifactVersion, String revision) {
        //La Version debe ser X.Y.Z-RCN
        //Donde N es el numero de version
        //Deberemos incrementer en +1 la version

        IncVersionResult result = new IncVersionResult()

        int rcVersionIndex = artifactVersion.indexOf('-')
        if (rcVersionIndex == -1) throw new Exception("Error it isn't a Version")
        else {
            String subRcVersion = artifactVersion.substring(rcVersionIndex + 1)
            if (subRcVersion.startsWith("RC")) {
                result.oldVersion = artifactVersion
                String rvVersionString = subRcVersion.substring(2)
                int rcVersion = 0
                if (rvVersionString.size() > 0)
                    rcVersion = Integer.parseInt(rvVersionString)
                rcVersion += 1
                //Tenemos ya la nueva version... ahora tenemos que actualizar la version de la RC
                result.artifactVersion = artifactVersion.substring(0, rcVersionIndex) + "-RC" + rcVersion
                if (revision) {
                    result.revision = result.artifactVersion
                }
            } else throw new Exception("Error it isn't a Version")
        }
        return result;
    }

    public static IncVersionResult incMinor(String version, String revision) {
        //La Version debe ser X.Y.Z-RCN
        //La siguiente debe ser X.Y+1.0-SNAPSHOT

        IncVersionResult result = new IncVersionResult()
        result.oldVersion = version
        version = getArtifactVersionWithoutQualifier(version)
        String major = null
        String minor = null

        version.tokenize('.').each {
            if (major == null) major = it
            else if (minor == null) minor = it
        }
        if (major != null && minor != null) {
            int minorValue = Integer.parseInt(minor) + 1
            result.artifactVersion = major + "." + minorValue + '.0-SNAPSHOT'
            if (revision) {
                result.revision = result.artifactVersion
            }
        }
        return result
    }

    static String nextVersion(String version) {
        def isRC = version.indexOf("-RC") != -1
        if (isRC) {
            return incRC(version)
        } else {
            return incMinor(version)
        }
    }

    static String incMajor(String version) {
        def qfIdx = version.indexOf("-")
        def hasQualifier = qfIdx != -1
        def qualifier = hasQualifier ? version.substring(qfIdx) : ""
        def rawVersion = version.substring(0, hasQualifier ? qfIdx : version.length())
        def tokenizedVersion = rawVersion.tokenize(".")
        return "${Integer.parseInt(tokenizedVersion[0]) + 1}.0.0${qualifier}"
    }

    static String incMinor(String version) {
        def qfIdx = version.indexOf("-")
        def hasQualifier = qfIdx != -1
        def qualifier = hasQualifier ? version.substring(qfIdx) : ""
        def rawVersion = version.substring(0, hasQualifier ? qfIdx : version.length())
        def tokenizedVersion = rawVersion.tokenize(".")
        return "${tokenizedVersion[0]}.${Integer.parseInt(tokenizedVersion[1]) + 1}.0${qualifier}"
    }

    static String incPatch(String version) {
        def qfIdx = version.indexOf("-")
        def hasQualifier = qfIdx != -1
        def qualifier = hasQualifier ? version.substring(qfIdx) : ""
        def rawVersion = version.substring(0, hasQualifier ? qfIdx : version.length())
        def tokenizedVersion = rawVersion.tokenize(".")
        return "${tokenizedVersion[0]}.${tokenizedVersion[1]}.${Integer.parseInt(tokenizedVersion[2]) + 1}${qualifier}"
    }

    static String incRC(String version) {
        def idx = version.indexOf("-RC")
        def isNotRC = idx == -1
        if (isNotRC) {
            throw new Exception("Version ${version} is not an RC. Cannot increase")
        }
        def currentVersion = version.substring(idx + 3)
        def newVersion = Integer.parseInt(currentVersion) + 1
        return version.replace("-RC" + currentVersion, "-RC" + newVersion)
    }

    static String getMajor(String version) {
        return getTokenizedVersion(version)[0]
    }

    static String getMinor(String version) {
        return getTokenizedVersion(version)[1]
    }

    static String getPatch(String version) {
        return getTokenizedVersion(version)[2]
    }

    static String getQualifier(String version) {
        return getTokenizedVersion(version)[3]
    }

    static String[] getTokenizedVersion(String version) {
        def qfIdx = version.indexOf("-")
        def hasQualifier = qfIdx != -1
        def qualifier = hasQualifier ? version.substring(qfIdx + 1) : ""
        def rawVersion = version.substring(0, hasQualifier ? qfIdx : version.length())
        def tokenizedVersion = rawVersion.split("\\.")
        return tokenizedVersion + qualifier
    }

}
