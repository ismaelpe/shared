package com.project.alm

import com.project.alm.GlobalVars
import com.project.alm.DeployStructure

class RunRemoteITBmxStructure extends BmxStructure {


	public RunRemoteITBmxStructure(String url) {
		//String url_int,String url_ext,String environment,String org_Cdp1, String org_Cdp2, String space, String console_Admin_Cdp1, String console_Admin_Cdp2, boolean hasCenter2
		super(url,
				GlobalVars.DOMAIN_EXT_URL_PRO,
				GlobalVars.DOMAIN_INT_CENTER_URL_PRO,
				GlobalVars.PRO_ENVIRONMENT,
				GlobalVars.BMX_PRO_ORG_CD1,
				GlobalVars.BMX_PRO_ORG_CD2,
				GlobalVars.BMX_PRO_SPACE,
				GlobalVars.blueMixUrl_CD1_PRO,
				GlobalVars.blueMixUrl_CD2_PRO,
				true)
	}

	public DeployStructure getDeployStructure(String idCenter) {
		DeployStructure deployStructure = null

		if (idCenter == GlobalVars.BMX_CD1)
			deployStructure = new DeployStructure(this.url_int,
					this.url_ext,
					this.url_cdp.replace("<center>", GlobalVars.BMX_CD1),
					this.environment,
					this.org_Cdp1,
					this.space,
					this.console_Admin_Cdp1,
					idCenter)
		else
			deployStructure = new DeployStructure(this.url_int,
					this.url_ext,
					this.url_cdp.replace("<center>", GlobalVars.BMX_CD2),
					this.environment,
					this.org_Cdp2,
					this.space,
					this.console_Admin_Cdp2,
					idCenter)


		deployStructure.suffixedComponentName = '<componentName>'
		return deployStructure
	}


}
