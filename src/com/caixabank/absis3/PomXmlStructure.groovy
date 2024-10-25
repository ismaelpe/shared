package com.caixabank.absis3

import com.caixabank.absis3.ArtifactSubType
import com.caixabank.absis3.ArtifactType
import com.caixabank.absis3.GarAppType
import com.caixabank.absis3.MavenVersionUtilities
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.Utilities

class PomXmlStructure {

    String applicationName
    String artifactName
    String artifactVersion
    String groupId
    String revision
    String archVersion
    String description = ""

    ArtifactType artifactType
    ArtifactSubType artifactSubType

    String artifactSampleApp = ""
    String artifactLib = ""
    String artifactMicro = ""
    String oldVersion = ""
    String contractPackage = ""
    String contractVersion = ""
    String typeVersion = ""

    List<String> moduleNames = new ArrayList<>()
    String moduleNameWithArchVersionRef
    
    boolean forceJDK8Micro = false

	public PomXmlStructure() {

    }
    
    public PomXmlStructure(ArtifactType artifactType, ArtifactSubType subtype, String artifactMicro, String version, String artifactName) {
        this.artifactMicro = artifactMicro
        this.artifactVersion = version
        this.artifactType = artifactType
        this.artifactSubType = subtype
        this.artifactName = artifactName
    }

    public PomXmlStructure(ArtifactType artifactType, ArtifactSubType subtype, String artifactMicro, String version, String artifactName, String applicationName) {
        this.artifactMicro = artifactMicro
        this.artifactVersion = version
        this.artifactType = artifactType
        this.artifactSubType = subtype
        this.artifactName = artifactName
        this.applicationName = applicationName
    }
	
	public String getMajorVersion() {
		String version = getArtifactVersionWithoutQualifier()

		String major = null

		version.tokenize('.').each {
			if (major == null) major = it
		}
		return major
	}

    public String getApp(GarAppType garAppType) {
        if (applicationName == null) {
            return MavenUtils.sanitizeArtifactName(artifactName, garAppType)
        } else return MavenUtils.sanitizeArtifactName(applicationName, garAppType)
    }

    //Architecture version will not be checked in hotfix branches
    public void validateArtifact(String environment) {

        def skipMinimumVersionValidationList = GlobalVars.ABSIS3_SKIP_MINIMUM_VERSION_VALIDATION_LIST.split(";")

        if(!skipMinimumVersionValidationList.contains(artifactName)){

            String archVersionForTheEnvironment = GlobalVars.getArchVersionForTheEnvironment(environment)

        	if (needsArchVersionUpgrade(archVersionForTheEnvironment)) {
        		throw new Exception("Needs an upgrade of arch version!!! ${archVersionForTheEnvironment}")
        	}

        }
    }


    public void validateArtifact() {
     	def skipMinimumVersionValidationList = GlobalVars.ABSIS3_SKIP_MINIMUM_VERSION_VALIDATION_LIST.split(";")
        if(!skipMinimumVersionValidationList.contains(artifactName)){
       		 if (needsArchVersionUpgrade()) {
       		 	throw new Exception("Needs an upgrade of arch version!!! ${GlobalVars.MINIMUM_VERSION_ARCH}")
       		 }
        }
    }

    private boolean needsArchVersionUpgrade(String archVersionForTheEnvironment) {
        return Utilities.isLowerThan(MavenVersionUtilities.getArtifactVersionWithoutQualifier(archVersion), archVersionForTheEnvironment)
    }

    private boolean needsArchVersionUpgrade() {
        return Utilities.isLowerThan(MavenVersionUtilities.getArtifactVersionWithoutQualifier(archVersion), GlobalVars.MINIMUM_VERSION_ARCH)
    }
	
	
	public boolean lowerThanMinICPArchVersion() {
		return Utilities.isLowerThan(MavenVersionUtilities.getArtifactVersionWithoutQualifier(archVersion), GlobalVars.MINIMUM_VERSION_ARCH_ICP)
	}

    public String getArtifactMajorVersion() {
        return MavenVersionUtilities.getArtifactMajorVersion(artifactVersion)
    }

