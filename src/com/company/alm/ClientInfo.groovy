package com.project.alm

class ClientInfo implements IClientInfo {

    String groupId
    String artifactId
    String artifactVersion
    String applicationName

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
            return MavenUtils.sanitizeArtifactName(artifactId, garAppType)
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

    String toString() {
        return "\n" +
            "\tartifactGroupId: ${groupId}\n" +
            "\tartifactId: ${artifactId}\n" +
            "\tartifactVersion: ${artifactVersion}\n" +
            "\tartifactType: ${artifactType?.toString()}\n" +
            "\tartifactSubType: ${artifactSubType?.toString()}\n"
    }

}
