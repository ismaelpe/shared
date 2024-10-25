package com.project.alm

import com.project.alm.AbstractBuildpack
import com.project.alm.GlobalVars

class Java8Buildpack extends AbstractBuildpack {


    public Java8Buildpack() {
        internal = true
        buildpack = GlobalVars.INTERNAL_BUILDPACK_JAVA8
    }

    public String getAlternativeJre() {
        return "manifestEnvJava8.yml"
    }

}