    public String getArtifactMinorVersion() {
        return MavenVersionUtilities.getArtifactMinorVersion(artifactVersion)
    }


    public String getArtifactFixVersion() {
        return MavenVersionUtilities.getArtifactFixVersion(artifactVersion)
    }
	
	public String getArtifactPreviousFixVersion() {
		int fixVersion = getArtifactFixVersion() as int
		if (fixVersion < 1) {
			throw new Exception("There is no previous fix version (fix = "+fixVersion+").")
		} else {
			return (fixVersion - 1) as String
		}
	}
	
	public String getPreviousFixArtifactVersionWithoutQualifier() {
		return getArtifactMajorVersion() + MavenVersionUtilities.VERSION_TOKEN + getArtifactMinorVersion() + MavenVersionUtilities.VERSION_TOKEN + getArtifactPreviousFixVersion()
	}

    public String getArtifactVersionWithoutQualifier() {
        return MavenVersionUtilities.getArtifactVersionWithoutQualifier(artifactVersion)
    }

    public String getBmxAppId() {
        if ((artifactType == ArtifactType.AGREGADOR)) {
            if (artifactSubType == ArtifactSubType.STARTER || artifactSubType == ArtifactSubType.PLUGIN || artifactSubType == ArtifactSubType.PLUGIN_STARTER || artifactSubType == ArtifactSubType.ARCH_LIB_WITH_SAMPLEAPP  || artifactSubType == ArtifactSubType.ARCH_LIB  || artifactSubType == ArtifactSubType.APP_LIB_WITH_SAMPLEAPP)
                return artifactSampleApp + "-" + getArtifactMajorVersion()
            else return artifactMicro + "-" + getArtifactMajorVersion()
        } else {
            if (artifactSubType == ArtifactSubType.SAMPLE_APP) return artifactSampleApp + "-" + getArtifactMajorVersion()
            else return artifactMicro + "-" + getArtifactMajorVersion()
        }
    }

    public boolean itContainsSampleApp() {
        if ((artifactType == ArtifactType.AGREGADOR)) {
            if (artifactSubType == ArtifactSubType.STARTER || artifactSubType == ArtifactSubType.PLUGIN || artifactSubType == ArtifactSubType.PLUGIN_STARTER || artifactSubType == ArtifactSubType.ARCH_LIB_WITH_SAMPLEAPP  || artifactSubType == ArtifactSubType.ARCH_LIB || artifactSubType == ArtifactSubType.APP_LIB_WITH_SAMPLEAPP)
                return true
            else return false
        } else {
            if (artifactSubType == ArtifactSubType.SAMPLE_APP) return true
            else return false
        }
    }

    public String getArtifactJar() {
        if ((artifactType == ArtifactType.AGREGADOR)) {
            if (artifactSubType == ArtifactSubType.STARTER || artifactSubType == ArtifactSubType.PLUGIN || artifactSubType == ArtifactSubType.PLUGIN_STARTER || artifactSubType == ArtifactSubType.ARCH_LIB_WITH_SAMPLEAPP  || artifactSubType == ArtifactSubType.ARCH_LIB || artifactSubType == ArtifactSubType.APP_LIB_WITH_SAMPLEAPP)
                return artifactSampleApp + "-" + artifactVersion + ".jar"
            else return artifactMicro + "-" + artifactVersion + ".jar"
        } else {
            if (artifactSubType == ArtifactSubType.SAMPLE_APP) return artifactName + "-" + artifactVersion + ".jar"
            else return artifactName + "-" + artifactVersion + ".jar"
        }
    }

