package com.project.alm

import com.project.alm.GlobalVars

class PreBmxStructure extends BmxStructure {


    public PreBmxStructure() {
        //String url_int,String url_ext,String environment,String org_Cdp1, String org_Cdp2, String space, String console_Admin_Cdp1, String console_Admin_Cdp2, boolean hasCenter2
        super(GlobalVars.DOMAIN_INT_URL_PRE,
                GlobalVars.DOMAIN_EXT_URL_PRE,
                GlobalVars.DOMAIN_INT_CENTER_URL_PRE,
                GlobalVars.PRE_ENVIRONMENT,
                GlobalVars.BMX_PRE_ORG_CD1,
                GlobalVars.BMX_PRE_ORG_CD2,
                GlobalVars.BMX_PRE_SPACE,
                GlobalVars.blueMixUrl_CD1_PRE,
                GlobalVars.blueMixUrl_CD2_PRE,
                true)
    }


}
