package com.project.alm


class ComponentVersionDocumentationPath implements Serializable {

    String version
    String versionPath

    ComponentVersionDocumentationPath(String version, String versionPath) {
        this.version = version
        this.versionPath = versionPath
    }

}