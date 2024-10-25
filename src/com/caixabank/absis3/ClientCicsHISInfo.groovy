package com.caixabank.absis3


class ClientCicsHISInfo implements IClientInfo {

    String applicationName
    String artifactName
    String artifactVersion
    String groupId

    ArtifactType artifactType
    ArtifactSubType artifactSubType

    @Override
    String getGroupId() {
        return groupId
    }

    @Override
    String getArtifactId() {
        return artifactId
    }

    @Override
    String getArtifactVersion() {
        return artifactVersion
    }

    @Override
    String getApp(GarAppType garAppType) {
        if (applicationName == null) {
            return sanitizeArtifactName(artifactName, garAppType)
        }
        return applicationName.replace('-', '')
    }

    @Override
    boolean isMicro() {
        return this.artifactSubType == ArtifactSubType.MICRO_APP || this.artifactSubType == ArtifactSubType.MICRO_ARCH || this.isSampleApp()
    }

    @Override
    boolean isSampleApp() {
        return this.artifactSubType == ArtifactSubType.SAMPLE_APP || this.artifactSubType == ArtifactSubType.PLUGIN_STARTER_SAMPLE_APP
    }

    @Override
    boolean isApplication() {
        return this.artifactSubType == ArtifactSubType.MICRO_APP || this.artifactSubType == ArtifactSubType.APP_LIB
    }

    String sanitizeArtifactName(String name, GarAppType garAppType) {

        if (garAppType == GarAppType.ARCH_MICRO) {
            name = name - '-micro'
        } else if (garAppType == GarAppType.ARCH_PLUGIN) {
            name = name - '-lib'
            name = name - '-spring-boot-starter'
            name = name - '-starter'
            name = name - '-plugin'
        } else if (garAppType == GarAppType.ARCH_LIBRARY) {
            name = name - '-lib'
        } else if (garAppType == GarAppType.LIBRARY) {
            name = name - '-lib'
        } else if (garAppType == GarAppType.MICRO_SERVICE) {
            name = name - '-micro'
        } else if (garAppType == GarAppType.DATA_SERVICE) {
            name = name - '-micro'
        }
       
        return name.replace('-', '')
    }

    public ClientCicsHISInfo() {

    }


    public String getArtifactMajorVersion() {
        String majorVersion

        int index = artifactVersion.indexOf('.')

        if (index == -1) return artifactVersion
        else return artifactVersion.substring(0, index)
    }

    public String getArtifactMinorVersion() {

        int index = getArtifactVersionWithoutQualifier().indexOf('.')

        if (index == -1) return "0"
        else {
            int index1 = getArtifactVersionWithoutQualifier().indexOf('.', index + 1)
            if (index1 == -1) return getArtifactVersionWithoutQualifier().substring(index + 1)
            else return getArtifactVersionWithoutQualifier().substring(index + 1, index1)
        }
    }


    public String getArtifactFixVersion() {

        int index = getArtifactVersionWithoutQualifier().indexOf('.')

        if (index == -1) return "0"
        else {
            int index1 = getArtifactVersionWithoutQualifier().indexOf('.', index + 1)
            if (index1 == -1) return "0"
            else {
                return getArtifactVersionWithoutQualifier().substring(index1 + 1)
            }
        }
    }

    public String getArtifactVersionWithoutQualifier() {
        String majorVersion

        int index = artifactVersion.indexOf('-')
        if (index == -1) return artifactVersion
        return artifactVersion.substring(0, index)
    }

     String toString() {
        return "\n" +
                "artifactName: $artifactName\n" +
                "artifactMajorVersion: $artifactMajorVersion\n" +
                "artifactVersion: $artifactVersion\n" +
                "artifactGroupId: $groupId\n"+
				"artifactType: ${artifactType?.toString()}\n"+
				"artifactSubType: ${artifactSubType?.toString()}\n"
    }

    public boolean isRelease() {
        return !isRCVersion() && !isSNAPSHOT()
    }

    public boolean isRCVersion() {
        if (artifactVersion.indexOf("-RC") != -1) return true
        else return false
    }

    public boolean isSNAPSHOT() {
        return this.artifactVersion.contains('SNAPSHOT')
    }

    public String getSpringAppName() {
        return this.artifactName + "-" + this.artifactMajorVersion
    }

    public String getArtifactVersionQualifier() {
        int index = artifactVersion.indexOf('-')
        return index < 0 ? "" : artifactVersion.substring(index + 1)
    }

}
