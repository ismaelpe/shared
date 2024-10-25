package com.caixabank.absis3

class AdsTransactionDefinition {
    String transaction
    String transactionVersion
    String modelFilePath
    String xmlFile
    //Maven variables
    String artifactId

    String version

    AdsTransactionDefinition(String transaction, String transactionVersion, String xmlFile, String modelFilePath, String artifactId, String mavenVersion) {
        this.transaction = transaction
        this.transactionVersion = transactionVersion
        this.modelFilePath = modelFilePath
        this.version = mavenVersion;
        this.artifactId = artifactId
        this.xmlFile = xmlFile
    }


}