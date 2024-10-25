package com.project.alm

class AbstractBuildpack {

    public boolean internal;
    public String buildpack;

    public ArtifactSubType(String buildpackParam, boolean internalParam) {
        internal = internalParam
        buildpack = buildpackParam

    }

    public String getAlternativeJre() {
        return null
    }

}

