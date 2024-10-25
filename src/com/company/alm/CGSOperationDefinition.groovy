package com.project.alm

class CGSOperationDefinition {
    String operation
    String operationVersion
    String modelFilePath
    String xmlFileIn
	String xmlFileOut
    //Maven variables
    String artifactId

    String version

    CGSOperationDefinition(String operation, String operationVersion, String xmlFileIn, String xmlFileOut, String modelFilePath, String artifactId, String mavenVersion) {
        this.operation = operation
        this.operationVersion = operationVersion
        this.modelFilePath = modelFilePath
        this.version = mavenVersion;
        this.artifactId = artifactId
        this.xmlFileIn = xmlFileIn
		this.xmlFileOut = xmlFileOut
    }


}
