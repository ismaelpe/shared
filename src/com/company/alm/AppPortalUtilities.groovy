package com.project.alm

import com.project.alm.GlobalVars

class AppPortalUtilities {

    static boolean evaluateResponse(def response, AppPortalRequestStatus statusRequest) {

        statusRequest.iteration = statusRequest.iteration + 1
        //Si es 404 o 200 damos por ok el resultado
        //Existen otros?

        //Evaluar status correcto

        if (response != null && response.status == 200) statusRequest.returnStatus = true
        else if (response != null && response.status == 404) statusRequest.returnStatus = true
        else {
            if (statusRequest.iteration > GlobalVars.HTTP_REQUEST_MAX_RETRIES) {                
                statusRequest.returnStatus = true
            } else statusRequest.returnStatus = false
        }
        return statusRequest.returnStatus
    }

}
