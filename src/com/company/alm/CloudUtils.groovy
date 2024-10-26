package com.project.alm

class CloudUtils {
		
	static boolean isEdenApp(String name) {
		def matcher = (name =~ /.{0,}\d{8}E\b/)
		return matcher.find()
	}
	
	static boolean isPrototypeApp(String name) {
		return name.endsWith("PROTOTYPE")
	}

    static boolean isWiremockServer(String name) {
		return name.endsWith("WIREMOCK")
	}

    static boolean isStressApp(String name) {
		return name.endsWith("STRESS")
	}
	
	static boolean isSampleApp(String name) {
		def matcher = (name =~ /.{0,}S\b/)
		return matcher.find()
	}
	
	
	static CloudAppResources generateCloudResources(String memory, String environment, boolean isArchProject ) {
		CloudAppResources cloudResources=new CloudAppResources()
		cloudResources.environment=environment
		cloudResources.isArchProject=isArchProject
		
		if (memory==null || memory=="") memory="768"
		
		cloudResources.limitsMemory=memory+"Mi"
		
		return cloudResources
	}

    static calculateCloudComponentName(PipelineData pipeline, PomXmlStructure pomXml, def parameters = [:]) {

        boolean isBBDD = parameters.isBBDD
		boolean isWiremock = parameters.isWiremock
		boolean isStressMicro = parameters.isStressMicro
        String aplicacion = MavenUtils.sanitizeArtifactName(pomXml.artifactName, pipeline.garArtifactType)

        def nameComponentInCloud=aplicacion

        if (!isBBDD)  nameComponentInCloud = nameComponentInCloud+pomXml.getArtifactMajorVersion()

		if (pipeline.branchStructure.branchType == BranchType.PROTOTYPE) {
			
			//si es prototype lleva postfijo
			nameComponentInCloud=nameComponentInCloud+"prototype"
			
			   //El elemento sera volatil es decir va a ser borrado en dos dias
		}else if (pipeline.branchStructure.branchType == BranchType.FEATURE && !isBBDD) {  
            String suffixComponent= normalizeCloudArtifactName(pipeline.branchStructure.featureNumber)
            if (pomXml.artifactSubType!=ArtifactSubType.MICRO_APP && pomXml.artifactSubType!=ArtifactSubType.MICRO_ARCH) {
                //Es una sample app
                nameComponentInCloud=nameComponentInCloud+"S"
            }
            if ((nameComponentInCloud.length() + suffixComponent.length())>33) {
                suffixComponent=suffixComponent.reverse().take(33-nameComponentInCloud.length()).reverse()
            }
            nameComponentInCloud=nameComponentInCloud + suffixComponent + Utilities.getActualDate("yyyyMMdd")+'E' //Esto nos sirve para indicar que este elemento es del EDEN
        }else {
            if (pomXml.artifactSubType!=ArtifactSubType.MICRO_APP && pomXml.artifactSubType!=ArtifactSubType.MICRO_ARCH) {
                //Es una sample app
                nameComponentInCloud=nameComponentInCloud+"S"
            }
            if (isBBDD==true) nameComponentInCloud=nameComponentInCloud+"bbdd"
			if (isWiremock) {
				nameComponentInCloud=nameComponentInCloud+"wiremock"
				aplicacion=aplicacion+"wiremock"
			}
			if (isStressMicro) {
				nameComponentInCloud=nameComponentInCloud+"stress"
				aplicacion=aplicacion+"stress"
			}
        }

        return [aplicacion: aplicacion, cloudComponentName: nameComponentInCloud]

    }

    static String normalizeCloudArtifactName(String suffixComponent) {
        if (suffixComponent!=null) {
            suffixComponent=suffixComponent.toLowerCase()
            suffixComponent=suffixComponent.replace('-','')

            if (suffixComponent.startsWith('us') ||
                suffixComponent.startsWith('ta') ||
                suffixComponent.startsWith('de') ||
                suffixComponent.startsWith('fix')
            ) suffixComponent=suffixComponent.substring(2)

            if (suffixComponent.length()>8) suffixComponent=suffixComponent.substring(0,8)
        }
        return suffixComponent
    }

}