    public String getRouteToArtifactJar() {
        if ((artifactType == ArtifactType.AGREGADOR)) {
            if (artifactSubType == ArtifactSubType.STARTER || artifactSubType == ArtifactSubType.PLUGIN || artifactSubType == ArtifactSubType.PLUGIN_STARTER || artifactSubType == ArtifactSubType.ARCH_LIB_WITH_SAMPLEAPP  || artifactSubType == ArtifactSubType.ARCH_LIB || artifactSubType == ArtifactSubType.APP_LIB_WITH_SAMPLEAPP)
                return artifactSampleApp + "/" + "target" + "/" + artifactSampleApp + "-" + artifactVersion + ".jar"
            else return artifactMicro + "/" + "target" + "/" + artifactMicro + "-" + artifactVersion + ".jar"
        } else {
            if (artifactSubType == ArtifactSubType.SAMPLE_APP) return "target/" + artifactName + "-" + artifactVersion + ".jar"
            else return "target/" + artifactName + "-" + artifactVersion + ".jar"
        }
    }


    public String getRouteToSonarReportTask() {
        return "target/sonar/report-task.txt"

    }

    public String getRouteToManifest() {
        if ((artifactType == ArtifactType.AGREGADOR)) {
            if (artifactSubType == ArtifactSubType.STARTER || artifactSubType == ArtifactSubType.PLUGIN || artifactSubType == ArtifactSubType.PLUGIN_STARTER || artifactSubType == ArtifactSubType.ARCH_LIB_WITH_SAMPLEAPP  || artifactSubType == ArtifactSubType.ARCH_LIB || artifactSubType == ArtifactSubType.APP_LIB_WITH_SAMPLEAPP)
                return artifactSampleApp + "/" + "manifest.yml"
            else return artifactMicro + "/" + "manifest.yml"
        } else {
            if (artifactSubType == ArtifactSubType.SAMPLE_APP) return "target/" + artifactName + "-" + artifactVersion + ".jar"
            else return "manifest.yml"
        }
    }

    public boolean isArchArtifact() {
        return GlobalVars.ARCH_ARTIFACT.equals(artifactName)
    }

    

    public String initXmlStructure(String artifactTypeString, String artifactSubTypeString, def project) {
        String resultat = ""

        artifactVersion = project.version.toString()
        groupId = project.groupId.toString()
        artifactName = project.artifactId.toString()
        artifactType = ArtifactType.valueOfType(artifactTypeString)
        artifactSubType = ArtifactSubType.valueOfSubType(artifactSubTypeString)
        description = project.description?.toString()

        if (project?.properties?.revision) {
            revision = project.properties.revision
        }

        if (ArtifactType.AGREGADOR == artifactType) {
            //Este puede tener una sample APP dentro de sus modulos
            def numberModules = project.depthFirst().findAll() { it.name() == 'modules' }

            if (numberModules != null && numberModules.size() > 0) {
                //es un proyecto con modulos dentro de el
                //artifactSampleApp=project.modules.module.find { module ->  module.text().endsWith("-sample-app") }
                project.modules.module.each {
                    if (it.text().endsWith("sample-app")) {
                        artifactSampleApp = it.text()
                    }
                    if (it.text().endsWith("-lib")) {
                        artifactLib = it.text()
                    }
					if (it.text().endsWith("-conf")) {
						artifactLib = it.text()
					}
                    if (it.text().endsWith("-server") || it.text().endsWith("-micro")) {
                        artifactMicro = it.text()
                    }
                    moduleNames.add(it.text())
                }

                if (artifactLib != "") {
                    moduleNameWithArchVersionRef = artifactLib
                }
                if (artifactMicro != "") {
                    moduleNameWithArchVersionRef = artifactMicro
                }
                if (artifactSampleApp != "") {
                    moduleNameWithArchVersionRef = artifactSampleApp
                }

            } else {
                
            }
        } else {
            /*
             MICRO_APP("MICRO_APP"),
             MICRO_ARCH("MICRO_ARCH"),
             PLUGIN_STARTER("PLUGIN_STARTER"),
             PLUGIN_STARTER_SAMPLE_APP("PLUGIN_STARTER_SAMPLE_APP"),
             STARTER("STARTER"),
             ARCH_LIB("ARCH_LIB"),
             APP_LIB("APP_LIB"),
             SAMPLE_APP("SAMPLE_APP"),
             PLUGIN("PLUGIN")
             */
            //Es un componente simple sin modulos por lo que tenemos que ver que tenemos que hacer con el
            if (ArtifactSubType.MICRO_ARCH == artifactSubType)
                artifactMicro = artifactName
            else if (ArtifactSubType.MICRO_APP == artifactSubType) {
                artifactMicro = artifactName
                contractPackage = project.properties[GlobalVars.CONTRACT_PACKAGE_PROP].toString()
                contractPackage =
                        contractPackage.trim()
                                .replaceAll("\n", "")
                                .replaceAll("\t", "")
                                .replaceAll(" ", "")
                contractVersion = project.properties[GlobalVars.CONTRACT_VERSION_PROP].toString().trim()
                
            } else if (ArtifactSubType.SAMPLE_APP == artifactSubType) artifactSampleApp = artifactName
            else {
                artifactLib = artifactName
            }

            if (GlobalVars.ABSIS3_SERVICES_SIMPLIFIED_ALM_WHITELIST.contains(artifactName)) {

                archVersion = GlobalVars.MINIMUM_VERSION_ARCH_PRO

            } else {

                archVersion = project.parent.version

            }

        }

        return resultat

    }


