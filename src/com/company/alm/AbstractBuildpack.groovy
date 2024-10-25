package com.caixabank.absis3

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

