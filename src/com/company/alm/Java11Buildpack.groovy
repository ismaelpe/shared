package com.project.alm

import com.project.alm.AbstractBuildpack
import com.project.alm.GlobalVars

class Java11Buildpack extends AbstractBuildpack {


    public Java11Buildpack() {
        internal = true
        buildpack = GlobalVars.INTERNAL_BUILDPACK_JAVA11
    }

    public String getAlternativeJre() {
        return "manifestEnvJava11.yml"
    }
}

