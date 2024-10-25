package com.project.alm

class ICPUtils {
		
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
	
	
	static ICPAppResources generateICPResources(String memory, String environment, boolean isArchProject ) {
		ICPAppResources icpResources=new ICPAppResources()
		icpResources.environment=environment
		icpResources.isArchProject=isArchProject
		
		if (memory==null || memory=="") memory="768"
		
		icpResources.limitsMemory=memory+"Mi"
		
		return icpResources
	}

    static calculateICPComponentName(PipelineData pipeline, PomXmlStructure pomXml, def parameters = [:]) {

        boolean isBBDD = parameters.isBBDD
		boolean isWiremock = parameters.isWiremock
		boolean isStressMicro = parameters.isStressMicro
        String aplicacion = MavenUtils.sanitizeArtifactName(pomXml.artifactName, pipeline.garArtifactType)

        def nameComponentInICP=aplicacion

        if (!isBBDD)  nameComponentInICP = nameComponentInICP+pomXml.getArtifactMajorVersion()

		if (pipeline.branchStructure.branchType == BranchType.PROTOTYPE) {
			
			//si es prototype lleva postfijo
			nameComponentInICP=nameComponentInICP+"prototype"
			
			   //El elemento sera volatil es decir va a ser borrado en dos dias
		}else if (pipeline.branchStructure.branchType == BranchType.FEATURE && !isBBDD) {  
            String suffixComponent= normalizeICPArtifactName(pipeline.branchStructure.featureNumber)
            if (pomXml.artifactSubType!=ArtifactSubType.MICRO_APP && pomXml.artifactSubType!=ArtifactSubType.MICRO_ARCH) {
                //Es una sample app
                nameComponentInICP=nameComponentInICP+"S"
            }
            if ((nameComponentInICP.length() + suffixComponent.length())>33) {
                suffixComponent=suffixComponent.reverse().take(33-nameComponentInICP.length()).reverse()
            }
            nameComponentInICP=nameComponentInICP + suffixComponent + Utilities.getActualDate("yyyyMMdd")+'E' //Esto nos sirve para indicar que este elemento es del EDEN
        }else {
            if (pomXml.artifactSubType!=ArtifactSubType.MICRO_APP && pomXml.artifactSubType!=ArtifactSubType.MICRO_ARCH) {
                //Es una sample app
                nameComponentInICP=nameComponentInICP+"S"
            }
            if (isBBDD==true) nameComponentInICP=nameComponentInICP+"bbdd"
			if (isWiremock) {
				nameComponentInICP=nameComponentInICP+"wiremock"
				aplicacion=aplicacion+"wiremock"
			}
			if (isStressMicro) {
				nameComponentInICP=nameComponentInICP+"stress"
				aplicacion=aplicacion+"stress"
			}
        }

        return [aplicacion: aplicacion, icpComponentName: nameComponentInICP]

    }

    static String normalizeICPArtifactName(String suffixComponent) {
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
