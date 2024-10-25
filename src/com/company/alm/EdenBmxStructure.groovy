package com.project.alm

import com.project.alm.GlobalVars

class EdenBmxStructure extends BmxStructure {


    public EdenBmxStructure() {
        //String url_int,String url_ext,String environment,String org_Cdp1, String org_Cdp2, String space, String console_Admin_Cdp1, String console_Admin_Cdp2, boolean hasCenter2
        super(GlobalVars.DOMAIN_INT_URL_TST,
                GlobalVars.DOMAIN_EXT_URL_TST,
                GlobalVars.DOMAIN_INT_CENTER_URL_TST,
                GlobalVars.EDEN_ENVIRONMENT,
                GlobalVars.BMX_TST_ORG_CD1,
                GlobalVars.BMX_TST_ORG_CD2,
                GlobalVars.BMX_EDEN_SPACE,
                GlobalVars.blueMixUrl_CD1_TST,
                GlobalVars.blueMixUrl_CD2_TST,
                true)
    }


}
