package com.caixabank.absis3


class ComponentVersionDocumentationPath implements Serializable {

    String version
    String versionPath

    ComponentVersionDocumentationPath(String version, String versionPath) {
        this.version = version
        this.versionPath = versionPath
    }

}