package com.project.alm


class ArtifactFilesPath implements Serializable {

    String subArtifactName
    String originPath
    String destinationFullPath

    ArtifactFilesPath(String subArtifactName, String originPath, String destinationFullPath) {
        this.subArtifactName = subArtifactName
        this.originPath = originPath
        this.destinationFullPath = destinationFullPath
    }

}