package com.caixabank.absis3

import com.caixabank.absis3.AncientVersionInfo
import com.caixabank.absis3.GlobalVars

class DeployStructure {

    String url_int
    String url_ext
    String url_cdp
    String environment
    String org
    String space
    String console_Admin
    String idCenter
    String suffixedComponentName = GlobalVars.NEW_COMPONENT_PREFIX
    AncientVersionInfo ancientVersion
    String springProfilesActive

    public DeployStructure() {
	
    }

    public DeployStructure(String url_int, String url_ext, String url_cdp, String environment, String org, String space, String console_Admin, String idCenter) {
        this.url_int = url_int
        this.url_ext = url_ext
        this.url_cdp = url_cdp
        this.environment = environment
        this.org = org
        this.space = space
        this.console_Admin = console_Admin
        this.idCenter = idCenter
    }


    public String getEnvVariables(String garAppType, String appName,String appMajorVersion, String domain, String subDomain, String company) {
        String absisBlueGreen = getAbsisBlueGreen()

        return "'\n    ABSIS_APP_ID: " + appName + "'" +
                "'\n    ABSIS_APP_TYPE: " + garAppType + "'" +
                "'\n    ABSIS_CENTER_ID: " + this.idCenter + "'" +
                "'\n    ABSIS_ENVIRONMENT: " + this.environment + "'" +
                "'\n    ABSIS_SPACE: " + this.space + "'" +
                "'\n    ABSIS_APP_DOMAIN: " + domain + "'" +
                "'\n    ABSIS_APP_SUBDOMAIN: " + subDomain + "'" +
                "'\n    ABSIS_APP_COMPANY: " + company + "'" +
                "'\n    http.additionalNonProxyHosts: " + this.url_int + "," + this.url_ext + "," + GlobalVars.HTTP_ADDITIONAL_NON_PROXY_HOSTS + "'" +
				"'\n    ABSIS_BLUE_GREEN: " + absisBlueGreen + "'" +
                "'\n    SPRING_PROFILES_ACTIVE: " + springProfilesActive + "'"

    }
	

	
	public String getAbsisBlueGreen() {
		String absisBlueGreen = "G";
		if (ancientVersion != null && !ancientVersion.notSupported) {
			if (ancientVersion.isBlue) absisBlueGreen = "B"
			else absisBlueGreen = "G"
		}
		return absisBlueGreen;
	}

    public String calculateSpringCloudActiveProfiles(boolean useHealthGroups) {
        String activeProfiles = "cloud,litmid," + this.environment
		activeProfiles += useHealthGroups ? ",healthgroups" : ""
        return activeProfiles.toLowerCase()
    }

    public String calculateSpringCloudActiveProfiles(String garAppType, String company, boolean useHealthGroups) {
        String activeProfiles = calculateSpringCloudActiveProfiles(useHealthGroups)
        boolean isBusinessApplication = GarAppType.MICRO_SERVICE.equalsName(garAppType) || GarAppType.DATA_SERVICE.equalsName(garAppType)
        def garAppTypeProfile = isBusinessApplication ? "app" : "arch"
        activeProfiles += ","+garAppTypeProfile
        activeProfiles += company ? ","+garAppTypeProfile+company : ""
        return activeProfiles.toLowerCase()
    }


    public String getVipaPerCenter() {
        return url_int.subSequence(0, 3) + idCenter + url_int.substring(3)
    }


}
