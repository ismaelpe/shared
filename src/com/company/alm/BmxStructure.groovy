package com.caixabank.absis3

import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.DeployStructure

class BmxStructure {

    String url_int
    String url_ext
    String url_cdp
    String environment
    String org_Cdp1
    String org_Cdp2
    String space

    String console_Admin_Cdp1
    String console_Admin_Cdp2
    
    boolean hasCenter2


    BmxStructure(String url_int, String url_ext, String url_cdp, String environment, String org_Cdp1, String org_Cdp2, String space,
                 String console_Admin_Cdp1, String console_Admin_Cdp2, boolean hasCenter2) {
        this.url_int = url_int
        this.url_ext = url_ext
        this.url_cdp = url_cdp
        this.environment = environment
        this.org_Cdp1 = org_Cdp1
        this.org_Cdp2 = org_Cdp2
        this.space = space
        this.console_Admin_Cdp1 = console_Admin_Cdp1
        this.console_Admin_Cdp2 = console_Admin_Cdp2
        this.hasCenter2 = hasCenter2
    }

    DeployStructure getDeployStructure(String idCenter) {
        if (idCenter == GlobalVars.BMX_CD1)
            return new DeployStructure(this.url_int,
                    this.url_ext,
                    this.url_cdp.replace("<center>", GlobalVars.BMX_CD1),
                    this.environment,
                    this.org_Cdp1,
                    this.space,
                    this.console_Admin_Cdp1,
                    idCenter)
        else
            return new DeployStructure(this.url_int,
                    this.url_ext,
                    this.url_cdp.replace("<center>", GlobalVars.BMX_CD2),
                    this.environment,
                    this.org_Cdp2,
                    this.space,
                    this.console_Admin_Cdp2,
                    idCenter)
    }

    boolean usesConfigServer() {
        return this.environment == GlobalVars.PRE_ENVIRONMENT || this.environment == GlobalVars.PRO_ENVIRONMENT
    }


    String toString() {
        return "BmxStructure:\n" +
                "\turl_int: $url_int\n" +
                "\turl_ext: $url_ext\n" +
                "\turl_cdp: $url_cdp\n" +
                "\tenvironment: $environment\n" +
                "\torg_Cdp1: $org_Cdp1\n" +
                "\torg_Cdp2: $org_Cdp2\n" +
                "\tspace: $space\n" +
                "\tconsole_Admin_Cdp1: $console_Admin_Cdp1\n" +
                "\tconsole_Admin_Cdp2: $console_Admin_Cdp2\n" +
                "\thasCenter2: $hasCenter2\n"
    }

}
	
	
