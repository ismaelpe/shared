package com.project.alm

class CicsVars {

    static String GPL_APPLICATION_NAME = "cics-client"

    static boolean AGILEWORKS_VALIDATION_ENABLED = true

    static String DEV_ENVIROMENT = "Dsv"
    static String TST_ENVIRONMENT = "Qld"
    static String PRE_ENVIRONMENT = "pre"
    static String PRO_ENVIRONMENT = "Prd"
    static String CLOSE_PIPELINE = "CLOSE"

    static String APP_TYPE = 'SIMPLE'
    static String APP_SUBTYPE = 'APP_LIB'

    static String STARTER_GROUPID = "com.caixabank.absis.apps.cicsclient"


    //Variables fijas. Seguramente esto finalmente se tendr� que indicar por configuracion global
    static String urlFTPs = "ftps://his02.srv.ri.dv.geos.loc/Integration/tiServices/"
    static String urlBaseWsdl = "https://his02.srv.ri.dv.geos.loc/tiServices/"

    static ArrayList urlFTPsTST = ["ftps://ssdtcageohis02.scentrais.gbpi.loc/Integration/tiServices/"]
    static ArrayList urlFTPsPRE = ["ftps://sedtcageohis03.scentrais.gbpi.loc/Integration/tiServices/", "ftps://sedtcageohis04.scentrais.gbpi.loc/Integration/tiServices/"]
    static ArrayList urlFTPsPRO = ["ftps://spdtcageohis03.scentrais.gbpi.loc/Integration/tiServices/", "ftps://spdtcageohis04.scentrais.gbpi.loc/Integration/tiServices/"]

}