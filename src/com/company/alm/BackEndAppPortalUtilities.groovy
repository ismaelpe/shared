package com.project.alm

import groovy.json.JsonSlurper

class BackEndAppPortalUtilities {


	public static String getMostRecentVersionInEnvironment(String majorVersion, String versions, String environment) {
		def parsedResponse = new JsonSlurper().parseText(versions)
		def candidate = null;
		for (Object version : parsedResponse) {
			if(environment.toUpperCase() == version.entorno && majorVersion == version.major){ 
				if(version.tipoVersion =="RC") {
					candidate = version.major + "." + version.minor + "." + version.fix + "-" + version.build
				}else {
					candidate = version.build
				}
			}
		}
		return candidate
	}
	
    public static String getMostRecentVersionInEnvironment(String majorVersion, ArrayList versions, String environment) {
		def parsedResponse = versions
		def candidate = null;
		for (Object version : parsedResponse) {
			if(environment.toUpperCase() == version.entorno && majorVersion == version.major){
				if(version.tipoVersion =="RC") {
					candidate = version.major + "." + version.minor + "." + version.fix + "-" + version.build
				}else {
					candidate = version.build
				}
			}
		}
		return candidate
	}
    public static List<String> getDependeciesNamesByType(String dependeciesJson, String type) {
        List<String> resultList = new ArrayList<String>()

        def parsedResponse = new JsonSlurper().parseText(dependeciesJson)

        for (Object item : parsedResponse) {
            if (item.tipo == type) {
                resultList.add(item.componente)
            }
        }


        return resultList

    }

    public static String getDependeciesJsonFilePath(PomXmlStructure pomXml) {
        String dependenciesPath = GlobalVars.JSON_DEPENDENCIES
        String sourceFolder = dependenciesPath

        if (pomXml.artifactType == ArtifactType.AGREGADOR) {            
            if (pomXml.artifactSubType == ArtifactSubType.MICRO_ARCH) {                
                sourceFolder = pomXml.artifactMicro + "/" + dependenciesPath
            } else if (pomXml.artifactSubType == ArtifactSubType.PLUGIN) {                
                sourceFolder = pomXml.artifactSampleApp + "/" + dependenciesPath
            }
        }
        return sourceFolder


    }

}
