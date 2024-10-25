package com.project.alm

class WorkspaceUtils {

    static boolean isThereASwaggerContract(def scriptContext, PomXmlStructure pomXml) {
        String rootFolder = getRootFolderPath(scriptContext.env.WORKSPACE, pomXml)
        def contractPath = "${rootFolder}/contract/swagger-micro-contract.yaml"
        scriptContext.printOpen("Contract path: ${contractPath}", EchoLevel.ALL)
        return scriptContext.fileExists("${contractPath}")

    }
    
    private static String getRootFolderPath(String workspacePath, PomXmlStructure pomXml) {
        String rootFolder = workspacePath
        if (pomXml.artifactType == ArtifactType.AGREGADOR) {
            if (pomXml.artifactSubType == ArtifactSubType.MICRO_ARCH) {
                rootFolder = rootFolder + "/" + pomXml.artifactMicro
            } else if (pomXml.artifactSubType == ArtifactSubType.PLUGIN || pomXml.artifactSubType == ArtifactSubType.STARTER || pomXml.artifactSubType == ArtifactSubType.ARCH_LIB || pomXml.artifactSubType == ArtifactSubType.APP_LIB || pomXml.artifactSubType == ArtifactSubType.APP_LIB_WITH_SAMPLEAPP) {
                rootFolder = rootFolder + "/" + pomXml.artifactSampleApp
            }
        }
        return rootFolder
    }

    static boolean areSwaggerContractClassesGenerated(def scriptContext, PomXmlStructure pomXml) {

		String contractPackage = pomXml.contractPackage
        try {

            if ( ! scriptContext || ! contractPackage ) return false

            String rootFolder = getRootFolderPath(scriptContext.env.WORKSPACE, pomXml)
            contractPackage = contractPackage.replace(".", "/")
            def contractApiPath = "${rootFolder}/src/main/java/${contractPackage}/api/domain"
            scriptContext.printOpen("Contract path: ${contractApiPath}", EchoLevel.ALL)
            def pathExists = scriptContext.fileExists("${contractApiPath}")

            if (pathExists) {

                def numberOfFiles = sh(returnStdout: true, script: "ls ${contractApiPath} | wc -l").trim()
                if (numberOfFiles?.toInteger() > 0) {

                    return true

                }

            }

        } catch(err) {

            // In case of any error in the check, we'll assume is not present
            return false

        }


        return false
    }

}
