package com.project.alm

class RevapiStructure {

    String tempDir

    String altTempDir

    String getRevapiPomPath() {
        return tempDir + File.separator + GlobalVars.REVAPI_POM_FILENAME
    }

    String getSwaggerContractPath() {
        return tempDir + File.separator + GlobalVars.SWAGGER_CONTRACT_FILENAME
    }

    String getRemoteSwaggerContractPath() {
        return altTempDir
    }

    String setRemoteSwaggerContractPath(String altTempDir) {
        this.altTempDir = altTempDir
    }

}
