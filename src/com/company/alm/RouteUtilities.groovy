package com.project.alm

import com.project.alm.Utilities
import com.project.alm.GlobalVars
import com.project.alm.AppCloud

class RouteUtilities {

    static boolean isOnlyBetaUrls(String mappedRoutes) {
        boolean onlyBeta = true
        List currentRoutes = Utilities.splitStringToListWithSplitter(mappedRoutes, ',')
        currentRoutes.each {
            onlyBeta = onlyBeta && it.toUpperCase().contains(GlobalVars.BETA_COMPONENT_SUFFIX.replace("<componentName>", "").toUpperCase())
        }
        return onlyBeta;
    }

    /**
     * Comprueba si el listado de rutas de un objeto una de ellas es la ruta principal (est� dando servicio)
     */
    static boolean isRouteAlive(String aliveRoute, String mappedRoutes) {
        boolean isAlive = false
        if (mappedRoutes != '' && aliveRoute) {
            List currentRoutes = Utilities.splitStringToListWithSplitter(mappedRoutes, ',')
            currentRoutes.each {
                isAlive = isAlive || it.toUpperCase().contains(aliveRoute.toUpperCase())
            }
        }
        return isAlive;
    }

    //service-manager-micro-server-1            started             1/1          1G        1G      service-manager-micro-server-1-beta.pro.ext.srv.project.com, service-manager-micro-server-1-beta.pro.int.srv.project.com, service-manager-micro-server-1-beta.pro1.int.srv.project.com
    static AppCloud getAppInfo(String appRoute) {
        int column = 0
        AppCloud appCloud = new AppCloud()

        def cDesc = appRoute.tokenize(" ")

        cDesc.each {
            part ->
                if (column == 0) appCloud.app = "${part}"
                else if (column > 4) appCloud.routes += "${part}"
                column++
        }

        return appCloud
    }

}