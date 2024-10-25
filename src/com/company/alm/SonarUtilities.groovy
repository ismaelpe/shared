package com.project.alm

import com.project.alm.GlobalVars

class SonarUtilities {

    static boolean evaluateResponse(def response, SonarRequestStatus statusRequest) {

        
        statusRequest.iteration = statusRequest.iteration + 1
        //Si es 404 o 200 damos por ok el resultado
        //Existen otros?

        //Evaluar status correcto

        if (response != null && response == "200") statusRequest.returnStatus = true
        else if (response != null && response == "404") statusRequest.returnStatus = true
        else {
            if (statusRequest.iteration > GlobalVars.SONAR_RETRY_POLICY) {                
                statusRequest.returnStatus = true
            } else statusRequest.returnStatus = false
        }
        return statusRequest.returnStatus
    }


}
