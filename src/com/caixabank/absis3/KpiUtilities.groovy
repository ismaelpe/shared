package com.caixabank.absis3

import com.caixabank.absis3.GlobalVars

class KpiUtilities {

    static boolean evaluateResponse(def response, KpiRequestStatus statusRequest) {

        statusRequest.iteration = statusRequest.iteration + 1
        //Si es 404 o 200 damos por ok el resultado
        //Existen otros?

        //Evaluar status correcto

        if (response != null && response.status == 200) statusRequest.returnStatus = true
        else if (response != null && response.status == 404) statusRequest.returnStatus = true
        else {
            if (statusRequest.iteration > GlobalVars.KPI_RETRY_POLICY) {                
                statusRequest.returnStatus = true
            } else statusRequest.returnStatus = false
        }
        return statusRequest.returnStatus
    }

}