    public String toString() {
        return "\n" +
                "\tartifactName: ${artifactName ? artifactName.toString() : ''}\n" +
                "\tartifactMajorVersion: ${artifactMajorVersion ? artifactMajorVersion.toString() : ''}\n" +
                "\tartifactVersion: ${artifactVersion ? artifactVersion.toString() : ''}\n" +
                "\tartifactMicro: ${artifactMicro ? artifactMicro.toString() : ''}\n" +
                "\tartifactSampleApp: ${artifactSampleApp ? artifactSampleApp.toString() : ''}\n" +
                "\tartifactLib: ${ artifactLib? artifactLib.toString() : ''}\n" +
				"\tartifactType: ${ artifactType? artifactType.toString() : ''}\n" +
				"\tartifactSubType: ${ artifactSubType? artifactSubType.toString() : ''}\n" +
                "\trevision: ${ revision? revision.toString() : ''}\n" +
                "\tarchVersion: ${ archVersion? archVersion.toString() : ''}\n"

    }

    public boolean isRelease() {
        return !isRCVersion() && !isSNAPSHOT()
    }

    public boolean isRCVersion() {
        return MavenVersionUtilities.isRCVersion(artifactVersion)
    }

    public void incRC() {
        //La Version debe ser X.Y.Z-RCN
        //Donde N es el numero de version
        //Deberemos incrementer en +1 la version
        int rcVersionIndex = this.artifactVersion.indexOf('-')
        if (rcVersionIndex == -1) throw new Exception("Error it isn't a Version")
        else {
            String subRcVersion = this.artifactVersion.substring(rcVersionIndex + 1)
            if (subRcVersion.startsWith("RC")) {
                this.oldVersion = this.artifactVersion
                String rvVersionString = subRcVersion.substring(2)
                int rcVersion = 0
                if (rvVersionString.size() > 0)
                    rcVersion = Integer.parseInt(rvVersionString)
                rcVersion += 1
                //Tenemos ya la nueva version... ahora tenemos que actualizar la version de la RC
                this.artifactVersion = this.artifactVersion.substring(0, rcVersionIndex) + "-RC" + rcVersion
                if (this?.revision) {
                    revision = this.artifactVersion
                }
            } else throw new Exception("Error it isn't a Version")
        }
    }

    public void incMinor() {
        //La Version debe ser X.Y.Z-RCN
        //La siguiente debe ser X.Y+1.0-SNAPSHOT
        String version = getArtifactVersionWithoutQualifier()
        String major = null
        String minor = null

        version.tokenize('.').each {
            if (major == null) major = it
            else if (minor == null) minor = it
        }
        if (major != null && minor != null) {
            int minorValue = Integer.parseInt(minor) + 1
            artifactVersion = major + "." + minorValue + '.0-SNAPSHOT'
            if (this?.revision) {
                revision = this.artifactVersion
            }
        }

    }

