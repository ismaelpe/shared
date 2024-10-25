package com.caixabank.absis3


class SourceDestinationDirectory implements Serializable {

    String originPath
    String destinationPath

    SourceDestinationDirectory(String originPath, String destinationPath) {
        this.originPath = originPath
        this.destinationPath = destinationPath
    }

}