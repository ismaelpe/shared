package com.caixabank.absis3

import com.caixabank.absis3.AbstractBuildpack
import com.caixabank.absis3.GlobalVars

class Java8Buildpack extends AbstractBuildpack {


    public Java8Buildpack() {
        internal = true
        buildpack = GlobalVars.INTERNAL_BUILDPACK_JAVA8
    }

    public String getAlternativeJre() {
        return "manifestEnvJava8.yml"
    }

}

