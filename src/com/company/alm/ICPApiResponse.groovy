package com.caixabank.absis3

import com.caixabank.absis3.DataSourceGenerator


class ICPApiResponse{

	int statusCode
	def body
	
	ICPApiResponse(int httpStatusCode, def responseBody){
		statusCode=httpStatusCode
		body=responseBody
	}
	ICPApiResponse(){
		statusCode=500		
	}
	@NonCPS
    @Deprecated
    //FIXME: Remove this when all pipeline-cps-method-mismatches have been solved for this class
	public String toString() {
        return "\n" +
                "statusCode: $statusCode\n" +
                "body: $body\n"//+
    }

    public String prettyPrint() {
        return "\n" +
            "statusCode: $statusCode\n" +
            "body: $body\n"//+
    }
	
}