    public void incFix() {
        //La Version debe ser X.Y.Z-RCN
        //La siguiente debe ser X.Y+1.0-SNAPSHOT
        String version = getArtifactVersionWithoutQualifier()
        String major = null
        String minor = null
        String fix = null

        version.tokenize('.').each {
            if (major == null) major = it
            else if (minor == null) minor = it
            else if (fix == null) fix = it
        }
        if (major != null && minor != null && fix != null) {
            int fixValue = Integer.parseInt(fix) + 1
            artifactVersion = major + "." + minor + "." + fixValue
            //if ( this?.revision ) { revision=this.artifactVersion }
        }

    }

    public boolean isSNAPSHOT() {
        return MavenVersionUtilities.isSNAPSHOT(artifactVersion)
    }

    public boolean isHOTFIX() {
        return MavenVersionUtilities.isHOTFIX(artifactVersion)
    }

    public String getSpringAppName() {
        return this.artifactName + "-" + this.artifactMajorVersion
    }
	
	public String getAppNameWithMajorMinorVersion() {
		return this.artifactName + "-" + this.artifactMajorVersion + "." + this.artifactMinorVersion
	}
	
	public boolean isConfigProject() {
		return this.artifactSubType == ArtifactSubType.ARCH_CFG || this.artifactSubType == ArtifactSubType.SRV_CFG
	}

    public boolean isMicro() {
        return this.artifactSubType == ArtifactSubType.MICRO_APP || this.artifactSubType == ArtifactSubType.MICRO_ARCH || this.isSampleApp()
    }

    public boolean isSampleApp() {
        return this.artifactSubType == ArtifactSubType.SAMPLE_APP || this.artifactSubType == ArtifactSubType.PLUGIN_STARTER_SAMPLE_APP
    }

    public String getArtifactVersionQualifier() {
        return MavenVersionUtilities.getArtifactVersionQualifier(artifactVersion)
    }

    public boolean isApplication() {
        return this.artifactSubType == ArtifactSubType.MICRO_APP || this.artifactSubType == ArtifactSubType.APP_LIB
    }

    public boolean isLibrary() {
        return this.artifactSubType == ArtifactSubType.ARCH_LIB_WITH_SAMPLEAPP || this.artifactSubType == ArtifactSubType.ARCH_LIB || this.artifactSubType == ArtifactSubType.APP_LIB || this.artifactSubType == ArtifactSubType.APP_LIB_WITH_SAMPLEAPP
    }

	public boolean isArchProject() {
		/*
		MICRO_APP("MICRO_APP"),
		MICRO_ARCH("MICRO_ARCH"),
		PLUGIN_STARTER("PLUGIN_STARTER"),
		PLUGIN_STARTER_SAMPLE_APP("PLUGIN_STARTER_SAMPLE_APP"),
		STARTER("STARTER"),
		ARCH_LIB("ARCH_LIB"),
		APP_LIB("APP_LIB"),
		SAMPLE_APP("SAMPLE_APP"),
		PLUGIN("PLUGIN")*/		

		if ( artifactSubType == ArtifactSubType.MICRO_APP || artifactSubType == ArtifactSubType.APP_LIB || this.artifactSubType == ArtifactSubType.APP_LIB_WITH_SAMPLEAPP ) return false
		else return true

	
	}
	
    public String getICPAppName() {
		if (isArchProject()) return GlobalVars.ICP_APP_ARCH
		else return GlobalVars.ICP_APP_APPS
    }


    public String getICPAppId() {
		if (isArchProject()) return GlobalVars.ICP_APP_ID_ARCH
		else return GlobalVars.ICP_APP_ID_APPS
    }
	
	public boolean isApplicationWithNewHealthGroups() {
		boolean isApplicationAtCustomLivenessProbeApplicationsList = GlobalVars.ICP_CUSTOM_LIVENESSPROBE_APPLICATIONS.contains(artifactName)
		boolean isApplicationArchVersionLowerThan1_15 = Utilities.isLowerThan(MavenVersionUtilities.getArtifactVersionWithoutQualifier(archVersion), "1.15.0")
		return isApplicationAtCustomLivenessProbeApplicationsList && !isApplicationArchVersionLowerThan1_15;
	}

}
