package com.caixabank.absis3

import com.caixabank.absis3.AbstractBuildpack
import com.caixabank.absis3.GlobalVars

class Java11Buildpack extends AbstractBuildpack {


    public Java11Buildpack() {
        internal = true
        buildpack = GlobalVars.INTERNAL_BUILDPACK_JAVA11
    }

    public String getAlternativeJre() {
        return "manifestEnvJava11.yml"
    }
}

