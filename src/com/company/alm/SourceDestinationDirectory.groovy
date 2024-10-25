package com.project.alm


class SourceDestinationDirectory implements Serializable {

    String originPath
    String destinationPath

    SourceDestinationDirectory(String originPath, String destinationPath) {
        this.originPath = originPath
        this.destinationPath = destinationPath
    }

}